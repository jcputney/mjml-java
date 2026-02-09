package dev.jcputney.mjml.render;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlConfiguration;
import dev.jcputney.mjml.MjmlRenderer;
import org.junit.jupiter.api.Test;

/**
 * Tests for Outlook/MSO VML background image rendering. This is a critical gap — the MSO
 * conditional code paths had zero dedicated tests prior to this.
 */
class OutlookVmlTest {

  // --- VML background image rendering via full render pipeline ---

  @Test
  void sectionWithBackgroundUrlContainsVmlRect() {
    String mjml = """
        <mjml>
          <mj-body>
            <mj-section background-url="https://example.com/bg.jpg" background-color="#ffffff">
              <mj-column>
                <mj-text>Content</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertTrue(html.contains("<v:rect"), "Output should contain VML v:rect for background image");
    assertTrue(html.contains("<v:fill"), "Output should contain VML v:fill for background image");
    assertTrue(html.contains("<v:textbox"), "Output should contain VML v:textbox");
  }

  @Test
  void sectionWithBackgroundUrlContainsMsoConditionalComments() {
    String mjml = """
        <mjml>
          <mj-body>
            <mj-section background-url="https://example.com/bg.jpg">
              <mj-column>
                <mj-text>Content</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertTrue(html.contains("<!--[if mso | IE]>"), "Output should contain MSO conditional open");
    assertTrue(html.contains("<![endif]-->"), "Output should contain MSO conditional close");
    assertTrue(html.contains("</v:textbox></v:rect>"),
        "VML rect/textbox should be closed inside MSO conditional");
  }

  @Test
  void sectionWithoutBackgroundUrlHasNoVml() {
    String mjml = """
        <mjml>
          <mj-body>
            <mj-section background-color="#ffffff">
              <mj-column>
                <mj-text>Content</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertFalse(html.contains("<v:rect"), "Section without bg-url should not have VML rect");
    assertFalse(html.contains("<v:fill"), "Section without bg-url should not have VML fill");
  }

  @Test
  void fullWidthSectionWithBackgroundUrlContainsVml() {
    String mjml = """
        <mjml>
          <mj-body>
            <mj-section full-width="full-width" background-url="https://example.com/bg.jpg" background-color="#cccccc">
              <mj-column>
                <mj-text>Full width with background</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertTrue(html.contains("<v:rect"), "Full-width section with bg-url should have VML rect");
    assertTrue(html.contains("mso-width-percent:1000"),
        "Full-width section VML should use mso-width-percent");
  }

  // --- VmlHelper.buildSectionVmlRect tests (unit level) ---

  @Test
  void vmlRectNoRepeatUsesFrameType() {
    String vml = VmlHelper.buildSectionVmlRect(
        "600px", "https://example.com/bg.jpg", "#ffffff",
        "center top", "cover", "no-repeat");
    assertTrue(vml.contains("type=\"frame\""),
        "no-repeat should use frame type");
  }

  @Test
  void vmlRectRepeatUsesTileType() {
    String vml = VmlHelper.buildSectionVmlRect(
        "600px", "https://example.com/bg.jpg", "#ffffff",
        "center top", "cover", "repeat");
    assertTrue(vml.contains("type=\"tile\""),
        "repeat should use tile type");
  }

  @Test
  void vmlRectAutoSizeUsesTileType() {
    String vml = VmlHelper.buildSectionVmlRect(
        "600px", "https://example.com/bg.jpg", "#ffffff",
        "center top", "auto", "repeat");
    assertTrue(vml.contains("type=\"tile\""),
        "auto size should use tile type");
  }

  @Test
  void vmlRectNoRepeatOriginFormula() {
    // For no-repeat, origin = (-50 + posPercent) / 100
    // center top = 50%, 0% => origin = (0/100, -50/100) = (0, -0.5)
    String vml = VmlHelper.buildSectionVmlRect(
        "600px", "https://example.com/bg.jpg", "",
        "center top", "cover", "no-repeat");
    assertTrue(vml.contains("origin=\"0, -0.5\""),
        "no-repeat center top should produce origin 0, -0.5");
    assertTrue(vml.contains("position=\"0, -0.5\""),
        "no-repeat center top should produce position 0, -0.5");
  }

  @Test
  void vmlRectRepeatOriginFormula() {
    // For repeat (non-auto), origin = posPercent / 100
    // center center = 50%, 50% => origin = (0.5, 0.5)
    String vml = VmlHelper.buildSectionVmlRect(
        "600px", "https://example.com/bg.jpg", "",
        "center center", "cover", "repeat");
    assertTrue(vml.contains("origin=\"0.5, 0.5\""),
        "repeat center center should produce origin 0.5, 0.5");
    assertTrue(vml.contains("position=\"0.5, 0.5\""),
        "repeat center center should produce position 0.5, 0.5");
  }

  @Test
  void vmlRectAutoSizeOriginDefaults() {
    // Auto size always uses origin 0.5, 0 regardless of position
    String vml = VmlHelper.buildSectionVmlRect(
        "600px", "https://example.com/bg.jpg", "",
        "left bottom", "auto", "repeat");
    assertTrue(vml.contains("origin=\"0.5, 0\""),
        "auto size should always use origin 0.5, 0");
    assertTrue(vml.contains("position=\"0.5, 0\""),
        "auto size should always use position 0.5, 0");
  }

  @Test
  void vmlRectCoverSizeAttributes() {
    String vml = VmlHelper.buildSectionVmlRect(
        "600px", "https://example.com/bg.jpg", "",
        "center top", "cover", "no-repeat");
    assertTrue(vml.contains("size=\"1,1\""), "cover should produce size 1,1");
    assertTrue(vml.contains("aspect=\"atleast\""), "cover should produce aspect atleast");
  }

  @Test
  void vmlRectContainSizeAttributes() {
    String vml = VmlHelper.buildSectionVmlRect(
        "600px", "https://example.com/bg.jpg", "",
        "center top", "contain", "no-repeat");
    assertTrue(vml.contains("size=\"1,1\""), "contain should produce size 1,1");
    assertTrue(vml.contains("aspect=\"atmost\""), "contain should produce aspect atmost");
  }

  @Test
  void vmlRectIncludesBgColor() {
    String vml = VmlHelper.buildSectionVmlRect(
        "600px", "https://example.com/bg.jpg", "#ff0000",
        "center top", "auto", "repeat");
    assertTrue(vml.contains("color=\"#ff0000\""),
        "VML fill should include background color");
  }

  @Test
  void vmlRectOmitsBgColorWhenEmpty() {
    String vml = VmlHelper.buildSectionVmlRect(
        "600px", "https://example.com/bg.jpg", "",
        "center top", "auto", "repeat");
    assertFalse(vml.contains("color=\"\""),
        "VML fill should not include empty color attribute");
  }

  // --- VML attribute escaping (XSS prevention) ---

  @Test
  void vmlRectEscapesMaliciousBackgroundUrl() {
    MjmlConfiguration config = MjmlConfiguration.builder()
        .sanitizeOutput(true)
        .build();

    String mjml = """
        <mjml>
          <mj-body>
            <mj-section background-url="x&quot; onmouseover=&quot;alert(1)" background-color="#fff">
              <mj-column>
                <mj-text>Content</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml, config).html();
    assertNotNull(html);
    assertFalse(html.contains("onmouseover=\"alert(1)\""),
        "XSS payload in background-url should be escaped in VML output");
  }

  @Test
  void vmlHelperEscapesUrlDirectly() {
    // Test that VmlHelper.buildSectionVmlRect uses escapeAttributeValue on the URL
    String vml = VmlHelper.buildSectionVmlRect(
        "600px", "https://example.com/bg.jpg\" onload=\"alert(1)", "#ffffff",
        "center top", "auto", "repeat");
    assertFalse(vml.contains("onload=\"alert(1)\""),
        "VML helper should escape attribute values in URLs");
    assertTrue(vml.contains("&quot;"),
        "Quotes in URLs should be escaped to &quot;");
  }

  // --- Wrapper VML tests ---

  @Test
  void wrapperVmlRectAlwaysUsesTile() {
    String vml = VmlHelper.buildWrapperVmlRect(
        "600px", "https://example.com/bg.jpg", "#ffffff",
        "center top", "cover");
    assertTrue(vml.contains("type=\"tile\""),
        "Wrapper VML should always use tile type");
  }

  @Test
  void wrapperVmlRectKeywordMapping() {
    // center top should map to "0.5, 0"
    String vml = VmlHelper.buildWrapperVmlRect(
        "600px", "https://example.com/bg.jpg", "",
        "center top", "auto");
    assertTrue(vml.contains("origin=\"0.5, 0\""),
        "Wrapper should use keyword-based origin mapping");
    assertTrue(vml.contains("position=\"0.5, 0\""),
        "Wrapper should use keyword-based position mapping");
  }

  @Test
  void wrapperVmlRectEscapesUrl() {
    String vml = VmlHelper.buildWrapperVmlRect(
        "600px", "https://example.com/<script>alert(1)</script>", "",
        "center top", "auto");
    assertFalse(vml.contains("<script>"),
        "Wrapper VML should escape HTML in URLs");
    assertTrue(vml.contains("&lt;script&gt;"),
        "HTML in URLs should be escaped");
  }

  // --- MSO skeleton output tests ---

  @Test
  void htmlOutputContainsMsoOfficeSettings() {
    String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Content</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertTrue(html.contains("<!--[if mso]>"), "Should contain MSO conditional for office settings");
    assertTrue(html.contains("<o:OfficeDocumentSettings>"), "Should contain office settings XML");
    assertTrue(html.contains("<o:AllowPNG/>"), "Should contain AllowPNG setting");
    assertTrue(html.contains("<o:PixelsPerInch>96</o:PixelsPerInch>"),
        "Should contain PixelsPerInch setting");
  }

  @Test
  void htmlOutputContainsMsoOutlookGroupFix() {
    String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Content</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertTrue(html.contains("<!--[if lte mso 11]>"),
        "Should contain lte mso 11 conditional");
    assertTrue(html.contains(".mj-outlook-group-fix"),
        "Should contain mj-outlook-group-fix style");
  }

  @Test
  void htmlOutputContainsXmlNamespaces() {
    String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Content</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertTrue(html.contains("xmlns:v=\"urn:schemas-microsoft-com:vml\""),
        "Should contain VML namespace declaration");
    assertTrue(html.contains("xmlns:o=\"urn:schemas-microsoft-com:office:office\""),
        "Should contain Office namespace declaration");
  }

  @Test
  void msoColumnTableRenderedForMultipleColumns() {
    String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Col 1</mj-text>
              </mj-column>
              <mj-column>
                <mj-text>Col 2</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertTrue(html.contains("Col 1"));
    assertTrue(html.contains("Col 2"));
    // MSO conditional table for column layout
    assertTrue(html.contains("<!--[if mso | IE]><table role=\"presentation\""),
        "Should contain MSO table for column layout");
  }
}
