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
 * The carousel component ({@code <mj-carousel>}).
 * Uses CSS radio buttons to implement a pure-CSS image carousel with
 * previous/next navigation and thumbnail strips. Generates unique hex IDs
 * per instance and injects carousel CSS into the global context.
 *
 * <p>The output matches the official MJML v4.18.0 carousel structure:
 * radio inputs for state, sibling combinator CSS for toggling visibility,
 * thumbnail anchors with labels, and table-based main content with
 * previous/next icon columns.
 */
public class MjCarousel extends BodyComponent {

  private static final Map<String, String> DEFAULTS = Map.ofEntries(
      Map.entry("align", "center"),
      Map.entry("background-color", ""),
      Map.entry("border-radius", "6px"),
      Map.entry("icon-width", "44px"),
      Map.entry("left-icon", "https://i.imgur.com/xTh3hln.png"),
      Map.entry("right-icon", "https://i.imgur.com/os7o9kz.png"),
      Map.entry("padding", ""),
      Map.entry("tb-border", "0"),
      Map.entry("tb-border-radius", "0"),
      Map.entry("tb-hover-border-color", ""),
      Map.entry("tb-selected-border-color", ""),
      Map.entry("tb-width", ""),
      Map.entry("thumbnails", "visible")
  );

  private final ComponentRegistry registry;
  private final String hexId;

  public MjCarousel(MjmlNode node, GlobalContext globalContext, RenderContext renderContext,
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
    String tbBorder = getAttribute("tb-border", "0");
    String tbBorderRadius = getAttribute("tb-border-radius", "0");
    String tbWidth = getAttribute("tb-width", "");
    String tbHoverBorderColor = getAttribute("tb-hover-border-color", "");
    String tbSelectedBorderColor = getAttribute("tb-selected-border-color", "");
    boolean showThumbnails = "visible".equals(getAttribute("thumbnails", "visible"));

    int containerWidth = (int) renderContext.getContainerWidth();

    // Inject carousel CSS into global context
    globalContext.addComponentStyle(
        buildCarouselCss(carouselId, count, iconWidthNum, tbHoverBorderColor,
            tbSelectedBorderColor));

    StringBuilder sb = new StringBuilder();

    // --- Start non-MSO conditional ---
    sb.append("<!--[if !mso]><!-->\n");
    sb.append("<div class=\"mj-carousel\">");

    // Radio inputs (all on one line, no extra whitespace between them)
    for (int i = 1; i <= count; i++) {
      sb.append("<input class=\"mj-carousel-radio ").append(carouselId).append("-radio ")
          .append(carouselId).append("-radio-").append(i).append("\"");
      if (i == 1) {
        sb.append(" checked=\"checked\"");
      }
      sb.append(" type=\"radio\"");
      sb.append(" name=\"mj-carousel-radio-").append(hexId).append("\"");
      sb.append(" id=\"").append(carouselId).append("-radio-").append(i).append("\"");
      sb.append(" style=\"display:none;mso-hide:all;\"");
      sb.append(" />");
    }

    sb.append("\n");

    // Content div
    sb.append("  <div class=\"mj-carousel-content ").append(carouselId)
        .append("-content\" style=\"display:table;width:100%;table-layout:fixed;text-align:center;font-size:0px;\">");

    // Thumbnails (inline-block anchors)
    if (showThumbnails) {
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

        int tbWidthInt = tbWidth.isEmpty() ? 0 : parseIntFromPx(tbWidth);

        sb.append("<a style=\"border:").append(tbBorder)
            .append(";border-radius:").append(tbBorderRadius)
            .append(";display:inline-block;overflow:hidden;width:")
            .append(tbWidthInt > 0 ? tbWidthInt + "px" : tbWidth).append(";\"");
        sb.append(" href=\"#").append(i).append("\"");
        sb.append(" target=\"_blank\"");
        sb.append(" class=\"mj-carousel-thumbnail ").append(carouselId).append("-thumbnail ")
            .append(carouselId).append("-thumbnail-").append(i).append(" \">");
        sb.append("<label for=\"").append(carouselId).append("-radio-").append(i).append("\">");
        sb.append("<img style=\"display:block;width:100%;height:auto;\"");
        sb.append(" src=\"").append(thumbSrc).append("\"");
        sb.append(" alt=\"").append(alt).append("\"");
        sb.append(" width=\"").append(tbWidthInt > 0 ? tbWidthInt : tbWidth.replace("px", ""))
            .append("\"");
        sb.append(" />");
        sb.append("</label>");
        sb.append("</a>");
      }
      sb.append("\n");
    }

    // Main carousel table
    sb.append("    <table style=\"caption-side:top;display:table-caption;table-layout:fixed;width:100%;\"");
    sb.append(" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"");
    sb.append(" role=\"presentation\" class=\"mj-carousel-main\">\n");
    sb.append("      <tbody>\n");
    sb.append("        <tr>\n");

    // Previous icons cell
    sb.append("          <td class=\"").append(carouselId)
        .append("-icons-cell\" style=\"font-size:0px;display:none;mso-hide:all;padding:0px;\">\n");
    sb.append("            <div class=\"mj-carousel-previous-icons\" style=\"display:none;mso-hide:all;\">");
    for (int i = 1; i <= count; i++) {
      sb.append("<label for=\"").append(carouselId).append("-radio-").append(i).append("\"");
      sb.append(" class=\"mj-carousel-previous mj-carousel-previous-").append(i).append("\">");
      sb.append("<img src=\"").append(leftIcon).append("\"");
      sb.append(" alt=\"previous\"");
      sb.append(" style=\"display:block;width:").append(iconWidthNum).append("px;height:auto;\"");
      sb.append(" width=\"").append(iconWidthNum).append("\"");
      sb.append(" />");
      sb.append("</label>");
    }
    sb.append("</div>\n");
    sb.append("          </td>\n");

    // Images cell
    sb.append("          <td style=\"padding:0px;\">\n");
    sb.append("            <div class=\"mj-carousel-images\">\n");
    for (int i = 1; i <= count; i++) {
      MjmlNode imgNode = images.get(i - 1);
      RenderContext childContext = renderContext.withPosition(i - 1, i == 1, i == count);
      BaseComponent component = registry.createComponent(imgNode, globalContext, childContext);

      String imageHtml;
      if (component instanceof MjCarouselImage carouselImage) {
        imageHtml = carouselImage.renderImage(borderRadius, containerWidth);
      } else if (component instanceof BodyComponent bodyComponent) {
        imageHtml = bodyComponent.render();
      } else {
        imageHtml = "";
      }

      sb.append("              <div class=\"mj-carousel-image mj-carousel-image-").append(i)
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
    sb.append("          <td class=\"").append(carouselId)
        .append("-icons-cell\" style=\"font-size:0px;display:none;mso-hide:all;padding:0px;\">\n");
    sb.append("            <div class=\"mj-carousel-next-icons\" style=\"display:none;mso-hide:all;\">");
    for (int i = 1; i <= count; i++) {
      sb.append("<label for=\"").append(carouselId).append("-radio-").append(i).append("\"");
      sb.append(" class=\"mj-carousel-next mj-carousel-next-").append(i).append("\">");
      sb.append("<img src=\"").append(rightIcon).append("\"");
      sb.append(" alt=\"next\"");
      sb.append(" style=\"display:block;width:").append(iconWidthNum).append("px;height:auto;\"");
      sb.append(" width=\"").append(iconWidthNum).append("\"");
      sb.append(" />");
      sb.append("</label>");
    }
    sb.append("</div>\n");
    sb.append("          </td>\n");

    sb.append("        </tr>\n");
    sb.append("      </tbody>\n");
    sb.append("    </table>\n");
    sb.append("  </div>\n");
    sb.append("</div>");
    sb.append("<!--<![endif]-->\n");

    // MSO fallback: show only the first image
    MjmlNode firstImgNode = images.get(0);
    RenderContext firstContext = renderContext.withPosition(0, true, count == 1);
    BaseComponent firstComponent = registry.createComponent(firstImgNode, globalContext,
        firstContext);
    String firstImageHtml;
    if (firstComponent instanceof MjCarouselImage carouselImage) {
      firstImageHtml = carouselImage.renderImage(borderRadius, containerWidth);
    } else if (firstComponent instanceof BodyComponent bodyComponent) {
      firstImageHtml = bodyComponent.render();
    } else {
      firstImageHtml = "";
    }

    sb.append("<!--[if mso]><div class=\"mj-carousel-image mj-carousel-image-1 \" style=\"\" >");
    sb.append(firstImageHtml);
    sb.append("</div><![endif]-->");

    return sb.toString();
  }

  /**
   * Builds the complete carousel CSS for injection into the document head.
   * This follows the exact structure from official MJML v4.18.0.
   */
  private String buildCarouselCss(String carouselId, int count, String iconWidthNum,
      String tbHoverBorderColor, String tbSelectedBorderColor) {
    StringBuilder css = new StringBuilder();

    // Base carousel styles
    css.append(".mj-carousel {\n");
    css.append("  -webkit-user-select: none;\n");
    css.append("  -moz-user-select: none;\n");
    css.append("  user-select: none;\n");
    css.append("}\n\n");

    // Icons cell width
    css.append(".").append(carouselId).append("-icons-cell {\n");
    css.append("  display: table-cell !important;\n");
    css.append("  width: ").append(iconWidthNum).append("px !important;\n");
    css.append("}\n\n");

    // Hide radio/next/previous by default
    css.append(".mj-carousel-radio,\n");
    css.append(".mj-carousel-next,\n");
    css.append(".mj-carousel-previous {\n");
    css.append("  display: none !important;\n");
    css.append("}\n\n");

    // Touch action for interaction elements
    css.append(".mj-carousel-thumbnail,\n");
    css.append(".mj-carousel-next,\n");
    css.append(".mj-carousel-previous {\n");
    css.append("  touch-action: manipulation;\n");
    css.append("}\n\n");

    // Hide all images when any radio is checked (sibling combinator chain)
    // For N images, we need selectors with 0 to N-1 +* levels
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
    // radio-i has (N - i) +* before +.mj-carousel-content
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

    // Show previous/next navigation icons
    css.append(".mj-carousel-previous-icons,\n");
    css.append(".mj-carousel-next-icons,\n");

    // Next navigation: radio-i shows next-(i+1), circular
    for (int i = 1; i <= count; i++) {
      int nextIdx = (i % count) + 1; // circular: 1->2, 2->3, ..., N->1
      css.append(".").append(carouselId).append("-radio-").append(i).append(":checked");
      css.append(siblingChain(count - i));
      css.append("+.mj-carousel-content .mj-carousel-next-").append(nextIdx);
      css.append(",\n");
    }

    // Previous navigation: radio-i shows previous-((i-2+N)%N+1), circular
    // radio-1 -> previous-N, radio-2 -> previous-1, radio-3 -> previous-2, etc.
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

    // Show active thumbnail with selected border color
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
    // Thumbnails are siblings before .mj-carousel-main, so from thumbnail-1 we need
    // (N-1) +* to reach .mj-carousel-main, decreasing for each subsequent thumbnail
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
    // thumbnail-i is at position i (1-indexed) among N thumbnails before .mj-carousel-main
    // From thumbnail-i, there are (N - i) more thumbnails, then .mj-carousel-main
    // So it's (N - i) +* then +.mj-carousel-main
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

    // noinput fallback rules
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

    // OWA rule
    css.append("[owa] .mj-carousel-thumbnail {\n");
    css.append("  display: none !important;\n");
    css.append("}\n\n");

    // Yahoo media query
    css.append("@media screen yahoo {\n\n");
    css.append("  .").append(carouselId).append("-icons-cell,\n");
    css.append("  .mj-carousel-previous-icons,\n");
    css.append("  .mj-carousel-next-icons {\n");
    css.append("    display: none !important;\n");
    css.append("  }\n\n");
    // Only first radio thumbnail gets transparent border in yahoo
    css.append("  .").append(carouselId).append("-radio-1:checked");
    css.append(siblingChain(count - 1));
    css.append("+.mj-carousel-content .").append(carouselId).append("-thumbnail-1 {\n");
    css.append("    border-color: transparent;\n");
    css.append("  }\n");
    css.append("}\n");

    return css.toString();
  }

  /**
   * Builds a sibling combinator chain of the form "+*+*+..." with the given number
   * of "+*" segments.
   */
  private String siblingChain(int starCount) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < starCount; i++) {
      sb.append("+*");
    }
    return sb.toString();
  }

  private int parseIntFromPx(String value) {
    return CssUnitParser.parseIntPx(value);
  }
}
