package dev.jcputney.mjml.component.head;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlRenderer;
import org.junit.jupiter.api.Test;

/** Tests for the mj-style component rendering. */
class MjStyleTest {

  private String render(String mjml) {
    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertFalse(html.isEmpty());
    return html;
  }

  @Test
  void regularStyleBlockInOutput() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-head>
            <mj-style>
              .custom-class { color: red; }
            </mj-style>
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

    assertTrue(html.contains(".custom-class"), "Should include the custom CSS class in the output");
    assertTrue(html.contains("color: red"), "Should include the CSS property value");
    assertTrue(html.contains("<style"), "Should include a style tag");
  }

  @Test
  void inlineStyleApplied() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-head>
            <mj-style inline="inline">
              .highlight { background-color: yellow; }
            </mj-style>
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text css-class="highlight">Highlighted</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(
        html.contains("background-color: yellow") || html.contains("background-color:yellow"),
        "Inline style should be applied directly to matching elements");
  }

  @Test
  void multipleStyleBlocks() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-head>
            <mj-style>
              .block-one { font-size: 14px; }
            </mj-style>
            <mj-style>
              .block-two { font-weight: bold; }
            </mj-style>
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

    assertTrue(html.contains(".block-one"), "Should include first style block");
    assertTrue(html.contains(".block-two"), "Should include second style block");
  }
}
