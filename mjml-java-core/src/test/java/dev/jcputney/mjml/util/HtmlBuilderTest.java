
package dev.jcputney.mjml.util;

import static dev.jcputney.mjml.util.HtmlBuilder.attrIf;
import static dev.jcputney.mjml.util.HtmlBuilder.attrs;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HtmlBuilderTest {

  @Test
  void openAndCloseProducesIndentedTags() {
    HtmlBuilder html = new HtmlBuilder();
    html.open("div");
    html.open("p");
    html.close("p");
    html.close("div");

    assertEquals("<div>\n  <p>\n  </p>\n</div>\n", html.toString());
  }

  @Test
  void openWithAttributesFormatsCorrectly() {
    HtmlBuilder html = new HtmlBuilder();
    html.open("table", attrs("border", "0", "role", "presentation"));
    html.close("table");

    assertEquals("<table border=\"0\" role=\"presentation\">\n</table>\n", html.toString());
  }

  @Test
  void startingIndentOffsetsAllOutput() {
    HtmlBuilder html = new HtmlBuilder(4);
    html.open("div");
    html.close("div");

    assertEquals("    <div>\n    </div>\n", html.toString());
  }

  @Test
  void inlineContentOnSameLine() {
    HtmlBuilder html = new HtmlBuilder();
    html.openInline("a", attrs("href", "https://example.com"));
    html.text("Click me");
    html.closeInlineLn("a");

    assertEquals("<a href=\"https://example.com\">Click me</a>\n", html.toString());
  }

  @Test
  void nestedInlineWithinBlock() {
    HtmlBuilder html = new HtmlBuilder();
    html.open("td");
    html.openInline("a", attrs("href", "#"));
    html.text("link");
    html.closeInlineLn("a");
    html.close("td");

    assertEquals("<td>\n  <a href=\"#\">link</a>\n</td>\n", html.toString());
  }

  @Test
  void rawVerbatimPassesThroughUnchanged() {
    HtmlBuilder html = new HtmlBuilder(4);
    html.rawVerbatim("<!--[if mso]><table><![endif]-->");

    assertEquals("<!--[if mso]><table><![endif]-->", html.toString());
  }

  @Test
  void rawAddsIndentAndNewline() {
    HtmlBuilder html = new HtmlBuilder(2);
    html.raw("<!--[if mso]>content<![endif]-->");

    assertEquals("  <!--[if mso]>content<![endif]-->\n", html.toString());
  }

  @Test
  void rawWithTrailingNewlineDoesNotDoubleIt() {
    HtmlBuilder html = new HtmlBuilder();
    html.raw("content\n");

    assertEquals("content\n", html.toString());
  }

  @Test
  void lineAddsIndentAndNewline() {
    HtmlBuilder html = new HtmlBuilder();
    html.open("div");
    html.line("some text");
    html.close("div");

    assertEquals("<div>\n  some text\n</div>\n", html.toString());
  }

  @Test
  void selfCloseFormatsCorrectly() {
    HtmlBuilder html = new HtmlBuilder();
    html.selfClose("img", attrs("src", "logo.png", "alt", "Logo"));

    assertEquals("<img src=\"logo.png\" alt=\"Logo\" />\n", html.toString());
  }

  @Test
  void manualIndentAndOutdent() {
    HtmlBuilder html = new HtmlBuilder();
    html.indent();
    html.line("indented");
    html.outdent();
    html.line("not indented");

    assertEquals("  indented\nnot indented\n", html.toString());
  }

  @Test
  void outdentDoesNotGoBelowZero() {
    HtmlBuilder html = new HtmlBuilder();
    html.outdent();
    html.outdent();
    html.line("still at zero");

    assertEquals("still at zero\n", html.toString());
  }

  @Test
  void attrsSkipsNullAndEmptyValues() {
    String result = attrs("border", "0", "class", null, "id", "", "role", "presentation");
    assertEquals(" border=\"0\" role=\"presentation\"", result);
  }

  @Test
  void attrsReturnsEmptyForNoArgs() {
    assertEquals("", attrs());
    assertEquals("", attrs((String[]) null));
  }

  @Test
  void attrsAlwaysEmitsAltAndStyleEvenWhenEmpty() {
    String result = attrs("alt", "", "class", "", "style", "");
    assertEquals(" alt=\"\" style=\"\"", result);
  }

  @Test
  void attrIfReturnsAttributeWhenPresent() {
    assertEquals(" class=\"foo\"", attrIf("class", "foo"));
  }

  @Test
  void attrIfReturnsEmptyWhenAbsent() {
    assertEquals("", attrIf("class", null));
    assertEquals("", attrIf("class", ""));
    assertEquals("", attrIf(null, "foo"));
  }

  @Test
  void complexNestedStructure() {
    HtmlBuilder html = new HtmlBuilder();
    html.open("table", attrs("border", "0", "cellpadding", "0", "cellspacing", "0"));
    html.open("tbody");
    html.open("tr");
    html.open("td", attrs("style", "padding:10px;"));
    html.openInline("a", attrs("href", "#"));
    html.text("Click");
    html.closeInlineLn("a");
    html.close("td");
    html.close("tr");
    html.close("tbody");
    html.close("table");

    String expected = "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n"
      + "  <tbody>\n"
      + "    <tr>\n"
      + "      <td style=\"padding:10px;\">\n"
      + "        <a href=\"#\">Click</a>\n"
      + "      </td>\n"
      + "    </tr>\n"
      + "  </tbody>\n"
      + "</table>\n";

    assertEquals(expected, html.toString());
  }

  @Test
  void conditionalNestingAdjustsIndentAutomatically() {
    // Simulates the hasBgUrl conditional in MjSection:
    // when hasBgUrl=true, an extra wrapping div exists
    boolean hasBgUrl = true;
    HtmlBuilder html = new HtmlBuilder();
    html.open("div", attrs("style", "margin:0 auto;"));
    if (hasBgUrl) {
      html.open("div", attrs("style", "line-height:0;font-size:0;"));
    }
    html.open("table");
    html.line("content");
    html.close("table");
    if (hasBgUrl) {
      html.close("div");
    }
    html.close("div");

    String expected = "<div style=\"margin:0 auto;\">\n"
      + "  <div style=\"line-height:0;font-size:0;\">\n"
      + "    <table>\n"
      + "      content\n"
      + "    </table>\n"
      + "  </div>\n"
      + "</div>\n";

    assertEquals(expected, html.toString());
  }

  @Test
  void newlineAppendsLineFeed() {
    HtmlBuilder html = new HtmlBuilder();
    html.text("a");
    html.newline();
    html.text("b");

    assertEquals("a\nb", html.toString());
  }

  @Test
  void textWithNullIsIgnored() {
    HtmlBuilder html = new HtmlBuilder();
    html.text(null);
    assertEquals("", html.toString());
  }

  @Test
  void chainingWorks() {
    String result = new HtmlBuilder().open("div").line("hello").close("div").toString();

    assertEquals("<div>\n  hello\n</div>\n", result);
  }

  @Test
  void msoStringWrapsInConditionalComments() {
    HtmlBuilder html = new HtmlBuilder();
    html.mso("</td></tr></table>");

    assertEquals("<!--[if mso | IE]></td></tr></table><![endif]-->\n", html.toString());
  }

  @Test
  void msoBlockWrapsBuilderOutput() {
    HtmlBuilder html = new HtmlBuilder();
    html.mso(() -> html.rawVerbatim("<table><tr><td>"));

    assertEquals("<!--[if mso | IE]><table><tr><td><![endif]-->\n", html.toString());
  }

  @Test
  void msoBlockWithStructuredContent() {
    HtmlBuilder html = new HtmlBuilder();
    html.mso(() -> html.rawVerbatim("<table" + attrs("role", "presentation", "border", "0") + "></table>"));

    assertEquals(
      "<!--[if mso | IE]><table role=\"presentation\" border=\"0\"></table><![endif]-->\n", html.toString());
  }

  @Test
  void wrapProducesMatchedTagsWithIndentation() {
    HtmlBuilder html = new HtmlBuilder();
    html.wrap("div", () -> html.line("content"));

    assertEquals("<div>\n  content\n</div>\n", html.toString());
  }

  @Test
  void wrapWithAttrsProducesMatchedTagsWithIndentation() {
    HtmlBuilder html = new HtmlBuilder();
    html.wrap("td", attrs("style", "padding:10px;"), () -> html.line("cell"));

    assertEquals("<td style=\"padding:10px;\">\n  cell\n</td>\n", html.toString());
  }

  @Test
  void wrapNestsCorrectly() {
    HtmlBuilder html = new HtmlBuilder();
    html.wrap("table", () -> html.wrap("tbody", () -> html.wrap("tr", () -> html.line("row"))));

    String expected = "<table>\n"
      + "  <tbody>\n"
      + "    <tr>\n"
      + "      row\n"
      + "    </tr>\n"
      + "  </tbody>\n"
      + "</table>\n";
    assertEquals(expected, html.toString());
  }

  @Test
  void wrapGuaranteesMatchedCloseTag() {
    HtmlBuilder html = new HtmlBuilder();
    html.wrap("div", attrs("class", "outer"), () -> {
      html.wrap("p", () -> html.line("hello"));
      html.wrap("p", () -> html.line("world"));
    });

    String expected = "<div class=\"outer\">\n"
      + "  <p>\n"
      + "    hello\n"
      + "  </p>\n"
      + "  <p>\n"
      + "    world\n"
      + "  </p>\n"
      + "</div>\n";
    assertEquals(expected, html.toString());
  }
}
