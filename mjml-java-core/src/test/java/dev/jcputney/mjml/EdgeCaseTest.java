package dev.jcputney.mjml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Edge case tests to verify the renderer handles unusual and boundary conditions. */
class EdgeCaseTest {

  @Test
  void throwsOnNullInput() {
    assertThrows(MjmlException.class, () -> MjmlRenderer.render((String) null));
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
    assertThrows(
        MjmlException.class, () -> MjmlRenderer.render("<html><body>Not MJML</body></html>"));
  }

  @Test
  void throwsOnMalformedXml() {
    assertThrows(
        MjmlException.class,
        () -> MjmlRenderer.render("<mjml><mj-body><unclosed></mj-body></mjml>"));
  }

  @Test
  void handlesMinimalDocument() {
    String html = MjmlRenderer.render("<mjml><mj-body></mj-body></mjml>").html();
    assertNotNull(html);
    assertTrue(html.contains("<!doctype html>"));
    assertTrue(html.contains("</html>"));
  }

  @Test
  void handlesHeadOnly() {
    String html =
        MjmlRenderer.render(
                // language=MJML
                """
        <mjml>
          <mj-head>
            <mj-title>Test</mj-title>
          </mj-head>
          <mj-body></mj-body>
        </mjml>
        """)
            .html();
    assertNotNull(html);
    assertTrue(html.contains("<title>Test</title>"));
  }

  @Test
  void handlesHtmlEntities() {
    String html =
        MjmlRenderer.render(
                // language=MJML
                """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Price: &amp; Copyright &copy; 2024</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """)
            .html();
    assertNotNull(html);
    assertTrue(html.contains("&amp;") || html.contains("&"), "Should handle ampersand");
  }

  @Test
  void handlesDeepNesting() {
    // mj-wrapper > mj-section > mj-column > mj-text
    String html =
        MjmlRenderer.render(
                // language=MJML
                """
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
        """)
            .html();
    assertNotNull(html);
    assertTrue(html.contains("Deep content"));
  }

  @Test
  void handlesMultipleTextBlocks() {
    String html =
        MjmlRenderer.render(
                // language=MJML
                """
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
        """)
            .html();
    assertTrue(html.contains("First"));
    assertTrue(html.contains("Second"));
    assertTrue(html.contains("Third"));
  }

  @Test
  void handlesEmptySection() {
    String html =
        MjmlRenderer.render(
                // language=MJML
                """
        <mjml>
          <mj-body>
            <mj-section>
            </mj-section>
          </mj-body>
        </mjml>
        """)
            .html();
    assertNotNull(html);
    assertTrue(html.contains("<!doctype html>"));
  }

  @Test
  void handlesEmptyColumn() {
    String html =
        MjmlRenderer.render(
                // language=MJML
                """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """)
            .html();
    assertNotNull(html);
    assertTrue(html.contains("<!doctype html>"));
  }

  @Test
  void handlesCustomBodyWidth() {
    String html =
        MjmlRenderer.render(
                // language=MJML
                """
        <mjml>
          <mj-body width="400px">
            <mj-section>
              <mj-column>
                <mj-text>Narrow</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """)
            .html();
    assertTrue(html.contains("400px"), "Should use custom body width");
  }

  @Test
  void handlesUnicodeContent() {
    String html =
        MjmlRenderer.render(
                // language=MJML
                """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Hello World &#x1F600;</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """)
            .html();
    assertNotNull(html);
  }

  @Test
  void handlesMultipleFonts() {
    String html =
        MjmlRenderer.render(
                // language=MJML
                """
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
        """)
            .html();
    assertTrue(html.contains("Roboto"), "Should include Roboto font");
    assertTrue(html.contains("Lato"), "Should include Lato font");
  }

  @Test
  void handlesPreviewText() {
    String html =
        MjmlRenderer.render(
                // language=MJML
                """
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
        """)
            .html();
    assertTrue(html.contains("This is the preview text"), "Should contain preview text");
    assertTrue(html.contains("display:none"), "Preview should be hidden");
  }

  @Test
  void handlesLanguageAttribute() {
    MjmlConfiguration config = MjmlConfiguration.builder().language("fr").build();
    String html =
        MjmlRenderer.render(
                // language=MJML
                """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Bonjour</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """,
                config)
            .html();
    assertTrue(html.contains("lang=\"fr\""), "Should set lang attribute");
  }

  @Test
  void handlesUnknownComponentGracefully() {
    // Unknown components should be silently skipped during rendering
    String html =
        MjmlRenderer.render(
                // language=MJML
                """
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
        """)
            .html();
    assertTrue(html.contains("Before"));
    assertTrue(html.contains("After"));
  }

  @Test
  void handlesLargeTemplate() {
    // Generate a large template with many sections
    StringBuilder sb = new StringBuilder();
    sb.append("<mjml><mj-body>");
    for (int i = 0; i < 50; i++) {
      sb.append("<mj-section><mj-column><mj-text>Section ")
          .append(i)
          .append("</mj-text></mj-column></mj-section>");
    }
    sb.append("</mj-body></mjml>");

    String html = MjmlRenderer.render(sb.toString()).html();
    assertNotNull(html);
    assertTrue(html.contains("Section 0"));
    assertTrue(html.contains("Section 49"));
  }

  @Test
  void handlesDirectionConfig() {
    MjmlConfiguration config = MjmlConfiguration.builder().direction("rtl").build();
    String html =
        MjmlRenderer.render(
                // language=MJML
                """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>RTL content</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """,
                config)
            .html();
    assertTrue(html.contains("dir=\"rtl\""), "Should set dir attribute to rtl");
  }

  @Test
  void handlesImageUsemap() {
    String html =
        MjmlRenderer.render(
                // language=MJML
                """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-image src="test.png" usemap="#mymap" />
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """)
            .html();
    assertTrue(html.contains("usemap=\"#mymap\""), "Should render usemap attribute");
  }

  @Test
  void handlesMjRawFileStart() {
    String html =
        MjmlRenderer.render(
                // language=MJML
                """
        <mjml>
          <mj-body>
            <mj-raw position="file-start"><!-- Custom file start content --></mj-raw>
            <mj-section>
              <mj-column>
                <mj-text>Body</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """)
            .html();
    int fileStartPos = html.indexOf("<!-- Custom file start content -->");
    int doctypePos = html.indexOf("<!doctype html>");
    assertTrue(fileStartPos >= 0, "Should contain file-start content");
    assertTrue(doctypePos >= 0, "Should contain doctype");
    assertTrue(fileStartPos < doctypePos, "File-start content should appear before doctype");
  }

  @Test
  void rendersRenderResult() {
    MjmlRenderResult result =
        MjmlRenderer.render(
            // language=MJML
            """
        <mjml>
          <mj-head>
            <mj-title>My Email</mj-title>
            <mj-preview>Preview text here</mj-preview>
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Hello</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """,
            MjmlConfiguration.defaults());

    assertNotNull(result.html());
    assertTrue(result.title().contains("My Email"));
    assertEquals("Preview text here", result.previewText());
  }

  @Test
  void toBuilderRoundTrip() {
    MjmlConfiguration config =
        MjmlConfiguration.builder()
            .language("fr")
            .direction("rtl")
            .sanitizeOutput(false)
            .maxInputSize(500_000)
            .maxNestingDepth(50)
            .build();

    MjmlConfiguration copy = config.toBuilder().build();

    assertEquals("fr", copy.getLanguage());
    assertEquals(Direction.RTL, copy.getDirection());
    assertFalse(copy.isSanitizeOutput());
    assertEquals(500_000, copy.getMaxInputSize());
    assertEquals(50, copy.getMaxNestingDepth());
  }

  @Test
  void toBuilderAllowsOverride() {
    MjmlConfiguration config = MjmlConfiguration.builder().language("fr").build();

    MjmlConfiguration modified = config.toBuilder().language("de").build();

    assertEquals("de", modified.getLanguage());
  }

  @Test
  void configurationToString() {
    MjmlConfiguration config = MjmlConfiguration.defaults();
    String str = config.toString();
    assertTrue(str.contains("MjmlConfiguration"));
    assertTrue(str.contains("language='und'"));
    assertTrue(str.contains("sanitizeOutput=true"));
  }

  @Test
  void renderPathFile() throws Exception {
    java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("test", ".mjml");
    try {
      java.nio.file.Files.writeString(
          tempFile,
          // language=MJML
          """
          <mjml>
            <mj-body>
              <mj-section>
                <mj-column>
                  <mj-text>From file</mj-text>
                </mj-column>
              </mj-section>
            </mj-body>
          </mjml>
          """);
      String html = MjmlRenderer.render(tempFile).html();
      assertTrue(html.contains("From file"));
    } finally {
      java.nio.file.Files.deleteIfExists(tempFile);
    }
  }
}
