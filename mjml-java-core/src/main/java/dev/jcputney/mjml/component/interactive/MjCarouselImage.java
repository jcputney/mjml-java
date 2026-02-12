package dev.jcputney.mjml.component.interactive;

import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
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

  private static final Map<String, String> DEFAULTS =
      Map.ofEntries(
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

  /**
   * Creates a new MjCarouselImage component.
   *
   * @param node the parsed MJML node for this component
   * @param globalContext the global rendering context
   * @param renderContext the current render context
   */
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

    StringBuilder img = new StringBuilder();
    img.append("<img");
    if (!title.isEmpty()) {
      img.append(" title=\"").append(escapeAttr(title)).append("\"");
    }
    img.append(" src=\"").append(escapeAttr(src)).append("\"");
    img.append(" alt=\"").append(escapeAttr(alt)).append("\"");
    img.append(" style=\"");
    if (!borderRadius.isEmpty()) {
      img.append("border-radius:").append(borderRadius).append(";");
    }
    img.append("display:block;width:").append(width).append("px;max-width:100%;height:auto;\"");
    img.append(" width=\"").append(width).append("\"");
    img.append(" border=\"0\"");
    img.append(" />");

    if (!href.isEmpty()) {
      String sb =
          "<a href=\""
              + escapeHref(href)
              + "\""
              + " target=\""
              + escapeAttr(target)
              + "\""
              + ">"
              + img
              + "</a>";
      return sb;
    }

    return img.toString();
  }

  @Override
  public String render() {
    // When rendered standalone (not via carousel parent), use container width
    int width = (int) renderContext.getContainerWidth();
    return renderImage("", width);
  }
}
