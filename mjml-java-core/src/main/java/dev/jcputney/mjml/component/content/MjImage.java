
package dev.jcputney.mjml.component.content;

import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import dev.jcputney.mjml.util.HtmlBuilder;
import java.util.LinkedHashMap;
import java.util.Map;
import static dev.jcputney.mjml.util.HtmlBuilder.attrs;
import static dev.jcputney.mjml.util.HtmlBuilder.unsortedAttrs;

/**
 * The image component (&lt;mj-image&gt;). Renders a responsive image that goes full-width on
 * mobile. Optionally wraps the image in an anchor tag when href is provided.
 */
public class MjImage extends BodyComponent {

  private static final Map<String, String> DEFAULTS = Map.ofEntries(
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

    String href = sanitizeHref(getAttribute("href", ""));

    boolean fluidOnMobile = "true".equals(getAttribute("fluid-on-mobile", ""));
    String fluidClass = fluidOnMobile ? "mj-full-width-mobile" : "";

    String tableStyle = buildStyle(orderedMap("border-collapse", "collapse", "border-spacing", "0px"));

    var tableAttrMap = new LinkedHashMap<>(HtmlBuilder.PRESENTATION_TABLE_ATTRS);
    if (!fluidClass.isEmpty()) {
      tableAttrMap.put("class", fluidClass);
    }
    tableAttrMap.put("style", tableStyle);
    String tableAttrs = attrs(tableAttrMap, "class");

    var tdAttrMap = new LinkedHashMap<String, String>();
    if (!fluidClass.isEmpty()) {
      tdAttrMap.put("class", fluidClass);
    }
    tdAttrMap.put("style", "width:" + widthPx + "px;");
    String tdAttrs = attrs(tdAttrMap, "class");

    HtmlBuilder html = new HtmlBuilder(24);
    html.wrap("table", tableAttrs,
      () -> html.wrap("tbody",
        () -> html.wrap("tr",
          () -> html.wrap("td", tdAttrs, () -> {
            String imgAttrStr = buildImageTagAttributes(widthPx);
            if (!href.isEmpty()) {
              var anchorAttrMap = new LinkedHashMap<String, String>();
              anchorAttrMap.put("href", escapeHref(href));
              anchorAttrMap.put("rel", escapeAttr(getAttribute("rel", "")));
              anchorAttrMap.put("target", escapeAttr(getAttribute("target", "_blank")));
              anchorAttrMap.put("title", escapeAttr(getAttribute("title", "")));
              html.wrap("a", attrs(anchorAttrMap),
                () -> html.selfClose("img", imgAttrStr));
            } else {
              html.selfClose("img", imgAttrStr);
            }
          }))));

    return html.toString();
  }

  /**
   * Builds a string of image tag attributes with values resolved, processed, and sorted alphabetically, placing the
   * "height" attribute at the end. The resulting attributes are ready for interpolation into an HTML `<img>` tag.
   * <p>
   * The method uses various helper methods to escape attribute values, resolve default attributes, and construct inline
   * styles for the image component.
   *
   * @param widthPx the width of the image in pixels; used to explicitly set the "width" attribute
   * @return a space-separated string of image tag attributes, including "alt", "src", "srcset", "sizes", "style",
   * "title", "usemap", "width", and "height"
   */
  private String buildImageTagAttributes(int widthPx) {
    // Build <img> attrs — sorted alphabetically, height trailing
    String srcset = getAttribute("srcset", "");
    var imgAttrMap = new LinkedHashMap<String, String>();
    imgAttrMap.put("alt", escapeAttr(getAttribute("alt", "")));
    imgAttrMap.put("src", escapeAttr(getAttribute("src", "")));
    imgAttrMap.put("srcset", srcset.isEmpty() ? "" : escapeAttr(srcset.replace(", ", ",\n")));
    imgAttrMap.put("sizes", escapeAttr(getAttribute("sizes", "")));
    imgAttrMap.put("style", buildStyle(buildImageStyles()));
    imgAttrMap.put("title", escapeAttr(getAttribute("title", "")));
    imgAttrMap.put("usemap", escapeAttr(getAttribute("usemap", "")));
    imgAttrMap.put("width", String.valueOf(widthPx));
    imgAttrMap.put("height", escapeAttr(getAttribute("height", "auto")));
    return unsortedAttrs(imgAttrMap);
  }

  /**
   * Builds a map of CSS styles for an image component, using default values or resolving attributes as needed. Styles
   * include properties such as border, border-radius, display, outline, text-decoration, height, width, and font-size.
   *
   * @return a map containing CSS style rules for the image component
   */
  private Map<String, String> buildImageStyles() {
    // Image inline style
    Map<String, String> imgStyleMap = new LinkedHashMap<>();
    imgStyleMap.put("border", getAttribute("border", "0"));
    addIfPresent(imgStyleMap, "border-radius");
    imgStyleMap.put("display", "block");
    imgStyleMap.put("outline", "none");
    imgStyleMap.put("text-decoration", "none");
    imgStyleMap.put("height", getAttribute("height", "auto"));
    imgStyleMap.put("width", "100%");
    imgStyleMap.put("font-size", "13px");
    return imgStyleMap;
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
