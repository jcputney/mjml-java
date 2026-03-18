package dev.jcputney.mjml.component.interactive;

import static dev.jcputney.mjml.util.HtmlBuilder.attrIf;
import static dev.jcputney.mjml.util.HtmlBuilder.attrs;

import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.component.ComponentRegistry;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import dev.jcputney.mjml.util.CssEscaper;
import dev.jcputney.mjml.util.CssUnitParser;
import dev.jcputney.mjml.util.HtmlBuilder;
import dev.jcputney.mjml.util.MsoHelper;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The hero component ({@code <mj-hero>}). Renders a full-width hero section with a background
 * image. Supports two modes:
 *
 * <ul>
 *   <li>{@code fixed-height} &mdash; the hero has a fixed pixel height
 *   <li>{@code fluid-height} &mdash; the hero height adapts to its content
 * </ul>
 *
 * Inner content is wrapped in a table for vertical alignment. VML background is emitted for Outlook
 * compatibility.
 */
public class MjHero extends BodyComponent {

  private static final Map<String, String> DEFAULTS =
      Map.ofEntries(
          Map.entry("background-color", "#ffffff"),
          Map.entry("background-height", ""),
          Map.entry("background-position", "center center"),
          Map.entry("background-url", ""),
          Map.entry("background-width", ""),
          Map.entry("border-radius", ""),
          Map.entry("container-background-color", ""),
          Map.entry("height", "0px"),
          Map.entry("inner-background-color", ""),
          Map.entry("inner-padding", ""),
          Map.entry("inner-padding-bottom", ""),
          Map.entry("inner-padding-left", ""),
          Map.entry("inner-padding-right", ""),
          Map.entry("inner-padding-top", ""),
          Map.entry("mode", "fluid-height"),
          Map.entry("padding", "0px"),
          Map.entry("padding-bottom", ""),
          Map.entry("padding-left", ""),
          Map.entry("padding-right", ""),
          Map.entry("padding-top", ""),
          Map.entry("vertical-align", "top"),
          Map.entry("width", "100%"));

  private final ComponentRegistry registry;

  public MjHero(
      MjmlNode node,
      GlobalContext globalContext,
      RenderContext renderContext,
      ComponentRegistry registry) {
    super(node, globalContext, renderContext);
    this.registry = registry;
  }

  private static int parsePaddingPart(String padding, int index) {
    return (int) CssUnitParser.parseShorthand(padding)[index];
  }

  @Override
  public String getTagName() {
    return "mj-hero";
  }

  @Override
  public Map<String, String> getDefaultAttributes() {
    return DEFAULTS;
  }

  @Override
  public String render() {
    String mode = getAttribute("mode", "fluid-height");
    if ("fixed-height".equals(mode)) {
      return renderFixedHeight();
    }
    return renderFluidHeight();
  }

  /**
   * Renders a fixed-height hero. The content td has an explicit height derived from the declared
   * height minus vertical padding.
   */
  private String renderFixedHeight() {
    String height = getAttribute("height", "");
    String padding = getAttribute("padding", "0px");

    // Compute inner height: declared height - top padding - bottom padding
    int innerHeight = 0;
    if (!height.isEmpty()) {
      int paddingTop = parsePaddingPart(padding, 0);
      int paddingBottom = parsePaddingPart(padding, 2);
      int h = CssUnitParser.parseIntPx(height);
      innerHeight = Math.max(0, h - paddingTop - paddingBottom);
    }

    // v:image uses the declared height
    String vImageHeight = height;
    String spacerPaddingPct = null;

    return renderHero(vImageHeight, innerHeight, spacerPaddingPct);
  }

  /**
   * Renders a fluid-height hero. Height adapts to content; spacer tds with padding-bottom
   * percentage maintain the background aspect ratio.
   */
  private String renderFluidHeight() {
    String bgHeight = getAttribute("background-height", "");
    int containerWidth = globalContext.metadata().getContainerWidth();

    // Compute padding-bottom percentage for fluid aspect ratio
    double paddingPct = 0;
    if (!bgHeight.isEmpty()) {
      int bgH = CssUnitParser.parseIntPx(bgHeight);
      String bgWidth = getAttribute("background-width", "");
      int bgW = !bgWidth.isEmpty() ? CssUnitParser.parseIntPx(bgWidth) : containerWidth;
      paddingPct = ((double) bgH / bgW) * 100.0;
    }

    // v:image uses background-height
    String vImageHeight = bgHeight;
    int innerHeight = 0;
    String spacerPaddingPct = paddingPct > 0 ? String.valueOf(Math.round(paddingPct)) : null;

    return renderHero(vImageHeight, innerHeight, spacerPaddingPct);
  }

  /**
   * Shared hero rendering logic for both fixed and fluid modes.
   *
   * @param vImageHeight height for the v:image element (empty string = omit)
   * @param innerHeight explicit height in px for the content td (0 = omit)
   * @param spacerPaddingPct if non-null, adds spacer tds with this padding-bottom %
   */
  private String renderHero(String vImageHeight, int innerHeight, String spacerPaddingPct) {
    String backgroundColor = getAttribute("background-color", "#ffffff");
    String backgroundUrl = getAttribute("background-url", "");
    String backgroundPosition = getAttribute("background-position", "center center");
    String verticalAlign = getAttribute("vertical-align", "top");
    String padding = getAttribute("padding", "0px");
    int containerWidth = globalContext.metadata().getContainerWidth();

    HtmlBuilder html = new HtmlBuilder();

    // MSO wrapper with v:image
    html.mso(
        () -> {
          html.rawVerbatim(
              "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"width:"
                  + containerWidth
                  + "px;\" width=\""
                  + containerWidth
                  + "\" ><tr><td style=\"line-height:0;font-size:0;mso-line-height-rule:exactly;\">");
          appendVmlImage(html, backgroundUrl, vImageHeight, containerWidth);
        });

    html.open("div", attrs("style", "margin:0 auto;max-width:" + containerWidth + "px;"));
    html.open(
        "table",
        attrs(
            "border",
            "0",
            "cellpadding",
            "0",
            "cellspacing",
            "0",
            "role",
            "presentation",
            "style",
            "width:100%;"));
    html.open("tbody");
    html.open("tr", attrs("style", "vertical-align:top;"));

    if (spacerPaddingPct != null) {
      appendSpacerTd(html, spacerPaddingPct);
    }

    appendContentTd(
        html,
        backgroundUrl,
        backgroundColor,
        backgroundPosition,
        padding,
        verticalAlign,
        innerHeight);

    // MSO inner table — style="" must be present
    html.mso(
        () ->
            html.rawVerbatim(
                // language=HTML
                """
            <table border="0" cellpadding="0" cellspacing="0" style="width:%dpx;" width="%d" ><tr><td style="">
            """
                    .formatted(containerWidth, containerWidth)));

    appendHeroContent(html);

    html.raw(MsoHelper.msoConditionalTableClosing());

    html.close("td");

    if (spacerPaddingPct != null) {
      appendSpacerTd(html, spacerPaddingPct);
    }

    html.close("tr");
    html.close("tbody");
    html.close("table");
    html.close("div");

    html.raw(MsoHelper.msoConditionalTableClosing());

    return html.toString();
  }

  private void appendVmlImage(
      HtmlBuilder html, String backgroundUrl, String vImageHeight, int containerWidth) {
    if (!backgroundUrl.isEmpty()) {
      String heightStyle = vImageHeight.isEmpty() ? "" : "height:" + escapeAttr(vImageHeight) + ";";
      html.rawVerbatim(
          "<v:image style=\"border:0;"
              + heightStyle
              + "mso-position-horizontal:center;position:absolute;top:0;width:"
              + containerWidth
              + "px;z-index:-3;\" src=\""
              + escapeAttr(backgroundUrl)
              + "\" xmlns:v=\"urn:schemas-microsoft-com:vml\" />");
    }
  }

  private void appendSpacerTd(HtmlBuilder html, String paddingPct) {
    html.rawVerbatim(
        "<td style=\"width:0.01%;padding-bottom:"
            + paddingPct
            + "%;mso-padding-bottom-alt:0;\" />\n");
  }

  private void appendContentTd(
      HtmlBuilder html,
      String backgroundUrl,
      String backgroundColor,
      String backgroundPosition,
      String padding,
      String verticalAlign,
      int innerHeight) {
    Map<String, String> tdStyles = new LinkedHashMap<>();
    tdStyles.put(
        "background", buildBackgroundValue(backgroundUrl, backgroundColor, backgroundPosition));
    if (!backgroundUrl.isEmpty()) {
      tdStyles.put("background-position", backgroundPosition);
      tdStyles.put("background-repeat", "no-repeat");
    }
    tdStyles.put("padding", padding);
    tdStyles.put("vertical-align", verticalAlign);
    if (innerHeight > 0) {
      tdStyles.put("height", innerHeight + "px");
    }

    // open() would add indent+newline; we need to stay in the builder's flow
    html.rawVerbatim(
        "<td"
            + attrIf("background", !backgroundUrl.isEmpty() ? escapeAttr(backgroundUrl) : null)
            + " style=\""
            + buildStyle(tdStyles)
            + "\""
            + (innerHeight > 0 ? " height=\"" + innerHeight + "\"" : "")
            + ">\n");
    html.indent(); // track depth for children
  }

  private void appendHeroContent(HtmlBuilder html) {
    String divAttrs = attrs("class", "mj-hero-content", "style", "margin:0px auto;");
    String tableAttrs =
        attrs(
            "border",
            "0",
            "cellpadding",
            "0",
            "cellspacing",
            "0",
            "role",
            "presentation",
            "style",
            "width:100%;margin:0px;");

    html.wrap(
        "div",
        divAttrs,
        () ->
            html.wrap(
                "table",
                tableAttrs,
                () ->
                    html.wrap(
                        "tbody",
                        () ->
                            html.wrap(
                                "tr",
                                () ->
                                    html.wrap(
                                        "td",
                                        attrs("style", ""),
                                        () ->
                                            html.wrap(
                                                "table",
                                                tableAttrs,
                                                () ->
                                                    html.wrap(
                                                        "tbody",
                                                        () ->
                                                            html.rawVerbatim(
                                                                renderChildrenAsRows()))))))));
  }

  private String renderChildrenAsRows() {
    HtmlBuilder html = new HtmlBuilder();

    for (MjmlNode child : node.getChildren()) {
      if (child.getTagName().startsWith("#")) {
        continue;
      }

      String childPadding = child.getAttribute("padding");
      if (childPadding == null || childPadding.isEmpty()) {
        childPadding = "10px 25px";
      }

      String align = child.getAttribute("align");
      if (align == null || align.isEmpty()) {
        align = "center";
      }

      String tdStyle =
          "font-size:0px;padding:" + escapeAttr(childPadding) + ";word-break:break-word;";

      html.open("tr");
      html.open("td", attrs("align", escapeAttr(align), "style", tdStyle));

      var component = registry.createComponent(child, globalContext, renderContext);
      if (component instanceof BodyComponent bodyComponent) {
        String rendered = bodyComponent.render();
        html.rawVerbatim(rendered);
        if (!rendered.endsWith("\n")) {
          html.newline();
        }
      }

      html.close("td");
      html.close("tr");
    }

    return html.toString();
  }

  private String buildBackgroundValue(String url, String color, String position) {
    if (url == null || url.isEmpty()) {
      return color;
    }
    return color
        + " url('"
        + CssEscaper.escapeCssUrl(url)
        + "') no-repeat "
        + position
        + " / cover";
  }
}
