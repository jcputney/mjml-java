package dev.jcputney.javamjml.css;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class CssSelectorMatcherTest {

  private HtmlElement el(String tag, Map<String, String> attrs) {
    return new HtmlElement(tag, attrs);
  }

  private HtmlElement el(String tag) {
    return el(tag, Map.of());
  }

  @Test
  void matchesTypeSelector() {
    assertTrue(CssSelectorMatcher.matches(
        CssSelectorParser.parse("div"), el("div")));
    assertFalse(CssSelectorMatcher.matches(
        CssSelectorParser.parse("div"), el("span")));
  }

  @Test
  void matchesUniversalSelector() {
    assertTrue(CssSelectorMatcher.matches(
        CssSelectorParser.parse("*"), el("div")));
    assertTrue(CssSelectorMatcher.matches(
        CssSelectorParser.parse("*"), el("span")));
  }

  @Test
  void matchesClassSelector() {
    assertTrue(CssSelectorMatcher.matches(
        CssSelectorParser.parse(".red"),
        el("div", Map.of("class", "red bold"))));
    assertFalse(CssSelectorMatcher.matches(
        CssSelectorParser.parse(".blue"),
        el("div", Map.of("class", "red bold"))));
  }

  @Test
  void matchesIdSelector() {
    assertTrue(CssSelectorMatcher.matches(
        CssSelectorParser.parse("#main"),
        el("div", Map.of("id", "main"))));
    assertFalse(CssSelectorMatcher.matches(
        CssSelectorParser.parse("#other"),
        el("div", Map.of("id", "main"))));
  }

  @Test
  void matchesCompoundSelector() {
    assertTrue(CssSelectorMatcher.matches(
        CssSelectorParser.parse("div.red"),
        el("div", Map.of("class", "red"))));
    assertFalse(CssSelectorMatcher.matches(
        CssSelectorParser.parse("div.red"),
        el("span", Map.of("class", "red"))));
    assertFalse(CssSelectorMatcher.matches(
        CssSelectorParser.parse("div.red"),
        el("div", Map.of("class", "blue"))));
  }

  @Test
  void matchesDescendantCombinator() {
    HtmlElement grandparent = el("div", Map.of("class", "container"));
    HtmlElement parent = el("table");
    HtmlElement child = el("td");

    grandparent.addChild(parent);
    parent.addChild(child);

    assertTrue(CssSelectorMatcher.matches(
        CssSelectorParser.parse(".container td"), child));
    assertTrue(CssSelectorMatcher.matches(
        CssSelectorParser.parse("div td"), child));
    assertFalse(CssSelectorMatcher.matches(
        CssSelectorParser.parse("span td"), child));
  }

  @Test
  void matchesChildCombinator() {
    HtmlElement parent = el("div");
    HtmlElement child = el("p");
    HtmlElement grandchild = el("span");

    parent.addChild(child);
    child.addChild(grandchild);

    assertTrue(CssSelectorMatcher.matches(
        CssSelectorParser.parse("div > p"), child));
    assertFalse(CssSelectorMatcher.matches(
        CssSelectorParser.parse("div > span"), grandchild));
    assertTrue(CssSelectorMatcher.matches(
        CssSelectorParser.parse("p > span"), grandchild));
  }

  @Test
  void matchesAdjacentSiblingCombinator() {
    HtmlElement parent = el("div");
    HtmlElement first = el("h1");
    HtmlElement second = el("p");

    parent.addChild(first);
    parent.addChild(second);

    assertTrue(CssSelectorMatcher.matches(
        CssSelectorParser.parse("h1 + p"), second));
    assertFalse(CssSelectorMatcher.matches(
        CssSelectorParser.parse("h1 + p"), first));
  }

  @Test
  void matchesGeneralSiblingCombinator() {
    HtmlElement parent = el("div");
    HtmlElement h1 = el("h1");
    HtmlElement p1 = el("p");
    HtmlElement p2 = el("p");

    parent.addChild(h1);
    parent.addChild(p1);
    parent.addChild(p2);

    assertTrue(CssSelectorMatcher.matches(
        CssSelectorParser.parse("h1 ~ p"), p1));
    assertTrue(CssSelectorMatcher.matches(
        CssSelectorParser.parse("h1 ~ p"), p2));
    assertFalse(CssSelectorMatcher.matches(
        CssSelectorParser.parse("h1 ~ p"), h1));
  }

  @Test
  void matchesAttributeExistence() {
    assertTrue(CssSelectorMatcher.matches(
        CssSelectorParser.parse("[href]"),
        el("a", Map.of("href", "https://example.com"))));
    assertFalse(CssSelectorMatcher.matches(
        CssSelectorParser.parse("[href]"),
        el("a", Map.of())));
  }

  @Test
  void matchesAttributeEquals() {
    assertTrue(CssSelectorMatcher.matches(
        CssSelectorParser.parse("[type=\"text\"]"),
        el("input", Map.of("type", "text"))));
    assertFalse(CssSelectorMatcher.matches(
        CssSelectorParser.parse("[type=\"text\"]"),
        el("input", Map.of("type", "email"))));
  }

  @Test
  void matchesAttributeContains() {
    assertTrue(CssSelectorMatcher.matches(
        CssSelectorParser.parse("[class*=\"col\"]"),
        el("div", Map.of("class", "col-6 offset-2"))));
  }

  @Test
  void matchesSelectorList() {
    CssSelector sel = CssSelectorParser.parse("h1, h2, h3");
    assertTrue(CssSelectorMatcher.matches(sel, el("h1")));
    assertTrue(CssSelectorMatcher.matches(sel, el("h2")));
    assertTrue(CssSelectorMatcher.matches(sel, el("h3")));
    assertFalse(CssSelectorMatcher.matches(sel, el("h4")));
  }

  @Test
  void pseudoClassDoesNotMatch() {
    assertFalse(CssSelectorMatcher.matches(
        CssSelectorParser.parse("a:hover"),
        el("a", Map.of("href", "#"))));
  }

  @Test
  void detectsPseudoSelectors() {
    assertTrue(CssSelectorMatcher.hasPseudo(CssSelectorParser.parse("a:hover")));
    assertTrue(CssSelectorMatcher.hasPseudo(CssSelectorParser.parse("p::before")));
    assertFalse(CssSelectorMatcher.hasPseudo(CssSelectorParser.parse(".red")));
    assertFalse(CssSelectorMatcher.hasPseudo(CssSelectorParser.parse("div > p")));
  }
}
