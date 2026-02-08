package dev.jcputney.javamjml.util;

/**
 * Calculates padding and border box dimensions from CSS shorthand values.
 */
public record CssBoxModel(
    double paddingTop, double paddingRight, double paddingBottom, double paddingLeft,
    double borderLeftWidth, double borderRightWidth
) {

  /**
   * Total horizontal spacing (left + right padding + borders).
   */
  public double horizontalSpacing() {
    return paddingLeft + paddingRight + borderLeftWidth + borderRightWidth;
  }

  /**
   * Total vertical spacing (top + bottom padding).
   */
  public double verticalSpacing() {
    return paddingTop + paddingBottom;
  }

  /**
   * Creates a CssBoxModel from padding and border attribute strings.
   */
  public static CssBoxModel fromAttributes(String padding, String border,
      String borderLeft, String borderRight) {
    double[] pad = CssUnitParser.parseShorthand(padding);
    double blw = parseBorderWidth(borderLeft);
    double brw = parseBorderWidth(borderRight);

    // If individual borders weren't specified, try the shorthand border
    if (blw == 0 && brw == 0 && border != null && !border.isEmpty() && !"none".equals(border)) {
      double bw = parseBorderWidth(border);
      blw = bw;
      brw = bw;
    }

    return new CssBoxModel(pad[0], pad[1], pad[2], pad[3], blw, brw);
  }

  /**
   * Parses the width from a CSS border shorthand like "1px solid #000".
   */
  private static double parseBorderWidth(String border) {
    if (border == null || border.isEmpty() || "none".equals(border)) {
      return 0;
    }
    String[] parts = border.trim().split("\\s+");
    if (parts.length > 0) {
      return CssUnitParser.parsePx(parts[0], 0);
    }
    return 0;
  }
}
