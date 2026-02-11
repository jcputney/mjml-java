package dev.jcputney.mjml.util;

import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Parses CSS unit values (px, %, em) and converts them to pixel values.
 */
public final class CssUnitParser {

  private static final Logger LOG = Logger.getLogger(CssUnitParser.class.getName());

  /** Pre-compiled whitespace pattern for splitting CSS shorthand values. */
  public static final Pattern WHITESPACE = Pattern.compile("\\s+");

  private static final Pattern NON_NUMERIC = Pattern.compile("[^0-9-]");

  /**
   * Pattern matching valid CSS numeric values: optional sign, digits with optional decimal,
   * optional unit suffix (px, %, em, rem). Rejects garbage like "abc123xyz" or "12px34".
   */
  private static final Pattern VALID_CSS_NUMBER = Pattern.compile(
      "^\\s*-?\\d+(?:\\.\\d+)?(?:px|%|em|rem)?\\s*$");

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
    String parsed = value.trim();
    try {
      return Double.parseDouble(parsed);
    } catch (NumberFormatException e) {
      LOG.fine(() -> "Failed to parse pixel value: " + parsed + ", using default: " + defaultValue);
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
    String[] parts = WHITESPACE.split(value.trim());
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
   * Parses an integer from a CSS pixel value like "10px", "10", or "10.5px".
   * Validates the input against a CSS number pattern first, rejecting garbage
   * like "abc123xyz" or "12px34". Returns 0 on failure.
   */
  public static int parseIntPx(String value) {
    if (value == null || value.isEmpty()) {
      return 0;
    }
    if (!VALID_CSS_NUMBER.matcher(value).matches()) {
      LOG.fine(() -> "Invalid CSS value for parseIntPx: " + value);
      return 0;
    }
    String stripped = value.trim().replaceAll("(?:px|%|em|rem)$", "").trim();
    try {
      return (int) Double.parseDouble(stripped);
    } catch (NumberFormatException e) {
      return 0;
    }
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

  /**
   * Formats a pixel width: integers without decimals, decimals as-is.
   */
  public static String formatPxWidth(double width) {
    if (width == Math.floor(width) && !Double.isInfinite(width)) {
      return String.valueOf((int) width);
    }
    return String.valueOf(width);
  }

  /**
   * Parses an integer pixel value from a string like "600px" or "600".
   * Returns the default value if parsing fails.
   */
  public static int parsePixels(String value, int defaultValue) {
    if (value == null || value.isEmpty()) {
      return defaultValue;
    }
    try {
      return Integer.parseInt(value.replace("px", "").trim());
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  private static double parseNumber(String s) {
    try {
      return Double.parseDouble(s.trim());
    } catch (NumberFormatException e) {
      return 0;
    }
  }
}
