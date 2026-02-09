package dev.jcputney.mjml.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MsoHelperTest {

  @Test
  void conditionalStart() {
    assertEquals("<!--[if mso | IE]>", MsoHelper.conditionalStart());
  }

  @Test
  void conditionalEnd() {
    assertEquals("<![endif]-->", MsoHelper.conditionalEnd());
  }

  @Test
  void msoTableOpening() {
    String result = MsoHelper.msoTableOpening(600, "my-class", "#ffffff", MsoHelper.MSO_TD_STYLE);
    assertTrue(result.startsWith("<table align=\"center\""));
    assertTrue(result.contains("class=\"my-class\""));
    assertTrue(result.contains("style=\"width:600px;\""));
    assertTrue(result.contains("width=\"600\""));
    assertTrue(result.contains("bgcolor=\"#ffffff\""));
    assertTrue(result.contains("line-height:0px;font-size:0px;mso-line-height-rule:exactly;"));
    assertTrue(result.endsWith("\">"));
  }

  @Test
  void msoTableOpeningNoBgColor() {
    String result = MsoHelper.msoTableOpening(600, "", null, MsoHelper.MSO_TD_STYLE);
    assertFalse(result.contains("bgcolor"));
  }

  @Test
  void msoTableOpeningEmptyBgColor() {
    String result = MsoHelper.msoTableOpening(600, "", "", MsoHelper.MSO_TD_STYLE);
    assertFalse(result.contains("bgcolor"));
  }

  @Test
  void msoTableClosing() {
    assertEquals("</td></tr></table>", MsoHelper.msoTableClosing());
  }

  @Test
  void msoConditionalTableClosing() {
    assertEquals("<!--[if mso | IE]></td></tr></table><![endif]-->",
        MsoHelper.msoConditionalTableClosing());
  }

  @Test
  void msoTdStyleConstant() {
    assertEquals("line-height:0px;font-size:0px;mso-line-height-rule:exactly;",
        MsoHelper.MSO_TD_STYLE);
  }

  @Test
  void msoTdStyleHeroConstant() {
    assertEquals("line-height:0;font-size:0;mso-line-height-rule:exactly;",
        MsoHelper.MSO_TD_STYLE_HERO);
  }

  @Test
  void msoTableOpeningWithHeroStyle() {
    String result = MsoHelper.msoTableOpening(500, "", null, MsoHelper.MSO_TD_STYLE_HERO);
    assertTrue(result.contains("line-height:0;font-size:0;mso-line-height-rule:exactly;"));
  }
}
