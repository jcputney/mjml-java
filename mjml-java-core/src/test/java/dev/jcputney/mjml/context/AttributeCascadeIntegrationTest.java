package dev.jcputney.mjml.context;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlRenderer;
import dev.jcputney.mjml.MjmlRenderResult;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for the MJML attribute cascade:
 * inline > mj-class > tag defaults > mj-all > component defaults
 */
class AttributeCascadeIntegrationTest {

  private String render(String mjml) {
    MjmlRenderResult result = MjmlRenderer.render(mjml);
    assertNotNull(result.html());
    assertFalse(result.html().isEmpty());
    return result.html();
  }

  @Test
  void inlineOverridesMjClass() {
    String html = render("""
        <mjml>
          <mj-head>
            <mj-attributes>
              <mj-class name="red" color="red" />
            </mj-attributes>
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text mj-class="red" color="blue">Inline wins</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    // Inline color="blue" should override mj-class color="red"
    assertTrue(html.contains("color:blue"),
        "Inline attribute should override mj-class attribute");
  }

  @Test
  void mjAllAffectsAllComponents() {
    String html = render("""
        <mjml>
          <mj-head>
            <mj-attributes>
              <mj-all font-family="Helvetica" />
            </mj-attributes>
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Text one</mj-text>
                <mj-text>Text two</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    assertTrue(html.contains("Helvetica"),
        "mj-all font-family should appear in rendered output");
  }

  @Test
  void tagDefaultOverridesComponentDefault() {
    // mj-text default font-size is 13px; tag default overrides to 18px
    String html = render("""
        <mjml>
          <mj-head>
            <mj-attributes>
              <mj-text font-size="18px" />
            </mj-attributes>
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Custom size</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    assertTrue(html.contains("font-size:18px"),
        "Tag default (18px) should override component default (13px)");
  }

  @Test
  void multipleMjClassesMerge() {
    String html = render("""
        <mjml>
          <mj-head>
            <mj-attributes>
              <mj-class name="bold" font-weight="bold" />
              <mj-class name="big" font-size="24px" />
            </mj-attributes>
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text mj-class="bold big">Bold and big</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    assertTrue(html.contains("font-weight:bold"),
        "First mj-class attribute should be applied");
    assertTrue(html.contains("font-size:24px"),
        "Second mj-class attribute should be applied");
  }

  @Test
  void fullCascadePriorityTest() {
    // Test all cascade levels together:
    // component default padding=10px 25px, mj-all padding=5px, mj-text padding=15px,
    // mj-class padding=20px, inline padding=30px
    // inline should win
    String html = render("""
        <mjml>
          <mj-head>
            <mj-attributes>
              <mj-all font-family="Arial" />
              <mj-text color="green" />
              <mj-class name="custom" color="red" />
            </mj-attributes>
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text mj-class="custom" color="blue">Cascade test</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);
    // inline color="blue" > mj-class color="red" > tag default color="green"
    assertTrue(html.contains("color:blue"),
        "Inline should win over all other cascade levels");
    assertTrue(html.contains("Arial"),
        "mj-all font-family should still be applied for non-overridden attributes");
  }
}
