package dev.jcputney.mjml.parser;

import dev.jcputney.mjml.IncludeResolver;
import dev.jcputney.mjml.MjmlIncludeException;
import dev.jcputney.mjml.MjmlValidationException;
import dev.jcputney.mjml.ResolverContext;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Processes mj-include elements in the parsed MJML tree.
 *
 * <p>Supports four include types:
 *
 * <ul>
 *   <li><b>mjml</b> (default): Includes an MJML fragment. The included content's children replace
 *       the mj-include element.
 *   <li><b>html</b>: Includes raw HTML as an mj-raw element.
 *   <li><b>css</b>: Includes CSS as an mj-style element (optionally inline when {@code
 *       css-inline="inline"} attribute is present).
 *   <li><b>css-inline</b>: Shorthand for {@code type="css" css-inline="inline"}. Includes CSS as an
 *       mj-style element with {@code inline="inline"}.
 * </ul>
 *
 * <p>Cycle detection prevents infinite recursion from circular includes.
 */
public final class IncludeProcessor {

  private static final Logger LOG = Logger.getLogger(IncludeProcessor.class.getName());
  private static final int DEFAULT_MAX_INCLUDE_DEPTH = 50;
  private static final int DEFAULT_MAX_NESTING_DEPTH = 100;

  private final IncludeResolver resolver;
  private final int maxInputSize;
  private final int maxIncludeDepth;
  private final int maxNestingDepth;

  /**
   * Creates a processor with default limits for include depth and nesting depth.
   *
   * @param resolver the resolver to use for loading include content
   */
  public IncludeProcessor(IncludeResolver resolver) {
    this(resolver, 0, DEFAULT_MAX_INCLUDE_DEPTH, DEFAULT_MAX_NESTING_DEPTH);
  }

  /**
   * Creates a processor with a size limit for included content.
   *
   * @param resolver the resolver to use for loading include content
   * @param maxInputSize maximum allowed size in characters for resolved content (0 = no limit)
   */
  public IncludeProcessor(IncludeResolver resolver, int maxInputSize) {
    this(resolver, maxInputSize, DEFAULT_MAX_INCLUDE_DEPTH, DEFAULT_MAX_NESTING_DEPTH);
  }

  /**
   * Creates a processor with limits for included content and include depth.
   *
   * @param resolver the resolver to use for loading include content
   * @param maxInputSize maximum allowed size in characters for resolved content (0 = no limit)
   * @param maxIncludeDepth maximum nested include depth before rejecting
   */
  public IncludeProcessor(IncludeResolver resolver, int maxInputSize, int maxIncludeDepth) {
    this(resolver, maxInputSize, maxIncludeDepth, DEFAULT_MAX_NESTING_DEPTH);
  }

  /**
   * Creates a processor with all limits specified.
   *
   * @param resolver the resolver to use for loading include content
   * @param maxInputSize maximum allowed size in characters for resolved content (0 = no limit)
   * @param maxIncludeDepth maximum nested include depth before rejecting
   * @param maxNestingDepth maximum element nesting depth for parsing included content
   */
  public IncludeProcessor(
      IncludeResolver resolver, int maxInputSize, int maxIncludeDepth, int maxNestingDepth) {
    this.resolver = resolver;
    this.maxInputSize = maxInputSize;
    if (maxIncludeDepth <= 0) {
      throw new IllegalArgumentException(
          "maxIncludeDepth must be positive, got: " + maxIncludeDepth);
    }
    this.maxIncludeDepth = maxIncludeDepth;
    this.maxNestingDepth = maxNestingDepth;
  }

  /**
   * Processes all mj-include elements in the document tree, recursively resolving included content
   * and replacing mj-include nodes with the resolved content.
   *
   * @param document the parsed MJML document to process
   */
  public void process(MjmlDocument document) {
    processNode(document.root(), new ArrayList<>(), new HashSet<>(), 0);
  }

  private void processNode(
      MjmlNode node, List<String> includeChain, Set<String> visitedPaths, int depth) {
    if (node == null) {
      return;
    }

    // Process children in a copy since we may modify the list
    List<MjmlNode> children = new ArrayList<>(node.getChildren());
    for (MjmlNode child : children) {
      if ("mj-include".equals(child.getTagName())) {
        resolveInclude(child, includeChain, visitedPaths, depth);
      } else {
        processNode(child, includeChain, visitedPaths, depth);
      }
    }
  }

  private void resolveInclude(
      MjmlNode includeNode, List<String> includeChain, Set<String> visitedPaths, int depth) {
    if (depth >= maxIncludeDepth) {
      throw new MjmlIncludeException(
          "Maximum include depth exceeded (" + maxIncludeDepth + "). Possible circular include.");
    }

    String path = includeNode.getAttribute("path");
    if (path == null || path.isBlank()) {
      throw new MjmlIncludeException("mj-include requires a 'path' attribute");
    }

    // Cycle detection
    if (visitedPaths.contains(path)) {
      throw new MjmlIncludeException("Circular include detected for path: " + path);
    }

    String type = includeNode.getAttribute("type", "mjml");
    LOG.fine(() -> "Resolving mj-include path=" + path + " type=" + type + " depth=" + depth);

    // Build resolver context from the current include chain
    ResolverContext context;
    if (depth == 0) {
      context = ResolverContext.root(type);
    } else {
      String includingPath =
          includeChain.isEmpty() ? null : includeChain.get(includeChain.size() - 1);
      context = new ResolverContext(includingPath, type, depth);
    }

    String content = resolver.resolve(path, context);
    if (maxInputSize > 0 && content != null && content.length() > maxInputSize) {
      throw new MjmlValidationException(
          "Included content from '"
              + path
              + "' exceeds maximum size ("
              + content.length()
              + " > "
              + maxInputSize
              + " characters)");
    }

    visitedPaths.add(path);
    includeChain.add(path);
    try {
      switch (type.toLowerCase()) {
        case "mjml" -> resolveAsMjml(includeNode, content, includeChain, visitedPaths, depth);
        case "html" -> resolveAsHtml(includeNode, content);
        case "css" -> resolveAsCss(includeNode, content);
        case "css-inline" -> resolveAsCssInline(includeNode, content);
        default -> throw new MjmlIncludeException("Unknown mj-include type: " + type);
      }
    } finally {
      visitedPaths.remove(path);
      includeChain.remove(includeChain.size() - 1);
    }
  }

  private void resolveAsMjml(
      MjmlNode includeNode,
      String mjmlContent,
      List<String> includeChain,
      Set<String> visitedPaths,
      int depth) {
    // Parse the included MJML fragment
    // The fragment may be a full <mjml> document or just MJML elements
    String wrapped = mjmlContent.trim();
    MjmlNode parsedRoot;

    if (wrapped.startsWith("<mjml")) {
      // Full MJML document - use its body/head children
      MjmlDocument includedDoc = MjmlParser.parse(wrapped, maxNestingDepth);
      parsedRoot = includedDoc.root();

      // Recursively process includes in the included document
      processNode(parsedRoot, includeChain, visitedPaths, depth + 1);

      // The include should be replaced by the children of the relevant section
      // If we're in mj-head, use head children; if in mj-body, use body children
      MjmlNode parent = includeNode.getParent();
      String parentTag = parent != null ? parent.getTagName() : "";

      List<MjmlNode> replacements = new ArrayList<>();
      if ("mj-head".equals(parentTag) && includedDoc.getHead() != null) {
        replacements.addAll(includedDoc.getHead().getChildren());
      } else if (includedDoc.getBody() != null) {
        replacements.addAll(includedDoc.getBody().getChildren());
      }

      includeNode.replaceWith(replacements);
    } else {
      // Fragment - wrap in a temporary root for parsing
      String tempWrapped = "<mjml><mj-body>" + wrapped + "</mj-body></mjml>";
      MjmlDocument includedDoc = MjmlParser.parse(tempWrapped, maxNestingDepth);
      MjmlNode body = includedDoc.getBody();

      if (body != null) {
        processNode(body, includeChain, visitedPaths, depth + 1);
        includeNode.replaceWith(new ArrayList<>(body.getChildren()));
      }
    }
  }

  private void resolveAsHtml(MjmlNode includeNode, String htmlContent) {
    // Create an mj-raw node with the HTML content
    MjmlNode rawNode = new MjmlNode("mj-raw");
    MjmlNode textNode = new MjmlNode("#cdata-section");
    textNode.setTextContent(htmlContent);
    rawNode.addChild(textNode);
    includeNode.replaceWith(List.of(rawNode));
  }

  private void resolveAsCss(MjmlNode includeNode, String cssContent) {
    // Create an mj-style node with the CSS content
    MjmlNode styleNode = new MjmlNode("mj-style");

    // Check for css-inline attribute
    String cssInline = includeNode.getAttribute("css-inline");
    if ("inline".equals(cssInline)) {
      styleNode.setAttribute("inline", "inline");
    }

    MjmlNode textNode = new MjmlNode("#cdata-section");
    textNode.setTextContent(cssContent);
    styleNode.addChild(textNode);
    includeNode.replaceWith(List.of(styleNode));
  }

  private void resolveAsCssInline(MjmlNode includeNode, String cssContent) {
    // Shorthand for type="css" css-inline="inline"
    MjmlNode styleNode = new MjmlNode("mj-style");
    styleNode.setAttribute("inline", "inline");

    MjmlNode textNode = new MjmlNode("#cdata-section");
    textNode.setTextContent(cssContent);
    styleNode.addChild(textNode);
    includeNode.replaceWith(List.of(styleNode));
  }
}
