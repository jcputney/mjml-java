package dev.jcputney.javamjml.css;

import dev.jcputney.javamjml.css.CssSelector.AttributeSelector;
import dev.jcputney.javamjml.css.CssSelector.ClassSelector;
import dev.jcputney.javamjml.css.CssSelector.Combinator;
import dev.jcputney.javamjml.css.CssSelector.CompoundSelector;
import dev.jcputney.javamjml.css.CssSelector.ComplexSelector;
import dev.jcputney.javamjml.css.CssSelector.IdSelector;
import dev.jcputney.javamjml.css.CssSelector.PseudoClassSelector;
import dev.jcputney.javamjml.css.CssSelector.PseudoElementSelector;
import dev.jcputney.javamjml.css.CssSelector.SelectorList;
import dev.jcputney.javamjml.css.CssSelector.SimpleSelector;
import dev.jcputney.javamjml.css.CssSelector.TypeSelector;
import dev.jcputney.javamjml.css.CssSelector.UniversalSelector;
import java.util.ArrayList;
import java.util.List;

/**
 * Recursive-descent parser for CSS selectors.
 * <p>
 * Grammar (simplified):
 * <pre>
 *   selector-list  = complex-selector (',' complex-selector)*
 *   complex-selector = compound-selector (combinator compound-selector)*
 *   combinator     = ' ' | '>' | '+' | '~'
 *   compound-selector = simple-selector+
 *   simple-selector = type | universal | class | id | attribute | pseudo
 * </pre>
 */
public final class CssSelectorParser {

  private final String input;
  private int pos;

  private CssSelectorParser(String input) {
    this.input = input;
    this.pos = 0;
  }

  /**
   * Parses a selector string into a CssSelector tree.
   *
   * @param selectorText the CSS selector text
   * @return the parsed selector, or null if parsing fails
   */
  public static CssSelector parse(String selectorText) {
    if (selectorText == null || selectorText.isBlank()) {
      return null;
    }
    try {
      CssSelectorParser parser = new CssSelectorParser(selectorText.trim());
      return parser.parseSelectorList();
    } catch (Exception e) {
      return null; // Invalid selector
    }
  }

  // --- Parser methods ---

  private CssSelector parseSelectorList() {
    List<CssSelector> selectors = new ArrayList<>();
    selectors.add(parseComplexSelector());

    while (pos < input.length()) {
      skipWhitespace();
      if (pos < input.length() && input.charAt(pos) == ',') {
        pos++; // consume ','
        skipWhitespace();
        selectors.add(parseComplexSelector());
      } else {
        break;
      }
    }

    if (selectors.size() == 1) {
      return selectors.get(0);
    }
    return new SelectorList(selectors);
  }

  private CssSelector parseComplexSelector() {
    CssSelector left = parseCompoundSelector();

    while (pos < input.length()) {
      int savedPos = pos;
      // Try to find a combinator
      Combinator comb = parseCombinator();
      if (comb == null) {
        break;
      }

      // Check if there's a valid compound selector after the combinator
      if (pos >= input.length() || input.charAt(pos) == ',' || input.charAt(pos) == ')') {
        pos = savedPos; // Restore position
        break;
      }

      CssSelector right = parseCompoundSelector();
      left = new ComplexSelector(left, comb, right);
    }

    return left;
  }

  private Combinator parseCombinator() {
    boolean hadWhitespace = skipWhitespace();

    if (pos >= input.length()) {
      return null;
    }

    char c = input.charAt(pos);
    if (c == '>') {
      pos++;
      skipWhitespace();
      return Combinator.CHILD;
    } else if (c == '+') {
      pos++;
      skipWhitespace();
      return Combinator.ADJACENT_SIBLING;
    } else if (c == '~') {
      pos++;
      skipWhitespace();
      return Combinator.GENERAL_SIBLING;
    } else if (hadWhitespace && c != ',' && c != ')') {
      // Whitespace combinator (descendant)
      return Combinator.DESCENDANT;
    }

    return null;
  }

  private CssSelector parseCompoundSelector() {
    List<SimpleSelector> parts = new ArrayList<>();

    while (pos < input.length()) {
      char c = input.charAt(pos);

      if (c == '.' || c == '#' || c == '[' || c == ':') {
        parts.add(parseSimpleSelector());
      } else if (c == '*') {
        pos++;
        parts.add(new UniversalSelector());
      } else if (isIdentStart(c)) {
        parts.add(parseTypeOrUniversal());
      } else {
        break;
      }
    }

    if (parts.isEmpty()) {
      throw new IllegalStateException("Expected selector at position " + pos);
    }

    if (parts.size() == 1) {
      return parts.get(0);
    }
    return new CompoundSelector(parts);
  }

  private SimpleSelector parseSimpleSelector() {
    char c = input.charAt(pos);

    return switch (c) {
      case '.' -> parseClassSelector();
      case '#' -> parseIdSelector();
      case '[' -> parseAttributeSelector();
      case ':' -> parsePseudoSelector();
      default -> parseTypeOrUniversal();
    };
  }

  private SimpleSelector parseTypeOrUniversal() {
    if (input.charAt(pos) == '*') {
      pos++;
      return new UniversalSelector();
    }
    String name = parseIdentifier();
    return new TypeSelector(name.toLowerCase());
  }

  private ClassSelector parseClassSelector() {
    pos++; // consume '.'
    String name = parseIdentifier();
    return new ClassSelector(name);
  }

  private IdSelector parseIdSelector() {
    pos++; // consume '#'
    String name = parseIdentifier();
    return new IdSelector(name);
  }

  private AttributeSelector parseAttributeSelector() {
    pos++; // consume '['
    skipWhitespace();

    String attr = parseIdentifier();
    skipWhitespace();

    if (pos < input.length() && input.charAt(pos) == ']') {
      pos++; // consume ']'
      return new AttributeSelector(attr, null, null);
    }

    // Parse operator
    String op = parseAttributeOperator();
    skipWhitespace();

    // Parse value
    String value = parseAttributeValue();
    skipWhitespace();

    if (pos < input.length() && input.charAt(pos) == ']') {
      pos++; // consume ']'
    }

    return new AttributeSelector(attr.toLowerCase(), op, value);
  }

  private String parseAttributeOperator() {
    if (pos >= input.length()) {
      return "=";
    }

    char c = input.charAt(pos);
    if (c == '=') {
      pos++;
      return "=";
    }

    if (pos + 1 < input.length() && input.charAt(pos + 1) == '=') {
      String op = "" + c + "=";
      pos += 2;
      return op;
    }

    return "=";
  }

  private String parseAttributeValue() {
    if (pos >= input.length()) {
      return "";
    }

    char quote = input.charAt(pos);
    if (quote == '"' || quote == '\'') {
      pos++; // consume opening quote
      int start = pos;
      while (pos < input.length() && input.charAt(pos) != quote) {
        pos++;
      }
      String value = input.substring(start, pos);
      if (pos < input.length()) {
        pos++; // consume closing quote
      }
      return value;
    }

    // Unquoted value
    int start = pos;
    while (pos < input.length() && input.charAt(pos) != ']'
        && !Character.isWhitespace(input.charAt(pos))) {
      pos++;
    }
    return input.substring(start, pos);
  }

  private SimpleSelector parsePseudoSelector() {
    pos++; // consume first ':'

    if (pos < input.length() && input.charAt(pos) == ':') {
      pos++; // consume second ':'  -> pseudo-element
      String name = parseIdentifier();
      return new PseudoElementSelector(name.toLowerCase());
    }

    // Pseudo-class
    String name = parseIdentifier();

    // Check for function argument
    String argument = null;
    if (pos < input.length() && input.charAt(pos) == '(') {
      pos++; // consume '('
      int depth = 1;
      int start = pos;
      while (pos < input.length() && depth > 0) {
        if (input.charAt(pos) == '(') {
          depth++;
        } else if (input.charAt(pos) == ')') {
          depth--;
        }
        if (depth > 0) {
          pos++;
        }
      }
      argument = input.substring(start, pos).trim();
      if (pos < input.length()) {
        pos++; // consume ')'
      }
    }

    return new PseudoClassSelector(name.toLowerCase(), argument);
  }

  private String parseIdentifier() {
    int start = pos;
    // CSS identifiers can start with a-z, A-Z, _, -, or escaped char
    // and continue with the same plus digits
    while (pos < input.length()) {
      char c = input.charAt(pos);
      if (isIdentChar(c)) {
        pos++;
      } else if (c == '\\' && pos + 1 < input.length()) {
        pos += 2; // escaped character
      } else {
        break;
      }
    }

    if (pos == start) {
      throw new IllegalStateException(
          "Expected identifier at position " + pos + " in: " + input);
    }

    return input.substring(start, pos);
  }

  private boolean skipWhitespace() {
    int start = pos;
    while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
      pos++;
    }
    return pos > start;
  }

  private static boolean isIdentStart(char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_' || c == '-';
  }

  private static boolean isIdentChar(char c) {
    return isIdentStart(c) || (c >= '0' && c <= '9');
  }
}
