package dev.jcputney.mjml;

/** Text direction for the HTML document. */
public enum Direction {
  /** Left-to-right text direction. */
  LTR("ltr"),
  /** Right-to-left text direction. */
  RTL("rtl"),
  /** Automatic text direction determined by the browser. */
  AUTO("auto");

  private final String value;

  Direction(String value) {
    this.value = value;
  }

  /**
   * Parses a direction string (case-insensitive).
   *
   * @param direction the direction string ("ltr", "rtl", or "auto")
   * @return the corresponding Direction enum value
   * @throws IllegalArgumentException if the string is not a valid direction
   */
  public static Direction of(String direction) {
    if (direction == null) {
      throw new IllegalArgumentException("Direction cannot be null");
    }
    return switch (direction.toLowerCase()) {
      case "ltr" -> LTR;
      case "rtl" -> RTL;
      case "auto" -> AUTO;
      default -> throw new IllegalArgumentException("Unknown direction: " + direction);
    };
  }

  /**
   * Returns the lowercase string representation used in HTML output.
   *
   * @return the lowercase direction string (e.g. "ltr", "rtl", "auto")
   */
  public String value() {
    return value;
  }

  @Override
  public String toString() {
    return value;
  }
}
