package dev.jcputney.mjml.render;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlConfiguration;
import dev.jcputney.mjml.MjmlRenderer;
import dev.jcputney.mjml.MjmlRenderResult;
import dev.jcputney.mjml.MjmlValidationException;
import org.junit.jupiter.api.Test;

/**
 * Tests for the RenderPipeline orchestration logic.
 */
class RenderPipelineTest {

  private static final String MINIMAL_MJML = """
      <mjml>
        <mj-body>
          <mj-section>
            <mj-column>
              <mj-text>Hello</mj-text>
            </mj-column>
          </mj-section>
        </mj-body>
      </mjml>
      """;

  private String render(String mjml) {
    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertFalse(html.isEmpty());
    return html;
  }

  private String render(String mjml, MjmlConfiguration config) {
    String html = MjmlRenderer.render(mjml, config).html();
    assertNotNull(html);
    assertFalse(html.isEmpty());
    return html;
  }

  @Test
  void basicRenderProducesValidHtml() {
    String html = render(MINIMAL_MJML);
    assertTrue(html.contains("<!doctype html>"), "Should start with doctype");
    assertTrue(html.contains("<html"), "Should contain html tag");
    assertTrue(html.contains("</html>"), "Should close html tag");
    assertTrue(html.contains("Hello"), "Should contain body text");
  }

  @Test
  void throwsValidationExceptionWhenInputExceedsMaxSize() {
    MjmlConfiguration config = MjmlConfiguration.builder()
        .maxInputSize(10)
        .build();
    assertThrows(MjmlValidationException.class, () ->
        MjmlRenderer.render(MINIMAL_MJML, config));
  }

  @Test
  void acceptsInputAtExactMaxSizeBoundary() {
    int exactSize = MINIMAL_MJML.length();
    MjmlConfiguration config = MjmlConfiguration.builder()
        .maxInputSize(exactSize)
        .build();
    assertDoesNotThrow(() -> MjmlRenderer.render(MINIMAL_MJML, config));
  }

  @Test
  void handlesNullIncludeResolverGracefully() {
    MjmlConfiguration config = MjmlConfiguration.builder().build();
    // No include resolver set, but doc has no includes — should work fine
    assertDoesNotThrow(() -> MjmlRenderer.render(MINIMAL_MJML, config));
  }

  @Test
  void mergesAdjacentMsoSectionTransitions() {
    String mjml = """
        <mjml>
          <mj-body>
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
          </mj-body>
        </mjml>
        """;
    String html = render(mjml);
    // Adjacent sections should have merged MSO transitions (no close-then-open pattern)
    assertFalse(
        html.contains("<!--[if mso | IE]></td></tr></table><![endif]-->\n    <!--[if mso | IE]><table "),
        "Adjacent MSO section close/open should be merged");
  }

  @Test
  void mergesHeroVmlTransitions() {
    String mjml = """
        <mjml>
          <mj-body>
            <mj-hero>
              <mj-text>Hero content</mj-text>
            </mj-hero>
            <mj-section>
              <mj-column>
                <mj-text>After hero</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;
    String html = render(mjml);
    // Hero + section should have merged VML transitions
    assertFalse(
        html.contains("<!--[if mso | IE]></v:textbox></v:rect></td></tr></table><![endif]-->\n    <!--[if mso | IE]><table "),
        "Hero-to-section VML close/open should be merged");
  }

  @Test
  void preservesNonAdjacentMsoSections() {
    // With a wrapper between two sections, the MSO transitions should NOT be merged
    String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Section 1</mj-text>
              </mj-column>
            </mj-section>
            <mj-wrapper>
              <mj-section>
                <mj-column>
                  <mj-text>Wrapped</mj-text>
                </mj-column>
              </mj-section>
            </mj-wrapper>
          </mj-body>
        </mjml>
        """;
    String html = render(mjml);
    // There should be multiple independent MSO blocks
    assertTrue(html.contains("<!--[if mso | IE]>"),
        "Should contain MSO conditionals");
  }

  @Test
  void registryRegistersAllExpectedComponentTags() {
    // Smoke test: render a doc with many component types to verify registration
    String mjml = """
        <mjml>
          <mj-head>
            <mj-title>Test</mj-title>
            <mj-preview>Preview</mj-preview>
            <mj-breakpoint width="600px" />
            <mj-font name="Roboto" href="https://fonts.googleapis.com/css?family=Roboto" />
            <mj-style>.test { color: red; }</mj-style>
            <mj-attributes>
              <mj-all font-family="Arial" />
            </mj-attributes>
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Text</mj-text>
                <mj-image src="https://example.com/img.jpg" width="200px" />
                <mj-button href="https://example.com">Click</mj-button>
                <mj-divider />
                <mj-spacer height="20px" />
                <mj-table><tr><td>Cell</td></tr></mj-table>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;
    String html = render(mjml);
    assertTrue(html.contains("Text"), "Should render mj-text");
    assertTrue(html.contains("Click"), "Should render mj-button");
    assertTrue(html.contains("Cell"), "Should render mj-table");
  }

  @Test
  void headComponentsProcessBeforeBody() {
    String mjml = """
        <mjml>
          <mj-head>
            <mj-attributes>
              <mj-text color="#ff0000" />
            </mj-attributes>
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Styled</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;
    String html = render(mjml);
    assertTrue(html.contains("#ff0000") || html.contains("ff0000"),
        "mj-attributes color should be applied to mj-text");
  }

  @Test
  void mjRawContentWithSelfClosingAndStylePreservedWithoutInlineStyles() {
    // Verifies that mj-raw content containing "/>", style="" is not mangled
    // when no inline styles are configured (the post-processing rewrites in
    // RenderPipeline only apply when inline CSS is present).
    String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-raw>
                  <img src="https://example.com/pixel.gif" style="" />
                  <input type="hidden" value="test" />
                </mj-raw>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;
    String html = render(mjml);
    // Without inline styles, the post-processing rewrites should NOT run,
    // so self-closing tags and empty style attributes are preserved.
    assertTrue(html.contains("/>"),
        "Self-closing tags in mj-raw should be preserved when no inline styles");
    assertTrue(html.contains("style=\"\""),
        "Empty style attributes in mj-raw should be preserved when no inline styles");
  }

  @Test
  void cssInliningAppliesWhenInlineStylesPresent() {
    String mjml = """
        <mjml>
          <mj-head>
            <mj-style inline="inline">
              .custom-class { background-color: #00ff00; }
            </mj-style>
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text css-class="custom-class">Inline styled</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;
    String html = render(mjml);
    // When inline CSS is applied, self-closing tags lose the slash (juice behavior emulation)
    assertFalse(html.contains(" />"),
        "CSS inlining should remove self-closing tag slashes");
  }
}
