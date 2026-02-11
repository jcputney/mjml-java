package dev.jcputney.mjml.component.body;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlRenderer;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for MjWrapper's render variants: normal, normal+bg-image, full-width. These
 * verify structural patterns that must be preserved during refactoring.
 */
class MjWrapperRenderTest {

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
            <mj-wrapper background-color="#f0f0f0" padding="20px 0">
              <mj-section>
                <mj-column><mj-text>Wrapper child</mj-text></mj-column>
              </mj-section>
            </mj-wrapper>
          </mj-body>
        </mjml>
        """);
    // MSO table wrapping present
    assertTrue(html.contains("<!--[if mso | IE]>"), "Should have MSO conditional");
    // Background on wrapper div
    assertTrue(html.contains("#f0f0f0"), "Should have wrapper background color");
    // No VML
    assertFalse(html.contains("v:rect"), "Should NOT have VML without bg image");
    // Child section rendered inside wrapper
    assertTrue(html.contains("Wrapper child"), "Should render child section");
  }

  @Test
  void renderNormalWithBgImage() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-body>
            <mj-wrapper background-url="https://example.com/wrap-bg.jpg" background-color="#222">
              <mj-section>
                <mj-column><mj-text>With wrapper bg</mj-text></mj-column>
              </mj-section>
            </mj-wrapper>
          </mj-body>
        </mjml>
        """);
    // VML rect for Outlook
    assertTrue(html.contains("v:rect"), "Should have VML rect");
    assertTrue(html.contains("v:fill"), "Should have VML fill");
    assertTrue(html.contains("wrap-bg.jpg"), "Should reference bg image");
    // Background CSS
    assertTrue(
        html.contains("url('https://example.com/wrap-bg.jpg')"), "Should have CSS background");
    // Child content
    assertTrue(html.contains("With wrapper bg"), "Should render child content");
  }

  @Test
  void renderFullWidth() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-body>
            <mj-wrapper full-width="full-width" background-color="#0000ff">
              <mj-section>
                <mj-column><mj-text>Full width wrapper</mj-text></mj-column>
              </mj-section>
            </mj-wrapper>
          </mj-body>
        </mjml>
        """);
    // Full-width: outer real HTML table
    assertTrue(html.contains("width:100%"), "Should have full-width table");
    assertTrue(html.contains("#0000ff"), "Should have background color");
    // Inner MSO table for width constraint
    assertTrue(html.contains("width:600px"), "Should have inner width constraint");
    // Content
    assertTrue(html.contains("Full width wrapper"), "Should render content");
  }
}
