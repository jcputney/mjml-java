package dev.jcputney.javamjml.util;

/**
 * Parses CSS unit values (px, %, em) and converts them to pixel values.
 */
public final class CssUnitParser {

  private CssUnitParser() {
  }

  /**
   * Parses a CSS value and converts it to pixels.
   * Supports px (direct), % (relative to containerWidth), and unitless (treated as px).
   */
  public static double toPixels(String value, double containerWidth) {
    if (value == null || value.isEmpty()) {
      return 0;
    }
    value = value.trim();

    if (value.endsWith("%")) {
      double pct = parseNumber(value.substring(0, value.length() - 1));
      return containerWidth * pct / 100.0;
    }
    if (value.endsWith("px")) {
      return parseNumber(value.substring(0, value.length() - 2));
    }
    // Try as unitless number
    return parseNumber(value);
  }

  /**
   * Parses a pixel value from a string like "10px" or "10".
   * Returns the provided default if parsing fails.
   */
  public static double parsePx(String value, double defaultValue) {
    if (value == null || value.isEmpty()) {
      return defaultValue;
    }
    value = value.trim();
    if (value.endsWith("px")) {
      value = value.substring(0, value.length() - 2);
    }
    try {
      return Double.parseDouble(value.trim());
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  /**
   * Parses a 1-4 value CSS shorthand into [top, right, bottom, left] pixel values.
   */
  public static double[] parseShorthand(String value) {
    if (value == null || value.isEmpty()) {
      return new double[]{0, 0, 0, 0};
    }
    String[] parts = value.trim().split("\\s+");
    double[] result = new double[4];

    switch (parts.length) {
      case 1 -> {
        double v = parsePx(parts[0], 0);
        result[0] = result[1] = result[2] = result[3] = v;
      }
      case 2 -> {
        double v = parsePx(parts[0], 0);
        double h = parsePx(parts[1], 0);
        result[0] = result[2] = v;
        result[1] = result[3] = h;
      }
      case 3 -> {
        result[0] = parsePx(parts[0], 0);
        result[1] = result[3] = parsePx(parts[1], 0);
        result[2] = parsePx(parts[2], 0);
      }
      default -> {
        result[0] = parsePx(parts[0], 0);
        result[1] = parsePx(parts[1], 0);
        result[2] = parsePx(parts[2], 0);
        result[3] = parsePx(parts[3], 0);
      }
    }
    return result;
  }

  /**
   * Formats a pixel value as an integer string with "px" suffix.
   */
  public static String formatPx(double value) {
    return ((int) value) + "px";
  }

  /**
   * Formats a pixel value as an integer string without any suffix.
   */
  public static String formatInt(double value) {
    return String.valueOf((int) value);
  }

  private static double parseNumber(String s) {
    try {
      return Double.parseDouble(s.trim());
    } catch (NumberFormatException e) {
      return 0;
    }
  }
}
