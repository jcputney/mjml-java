package dev.jcputney.mjml.css;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class CssParserTest {

  @Test
  void parsesSimpleRule() {
    List<CssRule> rules = CssParser.parseRules(".red { color: red; }");
    assertEquals(1, rules.size());
    assertEquals(".red", rules.get(0).selectorText());
    assertEquals(1, rules.get(0).declarations().size());
    assertEquals("color", rules.get(0).declarations().get(0).property());
    assertEquals("red", rules.get(0).declarations().get(0).value());
  }

  @Test
  void parsesMultipleDeclarations() {
    List<CssRule> rules = CssParser.parseRules("p { color: red; font-size: 14px; margin: 0; }");
    assertEquals(1, rules.size());
    assertEquals(3, rules.get(0).declarations().size());
    assertEquals("color", rules.get(0).declarations().get(0).property());
    assertEquals("font-size", rules.get(0).declarations().get(1).property());
    assertEquals("margin", rules.get(0).declarations().get(2).property());
  }

  @Test
  void parsesMultipleRules() {
    List<CssRule> rules =
        CssParser.parseRules(".a { color: red; } .b { color: blue; } .c { color: green; }");
    assertEquals(3, rules.size());
    assertEquals(".a", rules.get(0).selectorText());
    assertEquals(".b", rules.get(1).selectorText());
    assertEquals(".c", rules.get(2).selectorText());
  }

  @Test
  void parsesImportantDeclaration() {
    List<CssRule> rules = CssParser.parseRules("p { color: red !important; }");
    assertTrue(rules.get(0).declarations().get(0).important());
  }

  @Test
  void stripsComments() {
    List<CssRule> rules =
        CssParser.parseRules(
            "/* header styles */ .header { color: red; } /* footer */ .footer { color: blue; }");
    assertEquals(2, rules.size());
    assertEquals(".header", rules.get(0).selectorText());
    assertEquals(".footer", rules.get(1).selectorText());
  }

  @Test
  void preservesMediaQueries() {
    CssParser.ParseResult result =
        CssParser.parse(
            ".a { color: red; } @media (max-width: 600px) { .b { color: blue; } } .c { color: green; }");
    assertEquals(2, result.rules().size());
    assertEquals(1, result.preservedAtRules().size());
    assertTrue(result.preservedAtRules().get(0).contains("@media"));
    assertTrue(result.preservedAtRules().get(0).contains("max-width: 600px"));
  }

  @Test
  void preservesKeyframes() {
    CssParser.ParseResult result =
        CssParser.parse(
            "@keyframes spin { from { transform: rotate(0); } to { transform: rotate(360deg); } } .spinner { animation: spin 1s; }");
    assertEquals(1, result.rules().size());
    assertEquals(1, result.preservedAtRules().size());
    assertTrue(result.preservedAtRules().get(0).contains("@keyframes"));
  }

  @Test
  void handlesEmptyInput() {
    CssParser.ParseResult result = CssParser.parse("");
    assertTrue(result.rules().isEmpty());
    assertTrue(result.preservedAtRules().isEmpty());
  }

  @Test
  void handlesNullInput() {
    CssParser.ParseResult result = CssParser.parse(null);
    assertTrue(result.rules().isEmpty());
  }

  @Test
  void parsesComplexSelectors() {
    List<CssRule> rules =
        CssParser.parseRules(
            "div.container > p.text { color: #333; font-family: Arial, sans-serif; }");
    assertEquals(1, rules.size());
    assertEquals("div.container > p.text", rules.get(0).selectorText());
  }

  @Test
  void handlesUrlInDeclarations() {
    List<CssRule> rules =
        CssParser.parseRules(".bg { background: url('https://example.com/img.jpg') no-repeat; }");
    assertEquals(1, rules.size());
    assertEquals(
        "url('https://example.com/img.jpg') no-repeat", rules.get(0).declarations().get(0).value());
  }

  @Test
  void handlesNoTrailingSemicolon() {
    List<CssRule> rules = CssParser.parseRules(".red { color: red }");
    assertEquals(1, rules.size());
    assertEquals("red", rules.get(0).declarations().get(0).value());
  }
}
