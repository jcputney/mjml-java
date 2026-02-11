package dev.jcputney.mjml.css;

import java.util.List;

/**
 * Sealed hierarchy of CSS selectors.
 *
 * <p>The hierarchy is:
 *
 * <ul>
 *   <li>{@link SelectorList} - comma-separated list (e.g. "h1, h2, h3")
 *   <li>{@link ComplexSelector} - combinators (e.g. "div > p", "div p")
 *   <li>{@link CompoundSelector} - no-combinator chain (e.g. "div.red#main")
 *   <li>{@link SimpleSelector} - single selector part: {@link TypeSelector}, {@link ClassSelector},
 *       {@link IdSelector}, {@link AttributeSelector}, {@link PseudoClassSelector}, {@link
 *       PseudoElementSelector}, {@link UniversalSelector}
 * </ul>
 */
public sealed interface CssSelector {

  /**
   * Computes the specificity of this selector.
   *
   * @return the computed specificity for this selector
   */
  CssSpecificity specificity();

  // --- Selector List ---

  /** CSS combinator types used between selectors in a complex selector. */
  enum Combinator {
    /** Descendant combinator (space), matches any descendant. */
    DESCENDANT,
    /** Child combinator ({@code >}), matches direct children only. */
    CHILD,
    /** Adjacent sibling combinator ({@code +}), matches the immediately following sibling. */
    ADJACENT_SIBLING,
    /** General sibling combinator ({@code ~}), matches any following sibling. */
    GENERAL_SIBLING
  }

  // --- Complex Selector (with combinator) ---

  /**
   * A single simple selector part (type, class, ID, attribute, pseudo-class, or pseudo-element).
   */
  sealed interface SimpleSelector extends CssSelector {}

  /**
   * A comma-separated list of selectors (e.g. "h1, h2, h3").
   *
   * @param selectors the individual selectors in the list
   */
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

  // --- Compound Selector (chain of simple selectors) ---

  /**
   * A complex selector joining two selectors with a combinator (e.g. "div > p").
   *
   * @param left the left-hand selector
   * @param combinator the combinator joining left and right
   * @param right the right-hand selector
   */
  record ComplexSelector(CssSelector left, Combinator combinator, CssSelector right)
      implements CssSelector {

    @Override
    public CssSpecificity specificity() {
      return left.specificity().add(right.specificity());
    }

    @Override
    public String toString() {
      String comb =
          switch (combinator) {
            case DESCENDANT -> " ";
            case CHILD -> " > ";
            case ADJACENT_SIBLING -> " + ";
            case GENERAL_SIBLING -> " ~ ";
          };
      return left.toString() + comb + right.toString();
    }
  }

  // --- Simple Selectors ---

  /**
   * A compound selector consisting of a chain of simple selectors with no combinator (e.g.
   * "div.red#main").
   *
   * @param parts the simple selectors that make up this compound selector
   */
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

  /** The universal selector ({@code *}), which matches any element. */
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

  /**
   * A type (element name) selector (e.g. "div", "p").
   *
   * @param tagName the element tag name to match
   */
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

  /**
   * A class selector (e.g. ".red").
   *
   * @param className the class name to match (without the leading dot)
   */
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

  /**
   * An ID selector (e.g. "#main").
   *
   * @param id the ID value to match (without the leading hash)
   */
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
   * Attribute selector with operator. Operators: null (has attr), "=" (exact), "~=" (word), "|="
   * (prefix-dash), "^=" (starts-with), "$=" (ends-with), "*=" (contains)
   *
   * @param attribute the attribute name to match
   * @param operator the comparison operator, or {@code null} for presence-only checks
   * @param value the value to compare against, or {@code null} when operator is {@code null}
   */
  record AttributeSelector(String attribute, String operator, String value)
      implements SimpleSelector {

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

  /**
   * A pseudo-class selector (e.g. ":hover", ":nth-child(2)").
   *
   * @param name the pseudo-class name (without the leading colon)
   * @param argument the optional argument for functional pseudo-classes, or {@code null}
   */
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

  /**
   * A pseudo-element selector (e.g. "::before", "::after").
   *
   * @param name the pseudo-element name (without the leading double colon)
   */
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
