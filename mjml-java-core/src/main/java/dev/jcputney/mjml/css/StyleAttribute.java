package dev.jcputney.mjml.css;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses, merges, and serializes CSS inline style attributes. Handles specificity-aware merging
 * where higher specificity or later source order wins, and {@code !important} declarations take
 * precedence.
 */
public final class StyleAttribute {

  private StyleAttribute() {}

  /**
   * Parses an inline style string into a list of declarations. Example: "color: red; font-size:
   * 14px" -> [CssDeclaration("color","red",false), ...]
   *
   * @param style the inline CSS style string to parse, or {@code null}
   * @return a list of parsed {@link CssDeclaration} instances (empty if input is null or blank)
   */
  public static List<CssDeclaration> parse(String style) {
    List<CssDeclaration> result = new ArrayList<>();
    if (style == null || style.isBlank()) {
      return result;
    }

    for (String part : splitDeclarations(style)) {
      CssDeclaration decl = CssDeclaration.parse(part);
      if (decl != null) {
        result.add(decl);
      }
    }
    return result;
  }

  /**
   * Serializes a list of declarations back to an inline style string.
   *
   * @param declarations the list of CSS declarations to serialize
   * @return the serialized inline style string, or an empty string if declarations is null or empty
   */
  public static String serialize(List<CssDeclaration> declarations) {
    if (declarations == null || declarations.isEmpty()) {
      return "";
    }

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < declarations.size(); i++) {
      if (i > 0) {
        sb.append(" ");
      }
      CssDeclaration d = declarations.get(i);
      sb.append(d.property()).append(": ").append(d.value());
      if (d.important()) {
        sb.append(" !important");
      }
      sb.append(";");
    }
    return sb.toString();
  }

  /**
   * Merges new declarations into existing inline style declarations. Rules:
   *
   * <ol>
   *   <li>If existing has !important and new doesn't, existing wins
   *   <li>If new has !important, new wins
   *   <li>If neither or both have !important, new wins (later source order)
   * </ol>
   *
   * @param existing the current inline style declarations
   * @param incoming the new declarations to merge
   * @param incomingSpec specificity of the incoming rule (unused if inline=true)
   * @return merged declarations list
   */
  public static List<CssDeclaration> merge(
      List<CssDeclaration> existing, List<CssDeclaration> incoming, CssSpecificity incomingSpec) {

    // Use a map to track declarations by property, preserving insertion order
    Map<String, CssDeclaration> merged = new LinkedHashMap<>();

    // Add existing declarations
    for (CssDeclaration decl : existing) {
      merged.put(decl.property(), decl);
    }

    // Merge incoming declarations
    for (CssDeclaration newDecl : incoming) {
      CssDeclaration existingDecl = merged.get(newDecl.property());
      if (existingDecl == null) {
        // New property - always add
        merged.put(newDecl.property(), newDecl);
      } else if (existingDecl.important() && !newDecl.important()) {
        // Existing is !important and new is not - keep existing
      } else {
        // New wins: either new is !important, or neither/both are
        merged.put(newDecl.property(), newDecl);
      }
    }

    return new ArrayList<>(merged.values());
  }

  /**
   * Splits a style string on semicolons, being careful not to split within url() or quoted strings.
   */
  private static List<String> splitDeclarations(String style) {
    List<String> parts = new ArrayList<>();
    int depth = 0; // parentheses depth
    boolean inSingle = false;
    boolean inDouble = false;
    int start = 0;

    for (int i = 0; i < style.length(); i++) {
      char c = style.charAt(i);

      if (c == '\'' && !inDouble) {
        inSingle = !inSingle;
      } else if (c == '"' && !inSingle) {
        inDouble = !inDouble;
      } else if (c == '(' && !inSingle && !inDouble) {
        depth++;
      } else if (c == ')' && !inSingle && !inDouble) {
        depth = Math.max(0, depth - 1);
      } else if (c == ';' && depth == 0 && !inSingle && !inDouble) {
        String part = style.substring(start, i).trim();
        if (!part.isEmpty()) {
          parts.add(part);
        }
        start = i + 1;
      }
    }

    // Last part (no trailing semicolon)
    String last = style.substring(start).trim();
    if (!last.isEmpty()) {
      parts.add(last);
    }

    return parts;
  }
}
