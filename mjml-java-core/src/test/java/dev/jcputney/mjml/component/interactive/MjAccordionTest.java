package dev.jcputney.mjml.component.interactive;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlRenderer;
import org.junit.jupiter.api.Test;

/**
 * Tests for the mj-accordion component rendering.
 */
class MjAccordionTest {

  private String render(String mjml) {
    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertFalse(html.isEmpty());
    return html;
  }

  @Test
  void basicAccordionRendersWithChildren() {
    String html = render("""
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

    assertTrue(html.contains("Question 1"),
        "Should contain first accordion title");
    assertTrue(html.contains("Answer 1"),
        "Should contain first accordion text");
    assertTrue(html.contains("Question 2"),
        "Should contain second accordion title");
    assertTrue(html.contains("Answer 2"),
        "Should contain second accordion text");
    assertTrue(html.contains("mj-accordion"),
        "Should have mj-accordion class on the table");
    assertTrue(html.contains("checkbox"),
        "Should use checkbox hack for expand/collapse");
  }

  @Test
  void cssInjectionHappens() {
    String html = render("""
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

    assertTrue(html.contains("noinput.mj-accordion-checkbox"),
        "Should inject accordion CSS with noinput rule");
    assertTrue(html.contains("mj-accordion-content"),
        "Should inject CSS controlling accordion content visibility");
    assertTrue(html.contains("mj-accordion-more"),
        "Should inject CSS for more/less toggle icons");
  }

  @Test
  void multipleAccordionsDontDuplicateCss() {
    String html = render("""
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
    assertTrue(html.contains("First Accordion Title"),
        "Should render first accordion");
    assertTrue(html.contains("Second Accordion Title"),
        "Should render second accordion");

    // The accordion CSS should only appear once
    int firstIndex = html.indexOf("noinput.mj-accordion-checkbox");
    int lastIndex = html.lastIndexOf("noinput.mj-accordion-checkbox");
    assertTrue(firstIndex == lastIndex,
        "Accordion CSS should only be injected once, not duplicated for multiple accordions");
  }
}
