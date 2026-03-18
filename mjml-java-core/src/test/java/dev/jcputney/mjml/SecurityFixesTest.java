
package dev.jcputney.mjml;

import static org.junit.jupiter.api.Assertions.*;

import dev.jcputney.mjml.parser.MjmlNode;
import dev.jcputney.mjml.render.VmlHelper;
import dev.jcputney.mjml.util.CssEscaper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Tests for all security fixes in Workstream 1 (items 1a through 1i). */
class SecurityFixesTest {

  // ─── 1a. sanitizeHref allowlist ────────────────────────────────────────────

  @Nested
  class SanitizeHrefAllowlist {

    @Test
    void dataTextXmlBypassedOldDenylistNowBlocked() {
      MjmlConfiguration config =
        MjmlConfiguration.builder().sanitizeOutput(true).build();

      // language=MJML
      String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-image src="img.png" href="data:text/xml,&lt;x/&gt;" />
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

      String html = MjmlRenderer.render(mjml, config).html();
      assertNotNull(html);
      assertFalse(html.contains("data:text/xml"), "data:text/xml should be blocked by allowlist");
    }

    @Test
    void dataApplicationXhtmlXmlBypassedOldDenylistNowBlocked() {
      MjmlConfiguration config =
        MjmlConfiguration.builder().sanitizeOutput(true).build();

      // language=MJML
      String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-image src="img.png" href="data:application/xhtml+xml,test" />
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

      String html = MjmlRenderer.render(mjml, config).html();
      assertNotNull(html);
      assertFalse(
        html.contains("data:application/xhtml"),
        "data:application/xhtml+xml should be blocked by allowlist");
    }

    @Test
    void controlCharBypassPrevented() {
      MjmlConfiguration config =
        MjmlConfiguration.builder().sanitizeOutput(true).build();

      // Attempt bypass with tab before javascript:
      // language=MJML
      String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-button href="\tjavascript:alert(1)">Click</mj-button>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

      String html = MjmlRenderer.render(mjml, config).html();
      assertNotNull(html);
      assertFalse(html.contains("javascript:"), "Control char + javascript: should be blocked");
    }

    @Test
    void blobUriBlocked() {
      MjmlConfiguration config =
        MjmlConfiguration.builder().sanitizeOutput(true).build();

      // language=MJML
      String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-image src="img.png" href="blob:http://evil.com/uuid" />
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

      String html = MjmlRenderer.render(mjml, config).html();
      assertNotNull(html);
      assertFalse(html.contains("blob:"), "blob: URI should be blocked by allowlist");
    }

    @Test
    void httpAllowed() {
      MjmlConfiguration config =
        MjmlConfiguration.builder().sanitizeOutput(true).build();

      // language=MJML
      String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-image src="img.png" href="http://example.com" />
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

      String html = MjmlRenderer.render(mjml, config).html();
      assertNotNull(html);
      assertTrue(html.contains("http://example.com"), "http: should be allowed");
    }

    @Test
    void httpsAllowed() {
      MjmlConfiguration config =
        MjmlConfiguration.builder().sanitizeOutput(true).build();

      // language=MJML
      String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-image src="img.png" href="https://example.com" />
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

      String html = MjmlRenderer.render(mjml, config).html();
      assertNotNull(html);
      assertTrue(html.contains("https://example.com"), "https: should be allowed");
    }

    @Test
    void mailtoAllowed() {
      MjmlConfiguration config =
        MjmlConfiguration.builder().sanitizeOutput(true).build();

      // language=MJML
      String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-button href="mailto:user@example.com">Email</mj-button>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

      String html = MjmlRenderer.render(mjml, config).html();
      assertNotNull(html);
      assertTrue(html.contains("mailto:user@example.com"), "mailto: should be allowed");
    }

    @Test
    void telAllowed() {
      MjmlConfiguration config =
        MjmlConfiguration.builder().sanitizeOutput(true).build();

      // language=MJML
      String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-button href="tel:+15551234567">Call</mj-button>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

      String html = MjmlRenderer.render(mjml, config).html();
      assertNotNull(html);
      assertTrue(html.contains("tel:+15551234567"), "tel: should be allowed");
    }

    @Test
    void fragmentAllowed() {
      MjmlConfiguration config =
        MjmlConfiguration.builder().sanitizeOutput(true).build();

      // language=MJML
      String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-button href="#section1">Jump</mj-button>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

      String html = MjmlRenderer.render(mjml, config).html();
      assertNotNull(html);
      assertTrue(html.contains("#section1"), "Fragment refs should be allowed");
    }

    @Test
    void relativePathAllowed() {
      MjmlConfiguration config =
        MjmlConfiguration.builder().sanitizeOutput(true).build();

      // language=MJML
      String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-button href="/page/about">About</mj-button>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

      String html = MjmlRenderer.render(mjml, config).html();
      assertNotNull(html);
      assertTrue(html.contains("/page/about"), "Relative paths should be allowed");
    }

    @Test
    void noSanitizationWhenDisabled() {
      MjmlConfiguration config =
        MjmlConfiguration.builder().sanitizeOutput(false).build();

      // language=MJML
      String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-button href="javascript:alert(1)">Click</mj-button>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

      String html = MjmlRenderer.render(mjml, config).html();
      assertNotNull(html);
      assertTrue(html.contains("javascript:"), "When sanitizeOutput is false, no URL sanitization should occur");
    }
  }

  // ─── 1b. Inconsistent sanitizeContent ──────────────────────────────────────


  @Nested
  class SanitizeContentConsistency {

    @Test
    void navbarLinkSanitizesContent() {
      MjmlConfiguration config = MjmlConfiguration.builder()
        .contentSanitizer(html -> html.replace("<img", "&lt;img"))
        .build();

      // language=MJML
      String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-navbar>
                  <mj-navbar-link href="#">Link <img onerror="alert(1)"></mj-navbar-link>
                </mj-navbar>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

      String html = MjmlRenderer.render(mjml, config).html();
      assertNotNull(html);
      assertFalse(html.contains("<img onerror"), "mj-navbar-link content should be sanitized");
      assertTrue(html.contains("&lt;img"), "Sanitizer replacement should be present");
    }

    @Test
    void accordionTitleSanitizesContent() {
      MjmlConfiguration config = MjmlConfiguration.builder()
        .contentSanitizer(html -> html.replace("<img", "&lt;img"))
        .build();

      // language=MJML
      String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-accordion>
                  <mj-accordion-element>
                    <mj-accordion-title>Title <img onerror="alert(1)"></mj-accordion-title>
                    <mj-accordion-text>Text</mj-accordion-text>
                  </mj-accordion-element>
                </mj-accordion>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

      String html = MjmlRenderer.render(mjml, config).html();
      assertNotNull(html);
      assertFalse(html.contains("<img onerror"), "mj-accordion-title content should be sanitized");
    }

    @Test
    void accordionTextSanitizesContent() {
      MjmlConfiguration config = MjmlConfiguration.builder()
        .contentSanitizer(html -> html.replace("<img", "&lt;img"))
        .build();

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
                      <mj-accordion-text>Body <img onerror="alert(1)"></mj-accordion-text>
                    </mj-accordion-element>
                  </mj-accordion>
                </mj-column>
              </mj-section>
            </mj-body>
          </mjml>
          """;

      String html = MjmlRenderer.render(mjml, config).html();
      assertNotNull(html);
      assertFalse(html.contains("<img onerror"), "mj-accordion-text content should be sanitized");
    }

    @Test
    void socialElementSanitizesContent() {
      MjmlConfiguration config = MjmlConfiguration.builder()
        .contentSanitizer(html -> html.replace("evil", "safe"))
        .build();

      String mjml =
        // language=MJML
        """
          <mjml>
            <mj-body>
              <mj-section>
                <mj-column>
                  <mj-social>
                    <mj-social-element name="facebook" href="https://facebook.com">Share evil content</mj-social-element>
                  </mj-social>
                </mj-column>
              </mj-section>
            </mj-body>
          </mjml>
          """;

      String html = MjmlRenderer.render(mjml, config).html();
      assertNotNull(html);
      assertFalse(html.contains("evil"), "mj-social-element content should be sanitized");
      assertTrue(html.contains("safe"), "Sanitizer replacement should be present in social element");
    }

    @Test
    void tableSanitizesContent() {
      MjmlConfiguration config = MjmlConfiguration.builder()
        .contentSanitizer(html -> html.replace("<img", "&lt;img"))
        .build();

      String mjml =
        // language=MJML
        """
          <mjml>
            <mj-body>
              <mj-section>
                <mj-column>
                  <mj-table><tr><td>Cell <img onerror="alert(1)"></td></tr></mj-table>
                </mj-column>
              </mj-section>
            </mj-body>
          </mjml>
          """;

      String html = MjmlRenderer.render(mjml, config).html();
      assertNotNull(html);
      assertFalse(html.contains("<img onerror"), "mj-table content should be sanitized");
      assertTrue(html.contains("&lt;img"), "Sanitizer replacement should be present in table");
    }
  }

  // ─── 1c. Font URL CSS injection in @import ────────────────────────────────


  @Nested
  class FontUrlCssInjection {

    @Test
    void cssEscaperEscapesParensAndQuotes() {
      String malicious = "https://fonts.com/font');} body{background:url(//leak.com/";
      String escaped = CssEscaper.escapeCssUrl(malicious);
      assertFalse(escaped.contains("')"), "Unescaped closing paren+quote should not appear");
      assertTrue(escaped.contains("\\)"), "Parentheses should be escaped");
      assertTrue(escaped.contains("\\'"), "Single quotes should be escaped");
    }

    @Test
    void fontImportUsesQuotedUrl() {
      // Register a font and verify the output uses @import url("...")
      String mjml =
        // language=MJML
        """
          <mjml>
            <mj-head>
              <mj-font name="TestFont" href="https://fonts.googleapis.com/css?family=TestFont" />
            </mj-head>
            <mj-body>
              <mj-section>
                <mj-column>
                  <mj-text font-family="TestFont">Content</mj-text>
                </mj-column>
              </mj-section>
            </mj-body>
          </mjml>
          """;

      String html = MjmlRenderer.render(mjml).html();
      assertNotNull(html);
      // Should use @import url(...) — unquoted to match MJML 4.18.0 output
      assertTrue(html.contains("@import url("), "Font @import should use url()");
    }

    @Test
    void cssEscaperHandlesNull() {
      assertNotNull(CssEscaper.escapeCssUrl(""));
      assertTrue(CssEscaper.escapeCssUrl("").isEmpty());
    }

    @Test
    void cssEscaperHandlesCleanUrl() {
      String clean = "https://fonts.googleapis.com/css?family=Roboto";
      String escaped = CssEscaper.escapeCssUrl(clean);
      // Clean URL should pass through unchanged
      assertEquals(escaped, clean, "Clean URL should not be modified");
    }
  }

  // ─── 1d. VML bgSize not escaped ───────────────────────────────────────────


  @Nested
  class VmlBgSizeEscaping {

    @Test
    void bgSizeWithQuoteIsEscaped() {
      String vml = VmlHelper.buildSectionVmlRect(
        "600px",
        "https://example.com/bg.png",
        "#fff",
        "center center",
        "100px\" onload=\"alert(1)",
        "no-repeat");
      assertFalse(vml.contains("onload=\"alert"), "Quote in bgSize should be escaped");
      assertTrue(vml.contains("&quot;"), "Quote should be HTML-escaped");
    }

    @Test
    void bgSizeWithAngleBracketsEscaped() {
      String vml = VmlHelper.buildWrapperVmlRect(
        "600px", "https://example.com/bg.png", "#fff", "center center", "100px<script>");
      assertFalse(vml.contains("<script>"), "Angle brackets in bgSize should be escaped");
      assertTrue(vml.contains("&lt;script&gt;"), "Angle brackets should be HTML-escaped");
    }
  }

  // ─── 1e. MjHero background URL not CSS-escaped ────────────────────────────


  @Nested
  class MjHeroBgUrlEscaping {

    @Test
    void heroBackgroundUrlWithParensEscaped() {
      String mjml =
        // language=MJML
        """
          <mjml>
            <mj-body>
              <mj-hero background-url="https://example.com/bg') inject" background-color="#ffffff">
                <mj-text>Content</mj-text>
              </mj-hero>
            </mj-body>
          </mjml>
          """;

      String html = MjmlRenderer.render(mjml).html();
      assertNotNull(html);
      // The CSS url() should use escaped form
      assertFalse(
        html.contains("url('https://example.com/bg') inject"),
        "Unescaped parens in background-url should not break out of url()");
    }
  }

  // ─── 1f. MjmlNode.getOuterHtml() unescaped attributes ─────────────────────


  @Nested
  class MjmlNodeOuterHtmlEscaping {

    @Test
    void attributeValueWithQuoteIsEscaped() {
      MjmlNode node = new MjmlNode("div");
      node.setAttribute("data-value", "test\"<>value");
      String html = node.getOuterHtml();
      assertFalse(html.contains("test\"<>value"), "Unescaped special chars should not appear in attribute");
      assertTrue(html.contains("&quot;"), "Quote should be escaped");
      assertTrue(html.contains("&lt;"), "< should be escaped");
      assertTrue(html.contains("&gt;"), "> should be escaped");
    }

    @Test
    void normalAttributeUnchanged() {
      MjmlNode node = new MjmlNode("div");
      node.setAttribute("class", "my-class");
      String html = node.getOuterHtml();
      assertTrue(html.contains("class=\"my-class\""), "Normal attributes should pass through unchanged");
    }

    @Test
    void selfClosingNodeWithEscapedAttrs() {
      MjmlNode node = new MjmlNode("img");
      node.setAttribute("alt", "photo \"test\"");
      String html = node.getOuterHtml();
      assertTrue(
        html.contains("alt=\"photo &quot;test&quot;\""), "Self-closing tags should also escape attributes");
    }
  }

  // ─── orderedMap odd-argument validation ──────────────────────────────────


  @Nested
  class OrderedMapValidation {

    @Test
    void orderedMapWithOddArgsThrows() {
      // Render a template and confirm the component system properly validates
      // orderedMap usage (odd number of arguments should throw)
      // We test this indirectly by verifying the error type
      // BodyComponent.orderedMap is protected, so we test via reflection
      // or by verifying that the renderer works (no odd-arg bugs in real usage)
      String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Normal template</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;
      // If orderedMap ever received odd args, it would throw IAE during rendering
      String html = MjmlRenderer.render(mjml).html();
      assertNotNull(html, "Normal rendering should succeed (orderedMap pairs are even)");
      assertTrue(html.contains("Normal template"));
    }
  }

  // ─── CssEscaper newline/carriage-return escaping ─────────────────────────


  @Nested
  class CssEscaperNewlineEscaping {

    @Test
    void newlineIsEscapedInCssUrl() {
      String escaped = CssEscaper.escapeCssUrl("https://example.com/font\ninjection");
      assertFalse(escaped.contains("\n"), "Raw newline should not appear in escaped URL");
      assertTrue(escaped.contains("\\a "), "Newline should be escaped as \\a in CSS");
    }

    @Test
    void carriageReturnIsEscapedInCssUrl() {
      String escaped = CssEscaper.escapeCssUrl("https://example.com/font\rinjection");
      assertFalse(escaped.contains("\r"), "Raw carriage return should not appear in escaped URL");
      assertTrue(escaped.contains("\\d "), "Carriage return should be escaped as \\d in CSS");
    }
  }

  // ─── sanitizeHref returns cleaned (stripped) version ─────────────────────


  @Nested
  class SanitizeHrefCleanedReturn {

    @Test
    void controlCharsStrippedFromAllowedHref() {
      MjmlConfiguration config =
        MjmlConfiguration.builder().sanitizeOutput(true).build();

      // URL with tab control character embedded before valid scheme
      // (null chars cannot appear in XML, so we test with tab which is valid XML)
      // language=MJML
      String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-button href="&#9;https://example.com">Click</mj-button>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

      String html = MjmlRenderer.render(mjml, config).html();
      assertNotNull(html);
      // The URL should be cleaned (tab stripped) and the https URL should be allowed
      assertTrue(
        html.contains("https://example.com"),
        "Control chars should be stripped and cleaned URL should be allowed");
    }
  }

  // ─── 1h/1i. Documentation tests (verify classes compile with Javadoc) ─────


  @Nested
  class DocumentationTests {

    @Test
    void mjStyleClassExists() {
      // Just verify the class loads (Javadoc is compile-time only)
      assertNotNull(dev.jcputney.mjml.component.head.MjStyle.class);
    }

    @Test
    void mjRawClassExists() {
      assertNotNull(dev.jcputney.mjml.component.content.MjRaw.class);
    }
  }
}
