
package dev.jcputney.mjml.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    String vml = VmlHelper.buildSectionVmlRect(
      "600px", "https://example.com/bg.jpg", "#ffffff", "center top", "cover", "no-repeat");
    assertTrue(vml.contains("<v:rect"), "Should contain v:rect");
    assertTrue(vml.contains("src=\"https://example.com/bg.jpg\""), "Should contain image URL");
    assertTrue(vml.contains("type=\"frame\""), "no-repeat cover should be frame");
    assertTrue(vml.contains("<v:textbox"), "Should contain v:textbox");
  }

  @Test
  void buildSectionVmlRectRepeatUsesTile() {
    String vml = VmlHelper.buildSectionVmlRect(
      "600px", "https://example.com/pattern.png", "#cccccc", "left top", "cover", "repeat");
    assertTrue(vml.contains("<v:rect"), "Should contain v:rect");
    assertTrue(vml.contains("type=\"tile\""), "repeat should use tile type");
    assertTrue(vml.contains("color=\"#cccccc\""), "Should include background color");
    assertTrue(vml.contains("size=\"1,1\" aspect=\"atleast\""), "cover should have atleast aspect");
  }

  @Test
  void buildSectionVmlRectAutoSizeUsesDefaultOrigin() {
    String vml = VmlHelper.buildSectionVmlRect(
      "600px", "https://example.com/bg.jpg", null, "center top", "auto", "no-repeat");
    assertTrue(vml.contains("type=\"tile\""), "auto size should use tile type");
    // Origin should be 0.5, 0 for auto
    assertTrue(vml.contains("origin=\"0.5, 0\""), "auto size origin should be 0.5, 0");
  }

  @Test
  void buildSectionVmlRectWithContainSize() {
    String vml = VmlHelper.buildSectionVmlRect(
      "600px", "https://example.com/bg.jpg", "#fff", "center center", "contain", "no-repeat");
    assertTrue(vml.contains("size=\"1,1\" aspect=\"atmost\""), "contain should have atmost aspect");
  }

  @Test
  void buildSectionVmlRectNullBgColorOmitsColorAttribute() {
    String vml = VmlHelper.buildSectionVmlRect(
      "600px", "https://example.com/bg.jpg", null, "center top", "cover", "no-repeat");
    assertFalse(vml.contains("color="), "Should not include color when bgColor is null");
  }

  // --- Wrapper VML ---

  @Test
  void buildWrapperVmlRectContainsExpectedMarkup() {
    String vml = VmlHelper.buildWrapperVmlRect(
      "600px", "https://example.com/wrap-bg.jpg", "#ffffff", "center top", "cover");
    assertTrue(vml.contains("<v:rect"), "Should contain v:rect");
    assertTrue(vml.contains("src=\"https://example.com/wrap-bg.jpg\""), "Should contain image URL");
    assertTrue(vml.contains("type=\"tile\""), "wrapper always uses tile type");
    assertTrue(vml.contains("<v:textbox"), "Should contain v:textbox");
    assertTrue(vml.contains("size=\"1,1\" aspect=\"atleast\""), "cover should have atleast aspect");
  }

  @Test
  void buildWrapperVmlRectWithAutoSize() {
    String vml = VmlHelper.buildWrapperVmlRect("600px", "https://example.com/bg.jpg", "#333", "left top", "auto");
    assertTrue(vml.contains("type=\"tile\""), "wrapper should use tile type");
    assertFalse(vml.contains("size="), "auto size should not emit size attribute");
    assertTrue(vml.contains("origin=\"0, 0\""), "left top origin should be 0, 0");
  }

  @Test
  void buildWrapperVmlRectWithContainSize() {
    String vml =
      VmlHelper.buildWrapperVmlRect("600px", "https://example.com/bg.jpg", "#000", "right bottom", "contain");
    assertTrue(vml.contains("size=\"1,1\" aspect=\"atmost\""), "contain should have atmost aspect");
    assertTrue(vml.contains("origin=\"1, 1\""), "right bottom origin should be 1, 1");
  }

  // --- cssPositionToVmlOrigin ---

  @Test
  void cssPositionToVmlOriginCenterTop() {
    assertEquals("0.5, 0", VmlHelper.cssPositionToVmlOrigin("center top"));
  }

  @Test
  void cssPositionToVmlOriginRightBottom() {
    assertEquals("1, 1", VmlHelper.cssPositionToVmlOrigin("right bottom"));
  }
}
