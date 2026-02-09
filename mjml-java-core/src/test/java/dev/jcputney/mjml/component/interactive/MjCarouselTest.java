package dev.jcputney.mjml.component.interactive;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlRenderer;
import org.junit.jupiter.api.Test;

/**
 * Tests for the mj-carousel component rendering.
 */
class MjCarouselTest {

  private String render(String mjml) {
    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertFalse(html.isEmpty());
    return html;
  }

  @Test
  void radioButtonInputsPresent() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-carousel>
                  <mj-carousel-image src="https://example.com/img1.jpg" />
                  <mj-carousel-image src="https://example.com/img2.jpg" />
                </mj-carousel>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("type=\"radio\""),
        "Should contain radio button inputs for carousel navigation");
    assertTrue(html.contains("mj-carousel-radio"),
        "Should contain carousel radio CSS class");
  }

  @Test
  void carouselCssGenerated() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-carousel>
                  <mj-carousel-image src="https://example.com/img1.jpg" />
                  <mj-carousel-image src="https://example.com/img2.jpg" />
                </mj-carousel>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("mj-carousel"),
        "Should include carousel CSS classes");
    assertTrue(html.contains("<style"),
        "Should include style tag for carousel CSS");
  }

  @Test
  void multipleImagesRender() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-carousel>
                  <mj-carousel-image src="https://example.com/slide1.jpg" />
                  <mj-carousel-image src="https://example.com/slide2.jpg" />
                  <mj-carousel-image src="https://example.com/slide3.jpg" />
                </mj-carousel>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("slide1.jpg"),
        "Should contain first slide image");
    assertTrue(html.contains("slide2.jpg"),
        "Should contain second slide image");
    assertTrue(html.contains("slide3.jpg"),
        "Should contain third slide image");
  }

  @Test
  void firstImageCheckedByDefault() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-carousel>
                  <mj-carousel-image src="https://example.com/a.jpg" />
                  <mj-carousel-image src="https://example.com/b.jpg" />
                </mj-carousel>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("checked=\"checked\""),
        "First radio input should be checked by default");
  }
}
