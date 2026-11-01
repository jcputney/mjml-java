
package dev.jcputney.mjml.component.body;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlRenderer;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for MjSection's 4 render variants: normal, normal+bg-image, full-width,
 * full-width+bg-image. These verify structural patterns that must be preserved during refactoring.
 */
class MjSectionRenderTest {

  private String render(String mjml) {
    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertFalse(html.isEmpty());
    return html;
  }

  @Test
  void renderNormalSimple() {
    String html = render(
      // language=MJML
      """
        <mjml>
          <mj-body>
            <mj-section background-color="#ffffff" padding="20px 0">
              <mj-column><mj-text>Normal section</mj-text></mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    // MSO table wrapping present
    assertTrue(html.contains("<!--[if mso | IE]>"), "Should have MSO conditional");
    assertTrue(html.contains("width:600px"), "Should have container width");
    // Background color on div
    assertTrue(html.contains("background:#ffffff"), "Should have background color");
    // No VML rect (no background image)
    assertFalse(html.contains("v:rect"), "Should NOT have VML rect without bg image");
  }

  @Test
  void renderNormalWithBgImage() {
    String html = render(
      // language=MJML
      """
        <mjml>
          <mj-body>
            <mj-section background-url="https://example.com/bg.jpg" background-color="#ff0000">
              <mj-column><mj-text>With bg image</mj-text></mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    // VML rect for Outlook
    assertTrue(html.contains("v:rect"), "Should have VML rect for bg image");
    assertTrue(html.contains("v:fill"), "Should have VML fill");
    assertTrue(html.contains("v:textbox"), "Should have VML textbox");
    assertTrue(html.contains("bg.jpg"), "Should reference bg image URL");
    // Background CSS on div
    assertTrue(html.contains("url('https://example.com/bg.jpg')"), "Should have CSS background");
    // Close VML
    assertTrue(html.contains("</v:textbox></v:rect>"), "Should close VML elements");
  }

  @Test
  void renderFullWidthSimple() {
    String html = render(
      // language=MJML
      """
        <mjml>
          <mj-body>
            <mj-section full-width="full-width" background-color="#00ff00">
              <mj-column><mj-text>Full width</mj-text></mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    // Full-width: outer real HTML table with width:100%
    assertTrue(html.contains("width:100%"), "Should have full-width table");
    assertTrue(html.contains("#00ff00"), "Should have background color");
    // Inner MSO table for 600px constraint
    assertTrue(html.contains("width:600px"), "Should have inner 600px constraint");
    // No VML (no background image)
    assertFalse(html.contains("v:rect"), "Should NOT have VML without bg image");
  }

  @Test
  void renderFullWidthWithBgImage() {
    String html = render(
      // language=MJML
      """
        <mjml>
          <mj-body>
            <mj-section full-width="full-width" background-url="https://example.com/hero.jpg" background-color="#333">
              <mj-column><mj-text>Full width bg</mj-text></mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    // Outer table with background attribute
    assertTrue(
      html.contains("background=\"https://example.com/hero.jpg\""),
      "Should have background attribute on outer table");
    // VML for Outlook
    assertTrue(html.contains("v:rect"), "Should have VML rect");
    assertTrue(html.contains("mso-width-percent:1000"), "Should use percentage-based VML width");
    // Inner content
    assertTrue(html.contains("Full width bg"), "Should contain content text");
  }

  @Test
  void rawChildrenPreserveOrderAndColumnWidths() {
    for (String width : new String[] {"", " width=\"200px\""}) {
      String first = "<mj-column" + width + "><mj-text>Column one</mj-text></mj-column>";
      String second = "<mj-column><mj-text>Column two</mj-text></mj-column>";
      String before = "<span>RAW_BEFORE</span>";
      String between = "<span>RAW_BETWEEN</span>";
      String after = "<span>RAW_AFTER</span>";
      String baseline = render(rawTemplate(first + second));
      String html = render(rawTemplate(
        "<mj-raw>" + before + "</mj-raw>" + first
          + "<mj-raw>" + between + "</mj-raw>" + second
          + "<mj-raw>" + after + "</mj-raw>"));
      assertTrue(html.indexOf(before) < html.indexOf("Column one"));
      assertTrue(html.indexOf("Column one") < html.indexOf(between));
      assertTrue(html.indexOf(between) < html.indexOf("Column two"));
      assertTrue(html.indexOf("Column two") < html.indexOf(after));
      // Raw content must sit between conditional cells, never inside a column's td.
      String compact = html.replaceAll(">\\s+<", "><");
      assertTrue(compact.contains("<tr><![endif]-->" + before + "<!--[if mso | IE]><td"));
      assertTrue(compact.contains("</td><![endif]-->" + between + "<!--[if mso | IE]><td"));
      assertTrue(compact.contains("</td><![endif]-->" + after + "<!--[if mso | IE]></tr>"));
      // Removing raw children must restore identical output, including pixel widths and media queries.
      assertEquals(normalizeRawBoundaries(baseline), normalizeRawBoundaries(
        html.replace(before, "").replace(between, "").replace(after, "")));
    }
  }

  @Test
  void rawOnlyAndConsecutiveRawChildrenAreRetained() {
    String html = render(rawTemplate(
      "<mj-raw><span>FIRST_RAW</span></mj-raw><mj-raw><span>SECOND_RAW</span></mj-raw>"));
    assertTrue(html.contains("<span>FIRST_RAW</span>"));
    assertTrue(html.contains("<span>SECOND_RAW</span>"));
    assertTrue(html.indexOf("FIRST_RAW") < html.indexOf("SECOND_RAW"));
    assertTrue(html.replaceAll(">\\s+<", "><").contains(
      "<tr><![endif]--><span>FIRST_RAW</span><span>SECOND_RAW</span><!--[if mso | IE]></tr>"));
  }

  private String normalizeRawBoundaries(String html) {
    return html.replaceAll("<!\\[endif]-->\\s*<!--\\[if mso \\| IE]>", "")
      .replaceAll(">\\s+<", "><").trim();
  }

  private String rawTemplate(String children) {
    return "<mjml><mj-body><mj-section>" + children + "</mj-section></mj-body></mjml>";
  }
}
