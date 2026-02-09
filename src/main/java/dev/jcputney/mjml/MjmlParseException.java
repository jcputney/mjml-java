package dev.jcputney.mjml;

/**
 * Exception thrown when MJML parsing fails (malformed XML, missing root element, etc.).
 */
public class MjmlParseException extends MjmlException {

  public MjmlParseException(String message) {
    super(message);
  }

  public MjmlParseException(String message, Throwable cause) {
    super(message, cause);
  }
}
