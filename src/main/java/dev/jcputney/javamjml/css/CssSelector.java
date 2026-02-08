package dev.jcputney.javamjml.css;

import java.util.List;

/**
 * Sealed hierarchy of CSS selectors.
 * <p>
 * The hierarchy is:
 * <ul>
 *   <li>{@link SelectorList} - comma-separated list (e.g. "h1, h2, h3")</li>
 *   <li>{@link ComplexSelector} - combinators (e.g. "div > p", "div p")</li>
 *   <li>{@link CompoundSelector} - no-combinator chain (e.g. "div.red#main")</li>
 *   <li>{@link SimpleSelector} - single selector part:
 *     {@link TypeSelector}, {@link ClassSelector}, {@link IdSelector},
 *     {@link AttributeSelector}, {@link PseudoClassSelector}, {@link PseudoElementSelector},
 *     {@link UniversalSelector}</li>
 * </ul>
 */
public sealed interface CssSelector {

  /**
   * Computes the specificity of this selector.
   */
  CssSpecificity specificity();

  // --- Selector List ---

  record SelectorList(List<CssSelector> selectors) implements CssSelector {

    @Override
    public CssSpecificity specificity() {
      // A selector list's specificity is the max of its individual selectors
      // (each selector in the list is independently matched)
      CssSpecificity max = CssSpecificity.ZERO;
      for (CssSelector s : selectors) {
        CssSpecificity sp = s.specificity();
        if (sp.compareTo(max) > 0) {
          max = sp;
        }
      }
      return max;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < selectors.size(); i++) {
        if (i > 0) {
          sb.append(", ");
        }
        sb.append(selectors.get(i));
      }
      return sb.toString();
    }
  }

  // --- Complex Selector (with combinator) ---

  enum Combinator {
    DESCENDANT,       // " " (space)
    CHILD,            // ">"
    ADJACENT_SIBLING, // "+"
    GENERAL_SIBLING   // "~"
  }

  record ComplexSelector(CssSelector left, Combinator combinator,
                         CssSelector right) implements CssSelector {

    @Override
    public CssSpecificity specificity() {
      return left.specificity().add(right.specificity());
    }

    @Override
    public String toString() {
      String comb = switch (combinator) {
        case DESCENDANT -> " ";
        case CHILD -> " > ";
        case ADJACENT_SIBLING -> " + ";
        case GENERAL_SIBLING -> " ~ ";
      };
      return left.toString() + comb + right.toString();
    }
  }

  // --- Compound Selector (chain of simple selectors) ---

  record CompoundSelector(List<SimpleSelector> parts) implements CssSelector {

    @Override
    public CssSpecificity specificity() {
      CssSpecificity result = CssSpecificity.ZERO;
      for (SimpleSelector part : parts) {
        result = result.add(part.specificity());
      }
      return result;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      for (SimpleSelector part : parts) {
        sb.append(part);
      }
      return sb.toString();
    }
  }

  // --- Simple Selectors ---

  sealed interface SimpleSelector extends CssSelector {
  }

  record UniversalSelector() implements SimpleSelector {

    @Override
    public CssSpecificity specificity() {
      return CssSpecificity.ZERO;
    }

    @Override
    public String toString() {
      return "*";
    }
  }

  record TypeSelector(String tagName) implements SimpleSelector {

    @Override
    public CssSpecificity specificity() {
      return new CssSpecificity(0, 0, 1);
    }

    @Override
    public String toString() {
      return tagName;
    }
  }

  record ClassSelector(String className) implements SimpleSelector {

    @Override
    public CssSpecificity specificity() {
      return new CssSpecificity(0, 1, 0);
    }

    @Override
    public String toString() {
      return "." + className;
    }
  }

  record IdSelector(String id) implements SimpleSelector {

    @Override
    public CssSpecificity specificity() {
      return new CssSpecificity(1, 0, 0);
    }

    @Override
    public String toString() {
      return "#" + id;
    }
  }

  /**
   * Attribute selector with operator.
   * Operators: null (has attr), "=" (exact), "~=" (word), "|=" (prefix-dash),
   *            "^=" (starts-with), "$=" (ends-with), "*=" (contains)
   */
  record AttributeSelector(String attribute, String operator,
                            String value) implements SimpleSelector {

    @Override
    public CssSpecificity specificity() {
      return new CssSpecificity(0, 1, 0);
    }

    @Override
    public String toString() {
      if (operator == null) {
        return "[" + attribute + "]";
      }
      return "[" + attribute + operator + "\"" + value + "\"]";
    }
  }

  record PseudoClassSelector(String name, String argument) implements SimpleSelector {

    @Override
    public CssSpecificity specificity() {
      return new CssSpecificity(0, 1, 0);
    }

    @Override
    public String toString() {
      if (argument != null) {
        return ":" + name + "(" + argument + ")";
      }
      return ":" + name;
    }
  }

  record PseudoElementSelector(String name) implements SimpleSelector {

    @Override
    public CssSpecificity specificity() {
      return new CssSpecificity(0, 0, 1);
    }

    @Override
    public String toString() {
      return "::" + name;
    }
  }
}
