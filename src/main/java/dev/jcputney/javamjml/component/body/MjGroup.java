package dev.jcputney.javamjml.component.body;

import dev.jcputney.javamjml.component.BaseComponent;
import dev.jcputney.javamjml.component.BodyComponent;
import dev.jcputney.javamjml.component.ComponentRegistry;
import dev.jcputney.javamjml.context.GlobalContext;
import dev.jcputney.javamjml.context.RenderContext;
import dev.jcputney.javamjml.parser.MjmlNode;
import dev.jcputney.javamjml.util.CssUnitParser;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The group component (&lt;mj-group&gt;).
 * Groups columns together so they don't stack on mobile.
 * Renders as a single MSO table containing multiple columns.
 */
public class MjGroup extends BodyComponent {

  private static final Map<String, String> DEFAULTS = Map.of(
      "background-color", "",
      "direction", "ltr",
      "vertical-align", "top",
      "width", ""
  );

  private final ComponentRegistry registry;

  public MjGroup(MjmlNode node, GlobalContext globalContext, RenderContext renderContext,
      ComponentRegistry registry) {
    super(node, globalContext, renderContext);
    this.registry = registry;
  }

  @Override
  public String getTagName() {
    return "mj-group";
  }

  @Override
  public Map<String, String> getDefaultAttributes() {
    return DEFAULTS;
  }

  @Override
  public String render() {
    StringBuilder sb = new StringBuilder();
    double groupWidth = renderContext.getContainerWidth();
    String direction = getAttribute("direction", "ltr");

    // Build responsive class from the column width spec set by the parent section
    String widthSpec = renderContext.getColumnWidthSpec();
    if (widthSpec == null) {
      widthSpec = "100";
    }
    boolean isPixelWidth = widthSpec.endsWith("px");
    String responsiveClass;
    if (isPixelWidth) {
      String pxValue = widthSpec.replace("px", "");
      responsiveClass = "mj-column-px-" + pxValue;
    } else {
      responsiveClass = "mj-column-per-" + widthSpec.replace(".", "-");
    }

    // Outer div with responsive class
    sb.append("              <div class=\"").append(responsiveClass).append(" mj-outlook-group-fix\"");
    sb.append(" style=\"").append(buildOuterStyle(direction)).append("\"");
    sb.append(">\n");

    // MSO table open
    sb.append("                <!--[if mso | IE]><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" ><tr>");

    // Render column children
    List<MjmlNode> columns = getColumnChildren();
    double[] widths = calculateColumnWidths(columns, groupWidth);
    String[] widthSpecs = calculateColumnWidthSpecs(columns, groupWidth);

    for (int i = 0; i < columns.size(); i++) {
      MjmlNode col = columns.get(i);

      // MSO column td
      sb.append("<td style=\"vertical-align:top;width:")
          .append(formatPxWidth(widths[i]))
          .append("px;\" >");

      sb.append("<![endif]-->\n");

      // Column context with proper width spec for responsive class
      RenderContext colContext = renderContext
          .withColumnWidth(widths[i], widthSpecs[i])
          .withPosition(i, i == 0, i == columns.size() - 1)
          .withInsideGroup(true);

      BaseComponent component = registry.createComponent(col, globalContext, colContext);
      if (component instanceof BodyComponent bodyComponent) {
        sb.append(bodyComponent.render());
      }

      sb.append("                <!--[if mso | IE]></td>");

      if (i == columns.size() - 1) {
        sb.append("</tr></table><![endif]-->\n");
      }
    }

    sb.append("              </div>\n");

    // Add responsive media query for this group
    if (isPixelWidth) {
      globalContext.addMediaQuery(responsiveClass, widthSpec, "");
    } else {
      globalContext.addMediaQuery(responsiveClass, widthSpec, "%");
    }

    return sb.toString();
  }

  private String buildOuterStyle(String direction) {
    Map<String, String> styles = new LinkedHashMap<>();
    styles.put("font-size", "0");
    styles.put("line-height", "0");
    styles.put("text-align", "left");
    styles.put("display", "inline-block");
    styles.put("width", "100%");
    styles.put("direction", direction);
    String bgColor = getAttribute("background-color");
    if (bgColor != null && !bgColor.isEmpty()) {
      styles.put("background-color", bgColor);
    }
    return buildStyle(styles);
  }

  private List<MjmlNode> getColumnChildren() {
    List<MjmlNode> columns = new ArrayList<>();
    for (MjmlNode child : node.getChildren()) {
      String tag = child.getTagName();
      if ("mj-column".equals(tag)) {
        columns.add(child);
      }
    }
    return columns;
  }

  private double[] calculateColumnWidths(List<MjmlNode> columns, double groupWidth) {
    double[] widths = new double[columns.size()];
    double totalUsed = 0;
    int autoCount = 0;

    for (int i = 0; i < columns.size(); i++) {
      String widthAttr = columns.get(i).getAttribute("width");
      if (widthAttr != null && !widthAttr.isEmpty()) {
        if (widthAttr.endsWith("%")) {
          widths[i] = groupWidth * CssUnitParser.parsePx(
              widthAttr.replace("%", ""), 0) / 100.0;
        } else {
          widths[i] = CssUnitParser.parsePx(widthAttr, 0);
        }
        totalUsed += widths[i];
      } else {
        autoCount++;
      }
    }

    if (autoCount > 0) {
      double autoWidth = (groupWidth - totalUsed) / autoCount;
      for (int i = 0; i < widths.length; i++) {
        if (widths[i] == 0) {
          widths[i] = autoWidth;
        }
      }
    }

    return widths;
  }

  /**
   * Calculates column width specifications for responsive classes.
   * Each column's percentage is relative to the GROUP width (not section width).
   */
  private String[] calculateColumnWidthSpecs(List<MjmlNode> columns, double groupWidth) {
    String[] specs = new String[columns.size()];
    int autoCount = 0;
    for (MjmlNode col : columns) {
      String widthAttr = col.getAttribute("width");
      if (widthAttr == null || widthAttr.isEmpty()) {
        autoCount++;
      }
    }

    double autoPct = autoCount > 0 ? 100.0 / columns.size() : 0;

    for (int i = 0; i < columns.size(); i++) {
      String widthAttr = columns.get(i).getAttribute("width");
      if (widthAttr != null && !widthAttr.isEmpty() && widthAttr.endsWith("%")) {
        specs[i] = widthAttr.replace("%", "").trim();
      } else if (widthAttr != null && !widthAttr.isEmpty()) {
        specs[i] = widthAttr.trim();
      } else {
        if (autoPct == Math.floor(autoPct) && !Double.isInfinite(autoPct)) {
          specs[i] = String.valueOf((int) autoPct);
        } else {
          specs[i] = String.valueOf(autoPct);
        }
      }
    }
    return specs;
  }

  private static String formatPxWidth(double width) {
    return String.valueOf((int) width);
  }
}
