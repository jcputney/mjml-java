package dev.jcputney.mjml.component.body;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlRenderer;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for MjSection's 4 render variants: normal, normal+bg-image, full-width,
 * full-width+bg-image. These verify structural patterns that must be preserved during refactoring.
 */
class MjSectionRenderTest {

  private String render(String mjml) {
    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertFalse(html.isEmpty());
    return html;
  }

  @Test
  void renderNormalSimple() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-body>
            <mj-section background-color="#ffffff" padding="20px 0">
              <mj-column><mj-text>Normal section</mj-text></mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    // MSO table wrapping present
    assertTrue(html.contains("<!--[if mso | IE]>"), "Should have MSO conditional");
    assertTrue(html.contains("width:600px"), "Should have container width");
    // Background color on div
    assertTrue(html.contains("background:#ffffff"), "Should have background color");
    // No VML rect (no background image)
    assertFalse(html.contains("v:rect"), "Should NOT have VML rect without bg image");
  }

  @Test
  void renderNormalWithBgImage() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-body>
            <mj-section background-url="https://example.com/bg.jpg" background-color="#ff0000">
              <mj-column><mj-text>With bg image</mj-text></mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    // VML rect for Outlook
    assertTrue(html.contains("v:rect"), "Should have VML rect for bg image");
    assertTrue(html.contains("v:fill"), "Should have VML fill");
    assertTrue(html.contains("v:textbox"), "Should have VML textbox");
    assertTrue(html.contains("bg.jpg"), "Should reference bg image URL");
    // Background CSS on div
    assertTrue(html.contains("url('https://example.com/bg.jpg')"), "Should have CSS background");
    // Close VML
    assertTrue(html.contains("</v:textbox></v:rect>"), "Should close VML elements");
  }

  @Test
  void renderFullWidthSimple() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-body>
            <mj-section full-width="full-width" background-color="#00ff00">
              <mj-column><mj-text>Full width</mj-text></mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    // Full-width: outer real HTML table with width:100%
    assertTrue(html.contains("width:100%"), "Should have full-width table");
    assertTrue(html.contains("#00ff00"), "Should have background color");
    // Inner MSO table for 600px constraint
    assertTrue(html.contains("width:600px"), "Should have inner 600px constraint");
    // No VML (no background image)
    assertFalse(html.contains("v:rect"), "Should NOT have VML without bg image");
  }

  @Test
  void renderFullWidthWithBgImage() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-body>
            <mj-section full-width="full-width" background-url="https://example.com/hero.jpg" background-color="#333">
              <mj-column><mj-text>Full width bg</mj-text></mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    // Outer table with background attribute
    assertTrue(
        html.contains("background=\"https://example.com/hero.jpg\""),
        "Should have background attribute on outer table");
    // VML for Outlook
    assertTrue(html.contains("v:rect"), "Should have VML rect");
    assertTrue(html.contains("mso-width-percent:1000"), "Should use percentage-based VML width");
    // Inner content
    assertTrue(html.contains("Full width bg"), "Should contain content text");
  }
}
