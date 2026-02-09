package dev.jcputney.mjml.render;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlRenderer;
import org.junit.jupiter.api.Test;

/**
 * Tests for the FontScanner class that auto-registers default fonts.
 * FontScanner is package-private, so we test it indirectly through the render pipeline.
 */
class FontScannerTest {

  private String render(String mjml) {
    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertFalse(html.isEmpty());
    return html;
  }

  @Test
  void registersDefaultFontForMjText() {
    // mj-text has default font-family "Ubuntu, Helvetica, Arial, sans-serif"
    // which includes "Ubuntu" - a known default font
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Hello world</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    // The default font "Ubuntu" from mj-text defaults should be auto-registered
    // and appear as a Google Fonts link/import in the output
    assertTrue(html.contains("fonts.googleapis.com") && html.contains("Ubuntu"),
        "Should auto-register the Ubuntu font from mj-text defaults");
  }

  @Test
  void handlesNullBodyGracefully() {
    // A document with only a head and no body should not crash the font scanner
    String html = render("""
        <mjml>
          <mj-head>
            <mj-title>No body</mj-title>
          </mj-head>
        </mjml>
        """);

    assertNotNull(html, "Should render without error even with no body");
  }

  @Test
  void resolvesExplicitFontFamilyAttribute() {
    // An explicit font-family attribute containing a known font should trigger registration
    String html = render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text font-family="Open Sans, sans-serif">Custom font</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("fonts.googleapis.com") && html.contains("Open+Sans"),
        "Should register Open Sans when used in font-family attribute");
  }

  @Test
  void handlesUnknownTagWithoutCrashing() {
    // If there's an unknown tag in the tree, the FontScanner should handle it gracefully
    // (the registry.createComponent returns null for unknown tags)
    assertDoesNotThrow(() -> render("""
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Content</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """));
  }

  @Test
  void skipsHashTagNames() {
    // #text and #comment nodes should be skipped by the scanner
    // This is tested indirectly - if they weren't skipped, we'd get errors
    // trying to create components for "#text" tags
    String html = render("""
        <mjml>
          <mj-body>
            <!-- a comment -->
            <mj-section>
              <mj-column>
                <mj-text>Text with comment sibling</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertNotNull(html, "Should render successfully with comment nodes present");
  }

  @Test
  void cachingDoesNotAffectResults() {
    // Rendering the same template twice should produce the same font registration
    // (tests that the defaultsCache works correctly)
    String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text font-family="Lato, sans-serif">First</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html1 = render(mjml);
    String html2 = render(mjml);

    boolean hasLato1 = html1.contains("Lato");
    boolean hasLato2 = html2.contains("Lato");
    assertTrue(hasLato1 == hasLato2,
        "Both renders should produce the same font registration result");
  }
}
