package dev.jcputney.javamjml.component;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.javamjml.MjmlRenderer;
import org.junit.jupiter.api.Test;

/**
 * Tests that every MJML component renders without errors and produces
 * expected structural output. Each test exercises a specific component.
 */
class AllComponentsRenderTest {

  private String render(String mjml) {
    String html = MjmlRenderer.render(mjml);
    assertNotNull(html);
    assertFalse(html.isEmpty());
    return html;
  }

  // --- Content Components ---

  @Test
  void rendersImage() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-image src="https://example.com/img.jpg" alt="Test" width="300px" />
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    assertTrue(html.contains("img.jpg"), "Should contain image src");
    assertTrue(html.contains("alt=\"Test\""), "Should contain alt text");
  }

  @Test
  void rendersImageWithHref() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-image src="https://example.com/img.jpg" href="https://example.com" target="_blank" />
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    assertTrue(html.contains("<a href=\"https://example.com\""), "Should wrap in anchor");
    assertTrue(html.contains("<img"), "Should contain img tag");
  }

  @Test
  void rendersButton() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-button href="https://example.com">Click Me</mj-button>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    assertTrue(html.contains("Click Me"), "Should contain button text");
    assertTrue(html.contains("https://example.com"), "Should contain href");
    assertTrue(html.contains("#414141"), "Should have default background color");
  }

  @Test
  void rendersButtonWithCustomStyle() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-button background-color="#ff0000" color="#ffffff" border-radius="10px" font-size="20px" href="#">Buy Now</mj-button>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    assertTrue(html.contains("#ff0000"), "Should contain custom bg color");
    assertTrue(html.contains("10px"), "Should contain border radius");
    assertTrue(html.contains("Buy Now"), "Should contain button text");
  }

  @Test
  void rendersDivider() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-divider border-color="#ff0000" border-width="2px" />
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    assertTrue(html.contains("#ff0000"), "Should contain border color");
    assertTrue(html.contains("2px"), "Should contain border width");
  }

  @Test
  void rendersSpacer() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-spacer height="50px" />
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    assertTrue(html.contains("50px"), "Should contain spacer height");
  }

  @Test
  void rendersTable() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-table>
                  <tr><th>Name</th><th>Value</th></tr>
                  <tr><td>A</td><td>1</td></tr>
                </mj-table>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    assertTrue(html.contains("<th>Name</th>"), "Should contain table header");
    assertTrue(html.contains("<td>A</td>"), "Should contain table data");
  }

  @Test
  void rendersRaw() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-raw><div class="custom">Raw HTML</div></mj-raw>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    assertTrue(html.contains("class=\"custom\""), "Should pass through raw HTML");
    assertTrue(html.contains("Raw HTML"), "Should contain raw content");
  }

  // --- Layout Components ---

  @Test
  void rendersMultipleColumns() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column width="33.33%"><mj-text>Col 1</mj-text></mj-column>
              <mj-column width="33.33%"><mj-text>Col 2</mj-text></mj-column>
              <mj-column width="33.34%"><mj-text>Col 3</mj-text></mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    assertTrue(html.contains("Col 1"));
    assertTrue(html.contains("Col 2"));
    assertTrue(html.contains("Col 3"));
  }

  @Test
  void rendersGroup() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-group>
                <mj-column><mj-text>Grouped A</mj-text></mj-column>
                <mj-column><mj-text>Grouped B</mj-text></mj-column>
              </mj-group>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    assertTrue(html.contains("Grouped A"));
    assertTrue(html.contains("Grouped B"));
    assertTrue(html.contains("mj-outlook-group-fix"));
  }

  @Test
  void rendersWrapper() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-wrapper background-color="#f0f0f0">
              <mj-section>
                <mj-column><mj-text>Inside wrapper</mj-text></mj-column>
              </mj-section>
            </mj-wrapper>
          </mj-body>
        </mjml>
        """);
    assertTrue(html.contains("Inside wrapper"));
    assertTrue(html.contains("#f0f0f0"), "Should contain wrapper background");
  }

  @Test
  void rendersFullWidthSection() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section full-width="full-width" background-color="#ff0000">
              <mj-column><mj-text>Full width</mj-text></mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    assertTrue(html.contains("Full width"));
    assertTrue(html.contains("width:100%"), "Should have full-width style");
  }

  // --- Interactive Components ---

  @Test
  void rendersHero() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-hero mode="fixed-height" background-height="469px" background-url="https://example.com/hero.jpg" background-color="#2f2f2f" padding="100px 0px">
              <mj-text color="#ffffff">Hero Title</mj-text>
              <mj-button href="https://example.com">Call to Action</mj-button>
            </mj-hero>
          </mj-body>
        </mjml>
        """);
    assertTrue(html.contains("Hero Title"));
    assertTrue(html.contains("hero.jpg"), "Should contain background URL");
  }

  @Test
  void rendersAccordion() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-accordion>
                  <mj-accordion-element>
                    <mj-accordion-title>Question 1</mj-accordion-title>
                    <mj-accordion-text>Answer 1</mj-accordion-text>
                  </mj-accordion-element>
                  <mj-accordion-element>
                    <mj-accordion-title>Question 2</mj-accordion-title>
                    <mj-accordion-text>Answer 2</mj-accordion-text>
                  </mj-accordion-element>
                </mj-accordion>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    assertTrue(html.contains("Question 1"));
    assertTrue(html.contains("Answer 1"));
    assertTrue(html.contains("Question 2"));
    assertTrue(html.contains("checkbox"), "Should use checkbox hack");
  }

  @Test
  void rendersNavbar() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-navbar>
                  <mj-navbar-link href="/home">Home</mj-navbar-link>
                  <mj-navbar-link href="/about">About</mj-navbar-link>
                  <mj-navbar-link href="/contact">Contact</mj-navbar-link>
                </mj-navbar>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    assertTrue(html.contains("/home"), "Should contain Home link");
    assertTrue(html.contains("About"));
    assertTrue(html.contains("Contact"));
  }

  @Test
  void rendersSocial() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-social>
                  <mj-social-element name="facebook" href="https://facebook.com/me">Facebook</mj-social-element>
                  <mj-social-element name="twitter" href="https://twitter.com/me">Twitter</mj-social-element>
                </mj-social>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    assertTrue(html.contains("facebook.com/me"), "Should contain Facebook href");
    assertTrue(html.contains("twitter.com/me"), "Should contain Twitter href");
    assertTrue(html.contains("Facebook"), "Should contain Facebook label");
  }

  @Test
  void rendersCarousel() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-carousel>
                  <mj-carousel-image src="https://example.com/1.jpg" />
                  <mj-carousel-image src="https://example.com/2.jpg" />
                </mj-carousel>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    assertTrue(html.contains("1.jpg"), "Should contain first image");
    assertTrue(html.contains("2.jpg"), "Should contain second image");
    assertTrue(html.contains("radio"), "Should use radio button state machine");
  }

  // --- Head Components ---

  @Test
  void rendersWithAttributeDefaults() {
    String html = render("""
        <mjml>
          <mj-head>
            <mj-attributes>
              <mj-all font-family="Georgia, serif" />
              <mj-text color="#333333" font-size="16px" />
            </mj-attributes>
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Styled text</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    assertTrue(html.contains("#333333"), "Should use mj-text default color");
    assertTrue(html.contains("16px"), "Should use mj-text default font-size");
  }

  @Test
  void rendersWithMjClass() {
    String html = render("""
        <mjml>
          <mj-head>
            <mj-attributes>
              <mj-class name="red" color="red" />
              <mj-class name="big" font-size="24px" />
            </mj-attributes>
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text mj-class="red big">Classed text</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    assertTrue(html.contains("Classed text"));
    // Class attributes should be resolved via cascade
  }

  @Test
  void rendersWithCustomBreakpoint() {
    String html = render("""
        <mjml>
          <mj-head>
            <mj-breakpoint width="320px" />
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column><mj-text>Test</mj-text></mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    assertTrue(html.contains("320px"), "Should use custom breakpoint");
  }

  @Test
  void rendersBodyWithCustomWidth() {
    String html = render("""
        <mjml>
          <mj-body width="500px">
            <mj-section>
              <mj-column><mj-text>Narrow</mj-text></mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    assertTrue(html.contains("500px"), "Should use custom body width");
  }

  // --- Edge Cases ---

  @Test
  void rendersEmptyBody() {
    String html = render("""
        <mjml>
          <mj-body>
          </mj-body>
        </mjml>
        """);
    assertTrue(html.contains("<!doctype html>"));
    assertTrue(html.contains("</html>"));
  }

  @Test
  void rendersBodyWithNoHead() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column><mj-text>No head</mj-text></mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    assertTrue(html.contains("No head"));
  }

  @Test
  void rendersMultipleSections() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section background-color="#ff0000">
              <mj-column><mj-text color="#ffffff">Section 1</mj-text></mj-column>
            </mj-section>
            <mj-section background-color="#00ff00">
              <mj-column><mj-text>Section 2</mj-text></mj-column>
            </mj-section>
            <mj-section background-color="#0000ff">
              <mj-column><mj-text color="#ffffff">Section 3</mj-text></mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    assertTrue(html.contains("Section 1"));
    assertTrue(html.contains("Section 2"));
    assertTrue(html.contains("Section 3"));
    assertTrue(html.contains("#ff0000"));
    assertTrue(html.contains("#00ff00"));
    assertTrue(html.contains("#0000ff"));
  }

  @Test
  void rendersTextWithHtmlContent() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>
                  <h1>Title</h1>
                  <p>Paragraph with <b>bold</b> and <em>italic</em> text.</p>
                  <a href="https://example.com">A link</a>
                </mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    assertTrue(html.contains("<h1>Title</h1>"), "Should preserve h1 tag");
    assertTrue(html.contains("<b>bold</b>"), "Should preserve bold tag");
    assertTrue(html.contains("<em>italic</em>"), "Should preserve em tag");
  }

  @Test
  void rendersWithInlineStyle() {
    String html = render("""
        <mjml>
          <mj-head>
            <mj-style inline="inline">
              .red { color: red; }
            </mj-style>
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column><mj-text>Content</mj-text></mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    // The inline style should be collected for CSS inlining (Phase 5)
    assertNotNull(html);
  }
}
