package dev.jcputney.mjml.component.interactive;

import static dev.jcputney.mjml.util.HtmlBuilder.attrs;

import dev.jcputney.mjml.component.BaseComponent;
import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.component.ComponentRegistry;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import dev.jcputney.mjml.util.CssUnitParser;
import dev.jcputney.mjml.util.HtmlBuilder;
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
                        buildCarouselCss(carouselId, count, iconWidthNum, tbHoverBorderColor, tbSelectedBorderColor));

        HtmlBuilder html = new HtmlBuilder();

        html.rawVerbatim("<!--[if !mso]><!-->\n");
        html.rawVerbatim("<div class=\"mj-carousel\">");

        renderRadioInputs(html, carouselId, count);

        html.rawVerbatim("\n");

        html.rawVerbatim("  <div class=\"mj-carousel-content "
                + carouselId
                + "-content\" style=\"display:table;width:100%;table-layout:fixed;text-align:center;font-size:0px;\">");

        if (showThumbnails) {
            renderThumbnails(html, images, carouselId, count);
        }

        renderMainTable(
                html, images, carouselId, count, iconWidthNum, leftIcon, rightIcon, borderRadius, containerWidth);

        html.rawVerbatim("  </div>\n");
        html.rawVerbatim("</div>");
        html.rawVerbatim("<!--<![endif]-->\n");

        renderMsoFallback(html, images, borderRadius, containerWidth);

        return html.toString();
    }

    /** Renders the hidden radio inputs that track carousel state. */
    private void renderRadioInputs(HtmlBuilder html, String carouselId, int count) {
        for (int i = 1; i <= count; i++) {
            String radioClass = "mj-carousel-radio " + carouselId + "-radio " + carouselId + "-radio-" + i;
            html.rawVerbatim("<input"
                    + attrs("class", radioClass)
                    + (i == 1 ? " checked=\"checked\"" : "")
                    + " type=\"radio\""
                    + " name=\"mj-carousel-radio-"
                    + hexId
                    + "\""
                    + " id=\""
                    + carouselId
                    + "-radio-"
                    + i
                    + "\""
                    + " style=\"display:none;mso-hide:all;\" />");
        }
    }

    /** Renders the thumbnail strip with labeled anchor elements. */
    private void renderThumbnails(HtmlBuilder html, List<MjmlNode> images, String carouselId, int count) {
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

            html.rawVerbatim("<a"
                    + attrs("style", tbStyle, "href", "#" + i, "target", "_blank", "class", tbClass)
                    + ">"
                    + "<label for=\""
                    + carouselId
                    + "-radio-"
                    + i
                    + "\">"
                    + "<img style=\"display:block;width:100%;height:auto;\""
                    + attrs("src", escapeAttr(thumbSrc), "alt", escapeAttr(alt), "width", imgWidth)
                    + " />"
                    + "</label>"
                    + "</a>");
        }
        html.rawVerbatim("\n");
    }

    /** Renders the main carousel table with previous/next icon columns and image cells. */
    private void renderMainTable(
            HtmlBuilder html,
            List<MjmlNode> images,
            String carouselId,
            int count,
            String iconWidthNum,
            String leftIcon,
            String rightIcon,
            String borderRadius,
            int containerWidth) {
        html.rawVerbatim("    <table style=\"caption-side:top;display:table-caption;table-layout:fixed;width:100%;\""
                + " border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\""
                + " role=\"presentation\" class=\"mj-carousel-main\">\n"
                + "      <tbody>\n"
                + "        <tr>\n");

        renderIconCell(html, carouselId, count, iconWidthNum, leftIcon, "previous", "mj-carousel-previous-icons");

        html.rawVerbatim("          <td style=\"padding:0px;\">\n");
        html.rawVerbatim("            <div class=\"mj-carousel-images\">\n");
        for (int i = 1; i <= count; i++) {
            MjmlNode imgNode = images.get(i - 1);
            RenderContext childContext = renderContext.withPosition(i - 1, i == 1, i == count);
            BaseComponent component = registry.createComponent(imgNode, globalContext, childContext);

            String imageHtml = renderCarouselImage(component, borderRadius, containerWidth);

            String divStyle = (i == 1) ? "" : "display:none;mso-hide:all;";
            html.rawVerbatim("              <div class=\"mj-carousel-image mj-carousel-image-"
                    + i
                    + " \" style=\""
                    + divStyle
                    + "\">"
                    + imageHtml
                    + "</div>\n");
        }
        html.rawVerbatim("            </div>\n");
        html.rawVerbatim("          </td>\n");

        renderIconCell(html, carouselId, count, iconWidthNum, rightIcon, "next", "mj-carousel-next-icons");

        html.rawVerbatim("        </tr>\n      </tbody>\n    </table>\n");
    }

    /** Renders a previous or next icon cell with labeled navigation images. */
    private void renderIconCell(
            HtmlBuilder html,
            String carouselId,
            int count,
            String iconWidthNum,
            String iconSrc,
            String direction,
            String wrapperClass) {
        html.rawVerbatim("          <td class=\""
                + carouselId
                + "-icons-cell\" style=\"font-size:0px;display:none;mso-hide:all;padding:0px;\">\n");
        html.rawVerbatim("            <div class=\"" + wrapperClass + "\" style=\"display:none;mso-hide:all;\">");
        for (int i = 1; i <= count; i++) {
            String labelClass = "mj-carousel-" + direction + " mj-carousel-" + direction + "-" + i;
            html.rawVerbatim("<label for=\""
                    + carouselId
                    + "-radio-"
                    + i
                    + "\" class=\""
                    + labelClass
                    + "\">"
                    + "<img"
                    + attrs("src", escapeAttr(iconSrc), "alt", direction)
                    + " style=\"display:block;width:"
                    + iconWidthNum
                    + "px;height:auto;\""
                    + " width=\""
                    + iconWidthNum
                    + "\" />"
                    + "</label>");
        }
        html.rawVerbatim("</div>\n");
        html.rawVerbatim("          </td>\n");
    }

    /** Renders the MSO/Outlook fallback showing only the first image. */
    private void renderMsoFallback(HtmlBuilder html, List<MjmlNode> images, String borderRadius, int containerWidth) {
        int count = images.size();
        MjmlNode firstImgNode = images.get(0);
        RenderContext firstContext = renderContext.withPosition(0, true, count == 1);
        BaseComponent firstComponent = registry.createComponent(firstImgNode, globalContext, firstContext);

        String firstImageHtml = renderCarouselImage(firstComponent, borderRadius, containerWidth);

        html.rawVerbatim("<!--[if mso]><div class=\"mj-carousel-image mj-carousel-image-1 \" style=\"\" >"
                + firstImageHtml
                + "</div><![endif]-->");
    }

    /** Renders a single carousel image from its component. */
    private String renderCarouselImage(BaseComponent component, String borderRadius, int containerWidth) {
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
    private void appendNavigationCss(StringBuilder css, String carouselId, int count) {
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
    private void appendThumbnailCss(
            StringBuilder css, String carouselId, int count, String tbHoverBorderColor, String tbSelectedBorderColor) {
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

        // Hide image+div siblings
        css.append(".mj-carousel-image img+div,\n");
        css.append(".mj-carousel-thumbnail img+div {\n");
        css.append("  display: none !important;\n");
        css.append("}\n\n");

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
            css.append(".mj-carousel-thumbnail:hover {\n");
            css.append("  border-color: ").append(tbHoverBorderColor).append(" !important;\n");
            css.append("}\n\n");
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
     * @param css the output buffer
     * @param count the number of selectors to generate
     * @param selectorWriter writes the selector for the given zero-based index
     * @param declarations the CSS declarations inside the rule block (already indented)
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
}
