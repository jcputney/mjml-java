package dev.jcputney.mjml;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Negative and error handling tests for boundary conditions and invalid inputs. */
class ErrorHandlingTest {

  @Test
  void malformedMjmlThrowsParseException() {
    assertThrows(
        MjmlParseException.class,
        () -> MjmlRenderer.render("<mjml><mj-body><mj-section><mj-column>"));
  }

  @Test
  void nonMjmlRootThrowsParseException() {
    assertThrows(
        MjmlParseException.class, () -> MjmlRenderer.render("<html><body>Hello</body></html>"));
  }

  @Test
  void emptyBodyRenders() {
    String html = MjmlRenderer.render("<mjml><mj-body></mj-body></mjml>").html();
    assertNotNull(html);
    assertTrue(html.contains("<!doctype html>"));
  }

  @Test
  void emptyHeadRenders() {
    String html = MjmlRenderer.render("<mjml><mj-head></mj-head><mj-body></mj-body></mjml>").html();
    assertNotNull(html);
    assertTrue(html.contains("<!doctype html>"));
  }

  @Test
  void bodyOnlyNoHead() {
    String html =
        MjmlRenderer.render(
                // language=MJML
                """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>No head</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """)
            .html();
    assertTrue(html.contains("No head"));
  }

  @Test
  void unknownTagSkippedGracefully() {
    String html =
        assertDoesNotThrow(
            () ->
                MjmlRenderer.render(
                        // language=MJML
                        """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Before</mj-text>
                <mj-unknown-widget>Should be skipped</mj-unknown-widget>
                <mj-text>After</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """)
                    .html());
    assertTrue(html.contains("Before"));
    assertTrue(html.contains("After"));
  }

  @Test
  void inputSizeExceededThrowsValidation() {
    MjmlConfiguration config = MjmlConfiguration.builder().maxInputSize(10).build();
    String mjml = "<mjml><mj-body></mj-body></mjml>";
    assertThrows(MjmlValidationException.class, () -> MjmlRenderer.render(mjml, config));
  }

  @Test
  void blankInputThrows() {
    assertThrows(MjmlException.class, () -> MjmlRenderer.render("   "));
  }

  @Test
  void emptyStringThrows() {
    assertThrows(MjmlException.class, () -> MjmlRenderer.render(""));
  }

  @Test
  void sectionWithoutColumns() {
    // Section with no columns should still render without error
    String html =
        assertDoesNotThrow(
            () ->
                MjmlRenderer.render(
                        // language=MJML
                        """
        <mjml>
          <mj-body>
            <mj-section>
            </mj-section>
          </mj-body>
        </mjml>
        """)
                    .html());
    assertNotNull(html);
  }

  @Test
  void columnWithoutContent() {
    // Column with no content components should still render
    String html =
        assertDoesNotThrow(
            () ->
                MjmlRenderer.render(
                        // language=MJML
                        """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """)
                    .html());
    assertNotNull(html);
  }

  @Test
  void invalidAttributeValuesHandled() {
    // Invalid CSS values should not crash the renderer
    String html =
        assertDoesNotThrow(
            () ->
                MjmlRenderer.render(
                        // language=MJML
                        """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text padding="invalid" font-size="notapx">Content</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """)
                    .html());
    assertTrue(html.contains("Content"));
  }
}
