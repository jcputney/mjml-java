package dev.jcputney.mjml.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests that CssUnitParser.parseIntPx() rejects garbage input instead of extracting numbers from
 * within invalid strings.
 */
class CssUnitParserGarbageTest {

  @Test
  void garbageInputReturnsZero() {
    // Previously would return 123 by stripping non-numeric chars
    assertEquals(0, CssUnitParser.parseIntPx("abc123xyz"));
  }

  @Test
  void validPixelValueWorks() {
    assertEquals(10, CssUnitParser.parseIntPx("10px"));
  }

  @Test
  void validUnitlessValueWorks() {
    assertEquals(42, CssUnitParser.parseIntPx("42"));
  }

  @Test
  void validNegativeValueWorks() {
    assertEquals(-5, CssUnitParser.parseIntPx("-5px"));
  }

  @Test
  void validDecimalTruncated() {
    assertEquals(10, CssUnitParser.parseIntPx("10.5px"));
  }

  @Test
  void nullReturnsZero() {
    assertEquals(0, CssUnitParser.parseIntPx(null));
  }

  @Test
  void emptyReturnsZero() {
    assertEquals(0, CssUnitParser.parseIntPx(""));
  }

  @Test
  void whitespaceOnlyReturnsZero() {
    assertEquals(0, CssUnitParser.parseIntPx("   "));
  }

  @Test
  void mixedGarbageReturnsZero() {
    assertEquals(0, CssUnitParser.parseIntPx("12px34"));
  }

  @Test
  void percentValueWorks() {
    assertEquals(50, CssUnitParser.parseIntPx("50%"));
  }

  @Test
  void zeroWorks() {
    assertEquals(0, CssUnitParser.parseIntPx("0px"));
    assertEquals(0, CssUnitParser.parseIntPx("0"));
  }
}
