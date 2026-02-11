package dev.jcputney.mjml.component;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlConfiguration;
import dev.jcputney.mjml.MjmlRenderer;
import dev.jcputney.mjml.MjmlRenderResult;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Additional attribute-level tests for under-tested components:
 * mj-table, mj-raw, mj-wrapper, mj-group, mj-body.
 */
class UnderTestedComponentTest {

  private String render(String mjml) {
    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertFalse(html.isEmpty());
    return html;
  }

  // ── mj-table attribute tests ──────────────────────────────────────────────

  @Nested
  class MjTableAttributes {

    @Test
    void colorAttribute() {
      String html = render("""
          <mjml>
            <mj-body>
              <mj-section>
                <mj-column>
                  <mj-table color="#ff0000">
                    <tr><td>Red text</td></tr>
                  </mj-table>
                </mj-column>
              </mj-section>
            </mj-body>
          </mjml>
          """);

      assertTrue(html.contains("color:#ff0000"),
          "mj-table should apply color attribute");
      assertTrue(html.contains("Red text"));
    }

    @Test
    void fontSizeAttribute() {
      String html = render("""
          <mjml>
            <mj-body>
              <mj-section>
                <mj-column>
                  <mj-table font-size="18px">
                    <tr><td>Big text</td></tr>
                  </mj-table>
                </mj-column>
              </mj-section>
            </mj-body>
          </mjml>
          """);

      assertTrue(html.contains("font-size:18px"),
          "mj-table should apply font-size attribute");
    }

    @Test
    void fontFamilyAttribute() {
      String html = render("""
          <mjml>
            <mj-body>
              <mj-section>
                <mj-column>
                  <mj-table font-family="Georgia, serif">
                    <tr><td>Serif text</td></tr>
                  </mj-table>
                </mj-column>
              </mj-section>
            </mj-body>
          </mjml>
          """);

      assertTrue(html.contains("Georgia"),
          "mj-table should apply font-family attribute");
    }

    @Test
    void lineHeightAttribute() {
      String html = render("""
          <mjml>
            <mj-body>
              <mj-section>
                <mj-column>
                  <mj-table line-height="24px">
                    <tr><td>Spaced text</td></tr>
                  </mj-table>
                </mj-column>
              </mj-section>
            </mj-body>
          </mjml>
          """);

      assertTrue(html.contains("line-height:24px"),
          "mj-table should apply line-height attribute");
    }

    @Test
    void paddingAttribute() {
      String html = render("""
          <mjml>
            <mj-body>
              <mj-section>
                <mj-column>
                  <mj-table padding="10px 20px">
                    <tr><td>Padded</td></tr>
                  </mj-table>
                </mj-column>
              </mj-section>
            </mj-body>
          </mjml>
          """);

      assertTrue(html.contains("Padded"));
    }

    @Test
    void containerBackgroundColor() {
      String html = render("""
          <mjml>
            <mj-body>
              <mj-section>
                <mj-column>
                  <mj-table container-background-color="#eeeeee">
                    <tr><td>Bg color</td></tr>
                  </mj-table>
                </mj-column>
              </mj-section>
            </mj-body>
          </mjml>
          """);

      assertTrue(html.contains("#eeeeee"),
          "mj-table should apply container-background-color");
    }

    @Test
    void multiRowTable() {
      String html = render("""
          <mjml>
            <mj-body>
              <mj-section>
                <mj-column>
                  <mj-table>
                    <tr><th>Header 1</th><th>Header 2</th></tr>
                    <tr><td>Row 1 Col 1</td><td>Row 1 Col 2</td></tr>
                    <tr><td>Row 2 Col 1</td><td>Row 2 Col 2</td></tr>
                  </mj-table>
                </mj-column>
              </mj-section>
            </mj-body>
          </mjml>
          """);

      assertTrue(html.contains("Header 1"));
      assertTrue(html.contains("Row 1 Col 1"));
      assertTrue(html.contains("Row 2 Col 2"));
    }
  }

  // ── mj-raw attribute tests ────────────────────────────────────────────────

  @Nested
  class MjRawAttributes {

    @Test
    void cssClassAttribute() {
      String html = render("""
          <mjml>
            <mj-body>
              <mj-section>
                <mj-column>
                  <mj-raw>
                    <div id="custom-raw">Custom raw HTML</div>
                  </mj-raw>
                </mj-column>
              </mj-section>
            </mj-body>
          </mjml>
          """);

      assertTrue(html.contains("id=\"custom-raw\""),
          "mj-raw should pass through HTML attributes");
      assertTrue(html.contains("Custom raw HTML"));
    }

    @Test
    void complexHtmlContent() {
      String html = render("""
          <mjml>
            <mj-body>
              <mj-section>
                <mj-column>
                  <mj-raw>
                    <table style="width:100%">
                      <tr>
                        <td align="center">
                          <a href="https://example.com" style="color: blue;">Link</a>
                        </td>
                      </tr>
                    </table>
                  </mj-raw>
                </mj-column>
              </mj-section>
            </mj-body>
          </mjml>
          """);

      assertTrue(html.contains("https://example.com"),
          "mj-raw should pass through href in nested HTML");
      assertTrue(html.contains("color: blue;"),
          "mj-raw should pass through inline styles");
    }

    @Test
    void emptyRawContent() {
      String html = render("""
          <mjml>
            <mj-body>
              <mj-section>
                <mj-column>
                  <mj-raw></mj-raw>
                  <mj-text>After empty raw</mj-text>
                </mj-column>
              </mj-section>
            </mj-body>
          </mjml>
          """);

      assertTrue(html.contains("After empty raw"),
          "Empty mj-raw should not break rendering");
    }
  }

  // ── mj-wrapper attribute tests ────────────────────────────────────────────

  @Nested
  class MjWrapperAttributes {

    @Test
    void borderAttribute() {
      String html = render("""
          <mjml>
            <mj-body>
              <mj-wrapper border="1px solid #ddd">
                <mj-section>
                  <mj-column>
                    <mj-text>Bordered wrapper</mj-text>
                  </mj-column>
                </mj-section>
              </mj-wrapper>
            </mj-body>
          </mjml>
          """);

      assertTrue(html.contains("Bordered wrapper"));
      assertTrue(html.contains("border"),
          "Wrapper should apply border attribute");
    }

    @Test
    void borderRadiusAttribute() {
      String html = render("""
          <mjml>
            <mj-body>
              <mj-wrapper border-radius="8px" background-color="#fff">
                <mj-section>
                  <mj-column>
                    <mj-text>Rounded wrapper</mj-text>
                  </mj-column>
                </mj-section>
              </mj-wrapper>
            </mj-body>
          </mjml>
          """);

      assertTrue(html.contains("Rounded wrapper"));
    }

    @Test
    void textAlignAttribute() {
      String html = render("""
          <mjml>
            <mj-body>
              <mj-wrapper text-align="center">
                <mj-section>
                  <mj-column>
                    <mj-text>Centered wrapper</mj-text>
                  </mj-column>
                </mj-section>
              </mj-wrapper>
            </mj-body>
          </mjml>
          """);

      assertTrue(html.contains("Centered wrapper"));
      assertTrue(html.contains("text-align:center"),
          "Wrapper should apply text-align");
    }

    @Test
    void cssClassAttribute() {
      String html = render("""
          <mjml>
            <mj-body>
              <mj-wrapper css-class="my-wrapper">
                <mj-section>
                  <mj-column>
                    <mj-text>Classed wrapper</mj-text>
                  </mj-column>
                </mj-section>
              </mj-wrapper>
            </mj-body>
          </mjml>
          """);

      assertTrue(html.contains("my-wrapper"),
          "Wrapper should apply css-class");
    }

    @Test
    void multipleSectionsInsideWrapper() {
      String html = render("""
          <mjml>
            <mj-body>
              <mj-wrapper background-color="#eee">
                <mj-section>
                  <mj-column>
                    <mj-text>Section 1</mj-text>
                  </mj-column>
                </mj-section>
                <mj-section>
                  <mj-column>
                    <mj-text>Section 2</mj-text>
                  </mj-column>
                </mj-section>
              </mj-wrapper>
            </mj-body>
          </mjml>
          """);

      assertTrue(html.contains("Section 1"));
      assertTrue(html.contains("Section 2"),
          "Wrapper should render multiple child sections");
    }
  }

  // ── mj-group attribute tests ──────────────────────────────────────────────

  @Nested
  class MjGroupAttributes {

    @Test
    void backgroundColorAttribute() {
      String html = render("""
          <mjml>
            <mj-body>
              <mj-section>
                <mj-group background-color="#f0f0f0">
                  <mj-column>
                    <mj-text>Bg group</mj-text>
                  </mj-column>
                  <mj-column>
                    <mj-text>Col 2</mj-text>
                  </mj-column>
                </mj-group>
              </mj-section>
            </mj-body>
          </mjml>
          """);

      assertTrue(html.contains("Bg group"));
      assertTrue(html.contains("#f0f0f0"),
          "Group should apply background-color");
    }

    @Test
    void threeColumnsInGroup() {
      String html = render("""
          <mjml>
            <mj-body>
              <mj-section>
                <mj-group>
                  <mj-column>
                    <mj-text>A</mj-text>
                  </mj-column>
                  <mj-column>
                    <mj-text>B</mj-text>
                  </mj-column>
                  <mj-column>
                    <mj-text>C</mj-text>
                  </mj-column>
                </mj-group>
              </mj-section>
            </mj-body>
          </mjml>
          """);

      assertTrue(html.contains("A"));
      assertTrue(html.contains("B"));
      assertTrue(html.contains("C"),
          "Group should render all three columns");
      // Three columns should get approximately 33.33% width
      assertTrue(html.contains("33."),
          "Three columns should each get ~33.33% width");
    }

    @Test
    void verticalAlignAttribute() {
      String html = render("""
          <mjml>
            <mj-body>
              <mj-section>
                <mj-group vertical-align="bottom">
                  <mj-column>
                    <mj-text>Bottom aligned</mj-text>
                  </mj-column>
                </mj-group>
              </mj-section>
            </mj-body>
          </mjml>
          """);

      assertTrue(html.contains("Bottom aligned"),
          "Group with vertical-align should render content");
    }

    @Test
    void widthAttribute() {
      String html = render("""
          <mjml>
            <mj-body>
              <mj-section>
                <mj-group width="50%">
                  <mj-column>
                    <mj-text>Half-width group</mj-text>
                  </mj-column>
                </mj-group>
                <mj-column>
                  <mj-text>Other column</mj-text>
                </mj-column>
              </mj-section>
            </mj-body>
          </mjml>
          """);

      assertTrue(html.contains("Half-width group"));
      assertTrue(html.contains("Other column"));
    }
  }

  // ── mj-body attribute tests ───────────────────────────────────────────────

  @Nested
  class MjBodyAttributes {

    @Test
    void cssClassAttribute() {
      String html = render("""
          <mjml>
            <mj-body css-class="email-body">
              <mj-section>
                <mj-column>
                  <mj-text>Body with class</mj-text>
                </mj-column>
              </mj-section>
            </mj-body>
          </mjml>
          """);

      assertTrue(html.contains("email-body"),
          "Body should apply css-class");
    }

    @Test
    void customContainerWidth() {
      String html = render("""
          <mjml>
            <mj-body width="480px">
              <mj-section>
                <mj-column>
                  <mj-text>Narrow body</mj-text>
                </mj-column>
              </mj-section>
            </mj-body>
          </mjml>
          """);

      assertTrue(html.contains("480px"),
          "Body should apply custom width");
    }

    @Test
    void emptyBodyRendersValidHtml() {
      String html = render("""
          <mjml>
            <mj-body>
            </mj-body>
          </mjml>
          """);

      assertTrue(html.contains("<!doctype html>"),
          "Empty body should still produce valid HTML structure");
      assertTrue(html.contains("</html>"));
    }

    @Test
    void bodyWithOnlySections() {
      String html = render("""
          <mjml>
            <mj-body>
              <mj-section>
                <mj-column>
                  <mj-text>First</mj-text>
                </mj-column>
              </mj-section>
              <mj-section>
                <mj-column>
                  <mj-text>Second</mj-text>
                </mj-column>
              </mj-section>
              <mj-section>
                <mj-column>
                  <mj-text>Third</mj-text>
                </mj-column>
              </mj-section>
            </mj-body>
          </mjml>
          """);

      assertTrue(html.contains("First"));
      assertTrue(html.contains("Second"));
      assertTrue(html.contains("Third"),
          "Body should render all child sections");
    }

    @Test
    void bodyWithDirectionConfig() {
      MjmlConfiguration config = MjmlConfiguration.builder()
          .direction(dev.jcputney.mjml.Direction.RTL)
          .build();

      String mjml = """
          <mjml>
            <mj-body>
              <mj-section>
                <mj-column>
                  <mj-text>RTL content</mj-text>
                </mj-column>
              </mj-section>
            </mj-body>
          </mjml>
          """;

      MjmlRenderResult result = MjmlRenderer.render(mjml, config);
      assertNotNull(result);
      assertTrue(result.html().contains("dir=\"rtl\""),
          "Body should apply RTL direction from config");
    }
  }
}
