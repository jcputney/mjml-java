package dev.jcputney.mjml.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Builds CSS background-related style maps for sections and wrappers. Eliminates duplication
 * between MjSection and MjWrapper.
 */
public final class BackgroundCssHelper {

  private BackgroundCssHelper() {}

  /**
   * Builds the CSS background shorthand for a component with background image. Format: "#color
   * url('...') position / size repeat"
   *
   * @param bgColor the background color value, or {@code null}/empty if none
   * @param bgUrl the background image URL
   * @param bgPosition the background position value (e.g. "center top")
   * @param bgSize the background size value (e.g. "auto", "cover")
   * @param bgRepeat the background repeat value (e.g. "repeat", "no-repeat")
   * @return the CSS background shorthand string
   */
  public static String buildBackgroundCss(
      String bgColor, String bgUrl, String bgPosition, String bgSize, String bgRepeat) {
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
   *
   * @param bgCss the CSS background shorthand value
   * @param bgPosition the background position value
   * @param bgRepeat the background repeat value
   * @param bgSize the background size value
   * @param containerWidth the container width in pixels
   * @return a map of CSS property names to values for the background image div
   */
  public static Map<String, String> buildBgImageDivStyleMap(
      String bgCss, String bgPosition, String bgRepeat, String bgSize, int containerWidth) {
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
   *
   * @param bgCss the CSS background shorthand value
   * @param bgPosition the background position value
   * @param bgRepeat the background repeat value
   * @param bgSize the background size value
   * @return a map of CSS property names to values for the background image table
   */
  public static Map<String, String> buildBgImageTableStyleMap(
      String bgCss, String bgPosition, String bgRepeat, String bgSize) {
    Map<String, String> styles = new LinkedHashMap<>();
    styles.put("background", bgCss);
    styles.put("background-position", bgPosition);
    styles.put("background-repeat", bgRepeat);
    styles.put("background-size", bgSize);
    styles.put("width", "100%");
    return styles;
  }
}
