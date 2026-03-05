package dev.jcputney.mjml.component.interactive;

import static org.junit.jupiter.api.Assertions.*;

import dev.jcputney.mjml.MjmlRenderer;
import org.junit.jupiter.api.Test;

/** Tests for the mj-accordion component rendering. */
class MjAccordionTest {

  private String render(String mjml) {
    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertFalse(html.isEmpty());
    return html;
  }

  @Test
  void basicAccordionRendersWithChildren() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-accordion>
                  <mj-accordion-element>
                    <mj-accordion-title>Question 1</mj-accordion-title>
                    <mj-accordion-text>Answer 1</mj-accordion-text>
                  </mj-accordion-element>
                  <mj-accordion-element>
                    <mj-accordion-title>Question 2</mj-accordion-title>
                    <mj-accordion-text>Answer 2</mj-accordion-text>
                  </mj-accordion-element>
                </mj-accordion>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("Question 1"), "Should contain first accordion title");
    assertTrue(html.contains("Answer 1"), "Should contain first accordion text");
    assertTrue(html.contains("Question 2"), "Should contain second accordion title");
    assertTrue(html.contains("Answer 2"), "Should contain second accordion text");
    assertTrue(html.contains("mj-accordion"), "Should have mj-accordion class on the table");
    assertTrue(html.contains("checkbox"), "Should use checkbox hack for expand/collapse");
  }

  @Test
  void cssInjectionHappens() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-accordion>
                  <mj-accordion-element>
                    <mj-accordion-title>Title</mj-accordion-title>
                    <mj-accordion-text>Content</mj-accordion-text>
                  </mj-accordion-element>
                </mj-accordion>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(
        html.contains("noinput.mj-accordion-checkbox"),
        "Should inject accordion CSS with noinput rule");
    assertTrue(
        html.contains("mj-accordion-content"),
        "Should inject CSS controlling accordion content visibility");
    assertTrue(html.contains("mj-accordion-more"), "Should inject CSS for more/less toggle icons");
  }

  @Test
  void multipleAccordionsDontDuplicateCss() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-accordion>
                  <mj-accordion-element>
                    <mj-accordion-title>First Accordion Title</mj-accordion-title>
                    <mj-accordion-text>First Accordion Content</mj-accordion-text>
                  </mj-accordion-element>
                </mj-accordion>
                <mj-accordion>
                  <mj-accordion-element>
                    <mj-accordion-title>Second Accordion Title</mj-accordion-title>
                    <mj-accordion-text>Second Accordion Content</mj-accordion-text>
                  </mj-accordion-element>
                </mj-accordion>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    // Both accordions should render
    assertTrue(html.contains("First Accordion Title"), "Should render first accordion");
    assertTrue(html.contains("Second Accordion Title"), "Should render second accordion");

    // The accordion CSS should only appear once
    int firstIndex = html.indexOf("noinput.mj-accordion-checkbox");
    int lastIndex = html.lastIndexOf("noinput.mj-accordion-checkbox");
    assertEquals(
        firstIndex,
        lastIndex,
        "Accordion CSS should only be injected once, not duplicated for multiple accordions");
  }

  @Test
  void rightIconPosition() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-accordion icon-position="right">
                  <mj-accordion-element>
                    <mj-accordion-title>Right Icon Title</mj-accordion-title>
                    <mj-accordion-text>Right Icon Content</mj-accordion-text>
                  </mj-accordion-element>
                </mj-accordion>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("Right Icon Title"), "Should render the title content");
    assertTrue(html.contains("Right Icon Content"), "Should render the text content");
    assertTrue(html.contains("mj-accordion-ico"), "Should render the icon td");
  }

  @Test
  void leftIconPosition() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-accordion icon-position="left">
                  <mj-accordion-element>
                    <mj-accordion-title>Left Icon Title</mj-accordion-title>
                    <mj-accordion-text>Left Icon Content</mj-accordion-text>
                  </mj-accordion-element>
                </mj-accordion>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("Left Icon Title"), "Should render the title content");
    assertTrue(html.contains("Left Icon Content"), "Should render the text content");
    assertTrue(html.contains("mj-accordion-ico"), "Should render the icon td for left position");
  }

  @Test
  void customIconUrls() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-accordion
                  icon-wrapped-url="https://example.com/plus.png"
                  icon-unwrapped-url="https://example.com/minus.png"
                  icon-wrapped-alt="Expand"
                  icon-unwrapped-alt="Collapse">
                  <mj-accordion-element>
                    <mj-accordion-title>Custom Icons</mj-accordion-title>
                    <mj-accordion-text>Content here</mj-accordion-text>
                  </mj-accordion-element>
                </mj-accordion>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("example.com/plus.png"), "Should use custom wrapped icon URL");
    assertTrue(html.contains("example.com/minus.png"), "Should use custom unwrapped icon URL");
    assertTrue(html.contains("Expand"), "Should use custom wrapped alt text");
    assertTrue(html.contains("Collapse"), "Should use custom unwrapped alt text");
  }

  @Test
  void missingTitleChildStillRenders() {
    String html =
        render(
            // language=MJML
            """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-accordion>
                  <mj-accordion-element>
                    <mj-accordion-text>Text without title</mj-accordion-text>
                  </mj-accordion-element>
                </mj-accordion>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    assertTrue(html.contains("Text without title"), "Should render accordion text content");
    assertTrue(
        html.contains("mj-accordion-element"), "Should still render the accordion element label");
  }
}
