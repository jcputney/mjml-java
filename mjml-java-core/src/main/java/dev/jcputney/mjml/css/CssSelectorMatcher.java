package dev.jcputney.mjml.css;

import dev.jcputney.mjml.css.CssSelector.AttributeSelector;
import dev.jcputney.mjml.css.CssSelector.ClassSelector;
import dev.jcputney.mjml.css.CssSelector.CompoundSelector;
import dev.jcputney.mjml.css.CssSelector.ComplexSelector;
import dev.jcputney.mjml.css.CssSelector.IdSelector;
import dev.jcputney.mjml.css.CssSelector.PseudoClassSelector;
import dev.jcputney.mjml.css.CssSelector.PseudoElementSelector;
import dev.jcputney.mjml.css.CssSelector.SelectorList;
import dev.jcputney.mjml.css.CssSelector.SimpleSelector;
import dev.jcputney.mjml.css.CssSelector.TypeSelector;
import dev.jcputney.mjml.css.CssSelector.UniversalSelector;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Matches CSS selectors against {@link HtmlElement} nodes.
 * <p>
 * Supports all CSS2.1 selector types plus CSS3 attribute selectors.
 * Pseudo-classes and pseudo-elements never match (they're preserved in style blocks,
 * not inlined).
 */
public final class CssSelectorMatcher {

  private static final Pattern WHITESPACE = Pattern.compile("\\s+");

  private CssSelectorMatcher() {
  }

  /**
   * Tests whether the given selector matches the given element.
   */
  public static boolean matches(CssSelector selector, HtmlElement element) {
    if (selector == null || element == null) {
      return false;
    }

    if (selector instanceof SelectorList list) {
      return matchesSelectorList(list, element);
    } else if (selector instanceof ComplexSelector complex) {
      return matchesComplexSelector(complex, element);
    } else if (selector instanceof CompoundSelector compound) {
      return matchesCompoundSelector(compound, element);
    } else if (selector instanceof SimpleSelector simple) {
      return matchesSimpleSelector(simple, element);
    }
    return false;
  }

  /**
   * Returns true if the selector contains pseudo-classes or pseudo-elements
   * that cannot be inlined (e.g. :hover, ::before).
   */
  public static boolean hasPseudo(CssSelector selector) {
    if (selector instanceof SelectorList list) {
      for (CssSelector s : list.selectors()) {
        if (hasPseudo(s)) {
          return true;
        }
      }
      return false;
    } else if (selector instanceof ComplexSelector complex) {
      return hasPseudo(complex.left()) || hasPseudo(complex.right());
    } else if (selector instanceof CompoundSelector compound) {
      for (SimpleSelector part : compound.parts()) {
        if (hasPseudo(part)) {
          return true;
        }
      }
      return false;
    } else if (selector instanceof PseudoClassSelector) {
      return true;
    } else if (selector instanceof PseudoElementSelector) {
      return true;
    }
    return false;
  }

  // --- Matching logic ---

  private static boolean matchesSelectorList(SelectorList list, HtmlElement element) {
    for (CssSelector s : list.selectors()) {
      if (matches(s, element)) {
        return true;
      }
    }
    return false;
  }

  private static boolean matchesComplexSelector(ComplexSelector complex, HtmlElement element) {
    if (!matches(complex.right(), element)) {
      return false;
    }

    return switch (complex.combinator()) {
      case DESCENDANT -> matchesDescendant(complex.left(), element);
      case CHILD -> matchesChild(complex.left(), element);
      case ADJACENT_SIBLING -> matchesAdjacentSibling(complex.left(), element);
      case GENERAL_SIBLING -> matchesGeneralSibling(complex.left(), element);
    };
  }

  private static boolean matchesDescendant(CssSelector left, HtmlElement element) {
    HtmlElement ancestor = element.getParent();
    while (ancestor != null) {
      if (matches(left, ancestor)) {
        return true;
      }
      ancestor = ancestor.getParent();
    }
    return false;
  }

  private static boolean matchesChild(CssSelector left, HtmlElement element) {
    HtmlElement parent = element.getParent();
    return parent != null && matches(left, parent);
  }

  private static boolean matchesAdjacentSibling(CssSelector left, HtmlElement element) {
    HtmlElement prev = element.previousSibling();
    return prev != null && matches(left, prev);
  }

  private static boolean matchesGeneralSibling(CssSelector left, HtmlElement element) {
    if (element.getParent() == null) {
      return false;
    }
    int myIndex = element.indexInParent();
    for (int i = 0; i < myIndex; i++) {
      if (matches(left, element.getParent().getChildren().get(i))) {
        return true;
      }
    }
    return false;
  }

  private static boolean matchesCompoundSelector(CompoundSelector compound, HtmlElement element) {
    for (SimpleSelector part : compound.parts()) {
      if (!matchesSimpleSelector(part, element)) {
        return false;
      }
    }
    return true;
  }

  private static boolean matchesSimpleSelector(SimpleSelector selector, HtmlElement element) {
    if (selector instanceof UniversalSelector) {
      return true;
    } else if (selector instanceof TypeSelector type) {
      return element.getTagName().equalsIgnoreCase(type.tagName());
    } else if (selector instanceof ClassSelector cls) {
      return element.getClassNames().contains(cls.className());
    } else if (selector instanceof IdSelector id) {
      return id.id().equals(element.getId());
    } else if (selector instanceof AttributeSelector attr) {
      return matchesAttribute(attr, element);
    } else if (selector instanceof PseudoClassSelector) {
      return false; // Can't inline pseudo-classes
    } else if (selector instanceof PseudoElementSelector) {
      return false; // Can't inline pseudo-elements
    }
    return false;
  }

  private static boolean matchesAttribute(AttributeSelector attr, HtmlElement element) {
    String value = element.getAttribute(attr.attribute());

    if (attr.operator() == null) {
      return value != null;
    }

    if (value == null) {
      return false;
    }

    String expected = attr.value();
    String op = attr.operator();

    if ("=".equals(op)) {
      return value.equals(expected);
    } else if ("~=".equals(op)) {
      Set<String> words = Set.of(WHITESPACE.split(value));
      return words.contains(expected);
    } else if ("|=".equals(op)) {
      return value.equals(expected) || value.startsWith(expected + "-");
    } else if ("^=".equals(op)) {
      return value.startsWith(expected);
    } else if ("$=".equals(op)) {
      return value.endsWith(expected);
    } else if ("*=".equals(op)) {
      return value.contains(expected);
    }
    return false;
  }
}
