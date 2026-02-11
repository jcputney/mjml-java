package dev.jcputney.mjml.util;

/**
 * Utility to escape values for safe interpolation into CSS contexts.
 * Used to prevent CSS injection when embedding user-controlled values in
 * {@code url()}, {@code @import}, and other CSS constructs.
 */
public final class CssEscaper {

  private CssEscaper() {
  }

  /**
   * Escapes a URL for safe use inside a CSS {@code url("...")} context.
   * Escapes backslashes, double quotes, and parentheses to prevent breakout.
   */
  public static String escapeCssUrl(String url) {
    if (url == null || url.isEmpty()) {
      return url;
    }
    boolean needsEscape = false;
    for (int i = 0; i < url.length(); i++) {
      char c = url.charAt(i);
      if (c == '\\' || c == '"' || c == '\'' || c == '(' || c == ')') {
        needsEscape = true;
        break;
      }
    }
    if (!needsEscape) {
      return url;
    }
    StringBuilder sb = new StringBuilder(url.length() + 16);
    for (int i = 0; i < url.length(); i++) {
      char c = url.charAt(i);
      switch (c) {
        case '\\' -> sb.append("\\\\");
        case '"' -> sb.append("\\\"");
        case '\'' -> sb.append("\\'");
        case '(' -> sb.append("\\(");
        case ')' -> sb.append("\\)");
        default -> sb.append(c);
      }
    }
    return sb.toString();
  }
}
