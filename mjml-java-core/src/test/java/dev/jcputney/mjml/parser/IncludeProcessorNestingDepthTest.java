package dev.jcputney.mjml.parser;

import static org.junit.jupiter.api.Assertions.*;

import dev.jcputney.mjml.IncludeResolver;
import dev.jcputney.mjml.MjmlValidationException;
import org.junit.jupiter.api.Test;

/**
 * Tests that IncludeProcessor forwards maxNestingDepth to MjmlParser when parsing included MJML
 * fragments.
 */
class IncludeProcessorNestingDepthTest {

  @Test
  void includeRespectsMaxNestingDepth() {
    // A deeply nested MJML fragment that exceeds depth 3
    String deepFragment = "<mj-section><mj-column><mj-text>deep</mj-text></mj-column></mj-section>";
    IncludeResolver resolver = (path, ctx) -> deepFragment;

    // Set maxNestingDepth=3, which should be exceeded by the included content
    // (mjml > mj-body > mj-section > mj-column > mj-text = 5 levels when wrapped)
    IncludeProcessor processor = new IncludeProcessor(resolver, 0, 50, 3);

    // Create a document with an mj-include
    MjmlDocument doc =
        MjmlParser.parse("<mjml><mj-body><mj-include path=\"fragment.mjml\" /></mj-body></mjml>");

    assertThrows(MjmlValidationException.class, () -> processor.process(doc));
  }

  @Test
  void includeWorksWithSufficientNestingDepth() {
    String fragment = "<mj-section><mj-column><mj-text>ok</mj-text></mj-column></mj-section>";
    IncludeResolver resolver = (path, ctx) -> fragment;

    // Set maxNestingDepth high enough
    IncludeProcessor processor = new IncludeProcessor(resolver, 0, 50, 100);

    MjmlDocument doc =
        MjmlParser.parse("<mjml><mj-body><mj-include path=\"fragment.mjml\" /></mj-body></mjml>");

    // Should not throw
    assertDoesNotThrow(() -> processor.process(doc));
  }

  @Test
  void fullMjmlIncludeRespectsMaxNestingDepth() {
    String fullDoc =
        "<mjml><mj-body><mj-section><mj-column><mj-text>deep</mj-text></mj-column></mj-section></mj-body></mjml>";
    IncludeResolver resolver = (path, ctx) -> fullDoc;

    // depth 3 should not be enough for mjml > mj-body > mj-section > mj-column > mj-text
    IncludeProcessor processor = new IncludeProcessor(resolver, 0, 50, 3);

    MjmlDocument doc =
        MjmlParser.parse("<mjml><mj-body><mj-include path=\"full.mjml\" /></mj-body></mjml>");

    assertThrows(MjmlValidationException.class, () -> processor.process(doc));
  }
}
