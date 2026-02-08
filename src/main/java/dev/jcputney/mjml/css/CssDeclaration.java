package dev.jcputney.mjml.css;

/**
 * A single CSS property declaration, e.g. {@code color: red !important}.
 *
 * @param property  the CSS property name (lower-cased)
 * @param value     the property value
 * @param important whether {@code !important} was specified
 */
public record CssDeclaration(String property, String value, boolean important) {

  /**
   * Parses a single declaration string like "color: red !important".
   *
   * @return the parsed declaration, or null if the string is not a valid declaration
   */
  public static CssDeclaration parse(String text) {
    if (text == null || text.isBlank()) {
      return null;
    }

    int colonIndex = text.indexOf(':');
    if (colonIndex < 0) {
      return null;
    }

    String prop = text.substring(0, colonIndex).trim().toLowerCase();
    if (prop.isEmpty()) {
      return null;
    }

    String val = text.substring(colonIndex + 1).trim();
    boolean isImportant = false;

    if (val.toLowerCase().endsWith("!important")) {
      isImportant = true;
      val = val.substring(0, val.length() - 10).trim();
    } else {
      // Handle space before !important: "red ! important"
      int bangIndex = val.lastIndexOf('!');
      if (bangIndex >= 0) {
        String afterBang = val.substring(bangIndex + 1).trim();
        if ("important".equalsIgnoreCase(afterBang)) {
          isImportant = true;
          val = val.substring(0, bangIndex).trim();
        }
      }
    }

    if (val.isEmpty()) {
      return null;
    }

    return new CssDeclaration(prop, val, isImportant);
  }

  @Override
  public String toString() {
    return property + ":" + value + (important ? " !important" : "");
  }
}
