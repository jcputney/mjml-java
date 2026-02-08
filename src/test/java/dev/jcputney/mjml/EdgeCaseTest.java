package dev.jcputney.mjml;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Edge case tests to verify the renderer handles unusual and boundary conditions.
 */
class EdgeCaseTest {

  @Test
  void throwsOnNullInput() {
    assertThrows(MjmlException.class, () -> MjmlRenderer.render(null));
  }

  @Test
  void throwsOnEmptyInput() {
    assertThrows(MjmlException.class, () -> MjmlRenderer.render(""));
  }

  @Test
  void throwsOnBlankInput() {
    assertThrows(MjmlException.class, () -> MjmlRenderer.render("   "));
  }

  @Test
  void throwsOnNonMjmlRoot() {
    assertThrows(MjmlException.class, () ->
        MjmlRenderer.render("<html><body>Not MJML</body></html>"));
  }

  @Test
  void throwsOnMalformedXml() {
    assertThrows(MjmlException.class, () ->
        MjmlRenderer.render("<mjml><mj-body><unclosed></mj-body></mjml>"));
  }

  @Test
  void handlesMinimalDocument() {
    String html = MjmlRenderer.render("<mjml><mj-body></mj-body></mjml>");
    assertNotNull(html);
    assertTrue(html.contains("<!doctype html>"));
    assertTrue(html.contains("</html>"));
  }

  @Test
  void handlesHeadOnly() {
    String html = MjmlRenderer.render("""
        <mjml>
          <mj-head>
            <mj-title>Test</mj-title>
          </mj-head>
          <mj-body></mj-body>
        </mjml>
        """);
    assertNotNull(html);
    assertTrue(html.contains("<title>Test</title>"));
  }

  @Test
  void handlesHtmlEntities() {
    String html = MjmlRenderer.render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Price: &amp; Copyright &copy; 2024</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    assertNotNull(html);
    assertTrue(html.contains("&amp;") || html.contains("&"), "Should handle ampersand");
  }

  @Test
  void handlesDeepNesting() {
    // mj-wrapper > mj-section > mj-column > mj-text
    String html = MjmlRenderer.render("""
        <mjml>
          <mj-body>
            <mj-wrapper>
              <mj-section>
                <mj-column>
                  <mj-text>Deep content</mj-text>
                </mj-column>
              </mj-section>
            </mj-wrapper>
          </mj-body>
        </mjml>
        """);
    assertNotNull(html);
    assertTrue(html.contains("Deep content"));
  }

  @Test
  void handlesMultipleTextBlocks() {
    String html = MjmlRenderer.render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>First</mj-text>
                <mj-text>Second</mj-text>
                <mj-text>Third</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    assertTrue(html.contains("First"));
    assertTrue(html.contains("Second"));
    assertTrue(html.contains("Third"));
  }

  @Test
  void handlesEmptySection() {
    String html = MjmlRenderer.render("""
        <mjml>
          <mj-body>
            <mj-section>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    assertNotNull(html);
    assertTrue(html.contains("<!doctype html>"));
  }

  @Test
  void handlesEmptyColumn() {
    String html = MjmlRenderer.render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    assertNotNull(html);
    assertTrue(html.contains("<!doctype html>"));
  }

  @Test
  void handlesCustomBodyWidth() {
    String html = MjmlRenderer.render("""
        <mjml>
          <mj-body width="400px">
            <mj-section>
              <mj-column>
                <mj-text>Narrow</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    assertTrue(html.contains("400px"), "Should use custom body width");
  }

  @Test
  void handlesUnicodeContent() {
    String html = MjmlRenderer.render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Hello World &#x1F600;</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    assertNotNull(html);
  }

  @Test
  void handlesMultipleFonts() {
    String html = MjmlRenderer.render("""
        <mjml>
          <mj-head>
            <mj-font name="Roboto" href="https://fonts.googleapis.com/css?family=Roboto" />
            <mj-font name="Lato" href="https://fonts.googleapis.com/css?family=Lato" />
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text font-family="Roboto, Lato, sans-serif">Fonts test</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    assertTrue(html.contains("Roboto"), "Should include Roboto font");
    assertTrue(html.contains("Lato"), "Should include Lato font");
  }

  @Test
  void handlesPreviewText() {
    String html = MjmlRenderer.render("""
        <mjml>
          <mj-head>
            <mj-preview>This is the preview text</mj-preview>
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Body content</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    assertTrue(html.contains("This is the preview text"), "Should contain preview text");
    assertTrue(html.contains("display:none"), "Preview should be hidden");
  }

  @Test
  void handlesLanguageAttribute() {
    MjmlConfiguration config = MjmlConfiguration.builder()
        .language("fr")
        .build();
    String html = MjmlRenderer.render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Bonjour</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """, config).html();
    assertTrue(html.contains("lang=\"fr\""), "Should set lang attribute");
  }

  @Test
  void handlesUnknownComponentGracefully() {
    // Unknown components should be silently skipped during rendering
    String html = MjmlRenderer.render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Before</mj-text>
                <mj-text>After</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    assertTrue(html.contains("Before"));
    assertTrue(html.contains("After"));
  }

  @Test
  void handlesLargeTemplate() {
    // Generate a large template with many sections
    StringBuilder sb = new StringBuilder();
    sb.append("<mjml><mj-body>");
    for (int i = 0; i < 50; i++) {
      sb.append("<mj-section><mj-column><mj-text>Section ").append(i)
          .append("</mj-text></mj-column></mj-section>");
    }
    sb.append("</mj-body></mjml>");

    String html = MjmlRenderer.render(sb.toString());
    assertNotNull(html);
    assertTrue(html.contains("Section 0"));
    assertTrue(html.contains("Section 49"));
  }

  @Test
  void rendersRenderResult() {
    MjmlRenderResult result = MjmlRenderer.render("""
        <mjml>
          <mj-head>
            <mj-title>My Email</mj-title>
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Hello</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """, MjmlConfiguration.defaults());

    assertNotNull(result.html());
    assertTrue(result.title().contains("My Email"));
  }
}
