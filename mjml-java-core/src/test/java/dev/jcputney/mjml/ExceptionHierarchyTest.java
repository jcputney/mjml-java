package dev.jcputney.mjml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

/** Tests that the exception hierarchy is correctly structured. */
class ExceptionHierarchyTest {

  @Test
  void mjmlExceptionIsRuntimeException() {
    MjmlException ex = new MjmlException("test");
    assertInstanceOf(RuntimeException.class, ex, "MjmlException should extend RuntimeException");
  }

  @Test
  void mjmlParseExceptionExtendsMjmlException() {
    MjmlParseException ex = new MjmlParseException("parse error");
    assertInstanceOf(MjmlException.class, ex, "MjmlParseException should extend MjmlException");
    assertEquals("parse error", ex.getMessage());
  }

  @Test
  void mjmlValidationExceptionExtendsMjmlException() {
    MjmlValidationException ex = new MjmlValidationException("validation error");
    assertInstanceOf(
        MjmlException.class, ex, "MjmlValidationException should extend MjmlException");
    assertEquals("validation error", ex.getMessage());
  }

  @Test
  void mjmlIncludeExceptionExtendsMjmlException() {
    MjmlIncludeException ex = new MjmlIncludeException("include error");
    assertInstanceOf(MjmlException.class, ex, "MjmlIncludeException should extend MjmlException");
    assertEquals("include error", ex.getMessage());
  }

  @Test
  void mjmlRenderExceptionExtendsMjmlExceptionAndPreservesCause() {
    RuntimeException cause = new RuntimeException("root cause");
    MjmlRenderException ex = new MjmlRenderException("render failed", cause);
    assertInstanceOf(MjmlException.class, ex, "MjmlRenderException should extend MjmlException");
    assertEquals("render failed", ex.getMessage());
    assertSame(cause, ex.getCause(), "Cause should be preserved");
    assertNotNull(ex.getCause());
  }
}
