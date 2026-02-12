package dev.jcputney.mjml.component.content;

import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import dev.jcputney.mjml.util.CssUnitParser;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The text component (&lt;mj-text&gt;). Renders a div containing HTML text content with
 * configurable font family, size, color, line height, and alignment.
 */
public class MjText extends BodyComponent {

  private static final Map<String, String> DEFAULTS =
      Map.ofEntries(
          Map.entry("align", "left"),
          Map.entry("color", "#000000"),
          Map.entry("container-background-color", ""),
          Map.entry("font-family", "Ubuntu, Helvetica, Arial, sans-serif"),
          Map.entry("font-size", "13px"),
          Map.entry("font-style", ""),
          Map.entry("font-weight", ""),
          Map.entry("height", ""),
          Map.entry("letter-spacing", ""),
          Map.entry("line-height", "1"),
          Map.entry("padding", "10px 25px"),
          Map.entry("padding-bottom", ""),
          Map.entry("padding-left", ""),
          Map.entry("padding-right", ""),
          Map.entry("padding-top", ""),
          Map.entry("text-decoration", ""),
          Map.entry("text-transform", ""));

  private static final Set<String> BLOCK_ELEMENTS =
      Set.of(
          "ul", "ol", "p", "div", "h1", "h2", "h3", "h4", "h5", "h6", "blockquote", "table", "pre");

  private static final Pattern BLOCK_ELEMENT_PATTERN =
      Pattern.compile(
          "<(" + String.join("|", BLOCK_ELEMENTS) + ")[\\s>/]", Pattern.CASE_INSENSITIVE);

  /** Matches whitespace sequences containing at least one newline. */
  private static final Pattern NEWLINE_WHITESPACE = Pattern.compile("\\s*\\n\\s*");

  /**
   * Creates a new MjText component.
   *
   * @param node the parsed MJML node for this component
   * @param globalContext the global rendering context
   * @param renderContext the current render context
   */
  public MjText(MjmlNode node, GlobalContext globalContext, RenderContext renderContext) {
    super(node, globalContext, renderContext);
  }

  /**
   * Collapses whitespace in text runs (outside of HTML tags) but preserves newlines between
   * block-level element boundaries.
   */
  private static String collapseInlineWhitespace(String html) {
    StringBuilder sb = new StringBuilder();
    int i = 0;
    boolean afterBlockBoundary = false;

    while (i < html.length()) {
      if (html.charAt(i) == '<') {
        // Inside a tag — copy verbatim until >
        int end = html.indexOf('>', i);
        if (end < 0) {
          sb.append(html.substring(i));
          break;
        }
        String tag = html.substring(i, end + 1);
        // Normalize multi-line tag whitespace (e.g., attributes split across lines)
        tag = normalizeTagWhitespace(tag);
        String lower = tag.toLowerCase();
        String tagName =
            lower.startsWith("</")
                ? extractTagName(lower.substring(2))
                : extractTagName(lower.substring(1));
        boolean isBlock = isBlockElement(tagName);

        sb.append(tag);
        afterBlockBoundary = isBlock;
        i = end + 1;
      } else {
        // In a text run — find the next tag or end
        int next = html.indexOf('<', i);
        if (next < 0) {
          next = html.length();
        }
        String textRun = html.substring(i, next);
        String collapsed = CssUnitParser.WHITESPACE.matcher(textRun).replaceAll(" ");
        if (collapsed.trim().isEmpty() && textRun.contains("\n")) {
          // Whitespace-only text run containing newlines — preserve one newline
          // (matches MJML/cheerio behavior of preserving original whitespace structure)
          sb.append("\n");
        } else {
          sb.append(collapsed);
        }
        // Don't reset afterBlockBoundary for whitespace-only runs
        if (!collapsed.trim().isEmpty()) {
          afterBlockBoundary = false;
        }
        i = next;
      }
    }
    return sb.toString();
  }

  /**
   * Normalizes whitespace inside an HTML tag that spans multiple lines. Collapses
   * newline-containing whitespace to a single space and removes trailing space before {@code >}.
   */
  private static String normalizeTagWhitespace(String tag) {
    if (!tag.contains("\n")) {
      return tag;
    }
    String normalized = NEWLINE_WHITESPACE.matcher(tag).replaceAll(" ");
    // Remove space before closing > at end of tag (but not before />)
    if (normalized.endsWith(" >")) {
      normalized = normalized.substring(0, normalized.length() - 2) + ">";
    }
    return normalized;
  }

  private static String extractTagName(String s) {
    // Extract tag name from after < or </
    StringBuilder name = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == ' ' || c == '>' || c == '/' || c == '\t' || c == '\n') {
        break;
      }
      name.append(c);
    }
    return name.toString();
  }

  private static boolean containsBlockElements(String html) {
    return BLOCK_ELEMENT_PATTERN.matcher(html).find();
  }

  private static boolean isBlockElement(String tag) {
    return BLOCK_ELEMENTS.contains(tag) || "li".equals(tag) || "br".equals(tag);
  }

  @Override
  public String getTagName() {
    return "mj-text";
  }

  @Override
  public Map<String, String> getDefaultAttributes() {
    return DEFAULTS;
  }

  @Override
  public String render() {
    StringBuilder sb = new StringBuilder();

    sb.append("                        <div style=\"").append(buildTextStyle()).append("\">");
    String content = sanitizeContent(getContent());
    sb.append(content);
    // Add newline before closing div if content has block elements or ends with newline
    if (content.endsWith("\n")) {
      sb.append("                        </div>");
    } else {
      sb.append("</div>");
    }

    return sb.toString();
  }

  private String buildTextStyle() {
    Map<String, String> styles = new LinkedHashMap<>();
    styles.put("font-family", getAttribute("font-family"));
    styles.put("font-size", getAttribute("font-size"));
    styles.put("font-style", getAttribute("font-style", ""));
    styles.put("font-weight", getAttribute("font-weight", ""));
    styles.put("letter-spacing", getAttribute("letter-spacing", ""));
    styles.put("line-height", getAttribute("line-height"));
    styles.put("text-align", getAttribute("align"));
    styles.put("text-decoration", getAttribute("text-decoration", ""));
    styles.put("text-transform", getAttribute("text-transform", ""));
    styles.put("color", getAttribute("color"));

    addIfPresent(styles, "height");

    return buildStyle(styles);
  }

  /**
   * Gets the inner HTML content of the text element. MJML passes through HTML content as-is. We
   * collapse whitespace in text runs (between/outside HTML tags) while preserving structure between
   * block-level elements.
   */
  private String getContent() {
    String content = node.getInnerHtml();

    String trimmed = content.trim();
    if (trimmed.isEmpty()) {
      return "";
    }

    // If content contains no HTML tags at all, collapse all whitespace
    if (!trimmed.contains("<")) {
      return CssUnitParser.WHITESPACE.matcher(content).replaceAll(" ").trim();
    }

    // Has block elements: collapse whitespace within text runs but preserve
    // newlines between block-level tags
    if (containsBlockElements(trimmed)) {
      String processed = collapseInlineWhitespace(trimmed);
      // Add leading newline only when content starts with a block element.
      // When content starts with text (e.g., "You are... <p>...</p>"),
      // MJML puts the text directly after the containing <div>.
      boolean startsWithBlock = processed.length() > 0 && processed.charAt(0) == '<';
      return (startsWithBlock ? "\n" : "") + processed + "\n";
    }

    // Has inline HTML (e.g., <br/>, <a>, <span>) but no block elements.
    // Collapse whitespace but preserve newlines after br-like tags
    String result = collapseInlineWhitespace(trimmed);
    // Add trailing newline only when content has block-like breaks (e.g., <br/>)
    // that already introduced newlines — so </div> goes on its own line.
    // Pure inline content (e.g., <a>) should NOT get a trailing newline.
    if (result.contains("\n")) {
      return result + "\n";
    }
    return result;
  }
}
