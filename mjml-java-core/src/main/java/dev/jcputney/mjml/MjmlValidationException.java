package dev.jcputney.mjml;

/** Exception thrown when MJML input validation fails (size limit, nesting depth, etc.). */
public class MjmlValidationException extends MjmlException {

  /**
   * Creates a new validation exception with the specified detail message.
   *
   * @param message the detail message describing the validation failure
   */
  public MjmlValidationException(String message) {
    super(message);
  }

  /**
   * Creates a new validation exception with the specified detail message and cause.
   *
   * @param message the detail message describing the validation failure
   * @param cause the underlying cause of the validation failure
   */
  public MjmlValidationException(String message, Throwable cause) {
    super(message, cause);
  }
}
