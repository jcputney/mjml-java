package dev.jcputney.mjml.component.body;

import dev.jcputney.mjml.component.BaseComponent;
import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.component.ComponentRegistry;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import dev.jcputney.mjml.render.VmlHelper;
import dev.jcputney.mjml.util.ColumnWidthCalculator;
import dev.jcputney.mjml.util.CssBoxModel;
import dev.jcputney.mjml.util.CssUnitParser;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The section component (&lt;mj-section&gt;).
 * Renders a table-based row containing columns with responsive behavior.
 * Handles full-width sections, background colors/images, padding,
 * and MSO conditional column layout.
 */
public class MjSection extends AbstractSectionComponent {

  private static final Map<String, String> DEFAULTS = Map.ofEntries(
      Map.entry("background-color", ""),
      Map.entry("background-position", "top center"),
      Map.entry("background-position-x", ""),
      Map.entry("background-position-y", ""),
      Map.entry("background-repeat", "repeat"),
      Map.entry("background-size", "auto"),
      Map.entry("background-url", ""),
      Map.entry("border", "none"),
      Map.entry("border-bottom", ""),
      Map.entry("border-left", ""),
      Map.entry("border-radius", ""),
      Map.entry("border-right", ""),
      Map.entry("border-top", ""),
      Map.entry("direction", "ltr"),
      Map.entry("full-width", ""),
      Map.entry("padding", "20px 0"),
      Map.entry("text-align", "center"),
      Map.entry("text-padding", "4px 4px 4px 0")
  );

  public MjSection(MjmlNode node, GlobalContext globalContext, RenderContext renderContext,
      ComponentRegistry registry) {
    super(node, globalContext, renderContext, registry);
  }

  @Override
  public String getTagName() {
    return "mj-section";
  }

  @Override
  public Map<String, String> getDefaultAttributes() {
    return DEFAULTS;
  }

  @Override
  public String render() {
    if (renderContext.isInsideWrapper()) {
      return renderInsideWrapper();
    }
    boolean isFullWidth = "full-width".equals(getAttribute("full-width"));
    if (isFullWidth) {
      return renderFullWidth();
    }
    return renderNormal();
  }

  private String renderNormal() {
    StringBuilder sb = new StringBuilder();
    int containerWidth = globalContext.getContainerWidth();
    String bgColor = getAttribute("background-color");
    boolean hasBg = bgColor != null && !bgColor.isEmpty();
    boolean hasBgUrl = hasBackgroundUrl();
    String bgUrl = getAttribute("background-url", "");

    // MSO wrapper table
    sb.append(buildMsoTableOpen(containerWidth, bgColor, hasBg));
    if (hasBgUrl) {
      sb.append(buildVmlRect(containerWidth + "px", bgUrl, bgColor));
      sb.append("<![endif]-->\n");
    } else {
      sb.append("<![endif]-->\n");
    }

    if (hasBgUrl) {
      // Outer div with background CSS
      sb.append("    <div style=\"").append(buildBgImageDivStyle()).append("\">\n");
      // Line-height wrapper div
      sb.append("      <div style=\"line-height:0;font-size:0;\">\n");
      // Inner table with background attribute and background CSS
      sb.append("        <table align=\"center\" background=\"").append(escapeAttr(bgUrl))
          .append("\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\"");
      sb.append(" style=\"").append(buildBgImageTableStyle()).append("\"");
      sb.append(">\n");
    } else {
      // Main section div
      sb.append("    <div");
      sb.append(" style=\"").append(buildOuterDivStyle()).append("\"");
      String cssClass = getAttribute("css-class", "");
      if (!cssClass.isEmpty()) {
        sb.append(" class=\"").append(escapeAttr(cssClass)).append("\"");
      }
      sb.append(">\n");
      // Inner table
      sb.append("      <table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\"");
      sb.append(" style=\"").append(buildInnerTableStyle()).append("\"");
      sb.append(">\n");
    }

    // Inner content (shared across both variants)
    String indent = hasBgUrl ? "          " : "        ";
    sb.append(indent).append("<tbody>\n");
    sb.append(indent).append("  <tr>\n");
    sb.append(indent).append("    <td style=\"").append(buildInnerTdStyle()).append("\">\n");
    sb.append(renderColumnChildren());
    sb.append(indent).append("    </td>\n");
    sb.append(indent).append("  </tr>\n");
    sb.append(indent).append("</tbody>\n");

    if (hasBgUrl) {
      sb.append("        </table>\n");
      sb.append("      </div>\n");
      sb.append("    </div>\n");
      sb.append("    <!--[if mso | IE]></v:textbox></v:rect></td></tr></table><![endif]-->\n");
    } else {
      sb.append("      </table>\n");
      sb.append("    </div>\n");
      sb.append("    <!--[if mso | IE]></td></tr></table><![endif]-->\n");
    }

    return sb.toString();
  }

  /**
   * Builds the MSO conditional table open tag shared by normal and full-width variants.
   */
  private String buildMsoTableOpen(int containerWidth, String bgColor, boolean hasBg) {
    StringBuilder sb = new StringBuilder();
    sb.append("    <!--[if mso | IE]><table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"")
        .append(escapeAttr(getCssClass()))
        .append("\" role=\"presentation\" style=\"width:").append(containerWidth)
        .append("px;\" width=\"").append(containerWidth).append("\" ");
    if (hasBg) {
      sb.append("bgcolor=\"").append(escapeAttr(bgColor)).append("\" ");
    }
    sb.append("><tr><td style=\"line-height:0px;font-size:0px;mso-line-height-rule:exactly;\">");
    return sb.toString();
  }

  /**
   * Renders this section when inside a wrapper component.
   * No outer MSO table or background div — the wrapper provides those.
   * Just renders: div (max-width) -> inner table -> inner td (padding) -> columns.
   */
  private String renderInsideWrapper() {
    StringBuilder sb = new StringBuilder();
    int wrapperInnerWidth = (int) renderContext.getContainerWidth();

    // Simple div with max-width (no background — wrapper provides it)
    sb.append("              <div style=\"margin:0px auto;max-width:").append(wrapperInnerWidth).append("px;\">\n");

    // Inner table (no background)
    sb.append("                <table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"width:100%;\">\n");
    sb.append("                  <tbody>\n");
    sb.append("                    <tr>\n");

    // Inner td with this section's own padding
    sb.append("                      <td style=\"").append(buildInnerTdStyle()).append("\">\n");

    // Render column children
    sb.append(renderColumnChildren());

    sb.append("                      </td>\n");
    sb.append("                    </tr>\n");
    sb.append("                  </tbody>\n");
    sb.append("                </table>\n");
    sb.append("              </div>\n");

    return sb.toString();
  }

  /**
   * Renders a full-width section.
   * Outer: real HTML table with width:100%.
   * With bg image: adds VML rect, line-height wrapper, and background CSS.
   */
  private String renderFullWidth() {
    StringBuilder sb = new StringBuilder();
    int containerWidth = globalContext.getContainerWidth();
    String bgColor = getAttribute("background-color");
    boolean hasBg = bgColor != null && !bgColor.isEmpty();
    boolean hasBgUrl = hasBackgroundUrl();
    String bgUrl = getAttribute("background-url", "");

    // Outer full-width table (real HTML, not MSO conditional)
    sb.append("    <table align=\"center\" ");
    if (hasBgUrl) {
      sb.append("background=\"").append(escapeAttr(bgUrl)).append("\" ");
    }
    sb.append("border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"");
    if (hasBgUrl) {
      sb.append(buildBgImageTableStyle());
    } else {
      if (hasBg) {
        sb.append("background:").append(bgColor).append(";background-color:").append(bgColor).append(";");
      }
      sb.append("width:100%;");
    }
    sb.append("\">\n");
    sb.append("      <tbody>\n");
    sb.append("        <tr>\n");
    sb.append("          <td>\n");

    // MSO: VML rect (if bg image) + inner table
    sb.append("            <!--[if mso | IE]>");
    if (hasBgUrl) {
      sb.append(buildVmlRect("mso-width-percent:1000;", bgUrl, bgColor));
    }
    sb.append("<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"")
        .append(escapeAttr(getCssClass()))
        .append("\" role=\"presentation\" style=\"width:").append(containerWidth)
        .append("px;\" width=\"").append(containerWidth).append("\" ");
    if (hasBg) {
      sb.append("bgcolor=\"").append(escapeAttr(bgColor)).append("\" ");
    }
    sb.append("><tr><td style=\"line-height:0px;font-size:0px;mso-line-height-rule:exactly;\"><![endif]-->\n");

    // Inner div with max-width
    sb.append("            <div style=\"margin:0px auto;max-width:").append(containerWidth).append("px;\">\n");

    if (hasBgUrl) {
      sb.append("              <div style=\"line-height:0;font-size:0;\">\n");
    }

    // Inner table
    String innerIndent = hasBgUrl ? "                " : "              ";
    sb.append(innerIndent).append("<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"width:100%;\">\n");
    sb.append(innerIndent).append("  <tbody>\n");
    sb.append(innerIndent).append("    <tr>\n");
    sb.append(innerIndent).append("      <td style=\"").append(buildInnerTdStyle()).append("\">\n");

    sb.append(renderColumnChildren());

    sb.append(innerIndent).append("      </td>\n");
    sb.append(innerIndent).append("    </tr>\n");
    sb.append(innerIndent).append("  </tbody>\n");
    sb.append(innerIndent).append("</table>\n");

    if (hasBgUrl) {
      sb.append("              </div>\n");
    }
    sb.append("            </div>\n");

    // Close MSO
    if (hasBgUrl) {
      sb.append("            <!--[if mso | IE]></td></tr></table></v:textbox></v:rect><![endif]-->\n");
    } else {
      sb.append("            <!--[if mso | IE]></td></tr></table><![endif]-->\n");
    }

    sb.append("          </td>\n");
    sb.append("        </tr>\n");
    sb.append("      </tbody>\n");
    sb.append("    </table>\n");

    return sb.toString();
  }

  private String buildVmlRect(String widthStyle, String bgUrl, String bgColor) {
    return VmlHelper.buildSectionVmlRect(widthStyle, bgUrl, bgColor,
        resolveBackgroundPosition(),
        getAttribute("background-size", "auto"),
        getAttribute("background-repeat", "repeat"));
  }

  private String renderColumnChildren() {
    StringBuilder sb = new StringBuilder();
    List<MjmlNode> columns = getColumnChildren();

    if (columns.isEmpty()) {
      // Even with no columns, MJML emits an empty MSO table
      sb.append("              <!--[if mso | IE]><table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr></tr></table><![endif]-->\n");
      return sb.toString();
    }

    // Calculate column widths (pixel and percentage)
    double contentWidth = getContentWidth();
    double[] widths = ColumnWidthCalculator.calculatePixelWidths(columns, contentWidth, true);
    String[] widthSpecs = ColumnWidthCalculator.calculateWidthSpecs(columns);

    // MSO column table: one table wrapping ALL columns
    sb.append("              <!--[if mso | IE]><table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>");

    for (int i = 0; i < columns.size(); i++) {
      MjmlNode col = columns.get(i);
      boolean isGroup = "mj-group".equals(col.getTagName());

      // MSO column td — groups don't get vertical-align
      sb.append("<td class=\"").append(escapeAttr(getCssClass())).append("\" style=\"");
      if (!isGroup) {
        String verticalAlign = col.getAttribute("vertical-align", "top");
        sb.append("vertical-align:").append(verticalAlign).append(";");
      }
      sb.append("width:").append(formatPxWidth(widths[i]))
          .append("px;\" >");

      sb.append("<![endif]-->\n");

      RenderContext colContext = renderContext
          .withColumnWidth(widths[i], widthSpecs[i])
          .withPosition(i, i == 0, i == columns.size() - 1);

      BaseComponent component = registry.createComponent(col, globalContext, colContext);
      if (component instanceof BodyComponent bodyComponent) {
        sb.append(bodyComponent.render());
      }

      sb.append("              <!--[if mso | IE]></td>");

      // If this is the last column, close the MSO table
      if (i == columns.size() - 1) {
        sb.append("</tr></table><![endif]-->\n");
      }
    }

    return sb.toString();
  }

  private List<MjmlNode> getColumnChildren() {
    List<MjmlNode> columns = new ArrayList<>();
    for (MjmlNode child : node.getChildren()) {
      String tag = child.getTagName();
      if ("mj-column".equals(tag) || "mj-group".equals(tag)) {
        columns.add(child);
      }
    }
    return columns;
  }

  @Override
  public double getContentWidth() {
    double containerWidth = renderContext.getContainerWidth();
    CssBoxModel box = getBoxModel();
    return containerWidth - box.paddingLeft() - box.paddingRight()
        - box.borderLeftWidth() - box.borderRightWidth();
  }

  private String buildInnerTdStyle() {
    Map<String, String> styles = new LinkedHashMap<>();
    addBorderStyles(styles, "border", "border-bottom", "border-left", "border-right", "border-top");
    String borderRadius = getAttribute("border-radius", "");
    if (!borderRadius.isEmpty()) {
      styles.put("border-radius", borderRadius);
    }
    styles.put("direction", getAttribute("direction", "ltr"));
    styles.put("font-size", "0px");
    String padding = getAttribute("padding");
    if (padding != null && !padding.isEmpty()) {
      styles.put("padding", padding);
    }
    // Individual padding overrides (alphabetical order after shorthand)
    String pb = getAttribute("padding-bottom", "");
    if (!pb.isEmpty()) {
      styles.put("padding-bottom", pb);
    }
    String pl = getAttribute("padding-left", "");
    if (!pl.isEmpty()) {
      styles.put("padding-left", pl);
    }
    String pr = getAttribute("padding-right", "");
    if (!pr.isEmpty()) {
      styles.put("padding-right", pr);
    }
    String pt = getAttribute("padding-top", "");
    if (!pt.isEmpty()) {
      styles.put("padding-top", pt);
    }
    styles.put("text-align", getAttribute("text-align", "center"));
    return buildStyle(styles);
  }

  private static String formatPxWidth(double width) {
    return CssUnitParser.formatPxWidth(width);
  }
}
