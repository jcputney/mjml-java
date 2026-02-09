package dev.jcputney.mjml.util;

/**
 * Normalizes CSS background-position values to "x y" format.
 * Shared by MjSection and MjWrapper.
 */
public final class BackgroundPositionHelper {

  private BackgroundPositionHelper() {
  }

  /**
   * Normalizes background position to "x y" format.
   * Swaps y-x order: "top center" becomes "center top".
   * Single values get "center" appended: "50%" becomes "50% center".
   * Null/empty returns "center top".
   */
  public static String normalize(String pos) {
    if (pos == null || pos.isEmpty()) {
      return "center top";
    }
    String[] parts = pos.trim().split("\\s+");
    if (parts.length == 1) {
      return parts[0] + " " + "center";
    }
    String first = parts[0];
    String second = parts[1];
    if (isYValue(first) && isXValue(second)) {
      return second + " " + first;
    }
    return first + " " + second;
  }

  public static boolean isYValue(String val) {
    return "top".equals(val) || "bottom".equals(val);
  }

  public static boolean isXValue(String val) {
    return "left".equals(val) || "right".equals(val) || "center".equals(val);
  }
}
