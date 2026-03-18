
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
    String html = render(
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
    String html = render(
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
    assertTrue(html.contains("url('https://example.com/wrap-bg.jpg')"), "Should have CSS background");
    // Child content
    assertTrue(html.contains("With wrapper bg"), "Should render child content");
  }

  @Test
  void renderFullWidth() {
    String html = render(
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

  @Test
  void renderBgImageWithoutBgColor() {
    String html = render(
      // language=MJML
      """
        <mjml>
          <mj-body>
            <mj-wrapper background-url="https://example.com/bg-only.jpg">
              <mj-section>
                <mj-column><mj-text>Bg image only</mj-text></mj-column>
              </mj-section>
            </mj-wrapper>
          </mj-body>
        </mjml>
        """);
    // VML should still render
    assertTrue(html.contains("v:rect"), "Should have VML rect for bg image");
    assertTrue(html.contains("v:fill"), "Should have VML fill");
    assertTrue(html.contains("bg-only.jpg"), "Should reference bg image");
    assertTrue(html.contains("Bg image only"), "Should render child content");
  }

  @Test
  void renderBgWithRepeatSizePosition() {
    String html = render(
      // language=MJML
      """
        <mjml>
          <mj-body>
            <mj-wrapper
              background-url="https://example.com/pattern.png"
              background-color="#aabbcc"
              background-repeat="repeat"
              background-size="cover"
              background-position="left top"
            >
              <mj-section>
                <mj-column><mj-text>Repeat bg</mj-text></mj-column>
              </mj-section>
            </mj-wrapper>
          </mj-body>
        </mjml>
        """);
    assertTrue(html.contains("v:rect"), "Should have VML rect");
    assertTrue(html.contains("pattern.png"), "Should reference bg image");
    assertTrue(html.contains("#aabbcc"), "Should have background color");
    assertTrue(html.contains("Repeat bg"), "Should render child content");
  }

  @Test
  void renderWrapperWithMultipleChildSections() {
    String html = render(
      // language=MJML
      """
        <mjml>
          <mj-body>
            <mj-wrapper background-color="#eeeeee" css-class="my-wrapper">
              <mj-section>
                <mj-column><mj-text>Section One</mj-text></mj-column>
              </mj-section>
              <mj-section>
                <mj-column><mj-text>Section Two</mj-text></mj-column>
              </mj-section>
              <mj-section>
                <mj-column><mj-text>Section Three</mj-text></mj-column>
              </mj-section>
            </mj-wrapper>
          </mj-body>
        </mjml>
        """);
    // MSO conditional present
    assertTrue(html.contains("<!--[if mso | IE]>"), "Should have MSO conditional");
    // All three sections rendered
    assertTrue(html.contains("Section One"), "Should render first section");
    assertTrue(html.contains("Section Two"), "Should render second section");
    assertTrue(html.contains("Section Three"), "Should render third section");
    // css-class with -outlook suffix for MSO
    assertTrue(html.contains("my-wrapper-outlook"), "Should have -outlook suffix on MSO table");
    // Background color applied
    assertTrue(html.contains("#eeeeee"), "Should have wrapper background color");
  }
}
