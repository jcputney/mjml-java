package dev.jcputney.mjml.component.content;

import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The image component (&lt;mj-image&gt;).
 * Renders a responsive image that goes full-width on mobile.
 * Optionally wraps the image in an anchor tag when href is provided.
 */
public class MjImage extends BodyComponent {

  private static final Map<String, String> DEFAULTS = Map.ofEntries(
      Map.entry("align", "center"),
      Map.entry("border", "0"),
      Map.entry("border-radius", ""),
      Map.entry("height", "auto"),
      Map.entry("padding", "10px 25px"),
      Map.entry("src", ""),
      Map.entry("target", "_blank"),
      Map.entry("title", ""),
      Map.entry("width", ""),
      Map.entry("alt", ""),
      Map.entry("fluid-on-mobile", ""),
      Map.entry("href", ""),
      Map.entry("srcset", ""),
      Map.entry("sizes", "")
  );

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
    globalContext.setFluidOnMobileUsed(true);

    // Image style: border, border-radius (if set), display, outline, text-decoration,
    // height, width, font-size
    Map<String, String> imgStyleMap = new LinkedHashMap<>();
    imgStyleMap.put("border", getAttribute("border", "0"));
    String borderRadius = getAttribute("border-radius", "");
    if (!borderRadius.isEmpty()) {
      imgStyleMap.put("border-radius", borderRadius);
    }
    imgStyleMap.put("display", "block");
    imgStyleMap.put("outline", "none");
    imgStyleMap.put("text-decoration", "none");
    imgStyleMap.put("height", getAttribute("height", "auto"));
    imgStyleMap.put("width", "100%");
    imgStyleMap.put("font-size", "13px");
    String imgStyle = buildStyle(imgStyleMap);

    // Build <img> tag
    StringBuilder img = new StringBuilder();
    img.append("<img");
    img.append(" alt=\"").append(getAttribute("alt", "")).append("\"");
    img.append(" src=\"").append(getAttribute("src", "")).append("\"");

    String srcset = getAttribute("srcset", "");
    if (!srcset.isEmpty()) {
      // MJML formats srcset with newlines after each comma
      String formattedSrcset = srcset.replace(", ", ",\n");
      img.append(" srcset=\"").append(formattedSrcset).append("\"");
    }

    String sizes = getAttribute("sizes", "");
    if (!sizes.isEmpty()) {
      img.append(" sizes=\"").append(sizes).append("\"");
    }

    img.append(" style=\"").append(imgStyle).append("\"");

    String title = getAttribute("title", "");
    if (!title.isEmpty()) {
      img.append(" title=\"").append(title).append("\"");
    }

    img.append(" width=\"").append(widthPx).append("\"");
    img.append(" height=\"").append(getAttribute("height", "auto")).append("\"");
    img.append(" />");

    String href = getAttribute("href", "");

    boolean fluidOnMobile = "true".equals(getAttribute("fluid-on-mobile", ""));

    StringBuilder sb = new StringBuilder();
    sb.append("                        <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\"");
    sb.append(" style=\"").append(buildStyle(orderedMap(
        "border-collapse", "collapse",
        "border-spacing", "0px"
    ))).append("\"");
    if (fluidOnMobile) {
      sb.append(" class=\"mj-full-width-mobile\"");
    }
    sb.append(">\n");
    sb.append("                          <tbody>\n");
    sb.append("                            <tr>\n");
    sb.append("                              <td style=\"width:").append(widthPx).append("px;\"");
    if (fluidOnMobile) {
      sb.append(" class=\"mj-full-width-mobile\"");
    }
    sb.append(">\n");

    if (!href.isEmpty()) {
      sb.append("                                <a href=\"").append(href).append("\"");
      sb.append(" target=\"").append(getAttribute("target", "_blank")).append("\"");
      if (!title.isEmpty()) {
        sb.append(" title=\"").append(title).append("\"");
      }
      sb.append(">\n");
      sb.append("                                  ").append(img).append("\n");
      sb.append("                                </a>\n");
    } else {
      sb.append("                                ").append(img).append("\n");
    }

    sb.append("                              </td>\n");
    sb.append("                            </tr>\n");
    sb.append("                          </tbody>\n");
    sb.append("                        </table>");

    return sb.toString();
  }

  /**
   * Computes the actual image width in pixels: the minimum of the declared
   * width attribute (parsed as px) and the available width (container minus
   * the image component's own horizontal padding).
   */
  private double computeImageWidth(double containerWidth) {
    // Subtract the image's own horizontal padding from container width
    double hPadding = getBoxModel().paddingLeft() + getBoxModel().paddingRight();
    double availableWidth = containerWidth - hPadding;

    String widthAttr = getAttribute("width", "");
    if (widthAttr.isEmpty()) {
      return availableWidth;
    }
    double declaredWidth = parseWidth(widthAttr);
    return Math.min(declaredWidth, availableWidth);
  }
}
