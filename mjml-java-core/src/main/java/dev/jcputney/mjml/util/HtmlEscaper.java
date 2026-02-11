package dev.jcputney.mjml.util;

/**
 * Utility to escape HTML special characters in attribute values. Used when {@code sanitizeOutput}
 * is enabled in configuration.
 */
public final class HtmlEscaper {

  private HtmlEscaper() {}

  /**
   * Escapes HTML special characters in an attribute value: {@code &}, {@code "}, {@code '}, {@code
   * <}, {@code >}.
   *
   * @param value the attribute value to escape (may be {@code null} or empty)
   * @return the escaped value, or the original value if no escaping is needed
   */
  public static String escapeAttributeValue(String value) {
    if (value == null || value.isEmpty()) {
      return value;
    }
    // Quick check: if no special characters, return as-is
    boolean needsEscape = false;
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (c == '&' || c == '"' || c == '\'' || c == '<' || c == '>') {
        needsEscape = true;
        break;
      }
    }
    if (!needsEscape) {
      return value;
    }
    StringBuilder sb = new StringBuilder(value.length() + 16);
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      switch (c) {
        case '&' -> sb.append("&amp;");
        case '"' -> sb.append("&quot;");
        case '\'' -> sb.append("&#39;");
        case '<' -> sb.append("&lt;");
        case '>' -> sb.append("&gt;");
        default -> sb.append(c);
      }
    }
    return sb.toString();
  }
}
