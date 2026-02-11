package dev.jcputney.mjml.component;

import static org.junit.jupiter.api.Assertions.*;

import dev.jcputney.mjml.MjmlRenderResult;
import dev.jcputney.mjml.MjmlRenderer;
import org.junit.jupiter.api.Test;

/**
 * Tests that unknown MJML tags are handled gracefully (logged and skipped) rather than causing a
 * NullPointerException or render failure.
 */
class ComponentRegistryUnknownTagTest {

  @Test
  void unknownTagIsSkippedGracefully() {
    String mjml =
        // language=MJML
        """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-unknown>This tag is not registered</mj-unknown>
                <mj-text>This should still render</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    // Should not throw — unknown tags are skipped with a warning
    MjmlRenderResult result = MjmlRenderer.render(mjml);
    assertNotNull(result);
    assertTrue(
        result.html().contains("This should still render"),
        "Known tags should still render even when unknown tags are present");
  }

  @Test
  void unknownTagInHeadIsSkippedGracefully() {
    String mjml =
        // language=MJML
        """
        <mjml>
          <mj-head>
            <mj-unknown-head>something</mj-unknown-head>
            <mj-title>My Title</mj-title>
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Hello</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    MjmlRenderResult result = MjmlRenderer.render(mjml);
    assertNotNull(result);
    assertEquals("My Title", result.title());
  }
}
