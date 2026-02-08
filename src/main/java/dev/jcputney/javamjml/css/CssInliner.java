package dev.jcputney.javamjml.css;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Inlines CSS styles into HTML elements' style attributes.
 * <p>
 * This is a standalone utility that can be used independently of the MJML renderer.
 * It operates on the final HTML output, matching CSS rules to elements and merging
 * styles into inline {@code style=""} attributes.
 * <p>
 * Rules with pseudo-classes ({@code :hover}), pseudo-elements ({@code ::before}),
 * {@code @media} queries, {@code @font-face}, and {@code @keyframes} are preserved
 * in {@code <style>} blocks rather than being inlined.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * // Inline styles from <style> blocks in the HTML
 * String inlined = CssInliner.inline(html);
 *
 * // Inline additional CSS alongside <style> blocks
 * String inlined = CssInliner.inline(html, additionalCss);
 * }</pre>
 */
public final class CssInliner {

  private CssInliner() {
  }

  /**
   * Inlines CSS from {@code <style>} blocks in the HTML into element style attributes.
   * Non-inlineable rules (pseudo-classes, @media, etc.) are preserved in a single
   * {@code <style>} block in the output.
   *
   * @param html the HTML string
   * @return the HTML with CSS inlined
   */
  public static String inline(String html) {
    return inline(html, null);
  }

  /**
   * Inlines CSS from {@code <style>} blocks and additional CSS into element style attributes.
   *
   * @param html          the HTML string
   * @param additionalCss optional additional CSS to inline (may be null)
   * @return the HTML with CSS inlined
   */
  public static String inline(String html, String additionalCss) {
    if (html == null || html.isEmpty()) {
      return html;
    }

    // 1. Extract <style> blocks from HTML
    HtmlDocumentParser.StyleExtractionResult extracted =
        HtmlDocumentParser.extractStyles(html);
    String cleanHtml = extracted.html();
    String extractedCss = extracted.css();

    // Combine extracted CSS with additional CSS
    StringBuilder allCss = new StringBuilder();
    if (extractedCss != null && !extractedCss.isBlank()) {
      allCss.append(extractedCss).append("\n");
    }
    if (additionalCss != null && !additionalCss.isBlank()) {
      allCss.append(additionalCss);
    }

    if (allCss.isEmpty()) {
      return html; // No CSS to inline
    }

    // 2. Parse CSS into rules and preserved at-rules
    CssParser.ParseResult parseResult = CssParser.parse(allCss.toString());
    List<CssRule> rules = parseResult.rules();
    List<String> preservedAtRules = parseResult.preservedAtRules();

    // 3. Parse HTML into element tree
    HtmlElement root = HtmlDocumentParser.parse(cleanHtml);

    // 4. Separate inlineable rules from pseudo rules
    List<CssRule> inlineableRules = new ArrayList<>();
    List<CssRule> pseudoRules = new ArrayList<>();

    for (CssRule rule : rules) {
      CssSelector selector = CssSelectorParser.parse(rule.selectorText());
      if (selector != null && CssSelectorMatcher.hasPseudo(selector)) {
        pseudoRules.add(rule);
      } else {
        inlineableRules.add(rule);
      }
    }

    // 5. For each inlineable rule, match against all elements and collect styles
    // We need to track rule order for source-order precedence
    List<HtmlElement> allElements = root.allDescendants();

    // For each element, collect applicable declarations with their specificity and order
    record AppliedStyle(CssSpecificity specificity, int order,
                        List<CssDeclaration> declarations) {
    }

    for (HtmlElement element : allElements) {
      // Collect all matching rules for this element
      List<AppliedStyle> applicableStyles = new ArrayList<>();

      for (int ruleIndex = 0; ruleIndex < inlineableRules.size(); ruleIndex++) {
        CssRule rule = inlineableRules.get(ruleIndex);
        CssSelector selector = CssSelectorParser.parse(rule.selectorText());
        if (selector != null && CssSelectorMatcher.matches(selector, element)) {
          applicableStyles.add(
              new AppliedStyle(selector.specificity(), ruleIndex, rule.declarations()));
        }
      }

      if (applicableStyles.isEmpty()) {
        continue;
      }

      // Sort by specificity, then by source order
      applicableStyles.sort((a, b) -> {
        int cmp = a.specificity().compareTo(b.specificity());
        if (cmp != 0) {
          return cmp;
        }
        return Integer.compare(a.order(), b.order());
      });

      // Parse existing inline style
      List<CssDeclaration> existingStyle = StyleAttribute.parse(element.getStyle());

      // Merge each matching rule's declarations (in specificity + order)
      List<CssDeclaration> merged = existingStyle;
      for (AppliedStyle applied : applicableStyles) {
        merged = StyleAttribute.merge(merged, applied.declarations(), applied.specificity());
      }

      // Serialize back
      element.setStyle(StyleAttribute.serialize(merged));
    }

    // 6. Rebuild HTML with modified style attributes
    String result = rebuildHtml(cleanHtml, allElements);

    // 7. Re-insert preserved rules as <style> block
    StringBuilder preservedCss = new StringBuilder();
    for (CssRule rule : pseudoRules) {
      preservedCss.append(rule).append("\n");
    }
    for (String atRule : preservedAtRules) {
      preservedCss.append(atRule).append("\n");
    }

    if (!preservedCss.isEmpty()) {
      result = insertStyleBlock(result, preservedCss.toString());
    }

    return result;
  }

  /**
   * Inlines only the provided CSS into HTML elements' style attributes, without extracting
   * or removing any existing {@code <style>} blocks from the HTML. This is used by the
   * MJML renderer for {@code <mj-style inline="inline">} content, where base CSS resets
   * in the head must be preserved.
   *
   * @param html the HTML string
   * @param css  the CSS to inline
   * @return the HTML with the provided CSS inlined into matching elements
   */
  public static String inlineAdditionalOnly(String html, String css) {
    if (html == null || html.isEmpty() || css == null || css.isBlank()) {
      return html;
    }

    // 1. Parse CSS into rules (no extraction from HTML)
    CssParser.ParseResult parseResult = CssParser.parse(css);
    List<CssRule> rules = parseResult.rules();

    // 2. Parse HTML into element tree (with existing <style> blocks intact)
    HtmlElement root = HtmlDocumentParser.parse(html);

    // 3. Only inline rules that don't have pseudo-selectors
    List<CssRule> inlineableRules = new ArrayList<>();
    for (CssRule rule : rules) {
      CssSelector selector = CssSelectorParser.parse(rule.selectorText());
      if (selector != null && !CssSelectorMatcher.hasPseudo(selector)) {
        inlineableRules.add(rule);
      }
    }

    if (inlineableRules.isEmpty()) {
      return html;
    }

    // 4. Match and apply styles — only track elements that are actually modified
    List<HtmlElement> allElements = root.allDescendants();
    List<HtmlElement> modifiedElements = new ArrayList<>();

    record AppliedStyle(CssSpecificity specificity, int order,
                        List<CssDeclaration> declarations) {
    }

    for (HtmlElement element : allElements) {
      List<AppliedStyle> applicableStyles = new ArrayList<>();

      for (int ruleIndex = 0; ruleIndex < inlineableRules.size(); ruleIndex++) {
        CssRule rule = inlineableRules.get(ruleIndex);
        CssSelector selector = CssSelectorParser.parse(rule.selectorText());
        if (selector != null && CssSelectorMatcher.matches(selector, element)) {
          applicableStyles.add(
              new AppliedStyle(selector.specificity(), ruleIndex, rule.declarations()));
        }
      }

      if (applicableStyles.isEmpty()) {
        continue;
      }

      applicableStyles.sort((a, b) -> {
        int cmp = a.specificity().compareTo(b.specificity());
        if (cmp != 0) {
          return cmp;
        }
        return Integer.compare(a.order(), b.order());
      });

      List<CssDeclaration> existingStyle = StyleAttribute.parse(element.getStyle());
      List<CssDeclaration> merged = existingStyle;
      for (AppliedStyle applied : applicableStyles) {
        merged = StyleAttribute.merge(merged, applied.declarations(), applied.specificity());
      }

      element.setStyle(StyleAttribute.serialize(merged));
      modifiedElements.add(element);
    }

    if (modifiedElements.isEmpty()) {
      return html;
    }

    // 5. Rebuild HTML with only modified style attributes (existing <style> blocks remain intact)
    return rebuildHtml(html, modifiedElements);
  }

  /**
   * Rebuilds the HTML string with updated style attributes.
   * Uses position tracking from the parser to make in-place modifications.
   */
  private static String rebuildHtml(String html, List<HtmlElement> elements) {
    // Collect all style changes, sorted by position (descending to avoid offset shifts)
    TreeMap<Integer, StyleChange> changes = new TreeMap<>();

    for (HtmlElement element : elements) {
      String newStyle = element.getStyle();
      if (newStyle == null || newStyle.isEmpty()) {
        continue;
      }

      if (element.getStyleAttrStart() >= 0 && element.getStyleAttrEnd() >= 0) {
        // Element had an existing style attribute - replace its value
        changes.put(element.getStyleAttrStart(),
            new StyleChange(element.getStyleAttrStart(), element.getStyleAttrEnd(), newStyle));
      } else if (element.hasPositionInfo()) {
        // Element has no style attribute - insert one
        // Insert before the closing > of the opening tag
        int insertPos = element.getTagEnd() - 1;
        // Check if it's a self-closing tag
        if (insertPos > 0 && html.charAt(insertPos - 1) == '/') {
          insertPos = insertPos - 1;
          // Skip whitespace before /
          while (insertPos > element.getTagStart() && html.charAt(insertPos - 1) == ' ') {
            insertPos--;
          }
        }
        changes.put(insertPos,
            new StyleChange(insertPos, insertPos, " style=\"" + newStyle + "\""));
      }
    }

    if (changes.isEmpty()) {
      return html;
    }

    // Apply changes in reverse order (descending position)
    StringBuilder sb = new StringBuilder(html);
    for (StyleChange change : changes.descendingMap().values()) {
      sb.replace(change.start(), change.end(), change.replacement());
    }

    return sb.toString();
  }

  /**
   * Inserts a &lt;style&gt; block into the &lt;head&gt; of the HTML document.
   */
  private static String insertStyleBlock(String html, String css) {
    String styleBlock = "<style type=\"text/css\">\n" + css + "</style>\n";

    // Try to insert before </head>
    int headClose = html.toLowerCase().indexOf("</head>");
    if (headClose >= 0) {
      return html.substring(0, headClose) + styleBlock + html.substring(headClose);
    }

    // Fallback: insert after <body> tag
    int bodyStart = html.toLowerCase().indexOf("<body");
    if (bodyStart >= 0) {
      int bodyEnd = html.indexOf('>', bodyStart);
      if (bodyEnd >= 0) {
        return html.substring(0, bodyEnd + 1) + "\n" + styleBlock + html.substring(bodyEnd + 1);
      }
    }

    // Last resort: prepend
    return styleBlock + html;
  }

  private record StyleChange(int start, int end, String replacement) {
  }
}
