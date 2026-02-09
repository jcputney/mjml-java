package dev.jcputney.mjml;

/**
 * Exception thrown when a failure occurs during the MJML rendering phase
 * (as opposed to parsing or input validation).
 */
public class MjmlRenderException extends MjmlException {

  public MjmlRenderException(String message) {
    super(message);
  }

  public MjmlRenderException(String message, Throwable cause) {
    super(message, cause);
  }
}
