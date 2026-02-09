package dev.jcputney.mjml.component.content;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlRenderer;
import org.junit.jupiter.api.Test;

/**
 * Tests for the mj-button component rendering.
 */
class MjButtonTest {

  private String render(String mjml) {
    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertFalse(html.isEmpty());
    return html;
  }

  @Test
  void basicButtonWithTextAndHref() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-button href="https://example.com/signup">Sign Up</mj-button>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("Sign Up"),
        "Should contain button text");
    assertTrue(html.contains("href=\"https://example.com/signup\""),
        "Should contain href attribute on anchor");
    assertTrue(html.contains("#414141"),
        "Should have default background color");
    assertTrue(html.contains("<a "),
        "Should render as an anchor tag styled as a button");
  }

  @Test
  void customWidthInPixels() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-button href="#" width="300px">Wide Button</mj-button>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("Wide Button"),
        "Should contain button text");
    // The outer table should have width:300px
    assertTrue(html.contains("width:300px"),
        "Should have the custom width on the outer table");
    // The anchor should have a computed inner width (300 - horizontal inner-padding)
    // Default inner-padding is 10px 25px, so horizontal = 50px, inner = 250px
    assertTrue(html.contains("width:250px"),
        "Should have computed inner width on anchor (300 - 50 = 250)");
  }

  @Test
  void customWidthInPercent() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-button href="#" width="100%">Full Width</mj-button>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("Full Width"),
        "Should contain button text");
    // Percentage widths set width on the outer table but NOT the anchor
    assertTrue(html.contains("width:100%"),
        "Should have percentage width on outer table style");
  }

  @Test
  void relAttributeRenderedWhenPresent() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-button href="https://example.com" rel="noopener">External</mj-button>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("rel=\"noopener\""),
        "Should include rel attribute on the anchor tag");
    assertTrue(html.contains("External"),
        "Should contain button text");
  }
}
