
package dev.jcputney.mjml.util;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
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

  /**
   * When true, builder methods (open, close, wrap, selfClose) produce compact output with no
   * newlines or indentation. Set by conditional block methods (mso, notMso, notMsoIE) since
   * MSO conditional content must be inline.
   */
  private boolean compact;

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
    if (!compact) {
      appendIndent();
    }
    sb.append('<').append(tag);
    if (attrs != null && !attrs.isEmpty()) {
      sb.append(attrs);
    }
    sb.append('>');
    if (!compact) {
      sb.append('\n');
      depth += INDENT_SIZE;
    }
    return this;
  }

  /** Opens a tag with no attributes. */
  public HtmlBuilder open(String tag) {
    return open(tag, null);
  }

  /** Closes a tag: decreases indent, writes {@code {indent}</tag>\n}. */
  public HtmlBuilder close(String tag) {
    if (!compact) {
      depth = Math.max(0, depth - INDENT_SIZE);
      appendIndent();
    }
    sb.append("</").append(tag).append('>');
    if (!compact) {
      sb.append('\n');
    }
    return this;
  }

  /**
   * Base attributes shared by presentation-only tables in MJML email rendering:
   * {@code border="0", cellpadding="0", cellspacing="0", role="presentation"}. Copy this map and add additional
   * attributes as needed (e.g. {@code style}, {@code width}).
   */
  public static final Map<String, String> PRESENTATION_TABLE_ATTRS = Map.of(
    "border", "0", "cellpadding", "0", "cellspacing", "0", "role", "presentation");

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

  /**
   * Appends a closing tag for the specified inline element to the internal StringBuilder.
   *
   * @param tag the name of the inline element for which the closing tag should be appended
   */
  public void closeInline(String tag) {
    sb.append("</").append(tag).append('>');
  }

  /**
   * Appends a closing tag with a newline character to the StringBuilder.
   *
   * @param tag the name of the tag to be closed
   */
  public void closeInlineLn(String tag) {
    sb.append("</").append(tag).append(">\n");
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
    boolean wasCompact = compact;
    compact = true;
    block.run();
    compact = wasCompact;
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

  private static final String NOT_MSO_START = "<!--[if !mso]><!-->";
  private static final String NOT_MSO_IE_START = "<!--[if !mso | IE]><!-->";
  private static final String NOT_MSO_END = "<!--<![endif]-->";

  /**
   * Builds a sorted attribute string from a map. Keys are sorted alphabetically, with any {@code trailingKeys} placed
   * at the end in the order specified. Null or empty values cause the attribute to be omitted, except for attributes in
   * {@link #ALWAYS_EMIT} which are always emitted. Produces: {@code key1="val1" key2="val2"} (note leading space).
   *
   * <p>Values are NOT escaped — callers must escape via {@code escapeAttr()} before passing.
   *
   * @param attributes   the attribute map to render
   * @param trailingKeys attribute names that should appear after all alphabetically-sorted keys
   */
  public static String attrs(Map<String, String> attributes, String... trailingKeys) {
    if (attributes == null || attributes.isEmpty()) {
      return "";
    }
    Set<String> trailing = trailingKeys.length > 0 ? Set.of(trailingKeys) : Set.of();
    Comparator<String> order = (a, b) -> {
      boolean aTrail = trailing.contains(a);
      boolean bTrail = trailing.contains(b);
      if (aTrail != bTrail) {
        return aTrail ? 1 : -1;
      }
      return a.compareTo(b);
    };
    StringBuilder result = new StringBuilder();
    attributes.entrySet().stream()
      .filter(e -> {
        String key = e.getKey();
        String value = e.getValue();
        return key != null && value != null
          && (!value.isEmpty() || ALWAYS_EMIT.contains(key));
      })
      .sorted(Map.Entry.comparingByKey(order))
      .forEach(e -> result.append(' ').append(e.getKey())
        .append("=\"").append(e.getValue()).append('"'));
    return result.toString();
  }

  /**
   * Builds an unsorted attribute string from a map of key-value pairs. Keys are appended with their corresponding
   * values in the order they are provided in the map. Null or empty values are omitted, except for keys in
   * {@code ALWAYS_EMIT}, which are always included even if their values are empty.
   * <p>
   * Values are NOT escaped — callers must escape via {@code escapeAttr()} before passing.
   *
   * @param attributes a map of attribute names and their values; keys represent attribute names and values represent
   *                   attribute values
   * @return a space-separated string of unsorted attributes in the format {@code key1="value1" key2="value2"}. An empty
   * string is returned if the input map is null or empty.
   */
  public static String unsortedAttrs(Map<String, String> attributes) {
    if (attributes == null || attributes.isEmpty()) {
      return "";
    }
    StringBuilder result = new StringBuilder();
    attributes.forEach((key, value) -> {
      if (key != null && value != null && (!value.isEmpty() || ALWAYS_EMIT.contains(key))) {
        result.append(' ').append(key).append("=\"").append(value).append('"');
      }
    });
    return result.toString();
  }

  /**
   * Builds an attribute string from key/value pairs. Null or empty values cause the attribute to be
   * omitted, except for attributes in {@link #ALWAYS_EMIT} which are always emitted. Produces:
   * {@code key1="val1" key2="val2"} (note leading space).
   *
   * <p>Values are NOT escaped — callers must escape via {@code escapeAttr()} before passing.
   *
   * <p>Note: this overload preserves insertion order. Use {@link #attrs(Map, String...)} (Map)} for automatic
   * alphabetical sorting.
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
   * Wraps the output of a builder block in non-MSO conditional comments
   * ({@code <!--[if !mso]><!-->...<!--<![endif]-->}). Content is visible to all clients except Outlook desktop.
   * Followed by a newline.
   */
  public void notMso(Runnable block) {
    sb.append(NOT_MSO_START);
    boolean wasCompact = compact;
    compact = true;
    block.run();
    compact = wasCompact;
    sb.append(NOT_MSO_END).append('\n');
  }

  /**
   * Wraps the output of a builder block in non-MSO/IE conditional comments
   * ({@code <!--[if !mso | IE]><!-->...<!--<![endif]-->}). Content is hidden from both Outlook desktop and IE. Followed
   * by a newline.
   */
  public void notMsoIE(Runnable block) {
    sb.append(NOT_MSO_IE_START);
    boolean wasCompact = compact;
    compact = true;
    block.run();
    compact = wasCompact;
    sb.append(NOT_MSO_END).append('\n');
  }

  /**
   * Creates a self-closing HTML tag with optional attributes. The method appends the tag at the current indentation
   * level, includes the specified attributes if provided, and appends a newline.
   *
   * @param tag   the HTML tag name to be self-closed
   * @param attrs the attribute string to include within the tag; if null or empty, no attributes are added
   */
  public void selfClose(String tag, String attrs) {
    if (!compact) {
      appendIndent();
    }
    sb.append('<').append(tag);
    if (attrs != null && !attrs.isEmpty()) {
      sb.append(attrs);
    }
    sb.append(" />");
    if (!compact) {
      sb.append('\n');
    }
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
    if (!compact) {
      appendIndent();
    }
    sb.append('<').append(tag);
    if (attrs != null && !attrs.isEmpty()) {
      sb.append(attrs);
    }
    sb.append('>');
    if (!compact) {
      sb.append('\n');
      depth += INDENT_SIZE;
    }
    block.run();
    if (!compact) {
      depth = Math.max(0, depth - INDENT_SIZE);
      appendIndent();
    }
    sb.append("</").append(tag).append('>');
    if (!compact) {
      sb.append('\n');
    }
    return this;
  }

  /**
   * Appends a newline character to the internal string builder and returns the current instance of the
   * {@code HtmlBuilder}.
   *
   * @return this {@code HtmlBuilder} instance for method chaining
   */
  public HtmlBuilder newline() {
    sb.append('\n');
    return this;
  }

  // --- Indent control ---

  /**
   * Increases the current indentation depth by a predefined size. This method modifies the internal state of the
   * {@code HtmlBuilder} to reflect a deeper nesting level for subsequent content. The increase in depth is determined
   * by the constant {@code INDENT_SIZE}.
   */
  public void indent() {
    depth += INDENT_SIZE;
  }

  /**
   * Reduces the current indentation level by decreasing the depth. Ensures that the depth does not fall below zero. The
   * decrease is determined by the constant INDENT_SIZE.
   */
  public void outdent() {
    depth = Math.max(0, depth - INDENT_SIZE);
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
   * Wraps content in a presentation table
   * ({@code border="0" cellpadding="0" cellspacing="0" role="presentation" width="100%"}).
   *
   * @param block the block that produces the table's inner content
   */
  public HtmlBuilder table(Runnable block) {
    var tableAttrs = new LinkedHashMap<>(PRESENTATION_TABLE_ATTRS);
    return wrap("table", attrs(tableAttrs), block);
  }

  /**
   * Creates an HTML table element with the specified attributes and content block.
   *
   * @param attrs the attributes to be applied to the table element
   * @param block a runnable block that defines the content of the table
   * @return an HtmlBuilder instance with the table element wrapped
   */
  public HtmlBuilder table(String attrs, Runnable block) {
    return wrap("table", attrs, block);
  }

  /**
   * Wraps content in an HTML `{@code <table>}` tag, applying attributes and executing the provided block within the
   * table. Adds default presentation table attributes unless overridden by the input.
   *
   * @param attrs a map of attributes to be applied to the `<table>` tag; keys represent attribute names and values
   *              represent attribute values
   * @param block the block that produces the table's inner content
   * @return this {@code HtmlBuilder} instance for method chaining
   */
  public HtmlBuilder table(Map<String, String> attrs, Runnable block) {
    var tableAttrs = new LinkedHashMap<>(PRESENTATION_TABLE_ATTRS);
    tableAttrs.putAll(attrs);
    return wrap("table", attrs(tableAttrs), block);
  }

  /**
   * Wraps the output of a {@link Runnable} block with the specified HTML tag. Indents the block's content appropriately
   * and closes the tag after execution of the block.
   *
   * @param tag   the HTML tag to wrap the block with
   * @param block the block of content to be wrapped in the specified tag
   * @return this {@code HtmlBuilder} instance for method chaining
   */
  public HtmlBuilder wrap(String tag, Runnable block) {
    if (!compact) {
      appendIndent();
    }
    sb.append('<').append(tag).append('>');
    if (!compact) {
      sb.append('\n');
      depth += INDENT_SIZE;
    }
    block.run();
    if (!compact) {
      depth = Math.max(0, depth - INDENT_SIZE);
      appendIndent();
    }
    sb.append("</").append(tag).append('>');
    if (!compact) {
      sb.append('\n');
    }
    return this;
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

  /**
   * Appends a number of space characters to the StringBuilder `sb` based on the current
   * indentation depth. The number of spaces appended is determined by the value of `depth`.
   * If `depth` is negative, no spaces are appended.
   * <p>
   * This method relies on the `depth` variable to specify the indentation level and uses
   * the `Math.max` function to ensure that no negative spaces are appended.
   * The spaces are repeated using the `String.repeat` method.
   */
  private void appendIndent() {
    sb.append(" ".repeat(Math.max(0, depth)));
  }
}
