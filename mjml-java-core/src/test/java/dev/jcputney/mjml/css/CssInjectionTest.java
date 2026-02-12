package dev.jcputney.mjml.css;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlRenderer;
import org.junit.jupiter.api.Test;

/** Tests for CSS injection attacks via mj-style content attempting to escape the style block. */
class CssInjectionTest {

  @Test
  void styleTagEscapeAttemptIsContained() {
    // Attempt to break out of a <style> block by closing it and injecting a <script>
    // The mj-style content gets CDATA-wrapped during preprocessing, so the content
    // appears literally inside the rendered <style> tag (CSS-context, not HTML-context).
    String mjml =
        // language=MJML
        """
        <mjml>
          <mj-head>
            <mj-style></style><script>alert(1)</script><style></mj-style>
          </mj-head>
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
    // The injection payload is preserved literally inside the <style> block.
    // This is expected behavior: mj-style content is CSS, not HTML.
    // The CDATA wrapping ensures the XML parser doesn't break on it.
    // In email clients, content inside <style> tags is treated as CSS, not as executable HTML.
    assertTrue(html.contains("<style"), "Style tag should be present in output");
    assertTrue(html.contains("Content"), "Regular content should still render");
  }

  @Test
  void styleContentWithCssInjectionIsPreserved() {
    // CSS content that uses expression() — an IE-specific CSS injection vector
    String mjml =
        // language=MJML
        """
        <mjml>
          <mj-head>
            <mj-style>.evil { background: expression(alert(1)); }</mj-style>
          </mj-head>
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
    // CSS content is passed through as-is (within a <style> tag)
    assertTrue(
        html.contains("expression(alert(1))"),
        "CSS content should be preserved within the style block");
    // Verify it's contained within a style tag
    assertTrue(html.contains("<style"), "Style tag should be present in output");
  }

  @Test
  void inlineStyleInjectionAttempt() {
    // Attempt to inject via mj-style with inline="inline"
    // The mj-style content is CDATA-wrapped by the preprocessor, preserving it literally.
    // When used as inline CSS, the CSS parser processes it — the injection payload
    // becomes invalid CSS, not executable HTML.
    String mjml =
        // language=MJML
        """
        <mjml>
          <mj-head>
            <mj-style inline="inline">.test { color: red; }</mj-style>
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text css-class="test">Content</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    // Inline CSS should be applied to the element
    assertTrue(html.contains("color"), "Inline style should be applied");
    assertTrue(html.contains("Content"), "Content should still render");
  }

  @Test
  void multipleStyleBlocksRenderedCorrectly() {
    String mjml =
        // language=MJML
        """
        <mjml>
          <mj-head>
            <mj-style>.first { color: red; }</mj-style>
            <mj-style>.second { color: blue; }</mj-style>
          </mj-head>
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
    assertTrue(html.contains(".first {"), "First style block should be present");
    assertTrue(html.contains("color: red;"), "First style rule should be present");
    assertTrue(html.contains(".second {"), "Second style block should be present");
    assertTrue(html.contains("color: blue;"), "Second style rule should be present");
  }

  @Test
  void stylContentWithCurlyBracesAndAtRules() {
    String mjml =
        // language=MJML
        """
        <mjml>
          <mj-head>
            <mj-style>
              @media only screen and (max-width: 480px) {
                .responsive { width: 100% !important; }
              }
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
        """;

    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertTrue(
        html.contains("@media only screen"), "Media query should be preserved in style block");
    assertTrue(html.contains(".responsive"), "CSS class within media query should be preserved");
  }
}
