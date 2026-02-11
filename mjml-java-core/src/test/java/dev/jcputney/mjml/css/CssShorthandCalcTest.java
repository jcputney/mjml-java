package dev.jcputney.mjml.css;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlRenderer;
import org.junit.jupiter.api.Test;

/**
 * Tests for CSS shorthand properties and calc() expressions passing through correctly to output.
 */
class CssShorthandCalcTest {

  // -- CSS Shorthand properties in inline styles --

  @Test
  void marginShorthandInlineStyle() {
    String mjml = """
        <mjml>
          <mj-head>
            <mj-style inline="inline">
              .margin-test { margin: 10px 20px 30px 40px; }
            </mj-style>
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text css-class="margin-test">Margin test</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertTrue(html.contains("Margin test"));
    // Inline CSS should expand or pass through the shorthand
    assertTrue(html.contains("margin"),
        "Margin shorthand should be present in output");
  }

  @Test
  void paddingShorthandInStyles() {
    String mjml = """
        <mjml>
          <mj-head>
            <mj-style>
              .pad-test { padding: 5px 10px; }
            </mj-style>
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text css-class="pad-test">Padding test</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertTrue(html.contains("padding: 5px 10px"),
        "Padding shorthand should be preserved in style block");
  }

  @Test
  void borderShorthandInStyles() {
    String mjml = """
        <mjml>
          <mj-head>
            <mj-style>
              .border-test { border: 2px solid #333; }
            </mj-style>
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text css-class="border-test">Border test</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertTrue(html.contains("border: 2px solid #333"),
        "Border shorthand should be preserved in style block");
  }

  // -- calc() expressions --

  @Test
  void calcExpressionInStyleBlock() {
    String mjml = """
        <mjml>
          <mj-head>
            <mj-style>
              .calc-test { width: calc(100% - 20px); }
            </mj-style>
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text css-class="calc-test">Calc test</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertTrue(html.contains("calc(100% - 20px)"),
        "calc() expression should be preserved in style block");
  }

  @Test
  void calcExpressionInInlineStyle() {
    String mjml = """
        <mjml>
          <mj-head>
            <mj-style inline="inline">
              .calc-inline { max-width: calc(600px - 40px); }
            </mj-style>
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text css-class="calc-inline">Calc inline</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    // calc() should pass through even in inlined styles
    assertTrue(html.contains("calc(600px - 40px)"),
        "calc() expression should be preserved when inlined");
  }

  @Test
  void nestedCalcExpression() {
    String mjml = """
        <mjml>
          <mj-head>
            <mj-style>
              .nested-calc { width: calc(100% - calc(20px + 10px)); }
            </mj-style>
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text css-class="nested-calc">Nested calc</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertTrue(html.contains("calc(100% - calc(20px + 10px))"),
        "Nested calc() should be preserved in style block");
  }

  // -- Component-level padding shorthand --

  @Test
  void sectionPaddingShorthandAttribute() {
    String mjml = """
        <mjml>
          <mj-body>
            <mj-section padding="10px 20px 30px 40px">
              <mj-column>
                <mj-text>Section padding</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertTrue(html.contains("Section padding"),
        "Content should render with section padding shorthand");
    // The padding should appear in the rendered styles
    assertTrue(html.contains("padding"),
        "Padding values should appear in the output");
  }

  @Test
  void textPaddingShorthandAttribute() {
    String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text padding="5px 15px">Text padding shorthand</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertTrue(html.contains("Text padding shorthand"));
  }

  // -- CSS variables and custom properties --

  @Test
  void cssVariablesInStyleBlock() {
    String mjml = """
        <mjml>
          <mj-head>
            <mj-style>
              :root { --brand-color: #ff6600; }
              .var-test { color: var(--brand-color); }
            </mj-style>
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text css-class="var-test">Var test</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertTrue(html.contains("--brand-color"),
        "CSS custom properties should be preserved");
    assertTrue(html.contains("var(--brand-color)"),
        "var() function should be preserved");
  }
}
