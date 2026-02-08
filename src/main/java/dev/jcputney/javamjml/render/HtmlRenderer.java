package dev.jcputney.javamjml.render;

/**
 * StringBuilder-based HTML builder with helper methods for
 * MSO conditional comments, Outlook VML, and common HTML patterns.
 */
public class HtmlRenderer {

  private final StringBuilder sb;

  public HtmlRenderer() {
    this.sb = new StringBuilder(32768); // 32KB pre-allocation
  }

  public HtmlRenderer(int capacity) {
    this.sb = new StringBuilder(capacity);
  }

  /**
   * Appends raw HTML content.
   */
  public HtmlRenderer append(String html) {
    sb.append(html);
    return this;
  }

  /**
   * Appends a newline.
   */
  public HtmlRenderer nl() {
    sb.append('\n');
    return this;
  }

  /**
   * Appends a self-closing tag.
   */
  public HtmlRenderer selfClosingTag(String tag, String attributes) {
    sb.append('<').append(tag);
    if (attributes != null && !attributes.isEmpty()) {
      sb.append(attributes);
    }
    sb.append(" />");
    return this;
  }

  /**
   * Appends an opening tag.
   */
  public HtmlRenderer openTag(String tag, String attributes) {
    sb.append('<').append(tag);
    if (attributes != null && !attributes.isEmpty()) {
      sb.append(attributes);
    }
    sb.append('>');
    return this;
  }

  /**
   * Appends a closing tag.
   */
  public HtmlRenderer closeTag(String tag) {
    sb.append("</").append(tag).append('>');
    return this;
  }

  /**
   * Wraps content in MSO conditional comments: {@code <!--[if mso]>...<![endif]-->}
   */
  public HtmlRenderer msoConditional(String content) {
    sb.append("<!--[if mso]>").append(content).append("<![endif]-->");
    return this;
  }

  /**
   * Opens an MSO conditional block: {@code <!--[if mso]>}
   */
  public HtmlRenderer msoOpen() {
    sb.append("<!--[if mso]>\n");
    return this;
  }

  /**
   * Closes an MSO conditional block: {@code <![endif]-->}
   */
  public HtmlRenderer msoClose() {
    sb.append("<![endif]-->\n");
    return this;
  }

  /**
   * Opens an MSO-not conditional block: {@code <!--[if !mso]><!-->}
   */
  public HtmlRenderer notMsoOpen() {
    sb.append("<!--[if !mso]><!-->\n");
    return this;
  }

  /**
   * Closes an MSO-not conditional block: {@code <!--<![endif]-->}
   */
  public HtmlRenderer notMsoClose() {
    sb.append("<!--<![endif]-->\n");
    return this;
  }

  /**
   * Wraps content in MSO-only IE conditional: {@code <!--[if mso | IE]>...<![endif]-->}
   */
  public HtmlRenderer msoIeOpen() {
    sb.append("<!--[if mso | IE]>\n");
    return this;
  }

  /**
   * Closes MSO/IE conditional block.
   */
  public HtmlRenderer msoIeClose() {
    sb.append("<![endif]-->\n");
    return this;
  }

  @Override
  public String toString() {
    return sb.toString();
  }

  public int length() {
    return sb.length();
  }
}
