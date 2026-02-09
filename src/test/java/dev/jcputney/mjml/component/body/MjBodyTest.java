package dev.jcputney.mjml.component.body;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlRenderer;
import org.junit.jupiter.api.Test;

/**
 * Tests for the mj-body component rendering.
 */
class MjBodyTest {

  private String render(String mjml) {
    String html = MjmlRenderer.render(mjml);
    assertNotNull(html);
    assertFalse(html.isEmpty());
    return html;
  }

  @Test
  void backgroundColorRenders() {
    String html = render("""
        <mjml>
          <mj-body background-color="#f4f4f4">
            <mj-section>
              <mj-column>
                <mj-text>Content</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("background-color:#f4f4f4"),
        "Should set background-color on the body wrapper div");
  }

  @Test
  void containerWidthFromBodyAttribute() {
    String html = render("""
        <mjml>
          <mj-body width="500px">
            <mj-section>
              <mj-column>
                <mj-text>Narrow</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("500px"),
        "Should use the custom width from mj-body attribute");
    // The section MSO table should reference the custom width
    assertTrue(html.contains("width:500px"),
        "Section MSO table should use the body's container width");
  }

  @Test
  void defaultWidthIs600px() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Default width</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("width:600px"),
        "Should use the default 600px container width");
  }
}
