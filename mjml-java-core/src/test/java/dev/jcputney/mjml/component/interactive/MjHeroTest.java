package dev.jcputney.mjml.component.interactive;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlRenderer;
import org.junit.jupiter.api.Test;

/** Tests for the mj-hero component rendering. */
class MjHeroTest {

  private String render(String mjml) {
    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertFalse(html.isEmpty());
    return html;
  }

  @Test
  void fixedHeightModeRendersWithExplicitHeight() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-body>
            <mj-hero mode="fixed-height" height="400px" background-color="#2f2f2f">
              <mj-text color="#ffffff">Hero Content</mj-text>
            </mj-hero>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("Hero Content"), "Should render hero text content");
    assertTrue(html.contains("mj-hero-content"), "Should contain mj-hero-content class");
    assertTrue(html.contains("#2f2f2f"), "Should include background color");
  }

  @Test
  void fluidHeightModeRendersWithPaddingSpacers() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-body>
            <mj-hero mode="fluid-height" background-color="#333333">
              <mj-text color="#ffffff">Fluid Hero</mj-text>
            </mj-hero>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("Fluid Hero"), "Should render hero text content");
    assertTrue(html.contains("mj-hero-content"), "Should contain mj-hero-content class");
  }

  @Test
  void backgroundImageRendersVmlElement() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-body>
            <mj-hero mode="fixed-height" height="400px"
                     background-url="https://example.com/hero-bg.jpg"
                     background-color="#2f2f2f">
              <mj-text color="#ffffff">VML Hero</mj-text>
            </mj-hero>
          </mj-body>
        </mjml>
        """);

    assertTrue(
        html.contains("v:image") || html.contains("v:rect"),
        "Should contain VML elements for background image in MSO conditional");
    assertTrue(html.contains("hero-bg.jpg"), "Should reference the background image URL");
  }

  @Test
  void heroWithPaddingAndVerticalAlign() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-body>
            <mj-hero mode="fixed-height" height="300px" padding="40px 20px"
                     vertical-align="bottom" background-color="#1a1a1a">
              <mj-text color="#ffffff">Bottom aligned</mj-text>
            </mj-hero>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("Bottom aligned"), "Should render hero content");
    assertTrue(html.contains("vertical-align"), "Should include vertical-align in the output");
  }
}
