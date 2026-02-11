package dev.jcputney.mjml.component.head;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlRenderer;
import org.junit.jupiter.api.Test;

/** Tests for the mj-title component rendering. */
class MjTitleTest {

  private String render(String mjml) {
    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertFalse(html.isEmpty());
    return html;
  }

  @Test
  void titleExtractedToHtmlHead() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-head>
            <mj-title>My Email Title</mj-title>
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
        html.contains("<title>My Email Title</title>"),
        "Should include the title in the HTML head");
  }

  @Test
  void emptyTitleStillRendersTag() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-head>
            <mj-title></mj-title>
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

    assertTrue(html.contains("<title>"), "Should still include a title tag even when empty");
  }

  @Test
  void titleWithSpecialCharacters() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-head>
            <mj-title>Sale: 50% Off &amp; More!</mj-title>
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

    assertTrue(html.contains("<title>"), "Should render title tag with special characters");
    assertTrue(html.contains("50%"), "Should preserve percentage in title");
  }
}
