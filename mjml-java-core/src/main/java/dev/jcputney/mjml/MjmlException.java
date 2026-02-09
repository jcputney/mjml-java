package dev.jcputney.mjml;

/**
 * Runtime exception thrown when MJML parsing or rendering fails.
 */
public class MjmlException extends RuntimeException {

  public MjmlException(String message) {
    super(message);
  }

  public MjmlException(String message, Throwable cause) {
    super(message, cause);
  }
}
