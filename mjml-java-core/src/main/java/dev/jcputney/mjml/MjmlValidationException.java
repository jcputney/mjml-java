package dev.jcputney.mjml;

/**
 * Exception thrown when MJML input validation fails (size limit, nesting depth, etc.).
 */
public class MjmlValidationException extends MjmlException {

  public MjmlValidationException(String message) {
    super(message);
  }

  public MjmlValidationException(String message, Throwable cause) {
    super(message, cause);
  }
}
