package dev.jcputney.mjml.css;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class HtmlDocumentParserTest {

  @Test
  void parsesSimpleTag() {
    HtmlElement root = HtmlDocumentParser.parse("<div>hello</div>");
    assertEquals(1, root.getChildren().size());
    assertEquals("div", root.getChildren().get(0).getTagName());
  }

  @Test
  void parsesNestedTags() {
    HtmlElement root = HtmlDocumentParser.parse("<div><span>text</span></div>");
    HtmlElement div = root.getChildren().get(0);
    assertEquals("div", div.getTagName());
    assertEquals(1, div.getChildren().size());
    assertEquals("span", div.getChildren().get(0).getTagName());
  }

  @Test
  void parsesQuotedAttributes() {
    HtmlElement root = HtmlDocumentParser.parse(
        "<div class=\"foo\" id=\"bar\">text</div>");
    HtmlElement div = root.getChildren().get(0);
    assertEquals("foo", div.getAttribute("class"));
    assertEquals("bar", div.getAttribute("id"));
  }

  @Test
  void parsesSingleQuotedAttributes() {
    HtmlElement root = HtmlDocumentParser.parse(
        "<div class='foo'>text</div>");
    HtmlElement div = root.getChildren().get(0);
    assertEquals("foo", div.getAttribute("class"));
  }

  @Test
  void parsesBooleanAttributes() {
    HtmlElement root = HtmlDocumentParser.parse(
        "<input disabled readonly />");
    HtmlElement input = root.getChildren().get(0);
    assertEquals("", input.getAttribute("disabled"));
    assertEquals("", input.getAttribute("readonly"));
  }

  @Test
  void parsesSelfClosingTags() {
    HtmlElement root = HtmlDocumentParser.parse("<img src=\"test.png\" />");
    assertEquals(1, root.getChildren().size());
    assertEquals("img", root.getChildren().get(0).getTagName());
  }

  @Test
  void parsesVoidElements() {
    HtmlElement root = HtmlDocumentParser.parse(
        "<div><br><hr><img src=\"x\"></div>");
    HtmlElement div = root.getChildren().get(0);
    // br, hr, img are void elements and should be children of div
    assertEquals(3, div.getChildren().size());
  }

  @Test
  void skipsHtmlComments() {
    HtmlElement root = HtmlDocumentParser.parse(
        "<div><!-- comment --><span>text</span></div>");
    HtmlElement div = root.getChildren().get(0);
    assertEquals(1, div.getChildren().size());
    assertEquals("span", div.getChildren().get(0).getTagName());
  }

  @Test
  void skipsMsoConditionalComments() {
    HtmlElement root = HtmlDocumentParser.parse(
        "<div><!--[if mso]><table><tr><td><![endif]--><span>text</span></div>");
    HtmlElement div = root.getChildren().get(0);
    assertEquals(1, div.getChildren().size());
    assertEquals("span", div.getChildren().get(0).getTagName());
  }

  @Test
  void skipsDoctype() {
    HtmlElement root = HtmlDocumentParser.parse(
        "<!doctype html><html><body>text</body></html>");
    HtmlElement html = root.getChildren().get(0);
    assertEquals("html", html.getTagName());
  }

  @Test
  void extractsStyleBlocks() {
    String html = "<html><head><style type=\"text/css\">.foo { color: red; }</style></head>"
        + "<body><div>text</div></body></html>";
    HtmlDocumentParser.StyleExtractionResult result = HtmlDocumentParser.extractStyles(html);
    assertTrue(result.css().contains(".foo { color: red; }"));
    assertFalse(result.html().contains("<style"));
  }

  @Test
  void toleratesMismatchedTags() {
    // Should not throw
    HtmlElement root = HtmlDocumentParser.parse(
        "<div><span>text</div>");
    assertNotNull(root);
    assertEquals(1, root.getChildren().size());
  }

  @Test
  void tracksPositionInfo() {
    String html = "<div class=\"test\">text</div>";
    HtmlElement root = HtmlDocumentParser.parse(html);
    HtmlElement div = root.getChildren().get(0);
    assertTrue(div.hasPositionInfo());
    assertEquals(0, div.getTagStart());
    assertTrue(div.getTagEnd() > 0);
  }

  @Test
  void parsesEmptyInput() {
    HtmlElement root = HtmlDocumentParser.parse("");
    assertEquals(0, root.getChildren().size());
  }

  @Test
  void parsesNullInput() {
    HtmlElement root = HtmlDocumentParser.parse(null);
    assertEquals(0, root.getChildren().size());
  }

  @Test
  void skipsStyleContentFromParsing() {
    HtmlElement root = HtmlDocumentParser.parse(
        "<style><div>not a real div</div></style><div>real</div>");
    // The style element content should not be parsed as child elements
    List<HtmlElement> allElements = root.allDescendants();
    long divCount = allElements.stream()
        .filter(e -> "div".equals(e.getTagName()))
        .count();
    assertEquals(1, divCount, "Only the real div should be parsed");
  }
}
