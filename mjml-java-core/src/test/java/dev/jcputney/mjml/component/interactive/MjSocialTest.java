package dev.jcputney.mjml.component.interactive;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlRenderer;
import org.junit.jupiter.api.Test;

/**
 * Tests for the mj-social and mj-social-element component rendering.
 */
class MjSocialTest {

  private String render(String mjml) {
    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertFalse(html.isEmpty());
    return html;
  }

  @Test
  void knownNetworkFacebook() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-social>
                  <mj-social-element name="facebook" href="https://facebook.com/test">Facebook</mj-social-element>
                </mj-social>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("Facebook"),
        "Should render Facebook link text");
    assertTrue(html.contains("<img"),
        "Should render a social icon image");
    assertTrue(html.contains("facebook"),
        "Should reference facebook in the output");
  }

  @Test
  void knownNetworkTwitter() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-social>
                  <mj-social-element name="twitter" href="https://twitter.com/test">Twitter</mj-social-element>
                </mj-social>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("Twitter"),
        "Should render Twitter link text");
    assertTrue(html.contains("<img"),
        "Should render a social icon image");
  }

  @Test
  void customIconSrc() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-social>
                  <mj-social-element name="custom" src="https://example.com/custom-icon.png" href="https://example.com">Custom</mj-social-element>
                </mj-social>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("custom-icon.png"),
        "Should use the custom icon src");
    assertTrue(html.contains("Custom"),
        "Should render custom link text");
  }

  @Test
  void verticalModeLayout() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-social mode="vertical">
                  <mj-social-element name="facebook" href="https://facebook.com">FB</mj-social-element>
                  <mj-social-element name="twitter" href="https://twitter.com">TW</mj-social-element>
                </mj-social>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("FB"),
        "Should render first social element text");
    assertTrue(html.contains("TW"),
        "Should render second social element text");
  }

  @Test
  void multipleNetworks() {
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-social>
                  <mj-social-element name="facebook" href="https://facebook.com">FB</mj-social-element>
                  <mj-social-element name="twitter" href="https://twitter.com">TW</mj-social-element>
                  <mj-social-element name="google" href="https://google.com">G+</mj-social-element>
                </mj-social>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("FB"),
        "Should render Facebook element");
    assertTrue(html.contains("TW"),
        "Should render Twitter element");
    assertTrue(html.contains("G+"),
        "Should render Google element");
  }
}
