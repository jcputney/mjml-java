package dev.jcputney.mjml.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Builds CSS background-related style maps for sections and wrappers.
 * Eliminates duplication between MjSection and MjWrapper.
 */
public final class BackgroundCssHelper {

  private BackgroundCssHelper() {
  }

  /**
   * Builds the CSS background shorthand for a component with background image.
   * Format: "#color url('...') position / size repeat"
   */
  public static String buildBackgroundCss(String bgColor, String bgUrl,
      String bgPosition, String bgSize, String bgRepeat) {
    StringBuilder bg = new StringBuilder();
    if (bgColor != null && !bgColor.isEmpty()) {
      bg.append(bgColor).append(" ");
    }
    bg.append("url('").append(bgUrl).append("') ");
    bg.append(bgPosition).append(" / ").append(bgSize).append(" ").append(bgRepeat);
    return bg.toString();
  }

  /**
   * Builds style map for outer div of a section/wrapper with background image.
   */
  public static Map<String, String> buildBgImageDivStyleMap(String bgCss,
      String bgPosition, String bgRepeat, String bgSize, int containerWidth) {
    Map<String, String> styles = new LinkedHashMap<>();
    styles.put("background", bgCss);
    styles.put("background-position", bgPosition);
    styles.put("background-repeat", bgRepeat);
    styles.put("background-size", bgSize);
    styles.put("margin", "0px auto");
    styles.put("max-width", containerWidth + "px");
    return styles;
  }

  /**
   * Builds style map for inner table of a section/wrapper with background image.
   */
  public static Map<String, String> buildBgImageTableStyleMap(String bgCss,
      String bgPosition, String bgRepeat, String bgSize) {
    Map<String, String> styles = new LinkedHashMap<>();
    styles.put("background", bgCss);
    styles.put("background-position", bgPosition);
    styles.put("background-repeat", bgRepeat);
    styles.put("background-size", bgSize);
    styles.put("width", "100%");
    return styles;
  }
}
