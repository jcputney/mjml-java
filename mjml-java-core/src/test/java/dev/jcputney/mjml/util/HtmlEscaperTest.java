package dev.jcputney.mjml.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class HtmlEscaperTest {

  @Test
  void nullReturnsNull() {
    assertNull(HtmlEscaper.escapeAttributeValue(null));
  }

  @Test
  void emptyReturnsEmpty() {
    assertEquals("", HtmlEscaper.escapeAttributeValue(""));
  }

  @Test
  void noSpecialCharsReturnsAsIs() {
    assertEquals("hello world", HtmlEscaper.escapeAttributeValue("hello world"));
  }

  @Test
  void escapesAmpersand() {
    assertEquals("a&amp;b", HtmlEscaper.escapeAttributeValue("a&b"));
  }

  @Test
  void escapesDoubleQuote() {
    assertEquals("a&quot;b", HtmlEscaper.escapeAttributeValue("a\"b"));
  }

  @Test
  void escapesLessThan() {
    assertEquals("a&lt;b", HtmlEscaper.escapeAttributeValue("a<b"));
  }

  @Test
  void escapesGreaterThan() {
    assertEquals("a&gt;b", HtmlEscaper.escapeAttributeValue("a>b"));
  }

  @Test
  void escapesCombinedChars() {
    assertEquals("&amp;&quot;&lt;&gt;", HtmlEscaper.escapeAttributeValue("&\"<>"));
  }

  @Test
  void ampersandInEntityNotDoubleEscaped() {
    // If the input already contains an entity, the & gets escaped
    assertEquals("&amp;amp;", HtmlEscaper.escapeAttributeValue("&amp;"));
  }

  @Test
  void xssPayloadEscaped() {
    assertEquals(
        "&quot;&gt;&lt;script&gt;alert(1)&lt;/script&gt;",
        HtmlEscaper.escapeAttributeValue("\"><script>alert(1)</script>"));
  }
}
