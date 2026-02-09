package dev.jcputney.mjml.component.head;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlConfiguration;
import dev.jcputney.mjml.MjmlRenderer;
import org.junit.jupiter.api.Test;

/**
 * Tests for mj-html-attributes processing and application.
 */
class MjHtmlAttributesTest {

  @Test
  void appliesDataAttributeToMatchingElement() {
    String mjml = """
        <mjml>
          <mj-head>
            <mj-html-attributes>
              <mj-selector path=".custom-class">
                <mj-html-attribute name="data-id">123</mj-html-attribute>
              </mj-selector>
            </mj-html-attributes>
          </mj-head>
          <mj-body>
            <mj-section css-class="custom-class">
              <mj-column>
                <mj-text>Content</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertTrue(html.contains("data-id=\"123\""),
        "Should apply data-id attribute to element matching .custom-class");
  }

  @Test
  void appliesMultipleAttributesToSameSelector() {
    String mjml = """
        <mjml>
          <mj-head>
            <mj-html-attributes>
              <mj-selector path=".target">
                <mj-html-attribute name="data-x">a</mj-html-attribute>
                <mj-html-attribute name="data-y">b</mj-html-attribute>
              </mj-selector>
            </mj-html-attributes>
          </mj-head>
          <mj-body>
            <mj-section css-class="target">
              <mj-column>
                <mj-text>Content</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertTrue(html.contains("data-x=\"a\""), "Should apply data-x attribute");
    assertTrue(html.contains("data-y=\"b\""), "Should apply data-y attribute");
  }

  @Test
  void doesNotApplyToNonMatchingElements() {
    String mjml = """
        <mjml>
          <mj-head>
            <mj-html-attributes>
              <mj-selector path=".non-existent">
                <mj-html-attribute name="data-test">value</mj-html-attribute>
              </mj-selector>
            </mj-html-attributes>
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Content</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertTrue(!html.contains("data-test"),
        "Should not apply attribute when no element matches selector");
  }

  @Test
  void handlesEmptyHtmlAttributes() {
    String mjml = """
        <mjml>
          <mj-head>
            <mj-html-attributes>
            </mj-html-attributes>
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Content</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertTrue(html.contains("Content"));
  }

  @Test
  void handlesMultipleSelectorsInSameBlock() {
    String mjml = """
        <mjml>
          <mj-head>
            <mj-html-attributes>
              <mj-selector path=".first">
                <mj-html-attribute name="data-order">1</mj-html-attribute>
              </mj-selector>
              <mj-selector path=".second">
                <mj-html-attribute name="data-order">2</mj-html-attribute>
              </mj-selector>
            </mj-html-attributes>
          </mj-head>
          <mj-body>
            <mj-section css-class="first">
              <mj-column>
                <mj-text>First section</mj-text>
              </mj-column>
            </mj-section>
            <mj-section css-class="second">
              <mj-column>
                <mj-text>Second section</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertTrue(html.contains("data-order=\"1\""), "Should apply first selector attributes");
    assertTrue(html.contains("data-order=\"2\""), "Should apply second selector attributes");
  }
}
