package dev.jcputney.mjml.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MjmlPreprocessorTest {

  @Test
  void wrapsTextContentInCdata() {
    String input = "<mjml><mj-body><mj-text>Hello <b>World</b></mj-text></mj-body></mjml>";
    String result = MjmlPreprocessor.preprocess(input);
    assertTrue(result.contains("<![CDATA[Hello <b>World</b>]]>"));
  }

  @Test
  void wrapsButtonContentInCdata() {
    String input = "<mjml><mj-body><mj-button href=\"#\">Click <em>me</em></mj-button></mj-body></mjml>";
    String result = MjmlPreprocessor.preprocess(input);
    assertTrue(result.contains("<![CDATA[Click <em>me</em>]]>"));
  }

  @Test
  void wrapsContentBetweenOpenAndCloseTags() {
    // A self-closing tag followed by an open/close pair - verify the
    // open/close pair content gets wrapped correctly
    String input = "<mjml><mj-body><mj-section><mj-column><mj-text>Hi</mj-text></mj-column></mj-section></mj-body></mjml>";
    String result = MjmlPreprocessor.preprocess(input);
    assertTrue(result.contains("<![CDATA[Hi]]>"));
  }

  @Test
  void preservesHtmlEntitiesInsideCdata() {
    // Entities inside ending tag content (CDATA-wrapped) are preserved as-is
    String input = "<mjml><mj-body><mj-text>&nbsp;&copy;</mj-text></mj-body></mjml>";
    String result = MjmlPreprocessor.preprocess(input);
    assertTrue(result.contains("&nbsp;"));
    assertTrue(result.contains("&copy;"));
  }

  @Test
  void replacesHtmlEntitiesOutsideCdata() {
    // Entities outside ending tag content are replaced with numeric references
    String input = "<mjml><mj-head><mj-title>&nbsp;</mj-title></mj-head></mjml>";
    String result = MjmlPreprocessor.preprocess(input);
    assertTrue(result.contains("&#160;"));
  }

  @Test
  void preservesXmlEntities() {
    String input = "<mjml><mj-body><mj-text>&amp; &lt; &gt;</mj-text></mj-body></mjml>";
    String result = MjmlPreprocessor.preprocess(input);
    // XML entities should remain - they were not in our entity table
    assertTrue(result.contains("&amp;"));
    assertTrue(result.contains("&lt;"));
    assertTrue(result.contains("&gt;"));
  }

  @Test
  void handlesEmptyContent() {
    String input = "<mjml><mj-body><mj-text></mj-text></mj-body></mjml>";
    String result = MjmlPreprocessor.preprocess(input);
    // Empty content should not be wrapped
    assertFalse(result.contains("CDATA"));
  }

  @Test
  void handlesNullInput() {
    assertEquals(null, MjmlPreprocessor.preprocess(null));
    assertEquals("", MjmlPreprocessor.preprocess(""));
  }
}
