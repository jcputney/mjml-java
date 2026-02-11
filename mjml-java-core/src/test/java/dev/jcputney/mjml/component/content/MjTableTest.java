package dev.jcputney.mjml.component.content;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlRenderer;
import org.junit.jupiter.api.Test;

/** Tests for the mj-table component rendering. */
class MjTableTest {

  private String render(String mjml) {
    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertFalse(html.isEmpty());
    return html;
  }

  @Test
  void htmlTablePassthrough() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-table>
                  <tr>
                    <td>Cell 1</td>
                    <td>Cell 2</td>
                  </tr>
                </mj-table>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("<table"), "Should render a table element");
    assertTrue(html.contains("Cell 1"), "Should pass through cell content");
    assertTrue(html.contains("Cell 2"), "Should pass through second cell content");
    assertTrue(html.contains("<tr"), "Should pass through tr elements");
    assertTrue(html.contains("<td"), "Should pass through td elements");
  }

  @Test
  void customWidthApplied() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-table width="100%">
                  <tr><td>Data</td></tr>
                </mj-table>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("width:100%"), "Should apply custom width style");
  }

  @Test
  void tableAttributesRendered() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-table cellpadding="5" cellspacing="0">
                  <tr><td>Padded</td></tr>
                </mj-table>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("cellpadding=\"5\""), "Should include cellpadding attribute");
    assertTrue(html.contains("cellspacing=\"0\""), "Should include cellspacing attribute");
  }
}
