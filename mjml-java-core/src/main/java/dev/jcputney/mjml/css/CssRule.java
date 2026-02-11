package dev.jcputney.mjml.css;

import java.util.List;

/**
 * A CSS rule consisting of a selector string and a list of declarations. For example: {@code .red {
 * color: red; font-weight: bold; }}
 *
 * @param selectorText the raw selector text (may contain commas for selector lists)
 * @param declarations the list of property declarations in this rule
 */
public record CssRule(String selectorText, List<CssDeclaration> declarations) {

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(selectorText).append(" { ");
    for (int i = 0; i < declarations.size(); i++) {
      if (i > 0) {
        sb.append(" ");
      }
      sb.append(declarations.get(i)).append(";");
    }
    sb.append(" }");
    return sb.toString();
  }
}
