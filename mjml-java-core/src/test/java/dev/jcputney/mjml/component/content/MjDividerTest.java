package dev.jcputney.mjml.component.content;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlRenderer;
import org.junit.jupiter.api.Test;

/**
 * Tests for the mj-divider component rendering.
 */
class MjDividerTest {

  private String render(String mjml) {
    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertFalse(html.isEmpty());
    return html;
  }

  @Test
  void defaultDividerRendersWithCenterAlign() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-divider />
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("<p"),
        "Should render a p element for the divider");
    assertTrue(html.contains("border-top:"),
        "Should contain border-top style");
    assertTrue(html.contains("0px auto"),
        "Default align is center, should use margin 0px auto");
  }

  @Test
  void leftAlignUsesSingleZeroMargin() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-divider align="left" />
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("margin:0px"),
        "Left align should use margin 0px");
  }

  @Test
  void rightAlignUsesAutoLeftMargin() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-divider align="right" />
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("0px 0px 0px auto"),
        "Right align should use margin 0px 0px 0px auto");
  }

  @Test
  void customBorderStyle() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-divider border-color="#ff0000" border-style="dashed" border-width="3px" />
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("dashed"),
        "Should use the custom border style");
    assertTrue(html.contains("#ff0000"),
        "Should use the custom border color");
    assertTrue(html.contains("3px"),
        "Should use the custom border width");
  }

  @Test
  void customWidthApplied() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-divider width="50%" />
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("width:50%"),
        "Should apply custom width to the divider");
  }
}
