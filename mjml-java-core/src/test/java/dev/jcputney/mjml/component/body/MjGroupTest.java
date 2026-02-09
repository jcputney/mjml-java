package dev.jcputney.mjml.component.body;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlRenderer;
import org.junit.jupiter.api.Test;

/**
 * Tests for the mj-group component rendering.
 */
class MjGroupTest {

  private String render(String mjml) {
    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertFalse(html.isEmpty());
    return html;
  }

  @Test
  void outlookGroupFixClassPresent() {
    String html = render("""
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

    assertTrue(html.contains("mj-outlook-group-fix"),
        "Should contain mj-outlook-group-fix class on group div");
    assertTrue(html.contains("Col 1"),
        "Should render first column content");
    assertTrue(html.contains("Col 2"),
        "Should render second column content");
  }

  @Test
  void groupWidthDistribution() {
    String html = render("""
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

    assertTrue(html.contains("width:50%") || html.contains("width: 50%"),
        "Each column in a two-column group should get 50% width");
  }

  @Test
  void directionAttributeApplied() {
    String html = render("""
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

    assertTrue(html.contains("direction:rtl"),
        "Should apply rtl direction to the group");
  }
}
