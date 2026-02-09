package dev.jcputney.mjml.component.body;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlRenderer;
import org.junit.jupiter.api.Test;

/**
 * Tests for the mj-column component rendering.
 */
class MjColumnTest {

  private String render(String mjml) {
    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertFalse(html.isEmpty());
    return html;
  }

  @Test
  void innerBorderRadiusWithBorderCollapse() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column inner-border-radius="10px" padding="10px">
                <mj-text>Content</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("border-radius:10px"),
        "Should apply inner-border-radius to inner table");
    assertTrue(html.contains("border-collapse:separate"),
        "Should use border-collapse:separate when inner-border-radius is set");
  }

  @Test
  void verticalAlignApplied() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column vertical-align="middle">
                <mj-text>Centered</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("vertical-align:middle"),
        "Should apply vertical-align:middle to the column div");
  }

  @Test
  void backgroundColorApplied() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column background-color="#f0f0f0" padding="10px">
                <mj-text>Shaded</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("#f0f0f0"),
        "Should contain the column background color");
  }

  @Test
  void paddingTriggersGutterNesting() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column padding="20px">
                <mj-text>Padded</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("padding:20px"),
        "Should apply padding via gutter structure");
  }

  @Test
  void responsiveClassPresent() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Responsive</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("mj-outlook-group-fix"),
        "Should contain mj-outlook-group-fix class");
  }
}
