package dev.jcputney.mjml.css;

import dev.jcputney.mjml.css.CssSelector.ClassSelector;
import dev.jcputney.mjml.css.CssSelector.ComplexSelector;
import dev.jcputney.mjml.css.CssSelector.CompoundSelector;
import dev.jcputney.mjml.css.CssSelector.IdSelector;
import dev.jcputney.mjml.css.CssSelector.SelectorList;
import dev.jcputney.mjml.css.CssSelector.SimpleSelector;
import dev.jcputney.mjml.css.CssSelector.TypeSelector;
import dev.jcputney.mjml.css.CssSelector.UniversalSelector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * Inlines CSS styles into HTML elements' style attributes.
 *
 * <p>This is a standalone utility that can be used independently of the MJML renderer. It operates
 * on the final HTML output, matching CSS rules to elements and merging styles into inline {@code
 * style=""} attributes.
 *
 * <p>Rules with pseudo-classes ({@code :hover}), pseudo-elements ({@code ::before}), {@code @media}
 * queries, {@code @font-face}, and {@code @keyframes} are preserved in {@code <style>} blocks
 * rather than being inlined.
 *
 * <h2>Usage</h2>
 *
 * <pre>{@code
 * // Inline styles from <style> blocks in the HTML
 * String inlined = CssInliner.inline(html);
 *
 * // Inline additional CSS alongside <style> blocks
 * String inlined = CssInliner.inline(html, additionalCss);
 * }</pre>
 */
public final class CssInliner {

  private static final Logger LOG = Logger.getLogger(CssInliner.class.getName());

  private CssInliner() {}

  /**
   * Inlines CSS from {@code <style>} blocks in the HTML into element style attributes.
   * Non-inlineable rules (pseudo-classes, @media, etc.) are preserved in a single {@code <style>}
   * block in the output.
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
   * @param html the HTML string
   * @param additionalCss optional additional CSS to inline (may be null)
   * @return the HTML with CSS inlined
   */
  public static String inline(String html, String additionalCss) {
    if (html == null || html.isEmpty()) {
      return html;
    }

    // 1. Extract <style> blocks from HTML
    HtmlDocumentParser.StyleExtractionResult extracted = HtmlDocumentParser.extractStyles(html);
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
    LOG.fine(() -> "CSS inliner: " + rules.size() + " rules parsed from style blocks");

    // 4. Parse selectors once and separate inlineable rules from pseudo rules
    List<ParsedRule> inlineableRules = new ArrayList<>();
    List<CssRule> pseudoRules = new ArrayList<>();

    for (CssRule rule : rules) {
      CssSelector selector = CssSelectorParser.parse(rule.selectorText());
      if (CssSelectorMatcher.hasPseudo(selector)) {
        pseudoRules.add(rule);
      } else if (selector != null) {
        inlineableRules.add(new ParsedRule(selector, rule));
      }
    }

    LOG.fine(
        () ->
            "CSS inliner: "
                + inlineableRules.size()
                + " inlineable, "
                + pseudoRules.size()
                + " pseudo rules");

    // 5. Match and apply styles to all elements
    List<HtmlElement> allElements = root.allDescendants();
    matchAndApplyStyles(inlineableRules, allElements);

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
   * Inlines only the provided CSS into HTML elements' style attributes, without extracting or
   * removing any existing {@code <style>} blocks from the HTML. This is used by the MJML renderer
   * for {@code <mj-style inline="inline">} content, where base CSS resets in the head must be
   * preserved.
   *
   * @param html the HTML string
   * @param css the CSS to inline
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

    return inlineAdditionalOnlyWithElements(html, root, rules);
  }

  /**
   * Inlines only the provided CSS into HTML elements' style attributes using a pre-parsed element
   * tree, avoiding redundant HTML re-parsing when the tree is already available.
   *
   * @param html the original HTML string (for position-based modification)
   * @param root the pre-parsed element tree
   * @param css the CSS to inline
   * @return the HTML with the provided CSS inlined into matching elements
   */
  public static String inlineAdditionalOnly(String html, HtmlElement root, String css) {
    if (html == null || html.isEmpty() || css == null || css.isBlank()) {
      return html;
    }

    CssParser.ParseResult parseResult = CssParser.parse(css);
    return inlineAdditionalOnlyWithElements(html, root, parseResult.rules());
  }

  private static String inlineAdditionalOnlyWithElements(
      String html, HtmlElement root, List<CssRule> rules) {
    // Parse selectors once and filter to inlineable rules
    List<ParsedRule> inlineableRules = new ArrayList<>();
    for (CssRule rule : rules) {
      CssSelector selector = CssSelectorParser.parse(rule.selectorText());
      if (selector != null && !CssSelectorMatcher.hasPseudo(selector)) {
        inlineableRules.add(new ParsedRule(selector, rule));
      }
    }

    if (inlineableRules.isEmpty()) {
      return html;
    }

    // Match and apply styles, tracking modified elements
    List<HtmlElement> allElements = root.allDescendants();
    List<HtmlElement> modifiedElements = matchAndApplyStyles(inlineableRules, allElements);

    if (modifiedElements.isEmpty()) {
      return html;
    }

    // Rebuild HTML with only modified style attributes (existing <style> blocks remain intact)
    return rebuildHtml(html, modifiedElements);
  }

  /**
   * Matches CSS rules against HTML elements and merges matched styles into each element's inline
   * style attribute. Uses element indexes by tag name and class name to pre-filter candidates,
   * reducing O(n*m) to O(n + m*k) where k is the average candidate set size.
   *
   * @return the list of elements whose styles were actually modified
   */
  private static List<HtmlElement> matchAndApplyStyles(
      List<ParsedRule> inlineableRules, List<HtmlElement> elements) {
    // Build element indexes for fast candidate filtering
    ElementIndex index = new ElementIndex(elements);

    // For each element, collect applicable rules
    // We iterate per-element to maintain correct specificity ordering
    Map<HtmlElement, List<AppliedStyle>> elementStyles = new HashMap<>();

    for (int ruleIndex = 0; ruleIndex < inlineableRules.size(); ruleIndex++) {
      ParsedRule parsed = inlineableRules.get(ruleIndex);
      List<HtmlElement> candidates = index.getCandidates(parsed.selector());

      for (HtmlElement candidate : candidates) {
        if (CssSelectorMatcher.matches(parsed.selector(), candidate)) {
          elementStyles
              .computeIfAbsent(candidate, k -> new ArrayList<>())
              .add(
                  new AppliedStyle(
                      parsed.selector().specificity(), ruleIndex, parsed.rule().declarations()));
        }
      }
    }

    List<HtmlElement> modified = new ArrayList<>();
    for (HtmlElement element : elements) {
      List<AppliedStyle> applicableStyles = elementStyles.get(element);
      if (applicableStyles == null || applicableStyles.isEmpty()) {
        continue;
      }

      // Sort by specificity, then by source order
      applicableStyles.sort(
          (a, b) -> {
            int cmp = a.specificity().compareTo(b.specificity());
            if (cmp != 0) {
              return cmp;
            }
            return Integer.compare(a.order(), b.order());
          });

      // Parse existing inline style and merge
      List<CssDeclaration> existingStyle = StyleAttribute.parse(element.getStyle());
      List<CssDeclaration> merged = existingStyle;
      for (AppliedStyle applied : applicableStyles) {
        merged = StyleAttribute.merge(merged, applied.declarations(), applied.specificity());
      }

      element.setStyle(StyleAttribute.serialize(merged));
      modified.add(element);
    }

    return modified;
  }

  /**
   * Rebuilds the HTML string with updated style attributes. Uses position tracking from the parser
   * to make in-place modifications.
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
        changes.put(
            element.getStyleAttrStart(),
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
        changes.put(
            insertPos, new StyleChange(insertPos, insertPos, " style=\"" + newStyle + "\""));
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

  /** Inserts a &lt;style&gt; block into the &lt;head&gt; of the HTML document. */
  private static String insertStyleBlock(String html, String css) {
    String styleBlock = "<style type=\"text/css\">\n" + css + "</style>\n";

    String lower = html.toLowerCase();

    // Try to insert before </head>
    int headClose = lower.indexOf("</head>");
    if (headClose >= 0) {
      return html.substring(0, headClose) + styleBlock + html.substring(headClose);
    }

    // Fallback: insert after <body> tag
    int bodyStart = lower.indexOf("<body");
    if (bodyStart >= 0) {
      int bodyEnd = html.indexOf('>', bodyStart);
      if (bodyEnd >= 0) {
        return html.substring(0, bodyEnd + 1) + "\n" + styleBlock + html.substring(bodyEnd + 1);
      }
    }

    // Last resort: prepend
    return styleBlock + html;
  }

  private record ParsedRule(CssSelector selector, CssRule rule) {}

  private record AppliedStyle(
      CssSpecificity specificity, int order, List<CssDeclaration> declarations) {}

  /** Index of HTML elements by tag name and class name for fast candidate pre-filtering. */
  private static final class ElementIndex {

    private final Map<String, List<HtmlElement>> byTagName = new HashMap<>();
    private final Map<String, List<HtmlElement>> byClassName = new HashMap<>();
    private final Map<String, List<HtmlElement>> byId = new HashMap<>();
    private final List<HtmlElement> allElements;

    ElementIndex(List<HtmlElement> elements) {
      this.allElements = elements;
      for (HtmlElement element : elements) {
        byTagName.computeIfAbsent(element.getTagName(), k -> new ArrayList<>()).add(element);
        for (String cls : element.getClassNames()) {
          byClassName.computeIfAbsent(cls, k -> new ArrayList<>()).add(element);
        }
        String id = element.getId();
        if (id != null && !id.isEmpty()) {
          byId.computeIfAbsent(id, k -> new ArrayList<>()).add(element);
        }
      }
    }

    /**
     * Extracts the rightmost (key) selector from a complex selector chain. For "div > p.foo", this
     * returns "p.foo" since that's what the element must match directly.
     */
    private static CssSelector getRightmostSelector(CssSelector selector) {
      if (selector instanceof ComplexSelector complex) {
        return getRightmostSelector(complex.right());
      }
      return selector;
    }

    /**
     * Returns a pre-filtered list of candidate elements that could possibly match the given
     * selector. Falls back to all elements for selectors that can't be pre-filtered.
     */
    List<HtmlElement> getCandidates(CssSelector selector) {
      // Extract the rightmost/key selector from complex selectors
      CssSelector key = getRightmostSelector(selector);
      if (key == null) {
        return allElements;
      }

      // For selector lists, union candidates from each selector
      if (key instanceof SelectorList list) {
        Set<HtmlElement> union = new HashSet<>();
        for (CssSelector s : list.selectors()) {
          union.addAll(getCandidates(s));
        }
        return new ArrayList<>(union);
      }

      // Extract filtering info from compound or simple selectors
      String tagName = null;
      String className = null;
      String id = null;

      if (key instanceof CompoundSelector compound) {
        for (SimpleSelector part : compound.parts()) {
          if (part instanceof TypeSelector type) {
            tagName = type.tagName().toLowerCase();
          } else if (part instanceof ClassSelector cls) {
            className = cls.className();
          } else if (part instanceof IdSelector idSel) {
            id = idSel.id();
          }
        }
      } else if (key instanceof TypeSelector type) {
        tagName = type.tagName().toLowerCase();
      } else if (key instanceof ClassSelector cls) {
        className = cls.className();
      } else if (key instanceof IdSelector idSel) {
        id = idSel.id();
      } else if (key instanceof UniversalSelector) {
        return allElements;
      }

      // Use the most specific index available (ID > class > tag)
      if (id != null) {
        List<HtmlElement> result = byId.get(id);
        return result != null ? result : List.of();
      }
      if (className != null) {
        List<HtmlElement> result = byClassName.get(className);
        return result != null ? result : List.of();
      }
      if (tagName != null) {
        List<HtmlElement> result = byTagName.get(tagName);
        return result != null ? result : List.of();
      }

      return allElements;
    }
  }

  private record StyleChange(int start, int end, String replacement) {}
}
