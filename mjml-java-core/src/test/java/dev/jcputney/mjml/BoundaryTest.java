package dev.jcputney.mjml;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Boundary / edge-case tests for input validation limits.
 */
class BoundaryTest {

  private static final String MINIMAL_MJML = "<mjml><mj-body></mj-body></mjml>";

  @Test
  void inputExactlyAtMaxSizeIsAccepted() {
    // Create input exactly at the limit
    int limit = MINIMAL_MJML.length();
    MjmlConfiguration config = MjmlConfiguration.builder()
        .maxInputSize(limit)
        .build();

    MjmlRenderResult result = MjmlRenderer.render(MINIMAL_MJML, config);
    assertNotNull(result.html());
  }

  @Test
  void inputOneCharOverMaxSizeIsRejected() {
    int limit = MINIMAL_MJML.length() - 1;
    MjmlConfiguration config = MjmlConfiguration.builder()
        .maxInputSize(limit)
        .build();

    MjmlValidationException ex = assertThrows(MjmlValidationException.class,
        () -> MjmlRenderer.render(MINIMAL_MJML, config));
    assertTrue(ex.getMessage().contains("exceeds maximum"),
        "Error message should mention exceeding maximum");
  }

  @Test
  void minimalValidInputIsAccepted() {
    // Single character isn't valid MJML, but the smallest valid MJML should work
    assertDoesNotThrow(() -> {
      MjmlRenderResult result = MjmlRenderer.render(MINIMAL_MJML);
      assertNotNull(result.html());
    });
  }

  @Test
  void nestingDepthAtLimitIsRejected() {
    // Set nesting depth to 3 and create MJML that exceeds it
    // mjml > mj-body > mj-section > mj-column = 4 levels deep
    MjmlConfiguration config = MjmlConfiguration.builder()
        .maxNestingDepth(3)
        .build();

    String deepMjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Deep</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    assertThrows(MjmlValidationException.class,
        () -> MjmlRenderer.render(deepMjml, config),
        "Should reject input exceeding nesting depth limit");
  }

  @Test
  void normalNestingDepthIsAccepted() {
    // Default limit is 100, normal MJML should be well within that
    MjmlConfiguration config = MjmlConfiguration.builder()
        .maxNestingDepth(100)
        .build();

    String normalMjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Normal depth</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    MjmlRenderResult result = MjmlRenderer.render(normalMjml, config);
    assertNotNull(result.html());
    assertTrue(result.html().contains("Normal depth"));
  }

  @Test
  void maxInputSizeZeroOrNegativeThrowsOnBuild() {
    assertThrows(IllegalArgumentException.class,
        () -> MjmlConfiguration.builder().maxInputSize(0).build());
    assertThrows(IllegalArgumentException.class,
        () -> MjmlConfiguration.builder().maxInputSize(-1).build());
  }
}
