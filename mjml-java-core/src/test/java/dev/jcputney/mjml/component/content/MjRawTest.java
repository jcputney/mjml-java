package dev.jcputney.mjml.component.content;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlRenderer;
import org.junit.jupiter.api.Test;

/** Tests for the mj-raw component rendering. */
class MjRawTest {

  private String render(String mjml) {
    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertFalse(html.isEmpty());
    return html;
  }

  @Test
  void rawHtmlPassthrough() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-raw>
                  <div class="custom-div">Custom content here</div>
                </mj-raw>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("custom-div"), "Should pass through custom div class");
    assertTrue(html.contains("Custom content here"), "Should pass through raw HTML content");
  }

  @Test
  void positionFileStartRendersBeforeDoctype() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-body>
            <mj-raw position="file-start">
              <!-- file start comment -->
            </mj-raw>
            <mj-section>
              <mj-column>
                <mj-text>Body</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    int commentPos = html.indexOf("file start comment");
    int doctypePos = html.indexOf("<!doctype");
    assertTrue(commentPos >= 0, "Should contain the file-start raw content");
    assertTrue(commentPos < doctypePos, "File-start content should appear before doctype");
  }

  @Test
  void rawMsoConditionalPassthrough() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-body>
            <mj-raw>
              <!--[if mso]>
              <table><tr><td>MSO only</td></tr></table>
              <![endif]-->
            </mj-raw>
            <mj-section>
              <mj-column>
                <mj-text>Content</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("MSO only"), "Should pass through MSO conditional content");
  }
}
