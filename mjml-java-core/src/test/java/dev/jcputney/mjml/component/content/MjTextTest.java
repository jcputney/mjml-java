package dev.jcputney.mjml.component.content;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlRenderer;
import org.junit.jupiter.api.Test;

/** Tests for the mj-text component rendering. */
class MjTextTest {

  private String render(String mjml) {
    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertFalse(html.isEmpty());
    return html;
  }

  @Test
  void plainTextRenders() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Hello World</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("Hello World"), "Should contain the plain text content");
    assertTrue(html.contains("<div style=\""), "Should wrap text content in a styled div");
  }

  @Test
  void blockElementDetection() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>
                  <p>First paragraph</p>
                  <p>Second paragraph</p>
                </mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("<p>First paragraph</p>"), "Should preserve first p tag");
    assertTrue(html.contains("<p>Second paragraph</p>"), "Should preserve second p tag");
  }

  @Test
  void inlineHtmlPreservesStructure() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Click <a href="https://example.com">here</a> for more</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(
        html.contains("<a href=\"https://example.com\">here</a>"),
        "Should preserve inline anchor tag structure");
    assertTrue(html.contains("Click"), "Should preserve text before inline element");
    assertTrue(html.contains("for more"), "Should preserve text after inline element");
  }

  @Test
  void emptyContentRenders() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text></mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    // Should render without errors and produce valid HTML
    assertTrue(html.contains("<!doctype html>"), "Should produce valid HTML even with empty text");
    assertTrue(html.contains("<div style=\""), "Should still render the wrapping div");
  }
}
