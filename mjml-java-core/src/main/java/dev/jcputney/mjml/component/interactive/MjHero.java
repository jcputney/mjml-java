package dev.jcputney.mjml.component.interactive;

import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.component.ComponentRegistry;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import dev.jcputney.mjml.util.CssUnitParser;
import dev.jcputney.mjml.util.MsoHelper;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The hero component ({@code <mj-hero>}).
 * Renders a full-width hero section with a background image. Supports two modes:
 * <ul>
 *   <li>{@code fixed-height} &mdash; the hero has a fixed pixel height</li>
 *   <li>{@code fluid-height} &mdash; the hero height adapts to its content</li>
 * </ul>
 * Inner content is wrapped in a table for vertical alignment. VML background
 * is emitted for Outlook compatibility.
 */
public class MjHero extends BodyComponent {

  private static final Map<String, String> DEFAULTS = Map.ofEntries(
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
      Map.entry("mode", "fixed-height"),
      Map.entry("padding", "0px"),
      Map.entry("padding-bottom", ""),
      Map.entry("padding-left", ""),
      Map.entry("padding-right", ""),
      Map.entry("padding-top", ""),
      Map.entry("vertical-align", "top"),
      Map.entry("width", "100%")
  );

  private final ComponentRegistry registry;

  public MjHero(MjmlNode node, GlobalContext globalContext, RenderContext renderContext,
      ComponentRegistry registry) {
    super(node, globalContext, renderContext);
    this.registry = registry;
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
    String mode = getAttribute("mode", "fixed-height");
    if ("fluid-height".equals(mode)) {
      return renderFluidHeight();
    }
    return renderFixedHeight();
  }

  /**
   * Renders a fixed-height hero. The content td has an explicit height derived
   * from the declared height minus vertical padding.
   */
  private String renderFixedHeight() {
    String height = getAttribute("height", "");
    String padding = getAttribute("padding", "0px");

    // Compute inner height: declared height - top padding - bottom padding
    int innerHeight = 0;
    if (!height.isEmpty()) {
      int paddingTop = parsePaddingPart(padding, 0);
      int paddingBottom = parsePaddingPart(padding, 2);
      int h = parseIntPx(height);
      innerHeight = Math.max(0, h - paddingTop - paddingBottom);
    }

    // v:image uses the declared height
    String vImageHeight = height;
    String spacerPaddingPct = null;

    return renderHero(vImageHeight, innerHeight, spacerPaddingPct);
  }

  /**
   * Renders a fluid-height hero. Height adapts to content; spacer tds with
   * padding-bottom percentage maintain the background aspect ratio.
   */
  private String renderFluidHeight() {
    String bgHeight = getAttribute("background-height", "");
    int containerWidth = globalContext.getContainerWidth();

    // Compute padding-bottom percentage for fluid aspect ratio
    double paddingPct = 0;
    if (!bgHeight.isEmpty()) {
      int bgH = parseIntPx(bgHeight);
      String bgWidth = getAttribute("background-width", "");
      int bgW = !bgWidth.isEmpty() ? parseIntPx(bgWidth) : containerWidth;
      paddingPct = ((double) bgH / bgW) * 100.0;
    }

    // v:image uses background-height
    String vImageHeight = bgHeight;
    int innerHeight = 0;
    String spacerPaddingPct = paddingPct > 0
        ? String.valueOf(Math.round(paddingPct))
        : null;

    return renderHero(vImageHeight, innerHeight, spacerPaddingPct);
  }

  /**
   * Shared hero rendering logic for both fixed and fluid modes.
   *
   * @param vImageHeight    height for the v:image element (empty string = omit)
   * @param innerHeight     explicit height in px for the content td (0 = omit)
   * @param spacerPaddingPct if non-null, adds spacer tds with this padding-bottom %
   */
  private String renderHero(String vImageHeight, int innerHeight, String spacerPaddingPct) {
    StringBuilder sb = new StringBuilder();

    String backgroundColor = getAttribute("background-color", "#ffffff");
    String backgroundUrl = getAttribute("background-url", "");
    String backgroundPosition = getAttribute("background-position", "center center");
    String verticalAlign = getAttribute("vertical-align", "top");
    String padding = getAttribute("padding", "0px");
    int containerWidth = globalContext.getContainerWidth();

    // MSO wrapper with v:image
    sb.append("    <!--[if mso | IE]><table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"width:")
        .append(containerWidth).append("px;\" width=\"").append(containerWidth)
        .append("\" ><tr><td style=\"line-height:0;font-size:0;mso-line-height-rule:exactly;\">");
    appendVmlImage(sb, backgroundUrl, vImageHeight, containerWidth);
    sb.append("<![endif]-->\n");

    // Outer div
    sb.append("    <div style=\"margin:0 auto;max-width:").append(containerWidth).append("px;\">\n");

    // Table with vertical-align row
    sb.append("      <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"width:100%;\">\n");
    sb.append("        <tbody>\n");
    sb.append("          <tr style=\"vertical-align:top;\">\n");

    // Spacer td before content (fluid mode only)
    if (spacerPaddingPct != null) {
      appendSpacerTd(sb, spacerPaddingPct);
    }

    // Main content td with background
    appendContentTd(sb, backgroundUrl, backgroundColor, backgroundPosition,
        padding, verticalAlign, innerHeight);

    // MSO inner table for content
    sb.append("              <!--[if mso | IE]><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"width:")
        .append(containerWidth).append("px;\" width=\"").append(containerWidth)
        .append("\" ><tr><td style=\"\"><![endif]-->\n");

    // Hero content wrapper + children
    appendHeroContent(sb);

    // Close MSO inner table
    sb.append("              ").append(MsoHelper.msoConditionalTableClosing()).append("\n");

    sb.append("            </td>\n");

    // Spacer td after content (fluid mode only)
    if (spacerPaddingPct != null) {
      appendSpacerTd(sb, spacerPaddingPct);
    }

    sb.append("          </tr>\n");
    sb.append("        </tbody>\n");
    sb.append("      </table>\n");
    sb.append("    </div>\n");

    // Close MSO outer table
    sb.append("    ").append(MsoHelper.msoConditionalTableClosing()).append("\n");

    return sb.toString();
  }

  /**
   * Appends the VML v:image element for Outlook, if a background URL is set.
   */
  private void appendVmlImage(StringBuilder sb, String backgroundUrl,
      String vImageHeight, int containerWidth) {
    if (!backgroundUrl.isEmpty()) {
      sb.append("<v:image style=\"border:0;");
      if (!vImageHeight.isEmpty()) {
        sb.append("height:").append(escapeAttr(vImageHeight)).append(";");
      }
      sb.append("mso-position-horizontal:center;position:absolute;top:0;width:")
          .append(containerWidth).append("px;z-index:-3;\" src=\"")
          .append(escapeAttr(backgroundUrl)).append("\" xmlns:v=\"urn:schemas-microsoft-com:vml\" />");
    }
  }

  /**
   * Appends a spacer td used in fluid mode for aspect-ratio padding.
   */
  private void appendSpacerTd(StringBuilder sb, String paddingPct) {
    sb.append("            <td style=\"width:0.01%;padding-bottom:").append(paddingPct)
        .append("%;mso-padding-bottom-alt:0;\" />\n");
  }

  /**
   * Appends the main content td with background styles and optional height.
   */
  private void appendContentTd(StringBuilder sb, String backgroundUrl, String backgroundColor,
      String backgroundPosition, String padding, String verticalAlign, int innerHeight) {
    sb.append("            <td");
    if (!backgroundUrl.isEmpty()) {
      sb.append(" background=\"").append(escapeAttr(backgroundUrl)).append("\"");
    }
    sb.append(" style=\"");
    Map<String, String> tdStyles = new LinkedHashMap<>();
    tdStyles.put("background", buildBackgroundValue(backgroundUrl, backgroundColor, backgroundPosition));
    if (!backgroundUrl.isEmpty()) {
      tdStyles.put("background-position", backgroundPosition);
      tdStyles.put("background-repeat", "no-repeat");
    }
    tdStyles.put("padding", padding);
    tdStyles.put("vertical-align", verticalAlign);
    if (innerHeight > 0) {
      tdStyles.put("height", innerHeight + "px");
    }
    sb.append(buildStyle(tdStyles)).append("\"");
    if (innerHeight > 0) {
      sb.append(" height=\"").append(innerHeight).append("\"");
    }
    sb.append(">\n");
  }

  /**
   * Appends the hero content wrapper div and child component rows.
   */
  private void appendHeroContent(StringBuilder sb) {
    sb.append("              <div class=\"mj-hero-content\" style=\"margin:0px auto;\">\n");
    sb.append("                <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"width:100%;margin:0px;\">\n");
    sb.append("                  <tbody>\n");
    sb.append("                    <tr>\n");
    sb.append("                      <td style=\"\">\n");
    sb.append("                        <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"width:100%;margin:0px;\">\n");
    sb.append("                          <tbody>\n");
    sb.append(renderChildrenAsRows());
    sb.append("                          </tbody>\n");
    sb.append("                        </table>\n");
    sb.append("                      </td>\n");
    sb.append("                    </tr>\n");
    sb.append("                  </tbody>\n");
    sb.append("                </table>\n");
    sb.append("              </div>\n");
  }

  /**
   * Renders each child component inside its own table row with padding.
   */
  private String renderChildrenAsRows() {
    StringBuilder sb = new StringBuilder();

    for (MjmlNode child : node.getChildren()) {
      if (child.getTagName().startsWith("#")) {
        continue;
      }

      sb.append("                          <tr>\n");

      // Get the child's padding attribute
      String childPadding = child.getAttribute("padding");
      if (childPadding == null || childPadding.isEmpty()) {
        childPadding = "10px 25px";
      }

      String align = child.getAttribute("align");
      if (align == null || align.isEmpty()) {
        align = "center";
      }

      sb.append("                            <td align=\"").append(align)
          .append("\" style=\"font-size:0px;padding:").append(childPadding)
          .append(";word-break:break-word;\">\n");

      // Render the child component
      var component = registry.createComponent(child, globalContext, renderContext);
      if (component instanceof BodyComponent bodyComponent) {
        String rendered = bodyComponent.render();
        sb.append(rendered);
        if (!rendered.endsWith("\n")) {
          sb.append("\n");
        }
      }

      sb.append("                            </td>\n");
      sb.append("                          </tr>\n");
    }

    return sb.toString();
  }

  private String buildBackgroundValue(String url, String color, String position) {
    if (url == null || url.isEmpty()) {
      return color;
    }
    return color + " url('" + url + "') no-repeat " + position + " / cover";
  }

  private static int parseIntPx(String value) {
    return CssUnitParser.parseIntPx(value);
  }

  private static int parsePaddingPart(String padding, int index) {
    return (int) CssUnitParser.parseShorthand(padding)[index];
  }
}
