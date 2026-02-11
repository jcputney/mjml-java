package dev.jcputney.mjml;

/**
 * Exception thrown when a failure occurs during the MJML rendering phase (as opposed to parsing or
 * input validation).
 */
public class MjmlRenderException extends MjmlException {

  /**
   * Creates a new render exception with the specified detail message.
   *
   * @param message the detail message describing the render failure
   */
  public MjmlRenderException(String message) {
    super(message);
  }

  /**
   * Creates a new render exception with the specified detail message and cause.
   *
   * @param message the detail message describing the render failure
   * @param cause the underlying cause of the render failure
   */
  public MjmlRenderException(String message, Throwable cause) {
    super(message, cause);
  }
}
