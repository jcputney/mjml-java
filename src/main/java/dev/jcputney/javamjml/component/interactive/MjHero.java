package dev.jcputney.javamjml.component.interactive;

import dev.jcputney.javamjml.component.BodyComponent;
import dev.jcputney.javamjml.component.ComponentRegistry;
import dev.jcputney.javamjml.context.GlobalContext;
import dev.jcputney.javamjml.context.RenderContext;
import dev.jcputney.javamjml.parser.MjmlNode;
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
      Map.entry("height", ""),
      Map.entry("mode", "fixed-height"),
      Map.entry("padding", "0px"),
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
   * Renders a fixed-height hero. Structure:
   * MSO table + v:image -> outer div (with bg, height) ->
   *   inner table -> tr style=vertical-align:top ->
   *     td (background, padding, vertical-align, height) ->
   *       MSO table -> mj-hero-content div -> table -> children table (one tr per child)
   *     -> close MSO table
   *   -> close outer div -> close MSO table
   */
  private String renderFixedHeight() {
    StringBuilder sb = new StringBuilder();

    String backgroundColor = getAttribute("background-color", "#ffffff");
    String backgroundUrl = getAttribute("background-url", "");
    String backgroundPosition = getAttribute("background-position", "center center");
    String height = getAttribute("height", "");
    String verticalAlign = getAttribute("vertical-align", "top");
    String padding = getAttribute("padding", "0px");
    int containerWidth = globalContext.getContainerWidth();

    // Compute inner height for the td height attribute
    // In MJML, the td height = declared height - top padding - bottom padding
    int paddingTop = parsePaddingPart(padding, 0);
    int paddingBottom = parsePaddingPart(padding, 2);
    int innerHeight = 0;
    if (!height.isEmpty()) {
      int h = parseIntPx(height);
      innerHeight = h - paddingTop - paddingBottom;
      if (innerHeight < 0) {
        innerHeight = 0;
      }
    }

    // MSO wrapper with v:image
    sb.append("    <!--[if mso | IE]><table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"width:")
        .append(containerWidth).append("px;\" width=\"").append(containerWidth)
        .append("\" ><tr><td style=\"line-height:0;font-size:0;mso-line-height-rule:exactly;\">");

    if (!backgroundUrl.isEmpty()) {
      sb.append("<v:image style=\"border:0;");
      if (!height.isEmpty()) {
        sb.append("height:").append(height).append(";");
      }
      sb.append("mso-position-horizontal:center;position:absolute;top:0;width:")
          .append(containerWidth).append("px;z-index:-3;\" src=\"")
          .append(backgroundUrl).append("\" xmlns:v=\"urn:schemas-microsoft-com:vml\" />");
    }
    sb.append("<![endif]-->\n");

    // Outer div
    sb.append("    <div style=\"margin:0 auto;max-width:").append(containerWidth).append("px;\">\n");

    // Table with vertical-align row
    sb.append("      <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"width:100%;\">\n");
    sb.append("        <tbody>\n");
    sb.append("          <tr style=\"vertical-align:top;\">\n");

    // Main content td with background
    sb.append("            <td");
    if (!backgroundUrl.isEmpty()) {
      sb.append(" background=\"").append(backgroundUrl).append("\"");
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

    // MSO inner table for content
    sb.append("              <!--[if mso | IE]><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"width:")
        .append(containerWidth).append("px;\" width=\"").append(containerWidth)
        .append("\" ><tr><td style=\"\"><![endif]-->\n");

    // Hero content wrapper
    sb.append("              <div class=\"mj-hero-content\" style=\"margin:0px auto;\">\n");

    // Inner tables for children
    sb.append("                <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"width:100%;margin:0px;\">\n");
    sb.append("                  <tbody>\n");
    sb.append("                    <tr>\n");
    sb.append("                      <td style=\"\">\n");

    sb.append("                        <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"width:100%;margin:0px;\">\n");
    sb.append("                          <tbody>\n");

    // Render each child in its own tr
    sb.append(renderChildrenAsRows());

    sb.append("                          </tbody>\n");
    sb.append("                        </table>\n");
    sb.append("                      </td>\n");
    sb.append("                    </tr>\n");
    sb.append("                  </tbody>\n");
    sb.append("                </table>\n");

    sb.append("              </div>\n");

    // Close MSO inner table
    sb.append("              <!--[if mso | IE]></td></tr></table><![endif]-->\n");

    sb.append("            </td>\n");
    sb.append("          </tr>\n");
    sb.append("        </tbody>\n");
    sb.append("      </table>\n");
    sb.append("    </div>\n");

    // Close MSO outer table
    sb.append("    <!--[if mso | IE]></td></tr></table><![endif]-->\n");

    return sb.toString();
  }

  /**
   * Renders a fluid-height hero. Structure is similar to fixed-height but with:
   * - A spacer td with padding-bottom percentage for aspect ratio (before AND after content)
   * - No explicit height on the content td
   * - The v:image height uses background-height
   */
  private String renderFluidHeight() {
    StringBuilder sb = new StringBuilder();

    String backgroundColor = getAttribute("background-color", "#ffffff");
    String backgroundUrl = getAttribute("background-url", "");
    String backgroundPosition = getAttribute("background-position", "center center");
    String verticalAlign = getAttribute("vertical-align", "top");
    String padding = getAttribute("padding", "0px");
    int containerWidth = globalContext.getContainerWidth();

    // Compute padding-bottom percentage for fluid aspect ratio
    // Formula: (backgroundHeight / backgroundWidth) * 100
    String bgHeight = getAttribute("background-height", "");
    double paddingPct = 0;
    if (!bgHeight.isEmpty()) {
      int bgH = parseIntPx(bgHeight);
      String bgWidth = getAttribute("background-width", "");
      int bgW = !bgWidth.isEmpty() ? parseIntPx(bgWidth) : containerWidth;
      paddingPct = ((double) bgH / bgW) * 100.0;
    }

    // MSO wrapper with v:image (no height for fluid)
    sb.append("    <!--[if mso | IE]><table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"width:")
        .append(containerWidth).append("px;\" width=\"").append(containerWidth)
        .append("\" ><tr><td style=\"line-height:0;font-size:0;mso-line-height-rule:exactly;\">");

    if (!backgroundUrl.isEmpty()) {
      sb.append("<v:image style=\"border:0;");
      if (!bgHeight.isEmpty()) {
        sb.append("height:").append(bgHeight).append(";");
      }
      sb.append("mso-position-horizontal:center;position:absolute;top:0;width:")
          .append(containerWidth).append("px;z-index:-3;\" src=\"")
          .append(backgroundUrl).append("\" xmlns:v=\"urn:schemas-microsoft-com:vml\" />");
    }
    sb.append("<![endif]-->\n");

    // Outer div
    sb.append("    <div style=\"margin:0 auto;max-width:").append(containerWidth).append("px;\">\n");

    // Table
    sb.append("      <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"width:100%;\">\n");
    sb.append("        <tbody>\n");
    sb.append("          <tr style=\"vertical-align:top;\">\n");

    // Format percentage for spacer tds (MJML uses integer rounding)
    String spacerPctStr = "";
    if (paddingPct > 0) {
      spacerPctStr = String.valueOf(Math.round(paddingPct));
    }

    // Spacer td for fluid aspect ratio (before content)
    if (paddingPct > 0) {
      sb.append("            <td style=\"width:0.01%;padding-bottom:").append(spacerPctStr)
          .append("%;mso-padding-bottom-alt:0;\" />\n");
    }

    // Main content td
    sb.append("            <td");
    if (!backgroundUrl.isEmpty()) {
      sb.append(" background=\"").append(backgroundUrl).append("\"");
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
    sb.append(buildStyle(tdStyles)).append("\">\n");

    // MSO inner table
    sb.append("              <!--[if mso | IE]><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"width:")
        .append(containerWidth).append("px;\" width=\"").append(containerWidth)
        .append("\" ><tr><td style=\"\"><![endif]-->\n");

    // Hero content wrapper
    sb.append("              <div class=\"mj-hero-content\" style=\"margin:0px auto;\">\n");

    // Inner tables for children
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

    // Close MSO inner table
    sb.append("              <!--[if mso | IE]></td></tr></table><![endif]-->\n");

    sb.append("            </td>\n");

    // Second spacer td (after content) for fluid aspect ratio
    if (paddingPct > 0) {
      sb.append("            <td style=\"width:0.01%;padding-bottom:").append(spacerPctStr)
          .append("%;mso-padding-bottom-alt:0;\" />\n");
    }

    sb.append("          </tr>\n");
    sb.append("        </tbody>\n");
    sb.append("      </table>\n");
    sb.append("    </div>\n");

    // Close MSO outer table
    sb.append("    <!--[if mso | IE]></td></tr></table><![endif]-->\n");

    return sb.toString();
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
    if (value == null || value.isEmpty()) {
      return 0;
    }
    try {
      return Integer.parseInt(value.replaceAll("[^0-9-]", ""));
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  /**
   * Parses a specific part of a CSS padding shorthand.
   * Index: 0=top, 1=right, 2=bottom, 3=left
   */
  private static int parsePaddingPart(String padding, int index) {
    if (padding == null || padding.isEmpty()) {
      return 0;
    }
    String[] parts = padding.trim().split("\\s+");
    String value;
    switch (parts.length) {
      case 1:
        value = parts[0];
        break;
      case 2:
        value = (index == 0 || index == 2) ? parts[0] : parts[1];
        break;
      case 3:
        if (index == 0) {
          value = parts[0];
        } else if (index == 2) {
          value = parts[2];
        } else {
          value = parts[1];
        }
        break;
      default:
        value = parts[Math.min(index, parts.length - 1)];
        break;
    }
    return parseIntPx(value);
  }
}
