package dev.jcputney.mjml.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CssBoxModelTest {

  @Test
  void calculatesHorizontalSpacing() {
    CssBoxModel box = CssBoxModel.fromAttributes("10px 20px", "none", "", "");
    assertEquals(40.0, box.horizontalSpacing()); // 20 left + 20 right
  }

  @Test
  void includesBorderWidth() {
    CssBoxModel box = CssBoxModel.fromAttributes("0", "2px solid #000", "", "");
    assertEquals(4.0, box.horizontalSpacing()); // 2 left + 2 right
  }

  @Test
  void handlesIndividualBorders() {
    CssBoxModel box = CssBoxModel.fromAttributes("0", "none", "3px solid #000",
        "5px solid #000");
    assertEquals(8.0, box.horizontalSpacing()); // 3 left + 5 right
  }

  @Test
  void handlesNullValues() {
    CssBoxModel box = CssBoxModel.fromAttributes(null, null, null, null);
    assertEquals(0.0, box.horizontalSpacing());
    assertEquals(0.0, box.verticalSpacing());
  }
}
