package dev.jcputney.mjml.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CssUnitParserTest {

  @Test
  void parsesPixelValues() {
    assertEquals(100.0, CssUnitParser.toPixels("100px", 600));
    assertEquals(50.0, CssUnitParser.toPixels("50px", 600));
  }

  @Test
  void parsesPercentValues() {
    assertEquals(300.0, CssUnitParser.toPixels("50%", 600));
    assertEquals(600.0, CssUnitParser.toPixels("100%", 600));
  }

  @Test
  void parsesUnitlessValues() {
    assertEquals(100.0, CssUnitParser.toPixels("100", 600));
  }

  @Test
  void handlesNullAndEmpty() {
    assertEquals(0.0, CssUnitParser.toPixels(null, 600));
    assertEquals(0.0, CssUnitParser.toPixels("", 600));
  }

  @Test
  void parsesShorthand1Value() {
    double[] result = CssUnitParser.parseShorthand("10px");
    assertArrayEquals(new double[]{10, 10, 10, 10}, result);
  }

  @Test
  void parsesShorthand2Values() {
    double[] result = CssUnitParser.parseShorthand("10px 20px");
    assertArrayEquals(new double[]{10, 20, 10, 20}, result);
  }

  @Test
  void parsesShorthand3Values() {
    double[] result = CssUnitParser.parseShorthand("10px 20px 30px");
    assertArrayEquals(new double[]{10, 20, 30, 20}, result);
  }

  @Test
  void parsesShorthand4Values() {
    double[] result = CssUnitParser.parseShorthand("10px 20px 30px 40px");
    assertArrayEquals(new double[]{10, 20, 30, 40}, result);
  }

  @Test
  void formatsPxValue() {
    assertEquals("100px", CssUnitParser.formatPx(100.0));
    assertEquals("99px", CssUnitParser.formatPx(99.7));
  }

  @Test
  void formatsIntValue() {
    assertEquals("100", CssUnitParser.formatInt(100.0));
  }

  // --- parseIntPx tests (to cover MjHero:369-378, MjCarousel:472-481) ---

  @Test
  void parsePxReturnsIntegerForPixelValues() {
    assertEquals(300.0, CssUnitParser.parsePx("300px", 0));
    assertEquals(300.0, CssUnitParser.parsePx("300", 0));
  }

  @Test
  void parsePxReturnsDefaultForNullAndEmpty() {
    assertEquals(0.0, CssUnitParser.parsePx(null, 0));
    assertEquals(0.0, CssUnitParser.parsePx("", 0));
  }

  @Test
  void parsePxReturnsDefaultForInvalid() {
    assertEquals(0.0, CssUnitParser.parsePx("abc", 0));
  }

  @Test
  void parsePxReturnsCustomDefault() {
    assertEquals(42.0, CssUnitParser.parsePx(null, 42));
    assertEquals(42.0, CssUnitParser.parsePx("abc", 42));
  }

  // --- Horizontal padding extraction (covers MjButton:169-181) ---

  @Test
  void shorthandExtractsHorizontalPadding() {
    // "10px 20px" -> right=20, left=20, total=40
    double[] pad = CssUnitParser.parseShorthand("10px 20px");
    assertEquals(40.0, pad[1] + pad[3]);
  }

  @Test
  void shorthand4ValueExtractsHorizontalPadding() {
    // "10px 20px 30px 40px" -> right=20, left=40, total=60
    double[] pad = CssUnitParser.parseShorthand("10px 20px 30px 40px");
    assertEquals(60.0, pad[1] + pad[3]);
  }
}
