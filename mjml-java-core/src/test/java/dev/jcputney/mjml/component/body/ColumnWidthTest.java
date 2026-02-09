package dev.jcputney.mjml.component.body;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlRenderer;
import org.junit.jupiter.api.Test;

/**
 * Tests for column width calculation logic in MjSection and MjGroup.
 * Verifies auto-distribution, explicit widths, and percentage widths.
 */
class ColumnWidthTest {

  private String render(String mjml) {
    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertFalse(html.isEmpty());
    return html;
  }

  @Test
  void threeAutoColumnsGetEqualWidth() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column><mj-text>A</mj-text></mj-column>
              <mj-column><mj-text>B</mj-text></mj-column>
              <mj-column><mj-text>C</mj-text></mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    // 600 / 3 = 200px each
    assertTrue(html.contains("width:200px"), "Should have 200px column width");
    // Responsive class: 100/3 = 33.33...
    assertTrue(html.contains("mj-column-per-33"), "Should have 33% responsive class");
  }

  @Test
  void explicitPixelWidth() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column width="200px"><mj-text>Fixed</mj-text></mj-column>
              <mj-column><mj-text>Auto 1</mj-text></mj-column>
              <mj-column><mj-text>Auto 2</mj-text></mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    assertTrue(html.contains("width:200px"), "Should have explicit 200px column");
    assertTrue(html.contains("mj-column-px-200"), "Should have pixel responsive class");
  }

  @Test
  void percentageWidth() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column width="25%"><mj-text>Quarter</mj-text></mj-column>
              <mj-column width="75%"><mj-text>Three quarters</mj-text></mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    // 25% of 600 = 150px
    assertTrue(html.contains("width:150px"), "Should have 150px for 25% column");
    // 75% of 600 = 450px
    assertTrue(html.contains("width:450px"), "Should have 450px for 75% column");
    // Responsive classes
    assertTrue(html.contains("mj-column-per-25"), "Should have 25% responsive class");
    assertTrue(html.contains("mj-column-per-75"), "Should have 75% responsive class");
  }
}
