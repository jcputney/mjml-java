package dev.jcputney.javamjml.parser;

import dev.jcputney.javamjml.IncludeResolver;
import dev.jcputney.javamjml.MjmlException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Processes mj-include elements in the parsed MJML tree.
 * <p>
 * Supports three include types:
 * <ul>
 *   <li><b>mjml</b> (default): Includes an MJML fragment. The included content's children
 *       replace the mj-include element.</li>
 *   <li><b>html</b>: Includes raw HTML as an mj-raw element.</li>
 *   <li><b>css</b>: Includes CSS as an mj-style element (optionally inline).</li>
 * </ul>
 * <p>
 * Cycle detection prevents infinite recursion from circular includes.
 */
public final class IncludeProcessor {

  private static final int MAX_INCLUDE_DEPTH = 50;

  private final IncludeResolver resolver;

  public IncludeProcessor(IncludeResolver resolver) {
    this.resolver = resolver;
  }

  /**
   * Processes all mj-include elements in the document tree.
   */
  public void process(MjmlDocument document) {
    processNode(document.getRoot(), new HashSet<>(), 0);
  }

  private void processNode(MjmlNode node, Set<String> visitedPaths, int depth) {
    if (node == null) {
      return;
    }

    // Process children in a copy since we may modify the list
    List<MjmlNode> children = new ArrayList<>(node.getChildren());
    for (MjmlNode child : children) {
      if ("mj-include".equals(child.getTagName())) {
        resolveInclude(child, visitedPaths, depth);
      } else {
        processNode(child, visitedPaths, depth);
      }
    }
  }

  private void resolveInclude(MjmlNode includeNode, Set<String> visitedPaths, int depth) {
    if (depth >= MAX_INCLUDE_DEPTH) {
      throw new MjmlException("Maximum include depth exceeded (" + MAX_INCLUDE_DEPTH
          + "). Possible circular include.");
    }

    String path = includeNode.getAttribute("path");
    if (path == null || path.isBlank()) {
      throw new MjmlException("mj-include requires a 'path' attribute");
    }

    // Cycle detection
    if (visitedPaths.contains(path)) {
      throw new MjmlException("Circular include detected: " + path
          + " (include chain: " + visitedPaths + ")");
    }

    String type = includeNode.getAttribute("type", "mjml");
    String content = resolver.resolve(path);

    Set<String> newVisited = new HashSet<>(visitedPaths);
    newVisited.add(path);

    switch (type.toLowerCase()) {
      case "mjml" -> resolveAsMjml(includeNode, content, newVisited, depth);
      case "html" -> resolveAsHtml(includeNode, content);
      case "css" -> resolveAsCss(includeNode, content);
      default -> throw new MjmlException("Unknown mj-include type: " + type);
    }
  }

  private void resolveAsMjml(MjmlNode includeNode, String mjmlContent,
      Set<String> visitedPaths, int depth) {
    // Parse the included MJML fragment
    // The fragment may be a full <mjml> document or just MJML elements
    String wrapped = mjmlContent.trim();
    MjmlNode parsedRoot;

    if (wrapped.startsWith("<mjml")) {
      // Full MJML document - use its body/head children
      MjmlDocument includedDoc = MjmlParser.parse(wrapped);
      parsedRoot = includedDoc.getRoot();

      // Recursively process includes in the included document
      processNode(parsedRoot, visitedPaths, depth + 1);

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
      try {
        MjmlDocument includedDoc = MjmlParser.parse(tempWrapped);
        MjmlNode body = includedDoc.getBody();

        if (body != null) {
          processNode(body, visitedPaths, depth + 1);
          includeNode.replaceWith(new ArrayList<>(body.getChildren()));
        }
      } catch (MjmlException e) {
        // If parsing as body children fails, try as raw content
        MjmlNode rawNode = new MjmlNode("mj-raw");
        MjmlNode textNode = new MjmlNode("#cdata-section");
        textNode.setTextContent(wrapped);
        rawNode.addChild(textNode);
        includeNode.replaceWith(List.of(rawNode));
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
}
