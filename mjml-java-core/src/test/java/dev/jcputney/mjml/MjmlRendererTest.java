package dev.jcputney.mjml;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MjmlRendererTest {

  @Test
  void rendersSimpleTextTemplate() {
    String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Hello World</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml).html();

    assertNotNull(html);
    assertFalse(html.isEmpty());
    assertTrue(html.contains("<!doctype html>"));
    assertTrue(html.contains("Hello World"));
    assertTrue(html.contains("</html>"));
  }

  @Test
  void rendersWithTitle() {
    String mjml = """
        <mjml>
          <mj-head>
            <mj-title>My Email</mj-title>
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

    MjmlRenderResult result = MjmlRenderer.render(mjml, MjmlConfiguration.defaults());

    assertEquals("My Email", result.title());
    assertTrue(result.html().contains("<title>My Email</title>"));
  }

  @Test
  void rendersPreviewText() {
    String mjml = """
        <mjml>
          <mj-head>
            <mj-preview>Preview text here</mj-preview>
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
    assertTrue(html.contains("Preview text here"));
    assertTrue(html.contains("display:none"));
  }

  @Test
  void rendersTwoColumns() {
    String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Left column</mj-text>
              </mj-column>
              <mj-column>
                <mj-text>Right column</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml).html();

    assertTrue(html.contains("Left column"));
    assertTrue(html.contains("Right column"));
    // Should have MSO conditional comments for columns
    assertTrue(html.contains("<!--[if mso | IE]>"));
  }

  @Test
  void rendersWithLanguage() {
    String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Hello</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    MjmlConfiguration config = MjmlConfiguration.builder()
        .language("en")
        .build();
    MjmlRenderResult result = MjmlRenderer.render(mjml, config);

    assertTrue(result.html().contains("lang=\"en\""));
  }

  @Test
  void containsRequiredHtmlStructure() {
    String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Test</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml).html();

    // DOCTYPE
    assertTrue(html.contains("<!doctype html>"));
    // Namespaces for Outlook
    assertTrue(html.contains("xmlns:v=\"urn:schemas-microsoft-com:vml\""));
    assertTrue(html.contains("xmlns:o=\"urn:schemas-microsoft-com:office:office\""));
    // Meta tags
    assertTrue(html.contains("Content-Type"));
    assertTrue(html.contains("viewport"));
    // MSO Office settings
    assertTrue(html.contains("OfficeDocumentSettings"));
    assertTrue(html.contains("AllowPNG"));
    // Base CSS resets
    assertTrue(html.contains("#outlook a"));
    assertTrue(html.contains("border-collapse: collapse"));
    // Body
    assertTrue(html.contains("<body"));
    assertTrue(html.contains("</body>"));
  }

  @Test
  void throwsOnNullInput() {
    assertThrows(MjmlException.class, () -> MjmlRenderer.render((String) null));
  }

  @Test
  void throwsOnEmptyInput() {
    assertThrows(MjmlException.class, () -> MjmlRenderer.render(""));
  }

  @Test
  void rendersWithHtmlEntities() {
    String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>&amp; &lt;test&gt;</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    // Should render without error
    assertTrue(html.contains("<!doctype html>"));
  }

  @Test
  void rendersWithFonts() {
    String mjml = """
        <mjml>
          <mj-head>
            <mj-font name="Roboto" href="https://fonts.googleapis.com/css?family=Roboto" />
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text font-family="Roboto, sans-serif">Content</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml).html();
    assertTrue(html.contains("fonts.googleapis.com"));
    assertTrue(html.contains("Roboto"));
  }

  @Test
  void rendersWithCustomStyle() {
    String mjml = """
        <mjml>
          <mj-head>
            <mj-style>.custom { color: red; }</mj-style>
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
    assertTrue(html.contains(".custom { color: red; }"));
  }
}
