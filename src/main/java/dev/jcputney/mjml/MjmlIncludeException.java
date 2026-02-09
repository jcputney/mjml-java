package dev.jcputney.mjml;

/**
 * Exception thrown when mj-include processing fails (missing file, circular includes, etc.).
 */
public class MjmlIncludeException extends MjmlException {

  public MjmlIncludeException(String message) {
    super(message);
  }

  public MjmlIncludeException(String message, Throwable cause) {
    super(message, cause);
  }
}
