package dev.jcputney.mjml.component.head;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlRenderer;
import org.junit.jupiter.api.Test;

/** Tests for the mj-preview component rendering. */
class MjPreviewTest {

  private String render(String mjml) {
    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertFalse(html.isEmpty());
    return html;
  }

  @Test
  void previewTextInOutput() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-head>
            <mj-preview>This is preview text for email clients</mj-preview>
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Content</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(
        html.contains("This is preview text for email clients"),
        "Should include the preview text in the HTML output");
  }

  @Test
  void previewTextHiddenDiv() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-head>
            <mj-preview>Hidden preview</mj-preview>
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Content</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("display:none"), "Preview text should be in a hidden container");
    assertTrue(html.contains("Hidden preview"), "Should contain the preview text");
  }

  @Test
  void previewTextAccessibleViaResult() {
    var result =
        MjmlRenderer.render(
            // language=MJML
            """
        <mjml>
          <mj-head>
            <mj-preview>Result preview text</mj-preview>
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Content</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertNotNull(result.previewText(), "previewText should be available on the render result");
    assertTrue(
        result.previewText().contains("Result preview text"),
        "previewText should match the mj-preview content");
  }
}
