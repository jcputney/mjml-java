package dev.jcputney.mjml.component.content;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlRenderer;
import org.junit.jupiter.api.Test;

/**
 * Tests for the mj-image component rendering.
 */
class MjImageTest {

  private String render(String mjml) {
    String html = MjmlRenderer.render(mjml);
    assertNotNull(html);
    assertFalse(html.isEmpty());
    return html;
  }

  @Test
  void basicImageRendersWithSrcAndAlt() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-image src="https://example.com/photo.jpg" alt="A photo" width="300px" />
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("src=\"https://example.com/photo.jpg\""),
        "Should contain image src attribute");
    assertTrue(html.contains("alt=\"A photo\""),
        "Should contain alt text attribute");
    assertTrue(html.contains("<img"), "Should contain an img tag");
    assertTrue(html.contains("width=\"300\""),
        "Should contain width attribute on img tag");
  }

  @Test
  void hrefWrapsImageInAnchorTag() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-image src="https://example.com/logo.png" href="https://example.com/home" target="_blank" width="200px" />
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("<a href=\"https://example.com/home\""),
        "Should wrap image in anchor tag with href");
    assertTrue(html.contains("target=\"_blank\""),
        "Should include target attribute on anchor");
    assertTrue(html.contains("<img"),
        "Should still contain the img tag inside the anchor");
  }

  @Test
  void fluidOnMobileAddsCssClass() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-image src="https://example.com/wide.jpg" fluid-on-mobile="true" width="600px" />
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("mj-full-width-mobile"),
        "Should add mj-full-width-mobile class when fluid-on-mobile is true");
  }

  @Test
  void srcsetAttributeRenders() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-image src="https://example.com/img.jpg" srcset="https://example.com/img-300.jpg 300w, https://example.com/img-600.jpg 600w" width="300px" />
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("srcset=\""),
        "Should contain srcset attribute");
    assertTrue(html.contains("img-300.jpg 300w"),
        "Should contain first srcset entry");
    assertTrue(html.contains("img-600.jpg 600w"),
        "Should contain second srcset entry");
  }

  @Test
  void usemapAttributeRenders() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-image src="https://example.com/map.jpg" usemap="#mymap" width="400px" />
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("usemap=\"#mymap\""),
        "Should contain usemap attribute on the img tag");
  }
}
