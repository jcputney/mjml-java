package dev.jcputney.mjml.component.head;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlRenderer;
import org.junit.jupiter.api.Test;

/** Tests for the mj-breakpoint component rendering. */
class MjBreakpointTest {

  private String render(String mjml) {
    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertFalse(html.isEmpty());
    return html;
  }

  @Test
  void customBreakpointInMediaQuery() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-head>
            <mj-breakpoint width="400px" />
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Content</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("400px"), "Should include the custom breakpoint value in the output");
    assertTrue(html.contains("@media"), "Should contain a media query");
  }

  @Test
  void defaultBreakpointUsed() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Content</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("480px"), "Default breakpoint should be 480px");
    assertTrue(html.contains("@media"), "Should contain a media query with default breakpoint");
  }

  @Test
  void breakpointAffectsResponsiveStyles() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-head>
            <mj-breakpoint width="320px" />
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Content</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("320px"), "Should use 320px breakpoint in responsive styles");
  }
}
