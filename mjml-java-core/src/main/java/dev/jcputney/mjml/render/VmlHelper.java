package dev.jcputney.mjml.render;

import dev.jcputney.mjml.util.CssUnitParser;
import dev.jcputney.mjml.util.HtmlEscaper;

/**
 * Helper for generating Outlook VML (Vector Markup Language) for background images. Outlook doesn't
 * support CSS background-image, so VML is needed for background images in sections, wrappers, and
 * hero components.
 *
 * <p>MjSection and MjWrapper produce different VML output:
 *
 * <ul>
 *   <li>MjSection uses percent-based origin formulas with auto/repeat/no-repeat modes
 *   <li>MjWrapper always uses tile with simple keyword mapping (cssAxisToVml)
 * </ul>
 */
public final class VmlHelper {

  private VmlHelper() {}

  // --- Section VML (complex repeat/no-repeat/cover logic) ---

  /**
   * Builds VML rect markup for MjSection background image support in MSO/Outlook. Follows the
   * official MJML algorithm for computing VML origin/position/type.
   *
   * @param widthStyle the CSS width style (e.g. "600px" or "mso-width-percent:1000;")
   * @param bgUrl the background image URL
   * @param bgColor the fallback background color (may be null/empty)
   * @param bgPosition resolved CSS background-position (e.g. "center top")
   * @param bgSize CSS background-size value (e.g. "auto", "cover", "contain")
   * @param bgRepeat CSS background-repeat value (e.g. "repeat", "no-repeat")
   * @return the VML rect markup string for section background rendering in Outlook
   */
  public static String buildSectionVmlRect(
      String widthStyle,
      String bgUrl,
      String bgColor,
      String bgPosition,
      String bgSize,
      String bgRepeat) {
    StringBuilder sb = new StringBuilder();
    boolean isNoRepeat = "no-repeat".equals(bgRepeat);

    String[] posParts = CssUnitParser.WHITESPACE.split(bgPosition.trim());
    String posXStr = posParts.length > 0 ? posParts[0] : "center";
    String posYStr = posParts.length > 1 ? posParts[1] : "top";

    double posXPct = cssPositionToPercent(posXStr, true);
    double posYPct = cssPositionToPercent(posYStr, false);

    double vOriginX, vOriginY, vPosX, vPosY;
    String vmlType;

    if ("auto".equals(bgSize)) {
      vmlType = "tile";
      vOriginX = 0.5;
      vPosX = 0.5;
      vOriginY = 0;
      vPosY = 0;
    } else {
      vmlType = isNoRepeat ? "frame" : "tile";
      if (!isNoRepeat) {
        vOriginX = posXPct / 100.0;
        vPosX = posXPct / 100.0;
        vOriginY = posYPct / 100.0;
        vPosY = posYPct / 100.0;
      } else {
        vOriginX = (-50 + posXPct) / 100.0;
        vPosX = (-50 + posXPct) / 100.0;
        vOriginY = (-50 + posYPct) / 100.0;
        vPosY = (-50 + posYPct) / 100.0;
      }
    }

    sb.append("<v:rect style=\"");
    if (widthStyle.contains("mso-width-percent")) {
      sb.append(widthStyle);
    } else {
      sb.append("width:").append(widthStyle).append(";");
    }
    sb.append("\" xmlns:v=\"urn:schemas-microsoft-com:vml\" fill=\"true\" stroke=\"false\">");

    sb.append("<v:fill origin=\"")
        .append(formatVmlCoord(vOriginX))
        .append(", ")
        .append(formatVmlCoord(vOriginY))
        .append("\" position=\"")
        .append(formatVmlCoord(vPosX))
        .append(", ")
        .append(formatVmlCoord(vPosY))
        .append("\" src=\"")
        .append(HtmlEscaper.escapeAttributeValue(bgUrl))
        .append("\"");

    if (bgColor != null && !bgColor.isEmpty()) {
      sb.append(" color=\"").append(HtmlEscaper.escapeAttributeValue(bgColor)).append("\"");
    }

    sb.append(" type=\"").append(vmlType).append("\"");

    if ("cover".equals(bgSize)) {
      sb.append(" size=\"1,1\" aspect=\"atleast\"");
    } else if ("contain".equals(bgSize)) {
      sb.append(" size=\"1,1\" aspect=\"atmost\"");
    } else if (!"auto".equals(bgSize)) {
      String[] bgSplit = CssUnitParser.WHITESPACE.split(bgSize.trim());
      if (bgSplit.length == 1) {
        sb.append(" size=\"")
            .append(HtmlEscaper.escapeAttributeValue(bgSize))
            .append("\" aspect=\"atmost\"");
      } else {
        sb.append(" size=\"")
            .append(HtmlEscaper.escapeAttributeValue(String.join(",", bgSplit)))
            .append("\"");
      }
    }

    sb.append(" />");
    sb.append("<v:textbox style=\"mso-fit-shape-to-text:true\" inset=\"0,0,0,0\">");

    return sb.toString();
  }

  // --- Wrapper VML (simplified always-tile) ---

  /**
   * Builds VML rect markup for MjWrapper background image support in MSO/Outlook. Simpler than
   * section: always uses tile type with keyword-based origin mapping.
   *
   * @param widthStyle the CSS width style (e.g. "600px" or "mso-width-percent:1000;")
   * @param bgUrl the background image URL
   * @param bgColor the fallback background color (may be null/empty)
   * @param bgPosition resolved CSS background-position (e.g. "center top")
   * @param bgSize CSS background-size value (e.g. "auto", "cover", "contain")
   * @return the VML rect markup string for wrapper background rendering in Outlook
   */
  public static String buildWrapperVmlRect(
      String widthStyle, String bgUrl, String bgColor, String bgPosition, String bgSize) {
    StringBuilder sb = new StringBuilder();
    String vmlOrigin = cssPositionToVmlOrigin(bgPosition);

    sb.append("<v:rect style=\"");
    if (widthStyle.contains("mso-width-percent")) {
      sb.append(widthStyle);
    } else {
      sb.append("width:").append(widthStyle).append(";");
    }
    sb.append("\" xmlns:v=\"urn:schemas-microsoft-com:vml\" fill=\"true\" stroke=\"false\">");

    sb.append("<v:fill origin=\"")
        .append(vmlOrigin)
        .append("\" position=\"")
        .append(vmlOrigin)
        .append("\" src=\"")
        .append(HtmlEscaper.escapeAttributeValue(bgUrl))
        .append("\"");

    if (bgColor != null && !bgColor.isEmpty()) {
      sb.append(" color=\"").append(HtmlEscaper.escapeAttributeValue(bgColor)).append("\"");
    }

    sb.append(" type=\"tile\"");

    if ("cover".equals(bgSize)) {
      sb.append(" size=\"1,1\" aspect=\"atleast\"");
    } else if ("contain".equals(bgSize)) {
      sb.append(" size=\"1,1\" aspect=\"atmost\"");
    } else if (!"auto".equals(bgSize)) {
      sb.append(" size=\"")
          .append(HtmlEscaper.escapeAttributeValue(bgSize.trim().replace(" ", ",")))
          .append("\"");
    }

    sb.append(" />");
    sb.append("<v:textbox style=\"mso-fit-shape-to-text:true\" inset=\"0,0,0,0\">");

    return sb.toString();
  }

  // --- Shared sub-helpers ---

  /**
   * Converts a CSS position keyword or percentage to a percentage value.
   *
   * @param value the CSS position keyword (e.g. "left", "center", "right") or percentage string
   * @param isX {@code true} if this is for the X axis, {@code false} for Y axis
   * @return the corresponding percentage value (0-100)
   */
  public static double cssPositionToPercent(String value, boolean isX) {
    return switch (value) {
      case "left", "top" -> 0;
      case "right", "bottom" -> 100;
      case "center" -> 50;
      default -> {
        if (value.endsWith("%")) {
          try {
            yield Double.parseDouble(value.replace("%", ""));
          } catch (NumberFormatException e) {
            yield isX ? 50 : 0;
          }
        }
        yield isX ? 50 : 0;
      }
    };
  }

  /**
   * Formats a VML coordinate value. Integers are rendered without decimals.
   *
   * @param value the numeric coordinate value to format
   * @return the formatted coordinate string
   */
  public static String formatVmlCoord(double value) {
    if (value == Math.floor(value) && !Double.isInfinite(value)) {
      return String.valueOf((int) value);
    }
    return String.valueOf(value);
  }

  /**
   * Converts a CSS position string to a VML origin string. Maps keyword pairs to "x, y" format
   * using simple keyword lookup.
   */
  static String cssPositionToVmlOrigin(String cssPosition) {
    String[] parts = CssUnitParser.WHITESPACE.split(cssPosition.trim());
    String x = parts.length > 0 ? parts[0] : "center";
    String y = parts.length > 1 ? parts[1] : "top";
    return cssAxisToVml(x) + ", " + cssAxisToVml(y);
  }

  /**
   * Maps a CSS axis keyword to a VML coordinate value.
   *
   * @param value the CSS axis keyword (e.g. "left", "center", "right", "top", "bottom")
   * @return the VML coordinate string ("0", "0.5", or "1")
   */
  public static String cssAxisToVml(String value) {
    return switch (value) {
      case "left", "top" -> "0";
      case "center" -> "0.5";
      case "right", "bottom" -> "1";
      default -> "0.5";
    };
  }
}
