package dev.jcputney.mjml.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests for VML coordinate calculations in VmlHelper. Covers the formulas for section (complex) and
 * wrapper (simple) VML output.
 */
class VmlCoordinateTest {

  // --- cssPositionToPercent tests ---

  @Test
  void cssPositionToPercentKeywords() {
    assertEquals(0, VmlHelper.cssPositionToPercent("left", true));
    assertEquals(100, VmlHelper.cssPositionToPercent("right", true));
    assertEquals(0, VmlHelper.cssPositionToPercent("top", false));
    assertEquals(100, VmlHelper.cssPositionToPercent("bottom", false));
    assertEquals(50, VmlHelper.cssPositionToPercent("center", true));
    assertEquals(50, VmlHelper.cssPositionToPercent("center", false));
  }

  @Test
  void cssPositionToPercentValues() {
    assertEquals(75, VmlHelper.cssPositionToPercent("75%", true));
    assertEquals(0, VmlHelper.cssPositionToPercent("0%", false));
    assertEquals(100, VmlHelper.cssPositionToPercent("100%", true));
  }

  @Test
  void cssPositionToPercentDefaults() {
    assertEquals(50, VmlHelper.cssPositionToPercent("abc", true));
    assertEquals(0, VmlHelper.cssPositionToPercent("abc", false));
  }

  // --- formatVmlCoord tests ---

  @Test
  void formatVmlCoordIntegers() {
    assertEquals("0", VmlHelper.formatVmlCoord(0.0));
    assertEquals("1", VmlHelper.formatVmlCoord(1.0));
    assertEquals("-1", VmlHelper.formatVmlCoord(-1.0));
  }

  @Test
  void formatVmlCoordDecimals() {
    assertEquals("0.5", VmlHelper.formatVmlCoord(0.5));
    assertEquals("-0.25", VmlHelper.formatVmlCoord(-0.25));
  }

  // --- VML origin/position formula tests ---

  @Test
  void vmlAutoSizeUsesFixedOrigin() {
    double vOriginX = 0.5;
    double vPosX = 0.5;
    double vOriginY = 0;
    double vPosY = 0;

    assertEquals("0.5", VmlHelper.formatVmlCoord(vOriginX));
    assertEquals("0.5", VmlHelper.formatVmlCoord(vPosX));
    assertEquals("0", VmlHelper.formatVmlCoord(vOriginY));
    assertEquals("0", VmlHelper.formatVmlCoord(vPosY));
  }

  @Test
  void vmlRepeatFormula() {
    double posXPct = 50;
    double posYPct = 25;

    double vOriginX = posXPct / 100.0;
    double vOriginY = posYPct / 100.0;

    assertEquals("0.5", VmlHelper.formatVmlCoord(vOriginX));
    assertEquals("0.25", VmlHelper.formatVmlCoord(vOriginY));
  }

  @Test
  void vmlNoRepeatFormula() {
    double posXPct = 50;
    double posYPct = 0;

    double vOriginX = (-50 + posXPct) / 100.0;
    double vOriginY = (-50 + posYPct) / 100.0;

    assertEquals("0", VmlHelper.formatVmlCoord(vOriginX));
    assertEquals("-0.5", VmlHelper.formatVmlCoord(vOriginY));
  }

  @Test
  void vmlNoRepeatRightBottom() {
    double posXPct = 100;
    double posYPct = 100;

    double vOriginX = (-50 + posXPct) / 100.0;
    double vOriginY = (-50 + posYPct) / 100.0;

    assertEquals("0.5", VmlHelper.formatVmlCoord(vOriginX));
    assertEquals("0.5", VmlHelper.formatVmlCoord(vOriginY));
  }

  // --- cssAxisToVml tests (used by wrapper) ---

  @Test
  void cssAxisToVmlKeywords() {
    assertEquals("0", VmlHelper.cssAxisToVml("left"));
    assertEquals("0", VmlHelper.cssAxisToVml("top"));
    assertEquals("0.5", VmlHelper.cssAxisToVml("center"));
    assertEquals("1", VmlHelper.cssAxisToVml("right"));
    assertEquals("1", VmlHelper.cssAxisToVml("bottom"));
  }

  @Test
  void cssAxisToVmlDefault() {
    assertEquals("0.5", VmlHelper.cssAxisToVml("unknown"));
  }
}
