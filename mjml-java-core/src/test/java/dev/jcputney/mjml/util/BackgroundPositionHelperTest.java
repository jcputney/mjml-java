package dev.jcputney.mjml.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests for BackgroundPositionHelper which normalizes CSS background-position
 * values to "x y" format. Shared by MjSection and MjWrapper.
 */
class BackgroundPositionHelperTest {

  @Test
  void normalizeSwapsYXOrder() {
    assertEquals("center top", BackgroundPositionHelper.normalize("top center"));
  }

  @Test
  void normalizeSwapsBottomRight() {
    assertEquals("right bottom", BackgroundPositionHelper.normalize("bottom right"));
  }

  @Test
  void normalizeKeepsCorrectXYOrder() {
    assertEquals("center top", BackgroundPositionHelper.normalize("center top"));
  }

  @Test
  void normalizeKeepsLeftBottom() {
    assertEquals("left bottom", BackgroundPositionHelper.normalize("left bottom"));
  }

  @Test
  void normalizeSingleValueDefaultsYToCenter() {
    assertEquals("50% center", BackgroundPositionHelper.normalize("50%"));
    assertEquals("left center", BackgroundPositionHelper.normalize("left"));
  }

  @Test
  void normalizeNullReturnsDefault() {
    assertEquals("center top", BackgroundPositionHelper.normalize(null));
  }

  @Test
  void normalizeEmptyReturnsDefault() {
    assertEquals("center top", BackgroundPositionHelper.normalize(""));
  }

  @Test
  void normalizePercentValues() {
    assertEquals("50% 25%", BackgroundPositionHelper.normalize("50% 25%"));
  }

  @Test
  void normalizeTopLeft() {
    assertEquals("left top", BackgroundPositionHelper.normalize("top left"));
  }

  @Test
  void isYValueTrue() {
    assertTrue(BackgroundPositionHelper.isYValue("top"));
    assertTrue(BackgroundPositionHelper.isYValue("bottom"));
  }

  @Test
  void isYValueFalse() {
    assertFalse(BackgroundPositionHelper.isYValue("left"));
    assertFalse(BackgroundPositionHelper.isYValue("right"));
    assertFalse(BackgroundPositionHelper.isYValue("center"));
    assertFalse(BackgroundPositionHelper.isYValue("50%"));
  }

  @Test
  void isXValueTrue() {
    assertTrue(BackgroundPositionHelper.isXValue("left"));
    assertTrue(BackgroundPositionHelper.isXValue("right"));
    assertTrue(BackgroundPositionHelper.isXValue("center"));
  }

  @Test
  void isXValueFalse() {
    assertFalse(BackgroundPositionHelper.isXValue("top"));
    assertFalse(BackgroundPositionHelper.isXValue("bottom"));
    assertFalse(BackgroundPositionHelper.isXValue("50%"));
  }
}
