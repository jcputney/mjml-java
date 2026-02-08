package dev.jcputney.javamjml.render;

import dev.jcputney.javamjml.MjmlConfiguration;
import dev.jcputney.javamjml.MjmlRenderResult;
import dev.jcputney.javamjml.css.CssInliner;
import dev.jcputney.javamjml.component.BaseComponent;
import dev.jcputney.javamjml.component.BodyComponent;
import dev.jcputney.javamjml.component.ComponentRegistry;
import dev.jcputney.javamjml.component.HeadComponent;
import dev.jcputney.javamjml.component.body.MjBody;
import dev.jcputney.javamjml.component.body.MjColumn;
import dev.jcputney.javamjml.component.body.MjGroup;
import dev.jcputney.javamjml.component.body.MjSection;
import dev.jcputney.javamjml.component.body.MjWrapper;
import dev.jcputney.javamjml.component.content.MjButton;
import dev.jcputney.javamjml.component.content.MjDivider;
import dev.jcputney.javamjml.component.content.MjImage;
import dev.jcputney.javamjml.component.content.MjRaw;
import dev.jcputney.javamjml.component.content.MjSpacer;
import dev.jcputney.javamjml.component.content.MjTable;
import dev.jcputney.javamjml.component.content.MjText;
import dev.jcputney.javamjml.component.head.MjAttributes;
import dev.jcputney.javamjml.component.head.MjBreakpoint;
import dev.jcputney.javamjml.component.head.MjFont;
import dev.jcputney.javamjml.component.head.MjHead;
import dev.jcputney.javamjml.component.head.MjHtmlAttributes;
import dev.jcputney.javamjml.component.head.MjPreview;
import dev.jcputney.javamjml.component.head.MjStyle;
import dev.jcputney.javamjml.component.head.MjTitle;
import dev.jcputney.javamjml.component.interactive.MjAccordion;
import dev.jcputney.javamjml.component.interactive.MjAccordionElement;
import dev.jcputney.javamjml.component.interactive.MjAccordionText;
import dev.jcputney.javamjml.component.interactive.MjAccordionTitle;
import dev.jcputney.javamjml.component.interactive.MjCarousel;
import dev.jcputney.javamjml.component.interactive.MjCarouselImage;
import dev.jcputney.javamjml.component.interactive.MjHero;
import dev.jcputney.javamjml.component.interactive.MjNavbar;
import dev.jcputney.javamjml.component.interactive.MjNavbarLink;
import dev.jcputney.javamjml.component.interactive.MjSocial;
import dev.jcputney.javamjml.component.interactive.MjSocialElement;
import dev.jcputney.javamjml.context.AttributeResolver;
import dev.jcputney.javamjml.context.GlobalContext;
import dev.jcputney.javamjml.context.RenderContext;
import dev.jcputney.javamjml.parser.IncludeProcessor;
import dev.jcputney.javamjml.parser.MjmlDocument;
import dev.jcputney.javamjml.parser.MjmlNode;
import dev.jcputney.javamjml.parser.MjmlParser;
import java.util.Map;

/**
 * Orchestrates the 7-phase rendering pipeline:
 * <ol>
 *   <li>Preprocess (handled by MjmlParser)</li>
 *   <li>Parse (handled by MjmlParser)</li>
 *   <li>Resolve includes (future: expand mj-include)</li>
 *   <li>Process head (extract fonts, styles, attributes, etc.)</li>
 *   <li>Resolve attributes (cascade applied during rendering)</li>
 *   <li>Render body (top-down component rendering)</li>
 *   <li>Assemble skeleton + CSS inlining</li>
 * </ol>
 */
public class RenderPipeline {

  private final MjmlConfiguration configuration;
  private final ComponentRegistry registry;

  public RenderPipeline(MjmlConfiguration configuration) {
    this.configuration = configuration;
    this.registry = createRegistry();
  }

  /**
   * Renders MJML source to a complete HTML document.
   */
  public MjmlRenderResult render(String mjmlSource) {
    // Phase 1 & 2: Preprocess and parse
    MjmlDocument document = MjmlParser.parse(mjmlSource);

    // Create global context
    GlobalContext globalContext = new GlobalContext(configuration);

    // Phase 3: Resolve includes
    if (configuration.getIncludeResolver() != null) {
      IncludeProcessor includeProcessor = new IncludeProcessor(configuration.getIncludeResolver());
      includeProcessor.process(document);
    }

    // Phase 4: Process head
    processHead(document, globalContext);

    // Phase 4b: Auto-register default fonts used by components
    registerDefaultFonts(document, globalContext);

    // Phase 5 & 6: Render body (attribute cascade happens during rendering)
    String bodyHtml = renderBody(document, globalContext);

    // Phase 7: Assemble skeleton
    String html = HtmlSkeleton.assemble(bodyHtml, globalContext);

    // Phase 7b: CSS inlining (inline styles from mj-style inline="inline")
    if (!globalContext.getInlineStyles().isEmpty()) {
      StringBuilder inlineCss = new StringBuilder();
      for (String css : globalContext.getInlineStyles()) {
        inlineCss.append(css).append("\n");
      }
      html = CssInliner.inlineAdditionalOnly(html, inlineCss.toString());
      // Official MJML CSS inliner rewrites all elements, which has side effects:
      // 1. Empty style attributes become bare: style="" -> style
      // 2. Self-closing tags lose the slash: /> -> >
      html = html.replace(" style=\"\"", " style");
      html = html.replace(" />", ">");
    }

    return new MjmlRenderResult(html, globalContext.getTitle());
  }

  private void processHead(MjmlDocument document, GlobalContext globalContext) {
    MjmlNode head = document.getHead();
    if (head == null) {
      return;
    }

    RenderContext dummyContext = new RenderContext(600);

    for (MjmlNode child : head.getChildren()) {
      if ("#comment".equals(child.getTagName())) {
        String comment = child.getTextContent();
        if (comment != null && !comment.isBlank()) {
          globalContext.addHeadComment(comment.trim());
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
    String widthAttr = body.getAttribute("width", "600px");
    int containerWidth = parsePixels(widthAttr, 600);
    globalContext.setContainerWidth(containerWidth);

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
    reg.register("mj-attributes",
        (node, ctx, rctx) -> new MjAttributes(node, ctx, rctx, reg));

    // Head extras
    reg.register("mj-html-attributes", MjHtmlAttributes::new);

    // Body layout components
    reg.register("mj-body",
        (node, ctx, rctx) -> new MjBody(node, ctx, rctx, reg));
    reg.register("mj-section",
        (node, ctx, rctx) -> new MjSection(node, ctx, rctx, reg));
    reg.register("mj-column",
        (node, ctx, rctx) -> new MjColumn(node, ctx, rctx, reg));
    reg.register("mj-group",
        (node, ctx, rctx) -> new MjGroup(node, ctx, rctx, reg));
    reg.register("mj-wrapper",
        (node, ctx, rctx) -> new MjWrapper(node, ctx, rctx, reg));

    // Content components
    reg.register("mj-text", MjText::new);
    reg.register("mj-image", MjImage::new);
    reg.register("mj-button", MjButton::new);
    reg.register("mj-divider", MjDivider::new);
    reg.register("mj-spacer", MjSpacer::new);
    reg.register("mj-table", MjTable::new);
    reg.register("mj-raw", MjRaw::new);

    // Interactive components
    reg.register("mj-hero",
        (node, ctx, rctx) -> new MjHero(node, ctx, rctx, reg));
    reg.register("mj-accordion",
        (node, ctx, rctx) -> new MjAccordion(node, ctx, rctx, reg));
    reg.register("mj-accordion-element",
        (node, ctx, rctx) -> new MjAccordionElement(node, ctx, rctx, reg));
    reg.register("mj-accordion-title", MjAccordionTitle::new);
    reg.register("mj-accordion-text", MjAccordionText::new);
    reg.register("mj-carousel",
        (node, ctx, rctx) -> new MjCarousel(node, ctx, rctx, reg));
    reg.register("mj-carousel-image", MjCarouselImage::new);
    reg.register("mj-navbar",
        (node, ctx, rctx) -> new MjNavbar(node, ctx, rctx, reg));
    reg.register("mj-navbar-link", MjNavbarLink::new);
    reg.register("mj-social",
        (node, ctx, rctx) -> new MjSocial(node, ctx, rctx, reg));
    reg.register("mj-social-element", MjSocialElement::new);

    // Register custom components
    for (Map.Entry<String, dev.jcputney.javamjml.component.ComponentFactory> entry :
        configuration.getCustomComponents().entrySet()) {
      reg.register(entry.getKey(), entry.getValue());
    }

    return reg;
  }

  private void registerDefaultFonts(MjmlDocument document, GlobalContext globalContext) {
    MjmlNode body = document.getBody();
    if (body == null) {
      return;
    }
    scanFontsRecursive(body, globalContext);
  }

  private void scanFontsRecursive(MjmlNode node, GlobalContext globalContext) {
    String tagName = node.getTagName();
    if (tagName != null && !tagName.startsWith("#")) {
      // Get the component's default attributes to resolve font-family
      Map<String, String> defaults = getComponentDefaults(tagName);
      String fontFamily = AttributeResolver.resolve(node, "font-family", globalContext, defaults);
      if (fontFamily != null && !fontFamily.isEmpty()) {
        DefaultFontRegistry.registerUsedFonts(fontFamily, globalContext);
      }
    }
    for (MjmlNode child : node.getChildren()) {
      scanFontsRecursive(child, globalContext);
    }
  }

  private Map<String, String> getComponentDefaults(String tagName) {
    RenderContext dummyContext = new RenderContext(600);
    GlobalContext dummyGlobalContext = new GlobalContext(configuration);
    try {
      BaseComponent component = registry.createComponent(
          new MjmlNode(tagName), dummyGlobalContext, dummyContext);
      return component.getDefaultAttributes();
    } catch (Exception e) {
      return Map.of();
    }
  }

  private static int parsePixels(String value, int defaultValue) {
    if (value == null || value.isEmpty()) {
      return defaultValue;
    }
    try {
      return Integer.parseInt(value.replace("px", "").trim());
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }
}
