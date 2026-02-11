package dev.jcputney.mjml;

/** Exception thrown when mj-include processing fails (missing file, circular includes, etc.). */
public class MjmlIncludeException extends MjmlException {

  /**
   * Creates a new include exception with the specified detail message.
   *
   * @param message the detail message
   */
  public MjmlIncludeException(String message) {
    super(message);
  }

  /**
   * Creates a new include exception with the specified detail message and cause.
   *
   * @param message the detail message
   * @param cause the cause of this exception
   */
  public MjmlIncludeException(String message, Throwable cause) {
    super(message, cause);
  }
}
