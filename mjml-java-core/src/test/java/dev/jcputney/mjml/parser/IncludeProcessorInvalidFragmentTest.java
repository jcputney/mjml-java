package dev.jcputney.mjml.parser;

import static org.junit.jupiter.api.Assertions.*;

import dev.jcputney.mjml.IncludeResolver;
import dev.jcputney.mjml.MjmlParseException;
import dev.jcputney.mjml.ResolverContext;
import org.junit.jupiter.api.Test;

/**
 * Tests that invalid MJML fragments in includes propagate exceptions
 * instead of silently converting to mj-raw.
 */
class IncludeProcessorInvalidFragmentTest {

  @Test
  void invalidMjmlFragmentThrowsInsteadOfBecomingRaw() {
    // Invalid XML (unclosed tag)
    String invalidFragment = "<mj-section><mj-column><mj-text>broken";
    IncludeResolver resolver = (path, ctx) -> invalidFragment;

    IncludeProcessor processor = new IncludeProcessor(resolver, 0, 50, 100);

    MjmlDocument doc = MjmlParser.parse(
        "<mjml><mj-body><mj-include path=\"bad.mjml\" /></mj-body></mjml>");

    // Should throw instead of silently converting to mj-raw
    assertThrows(MjmlParseException.class, () -> processor.process(doc));
  }

  @Test
  void validFragmentStillWorks() {
    String validFragment = "<mj-section><mj-column><mj-text>hello</mj-text></mj-column></mj-section>";
    IncludeResolver resolver = (path, ctx) -> validFragment;

    IncludeProcessor processor = new IncludeProcessor(resolver, 0, 50, 100);

    MjmlDocument doc = MjmlParser.parse(
        "<mjml><mj-body><mj-include path=\"good.mjml\" /></mj-body></mjml>");

    assertDoesNotThrow(() -> processor.process(doc));

    // Verify the include was replaced with actual content
    MjmlNode body = doc.getBody();
    assertNotNull(body);
    // Should have the section as a child, not mj-include or mj-raw
    boolean hasSection = body.getChildren().stream()
        .anyMatch(n -> "mj-section".equals(n.getTagName()));
    assertTrue(hasSection, "Fragment should have been parsed as MJML components");
  }
}
