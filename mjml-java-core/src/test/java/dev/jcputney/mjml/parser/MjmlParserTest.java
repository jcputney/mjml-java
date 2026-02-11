package dev.jcputney.mjml.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.jcputney.mjml.MjmlException;
import org.junit.jupiter.api.Test;

class MjmlParserTest {

  @Test
  void parsesSimpleDocument() {
    String mjml =
        "<mjml><mj-body><mj-section><mj-column><mj-text>Hello</mj-text></mj-column></mj-section></mj-body></mjml>";
    MjmlDocument doc = MjmlParser.parse(mjml);

    assertNotNull(doc);
    assertNotNull(doc.root());
    assertEquals("mjml", doc.root().getTagName());
    assertNotNull(doc.getBody());
    assertEquals("mj-body", doc.getBody().getTagName());
  }

  @Test
  void parsesHeadAndBody() {
    String mjml = "<mjml><mj-head><mj-title>Test</mj-title></mj-head><mj-body></mj-body></mjml>";
    MjmlDocument doc = MjmlParser.parse(mjml);

    assertNotNull(doc.getHead());
    assertNotNull(doc.getBody());
    assertEquals("mj-head", doc.getHead().getTagName());
  }

  @Test
  void parsesAttributes() {
    String mjml = "<mjml><mj-body background-color=\"#ffffff\" width=\"500px\"></mj-body></mjml>";
    MjmlDocument doc = MjmlParser.parse(mjml);

    MjmlNode body = doc.getBody();
    assertEquals("#ffffff", body.getAttribute("background-color"));
    assertEquals("500px", body.getAttribute("width"));
  }

  @Test
  void parsesHtmlContentInText() {
    String mjml =
        "<mjml><mj-body><mj-section><mj-column><mj-text>Hello <b>World</b></mj-text></mj-column></mj-section></mj-body></mjml>";
    MjmlDocument doc = MjmlParser.parse(mjml);

    MjmlNode section = doc.getBody().getFirstChildByTag("mj-section");
    MjmlNode column = section.getFirstChildByTag("mj-column");
    MjmlNode text = column.getFirstChildByTag("mj-text");
    assertNotNull(text);
    // Content should be preserved via CDATA
    String inner = text.getInnerHtml();
    assertNotNull(inner);
    assertEquals("Hello <b>World</b>", inner.trim());
  }

  @Test
  void throwsOnNullInput() {
    assertThrows(MjmlException.class, () -> MjmlParser.parse(null));
  }

  @Test
  void throwsOnEmptyInput() {
    assertThrows(MjmlException.class, () -> MjmlParser.parse(""));
  }

  @Test
  void throwsOnNonMjmlRoot() {
    assertThrows(MjmlException.class, () -> MjmlParser.parse("<html><body></body></html>"));
  }

  @Test
  void parsesMultipleChildren() {
    String mjml =
        "<mjml><mj-body><mj-section><mj-column><mj-text>A</mj-text></mj-column><mj-column><mj-text>B</mj-text></mj-column></mj-section></mj-body></mjml>";
    MjmlDocument doc = MjmlParser.parse(mjml);

    MjmlNode section = doc.getBody().getFirstChildByTag("mj-section");
    assertEquals(2, section.getChildrenByTag("mj-column").size());
  }
}
