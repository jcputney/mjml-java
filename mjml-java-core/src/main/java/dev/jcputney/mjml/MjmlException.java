package dev.jcputney.mjml;

/** Runtime exception thrown when MJML parsing or rendering fails. */
public class MjmlException extends RuntimeException {

  /**
   * Creates a new exception with the specified detail message.
   *
   * @param message the detail message
   */
  public MjmlException(String message) {
    super(message);
  }

  /**
   * Creates a new exception with the specified detail message and cause.
   *
   * @param message the detail message
   * @param cause the cause of this exception
   */
  public MjmlException(String message, Throwable cause) {
    super(message, cause);
  }
}
