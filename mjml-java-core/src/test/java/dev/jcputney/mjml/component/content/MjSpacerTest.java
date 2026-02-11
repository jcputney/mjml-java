package dev.jcputney.mjml.component.content;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlRenderer;
import org.junit.jupiter.api.Test;

/** Tests for the mj-spacer component rendering. */
class MjSpacerTest {

  private String render(String mjml) {
    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertFalse(html.isEmpty());
    return html;
  }

  @Test
  void defaultSpacerRendersWithDefaultHeight() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-spacer />
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("height:0px"), "Default spacer should have 0px height per MJML spec");
    assertTrue(html.contains("line-height:0px"), "Default spacer should have matching line-height");
  }

  @Test
  void customHeightApplied() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-spacer height="50px" />
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("height:50px"), "Should use custom height of 50px");
    assertTrue(html.contains("line-height:50px"), "Line-height should match custom height");
  }

  @Test
  void containerBackgroundColorApplied() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-spacer height="30px" container-background-color="#eaeaea" />
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("#eaeaea"), "Should include the container background color");
  }

  @Test
  void figureSpaceCharacterPresent() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-spacer />
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(
        html.contains("&#8202;"), "Should contain the figure space character for spacer content");
  }
}
