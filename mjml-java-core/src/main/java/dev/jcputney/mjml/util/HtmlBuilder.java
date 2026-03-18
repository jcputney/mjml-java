
package dev.jcputney.mjml.util;

import java.util.Set;

/**
 * Fluent HTML builder that manages indentation automatically. Designed for use in MJML component
 * {@code render()} methods to replace manual StringBuilder with hardcoded indentation strings.
 *
 * <p>Usage:
 *
 * <pre>{@code
 * HtmlBuilder html = new HtmlBuilder();
 * html.open("table", attrs("border", "0", "role", "presentation"));
 * html.open("tbody");
 * html.open("tr");
 * html.open("td", attrs("style", buildStyle(styles)));
 * html.text("content");
 * html.close("td");
 * html.close("tr");
 * html.close("tbody");
 * html.close("table");
 * return html.toString();
 * }</pre>
 */
public final class HtmlBuilder {

  private static final int INDENT_SIZE = 2;

  private final StringBuilder sb;
  private int depth;

  /** Creates a builder at indent depth 0. */
  public HtmlBuilder() {
    this(0);
  }

  /** Creates a builder with the given starting indent depth in spaces. */
  public HtmlBuilder(int startingIndent) {
    this.sb = new StringBuilder(512);
    this.depth = Math.max(0, startingIndent);
  }

  // --- Core tag methods ---

  /**
   * Opens a tag with attributes, writes a newline, and increases indent. Produces: {@code
   * {indent}<tag attrs>\n}
   */
  public HtmlBuilder open(String tag, String attrs) {
    appendIndent();
    sb.append('<').append(tag);
    if (attrs != null && !attrs.isEmpty()) {
      sb.append(attrs);
    }
    sb.append(">\n");
    depth += INDENT_SIZE;
    return this;
  }

  /** Opens a tag with no attributes. */
  public HtmlBuilder open(String tag) {
    return open(tag, null);
  }

  /** Closes a tag: decreases indent, writes {@code {indent}</tag>\n}. */
  public HtmlBuilder close(String tag) {
    depth = Math.max(0, depth - INDENT_SIZE);
    appendIndent();
    sb.append("</").append(tag).append(">\n");
    return this;
  }

  /** Writes a self-closing tag: {@code {indent}<tag attrs>\n}. */
  public HtmlBuilder selfClose(String tag, String attrs) {
    appendIndent();
    sb.append('<').append(tag);
    if (attrs != null && !attrs.isEmpty()) {
      sb.append(attrs);
    }
    sb.append(">\n");
    return this;
  }

  // --- Inline content (tag + content on same line) ---

  /**
   * Opens a tag on the current line WITHOUT a trailing newline. Used when content follows on the
   * same line. Produces: {@code {indent}<tag attrs>}
   */
  public HtmlBuilder openInline(String tag, String attrs) {
    appendIndent();
    sb.append('<').append(tag);
    if (attrs != null && !attrs.isEmpty()) {
      sb.append(attrs);
    }
    sb.append('>');
    return this;
  }

  /** Opens an inline tag with no attributes. */
  public HtmlBuilder openInline(String tag) {
    return openInline(tag, null);
  }

  /** Closes a tag on the current line without indent or newline. Produces: {@code </tag>} */
  public HtmlBuilder closeInline(String tag) {
    sb.append("</").append(tag).append('>');
    return this;
  }

  /**
   * Closes a tag on the current line and appends a newline. Used to end a line started with {@link
   * #openInline}. Produces: {@code </tag>\n}
   */
  public HtmlBuilder closeInlineLn(String tag) {
    sb.append("</").append(tag).append(">\n");
    return this;
  }

  // --- Text and raw content ---

  /** Appends raw text on the current line (no indent, no newline). */
  public HtmlBuilder text(String content) {
    if (content != null) {
      sb.append(content);
    }
    return this;
  }

  /** Appends indented text followed by a newline. */
  public HtmlBuilder line(String content) {
    appendIndent();
    if (content != null) {
      sb.append(content);
    }
    sb.append('\n');
    return this;
  }

  /**
   * Appends raw HTML at the current indent level, followed by a newline. Only the first line gets
   * the indentation prefix.
   */
  public HtmlBuilder raw(String html) {
    if (html != null && !html.isEmpty()) {
      appendIndent();
      sb.append(html);
      if (!html.endsWith("\n")) {
        sb.append('\n');
      }
    }
    return this;
  }

  /**
   * Appends raw HTML verbatim with NO indentation or newline added. Used for MSO conditionals, VML,
   * and preformatted content.
   */
  public HtmlBuilder rawVerbatim(String html) {
    if (html != null) {
      sb.append(html);
    }
    return this;
  }

  // --- MSO conditional helpers ---

  private static final String MSO_START = "<!--[if mso | IE]>";
  private static final String MSO_END = "<![endif]-->";

  /**
   * Wraps the output of a builder block in MSO conditional comments. Everything written by the
   * block appears between {@code <!--[if mso | IE]>} and {@code <![endif]-->}, followed by a
   * newline.
   */
  public HtmlBuilder mso(Runnable block) {
    sb.append(MSO_START);
    block.run();
    sb.append(MSO_END).append('\n');
    return this;
  }

  /**
   * Wraps a string in MSO conditional comments followed by a newline. Shorthand for simple MSO
   * content that doesn't need builder calls.
   */
  public HtmlBuilder mso(String content) {
    sb.append(MSO_START);
    if (content != null) {
      sb.append(content);
    }
    sb.append(MSO_END).append('\n');
    return this;
  }

  /**
   * Opens a tag, executes the block, and closes the tag — guaranteeing matched open/close. Manages
   * indentation: increments depth before the block and decrements after, just like paired {@link
   * #open}/{@link #close} calls.
   *
   * @param tag   the HTML tag name
   * @param block the block that produces the tag's inner content
   */
  public HtmlBuilder wrap(String tag, Runnable block) {
    appendIndent();
    sb.append('<').append(tag).append(">\n");
    depth += INDENT_SIZE;
    block.run();
    depth = Math.max(0, depth - INDENT_SIZE);
    appendIndent();
    sb.append("</").append(tag).append(">\n");
    return this;
  }

  /**
   * Opens a tag with attributes, executes the block, and closes the tag. Manages indentation like
   * {@link #wrap(String, Runnable)}.
   *
   * @param tag   the HTML tag name
   * @param attrs the attribute string (from {@link #attrs})
   * @param block the block that produces the tag's inner content
   */
  public HtmlBuilder wrap(String tag, String attrs, Runnable block) {
    appendIndent();
    sb.append('<').append(tag);
    if (attrs != null && !attrs.isEmpty()) {
      sb.append(attrs);
    }
    sb.append(">\n");
    depth += INDENT_SIZE;
    block.run();
    depth = Math.max(0, depth - INDENT_SIZE);
    appendIndent();
    sb.append("</").append(tag).append(">\n");
    return this;
  }

  /** Appends a newline character. */
  public HtmlBuilder newline() {
    sb.append('\n');
    return this;
  }

  // --- Indent control ---

  /** Increases indent depth by one level. */
  public HtmlBuilder indent() {
    depth += INDENT_SIZE;
    return this;
  }

  /** Decreases indent depth by one level. */
  public HtmlBuilder outdent() {
    depth = Math.max(0, depth - INDENT_SIZE);
    return this;
  }

  // --- Output ---

  @Override
  public String toString() {
    return sb.toString();
  }

  // --- Static attribute helpers ---

  /**
   * HTML attributes that must always be emitted even when their value is empty. {@code alt=""} is
   * required for accessibility on {@code <img>} tags.
   */
  private static final Set<String> ALWAYS_EMIT = Set.of("alt", "style");

  /**
   * Builds an attribute string from key/value pairs. Null or empty values cause the attribute to be
   * omitted, except for attributes in {@link #ALWAYS_EMIT} which are always emitted. Produces:
   * {@code key1="val1" key2="val2"} (note leading space).
   *
   * <p>Values are NOT escaped — callers must escape via {@code escapeAttr()} before passing.
   */
  public static String attrs(String... keyValuePairs) {
    if (keyValuePairs == null || keyValuePairs.length == 0) {
      return "";
    }
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < keyValuePairs.length - 1; i += 2) {
      String key = keyValuePairs[i];
      String value = keyValuePairs[i + 1];
      if (key != null && value != null && (!value.isEmpty() || ALWAYS_EMIT.contains(key))) {
        result.append(' ').append(key).append("=\"").append(value).append('"');
      }
    }
    return result.toString();
  }

  /**
   * Conditionally adds a single attribute if the value is non-null and non-empty. Returns {@code
   * key="value"} (with leading space) or empty string.
   */
  public static String attrIf(String key, String value) {
    if (key == null || value == null || value.isEmpty()) {
      return "";
    }
    return " " + key + "=\"" + value + "\"";
  }

  // --- Internal ---

  private void appendIndent() {
    for (int i = 0; i < depth; i++) {
      sb.append(' ');
    }
  }
}
