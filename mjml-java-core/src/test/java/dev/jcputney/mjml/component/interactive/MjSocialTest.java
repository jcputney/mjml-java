package dev.jcputney.mjml.component.interactive;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlRenderer;
import org.junit.jupiter.api.Test;

/** Tests for the mj-social and mj-social-element component rendering. */
class MjSocialTest {

    private String render(String mjml) {
        String html = MjmlRenderer.render(mjml).html();
        assertNotNull(html);
        assertFalse(html.isEmpty());
        return html;
    }

    @Test
    void knownNetworkFacebook() {
        String html = render(
                // language=MJML
                """
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

        assertTrue(html.contains("Facebook"), "Should render Facebook link text");
        assertTrue(html.contains("<img"), "Should render a social icon image");
        assertTrue(html.contains("facebook"), "Should reference facebook in the output");
    }

    @Test
    void knownNetworkTwitter() {
        String html = render(
                // language=MJML
                """
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

        assertTrue(html.contains("Twitter"), "Should render Twitter link text");
        assertTrue(html.contains("<img"), "Should render a social icon image");
    }

    @Test
    void customIconSrc() {
        String html = render(
                // language=MJML
                """
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

        assertTrue(html.contains("custom-icon.png"), "Should use the custom icon src");
        assertTrue(html.contains("Custom"), "Should render custom link text");
    }

    @Test
    void verticalModeLayout() {
        String html = render(
                // language=MJML
                """
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

        assertTrue(html.contains("FB"), "Should render first social element text");
        assertTrue(html.contains("TW"), "Should render second social element text");
    }

    @Test
    void verticalModeLayoutProducesRows() {
        String html = render(
                // language=MJML
                """
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

        // In vertical mode, each element renders as a <tr> rather than an inline table
        assertTrue(html.contains("FB"), "Should render first social element text");
        assertTrue(html.contains("TW"), "Should render second social element text");
        // Vertical mode should NOT use inline-table
        assertFalse(html.contains("display:inline-table"), "Vertical mode should not use inline-table display");
    }

    @Test
    void noHrefRenderingUsesSpanNotAnchor() {
        String html = render(
                // language=MJML
                """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-social>
                  <mj-social-element name="facebook">No link</mj-social-element>
                </mj-social>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

        assertTrue(html.contains("No link"), "Should render text content");
        // Without href, the text should be in a <span> not an <a>
        assertTrue(html.contains("<span style="), "No-href social element should use <span> for text, not <a>");
    }

    @Test
    void customIconWithNoName() {
        String html = render(
                // language=MJML
                """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-social>
                  <mj-social-element src="https://example.com/icon.png" href="https://example.com">Custom</mj-social-element>
                </mj-social>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

        assertTrue(html.contains("icon.png"), "Should use the custom src attribute directly");
        assertTrue(html.contains("Custom"), "Should render custom text label");
    }

    @Test
    void iconSizeOverride() {
        String html = render(
                // language=MJML
                """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-social>
                  <mj-social-element name="facebook" icon-size="40px" href="https://facebook.com">FB</mj-social-element>
                </mj-social>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

        assertTrue(html.contains("width:40px"), "Should use custom icon-size in width style");
        assertTrue(html.contains("width=\"40\""), "Should use custom icon-size as img width attribute");
    }

    @Test
    void zeroPaddingRendersCorrectly() {
        String html = render(
                // language=MJML
                """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-social>
                  <mj-social-element name="facebook" padding="0px" href="https://facebook.com">FB</mj-social-element>
                </mj-social>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

        assertTrue(html.contains("padding:0px"), "Should respect zero padding");
        assertTrue(html.contains("FB"), "Should render text content");
    }

    @Test
    void parentInnerPaddingOverridesChildDefault() {
        String html = render(
                // language=MJML
                """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-social inner-padding="8px 12px">
                  <mj-social-element name="facebook" href="https://facebook.com">FB</mj-social-element>
                </mj-social>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

        assertTrue(html.contains("padding:8px 12px"), "Should use parent's inner-padding override");
    }

    @Test
    void noshareVariantDoesNotApplyShareUrl() {
        String html = render(
                // language=MJML
                """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-social>
                  <mj-social-element name="facebook-noshare" href="https://facebook.com/mypage">FB</mj-social-element>
                </mj-social>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

        // -noshare should prevent share URL substitution, keeping the original href
        assertTrue(html.contains("facebook.com/mypage"), "Noshare variant should preserve the original href");
    }

    @Test
    void multipleNetworks() {
        String html = render(
                // language=MJML
                """
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

        assertTrue(html.contains("FB"), "Should render Facebook element");
        assertTrue(html.contains("TW"), "Should render Twitter element");
        assertTrue(html.contains("G+"), "Should render Google element");
    }
}
