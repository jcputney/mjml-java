package dev.jcputney.mjml.component.content;

import static dev.jcputney.mjml.util.HtmlBuilder.attrIf;
import static dev.jcputney.mjml.util.HtmlBuilder.attrs;

import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import dev.jcputney.mjml.util.HtmlBuilder;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The image component (&lt;mj-image&gt;). Renders a responsive image that goes full-width on
 * mobile. Optionally wraps the image in an anchor tag when href is provided.
 */
public class MjImage extends BodyComponent {

  private static final Map<String, String> DEFAULTS =
      Map.ofEntries(
          Map.entry("align", "center"),
          Map.entry("border", "0"),
          Map.entry("border-bottom", ""),
          Map.entry("border-left", ""),
          Map.entry("border-radius", ""),
          Map.entry("border-right", ""),
          Map.entry("border-top", ""),
          Map.entry("container-background-color", ""),
          Map.entry("height", "auto"),
          Map.entry("max-height", ""),
          Map.entry("name", ""),
          Map.entry("padding", "10px 25px"),
          Map.entry("src", ""),
          Map.entry("target", "_blank"),
          Map.entry("title", ""),
          Map.entry("width", ""),
          Map.entry("alt", ""),
          Map.entry("fluid-on-mobile", ""),
          Map.entry("href", ""),
          Map.entry("rel", ""),
          Map.entry("srcset", ""),
          Map.entry("sizes", ""),
          Map.entry("usemap", ""));

  public MjImage(MjmlNode node, GlobalContext globalContext, RenderContext renderContext) {
    super(node, globalContext, renderContext);
  }

  @Override
  public String getTagName() {
    return "mj-image";
  }

  @Override
  public Map<String, String> getDefaultAttributes() {
    return DEFAULTS;
  }

  @Override
  public String render() {
    double containerWidth = renderContext.getContainerWidth();
    double imageWidth = computeImageWidth(containerWidth);
    int widthPx = (int) imageWidth;

    // MJML always adds fluid-on-mobile responsive styles when images are present
    globalContext.styles().setFluidOnMobileUsed(true);

    // Image style: border, border-radius (if set), display, outline, text-decoration,
    // height, width, font-size
    Map<String, String> imgStyleMap = new LinkedHashMap<>();
    imgStyleMap.put("border", getAttribute("border", "0"));
    addIfPresent(imgStyleMap, "border-radius");
    imgStyleMap.put("display", "block");
    imgStyleMap.put("outline", "none");
    imgStyleMap.put("text-decoration", "none");
    imgStyleMap.put("height", getAttribute("height", "auto"));
    imgStyleMap.put("width", "100%");
    imgStyleMap.put("font-size", "13px");
    String imgStyle = buildStyle(imgStyleMap);

    // Build <img> tag as a string (self-closing, used inline)
    String srcset = getAttribute("srcset", "");
    String formattedSrcset = srcset.isEmpty() ? "" : escapeAttr(srcset.replace(", ", ",\n"));
    String sizes = getAttribute("sizes", "");
    String title = getAttribute("title", "");
    String usemap = getAttribute("usemap", "");

    String imgTag =
        "<img"
            + attrs("alt", escapeAttr(getAttribute("alt", "")))
            + attrs("src", escapeAttr(getAttribute("src", "")))
            + attrIf("srcset", formattedSrcset)
            + attrIf("sizes", escapeAttr(sizes))
            + attrs("style", imgStyle)
            + attrIf("title", escapeAttr(title))
            + attrIf("usemap", escapeAttr(usemap))
            + attrs("width", String.valueOf(widthPx))
            + attrs("height", escapeAttr(getAttribute("height", "auto")))
            + " />";

    String href = sanitizeHref(getAttribute("href", ""));

    boolean fluidOnMobile = "true".equals(getAttribute("fluid-on-mobile", ""));
    String fluidClass = fluidOnMobile ? "mj-full-width-mobile" : "";

    String tableStyle =
        buildStyle(
            orderedMap(
                "border-collapse", "collapse",
                "border-spacing", "0px"));

    HtmlBuilder html = new HtmlBuilder(24);

    html.open(
        "table",
        attrs(
                "border", "0",
                "cellpadding", "0",
                "cellspacing", "0",
                "role", "presentation",
                "style", tableStyle)
            + attrIf("class", fluidClass));
    html.open("tbody");
    html.open("tr");
    html.open("td", attrs("style", "width:" + widthPx + "px;") + attrIf("class", fluidClass));

    if (!href.isEmpty()) {
      String rel = getAttribute("rel", "");
      html.open(
          "a",
          attrs("href", escapeHref(href))
              + attrIf("rel", escapeAttr(rel))
              + attrs("target", escapeAttr(getAttribute("target", "_blank")))
              + attrIf("title", escapeAttr(title)));
      html.raw(imgTag);
      html.close("a");
    } else {
      html.raw(imgTag);
    }

    html.close("td");
    html.close("tr");
    html.close("tbody");
    html.close("table");

    return html.toString();
  }

  /**
   * Computes the actual image width in pixels: the minimum of the declared width attribute (parsed
   * as px) and the available width (container minus the image component's own horizontal padding).
   */
  private double computeImageWidth(double containerWidth) {
    // Subtract the image's own horizontal padding from container width,
    // checking individual padding-left/right overrides before shorthand
    double paddingLeft = resolveShorthandSide("padding", "padding-left", 3);
    double paddingRight = resolveShorthandSide("padding", "padding-right", 1);
    double availableWidth = containerWidth - paddingLeft - paddingRight;

    String widthAttr = getAttribute("width", "");
    if (widthAttr.isEmpty()) {
      return availableWidth;
    }
    double declaredWidth = parseWidth(widthAttr);
    return Math.min(declaredWidth, availableWidth);
  }
}
