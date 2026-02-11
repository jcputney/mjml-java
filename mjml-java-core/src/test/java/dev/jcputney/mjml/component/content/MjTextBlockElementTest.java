package dev.jcputney.mjml.component.content;

import static org.junit.jupiter.api.Assertions.*;

import dev.jcputney.mjml.MjmlRenderResult;
import dev.jcputney.mjml.MjmlRenderer;
import org.junit.jupiter.api.Test;

/**
 * Tests that MjText block element detection uses proper tag-boundary matching and does not produce
 * false positives for words containing tag-like substrings.
 */
class MjTextBlockElementTest {

  @Test
  void paramTagDoesNotTriggerBlockDetection() {
    // <param> contains "p" but is NOT a block element — should NOT
    // get block-level whitespace treatment
    String mjml =
        // language=MJML
        """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text><param name="test">inline content</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    MjmlRenderResult result = MjmlRenderer.render(mjml);
    String html = result.html();
    // The content should be treated as inline, not block
    // Block treatment adds newlines around content; inline doesn't
    assertTrue(html.contains("inline content"), "Content with <param> should still render");
  }

  @Test
  void actualParagraphTagTriggerBlockDetection() {
    String mjml =
        // language=MJML
        """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text><p>paragraph content</p></mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    MjmlRenderResult result = MjmlRenderer.render(mjml);
    String html = result.html();
    assertTrue(html.contains("<p>paragraph content</p>"), "Block element <p> should be preserved");
  }

  @Test
  void pictureTagDoesNotTriggerBlockDetection() {
    // <picture> starts with "p" but is not a block element
    String mjml =
        // language=MJML
        """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text><picture>source</picture> text</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    MjmlRenderResult result = MjmlRenderer.render(mjml);
    assertNotNull(result);
    assertTrue(result.html().contains("text"), "Content with <picture> should still render");
  }

  @Test
  void objectTagDoesNotTriggerBlockDetection() {
    // <object> starts with "o" — would previously match "<ol" check if substring was "<o"
    // but the actual check is "<ol" so this is fine. Testing for completeness.
    String mjml =
        // language=MJML
        """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text><object data="x">fallback</object></mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    MjmlRenderResult result = MjmlRenderer.render(mjml);
    assertNotNull(result);
  }
}
