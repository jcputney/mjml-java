package dev.jcputney.mjml.css;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import dev.jcputney.mjml.css.CssSelector.AttributeSelector;
import dev.jcputney.mjml.css.CssSelector.ClassSelector;
import dev.jcputney.mjml.css.CssSelector.Combinator;
import dev.jcputney.mjml.css.CssSelector.ComplexSelector;
import dev.jcputney.mjml.css.CssSelector.CompoundSelector;
import dev.jcputney.mjml.css.CssSelector.IdSelector;
import dev.jcputney.mjml.css.CssSelector.PseudoClassSelector;
import dev.jcputney.mjml.css.CssSelector.PseudoElementSelector;
import dev.jcputney.mjml.css.CssSelector.SelectorList;
import dev.jcputney.mjml.css.CssSelector.TypeSelector;
import dev.jcputney.mjml.css.CssSelector.UniversalSelector;
import org.junit.jupiter.api.Test;

class CssSelectorParserTest {

  @Test
  void parsesTypeSelector() {
    CssSelector sel = CssSelectorParser.parse("div");
    assertInstanceOf(TypeSelector.class, sel);
    assertEquals("div", ((TypeSelector) sel).tagName());
  }

  @Test
  void parsesUniversalSelector() {
    CssSelector sel = CssSelectorParser.parse("*");
    assertInstanceOf(UniversalSelector.class, sel);
  }

  @Test
  void parsesClassSelector() {
    CssSelector sel = CssSelectorParser.parse(".red");
    assertInstanceOf(ClassSelector.class, sel);
    assertEquals("red", ((ClassSelector) sel).className());
  }

  @Test
  void parsesIdSelector() {
    CssSelector sel = CssSelectorParser.parse("#main");
    assertInstanceOf(IdSelector.class, sel);
    assertEquals("main", ((IdSelector) sel).id());
  }

  @Test
  void parsesCompoundSelector() {
    CssSelector sel = CssSelectorParser.parse("div.red#main");
    assertInstanceOf(CompoundSelector.class, sel);
    CompoundSelector compound = (CompoundSelector) sel;
    assertEquals(3, compound.parts().size());
  }

  @Test
  void parsesDescendantCombinator() {
    CssSelector sel = CssSelectorParser.parse("div p");
    assertInstanceOf(ComplexSelector.class, sel);
    ComplexSelector complex = (ComplexSelector) sel;
    assertEquals(Combinator.DESCENDANT, complex.combinator());
  }

  @Test
  void parsesChildCombinator() {
    CssSelector sel = CssSelectorParser.parse("div > p");
    assertInstanceOf(ComplexSelector.class, sel);
    ComplexSelector complex = (ComplexSelector) sel;
    assertEquals(Combinator.CHILD, complex.combinator());
  }

  @Test
  void parsesAdjacentSiblingCombinator() {
    CssSelector sel = CssSelectorParser.parse("h1 + p");
    assertInstanceOf(ComplexSelector.class, sel);
    assertEquals(Combinator.ADJACENT_SIBLING, ((ComplexSelector) sel).combinator());
  }

  @Test
  void parsesGeneralSiblingCombinator() {
    CssSelector sel = CssSelectorParser.parse("h1 ~ p");
    assertInstanceOf(ComplexSelector.class, sel);
    assertEquals(Combinator.GENERAL_SIBLING, ((ComplexSelector) sel).combinator());
  }

  @Test
  void parsesSelectorList() {
    CssSelector sel = CssSelectorParser.parse("h1, h2, h3");
    assertInstanceOf(SelectorList.class, sel);
    assertEquals(3, ((SelectorList) sel).selectors().size());
  }

  @Test
  void parsesAttributeExistenceSelector() {
    CssSelector sel = CssSelectorParser.parse("[href]");
    assertInstanceOf(AttributeSelector.class, sel);
    AttributeSelector attr = (AttributeSelector) sel;
    assertEquals("href", attr.attribute());
    assertNull(attr.operator());
  }

  @Test
  void parsesAttributeEqualSelector() {
    CssSelector sel = CssSelectorParser.parse("[type=\"text\"]");
    assertInstanceOf(AttributeSelector.class, sel);
    AttributeSelector attr = (AttributeSelector) sel;
    assertEquals("type", attr.attribute());
    assertEquals("=", attr.operator());
    assertEquals("text", attr.value());
  }

  @Test
  void parsesAttributeContainsSelector() {
    CssSelector sel = CssSelectorParser.parse("[class*=\"col\"]");
    assertInstanceOf(AttributeSelector.class, sel);
    assertEquals("*=", ((AttributeSelector) sel).operator());
  }

  @Test
  void parsesPseudoClass() {
    CssSelector sel = CssSelectorParser.parse("a:hover");
    assertInstanceOf(CompoundSelector.class, sel);
    CompoundSelector compound = (CompoundSelector) sel;
    assertEquals(2, compound.parts().size());
    assertInstanceOf(PseudoClassSelector.class, compound.parts().get(1));
    assertEquals("hover", ((PseudoClassSelector) compound.parts().get(1)).name());
  }

  @Test
  void parsesPseudoElement() {
    CssSelector sel = CssSelectorParser.parse("p::before");
    assertInstanceOf(CompoundSelector.class, sel);
    CompoundSelector compound = (CompoundSelector) sel;
    assertInstanceOf(PseudoElementSelector.class, compound.parts().get(1));
  }

  @Test
  void parsesComplexChain() {
    // "div.container > table td.content"
    CssSelector sel = CssSelectorParser.parse("div.container > table td.content");
    assertNotNull(sel);
    // Should be: (div.container > table) td.content (descendant between table and td)
    assertInstanceOf(ComplexSelector.class, sel);
  }

  @Test
  void returnsNullForEmpty() {
    assertNull(CssSelectorParser.parse(""));
    assertNull(CssSelectorParser.parse(null));
    assertNull(CssSelectorParser.parse("   "));
  }

  @Test
  void specificityCounts() {
    // Type: (0,0,1)
    assertEquals(new CssSpecificity(0, 0, 1), CssSelectorParser.parse("div").specificity());

    // Class: (0,1,0)
    assertEquals(new CssSpecificity(0, 1, 0), CssSelectorParser.parse(".red").specificity());

    // ID: (1,0,0)
    assertEquals(new CssSpecificity(1, 0, 0), CssSelectorParser.parse("#main").specificity());

    // Compound: div.red#main = (1,1,1)
    assertEquals(
        new CssSpecificity(1, 1, 1), CssSelectorParser.parse("div.red#main").specificity());

    // Complex: div p = (0,0,2)
    assertEquals(new CssSpecificity(0, 0, 2), CssSelectorParser.parse("div p").specificity());

    // Complex: #nav .item a = (1,1,1)
    assertEquals(
        new CssSpecificity(1, 1, 1), CssSelectorParser.parse("#nav .item a").specificity());
  }
}
