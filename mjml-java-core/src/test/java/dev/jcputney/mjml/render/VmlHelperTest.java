package dev.jcputney.mjml.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Tests for VmlHelper static utility methods. */
class VmlHelperTest {

  // --- cssPositionToPercent tests ---

  @Test
  void cssPositionToPercentLeft() {
    assertEquals(0.0, VmlHelper.cssPositionToPercent("left", true));
  }

  @Test
  void cssPositionToPercentCenter() {
    assertEquals(50.0, VmlHelper.cssPositionToPercent("center", true));
    assertEquals(50.0, VmlHelper.cssPositionToPercent("center", false));
  }

  @Test
  void cssPositionToPercentRight() {
    assertEquals(100.0, VmlHelper.cssPositionToPercent("right", true));
  }

  @Test
  void cssPositionToPercentTopAndBottom() {
    assertEquals(0.0, VmlHelper.cssPositionToPercent("top", false));
    assertEquals(100.0, VmlHelper.cssPositionToPercent("bottom", false));
  }

  @Test
  void cssPositionToPercentWithPercentage() {
    assertEquals(75.0, VmlHelper.cssPositionToPercent("75%", true));
    assertEquals(0.0, VmlHelper.cssPositionToPercent("0%", false));
    assertEquals(100.0, VmlHelper.cssPositionToPercent("100%", true));
  }

  @Test
  void cssPositionToPercentInvalidDefaultsBasedOnAxis() {
    // Invalid value: X axis defaults to 50, Y axis defaults to 0
    assertEquals(50.0, VmlHelper.cssPositionToPercent("invalid", true));
    assertEquals(0.0, VmlHelper.cssPositionToPercent("invalid", false));
    assertEquals(50.0, VmlHelper.cssPositionToPercent("abc%", true));
  }

  // --- formatVmlCoord tests ---

  @Test
  void formatVmlCoordInteger() {
    assertEquals("0", VmlHelper.formatVmlCoord(0.0));
    assertEquals("1", VmlHelper.formatVmlCoord(1.0));
    assertEquals("-1", VmlHelper.formatVmlCoord(-1.0));
  }

  @Test
  void formatVmlCoordFraction() {
    assertEquals("0.5", VmlHelper.formatVmlCoord(0.5));
    assertEquals("0.75", VmlHelper.formatVmlCoord(0.75));
    assertEquals("-0.5", VmlHelper.formatVmlCoord(-0.5));
  }

  // --- cssAxisToVml tests ---

  @Test
  void cssAxisToVmlAllKeywords() {
    assertEquals("0", VmlHelper.cssAxisToVml("left"));
    assertEquals("0", VmlHelper.cssAxisToVml("top"));
    assertEquals("0.5", VmlHelper.cssAxisToVml("center"));
    assertEquals("1", VmlHelper.cssAxisToVml("right"));
    assertEquals("1", VmlHelper.cssAxisToVml("bottom"));
    assertEquals("0.5", VmlHelper.cssAxisToVml("unknown"), "Unknown values should default to 0.5");
  }

  // --- buildSectionVmlRect integration ---

  @Test
  void buildSectionVmlRectContainsExpectedMarkup() {
    String vml =
        VmlHelper.buildSectionVmlRect(
            "600px", "https://example.com/bg.jpg", "#ffffff", "center top", "cover", "no-repeat");
    assertTrue(vml.contains("<v:rect"), "Should contain v:rect");
    assertTrue(vml.contains("src=\"https://example.com/bg.jpg\""), "Should contain image URL");
    assertTrue(vml.contains("type=\"frame\""), "no-repeat cover should be frame");
    assertTrue(vml.contains("<v:textbox"), "Should contain v:textbox");
  }
}
