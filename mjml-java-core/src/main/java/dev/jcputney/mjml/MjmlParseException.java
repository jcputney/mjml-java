package dev.jcputney.mjml;

/** Exception thrown when MJML parsing fails (malformed XML, missing root element, etc.). */
public class MjmlParseException extends MjmlException {

  /**
   * Creates a new parse exception with the specified detail message.
   *
   * @param message the detail message describing the parse failure
   */
  public MjmlParseException(String message) {
    super(message);
  }

  /**
   * Creates a new parse exception with the specified detail message and cause.
   *
   * @param message the detail message describing the parse failure
   * @param cause the underlying cause of the parse failure
   */
  public MjmlParseException(String message, Throwable cause) {
    super(message, cause);
  }
}
