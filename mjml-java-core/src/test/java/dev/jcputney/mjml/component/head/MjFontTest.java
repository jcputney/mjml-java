package dev.jcputney.mjml.component.head;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlRenderer;
import org.junit.jupiter.api.Test;

/**
 * Tests for the mj-font component rendering.
 */
class MjFontTest {

  private String render(String mjml) {
    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertFalse(html.isEmpty());
    return html;
  }

  @Test
  void fontLinkInOutput() {
    String html = render("""
        <mjml>
          <mj-head>
            <mj-font name="Roboto" href="https://fonts.googleapis.com/css?family=Roboto" />
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text font-family="Roboto, sans-serif">Roboto text</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("fonts.googleapis.com"),
        "Should include Google Fonts link in the HTML output");
  }

  @Test
  void fontRegistrationAppearsInStyleTag() {
    String html = render("""
        <mjml>
          <mj-head>
            <mj-font name="Open Sans" href="https://fonts.googleapis.com/css?family=Open+Sans" />
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text font-family="Open Sans, sans-serif">Open Sans text</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("@import url(") || html.contains("<link"),
        "Should include font import or link tag in the output");
  }

  @Test
  void multipleFontsRegistered() {
    String html = render("""
        <mjml>
          <mj-head>
            <mj-font name="Lato" href="https://fonts.googleapis.com/css?family=Lato" />
            <mj-font name="Merriweather" href="https://fonts.googleapis.com/css?family=Merriweather" />
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text font-family="Lato, sans-serif">Lato text</mj-text>
                <mj-text font-family="Merriweather, serif">Merriweather text</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("family=Lato"),
        "Should include first font reference");
    assertTrue(html.contains("family=Merriweather"),
        "Should include second font reference");
  }
}
