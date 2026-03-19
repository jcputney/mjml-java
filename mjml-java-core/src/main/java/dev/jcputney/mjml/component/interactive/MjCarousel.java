
package dev.jcputney.mjml.component.interactive;

import dev.jcputney.mjml.component.BaseComponent;
import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.component.ComponentRegistry;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import dev.jcputney.mjml.util.CssUnitParser;
import dev.jcputney.mjml.util.HtmlBuilder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static dev.jcputney.mjml.util.HtmlBuilder.attrs;
import static dev.jcputney.mjml.util.HtmlBuilder.unsortedAttrs;

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

  private static final Map<String, String> DEFAULTS = Map.ofEntries(
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

  // Render-scoped state — set once at the start of render(), used by all helper methods
  private String carouselId;
  private List<MjmlNode> images;
  private int count;
  private String borderRadius;
  private int containerWidth;

  public MjCarousel(
    MjmlNode node, GlobalContext globalContext, RenderContext renderContext, ComponentRegistry registry) {
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
    images = node.getChildrenByTag("mj-carousel-image");
    if (images.isEmpty()) {
      return "";
    }

    count = images.size();
    carouselId = "mj-carousel-" + hexId;
    borderRadius = getAttribute("border-radius", "6px");
    containerWidth = (int) renderContext.getContainerWidth();

    String iconWidth = getAttribute("icon-width", "44px");
    String iconWidthNum = iconWidth.replace("px", "");
    boolean showThumbnails = "visible".equals(getAttribute("thumbnails", "visible"));

    globalContext.styles().addComponentStyle(buildCarouselCss(iconWidthNum));

    HtmlBuilder html = new HtmlBuilder();

    html.rawVerbatim("<!--[if !mso]><!-->\n");
    html.openInline("div", attrs("class", "mj-carousel"));
    html.indent();

    renderRadioInputs(html);
    html.newline();

    html.openInline("div", attrs("class", "mj-carousel-content " + carouselId + "-content",
      "style", "display:table;width:100%;table-layout:fixed;text-align:center;font-size:0px;"));
    html.indent();

    if (showThumbnails) {
      renderThumbnails(html);
    }

    renderMainTable(html, iconWidthNum, getAttribute("left-icon"), getAttribute("right-icon"));

    html.close("div");
    html.close("div");
    html.rawVerbatim("<!--<![endif]-->\n");

    renderMsoFallback(html);

    return html.toString();
  }

  /** Renders the hidden radio inputs that track carousel state. */
  private void renderRadioInputs(HtmlBuilder html) {
    for (int i = 1; i <= count; i++) {
      var inputAttrs = new LinkedHashMap<String, String>();
      inputAttrs.put("class", "mj-carousel-radio " + carouselId + "-radio " + carouselId + "-radio-" + i);
      if (i == 1) {
        inputAttrs.put("checked", "checked");
      }
      inputAttrs.put("type", "radio");
      inputAttrs.put("name", "mj-carousel-radio-" + hexId);
      inputAttrs.put("id", carouselId + "-radio-" + i);
      inputAttrs.put("style", "display:none;mso-hide:all;");
      html.rawVerbatim("<input" + unsortedAttrs(inputAttrs) + " />");
    }
  }

  /** Renders the thumbnail strip with labeled anchor elements. */
  private void renderThumbnails(HtmlBuilder html) {
    String tbBorder = getAttribute("tb-border", "0");
    String tbBorderRadius = getAttribute("tb-border-radius", "0");
    String tbWidth = getAttribute("tb-width", "");
    int tbWidthInt = tbWidth.isEmpty() ? 0 : CssUnitParser.parseIntPx(tbWidth);

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

      String tbStyle = "border:"
        + tbBorder
        + ";border-radius:"
        + tbBorderRadius
        + ";display:inline-block;overflow:hidden;width:"
        + (tbWidthInt > 0 ? tbWidthInt + "px" : tbWidth)
        + ";";
      String tbClass =
        "mj-carousel-thumbnail " + carouselId + "-thumbnail " + carouselId + "-thumbnail-" + i + " ";
      String imgWidth = String.valueOf(tbWidthInt > 0 ? tbWidthInt : tbWidth.replace("px", ""));

      var anchorAttrs = new LinkedHashMap<String, String>();
      anchorAttrs.put("style", tbStyle);
      anchorAttrs.put("href", "#" + i);
      anchorAttrs.put("target", "_blank");
      anchorAttrs.put("class", tbClass);

      var imgAttrs = new LinkedHashMap<String, String>();
      imgAttrs.put("style", "display:block;width:100%;height:auto;");
      imgAttrs.put("src", escapeAttr(thumbSrc));
      imgAttrs.put("alt", escapeAttr(alt));
      imgAttrs.put("width", imgWidth);

      // Thumbnail: <a><label><img/></label></a> — all inline
      html.rawVerbatim("<a" + unsortedAttrs(anchorAttrs) + ">"
        + "<label for=\"" + carouselId + "-radio-" + i + "\">"
        + "<img" + unsortedAttrs(imgAttrs) + " />"
        + "</label>"
        + "</a>");
    }
    html.newline();
  }

  /** Renders the main carousel table with previous/next icon columns and image cells. */
  private void renderMainTable(HtmlBuilder html, String iconWidthNum, String leftIcon, String rightIcon) {
    String mainTableAttrs = attrs("style", "caption-side:top;display:table-caption;table-layout:fixed;width:100%;",
      "border", "0", "cellpadding", "0", "cellspacing", "0", "width", "100%",
      "role", "presentation", "class", "mj-carousel-main");
    html.wrap("table", mainTableAttrs,
      () -> html.wrap("tbody",
        () -> html.wrap("tr", () -> {
          renderIconCell(html, iconWidthNum, leftIcon, "previous", "mj-carousel-previous-icons");

          html.wrap("td", attrs("style", "padding:0px;"),
            () -> html.wrap("div", attrs("class", "mj-carousel-images"), () -> {
              for (int i = 1; i <= count; i++) {
                MjmlNode imgNode = images.get(i - 1);
                RenderContext childContext = renderContext.withPosition(i - 1, i == 1, i == count);
                BaseComponent component = registry.createComponent(imgNode, globalContext, childContext);
                String imageHtml = renderCarouselImage(component);
                String divStyle = (i == 1) ? "" : "display:none;mso-hide:all;";
                html.openInline("div",
                  attrs("class", "mj-carousel-image mj-carousel-image-" + i + " ", "style", divStyle))
                  .text(imageHtml)
                  .closeInlineLn("div");
              }
            }));

          renderIconCell(html, iconWidthNum, rightIcon, "next", "mj-carousel-next-icons");
        })));
  }

  private void renderIconCell(HtmlBuilder html, String iconWidthNum, String iconSrc, String direction,
    String wrapperClass) {
    String tdStyle = "font-size:0px;display:none;mso-hide:all;padding:0px;";
    html.wrap("td", attrs("class", carouselId + "-icons-cell", "style", tdStyle), () -> {
      html.wrap("div", attrs("class", wrapperClass, "style", "display:none;mso-hide:all;"), () -> {
        var imgAttrs = new LinkedHashMap<String, String>();
        imgAttrs.put("src", escapeAttr(iconSrc));
        imgAttrs.put("alt", direction);
        imgAttrs.put("style", "display:block;width:" + iconWidthNum + "px;height:auto;");
        imgAttrs.put("width", iconWidthNum);

        for (int i = 1; i <= count; i++) {
          String labelClass = "mj-carousel-" + direction + " mj-carousel-" + direction + "-" + i;
          html.openInline("label", attrs("for", carouselId + "-radio-" + i, "class", labelClass));
          html.selfClose("img", unsortedAttrs(imgAttrs));
          html.closeInline("label");
        }
      });
    });
  }

  /** Renders the MSO/Outlook fallback showing only the first image. */
  private void renderMsoFallback(HtmlBuilder html) {
    MjmlNode firstImgNode = images.get(0);
    RenderContext firstContext = renderContext.withPosition(0, true, count == 1);
    BaseComponent firstComponent = registry.createComponent(firstImgNode, globalContext, firstContext);

    String firstImageHtml = renderCarouselImage(firstComponent);

    // Uses <!--[if mso]> (not mso | IE) — Outlook-only fallback
    html.rawVerbatim("<!--[if mso]>");
    html.wrap("div", attrs("class", "mj-carousel-image mj-carousel-image-1 ", "style", "") + " ",
      () -> html.text(firstImageHtml));
    html.rawVerbatim("<![endif]-->");
  }

  /** Renders a single carousel image from its component. */
  private String renderCarouselImage(BaseComponent component) {
    if (component instanceof MjCarouselImage carouselImage) {
      return carouselImage.renderImage(borderRadius, containerWidth);
    } else if (component instanceof BodyComponent bodyComponent) {
      return bodyComponent.render();
    }
    return "";
  }

  /** Builds the complete carousel CSS for injection into the document head. */
  private String buildCarouselCss(String iconWidthNum) {
    String tbHoverBorderColor = getAttribute("tb-hover-border-color", "");
    String tbSelectedBorderColor = getAttribute("tb-selected-border-color", "");

    StringBuilder css = new StringBuilder();

    appendBaseCss(css, iconWidthNum);
    appendRadioVisibilityCss(css);
    appendNavigationCss(css);
    appendThumbnailCss(css, tbHoverBorderColor, tbSelectedBorderColor);
    appendFallbackCss(css);

    return css.toString();
  }

  private void appendBaseCss(StringBuilder css, String iconWidthNum) {
    css.append("""
      .mj-carousel {
        -webkit-user-select: none;
        -moz-user-select: none;
        user-select: none;
      }

      .%s-icons-cell {
        display: table-cell !important;
        width: %spx !important;
      }

      .mj-carousel-radio,
      .mj-carousel-next,
      .mj-carousel-previous {
        display: none !important;
      }

      .mj-carousel-thumbnail,
      .mj-carousel-next,
      .mj-carousel-previous {
        touch-action: manipulation;
      }

      """.formatted(carouselId, iconWidthNum));
  }

  /** Appends CSS rules for hiding all images and showing the active image per radio state. */
  private void appendRadioVisibilityCss(StringBuilder css) {
    // Hide all images when any radio is checked
    appendCssRuleBlock(
      css,
      count,
      (sb, i) -> {
        sb.append(".").append(carouselId).append("-radio:checked");
        sb.append(siblingChain(i));
        sb.append("+.mj-carousel-content .mj-carousel-image");
      },
      "  display: none !important;\n");

    // Show specific image when its radio is checked
    appendCssRuleBlock(
      css,
      count,
      (sb, i) -> {
        int idx = i + 1;
        sb.append(".")
          .append(carouselId)
          .append("-radio-")
          .append(idx)
          .append(":checked");
        sb.append(siblingChain(count - idx));
        sb.append("+.mj-carousel-content .mj-carousel-image-").append(idx);
      },
      "  display: block !important;\n");
  }

  /** Appends CSS rules for previous/next navigation icon visibility. */
  private void appendNavigationCss(StringBuilder css) {
    css.append(".mj-carousel-previous-icons,\n");
    css.append(".mj-carousel-next-icons,\n");

    // Next: radio-i shows next-(i+1), circular — these selectors always end with comma
    for (int i = 1; i <= count; i++) {
      int nextIdx = (i % count) + 1;
      css.append(".").append(carouselId).append("-radio-").append(i).append(":checked");
      css.append(siblingChain(count - i));
      css.append("+.mj-carousel-content .mj-carousel-next-").append(nextIdx);
      css.append(",\n");
    }

    // Previous: radio-i shows previous-((i-2+N)%N+1), circular
    appendCssRuleBlock(
      css,
      count,
      (sb, i) -> {
        int idx = i + 1;
        int prevIdx = ((idx - 2 + count) % count) + 1;
        sb.append(".")
          .append(carouselId)
          .append("-radio-")
          .append(idx)
          .append(":checked");
        sb.append(siblingChain(count - idx));
        sb.append("+.mj-carousel-content .mj-carousel-previous-").append(prevIdx);
      },
      "  display: block !important;\n");
  }

  /** Appends CSS rules for thumbnail selected border, hover behavior, and visibility. */
  private void appendThumbnailCss(StringBuilder css, String tbHoverBorderColor, String tbSelectedBorderColor) {
    // Active thumbnail selected border color
    if (!tbSelectedBorderColor.isEmpty()) {
      appendCssRuleBlock(
        css,
        count,
        (sb, i) -> {
          int idx = i + 1;
          sb.append(".")
            .append(carouselId)
            .append("-radio-")
            .append(idx)
            .append(":checked");
          sb.append(siblingChain(count - idx));
          sb.append("+.mj-carousel-content .")
            .append(carouselId)
            .append("-thumbnail-")
            .append(idx);
        },
        "  border-color: " + tbSelectedBorderColor + " !important;\n");
    }

    // Show thumbnails as inline-block when radio checked
    appendCssRuleBlock(
      css,
      count,
      (sb, i) -> {
        int idx = i + 1;
        sb.append(".")
          .append(carouselId)
          .append("-radio-")
          .append(idx)
          .append(":checked");
        sb.append(siblingChain(count - idx));
        sb.append("+.mj-carousel-content .").append(carouselId).append("-thumbnail");
      },
      "  display: inline-block !important;\n");

    css.append("""
      .mj-carousel-image img+div,
      .mj-carousel-thumbnail img+div {
        display: none !important;
      }

      """);

    // Thumbnail hover: hide all images (iterates count-1 down to 0)
    appendCssRuleBlock(
      css,
      count,
      (sb, i) -> {
        int level = count - 1 - i;
        sb.append(".").append(carouselId).append("-thumbnail:hover");
        sb.append(siblingChain(level));
        sb.append("+.mj-carousel-main .mj-carousel-image");
      },
      "  display: none !important;\n");

    // Thumbnail hover border color
    if (!tbHoverBorderColor.isEmpty()) {
      css.append("""
        .mj-carousel-thumbnail:hover {
          border-color: %s !important;
        }

        """.formatted(tbHoverBorderColor));
    }

    // Thumbnail hover: show specific image
    appendCssRuleBlock(
      css,
      count,
      (sb, i) -> {
        int idx = i + 1;
        sb.append(".")
          .append(carouselId)
          .append("-thumbnail-")
          .append(idx)
          .append(":hover");
        sb.append(siblingChain(count - idx));
        sb.append("+.mj-carousel-main .mj-carousel-image-").append(idx);
      },
      "  display: block !important;\n");
  }

  /**
   * Appends a CSS rule block consisting of comma-separated selectors followed by a declaration
   * block. The selectorWriter is called with a zero-based index to generate each selector.
   *
   * @param css            the output buffer
   * @param count          the number of selectors to generate
   * @param selectorWriter writes the selector for the given zero-based index
   * @param declarations   the CSS declarations inside the rule block (already indented)
   */
  private void appendCssRuleBlock(StringBuilder css, int count, SelectorWriter selectorWriter, String declarations) {
    for (int i = 0; i < count; i++) {
      selectorWriter.write(css, i);
      if (i < count - 1) {
        css.append(",\n");
      } else {
        css.append(" {\n");
        css.append(declarations);
        css.append("}\n\n");
      }
    }
  }

  /** Functional interface for writing a single CSS selector into a StringBuilder. */
  @FunctionalInterface
  private interface SelectorWriter {
    void write(StringBuilder sb, int index);
  }

  /** Appends fallback CSS rules: noinput, OWA, and Yahoo media query. */
  private void appendFallbackCss(StringBuilder css) {
    String yahooSelector = "." + carouselId + "-radio-1:checked"
      + siblingChain(count - 1)
      + "+.mj-carousel-content ." + carouselId + "-thumbnail-1";

    css.append("""
      .mj-carousel noinput {
        display: block !important;
      }

      .mj-carousel noinput .mj-carousel-image-1 {
        display: block !important;
      }

      .mj-carousel noinput .mj-carousel-arrows,
      .mj-carousel noinput .mj-carousel-thumbnails {
        display: none !important;
      }

      [owa] .mj-carousel-thumbnail {
        display: none !important;
      }

      @media screen yahoo {

        .%s-icons-cell,
        .mj-carousel-previous-icons,
        .mj-carousel-next-icons {
          display: none !important;
        }

        %s {
          border-color: transparent;
        }
      }
      """.formatted(carouselId, yahooSelector));
  }

  /**
   * Builds a sibling combinator chain of the form "+*+*+..." with the given number of "+*"
   * segments.
   */
  private String siblingChain(int starCount) {
    return "+*".repeat(starCount);
  }
}
