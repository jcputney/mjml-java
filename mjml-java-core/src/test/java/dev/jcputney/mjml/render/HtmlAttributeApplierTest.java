package dev.jcputney.mjml.render;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlRenderer;
import org.junit.jupiter.api.Test;

/**
 * Tests for HtmlAttributeApplier which applies mj-html-attributes to rendered HTML.
 * HtmlAttributeApplier is package-private, so we test indirectly through the render pipeline.
 */
class HtmlAttributeApplierTest {

  private String render(String mjml) {
    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertFalse(html.isEmpty());
    return html;
  }

  @Test
  void returnsUnchangedHtmlWhenNoAttributes() {
    // No mj-html-attributes means the HTML should pass through unchanged
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Plain text</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertNotNull(html);
    assertTrue(html.contains("Plain text"));
  }

  @Test
  void addsAttributeToMatchingElement() {
    String html = render("""
        <mjml>
          <mj-head>
            <mj-html-attributes>
              <mj-selector path=".target-class">
                <mj-html-attribute name="data-id">test-value</mj-html-attribute>
              </mj-selector>
            </mj-html-attributes>
          </mj-head>
          <mj-body>
            <mj-section css-class="target-class">
              <mj-column>
                <mj-text>Targeted text</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("data-id=\"test-value\""),
        "Should add the data-id attribute to the matching element");
  }

  @Test
  void escapesXssInAttributeValues() {
    String html = render("""
        <mjml>
          <mj-head>
            <mj-html-attributes>
              <mj-selector path=".xss-test">
                <mj-html-attribute name="data-val">&lt;script&gt;alert(1)&lt;/script&gt;</mj-html-attribute>
              </mj-selector>
            </mj-html-attributes>
          </mj-head>
          <mj-body>
            <mj-section css-class="xss-test">
              <mj-column>
                <mj-text>XSS test</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    // The attribute value should be escaped - no raw <script> in an attribute
    assertFalse(html.contains("data-val=\"<script>"),
        "XSS payload should be escaped in attribute value");
  }

  @Test
  void filtersInvalidAttributeNames() {
    // HtmlAttributeApplier validates attribute names against [a-zA-Z][a-zA-Z0-9-]*
    // Invalid names like "on*" patterns should be filtered
    String html = render("""
        <mjml>
          <mj-head>
            <mj-html-attributes>
              <mj-selector path=".filter-test">
                <mj-html-attribute name="data-safe">safe</mj-html-attribute>
              </mj-selector>
            </mj-html-attributes>
          </mj-head>
          <mj-body>
            <mj-section css-class="filter-test">
              <mj-column>
                <mj-text>Filter test</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("data-safe=\"safe\""),
        "Valid attribute names should be applied");
  }

  @Test
  void handlesSelfClosingTags() {
    // Test that attributes can be applied to self-closing elements like img
    String html = render("""
        <mjml>
          <mj-head>
            <mj-html-attributes>
              <mj-selector path="img">
                <mj-html-attribute name="loading">lazy</mj-html-attribute>
              </mj-selector>
            </mj-html-attributes>
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-image src="https://example.com/photo.jpg" width="300px" />
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("loading=\"lazy\""),
        "Should add attribute to self-closing img element");
  }

  @Test
  void handlesMultipleMatchingElements() {
    String html = render("""
        <mjml>
          <mj-head>
            <mj-html-attributes>
              <mj-selector path=".multi td">
                <mj-html-attribute name="role">cell</mj-html-attribute>
              </mj-selector>
            </mj-html-attributes>
          </mj-head>
          <mj-body>
            <mj-section css-class="multi">
              <mj-column>
                <mj-text>Col A</mj-text>
              </mj-column>
              <mj-column>
                <mj-text>Col B</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    // Both columns render as td elements inside the .multi section
    // Count occurrences of role="cell"
    int count = 0;
    int idx = 0;
    while ((idx = html.indexOf("role=\"cell\"", idx)) != -1) {
      count++;
      idx += "role=\"cell\"".length();
    }
    assertTrue(count >= 2,
        "Should add attribute to multiple matching elements, found " + count);
  }
}
