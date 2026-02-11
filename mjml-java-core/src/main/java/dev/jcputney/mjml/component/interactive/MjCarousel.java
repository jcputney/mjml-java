package dev.jcputney.mjml.component.interactive;

import dev.jcputney.mjml.component.BaseComponent;
import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.component.ComponentRegistry;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import dev.jcputney.mjml.util.CssUnitParser;
import java.util.List;
import java.util.Map;

/**
 * The carousel component ({@code <mj-carousel>}). Uses CSS radio buttons to implement a pure-CSS
 * image carousel with previous/next navigation and thumbnail strips. Generates unique hex IDs per
 * instance and injects carousel CSS into the global context.
 *
 * <p>The output matches the official MJML v4.18.0 carousel structure: radio inputs for state,
 * sibling combinator CSS for toggling visibility, thumbnail anchors with labels, and table-based
 * main content with previous/next icon columns.
 */
public class MjCarousel extends BodyComponent {

  /** Default left navigation arrow icon (matches official MJML v4). */
  private static final String DEFAULT_LEFT_ICON = "https://i.imgur.com/xTh3hln.png";

  /** Default right navigation arrow icon (matches official MJML v4). */
  private static final String DEFAULT_RIGHT_ICON = "https://i.imgur.com/os7o9kz.png";

  private static final Map<String, String> DEFAULTS =
      Map.ofEntries(
          Map.entry("align", "center"),
          Map.entry("background-color", ""),
          Map.entry("border-radius", "6px"),
          Map.entry("container-background-color", ""),
          Map.entry("icon-width", "44px"),
          Map.entry("left-icon", DEFAULT_LEFT_ICON),
          Map.entry("right-icon", DEFAULT_RIGHT_ICON),
          Map.entry("padding", ""),
          Map.entry("tb-border", "2px solid transparent"),
          Map.entry("tb-border-radius", "6px"),
          Map.entry("tb-hover-border-color", "#fead0d"),
          Map.entry("tb-selected-border-color", "#ccc"),
          Map.entry("tb-width", ""),
          Map.entry("thumbnails", "visible"));

  private final ComponentRegistry registry;
  private final String hexId;

  /**
   * Creates a new MjCarousel component.
   *
   * @param node the parsed MJML node for this component
   * @param globalContext the global rendering context
   * @param renderContext the current render context
   * @param registry the component registry for creating child components
   */
  public MjCarousel(
      MjmlNode node,
      GlobalContext globalContext,
      RenderContext renderContext,
      ComponentRegistry registry) {
    super(node, globalContext, renderContext);
    this.registry = registry;
    this.hexId = renderContext.nextUniqueId("carousel");
  }

  @Override
  public String getTagName() {
    return "mj-carousel";
  }

  @Override
  public Map<String, String> getDefaultAttributes() {
    return DEFAULTS;
  }

  @Override
  public String render() {
    List<MjmlNode> images = node.getChildrenByTag("mj-carousel-image");
    if (images.isEmpty()) {
      return "";
    }

    int count = images.size();
    String carouselId = "mj-carousel-" + hexId;
    String iconWidth = getAttribute("icon-width", "44px");
    String iconWidthNum = iconWidth.replace("px", "");
    String leftIcon = getAttribute("left-icon");
    String rightIcon = getAttribute("right-icon");
    String borderRadius = getAttribute("border-radius", "6px");
    String tbHoverBorderColor = getAttribute("tb-hover-border-color", "");
    String tbSelectedBorderColor = getAttribute("tb-selected-border-color", "");
    boolean showThumbnails = "visible".equals(getAttribute("thumbnails", "visible"));

    int containerWidth = (int) renderContext.getContainerWidth();

    // Inject carousel CSS into global context
    globalContext
        .styles()
        .addComponentStyle(
            buildCarouselCss(
                carouselId, count, iconWidthNum, tbHoverBorderColor, tbSelectedBorderColor));

    StringBuilder sb = new StringBuilder();

    // --- Start non-MSO conditional ---
    sb.append("<!--[if !mso]><!-->\n");
    sb.append("<div class=\"mj-carousel\">");

    renderRadioInputs(sb, carouselId, count);

    sb.append("\n");

    // Content div
    sb.append("  <div class=\"mj-carousel-content ")
        .append(carouselId)
        .append(
            "-content\" style=\"display:table;width:100%;table-layout:fixed;text-align:center;font-size:0px;\">");

    if (showThumbnails) {
      renderThumbnails(sb, images, carouselId, count);
    }

    renderMainTable(
        sb,
        images,
        carouselId,
        count,
        iconWidthNum,
        leftIcon,
        rightIcon,
        borderRadius,
        containerWidth);

    sb.append("  </div>\n");
    sb.append("</div>");
    sb.append("<!--<![endif]-->\n");

    renderMsoFallback(sb, images, borderRadius, containerWidth);

    return sb.toString();
  }

  /** Renders the hidden radio inputs that track carousel state. */
  private void renderRadioInputs(StringBuilder sb, String carouselId, int count) {
    for (int i = 1; i <= count; i++) {
      sb.append("<input class=\"mj-carousel-radio ")
          .append(carouselId)
          .append("-radio ")
          .append(carouselId)
          .append("-radio-")
          .append(i)
          .append("\"");
      if (i == 1) {
        sb.append(" checked=\"checked\"");
      }
      sb.append(" type=\"radio\"");
      sb.append(" name=\"mj-carousel-radio-").append(hexId).append("\"");
      sb.append(" id=\"").append(carouselId).append("-radio-").append(i).append("\"");
      sb.append(" style=\"display:none;mso-hide:all;\"");
      sb.append(" />");
    }
  }

  /** Renders the thumbnail strip with labeled anchor elements. */
  private void renderThumbnails(
      StringBuilder sb, List<MjmlNode> images, String carouselId, int count) {
    String tbBorder = getAttribute("tb-border", "0");
    String tbBorderRadius = getAttribute("tb-border-radius", "0");
    String tbWidth = getAttribute("tb-width", "");
    int tbWidthInt = tbWidth.isEmpty() ? 0 : parseIntFromPx(tbWidth);

    for (int i = 1; i <= count; i++) {
      MjmlNode imgNode = images.get(i - 1);
      String thumbSrc = imgNode.getAttribute("thumbnails-src", "");
      if (thumbSrc == null || thumbSrc.isEmpty()) {
        thumbSrc = imgNode.getAttribute("src", "");
      }
      String alt = imgNode.getAttribute("alt", "");
      if (alt == null) {
        alt = "";
      }

      sb.append("<a style=\"border:")
          .append(tbBorder)
          .append(";border-radius:")
          .append(tbBorderRadius)
          .append(";display:inline-block;overflow:hidden;width:")
          .append(tbWidthInt > 0 ? tbWidthInt + "px" : tbWidth)
          .append(";\"");
      sb.append(" href=\"#").append(i).append("\"");
      sb.append(" target=\"_blank\"");
      sb.append(" class=\"mj-carousel-thumbnail ")
          .append(carouselId)
          .append("-thumbnail ")
          .append(carouselId)
          .append("-thumbnail-")
          .append(i)
          .append(" \">");
      sb.append("<label for=\"").append(carouselId).append("-radio-").append(i).append("\">");
      sb.append("<img style=\"display:block;width:100%;height:auto;\"");
      sb.append(" src=\"").append(escapeAttr(thumbSrc)).append("\"");
      sb.append(" alt=\"").append(escapeAttr(alt)).append("\"");
      sb.append(" width=\"")
          .append(tbWidthInt > 0 ? tbWidthInt : tbWidth.replace("px", ""))
          .append("\"");
      sb.append(" />");
      sb.append("</label>");
      sb.append("</a>");
    }
    sb.append("\n");
  }

  /** Renders the main carousel table with previous/next icon columns and image cells. */
  private void renderMainTable(
      StringBuilder sb,
      List<MjmlNode> images,
      String carouselId,
      int count,
      String iconWidthNum,
      String leftIcon,
      String rightIcon,
      String borderRadius,
      int containerWidth) {
    sb.append(
        "    <table style=\"caption-side:top;display:table-caption;table-layout:fixed;width:100%;\"");
    sb.append(" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"");
    sb.append(" role=\"presentation\" class=\"mj-carousel-main\">\n");
    sb.append("      <tbody>\n");
    sb.append("        <tr>\n");

    // Previous icons cell
    renderIconCell(
        sb, carouselId, count, iconWidthNum, leftIcon, "previous", "mj-carousel-previous-icons");

    // Images cell
    sb.append("          <td style=\"padding:0px;\">\n");
    sb.append("            <div class=\"mj-carousel-images\">\n");
    for (int i = 1; i <= count; i++) {
      MjmlNode imgNode = images.get(i - 1);
      RenderContext childContext = renderContext.withPosition(i - 1, i == 1, i == count);
      BaseComponent component = registry.createComponent(imgNode, globalContext, childContext);

      String imageHtml = renderCarouselImage(component, borderRadius, containerWidth);

      sb.append("              <div class=\"mj-carousel-image mj-carousel-image-")
          .append(i)
          .append(" \"");
      if (i == 1) {
        sb.append(" style=\"\">");
      } else {
        sb.append(" style=\"display:none;mso-hide:all;\">");
      }
      sb.append(imageHtml);
      sb.append("</div>\n");
    }
    sb.append("            </div>\n");
    sb.append("          </td>\n");

    // Next icons cell
    renderIconCell(
        sb, carouselId, count, iconWidthNum, rightIcon, "next", "mj-carousel-next-icons");

    sb.append("        </tr>\n");
    sb.append("      </tbody>\n");
    sb.append("    </table>\n");
  }

  /** Renders a previous or next icon cell with labeled navigation images. */
  private void renderIconCell(
      StringBuilder sb,
      String carouselId,
      int count,
      String iconWidthNum,
      String iconSrc,
      String direction,
      String wrapperClass) {
    sb.append("          <td class=\"")
        .append(carouselId)
        .append("-icons-cell\" style=\"font-size:0px;display:none;mso-hide:all;padding:0px;\">\n");
    sb.append("            <div class=\"")
        .append(wrapperClass)
        .append("\" style=\"display:none;mso-hide:all;\">");
    for (int i = 1; i <= count; i++) {
      sb.append("<label for=\"").append(carouselId).append("-radio-").append(i).append("\"");
      sb.append(" class=\"mj-carousel-")
          .append(direction)
          .append(" mj-carousel-")
          .append(direction)
          .append("-")
          .append(i)
          .append("\">");
      sb.append("<img src=\"").append(escapeAttr(iconSrc)).append("\"");
      sb.append(" alt=\"").append(direction).append("\"");
      sb.append(" style=\"display:block;width:").append(iconWidthNum).append("px;height:auto;\"");
      sb.append(" width=\"").append(iconWidthNum).append("\"");
      sb.append(" />");
      sb.append("</label>");
    }
    sb.append("</div>\n");
    sb.append("          </td>\n");
  }

  /** Renders the MSO/Outlook fallback showing only the first image. */
  private void renderMsoFallback(
      StringBuilder sb, List<MjmlNode> images, String borderRadius, int containerWidth) {
    int count = images.size();
    MjmlNode firstImgNode = images.get(0);
    RenderContext firstContext = renderContext.withPosition(0, true, count == 1);
    BaseComponent firstComponent =
        registry.createComponent(firstImgNode, globalContext, firstContext);

    String firstImageHtml = renderCarouselImage(firstComponent, borderRadius, containerWidth);

    sb.append("<!--[if mso]><div class=\"mj-carousel-image mj-carousel-image-1 \" style=\"\" >");
    sb.append(firstImageHtml);
    sb.append("</div><![endif]-->");
  }

  /** Renders a single carousel image from its component. */
  private String renderCarouselImage(
      BaseComponent component, String borderRadius, int containerWidth) {
    if (component instanceof MjCarouselImage carouselImage) {
      return carouselImage.renderImage(borderRadius, containerWidth);
    } else if (component instanceof BodyComponent bodyComponent) {
      return bodyComponent.render();
    }
    return "";
  }

  /** Builds the complete carousel CSS for injection into the document head. */
  private String buildCarouselCss(
      String carouselId,
      int count,
      String iconWidthNum,
      String tbHoverBorderColor,
      String tbSelectedBorderColor) {
    StringBuilder css = new StringBuilder();

    appendBaseCss(css, carouselId, iconWidthNum);
    appendRadioVisibilityCss(css, carouselId, count);
    appendNavigationCss(css, carouselId, count);
    appendThumbnailCss(css, carouselId, count, tbHoverBorderColor, tbSelectedBorderColor);
    appendFallbackCss(css, carouselId, count);

    return css.toString();
  }

  /** Appends base carousel styles: user-select, icon cell width, radio/nav defaults. */
  private void appendBaseCss(StringBuilder css, String carouselId, String iconWidthNum) {
    css.append(".mj-carousel {\n");
    css.append("  -webkit-user-select: none;\n");
    css.append("  -moz-user-select: none;\n");
    css.append("  user-select: none;\n");
    css.append("}\n\n");

    css.append(".").append(carouselId).append("-icons-cell {\n");
    css.append("  display: table-cell !important;\n");
    css.append("  width: ").append(iconWidthNum).append("px !important;\n");
    css.append("}\n\n");

    css.append(".mj-carousel-radio,\n");
    css.append(".mj-carousel-next,\n");
    css.append(".mj-carousel-previous {\n");
    css.append("  display: none !important;\n");
    css.append("}\n\n");

    css.append(".mj-carousel-thumbnail,\n");
    css.append(".mj-carousel-next,\n");
    css.append(".mj-carousel-previous {\n");
    css.append("  touch-action: manipulation;\n");
    css.append("}\n\n");
  }

  /** Appends CSS rules for hiding all images and showing the active image per radio state. */
  private void appendRadioVisibilityCss(StringBuilder css, String carouselId, int count) {
    // Hide all images when any radio is checked
    for (int level = 0; level < count; level++) {
      css.append(".").append(carouselId).append("-radio:checked");
      css.append(siblingChain(level));
      css.append("+.mj-carousel-content .mj-carousel-image");
      if (level < count - 1) {
        css.append(",\n");
      } else {
        css.append(" {\n");
        css.append("  display: none !important;\n");
        css.append("}\n\n");
      }
    }

    // Show specific image when its radio is checked
    for (int i = 1; i <= count; i++) {
      css.append(".").append(carouselId).append("-radio-").append(i).append(":checked");
      css.append(siblingChain(count - i));
      css.append("+.mj-carousel-content .mj-carousel-image-").append(i);
      if (i < count) {
        css.append(",\n");
      } else {
        css.append(" {\n");
        css.append("  display: block !important;\n");
        css.append("}\n\n");
      }
    }
  }

  /** Appends CSS rules for previous/next navigation icon visibility. */
  private void appendNavigationCss(StringBuilder css, String carouselId, int count) {
    css.append(".mj-carousel-previous-icons,\n");
    css.append(".mj-carousel-next-icons,\n");

    // Next: radio-i shows next-(i+1), circular
    for (int i = 1; i <= count; i++) {
      int nextIdx = (i % count) + 1;
      css.append(".").append(carouselId).append("-radio-").append(i).append(":checked");
      css.append(siblingChain(count - i));
      css.append("+.mj-carousel-content .mj-carousel-next-").append(nextIdx);
      css.append(",\n");
    }

    // Previous: radio-i shows previous-((i-2+N)%N+1), circular
    for (int i = 1; i <= count; i++) {
      int prevIdx = ((i - 2 + count) % count) + 1;
      css.append(".").append(carouselId).append("-radio-").append(i).append(":checked");
      css.append(siblingChain(count - i));
      css.append("+.mj-carousel-content .mj-carousel-previous-").append(prevIdx);
      if (i < count) {
        css.append(",\n");
      } else {
        css.append(" {\n");
        css.append("  display: block !important;\n");
        css.append("}\n\n");
      }
    }
  }

  /** Appends CSS rules for thumbnail selected border, hover behavior, and visibility. */
  private void appendThumbnailCss(
      StringBuilder css,
      String carouselId,
      int count,
      String tbHoverBorderColor,
      String tbSelectedBorderColor) {
    // Active thumbnail selected border color
    if (!tbSelectedBorderColor.isEmpty()) {
      for (int i = 1; i <= count; i++) {
        css.append(".").append(carouselId).append("-radio-").append(i).append(":checked");
        css.append(siblingChain(count - i));
        css.append("+.mj-carousel-content .").append(carouselId).append("-thumbnail-").append(i);
        if (i < count) {
          css.append(",\n");
        } else {
          css.append(" {\n");
          css.append("  border-color: ").append(tbSelectedBorderColor).append(" !important;\n");
          css.append("}\n\n");
        }
      }
    }

    // Show thumbnails as inline-block when radio checked
    for (int i = 1; i <= count; i++) {
      css.append(".").append(carouselId).append("-radio-").append(i).append(":checked");
      css.append(siblingChain(count - i));
      css.append("+.mj-carousel-content .").append(carouselId).append("-thumbnail");
      if (i < count) {
        css.append(",\n");
      } else {
        css.append(" {\n");
        css.append("  display: inline-block !important;\n");
        css.append("}\n\n");
      }
    }

    // Hide image+div siblings
    css.append(".mj-carousel-image img+div,\n");
    css.append(".mj-carousel-thumbnail img+div {\n");
    css.append("  display: none !important;\n");
    css.append("}\n\n");

    // Thumbnail hover: hide all images
    for (int level = count - 1; level >= 0; level--) {
      css.append(".").append(carouselId).append("-thumbnail:hover");
      css.append(siblingChain(level));
      css.append("+.mj-carousel-main .mj-carousel-image");
      if (level > 0) {
        css.append(",\n");
      } else {
        css.append(" {\n");
        css.append("  display: none !important;\n");
        css.append("}\n\n");
      }
    }

    // Thumbnail hover border color
    if (!tbHoverBorderColor.isEmpty()) {
      css.append(".mj-carousel-thumbnail:hover {\n");
      css.append("  border-color: ").append(tbHoverBorderColor).append(" !important;\n");
      css.append("}\n\n");
    }

    // Thumbnail hover: show specific image
    for (int i = 1; i <= count; i++) {
      css.append(".").append(carouselId).append("-thumbnail-").append(i).append(":hover");
      css.append(siblingChain(count - i));
      css.append("+.mj-carousel-main .mj-carousel-image-").append(i);
      if (i < count) {
        css.append(",\n");
      } else {
        css.append(" {\n");
        css.append("  display: block !important;\n");
        css.append("}\n\n");
      }
    }
  }

  /** Appends fallback CSS rules: noinput, OWA, and Yahoo media query. */
  private void appendFallbackCss(StringBuilder css, String carouselId, int count) {
    css.append(".mj-carousel noinput {\n");
    css.append("  display: block !important;\n");
    css.append("}\n\n");

    css.append(".mj-carousel noinput .mj-carousel-image-1 {\n");
    css.append("  display: block !important;\n");
    css.append("}\n\n");

    css.append(".mj-carousel noinput .mj-carousel-arrows,\n");
    css.append(".mj-carousel noinput .mj-carousel-thumbnails {\n");
    css.append("  display: none !important;\n");
    css.append("}\n\n");

    css.append("[owa] .mj-carousel-thumbnail {\n");
    css.append("  display: none !important;\n");
    css.append("}\n\n");

    css.append("@media screen yahoo {\n\n");
    css.append("  .").append(carouselId).append("-icons-cell,\n");
    css.append("  .mj-carousel-previous-icons,\n");
    css.append("  .mj-carousel-next-icons {\n");
    css.append("    display: none !important;\n");
    css.append("  }\n\n");
    css.append("  .").append(carouselId).append("-radio-1:checked");
    css.append(siblingChain(count - 1));
    css.append("+.mj-carousel-content .").append(carouselId).append("-thumbnail-1 {\n");
    css.append("    border-color: transparent;\n");
    css.append("  }\n");
    css.append("}\n");
  }

  /**
   * Builds a sibling combinator chain of the form "+*+*+..." with the given number of "+*"
   * segments.
   */
  private String siblingChain(int starCount) {
    return "+*".repeat(starCount);
  }

  private int parseIntFromPx(String value) {
    return CssUnitParser.parseIntPx(value);
  }
}
