package dev.jcputney.mjml.render;

import dev.jcputney.mjml.MjmlConfiguration;
import dev.jcputney.mjml.MjmlException;
import dev.jcputney.mjml.MjmlRenderException;
import dev.jcputney.mjml.MjmlRenderResult;
import dev.jcputney.mjml.MjmlValidationException;
import dev.jcputney.mjml.component.BaseComponent;
import dev.jcputney.mjml.component.ComponentRegistry;
import dev.jcputney.mjml.component.ContainerComponentFactory;
import dev.jcputney.mjml.component.HeadComponent;
import dev.jcputney.mjml.component.body.MjBody;
import dev.jcputney.mjml.component.body.MjColumn;
import dev.jcputney.mjml.component.body.MjGroup;
import dev.jcputney.mjml.component.body.MjSection;
import dev.jcputney.mjml.component.body.MjWrapper;
import dev.jcputney.mjml.component.content.MjButton;
import dev.jcputney.mjml.component.content.MjDivider;
import dev.jcputney.mjml.component.content.MjImage;
import dev.jcputney.mjml.component.content.MjRaw;
import dev.jcputney.mjml.component.content.MjSpacer;
import dev.jcputney.mjml.component.content.MjTable;
import dev.jcputney.mjml.component.content.MjText;
import dev.jcputney.mjml.component.head.MjAttributes;
import dev.jcputney.mjml.component.head.MjBreakpoint;
import dev.jcputney.mjml.component.head.MjFont;
import dev.jcputney.mjml.component.head.MjHead;
import dev.jcputney.mjml.component.head.MjHtmlAttributes;
import dev.jcputney.mjml.component.head.MjPreview;
import dev.jcputney.mjml.component.head.MjStyle;
import dev.jcputney.mjml.component.head.MjTitle;
import dev.jcputney.mjml.component.interactive.MjAccordion;
import dev.jcputney.mjml.component.interactive.MjAccordionElement;
import dev.jcputney.mjml.component.interactive.MjAccordionText;
import dev.jcputney.mjml.component.interactive.MjAccordionTitle;
import dev.jcputney.mjml.component.interactive.MjCarousel;
import dev.jcputney.mjml.component.interactive.MjCarouselImage;
import dev.jcputney.mjml.component.interactive.MjHero;
import dev.jcputney.mjml.component.interactive.MjNavbar;
import dev.jcputney.mjml.component.interactive.MjNavbarLink;
import dev.jcputney.mjml.component.interactive.MjSocial;
import dev.jcputney.mjml.component.interactive.MjSocialElement;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.css.CssInliner;
import dev.jcputney.mjml.parser.IncludeProcessor;
import dev.jcputney.mjml.parser.MjmlDocument;
import dev.jcputney.mjml.parser.MjmlNode;
import dev.jcputney.mjml.parser.MjmlParser;
import dev.jcputney.mjml.util.CssUnitParser;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Orchestrates the 7-phase rendering pipeline:
 *
 * <ol>
 *   <li>Preprocess (handled by MjmlParser)
 *   <li>Parse (handled by MjmlParser)
 *   <li>Resolve includes (expand mj-include)
 *   <li>Process head (extract fonts, styles, attributes, etc.)
 *   <li>Resolve attributes (cascade applied during rendering)
 *   <li>Render body (top-down component rendering)
 *   <li>Assemble skeleton + CSS inlining
 * </ol>
 */
public final class RenderPipeline {

  private static final Logger LOG = Logger.getLogger(RenderPipeline.class.getName());

  /**
   * Cache of frozen registries keyed by configuration. The registry is stateless once built and
   * frozen, so it can be safely shared across threads and render calls. Uses a synchronized
   * access-ordered LinkedHashMap to provide a bounded LRU-style cache. Keys use MjmlConfiguration's
   * equals/hashCode, which compares value fields by equality and functional fields
   * (includeResolver, contentSanitizer) by reference identity.
   */
  private static final int REGISTRY_CACHE_MAX_SIZE = 256;

  private static final Map<MjmlConfiguration, ComponentRegistry> REGISTRY_CACHE =
      Collections.synchronizedMap(
          new LinkedHashMap<>(REGISTRY_CACHE_MAX_SIZE, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(
                Map.Entry<MjmlConfiguration, ComponentRegistry> eldest) {
              return size() > REGISTRY_CACHE_MAX_SIZE;
            }
          });
  // Pre-compiled patterns for MSO transition merging. Use \s+ between the closing
  // conditional and the opening conditional so that indentation changes don't break the merge.
  private static final Pattern MSO_SECTION_MERGE =
      Pattern.compile(
          "<!--\\[if mso \\| IE]></td></tr></table><!\\[endif]-->\\s+"
              + "<!--\\[if mso \\| IE]><table ");
  private static final Pattern MSO_VML_MERGE =
      Pattern.compile(
          "<!--\\[if mso \\| IE]></v:textbox></v:rect></td></tr></table><!\\[endif]-->\\s+"
              + "<!--\\[if mso \\| IE]><table ");
  private final MjmlConfiguration configuration;
  private final ComponentRegistry registry;
  private final FontScanner fontScanner;

  /**
   * Creates a new render pipeline for the given configuration.
   *
   * @param configuration the MJML configuration controlling rendering behavior
   */
  public RenderPipeline(MjmlConfiguration configuration) {
    this.configuration = configuration;
    this.registry = getOrCreateRegistry(configuration);
    this.fontScanner = new FontScanner(configuration, registry);
  }

  /**
   * Merges adjacent MSO conditional section transitions in the rendered body HTML. When two
   * sections are adjacent, this merges the close/open pattern into a single conditional block. Uses
   * regex with {@code \s+} to tolerate whitespace variations.
   */
  private static String mergeMsoSectionTransitions(String html) {
    html =
        MSO_SECTION_MERGE.matcher(html).replaceAll("<!--[if mso | IE]></td></tr></table><table ");
    html =
        MSO_VML_MERGE
            .matcher(html)
            .replaceAll("<!--[if mso | IE]></v:textbox></v:rect></td></tr></table><table ");
    return html;
  }

  private static void warnUnresolvedIncludes(MjmlNode node) {
    if (node == null) {
      return;
    }
    for (MjmlNode child : node.getChildren()) {
      if ("mj-include".equals(child.getTagName())) {
        String path = child.getAttribute("path", "(unknown)");
        LOG.warning(() -> "Unresolved mj-include remains in tree: " + path);
      } else {
        warnUnresolvedIncludes(child);
      }
    }
  }

  private ComponentRegistry getOrCreateRegistry(MjmlConfiguration config) {
    ComponentRegistry cached = REGISTRY_CACHE.get(config);
    if (cached != null) {
      return cached;
    }
    synchronized (REGISTRY_CACHE) {
      cached = REGISTRY_CACHE.get(config);
      if (cached != null) {
        return cached;
      }
      ComponentRegistry created = createAndFreezeRegistry(config);
      REGISTRY_CACHE.put(config, created);
      return created;
    }
  }

  private ComponentRegistry createAndFreezeRegistry(MjmlConfiguration config) {
    ComponentRegistry reg = createRegistry();
    reg.freeze();
    return reg;
  }

  /**
   * Renders MJML source to a complete HTML document.
   *
   * @param mjmlSource the raw MJML markup to render
   * @return the render result containing the HTML output, title, and preview text
   */
  public MjmlRenderResult render(String mjmlSource) {
    // Validate input size
    int maxSize = configuration.getMaxInputSize();
    if (mjmlSource != null && mjmlSource.length() > maxSize) {
      throw new MjmlValidationException(
          "Input size " + mjmlSource.length() + " characters exceeds maximum allowed " + maxSize);
    }

    LOG.fine("Starting render pipeline");

    // Phase 1 & 2: Preprocess and parse
    MjmlDocument document = MjmlParser.parse(mjmlSource, configuration.getMaxNestingDepth());
    LOG.fine("Parsed MJML document");

    // Create global context
    GlobalContext globalContext = new GlobalContext(configuration);

    // Phase 3: Resolve includes
    if (configuration.getIncludeResolver() != null) {
      IncludeProcessor includeProcessor =
          new IncludeProcessor(
              configuration.getIncludeResolver(),
              configuration.getMaxInputSize(),
              configuration.getMaxIncludeDepth(),
              configuration.getMaxNestingDepth());
      includeProcessor.process(document);
    }

    // Warn about unresolved mj-include nodes
    warnUnresolvedIncludes(document.root());

    try {
      // Phase 4: Process head
      processHead(document, globalContext);

      // Phase 4b: Auto-register default fonts used by components
      fontScanner.registerDefaultFonts(document, globalContext);

      // Phase 5 & 6: Render body (attribute cascade happens during rendering)
      String bodyHtml = renderBody(document, globalContext);

      // Phase 6a: Merge adjacent MSO section transitions
      bodyHtml = mergeMsoSectionTransitions(bodyHtml);

      // Phase 6b: Apply mj-html-attributes to rendered body
      if (!globalContext.attributes().getHtmlAttributes().isEmpty()) {
        bodyHtml = HtmlAttributeApplier.apply(bodyHtml, globalContext);
      }

      // Phase 7: Assemble skeleton
      String html = HtmlSkeleton.assemble(bodyHtml, globalContext);

      // Phase 7b: CSS inlining (inline styles from mj-style inline="inline")
      if (!globalContext.styles().getInlineStyles().isEmpty()) {
        StringBuilder inlineCss = new StringBuilder();
        for (String css : globalContext.styles().getInlineStyles()) {
          inlineCss.append(css).append("\n");
        }
        html = CssInliner.inlineAdditionalOnly(html, inlineCss.toString());
        // Post-processing to match official MJML v4 output exactly.
        // The official MJML toolchain uses juice for CSS inlining, which rewrites
        // all HTML elements through cheerio. This round-trip serialization introduces
        // two side effects in the final output:
        // 1. cheerio serializes empty style attributes without quotes: style="" -> style
        // 2. cheerio serializes self-closing tags without the slash: /> -> >
        // Our CSS inliner preserves the original markup, so we apply these
        // transformations explicitly to match the expected golden output.
        html = html.replace(" style=\"\"", " style");
        html = html.replace(" />", ">");
      }

      return new MjmlRenderResult(
          html, globalContext.metadata().getTitle(), globalContext.metadata().getPreviewText());
    } catch (MjmlException e) {
      throw e;
    } catch (Exception e) {
      throw new MjmlRenderException("Rendering failed: " + e.getMessage(), e);
    }
  }

  private void processHead(MjmlDocument document, GlobalContext globalContext) {
    MjmlNode head = document.getHead();
    if (head == null) {
      return;
    }

    RenderContext dummyContext = new RenderContext(MjmlConfiguration.DEFAULT_CONTAINER_WIDTH);

    for (MjmlNode child : head.getChildren()) {
      if ("#comment".equals(child.getTagName())) {
        String comment = child.getTextContent();
        if (comment != null && !comment.isBlank()) {
          globalContext.metadata().addHeadComment(comment.trim());
        }
        continue;
      }
      if (child.getTagName().startsWith("#")) {
        continue;
      }
      BaseComponent component = registry.createComponent(child, globalContext, dummyContext);
      if (component instanceof HeadComponent headComponent) {
        headComponent.process();
      }
    }
  }

  private String renderBody(MjmlDocument document, GlobalContext globalContext) {
    MjmlNode body = document.getBody();
    if (body == null) {
      return "";
    }

    // Parse body width
    String widthAttr = body.getAttribute("width", MjmlConfiguration.DEFAULT_CONTAINER_WIDTH + "px");
    int containerWidth =
        CssUnitParser.parsePixels(widthAttr, MjmlConfiguration.DEFAULT_CONTAINER_WIDTH);
    globalContext.metadata().setContainerWidth(containerWidth);

    RenderContext renderContext = new RenderContext(containerWidth);

    MjBody mjBody = new MjBody(body, globalContext, renderContext, registry);
    return mjBody.render();
  }

  private ComponentRegistry createRegistry() {
    ComponentRegistry reg = new ComponentRegistry();

    // Head components
    reg.register("mj-head", MjHead::new);
    reg.register("mj-title", MjTitle::new);
    reg.register("mj-preview", MjPreview::new);
    reg.register("mj-font", MjFont::new);
    reg.register("mj-breakpoint", MjBreakpoint::new);
    reg.register("mj-style", MjStyle::new);
    reg.register("mj-attributes", (node, ctx, rctx) -> new MjAttributes(node, ctx, rctx, reg));

    // Head extras
    reg.register("mj-html-attributes", MjHtmlAttributes::new);

    // Body layout components
    reg.register("mj-body", (node, ctx, rctx) -> new MjBody(node, ctx, rctx, reg));
    reg.register("mj-section", (node, ctx, rctx) -> new MjSection(node, ctx, rctx, reg));
    reg.register("mj-column", (node, ctx, rctx) -> new MjColumn(node, ctx, rctx, reg));
    reg.register("mj-group", (node, ctx, rctx) -> new MjGroup(node, ctx, rctx, reg));
    reg.register("mj-wrapper", (node, ctx, rctx) -> new MjWrapper(node, ctx, rctx, reg));

    // Content components
    reg.register("mj-text", MjText::new);
    reg.register("mj-image", MjImage::new);
    reg.register("mj-button", MjButton::new);
    reg.register("mj-divider", MjDivider::new);
    reg.register("mj-spacer", MjSpacer::new);
    reg.register("mj-table", MjTable::new);
    reg.register("mj-raw", MjRaw::new);

    // Interactive components
    reg.register("mj-hero", (node, ctx, rctx) -> new MjHero(node, ctx, rctx, reg));
    reg.register("mj-accordion", (node, ctx, rctx) -> new MjAccordion(node, ctx, rctx, reg));
    reg.register(
        "mj-accordion-element", (node, ctx, rctx) -> new MjAccordionElement(node, ctx, rctx, reg));
    reg.register("mj-accordion-title", MjAccordionTitle::new);
    reg.register("mj-accordion-text", MjAccordionText::new);
    reg.register("mj-carousel", (node, ctx, rctx) -> new MjCarousel(node, ctx, rctx, reg));
    reg.register("mj-carousel-image", MjCarouselImage::new);
    reg.register("mj-navbar", (node, ctx, rctx) -> new MjNavbar(node, ctx, rctx, reg));
    reg.register("mj-navbar-link", MjNavbarLink::new);
    reg.register("mj-social", (node, ctx, rctx) -> new MjSocial(node, ctx, rctx, reg));
    reg.register("mj-social-element", MjSocialElement::new);

    // Register custom components
    for (Map.Entry<String, dev.jcputney.mjml.component.ComponentFactory> entry :
        configuration.getCustomComponents().entrySet()) {
      reg.register(entry.getKey(), entry.getValue());
    }

    // Register custom container components (with registry access)
    for (Map.Entry<String, ContainerComponentFactory> entry :
        configuration.getCustomContainerComponents().entrySet()) {
      reg.register(
          entry.getKey(), (node, ctx, rctx) -> entry.getValue().create(node, ctx, rctx, reg));
    }

    return reg;
  }
}
