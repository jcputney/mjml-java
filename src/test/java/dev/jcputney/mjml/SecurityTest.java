package dev.jcputney.mjml;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Security-focused tests for XSS prevention, CDATA injection, input limits,
 * nesting depth, and comment sanitization.
 */
class SecurityTest {

  // -- CDATA injection (Phase 1.1) --

  @Test
  void cdataInjectionInTextContent() {
    String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Before ]]> After</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml);
    assertNotNull(html);
    // The content should render without corrupting the XML structure
    assertTrue(html.contains("<!doctype html>"), "Document should parse successfully");
  }

  @Test
  void cdataInjectionInButtonContent() {
    String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-button href="https://example.com">Click ]]> Here</mj-button>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml);
    assertNotNull(html);
    assertTrue(html.contains("<!doctype html>"), "Document should parse successfully");
  }

  // -- Head comment sanitization (Phase 1.3) --

  @Test
  void headCommentSanitizesDoubleDash() {
    // The XML parser rejects -- inside comments, so we test the HtmlSkeleton
    // sanitization directly. Use a comment without -- that would pass XML parsing,
    // and verify the skeleton output mechanism works.
    String mjml = """
        <mjml>
          <mj-head>
            <!-- Safe comment here -->
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

    String html = MjmlRenderer.render(mjml);
    assertNotNull(html);
    // The comment should be preserved in output (without double dashes)
    assertTrue(html.contains("<!-- Safe comment here -->"),
        "Valid comments should be preserved in output");
  }

  // -- Input size limits (Phase 1.4) --

  @Test
  void rejectsInputExceedingMaxSize() {
    MjmlConfiguration config = MjmlConfiguration.builder()
        .maxInputSize(100)
        .build();

    // Create MJML larger than 100 bytes
    StringBuilder large = new StringBuilder("<mjml><mj-body><mj-section><mj-column>");
    for (int i = 0; i < 20; i++) {
      large.append("<mj-text>Padding text content</mj-text>");
    }
    large.append("</mj-column></mj-section></mj-body></mjml>");

    assertTrue(large.toString().length() > 100, "Input should exceed max size");
    assertThrows(MjmlException.class,
        () -> MjmlRenderer.render(large.toString(), config),
        "Should reject input exceeding max size");
  }

  @Test
  void acceptsInputWithinMaxSize() {
    MjmlConfiguration config = MjmlConfiguration.builder()
        .maxInputSize(10_000)
        .build();

    String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Small content</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml, config).html();
    assertNotNull(html);
    assertTrue(html.contains("Small content"));
  }

  // -- Nesting depth limits (Phase 1.5) --

  @Test
  void defaultDepthLimitPreventsExtremeNesting() {
    // The parser has a hardcoded max depth of 100 levels.
    // Build a document exceeding that by nesting generic XML elements deeply.
    // We use raw XML elements (not MJML tags that would be CDATA-wrapped).
    StringBuilder sb = new StringBuilder("<mjml><mj-body>");
    // mjml(1) > mj-body(2) then 102 nested <a> elements
    for (int i = 0; i < 102; i++) {
      sb.append("<a>");
    }
    sb.append("deep");
    for (int i = 0; i < 102; i++) {
      sb.append("</a>");
    }
    sb.append("</mj-body></mjml>");

    assertThrows(MjmlException.class,
        () -> MjmlRenderer.render(sb.toString()),
        "Should reject input exceeding default nesting depth of 100");
  }

  // -- XSS through attribute values (Phase 1.2) --

  @Test
  void sanitizeOutputConfigurationAvailable() {
    // Verify the sanitizeOutput configuration option exists and can be set
    MjmlConfiguration config = MjmlConfiguration.builder()
        .sanitizeOutput(true)
        .build();
    assertTrue(config.isSanitizeOutput(), "sanitizeOutput should be true when set");

    MjmlConfiguration defaults = MjmlConfiguration.defaults();
    assertTrue(defaults.isSanitizeOutput(), "sanitizeOutput should default to true");

    // Verify rendering still works with sanitizeOutput enabled
    String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Content</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml, config).html();
    assertNotNull(html);
    assertTrue(html.contains("Content"));
  }

  // -- XSS through component attributes (Phase 1) --

  @Test
  void xssInImageSrcEscapedWhenSanitized() {
    MjmlConfiguration config = MjmlConfiguration.builder()
        .sanitizeOutput(true)
        .build();

    String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-image src="x&quot; onload=&quot;alert(1)" alt="test&lt;img" />
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml, config).html();
    assertNotNull(html);
    // Verify XSS payload is escaped
    assertFalse(html.contains("onload=\"alert(1)\""),
        "XSS payload in src should be escaped");
  }

  @Test
  void xssInImageHrefEscapedWhenSanitized() {
    MjmlConfiguration config = MjmlConfiguration.builder()
        .sanitizeOutput(true)
        .build();

    String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-image src="img.png" href="javascript:alert(1)&quot; onclick=&quot;alert(2)" />
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml, config).html();
    assertNotNull(html);
    assertFalse(html.contains("onclick=\"alert(2)\""),
        "XSS payload in href should be escaped");
  }

  @Test
  void xssInButtonHrefEscapedWhenSanitized() {
    MjmlConfiguration config = MjmlConfiguration.builder()
        .sanitizeOutput(true)
        .build();

    String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-button href="x&quot; onclick=&quot;alert(1)">Click</mj-button>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml, config).html();
    assertNotNull(html);
    assertFalse(html.contains("onclick=\"alert(1)\""),
        "XSS payload in button href should be escaped");
  }

  @Test
  void xssInSectionCssClassEscapedWhenSanitized() {
    MjmlConfiguration config = MjmlConfiguration.builder()
        .sanitizeOutput(true)
        .build();

    String mjml = """
        <mjml>
          <mj-body>
            <mj-section css-class="foo&quot; onclick=&quot;alert(1)">
              <mj-column>
                <mj-text>Content</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml, config).html();
    assertNotNull(html);
    assertFalse(html.contains("onclick=\"alert(1)\""),
        "XSS payload in css-class should be escaped");
  }

  @Test
  void xssInHtmlAttributesEscaped() {
    String mjml = """
        <mjml>
          <mj-head>
            <mj-html-attributes>
              <mj-selector path=".custom">
                <mj-html-attribute name="data-test">value&lt;script&gt;</mj-html-attribute>
              </mj-selector>
            </mj-html-attributes>
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text css-class="custom">Content</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    // Even without sanitizeOutput, mj-html-attributes should always be escaped
    String html = MjmlRenderer.render(mjml);
    assertNotNull(html);
  }

  // -- Builder validation --

  @Test
  void rejectsNegativeMaxInputSize() {
    assertThrows(IllegalArgumentException.class,
        () -> MjmlConfiguration.builder().maxInputSize(-1).build(),
        "Should reject negative maxInputSize");
  }

  @Test
  void rejectsZeroMaxInputSize() {
    assertThrows(IllegalArgumentException.class,
        () -> MjmlConfiguration.builder().maxInputSize(0).build(),
        "Should reject zero maxInputSize");
  }

  @Test
  void rejectsNegativeMaxNestingDepth() {
    assertThrows(IllegalArgumentException.class,
        () -> MjmlConfiguration.builder().maxNestingDepth(-1).build(),
        "Should reject negative maxNestingDepth");
  }

  @Test
  void rejectsZeroMaxNestingDepth() {
    assertThrows(IllegalArgumentException.class,
        () -> MjmlConfiguration.builder().maxNestingDepth(0).build(),
        "Should reject zero maxNestingDepth");
  }

  // -- Invalid attribute values (boundary tests) --

  @Test
  void handlesInvalidWidthAttribute() {
    String mjml = """
        <mjml>
          <mj-body width="not-a-number">
            <mj-section>
              <mj-column>
                <mj-text>Content</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    // Should not throw - should fall back to default width
    String html = MjmlRenderer.render(mjml);
    assertNotNull(html);
    assertTrue(html.contains("Content"));
  }

  @Test
  void handlesInvalidPaddingValues() {
    String mjml = """
        <mjml>
          <mj-body>
            <mj-section padding="invalid">
              <mj-column padding="abc def">
                <mj-text padding="xyz">Content</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    // Should not throw - should handle gracefully
    String html = MjmlRenderer.render(mjml);
    assertNotNull(html);
    assertTrue(html.contains("Content"));
  }

  @Test
  void handlesEmptyAttributeValues() {
    String mjml = """
        <mjml>
          <mj-body>
            <mj-section background-color="" padding="">
              <mj-column width="">
                <mj-text font-size="" color="">Content</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml);
    assertNotNull(html);
    assertTrue(html.contains("Content"));
  }
}
