package dev.jcputney.mjml.css;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Lightweight HTML tokenizer that builds an {@link HtmlElement} tree from HTML text.
 *
 * <p>This is NOT a full HTML5 parser. It handles the well-structured HTML output from the MJML
 * renderer, including:
 *
 * <ul>
 *   <li>Opening tags with attributes
 *   <li>Self-closing tags
 *   <li>Closing tags
 *   <li>HTML comments (including MSO conditionals)
 *   <li>DOCTYPE declarations
 * </ul>
 *
 * <p>It tracks tag positions in the source string to enable in-place style modification.
 */
public final class HtmlDocumentParser {

  private static final Set<String> VOID_ELEMENTS =
      Set.of(
          "area", "base", "br", "col", "embed", "hr", "img", "input", "link", "meta", "param",
          "source", "track", "wbr");

  /** Raw text elements whose content should not be parsed as HTML. */
  private static final Set<String> RAW_TEXT_ELEMENTS = Set.of("style", "script");

  private HtmlDocumentParser() {}

  /**
   * Parses HTML into an element tree rooted at a virtual document element.
   *
   * @param html the HTML string
   * @return the root element containing the document tree
   */
  public static HtmlElement parse(String html) {
    HtmlElement root = new HtmlElement("#document", Map.of());
    if (html == null || html.isEmpty()) {
      return root;
    }

    Deque<HtmlElement> stack = new ArrayDeque<>();
    stack.push(root);

    int pos = 0;
    int len = html.length();

    while (pos < len) {
      int tagStart = html.indexOf('<', pos);
      if (tagStart < 0) {
        break;
      }
      pos = tagStart;

      int newPos = skipSpecialTag(html, pos, len);
      if (newPos < 0) {
        break;
      }
      if (newPos > pos) {
        pos = newPos;
        continue;
      }

      if (pos + 1 < len && html.charAt(pos + 1) == '/') {
        pos = handleClosingTag(html, pos, stack);
        continue;
      }

      pos = handleOpeningTag(html, pos, stack);
    }

    return root;
  }

  /**
   * Attempts to skip comments, DOCTYPE, CDATA, and processing instructions. Returns the new
   * position after skipping, or the original position if not a special tag, or -1 if the tag is
   * unterminated (signals parse loop should break).
   */
  private static int skipSpecialTag(String html, int pos, int len) {
    // Skip comments (including MSO conditionals)
    if (pos + 3 < len && html.startsWith("<!--", pos)) {
      int commentEnd = html.indexOf("-->", pos + 4);
      return commentEnd < 0 ? -1 : commentEnd + 3;
    }

    // Skip DOCTYPE
    if (pos + 8 < len && html.regionMatches(true, pos, "<!doctype", 0, 9)) {
      int docEnd = html.indexOf('>', pos);
      return docEnd < 0 ? -1 : docEnd + 1;
    }

    // Skip CDATA
    if (pos + 8 < len && html.startsWith("<![CDATA[", pos)) {
      int cdataEnd = html.indexOf("]]>", pos + 9);
      return cdataEnd < 0 ? -1 : cdataEnd + 3;
    }

    // Skip processing instructions
    if (pos + 1 < len && html.charAt(pos + 1) == '?') {
      int piEnd = html.indexOf("?>", pos);
      return piEnd < 0 ? -1 : piEnd + 2;
    }

    return pos;
  }

  /**
   * Handles a closing tag at the given position. Returns the new parse position, or -1 if the tag
   * is unterminated.
   */
  private static int handleClosingTag(String html, int pos, Deque<HtmlElement> stack) {
    int closeEnd = html.indexOf('>', pos);
    if (closeEnd < 0) {
      return html.length();
    }
    String tagName = html.substring(pos + 2, closeEnd).trim().toLowerCase();
    popUntilTag(stack, tagName);
    return closeEnd + 1;
  }

  /**
   * Handles an opening tag at the given position. Creates the element, adds it to the tree, and
   * optionally skips raw text content. Returns the new parse position.
   */
  private static int handleOpeningTag(String html, int pos, Deque<HtmlElement> stack) {
    int tagEnd = findTagEnd(html, pos);
    if (tagEnd < 0) {
      return html.length();
    }

    String tagContent = html.substring(pos + 1, tagEnd);
    boolean selfClosing = tagContent.endsWith("/");
    if (selfClosing) {
      tagContent = tagContent.substring(0, tagContent.length() - 1);
    }

    int firstSpace = indexOfWhitespace(tagContent);
    String tagName;
    String attrString;
    if (firstSpace < 0) {
      tagName = tagContent.trim().toLowerCase();
      attrString = "";
    } else {
      tagName = tagContent.substring(0, firstSpace).trim().toLowerCase();
      // Don't trim — parseAttributes skips leading whitespace internally,
      // and trimming shifts positions, breaking style attribute range tracking
      attrString = tagContent.substring(firstSpace);
    }

    if (tagName.isEmpty() || tagName.startsWith("!")) {
      return tagEnd + 1;
    }

    int[] styleRange = new int[] {-1, -1};
    Map<String, String> attrs =
        parseAttributes(
            attrString, pos + 1 + (firstSpace >= 0 ? firstSpace : tagContent.length()), styleRange);

    HtmlElement element = new HtmlElement(tagName, attrs);
    element.setTagStart(pos);
    element.setTagEnd(tagEnd + 1);
    if (styleRange[0] >= 0) {
      element.setStyleAttrStart(styleRange[0]);
      element.setStyleAttrEnd(styleRange[1]);
    }

    stack.peek().addChild(element);

    if (!selfClosing && !VOID_ELEMENTS.contains(tagName)) {
      stack.push(element);
    }

    int newPos = tagEnd + 1;

    if (!selfClosing && RAW_TEXT_ELEMENTS.contains(tagName)) {
      newPos = skipRawTextContent(html, tagName, newPos, stack);
    }

    return newPos;
  }

  /** Skips the content of a raw text element (style, script) and pops it from the stack. */
  private static int skipRawTextContent(
      String html, String tagName, int pos, Deque<HtmlElement> stack) {
    String closeTag = "</" + tagName;
    int closeStart = indexOfIgnoreCase(html, closeTag, pos);
    if (closeStart >= 0) {
      int closeEnd = html.indexOf('>', closeStart);
      if (closeEnd >= 0) {
        popUntilTag(stack, tagName);
        return closeEnd + 1;
      }
    }
    return pos;
  }

  /**
   * Extracts all CSS from &lt;style&gt; blocks in the HTML and returns the HTML with those blocks
   * removed.
   *
   * @param html the HTML string to extract styles from
   * @return a {@link StyleExtractionResult} containing the cleaned HTML and extracted CSS
   */
  public static StyleExtractionResult extractStyles(String html) {
    StringBuilder cleanHtml = new StringBuilder(html.length());
    StringBuilder css = new StringBuilder();

    int pos = 0;
    while (pos < html.length()) {
      // Find <style
      int styleStart = indexOfIgnoreCase(html, "<style", pos);
      if (styleStart < 0) {
        cleanHtml.append(html, pos, html.length());
        break;
      }

      // Append everything before <style
      cleanHtml.append(html, pos, styleStart);

      // Find > after <style
      int tagEnd = html.indexOf('>', styleStart);
      if (tagEnd < 0) {
        cleanHtml.append(html, styleStart, html.length());
        break;
      }

      // Find </style>
      int closeStart = indexOfIgnoreCase(html, "</style", tagEnd + 1);
      if (closeStart < 0) {
        cleanHtml.append(html, styleStart, html.length());
        break;
      }

      int closeEnd = html.indexOf('>', closeStart);
      if (closeEnd < 0) {
        cleanHtml.append(html, styleStart, html.length());
        break;
      }

      // Extract CSS content
      String cssContent = html.substring(tagEnd + 1, closeStart).trim();
      if (!cssContent.isEmpty()) {
        css.append(cssContent).append("\n");
      }

      pos = closeEnd + 1;
    }

    return new StyleExtractionResult(cleanHtml.toString(), css.toString());
  }

  private static int findTagEnd(String html, int tagStart) {
    boolean inSingle = false;
    boolean inDouble = false;
    for (int i = tagStart + 1; i < html.length(); i++) {
      char c = html.charAt(i);
      if (c == '\'' && !inDouble) {
        inSingle = !inSingle;
      } else if (c == '"' && !inSingle) {
        inDouble = !inDouble;
      } else if (c == '>' && !inSingle && !inDouble) {
        return i;
      }
    }
    return -1;
  }

  // --- Helper methods ---

  private static void popUntilTag(Deque<HtmlElement> stack, String tagName) {
    while (stack.size() > 1) {
      HtmlElement top = stack.peek();
      if (top.getTagName().equals(tagName)) {
        stack.pop();
        return;
      }
      // Implicit close of current element (tolerant parsing)
      stack.pop();
    }
  }

  private static Map<String, String> parseAttributes(
      String attrString, int baseOffset, int[] styleRange) {
    Map<String, String> attrs = new LinkedHashMap<>();
    if (attrString == null || attrString.isBlank()) {
      return attrs;
    }

    int[] posHolder = {0};
    int len = attrString.length();

    while (posHolder[0] < len) {
      skipWhitespace(attrString, posHolder, len);
      if (posHolder[0] >= len) {
        break;
      }

      // Parse attribute name
      int nameStart = posHolder[0];
      while (posHolder[0] < len
          && attrString.charAt(posHolder[0]) != '='
          && !Character.isWhitespace(attrString.charAt(posHolder[0]))
          && attrString.charAt(posHolder[0]) != '/') {
        posHolder[0]++;
      }

      if (posHolder[0] == nameStart) {
        posHolder[0]++;
        continue;
      }

      String name = attrString.substring(nameStart, posHolder[0]).toLowerCase();

      skipWhitespace(attrString, posHolder, len);

      if (posHolder[0] >= len || attrString.charAt(posHolder[0]) != '=') {
        attrs.put(name, "");
        continue;
      }
      posHolder[0]++; // consume '='

      skipWhitespace(attrString, posHolder, len);

      if (posHolder[0] >= len) {
        attrs.put(name, "");
        continue;
      }

      String value =
          parseAttrValue(attrString, posHolder, len, baseOffset, "style".equals(name), styleRange);
      attrs.put(name, value);
    }

    return attrs;
  }

  private static void skipWhitespace(String s, int[] posHolder, int len) {
    while (posHolder[0] < len && Character.isWhitespace(s.charAt(posHolder[0]))) {
      posHolder[0]++;
    }
  }

  private static String parseAttrValue(
      String attrString,
      int[] posHolder,
      int len,
      int baseOffset,
      boolean isStyle,
      int[] styleRange) {
    char quote = attrString.charAt(posHolder[0]);
    if (quote == '"' || quote == '\'') {
      posHolder[0]++; // consume opening quote
      int valueStart = posHolder[0];
      int absValueStart = baseOffset + posHolder[0];

      while (posHolder[0] < len && attrString.charAt(posHolder[0]) != quote) {
        posHolder[0]++;
      }
      String value = attrString.substring(valueStart, posHolder[0]);

      if (isStyle) {
        styleRange[0] = absValueStart;
        styleRange[1] = baseOffset + posHolder[0];
      }

      if (posHolder[0] < len) {
        posHolder[0]++; // consume closing quote
      }
      return value;
    }

    // Unquoted attribute value
    int valueStart = posHolder[0];
    while (posHolder[0] < len
        && !Character.isWhitespace(attrString.charAt(posHolder[0]))
        && attrString.charAt(posHolder[0]) != '>') {
      posHolder[0]++;
    }
    return attrString.substring(valueStart, posHolder[0]);
  }

  private static int indexOfWhitespace(String s) {
    for (int i = 0; i < s.length(); i++) {
      if (Character.isWhitespace(s.charAt(i))) {
        return i;
      }
    }
    return -1;
  }

  private static int indexOfIgnoreCase(String haystack, String needle, int fromIndex) {
    int needleLen = needle.length();
    int limit = haystack.length() - needleLen;
    for (int i = fromIndex; i <= limit; i++) {
      if (haystack.regionMatches(true, i, needle, 0, needleLen)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Result of extracting CSS from HTML, containing the cleaned HTML and collected CSS text.
   *
   * @param html the HTML with style blocks removed
   * @param css the concatenated CSS extracted from style blocks
   */
  public record StyleExtractionResult(String html, String css) {}
}
