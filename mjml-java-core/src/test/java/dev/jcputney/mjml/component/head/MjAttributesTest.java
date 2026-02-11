package dev.jcputney.mjml.component.head;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlRenderer;
import org.junit.jupiter.api.Test;

/** Tests for the mj-attributes component rendering. */
class MjAttributesTest {

  private String render(String mjml) {
    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertFalse(html.isEmpty());
    return html;
  }

  @Test
  void mjAllAppliesToAllComponents() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-head>
            <mj-attributes>
              <mj-all font-family="Helvetica" />
            </mj-attributes>
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Text with global font</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("Helvetica"), "mj-all font-family should be applied to text content");
  }

  @Test
  void mjClassAppliedWhenReferenced() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-head>
            <mj-attributes>
              <mj-class name="red-text" color="#ff0000" />
            </mj-attributes>
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text mj-class="red-text">Red text</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("#ff0000"), "mj-class color should be applied to the text component");
  }

  @Test
  void tagSpecificDefaults() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-head>
            <mj-attributes>
              <mj-text font-size="18px" />
            </mj-attributes>
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Larger text</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("18px"), "Tag-specific default font-size should be applied");
  }

  @Test
  void inlineOverridesClass() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-head>
            <mj-attributes>
              <mj-class name="blue" color="#0000ff" />
            </mj-attributes>
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text mj-class="blue" color="#00ff00">Green wins</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("#00ff00"), "Inline color should override mj-class color");
  }
}
