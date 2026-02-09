package dev.jcputney.mjml.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    assertThrows(IllegalArgumentException.class, () -> MjmlPreprocessor.preprocess(null));
    assertEquals("", MjmlPreprocessor.preprocess(""));
  }

  // --- New tests ---

  @Test
  void preventsCdataInjectionViaClosingSequence() {
    // Content containing ]]> should be escaped to prevent CDATA injection
    String input = "<mjml><mj-body><mj-text>Injected ]]> content</mj-text></mj-body></mjml>";
    String result = MjmlPreprocessor.preprocess(input);
    // The ]]> inside should be split: ]]]]><![CDATA[>
    assertTrue(result.contains("<![CDATA["), "Should contain CDATA wrapper");
    assertFalse(result.contains("<![CDATA[Injected ]]> content]]>"),
        "Should not contain unescaped ]]> inside CDATA");
    assertTrue(result.contains("]]]]><![CDATA[>"),
        "Should escape ]]> as ]]]]><![CDATA[>");
  }

  @Test
  void doesNotDoubleWrapExistingCdata() {
    // Content that is already CDATA-wrapped should not be double-wrapped
    String input = "<mjml><mj-body><mj-text><![CDATA[Already wrapped]]></mj-text></mj-body></mjml>";
    String result = MjmlPreprocessor.preprocess(input);
    // Should not contain nested CDATA (i.e. <![CDATA[<![CDATA[)
    assertFalse(result.contains("<![CDATA[<![CDATA["),
        "Should not double-wrap CDATA content");
    assertTrue(result.contains("<![CDATA[Already wrapped]]>"),
        "Original CDATA should be preserved");
  }

  @Test
  void selfClosingEndingTagNotWrapped() {
    // Self-closing ending tags like <mj-text /> should not trigger CDATA wrapping
    String input = "<mjml><mj-body><mj-text /></mj-body></mjml>";
    String result = MjmlPreprocessor.preprocess(input);
    assertFalse(result.contains("CDATA"), "Self-closing tag should not have CDATA");
    assertTrue(result.contains("<mj-text />"), "Self-closing tag should be preserved");
  }

  @Test
  void handlesUnterminatedCdataInEntityReplacement() {
    // If there's an unterminated CDATA section, entity replacement should handle it gracefully
    String input = "<mjml><mj-body><mj-text>Start <![CDATA[unterminated</mj-text></mj-body></mjml>";
    // The preprocessor should not crash on this input
    String result = MjmlPreprocessor.preprocess(input);
    assertFalse(result.isEmpty(), "Should produce output even with unterminated CDATA");
  }

  @Test
  void wrapsNavbarLinkContent() {
    // mj-navbar-link is an ending tag and should have its content CDATA-wrapped
    String input = "<mjml><mj-body><mj-navbar-link href=\"#\">Link <b>Text</b></mj-navbar-link></mj-body></mjml>";
    String result = MjmlPreprocessor.preprocess(input);
    assertTrue(result.contains("<![CDATA[Link <b>Text</b>]]>"),
        "mj-navbar-link content should be CDATA-wrapped");
  }

  @Test
  void wrapsAccordionTitleContent() {
    // mj-accordion-title is an ending tag
    String input = "<mjml><mj-body><mj-accordion-title>Title <em>content</em></mj-accordion-title></mj-body></mjml>";
    String result = MjmlPreprocessor.preprocess(input);
    assertTrue(result.contains("<![CDATA[Title <em>content</em>]]>"),
        "mj-accordion-title content should be CDATA-wrapped");
  }

  @Test
  void wrapsAccordionTextContent() {
    // mj-accordion-text is an ending tag
    String input = "<mjml><mj-body><mj-accordion-text>Body <strong>text</strong></mj-accordion-text></mj-body></mjml>";
    String result = MjmlPreprocessor.preprocess(input);
    assertTrue(result.contains("<![CDATA[Body <strong>text</strong>]]>"),
        "mj-accordion-text content should be CDATA-wrapped");
  }

  @Test
  void doesNotWrapNonEndingTagContent() {
    // mj-section is NOT an ending tag - its content should not be CDATA-wrapped
    String input = "<mjml><mj-body><mj-section><mj-column /></mj-section></mj-body></mjml>";
    String result = MjmlPreprocessor.preprocess(input);
    assertFalse(result.contains("CDATA"),
        "Non-ending tag mj-section should not have CDATA-wrapped content");
  }
}
