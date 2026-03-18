package dev.jcputney.mjml.component.interactive;

import static dev.jcputney.mjml.util.HtmlBuilder.attrIf;
import static dev.jcputney.mjml.util.HtmlBuilder.attrs;

import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import dev.jcputney.mjml.util.HtmlBuilder;
import java.util.Map;

/**
 * A single image in a carousel ({@code <mj-carousel-image>}). Renders an image element with an
 * optional link wrapper. The {@code thumbnails-src} attribute can specify an alternate thumbnail
 * image for the carousel navigation.
 *
 * <p>When rendered by the parent MjCarousel, each image produces output like:
 *
 * <pre>{@code
 * <a href="HREF" target="TARGET">
 *   <img title="TITLE" src="SRC" alt="ALT"
 *        style="border-radius:RADIUS;display:block;width:WIDTHpx;max-width:100%;height:auto;"
 *        width="WIDTH_INT" border="0" />
 * </a>
 * }</pre>
 */
public class MjCarouselImage extends BodyComponent {

    private static final Map<String, String> DEFAULTS = Map.ofEntries(
            Map.entry("alt", ""),
            Map.entry("css-class", ""),
            Map.entry("href", ""),
            Map.entry("rel", ""),
            Map.entry("src", ""),
            Map.entry("target", "_blank"),
            Map.entry("tb-border", ""),
            Map.entry("tb-border-radius", ""),
            Map.entry("tb-hover-border-color", ""),
            Map.entry("tb-selected-border-color", ""),
            Map.entry("title", ""),
            Map.entry("thumbnails-src", ""));

    public MjCarouselImage(MjmlNode node, GlobalContext globalContext, RenderContext renderContext) {
        super(node, globalContext, renderContext);
    }

    @Override
    public String getTagName() {
        return "mj-carousel-image";
    }

    @Override
    public Map<String, String> getDefaultAttributes() {
        return DEFAULTS;
    }

    /**
     * Renders this carousel image. The parent MjCarousel calls this method and wraps the result in
     * the appropriate carousel image div.
     *
     * @param borderRadius the border-radius inherited from the parent mj-carousel
     * @param width the pixel width of the image (container width)
     * @return the rendered HTML for this image
     */
    public String renderImage(String borderRadius, int width) {
        String src = getAttribute("src", "");
        String alt = getAttribute("alt", "");
        String title = getAttribute("title", "");
        String href = sanitizeHref(getAttribute("href", ""));
        String target = getAttribute("target", "_blank");

        // Build inline style for the image
        String imgStyle = (borderRadius.isEmpty() ? "" : "border-radius:" + borderRadius + ";")
                + "display:block;width:"
                + width
                + "px;max-width:100%;height:auto;";

        String imgTag = "<img"
                + attrIf("title", escapeAttr(title))
                + attrs("src", escapeAttr(src), "alt", escapeAttr(alt), "style", imgStyle)
                + attrs("width", String.valueOf(width))
                + attrs("border", "0")
                + " />";

        if (!href.isEmpty()) {
            HtmlBuilder html = new HtmlBuilder();
            html.openInline("a", attrs("href", escapeHref(href), "target", escapeAttr(target)))
                    .text(imgTag)
                    .closeInline("a");
            return html.toString();
        }

        return imgTag;
    }

    @Override
    public String render() {
        // When rendered standalone (not via carousel parent), use container width
        int width = (int) renderContext.getContainerWidth();
        return renderImage("", width);
    }
}
