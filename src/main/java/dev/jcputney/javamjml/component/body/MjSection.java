package dev.jcputney.javamjml.component.body;

import dev.jcputney.javamjml.component.BaseComponent;
import dev.jcputney.javamjml.component.BodyComponent;
import dev.jcputney.javamjml.component.ComponentRegistry;
import dev.jcputney.javamjml.context.GlobalContext;
import dev.jcputney.javamjml.context.RenderContext;
import dev.jcputney.javamjml.parser.MjmlNode;
import dev.jcputney.javamjml.util.CssBoxModel;
import dev.jcputney.javamjml.util.CssUnitParser;
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
public class MjSection extends BodyComponent {

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

  private final ComponentRegistry registry;

  public MjSection(MjmlNode node, GlobalContext globalContext, RenderContext renderContext,
      ComponentRegistry registry) {
    super(node, globalContext, renderContext);
    this.registry = registry;
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

  private boolean hasBackgroundUrl() {
    String url = getAttribute("background-url", "");
    return !url.isEmpty();
  }

  private String renderNormal() {
    if (hasBackgroundUrl()) {
      return renderNormalWithBgImage();
    }
    return renderNormalSimple();
  }

  private String renderNormalSimple() {
    StringBuilder sb = new StringBuilder();
    int containerWidth = globalContext.getContainerWidth();
    String bgColor = getAttribute("background-color");
    boolean hasBg = bgColor != null && !bgColor.isEmpty();

    // MSO wrapper table
    sb.append("    <!--[if mso | IE]><table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"")
        .append(getCssClass())
        .append("\" role=\"presentation\" style=\"width:").append(containerWidth)
        .append("px;\" width=\"").append(containerWidth).append("\" ");
    if (hasBg) {
      sb.append("bgcolor=\"").append(bgColor).append("\" ");
    }
    sb.append("><tr><td style=\"line-height:0px;font-size:0px;mso-line-height-rule:exactly;\"><![endif]-->\n");

    // Main section div (no padding here — padding goes on inner td)
    sb.append("    <div");
    sb.append(" style=\"").append(buildSectionStyle()).append("\"");
    String cssClass = getAttribute("css-class", "");
    if (!cssClass.isEmpty()) {
      sb.append(" class=\"").append(cssClass).append("\"");
    }
    sb.append(">\n");

    // Inner table
    sb.append("      <table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\"");
    sb.append(" style=\"").append(buildInnerTableStyle()).append("\"");
    sb.append(">\n");
    sb.append("        <tbody>\n");
    sb.append("          <tr>\n");

    // Inner td with padding
    sb.append("            <td style=\"").append(buildInnerTdStyle()).append("\">\n");

    // Render column children
    sb.append(renderColumnChildren());

    sb.append("            </td>\n");
    sb.append("          </tr>\n");
    sb.append("        </tbody>\n");
    sb.append("      </table>\n");
    sb.append("    </div>\n");

    // Close MSO wrapper
    sb.append("    <!--[if mso | IE]></td></tr></table><![endif]-->\n");

    return sb.toString();
  }

  /**
   * Renders a normal (non-full-width) section with a background image.
   * Structure: MSO table + VML rect -> div (with bg CSS) -> line-height div -> inner table -> columns
   */
  private String renderNormalWithBgImage() {
    StringBuilder sb = new StringBuilder();
    int containerWidth = globalContext.getContainerWidth();
    String bgColor = getAttribute("background-color");
    boolean hasBg = bgColor != null && !bgColor.isEmpty();
    String bgUrl = getAttribute("background-url", "");

    // MSO wrapper table with VML
    sb.append("    <!--[if mso | IE]><table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"")
        .append(getCssClass())
        .append("\" role=\"presentation\" style=\"width:").append(containerWidth)
        .append("px;\" width=\"").append(containerWidth).append("\" ");
    if (hasBg) {
      sb.append("bgcolor=\"").append(bgColor).append("\" ");
    }
    sb.append("><tr><td style=\"line-height:0px;font-size:0px;mso-line-height-rule:exactly;\">");

    // VML rect for background image
    sb.append(buildVmlRect(containerWidth + "px", bgUrl, bgColor));

    sb.append("<![endif]-->\n");

    // Outer div with background CSS
    sb.append("    <div style=\"").append(buildBgImageDivStyle()).append("\">\n");

    // Line-height wrapper div
    sb.append("      <div style=\"line-height:0;font-size:0;\">\n");

    // Inner table with background attribute and background CSS
    sb.append("        <table align=\"center\" background=\"").append(bgUrl)
        .append("\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\"");
    sb.append(" style=\"").append(buildBgImageTableStyle()).append("\"");
    sb.append(">\n");
    sb.append("          <tbody>\n");
    sb.append("            <tr>\n");

    // Inner td with padding
    sb.append("              <td style=\"").append(buildInnerTdStyle()).append("\">\n");

    // Render column children
    sb.append(renderColumnChildren());

    sb.append("              </td>\n");
    sb.append("            </tr>\n");
    sb.append("          </tbody>\n");
    sb.append("        </table>\n");
    sb.append("      </div>\n");
    sb.append("    </div>\n");

    // Close VML and MSO wrapper
    sb.append("    <!--[if mso | IE]></v:textbox></v:rect></td></tr></table><![endif]-->\n");

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
   * Full-width sections use a real HTML table (not MSO conditional) as the outer wrapper,
   * with the MSO table inside for column width control.
   */
  private String renderFullWidth() {
    if (hasBackgroundUrl()) {
      return renderFullWidthWithBgImage();
    }
    return renderFullWidthSimple();
  }

  /**
   * Full-width section with no background image.
   * Outer: real HTML table with width:100% and background.
   * Inner: MSO table for 600px constraint, then div/table/td/columns.
   */
  private String renderFullWidthSimple() {
    StringBuilder sb = new StringBuilder();
    int containerWidth = globalContext.getContainerWidth();
    String bgColor = getAttribute("background-color");
    boolean hasBg = bgColor != null && !bgColor.isEmpty();

    // Outer full-width table (real HTML, not MSO conditional)
    sb.append("    <table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"");
    if (hasBg) {
      sb.append("background:").append(bgColor).append(";background-color:").append(bgColor).append(";");
    }
    sb.append("width:100%;\">\n");
    sb.append("      <tbody>\n");
    sb.append("        <tr>\n");
    sb.append("          <td>\n");

    // MSO inner table for width constraint
    sb.append("            <!--[if mso | IE]><table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"")
        .append(getCssClass())
        .append("\" role=\"presentation\" style=\"width:").append(containerWidth)
        .append("px;\" width=\"").append(containerWidth).append("\" ");
    if (hasBg) {
      sb.append("bgcolor=\"").append(bgColor).append("\" ");
    }
    sb.append("><tr><td style=\"line-height:0px;font-size:0px;mso-line-height-rule:exactly;\"><![endif]-->\n");

    // Inner div with max-width
    sb.append("            <div style=\"margin:0px auto;max-width:").append(containerWidth).append("px;\">\n");

    // Inner table (NO background for full-width — background is on outer table)
    sb.append("              <table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"width:100%;\">\n");
    sb.append("                <tbody>\n");
    sb.append("                  <tr>\n");

    // Inner td with padding
    sb.append("                    <td style=\"").append(buildInnerTdStyle()).append("\">\n");

    sb.append(renderColumnChildren());

    sb.append("                    </td>\n");
    sb.append("                  </tr>\n");
    sb.append("                </tbody>\n");
    sb.append("              </table>\n");
    sb.append("            </div>\n");

    // Close MSO inner table
    sb.append("            <!--[if mso | IE]></td></tr></table><![endif]-->\n");

    sb.append("          </td>\n");
    sb.append("        </tr>\n");
    sb.append("      </tbody>\n");
    sb.append("    </table>\n");

    return sb.toString();
  }

  /**
   * Full-width section with a background image.
   * Outer: real HTML table with width:100%, background attribute, and background CSS.
   * MSO: VML rect with fill for background image support in Outlook.
   * Inner: line-height div -> table -> columns.
   */
  private String renderFullWidthWithBgImage() {
    StringBuilder sb = new StringBuilder();
    int containerWidth = globalContext.getContainerWidth();
    String bgColor = getAttribute("background-color");
    boolean hasBg = bgColor != null && !bgColor.isEmpty();
    String bgUrl = getAttribute("background-url", "");

    // Outer full-width table with background
    sb.append("    <table align=\"center\" background=\"").append(bgUrl)
        .append("\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"");
    sb.append(buildFullWidthBgTableStyle());
    sb.append("\">\n");
    sb.append("      <tbody>\n");
    sb.append("        <tr>\n");
    sb.append("          <td>\n");

    // MSO: VML rect with inner table
    sb.append("            <!--[if mso | IE]>");
    sb.append(buildVmlRect("mso-width-percent:1000;", bgUrl, bgColor));
    sb.append("<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"")
        .append(getCssClass())
        .append("\" role=\"presentation\" style=\"width:").append(containerWidth)
        .append("px;\" width=\"").append(containerWidth).append("\" ");
    if (hasBg) {
      sb.append("bgcolor=\"").append(bgColor).append("\" ");
    }
    sb.append("><tr><td style=\"line-height:0px;font-size:0px;mso-line-height-rule:exactly;\"><![endif]-->\n");

    // Inner div with max-width
    sb.append("            <div style=\"margin:0px auto;max-width:").append(containerWidth).append("px;\">\n");

    // Line-height wrapper
    sb.append("              <div style=\"line-height:0;font-size:0;\">\n");

    // Inner table (NO background for full-width)
    sb.append("                <table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"width:100%;\">\n");
    sb.append("                  <tbody>\n");
    sb.append("                    <tr>\n");

    // Inner td with padding
    sb.append("                      <td style=\"").append(buildInnerTdStyle()).append("\">\n");

    sb.append(renderColumnChildren());

    sb.append("                      </td>\n");
    sb.append("                    </tr>\n");
    sb.append("                  </tbody>\n");
    sb.append("                </table>\n");
    sb.append("              </div>\n");
    sb.append("            </div>\n");

    // Close MSO VML and table
    sb.append("            <!--[if mso | IE]></td></tr></table></v:textbox></v:rect><![endif]-->\n");

    sb.append("          </td>\n");
    sb.append("        </tr>\n");
    sb.append("      </tbody>\n");
    sb.append("    </table>\n");

    return sb.toString();
  }

  /**
   * Builds VML rect markup for background image support in MSO/Outlook.
   */
  private String buildVmlRect(String widthStyle, String bgUrl, String bgColor) {
    StringBuilder sb = new StringBuilder();
    String bgPosition = resolveBackgroundPosition();
    String bgSize = getAttribute("background-size", "auto");

    // Convert CSS position to VML origin/position
    String vmlOrigin = cssPositionToVmlOrigin(bgPosition);
    String vmlPosition = vmlOrigin; // Same values for origin and position

    sb.append("<v:rect style=\"");
    if (widthStyle.contains("mso-width-percent")) {
      sb.append(widthStyle);
    } else {
      sb.append("width:").append(widthStyle).append(";");
    }
    sb.append("\" xmlns:v=\"urn:schemas-microsoft-com:vml\" fill=\"true\" stroke=\"false\">");

    sb.append("<v:fill origin=\"").append(vmlOrigin)
        .append("\" position=\"").append(vmlPosition)
        .append("\" src=\"").append(bgUrl).append("\"");

    if (bgColor != null && !bgColor.isEmpty()) {
      sb.append(" color=\"").append(bgColor).append("\"");
    }

    sb.append(" type=\"tile\"");

    // Add size/aspect based on background-size
    if ("cover".equals(bgSize)) {
      sb.append(" size=\"1,1\" aspect=\"atleast\"");
    } else if ("contain".equals(bgSize)) {
      sb.append(" size=\"1,1\" aspect=\"atmost\"");
    } else if (!"auto".equals(bgSize)) {
      // Pixel or other dimensions: e.g. "200px 200px" -> "200px,200px"
      String vmlSize = bgSize.trim().replace(" ", ",");
      sb.append(" size=\"").append(vmlSize).append("\"");
    }

    sb.append(" />");

    sb.append("<v:textbox style=\"mso-fit-shape-to-text:true\" inset=\"0,0,0,0\">");

    return sb.toString();
  }

  /**
   * Resolves background position from individual x/y properties or the combined property.
   * Normalizes "top center" to "center top" format.
   */
  private String resolveBackgroundPosition() {
    String posX = getAttribute("background-position-x", "");
    String posY = getAttribute("background-position-y", "");
    if (!posX.isEmpty() && !posY.isEmpty()) {
      return posX + " " + posY;
    }
    String pos = getAttribute("background-position", "top center");
    // Normalize: MJML uses "x y" order in output
    return normalizeBackgroundPosition(pos);
  }

  /**
   * Normalizes background position to "x y" format.
   * MJML output uses: "center top", "center center", "right bottom", etc.
   */
  private static String normalizeBackgroundPosition(String pos) {
    if (pos == null || pos.isEmpty()) {
      return "center top";
    }
    String[] parts = pos.trim().split("\\s+");
    if (parts.length == 1) {
      // Single value: treat as x, default y to center
      return parts[0] + " " + "center";
    }
    // Check if first part is a y-value (top/bottom) and second is x-value
    String first = parts[0];
    String second = parts[1];
    if (isYValue(first) && isXValue(second)) {
      // Swap: "top center" -> "center top"
      return second + " " + first;
    }
    return first + " " + second;
  }

  private static boolean isYValue(String val) {
    return "top".equals(val) || "bottom".equals(val);
  }

  private static boolean isXValue(String val) {
    return "left".equals(val) || "right".equals(val) || "center".equals(val);
  }

  /**
   * Converts CSS background-position to VML origin/position values.
   * CSS "center top" -> VML "0.5, 0"
   * CSS "center center" -> VML "0.5, 0.5"
   * CSS "right bottom" -> VML "1, 1"
   */
  private static String cssPositionToVmlOrigin(String cssPosition) {
    String[] parts = cssPosition.trim().split("\\s+");
    String x = parts.length > 0 ? parts[0] : "center";
    String y = parts.length > 1 ? parts[1] : "top";

    String vx = cssAxisToVml(x);
    String vy = cssAxisToVml(y);

    return vx + ", " + vy;
  }

  private static String cssAxisToVml(String value) {
    switch (value) {
      case "left":
      case "top":
        return "0";
      case "center":
        return "0.5";
      case "right":
      case "bottom":
        return "1";
      default:
        // Percentage or pixel value
        if (value.endsWith("%")) {
          try {
            double pct = Double.parseDouble(value.replace("%", "")) / 100.0;
            if (pct == Math.floor(pct) && !Double.isInfinite(pct)) {
              return String.valueOf((int) pct);
            }
            return String.valueOf(pct);
          } catch (NumberFormatException e) {
            return "0.5";
          }
        }
        return "0.5";
    }
  }

  /**
   * Builds the CSS background shorthand for a section with background image.
   * Format: "#color url('...') position / size repeat"
   */
  private String buildBackgroundCss() {
    String bgColor = getAttribute("background-color", "");
    String bgUrl = getAttribute("background-url", "");
    String bgPosition = resolveBackgroundPosition();
    String bgSize = getAttribute("background-size", "auto");
    String bgRepeat = getAttribute("background-repeat", "repeat");

    StringBuilder bg = new StringBuilder();
    if (!bgColor.isEmpty()) {
      bg.append(bgColor).append(" ");
    }
    bg.append("url('").append(bgUrl).append("') ");
    bg.append(bgPosition).append(" / ").append(bgSize).append(" ").append(bgRepeat);

    return bg.toString();
  }

  /**
   * Builds style for outer div of a normal section with background image.
   */
  private String buildBgImageDivStyle() {
    Map<String, String> styles = new LinkedHashMap<>();
    String bgCss = buildBackgroundCss();
    styles.put("background", bgCss);
    String bgPosition = resolveBackgroundPosition();
    styles.put("background-position", bgPosition);
    styles.put("background-repeat", getAttribute("background-repeat", "repeat"));
    styles.put("background-size", getAttribute("background-size", "auto"));
    styles.put("margin", "0px auto");
    styles.put("max-width", globalContext.getContainerWidth() + "px");
    return buildStyle(styles);
  }

  /**
   * Builds style for inner table of a section with background image.
   * Includes background attribute and full background CSS.
   */
  private String buildBgImageTableStyle() {
    Map<String, String> styles = new LinkedHashMap<>();
    String bgCss = buildBackgroundCss();
    styles.put("background", bgCss);
    String bgPosition = resolveBackgroundPosition();
    styles.put("background-position", bgPosition);
    styles.put("background-repeat", getAttribute("background-repeat", "repeat"));
    styles.put("background-size", getAttribute("background-size", "auto"));
    styles.put("width", "100%");
    return buildStyle(styles);
  }

  /**
   * Builds style for the outer table of a full-width section with background image.
   */
  private String buildFullWidthBgTableStyle() {
    Map<String, String> styles = new LinkedHashMap<>();
    String bgColor = getAttribute("background-color", "");
    String bgUrl = getAttribute("background-url", "");
    String bgPosition = resolveBackgroundPosition();
    String bgSize = getAttribute("background-size", "auto");
    String bgRepeat = getAttribute("background-repeat", "repeat");

    StringBuilder bg = new StringBuilder();
    if (!bgColor.isEmpty()) {
      bg.append(bgColor).append(" ");
    }
    bg.append("url('").append(bgUrl).append("') ").append(bgPosition)
        .append(" / ").append(bgSize).append(" ").append(bgRepeat);
    styles.put("background", bg.toString());
    styles.put("background-position", bgPosition);
    styles.put("background-repeat", bgRepeat);
    styles.put("background-size", bgSize);
    styles.put("width", "100%");

    return buildStyle(styles);
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
    double[] widths = calculateColumnWidths(columns, contentWidth);
    String[] widthSpecs = calculateColumnWidthSpecs(columns);

    // MSO column table: one table wrapping ALL columns
    sb.append("              <!--[if mso | IE]><table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>");

    for (int i = 0; i < columns.size(); i++) {
      MjmlNode col = columns.get(i);
      boolean isGroup = "mj-group".equals(col.getTagName());

      // MSO column td — groups don't get vertical-align
      sb.append("<td class=\"").append(getCssClass()).append("\" style=\"");
      if (!isGroup) {
        sb.append("vertical-align:top;");
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

  private double[] calculateColumnWidths(List<MjmlNode> columns, double containerWidth) {
    double[] widths = new double[columns.size()];
    double totalUsed = 0;
    int autoCount = 0;

    for (int i = 0; i < columns.size(); i++) {
      String widthAttr = columns.get(i).getAttribute("width");
      if (widthAttr != null && !widthAttr.isEmpty()) {
        if (widthAttr.endsWith("%")) {
          widths[i] = containerWidth * CssUnitParser.parsePx(
              widthAttr.replace("%", ""), 0) / 100.0;
        } else {
          widths[i] = CssUnitParser.parsePx(widthAttr, 0);
        }
        totalUsed += widths[i];
      } else {
        autoCount++;
      }
    }

    // Distribute remaining width among auto columns
    // Use pct * containerWidth / 100 to match MJML's floating-point behavior
    if (autoCount > 0) {
      double autoPct = 100.0 / columns.size();
      double autoWidth = autoPct * containerWidth / 100.0;
      for (int i = 0; i < widths.length; i++) {
        if (widths[i] == 0) {
          widths[i] = autoWidth;
        }
      }
    }

    return widths;
  }

  /**
   * Calculates the column width specifications for media queries.
   * Returns strings like "100" (percentage), "33.33" (percentage), or "150px" (pixel).
   */
  private String[] calculateColumnWidthSpecs(List<MjmlNode> columns) {
    String[] specs = new String[columns.size()];
    int autoCount = 0;
    for (MjmlNode col : columns) {
      String widthAttr = col.getAttribute("width");
      if (widthAttr == null || widthAttr.isEmpty()) {
        autoCount++;
      }
    }

    double autoPct = autoCount > 0 ? 100.0 / columns.size() : 0;
    String autoPctStr = null;
    if (autoCount > 0) {
      if (autoPct == Math.floor(autoPct) && !Double.isInfinite(autoPct)) {
        autoPctStr = String.valueOf((int) autoPct);
      } else {
        autoPctStr = String.valueOf(autoPct);
      }
    }

    for (int i = 0; i < columns.size(); i++) {
      String widthAttr = columns.get(i).getAttribute("width");
      if (widthAttr != null && !widthAttr.isEmpty() && widthAttr.endsWith("%")) {
        // Percentage width: use literal value
        specs[i] = widthAttr.replace("%", "").trim();
      } else if (widthAttr != null && !widthAttr.isEmpty()) {
        // Pixel width: keep as pixel spec (e.g. "150px")
        specs[i] = widthAttr.trim();
      } else {
        // Auto: use computed percentage
        specs[i] = autoPctStr;
      }
    }
    return specs;
  }

  @Override
  public CssBoxModel getBoxModel() {
    CssBoxModel base = super.getBoxModel();
    // Individual padding properties override shorthand values
    String pt = getAttribute("padding-top", "");
    String pr = getAttribute("padding-right", "");
    String pb = getAttribute("padding-bottom", "");
    String pl = getAttribute("padding-left", "");
    double padTop = !pt.isEmpty() ? CssUnitParser.parsePx(pt, 0) : base.paddingTop();
    double padRight = !pr.isEmpty() ? CssUnitParser.parsePx(pr, 0) : base.paddingRight();
    double padBottom = !pb.isEmpty() ? CssUnitParser.parsePx(pb, 0) : base.paddingBottom();
    double padLeft = !pl.isEmpty() ? CssUnitParser.parsePx(pl, 0) : base.paddingLeft();
    return new CssBoxModel(padTop, padRight, padBottom, padLeft,
        base.borderLeftWidth(), base.borderRightWidth());
  }

  @Override
  public double getContentWidth() {
    double containerWidth = renderContext.getContainerWidth();
    CssBoxModel box = getBoxModel();
    return containerWidth - box.paddingLeft() - box.paddingRight()
        - box.borderLeftWidth() - box.borderRightWidth();
  }

  private String buildSectionStyle() {
    Map<String, String> styles = new LinkedHashMap<>();
    String bgColor = getAttribute("background-color");
    if (bgColor != null && !bgColor.isEmpty()) {
      styles.put("background", bgColor);
      styles.put("background-color", bgColor);
    }
    styles.put("margin", "0px auto");
    styles.put("max-width", globalContext.getContainerWidth() + "px");
    String borderRadius = getAttribute("border-radius", "");
    if (!borderRadius.isEmpty()) {
      styles.put("border-radius", borderRadius);
      styles.put("overflow", "hidden");
    }
    return buildStyle(styles);
  }

  private String buildInnerTableStyle() {
    Map<String, String> styles = new LinkedHashMap<>();
    String bgColor = getAttribute("background-color");
    if (bgColor != null && !bgColor.isEmpty()) {
      styles.put("background", bgColor);
      styles.put("background-color", bgColor);
    }
    styles.put("width", "100%");
    String borderRadius = getAttribute("border-radius", "");
    if (!borderRadius.isEmpty()) {
      styles.put("border-collapse", "separate");
    }
    return buildStyle(styles);
  }

  private String buildInnerTdStyle() {
    Map<String, String> styles = new LinkedHashMap<>();
    // Section border goes on the inner td
    String border = getAttribute("border", "");
    if (!border.isEmpty() && !"none".equals(border)) {
      styles.put("border", border);
    }
    String borderBottom = getAttribute("border-bottom", "");
    if (!borderBottom.isEmpty()) {
      styles.put("border-bottom", borderBottom);
    }
    String borderLeft = getAttribute("border-left", "");
    if (!borderLeft.isEmpty()) {
      styles.put("border-left", borderLeft);
    }
    String borderRight = getAttribute("border-right", "");
    if (!borderRight.isEmpty()) {
      styles.put("border-right", borderRight);
    }
    String borderTop = getAttribute("border-top", "");
    if (!borderTop.isEmpty()) {
      styles.put("border-top", borderTop);
    }
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
    if (width == Math.floor(width) && !Double.isInfinite(width)) {
      return String.valueOf((int) width);
    }
    return String.valueOf(width);
  }

  private String getCssClass() {
    String cssClass = getAttribute("css-class", "");
    // MJML outputs empty class="" for MSO elements
    return cssClass;
  }
}
