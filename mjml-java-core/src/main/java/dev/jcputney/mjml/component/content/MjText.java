package dev.jcputney.mjml.component.content;

import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import dev.jcputney.mjml.util.CssUnitParser;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The text component (&lt;mj-text&gt;).
 * Renders a div containing HTML text content with configurable
 * font family, size, color, line height, and alignment.
 */
public class MjText extends BodyComponent {

  private static final Map<String, String> DEFAULTS = Map.ofEntries(
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
      Map.entry("text-transform", "")
  );

  public MjText(MjmlNode node, GlobalContext globalContext, RenderContext renderContext) {
    super(node, globalContext, renderContext);
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
   * Gets the inner HTML content of the text element.
   * MJML passes through HTML content as-is. We collapse whitespace in text
   * runs (between/outside HTML tags) while preserving structure between
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
      return "\n" + processed + "\n";
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

  /**
   * Collapses whitespace in text runs (outside of HTML tags) but preserves
   * newlines between block-level element boundaries.
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
        String lower = tag.toLowerCase();
        String tagName = lower.startsWith("</")
            ? extractTagName(lower.substring(2))
            : extractTagName(lower.substring(1));
        boolean isBlock = isBlockElement(tagName);

        // Before a block-level tag (open or close), if we're after another block boundary
        // and there's only whitespace between them, use newline
        if (isBlock && afterBlockBoundary) {
          // Already handled by whitespace-only text run check below
        }
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
        if (afterBlockBoundary && collapsed.trim().isEmpty()) {
          // Whitespace-only text run after a block boundary — emit newline
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
    String lower = html.toLowerCase();
    return lower.contains("<ul") || lower.contains("<ol") || lower.contains("<p")
        || lower.contains("<div") || lower.contains("<h1") || lower.contains("<h2")
        || lower.contains("<h3") || lower.contains("<h4") || lower.contains("<h5")
        || lower.contains("<h6") || lower.contains("<blockquote") || lower.contains("<table")
        || lower.contains("<pre");
  }

  private static boolean isBlockElement(String tag) {
    return "ul".equals(tag) || "ol".equals(tag) || "p".equals(tag)
        || "div".equals(tag) || "h1".equals(tag) || "h2".equals(tag)
        || "h3".equals(tag) || "h4".equals(tag) || "h5".equals(tag)
        || "h6".equals(tag) || "blockquote".equals(tag) || "table".equals(tag)
        || "pre".equals(tag) || "li".equals(tag) || "br".equals(tag);
  }
}
