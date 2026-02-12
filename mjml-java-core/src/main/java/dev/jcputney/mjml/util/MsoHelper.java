package dev.jcputney.mjml.util;

/**
 * Shared helpers for generating MSO/IE conditional comments used by {@code <mj-section>}, {@code
 * <mj-wrapper>}, and {@code <mj-hero>}.
 *
 * <p>Outlook and IE do not support modern CSS layout, so MJML emits conditional-comment-wrapped
 * tables to constrain widths. These helpers eliminate duplication of the boilerplate strings across
 * components.
 */
public final class MsoHelper {

  /** Standard MSO td style for resetting line-height. */
  public static final String MSO_TD_STYLE =
      "line-height:0px;font-size:0px;mso-line-height-rule:exactly;";

  /** Variant used by mj-hero (no trailing 'px' on line-height). */
  public static final String MSO_TD_STYLE_HERO =
      "line-height:0;font-size:0;mso-line-height-rule:exactly;";

  /** Pre-computed MSO conditional table closing string. */
  private static final String MSO_CONDITIONAL_TABLE_CLOSING =
      "<!--[if mso | IE]></td></tr></table><![endif]-->";

  private MsoHelper() {}

  /**
   * Opens an MSO conditional comment block.
   *
   * @return {@code <!--[if mso | IE]>}
   */
  public static String conditionalStart() {
    return "<!--[if mso | IE]>";
  }

  /**
   * Closes an MSO conditional comment block.
   *
   * @return {@code <![endif]-->}
   */
  public static String conditionalEnd() {
    return "<![endif]-->";
  }

  /**
   * Builds an MSO table opening with width constraint. Emits: {@code <table align="center"
   * border="0" cellpadding="0" cellspacing="0" class="{cssClass}" role="presentation"
   * style="width:{width}px;" width="{width}" [bgcolor="{bgColor}"]><tr><td style="{tdStyle}">}
   *
   * @param width container width in pixels
   * @param cssClass CSS class for the table (already escaped)
   * @param bgColor background color, or null/empty to omit bgcolor attribute
   * @param tdStyle style string for the inner td
   * @return the MSO table opening markup (without conditional comments)
   */
  public static String msoTableOpening(int width, String cssClass, String bgColor, String tdStyle) {
    StringBuilder sb = new StringBuilder();
    // MJML appends "-outlook" suffix to css-class on MSO tables
    String msoClass = cssClass.isEmpty() ? "" : cssClass + "-outlook";
    sb.append("<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"")
        .append(msoClass)
        .append("\" role=\"presentation\" style=\"width:")
        .append(width)
        .append("px;\" width=\"")
        .append(width)
        .append("\" ");
    if (bgColor != null && !bgColor.isEmpty()) {
      sb.append("bgcolor=\"").append(bgColor).append("\" ");
    }
    sb.append("><tr><td style=\"").append(tdStyle).append("\">");
    return sb.toString();
  }

  /**
   * Builds an MSO table closing.
   *
   * @return {@code </td></tr></table>}
   */
  public static String msoTableClosing() {
    return "</td></tr></table>";
  }

  /**
   * Builds a complete MSO table closing wrapped in conditional comments.
   *
   * @return {@code <!--[if mso | IE]></td></tr></table><![endif]-->}
   */
  public static String msoConditionalTableClosing() {
    return MSO_CONDITIONAL_TABLE_CLOSING;
  }
}
