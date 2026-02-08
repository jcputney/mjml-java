package dev.jcputney.mjml.util;

import dev.jcputney.mjml.parser.MjmlNode;
import java.util.List;

/**
 * Calculates column pixel widths and responsive width specifications
 * from a list of column nodes and a container width.
 * Shared by MjSection and MjGroup.
 */
public final class ColumnWidthCalculator {

  private ColumnWidthCalculator() {
  }

  /**
   * Calculates pixel widths for each column. Columns with explicit widths get their
   * specified value; auto columns share remaining space equally using percentage-based
   * distribution to match MJML's floating-point behavior.
   *
   * @param columns        the column nodes
   * @param containerWidth the available width in pixels
   * @param usePercentAuto if true, auto columns use pct*containerWidth/100 (MJML section
   *                       behavior); if false, auto columns use (remaining)/count (MJML group behavior)
   */
  public static double[] calculatePixelWidths(List<MjmlNode> columns, double containerWidth,
      boolean usePercentAuto) {
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

    if (autoCount > 0) {
      double autoWidth;
      if (usePercentAuto) {
        double autoPct = 100.0 / columns.size();
        autoWidth = autoPct * containerWidth / 100.0;
      } else {
        autoWidth = (containerWidth - totalUsed) / autoCount;
      }
      for (int i = 0; i < widths.length; i++) {
        if (widths[i] == 0) {
          widths[i] = autoWidth;
        }
      }
    }

    return widths;
  }

  /**
   * Calculates column width specifications for responsive media queries.
   * Returns strings like "100" (percentage), "33.33" (percentage), or "150px" (pixel).
   */
  public static String[] calculateWidthSpecs(List<MjmlNode> columns) {
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
      autoPctStr = CssUnitParser.formatPxWidth(autoPct);
    }

    for (int i = 0; i < columns.size(); i++) {
      String widthAttr = columns.get(i).getAttribute("width");
      if (widthAttr != null && !widthAttr.isEmpty() && widthAttr.endsWith("%")) {
        specs[i] = widthAttr.replace("%", "").trim();
      } else if (widthAttr != null && !widthAttr.isEmpty()) {
        specs[i] = widthAttr.trim();
      } else {
        specs[i] = autoPctStr;
      }
    }
    return specs;
  }
}
