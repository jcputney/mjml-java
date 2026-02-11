package dev.jcputney.mjml.render;

import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.css.CssSelector;
import dev.jcputney.mjml.css.CssSelectorMatcher;
import dev.jcputney.mjml.css.CssSelectorParser;
import dev.jcputney.mjml.css.HtmlDocumentParser;
import dev.jcputney.mjml.css.HtmlElement;
import dev.jcputney.mjml.util.HtmlEscaper;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Applies {@code mj-html-attributes} to rendered HTML by matching CSS selectors against elements
 * and inserting the specified attributes.
 */
final class HtmlAttributeApplier {

  private static final Pattern VALID_ATTR_NAME = Pattern.compile("[a-zA-Z][a-zA-Z0-9-]*");

  private HtmlAttributeApplier() {}

  /** Applies mj-html-attributes from the global context to the rendered HTML. */
  static String apply(String html, GlobalContext ctx) {
    Map<String, Map<String, String>> htmlAttrs = ctx.attributes().getHtmlAttributes();
    if (htmlAttrs.isEmpty()) {
      return html;
    }

    HtmlElement root = HtmlDocumentParser.parse(html);
    return applyToElements(html, root, ctx);
  }

  /**
   * Applies mj-html-attributes from the global context to the rendered HTML, using a pre-parsed
   * element tree to avoid re-parsing.
   *
   * @param html the original HTML string (for position-based modification)
   * @param root the pre-parsed element tree
   * @param ctx the global context containing html attributes
   * @return the HTML with attributes applied
   */
  static String applyToElements(String html, HtmlElement root, GlobalContext ctx) {
    Map<String, Map<String, String>> htmlAttrs = ctx.attributes().getHtmlAttributes();
    if (htmlAttrs.isEmpty()) {
      return html;
    }

    List<HtmlElement> allElements = root.allDescendants();

    // Track attribute insertions by position (descending to avoid offset shifts)
    TreeMap<Integer, String> insertions = new TreeMap<>();

    for (Map.Entry<String, Map<String, String>> entry : htmlAttrs.entrySet()) {
      String selectorText = entry.getKey();
      Map<String, String> attrs = entry.getValue();

      CssSelector selector = CssSelectorParser.parse(selectorText);
      if (selector == null) {
        continue;
      }

      for (HtmlElement element : allElements) {
        if (CssSelectorMatcher.matches(selector, element)) {
          if (element.hasPositionInfo()) {
            // Insert attributes before the closing > of the opening tag
            int insertPos = element.getTagEnd() - 1;
            // Check for self-closing tag
            if (insertPos > 0 && html.charAt(insertPos - 1) == '/') {
              insertPos = insertPos - 1;
              while (insertPos > element.getTagStart() && html.charAt(insertPos - 1) == ' ') {
                insertPos--;
              }
            }
            StringBuilder attrStr = new StringBuilder();
            for (Map.Entry<String, String> attr : attrs.entrySet()) {
              // Validate attribute name (alphanumeric + hyphens only)
              String attrName = attr.getKey();
              if (!VALID_ATTR_NAME.matcher(attrName).matches()) {
                continue;
              }
              attrStr
                  .append(' ')
                  .append(attrName)
                  .append("=\"")
                  .append(HtmlEscaper.escapeAttributeValue(attr.getValue()))
                  .append('"');
            }
            // Merge with existing insertion at same position
            String existing = insertions.getOrDefault(insertPos, "");
            insertions.put(insertPos, existing + attrStr);
          }
        }
      }
    }

    if (insertions.isEmpty()) {
      return html;
    }

    // Apply insertions in reverse order
    StringBuilder sb = new StringBuilder(html);
    for (Map.Entry<Integer, String> ins : insertions.descendingMap().entrySet()) {
      sb.insert(ins.getKey(), ins.getValue());
    }
    return sb.toString();
  }
}
