package dev.jcputney.mjml;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.parser.MjmlNode;
import dev.jcputney.mjml.render.VmlHelper;
import dev.jcputney.mjml.util.CssEscaper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Security edge case tests covering: - sanitizeHref allowlist coverage - sanitizeContent
 * consistency across components - Font URL CSS injection - VML escaping
 */
class SecurityEdgeCaseTest {

  // ── sanitizeHref allowlist: blocked schemes ─────────────────────────────

  /** Escape just enough for XML attribute values. */
  private static String xmlEscape(String s) {
    return s.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;");
  }

  // ── sanitizeHref allowlist: allowed schemes ─────────────────────────────

  private String renderImageWithHref(String href) {
    MjmlConfiguration config = MjmlConfiguration.builder().sanitizeOutput(true).build();

    String mjml =
        // language=MJML
        """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-image src="img.png" href="%s" />
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """
            .formatted(xmlEscape(href));

    return MjmlRenderer.render(mjml, config).html();
  }

  // ── sanitizeContent consistency (all 5 components) ──────────────────────

  private String renderButtonWithHref(String href) {
    MjmlConfiguration config = MjmlConfiguration.builder().sanitizeOutput(true).build();

    String mjml =
        // language=MJML
        """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-button href="%s">Click</mj-button>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """
            .formatted(xmlEscape(href));

    return MjmlRenderer.render(mjml, config).html();
  }

  // ── Font URL CSS injection ──────────────────────────────────────────────

  @Nested
  class SanitizeHrefBlocked {

    @Test
    void dataTextXmlBlocked() {
      String html = renderImageWithHref("data:text/xml,&lt;root/&gt;");
      assertFalse(html.contains("data:text/xml"), "data:text/xml should be blocked by allowlist");
    }

    @Test
    void dataApplicationXhtmlXmlBlocked() {
      String html = renderImageWithHref("data:application/xhtml+xml,test");
      assertFalse(
          html.contains("data:application/xhtml"), "data:application/xhtml+xml should be blocked");
    }

    @Test
    void javascriptSchemeBlocked() {
      String html = renderButtonWithHref("javascript:alert(document.cookie)");
      assertFalse(html.contains("javascript:"), "javascript: should be blocked");
    }

    @Test
    void vbscriptSchemeBlocked() {
      String html = renderButtonWithHref("vbscript:MsgBox(1)");
      assertFalse(html.contains("vbscript:"), "vbscript: should be blocked");
    }

    @Test
    void dataTextHtmlBlocked() {
      String html =
          renderImageWithHref("data:text/html;base64,PHNjcmlwdD5hbGVydCgxKTwvc2NyaXB0Pg==");
      assertFalse(html.contains("data:text/html"), "data:text/html should be blocked");
    }

    @Test
    void blobSchemeBlocked() {
      String html = renderImageWithHref("blob:http://evil.com/uuid");
      assertFalse(html.contains("blob:"), "blob: should be blocked");
    }

    @Test
    void dataImagePngBlocked() {
      String html = renderImageWithHref("data:image/png;base64,iVBORw0KGg==");
      assertFalse(html.contains("data:image/png"), "data:image/png should be blocked by allowlist");
    }
  }

  // ── VML escaping ────────────────────────────────────────────────────────

  @Nested
  class SanitizeHrefAllowed {

    @Test
    void httpAllowed() {
      String html = renderImageWithHref("http://example.com/page");
      assertTrue(html.contains("http://example.com/page"), "http: should be allowed");
    }

    @Test
    void httpsAllowed() {
      String html = renderImageWithHref("https://example.com/page");
      assertTrue(html.contains("https://example.com/page"), "https: should be allowed");
    }

    @Test
    void mailtoAllowed() {
      String html = renderButtonWithHref("mailto:user@example.com");
      assertTrue(html.contains("mailto:user@example.com"), "mailto: should be allowed");
    }

    @Test
    void telAllowed() {
      String html = renderButtonWithHref("tel:+15551234567");
      assertTrue(html.contains("tel:+15551234567"), "tel: should be allowed");
    }

    @Test
    void anchorHashAllowed() {
      String html = renderButtonWithHref("#section-id");
      assertTrue(html.contains("#section-id"), "# anchor refs should be allowed");
    }

    @Test
    void relativePathAllowed() {
      String html = renderButtonWithHref("/relative/path");
      assertTrue(html.contains("/relative/path"), "/relative paths should be allowed");
    }
  }

  // ── MjmlNode.getOuterHtml() escaping ───────────────────────────────────

  @Nested
  class SanitizeContentConsistency {

    private final MjmlConfiguration SANITIZER_CONFIG =
        MjmlConfiguration.builder()
            .contentSanitizer(html -> html.replace("MARKER", "SANITIZED"))
            .build();

    @Test
    void navbarLinkCallsSanitizeContent() {
      String mjml =
          // language=MJML
          """
          <mjml>
            <mj-body>
              <mj-section>
                <mj-column>
                  <mj-navbar>
                    <mj-navbar-link href="#">MARKER link</mj-navbar-link>
                  </mj-navbar>
                </mj-column>
              </mj-section>
            </mj-body>
          </mjml>
          """;

      String html = MjmlRenderer.render(mjml, SANITIZER_CONFIG).html();
      assertTrue(html.contains("SANITIZED"), "mj-navbar-link should apply ContentSanitizer");
      assertFalse(html.contains("MARKER"), "Original marker should be replaced");
    }

    @Test
    void accordionTitleCallsSanitizeContent() {
      String mjml =
          // language=MJML
          """
          <mjml>
            <mj-body>
              <mj-section>
                <mj-column>
                  <mj-accordion>
                    <mj-accordion-element>
                      <mj-accordion-title>MARKER title</mj-accordion-title>
                      <mj-accordion-text>Body text</mj-accordion-text>
                    </mj-accordion-element>
                  </mj-accordion>
                </mj-column>
              </mj-section>
            </mj-body>
          </mjml>
          """;

      String html = MjmlRenderer.render(mjml, SANITIZER_CONFIG).html();
      assertTrue(html.contains("SANITIZED"), "mj-accordion-title should apply ContentSanitizer");
    }

    @Test
    void accordionTextCallsSanitizeContent() {
      String mjml =
          // language=MJML
          """
          <mjml>
            <mj-body>
              <mj-section>
                <mj-column>
                  <mj-accordion>
                    <mj-accordion-element>
                      <mj-accordion-title>Title</mj-accordion-title>
                      <mj-accordion-text>MARKER body</mj-accordion-text>
                    </mj-accordion-element>
                  </mj-accordion>
                </mj-column>
              </mj-section>
            </mj-body>
          </mjml>
          """;

      String html = MjmlRenderer.render(mjml, SANITIZER_CONFIG).html();
      assertTrue(html.contains("SANITIZED"), "mj-accordion-text should apply ContentSanitizer");
    }

    @Test
    void socialElementCallsSanitizeContent() {
      String mjml =
          // language=MJML
          """
          <mjml>
            <mj-body>
              <mj-section>
                <mj-column>
                  <mj-social>
                    <mj-social-element name="facebook" href="https://facebook.com">MARKER share</mj-social-element>
                  </mj-social>
                </mj-column>
              </mj-section>
            </mj-body>
          </mjml>
          """;

      String html = MjmlRenderer.render(mjml, SANITIZER_CONFIG).html();
      assertTrue(html.contains("SANITIZED"), "mj-social-element should apply ContentSanitizer");
    }

    @Test
    void tableCallsSanitizeContent() {
      String mjml =
          // language=MJML
          """
          <mjml>
            <mj-body>
              <mj-section>
                <mj-column>
                  <mj-table><tr><td>MARKER cell</td></tr></mj-table>
                </mj-column>
              </mj-section>
            </mj-body>
          </mjml>
          """;

      String html = MjmlRenderer.render(mjml, SANITIZER_CONFIG).html();
      assertTrue(html.contains("SANITIZED"), "mj-table should apply ContentSanitizer");
    }
  }

  // ── Helper methods ─────────────────────────────────────────────────────

  @Nested
  class FontUrlInjection {

    @Test
    void fontUrlWithInjectionPayloadIsEscaped() {
      String maliciousUrl = "https://fonts.com/font');} body{background:url(//leak.com/";
      String escaped = CssEscaper.escapeCssUrl(maliciousUrl);

      // CssEscaper escapes parens and quotes to prevent url() breakout
      assertFalse(escaped.contains("')"), "Closing paren+quote should be escaped");
      assertTrue(escaped.contains("\\)"), "Parentheses should be backslash-escaped");
      assertTrue(escaped.contains("\\'"), "Single quotes should be backslash-escaped");
      // Verify the escaped string cannot break out of url("...")
      // The original injection: url("...font');} body{background:url(//leak.com/...")
      // After escaping:         url("...font\\'\\);} body{background:url\\(//leak.com/...")
      // The ') breakout is neutralized because ' and ) are escaped.
      assertFalse(
          escaped.contains("');"), "Unescaped single-quote+paren breakout should be prevented");
    }

    @Test
    void fontUrlWithDoubleQuoteIsEscaped() {
      String escaped = CssEscaper.escapeCssUrl("https://fonts.com/font\"); body{x:url(//evil");
      assertTrue(escaped.contains("\\\""), "Double quotes should be escaped with backslash");
      assertTrue(escaped.contains("\\("), "Parentheses should be escaped");
    }

    @Test
    void fontUrlWithBackslashIsEscaped() {
      String escaped = CssEscaper.escapeCssUrl("https://fonts.com/\\path");
      assertTrue(escaped.contains("\\\\"), "Backslash should be escaped");
    }
  }

  @Nested
  class VmlEscaping {

    @Test
    void vmlBgUrlWithQuoteIsEscaped() {
      String vml =
          VmlHelper.buildSectionVmlRect(
              "600px",
              "https://evil.com/bg.png\" onload=\"alert(1)",
              "#fff",
              "center center",
              "auto",
              "repeat");
      assertFalse(vml.contains("onload=\"alert"), "Quote in bgUrl should be escaped in VML");
      assertTrue(vml.contains("&quot;"), "Quote should appear as &quot;");
    }

    @Test
    void vmlBgUrlWithAngleBracketsIsEscaped() {
      String vml =
          VmlHelper.buildSectionVmlRect(
              "600px", "https://evil.com/<script>", "#fff", "center center", "auto", "repeat");
      assertFalse(vml.contains("<script>"), "Angle brackets in bgUrl should be escaped in VML");
      assertTrue(vml.contains("&lt;script&gt;"), "Angle brackets should appear as entities");
    }

    @Test
    void vmlBgColorWithSpecialCharsIsEscaped() {
      String vml =
          VmlHelper.buildSectionVmlRect(
              "600px",
              "https://example.com/bg.png",
              "#fff\" onload=\"alert(1)",
              "center center",
              "auto",
              "repeat");
      assertFalse(vml.contains("onload=\"alert"), "Quote in bgColor should be escaped in VML");
    }

    @Test
    void vmlBgSizeWithQuoteIsEscaped() {
      String vml =
          VmlHelper.buildSectionVmlRect(
              "600px",
              "https://example.com/bg.png",
              "#fff",
              "center center",
              "100px\" onload=\"alert(1)",
              "no-repeat");
      assertFalse(vml.contains("onload=\"alert"), "Quote in bgSize should be escaped in VML");
    }

    @Test
    void wrapperVmlBgSizeWithAngleBracketsEscaped() {
      String vml =
          VmlHelper.buildWrapperVmlRect(
              "600px",
              "https://example.com/bg.png",
              "#fff",
              "center center",
              "100px<img onerror=alert(1)>");
      assertFalse(vml.contains("<img"), "Angle brackets in wrapper bgSize should be escaped");
    }

    @Test
    void vmlAmpersandInUrlIsEscaped() {
      String vml =
          VmlHelper.buildSectionVmlRect(
              "600px",
              "https://example.com/bg.png?a=1&b=2",
              "#fff",
              "center center",
              "auto",
              "repeat");
      assertTrue(
          vml.contains("&amp;"), "Ampersand in URL should be HTML-escaped in VML attributes");
    }
  }

  @Nested
  class MjmlNodeEscaping {

    @Test
    void attributeWithQuoteAndAngleBrackets() {
      MjmlNode node = new MjmlNode("div");
      node.setAttribute("data-val", "a\"b<c>d&e");
      String html = node.getOuterHtml();

      assertTrue(html.contains("&quot;"), "Quote should be escaped");
      assertTrue(html.contains("&lt;"), "< should be escaped");
      assertTrue(html.contains("&gt;"), "> should be escaped");
      assertTrue(html.contains("&amp;"), "& should be escaped");
      assertFalse(html.contains("a\"b"), "Raw quote should not appear");
    }

    @Test
    void multipleAttributesAllEscaped() {
      MjmlNode node = new MjmlNode("span");
      node.setAttribute("a", "x\"y");
      node.setAttribute("b", "1<2");
      String html = node.getOuterHtml();

      assertTrue(html.contains("a=\"x&quot;y\""), "First attr should be escaped");
      assertTrue(html.contains("b=\"1&lt;2\""), "Second attr should be escaped");
    }
  }
}
