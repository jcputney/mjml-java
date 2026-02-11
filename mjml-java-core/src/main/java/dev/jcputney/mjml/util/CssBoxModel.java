package dev.jcputney.mjml.util;

/**
 * Calculates padding and border box dimensions from CSS shorthand values.
 *
 * @param paddingTop the top padding in pixels
 * @param paddingRight the right padding in pixels
 * @param paddingBottom the bottom padding in pixels
 * @param paddingLeft the left padding in pixels
 * @param borderLeftWidth the left border width in pixels
 * @param borderRightWidth the right border width in pixels
 */
public record CssBoxModel(
    double paddingTop,
    double paddingRight,
    double paddingBottom,
    double paddingLeft,
    double borderLeftWidth,
    double borderRightWidth) {

  /**
   * Creates a CssBoxModel from padding and border attribute strings.
   *
   * @param padding the CSS padding shorthand string (e.g., "10px 20px")
   * @param border the CSS border shorthand string (e.g., "1px solid #000")
   * @param borderLeft the CSS left border shorthand string
   * @param borderRight the CSS right border shorthand string
   * @return a new CssBoxModel with parsed padding and border values
   */
  public static CssBoxModel fromAttributes(
      String padding, String border, String borderLeft, String borderRight) {
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

  /** Parses the width from a CSS border shorthand like "1px solid #000". */
  private static double parseBorderWidth(String border) {
    if (border == null || border.isEmpty() || "none".equals(border)) {
      return 0;
    }
    String[] parts = CssUnitParser.WHITESPACE.split(border.trim());
    if (parts.length > 0) {
      return CssUnitParser.parsePx(parts[0], 0);
    }
    return 0;
  }

  /**
   * Total horizontal spacing (left + right padding + borders).
   *
   * @return the sum of left padding, right padding, left border width, and right border width
   */
  public double horizontalSpacing() {
    return paddingLeft + paddingRight + borderLeftWidth + borderRightWidth;
  }

  /**
   * Total vertical spacing (top + bottom padding).
   *
   * @return the sum of top and bottom padding
   */
  public double verticalSpacing() {
    return paddingTop + paddingBottom;
  }
}
