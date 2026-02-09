package dev.jcputney.mjml.css;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses CSS text into a list of {@link CssRule} objects.
 * <p>
 * Handles:
 * <ul>
 *   <li>Regular rules (selector { declarations })</li>
 *   <li>Comments (/* ... *{@literal /})</li>
 *   <li>@-rules (@media, @font-face, @keyframes) which are preserved but not parsed as rules</li>
 * </ul>
 * <p>
 * @-rules that should not be inlined are collected separately so they can be
 * placed back into {@code <style>} blocks in the output.
 */
public final class CssParser {

  private CssParser() {
  }

  /**
   * Result of parsing CSS: regular rules to inline and preserved at-rules.
   */
  public record ParseResult(List<CssRule> rules, List<String> preservedAtRules) {
  }

  /**
   * Parses CSS text into rules and preserved at-rules.
   *
   * @param css the CSS text to parse
   * @return parse result with rules and preserved at-rules
   */
  public static ParseResult parse(String css) {
    List<CssRule> rules = new ArrayList<>();
    List<String> preserved = new ArrayList<>();

    if (css == null || css.isBlank()) {
      return new ParseResult(rules, preserved);
    }

    // Strip comments
    String stripped = stripComments(css);

    int pos = 0;
    int len = stripped.length();

    while (pos < len) {
      // Skip whitespace
      pos = skipWhitespace(stripped, pos);
      if (pos >= len) {
        break;
      }

      if (stripped.charAt(pos) == '@') {
        // @-rule
        pos = parseAtRule(stripped, pos, rules, preserved);
      } else {
        // Regular rule
        pos = parseRule(stripped, pos, rules);
      }
    }

    return new ParseResult(rules, preserved);
  }

  /**
   * Parses CSS text and returns only the inlineable rules (convenience method).
   */
  public static List<CssRule> parseRules(String css) {
    return parse(css).rules();
  }

  /**
   * Strips CSS comments (/* ... *{@literal /}).
   */
  private static String stripComments(String css) {
    StringBuilder sb = new StringBuilder(css.length());
    int i = 0;
    while (i < css.length()) {
      if (i + 1 < css.length() && css.charAt(i) == '/' && css.charAt(i + 1) == '*') {
        // Find end of comment
        int end = css.indexOf("*/", i + 2);
        if (end < 0) {
          break; // Unterminated comment
        }
        i = end + 2;
      } else {
        sb.append(css.charAt(i));
        i++;
      }
    }
    return sb.toString();
  }

  /**
   * Parses an @-rule starting at pos. Handles both block (@media {}) and
   * statement (@import ..;) at-rules.
   */
  private static int parseAtRule(String css, int pos, List<CssRule> rules,
      List<String> preserved) {
    int len = css.length();

    // Find the at-rule name
    int nameStart = pos + 1;
    int nameEnd = nameStart;
    while (nameEnd < len && !Character.isWhitespace(css.charAt(nameEnd))
        && css.charAt(nameEnd) != '{' && css.charAt(nameEnd) != ';') {
      nameEnd++;
    }
    String name = css.substring(nameStart, nameEnd).toLowerCase();

    if ("media".equals(name) || "keyframes".equals(name) || "font-face".equals(name)
        || name.startsWith("-webkit-keyframes") || name.startsWith("-moz-keyframes")) {
      // Block at-rule - find matching closing brace
      int braceStart = css.indexOf('{', nameEnd);
      if (braceStart < 0) {
        return len; // Malformed
      }

      int braceEnd = findMatchingBrace(css, braceStart);
      if (braceEnd < 0) {
        return len; // Malformed
      }

      String atRule = css.substring(pos, braceEnd + 1).trim();
      preserved.add(atRule);
      return braceEnd + 1;
    } else {
      // Statement at-rule (e.g. @import, @charset) - find semicolon
      int semi = css.indexOf(';', pos);
      if (semi < 0) {
        return len;
      }
      // We don't preserve @import/@charset - they're not relevant for inlining
      return semi + 1;
    }
  }

  /**
   * Parses a regular CSS rule starting at pos.
   */
  private static int parseRule(String css, int pos, List<CssRule> rules) {
    int len = css.length();

    // Find the opening brace
    int braceStart = css.indexOf('{', pos);
    if (braceStart < 0) {
      return len; // Malformed
    }

    String selector = css.substring(pos, braceStart).trim();
    if (selector.isEmpty()) {
      return braceStart + 1;
    }

    // Find the closing brace
    int braceEnd = findMatchingBrace(css, braceStart);
    if (braceEnd < 0) {
      return len;
    }

    String body = css.substring(braceStart + 1, braceEnd).trim();
    List<CssDeclaration> declarations = parseDeclarations(body);

    if (!declarations.isEmpty()) {
      rules.add(new CssRule(selector, declarations));
    }

    return braceEnd + 1;
  }

  /**
   * Parses a semicolon-separated list of declarations.
   */
  static List<CssDeclaration> parseDeclarations(String body) {
    List<CssDeclaration> declarations = new ArrayList<>();
    if (body == null || body.isBlank()) {
      return declarations;
    }

    // Split on semicolons, handling url() and quoted strings
    int start = 0;
    int depth = 0;
    boolean inSingle = false;
    boolean inDouble = false;

    for (int i = 0; i < body.length(); i++) {
      char c = body.charAt(i);
      if (c == '\'' && !inDouble) {
        inSingle = !inSingle;
      } else if (c == '"' && !inSingle) {
        inDouble = !inDouble;
      } else if (c == '(' && !inSingle && !inDouble) {
        depth++;
      } else if (c == ')' && !inSingle && !inDouble) {
        depth = Math.max(0, depth - 1);
      } else if (c == ';' && depth == 0 && !inSingle && !inDouble) {
        String part = body.substring(start, i).trim();
        CssDeclaration decl = CssDeclaration.parse(part);
        if (decl != null) {
          declarations.add(decl);
        }
        start = i + 1;
      }
    }

    // Last declaration (no trailing semicolon)
    String last = body.substring(start).trim();
    CssDeclaration decl = CssDeclaration.parse(last);
    if (decl != null) {
      declarations.add(decl);
    }

    return declarations;
  }

  /**
   * Finds the matching closing brace for an opening brace at pos.
   * Handles nested braces.
   */
  private static int findMatchingBrace(String css, int openPos) {
    int depth = 1;
    boolean inSingle = false;
    boolean inDouble = false;

    for (int i = openPos + 1; i < css.length(); i++) {
      char c = css.charAt(i);
      if (c == '\'' && !inDouble) {
        inSingle = !inSingle;
      } else if (c == '"' && !inSingle) {
        inDouble = !inDouble;
      } else if (!inSingle && !inDouble) {
        if (c == '{') {
          depth++;
        } else if (c == '}') {
          depth--;
          if (depth == 0) {
            return i;
          }
        }
      }
    }
    return -1;
  }

  private static int skipWhitespace(String css, int pos) {
    while (pos < css.length() && Character.isWhitespace(css.charAt(pos))) {
      pos++;
    }
    return pos;
  }
}
