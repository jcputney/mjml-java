package dev.jcputney.mjml.render;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.Direction;
import dev.jcputney.mjml.MjmlConfiguration;
import dev.jcputney.mjml.MjmlRenderer;
import org.junit.jupiter.api.Test;

/** Tests for the HtmlSkeleton document assembly. */
class HtmlSkeletonTest {

  private static final String MINIMAL_MJML =
      // language=MJML
      """
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

  private String render(String mjml) {
    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertFalse(html.isEmpty());
    return html;
  }

  private String render(String mjml, MjmlConfiguration config) {
    String html = MjmlRenderer.render(mjml, config).html();
    assertNotNull(html);
    assertFalse(html.isEmpty());
    return html;
  }

  @Test
  void outputStartsWithDoctype() {
    String html = render(MINIMAL_MJML);
    assertTrue(html.startsWith("<!doctype html>"), "Output should start with <!doctype html>");
  }

  @Test
  void defaultLanguageIsUnd() {
    String html = render(MINIMAL_MJML);
    assertTrue(html.contains("lang=\"und\""), "Default language should be 'und'");
  }

  @Test
  void configuredLanguageAppearsInOutput() {
    MjmlConfiguration config = MjmlConfiguration.builder().language("fr").build();
    String html = render(MINIMAL_MJML, config);
    assertTrue(html.contains("lang=\"fr\""), "Configured language 'fr' should appear in html tag");
  }

  @Test
  void configuredLanguageIsEscapedInOutput() {
    MjmlConfiguration config =
        MjmlConfiguration.builder().language("en\" onload=\"alert(1)").build();
    String html = render(MINIMAL_MJML, config);
    assertTrue(
        html.contains("lang=\"en&quot; onload=&quot;alert(1)\""),
        "Language value should be escaped before insertion into html tag");
    assertFalse(
        html.contains("lang=\"en\" onload=\"alert(1)\""),
        "Unescaped language value must not be emitted");
  }

  @Test
  void defaultDirectionIsAuto() {
    String html = render(MINIMAL_MJML);
    assertTrue(html.contains("dir=\"auto\""), "Default direction should be 'auto'");
  }

  @Test
  void configuredDirectionRtlAppearsInOutput() {
    MjmlConfiguration config = MjmlConfiguration.builder().direction(Direction.RTL).build();
    String html = render(MINIMAL_MJML, config);
    assertTrue(
        html.contains("dir=\"rtl\""), "Configured direction 'rtl' should appear in html tag");
  }

  @Test
  void titleAppearsWhenSetViaMjTitle() {
    String mjml =
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
        """;
    String html = render(mjml);
    assertTrue(
        html.contains("<title>My Email Title</title>"),
        "Title from mj-title should appear in output");
  }

  @Test
  void htmlInTitleGetsEscaped() {
    String mjml =
        // language=MJML
        """
        <mjml>
          <mj-head>
            <mj-title><script>alert('xss')</script></mj-title>
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
    String html = render(mjml);
    assertFalse(html.contains("<script>"), "HTML in title should be escaped, not rendered raw");
    assertTrue(html.contains("&lt;script&gt;"), "Script tag should be HTML-escaped in title");
  }

  @Test
  void previewTextAppearsWhenSetViaMjPreview() {
    String mjml =
        // language=MJML
        """
        <mjml>
          <mj-head>
            <mj-preview>Check out our latest deals!</mj-preview>
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
    String html = render(mjml);
    assertTrue(
        html.contains("Check out our latest deals!"), "Preview text should appear in hidden div");
    assertTrue(html.contains("display:none"), "Preview text div should be hidden");
  }

  @Test
  void htmlInPreviewTextGetsEscaped() {
    // Use XML-safe entities in the MJML source since mj-preview is parsed as XML.
    // XML parser converts &amp; -> & and &lt; -> <, then HtmlSkeleton.escapeHtml()
    // re-escapes them for safe HTML output.
    String mjml =
        // language=MJML
        """
        <mjml>
          <mj-head>
            <mj-preview>A &amp; B &lt;script&gt;</mj-preview>
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
    String html = render(mjml);
    // escapeHtml should have re-escaped the & and < from the parsed text
    assertTrue(
        html.contains("A &amp; B &lt;script&gt;"),
        "Preview text should have special chars escaped");
    assertFalse(
        html.contains("display:none") && html.contains("<script>"),
        "Raw HTML should not appear unescaped in preview text");
  }

  @Test
  void fontImportsAppearForRegisteredFonts() {
    String mjml =
        // language=MJML
        """
        <mjml>
          <mj-head>
            <mj-font name="Roboto" href="https://fonts.googleapis.com/css?family=Roboto" />
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
    String html = render(mjml);
    assertTrue(html.contains("fonts.googleapis.com"), "Font URL should appear in output");
    assertTrue(html.contains("@import url("), "Font should have @import statement");
    assertTrue(
        html.contains("rel=\"stylesheet\""), "Font should have link tag with rel stylesheet");
  }

  @Test
  void mediaQueriesIncludeBreakpoint() {
    String html = render(MINIMAL_MJML);
    // A section with a column generates media queries for responsive widths
    assertTrue(
        html.contains("@media only screen and (min-width:"),
        "Should contain media query with breakpoint");
  }

  @Test
  void fileStartContentFromMjRawAppearsBeforeDoctype() {
    String mjml =
        // language=MJML
        """
        <mjml>
          <mj-body>
            <mj-raw position="file-start"><!-- custom file-start content --></mj-raw>
            <mj-section>
              <mj-column>
                <mj-text>Content</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;
    String html = render(mjml);
    int fileStartIdx = html.indexOf("<!-- custom file-start content -->");
    int doctypeIdx = html.indexOf("<!doctype html>");
    assertTrue(fileStartIdx >= 0, "File-start content should be present");
    assertTrue(doctypeIdx >= 0, "Doctype should be present");
    assertTrue(fileStartIdx < doctypeIdx, "File-start content should appear before doctype");
  }
}
