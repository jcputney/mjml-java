package dev.jcputney.mjml.component.interactive;

import static org.junit.jupiter.api.Assertions.*;

import dev.jcputney.mjml.MjmlRenderResult;
import dev.jcputney.mjml.MjmlRenderer;
import org.junit.jupiter.api.Test;

/** Tests that mj-hero defaults to fluid-height mode, matching the official MJML spec. */
class MjHeroDefaultModeTest {

  @Test
  void defaultModeIsFluidHeight() {
    // mj-hero without explicit mode should use fluid-height
    String mjml =
        // language=MJML
        """
        <mjml>
          <mj-body>
            <mj-hero background-url="https://example.com/bg.jpg"
                     background-height="400px" background-width="600px">
              <mj-text>Hero content</mj-text>
            </mj-hero>
          </mj-body>
        </mjml>
        """;

    MjmlRenderResult result = MjmlRenderer.render(mjml);
    assertNotNull(result);
    String html = result.html();

    // In fluid-height mode, spacer tds with padding-bottom percentage are emitted
    // for aspect-ratio preservation
    assertTrue(
        html.contains("padding-bottom:"),
        "Fluid-height mode should include padding-bottom spacer tds");
  }

  @Test
  void explicitFixedHeightModeStillWorks() {
    String mjml =
        // language=MJML
        """
        <mjml>
          <mj-body>
            <mj-hero mode="fixed-height" height="400px"
                     background-url="https://example.com/bg.jpg">
              <mj-text>Hero content</mj-text>
            </mj-hero>
          </mj-body>
        </mjml>
        """;

    MjmlRenderResult result = MjmlRenderer.render(mjml);
    assertNotNull(result);
    assertTrue(result.html().contains("Hero content"));
  }

  @Test
  void explicitFluidHeightModeWorks() {
    String mjml =
        // language=MJML
        """
        <mjml>
          <mj-body>
            <mj-hero mode="fluid-height"
                     background-url="https://example.com/bg.jpg"
                     background-height="400px" background-width="600px">
              <mj-text>Hero content</mj-text>
            </mj-hero>
          </mj-body>
        </mjml>
        """;

    MjmlRenderResult result = MjmlRenderer.render(mjml);
    assertNotNull(result);
    assertTrue(result.html().contains("Hero content"));
  }
}
