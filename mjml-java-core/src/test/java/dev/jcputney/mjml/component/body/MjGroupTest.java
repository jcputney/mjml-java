
package dev.jcputney.mjml.component.body;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlRenderer;
import org.junit.jupiter.api.Test;

/** Tests for the mj-group component rendering. */
class MjGroupTest {

  private String render(String mjml) {
    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertFalse(html.isEmpty());
    return html;
  }

  @Test
  void outlookGroupFixClassPresent() {
    String html = render(
      // language=MJML
      """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-group>
                <mj-column>
                  <mj-text>Col 1</mj-text>
                </mj-column>
                <mj-column>
                  <mj-text>Col 2</mj-text>
                </mj-column>
              </mj-group>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("mj-outlook-group-fix"), "Should contain mj-outlook-group-fix class on group div");
    assertTrue(html.contains("Col 1"), "Should render first column content");
    assertTrue(html.contains("Col 2"), "Should render second column content");
  }

  @Test
  void groupWidthDistribution() {
    String html = render(
      // language=MJML
      """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-group>
                <mj-column>
                  <mj-text>A</mj-text>
                </mj-column>
                <mj-column>
                  <mj-text>B</mj-text>
                </mj-column>
              </mj-group>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(
      html.contains("width:50%") || html.contains("width: 50%"),
      "Each column in a two-column group should get 50% width");
  }

  @Test
  void directionAttributeApplied() {
    String html = render(
      // language=MJML
      """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-group direction="rtl">
                <mj-column>
                  <mj-text>Right</mj-text>
                </mj-column>
                <mj-column>
                  <mj-text>Left</mj-text>
                </mj-column>
              </mj-group>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("direction:rtl"), "Should apply rtl direction to the group");
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
    return "<mjml><mj-body><mj-section><mj-group>" + children + "</mj-group></mj-section></mj-body></mjml>";
  }
}
