package dev.jcputney.mjml.render;

/**
 * Helper for generating Outlook VML (Vector Markup Language) for background images.
 * Outlook doesn't support CSS background-image, so VML is needed for
 * background images in sections, wrappers, and hero components.
 */
public final class VmlHelper {

  private VmlHelper() {
  }

  /**
   * Renders a VML background image wrapper for Outlook.
   * This wraps the given content in v:rect/v:fill/v:textbox elements.
   *
   * @param backgroundUrl  the background image URL
   * @param bgColor        the fallback background color
   * @param bgPosition     CSS background-position value
   * @param bgRepeat       CSS background-repeat value
   * @param bgSize         CSS background-size value
   * @param width          container width in pixels
   * @param content        the HTML content to wrap inside the VML element
   * @return the VML-wrapped HTML string
   */
  public static String renderBackground(String backgroundUrl, String bgColor,
      String bgPosition, String bgRepeat, String bgSize,
      int width, String content) {
    if (backgroundUrl == null || backgroundUrl.isEmpty()) {
      return content;
    }

    StringBuilder sb = new StringBuilder();

    // Open MSO conditional with VML
    sb.append("<!--[if mso | IE]>\n");
    sb.append("<v:rect xmlns:v=\"urn:schemas-microsoft-com:vml\" fill=\"true\" stroke=\"false\"");
    sb.append(" style=\"width:").append(width).append("px;\">\n");

    // VML fill
    sb.append("<v:fill origin=\"0.5, 0\" position=\"0.5, 0\"");
    sb.append(" src=\"").append(backgroundUrl).append("\"");

    if (bgColor != null && !bgColor.isEmpty()) {
      sb.append(" color=\"").append(bgColor).append("\"");
    }

    String vmlType = getVmlFillType(bgSize, bgRepeat);
    sb.append(" type=\"").append(vmlType).append("\"");

    if ("cover".equals(bgSize) || "contain".equals(bgSize)) {
      sb.append(" size=\"1,1\" aspect=\"atleast\"");
    }

    sb.append(" />\n");

    // VML textbox
    sb.append("<v:textbox style=\"mso-fit-shape-to-text:true\" inset=\"0,0,0,0\">\n");
    sb.append("<![endif]-->\n");

    // Content
    sb.append(content);

    // Close VML
    sb.append("<!--[if mso | IE]>\n");
    sb.append("</v:textbox>\n");
    sb.append("</v:rect>\n");
    sb.append("<![endif]-->\n");

    return sb.toString();
  }

  /**
   * Renders a VML background for a hero component with specified height.
   */
  public static String renderHeroBackground(String backgroundUrl, String bgColor,
      String bgPosition, int width, int height, String content) {
    if (backgroundUrl == null || backgroundUrl.isEmpty()) {
      return content;
    }

    StringBuilder sb = new StringBuilder();

    sb.append("<!--[if mso | IE]>\n");
    sb.append("<v:image xmlns:v=\"urn:schemas-microsoft-com:vml\" fill=\"true\" stroke=\"false\"");
    sb.append(" style=\"width:").append(width).append("px;height:").append(height).append("px;\"");
    sb.append(" src=\"").append(backgroundUrl).append("\" />\n");
    sb.append("<v:rect xmlns:v=\"urn:schemas-microsoft-com:vml\" fill=\"true\" stroke=\"false\"");
    sb.append(" style=\"position:absolute;width:").append(width).append("px;height:").append(height)
        .append("px;\">\n");

    if (bgColor != null && !bgColor.isEmpty()) {
      sb.append("<v:fill opacity=\"0\" color=\"").append(bgColor).append("\" />\n");
    }

    sb.append(
        "<v:textbox inset=\"0,0,0,0\" style=\"mso-fit-shape-to-text:true\" >\n");
    sb.append("<![endif]-->\n");

    sb.append(content);

    sb.append("<!--[if mso | IE]>\n");
    sb.append("</v:textbox>\n");
    sb.append("</v:rect>\n");
    sb.append("<![endif]-->\n");

    return sb.toString();
  }

  /**
   * Maps CSS background properties to VML fill type.
   */
  private static String getVmlFillType(String bgSize, String bgRepeat) {
    if ("cover".equals(bgSize) || "contain".equals(bgSize)) {
      return "frame";
    }
    if ("no-repeat".equals(bgRepeat)) {
      return "frame";
    }
    return "tile";
  }
}
