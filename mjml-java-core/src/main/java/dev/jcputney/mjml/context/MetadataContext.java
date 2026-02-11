package dev.jcputney.mjml.context;

import dev.jcputney.mjml.MjmlConfiguration;
import dev.jcputney.mjml.util.CssUnitParser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Document-level metadata gathered during head processing. Contains title, preview text,
 * breakpoint, container width, body background color, head comments, and file-start content.
 *
 * <p>This is one of three focused sub-contexts extracted from {@link GlobalContext}. See also
 * {@link StyleContext} and {@link AttributeContext}.
 *
 * <p><strong>Thread safety:</strong> This class is <em>not</em> thread-safe. Each render pipeline
 * creates its own instance.
 */
public class MetadataContext {

  private final List<String> headComments = new ArrayList<>();
  private final List<String> fileStartContent = new ArrayList<>();
  private String title = "";
  private String previewText = "";
  private String breakpoint = "480px";
  private int containerWidth = MjmlConfiguration.DEFAULT_CONTAINER_WIDTH;
  private String bodyBackgroundColor = "";

  /**
   * Creates a new {@code MetadataContext} with default values: empty title and preview text, a
   * breakpoint of {@code "480px"}, the default container width, and no head comments or file-start
   * content.
   */
  public MetadataContext() {
    // default values are set by field initializers
  }

  /**
   * Returns the document title.
   *
   * @return the title, never {@code null}
   */
  public String getTitle() {
    return title;
  }

  /**
   * Sets the document title. A {@code null} value is normalized to an empty string.
   *
   * @param title the title to set
   */
  public void setTitle(String title) {
    this.title = title != null ? title : "";
  }

  /**
   * Returns the email preview text (shown in inbox previews).
   *
   * @return the preview text, never {@code null}
   */
  public String getPreviewText() {
    return previewText;
  }

  /**
   * Sets the email preview text. A {@code null} value is normalized to an empty string.
   *
   * @param previewText the preview text to set
   */
  public void setPreviewText(String previewText) {
    this.previewText = previewText != null ? previewText : "";
  }

  /**
   * Returns the responsive breakpoint as a CSS value string (e.g. {@code "480px"}).
   *
   * @return the breakpoint string
   */
  public String getBreakpoint() {
    return breakpoint;
  }

  /**
   * Sets the responsive breakpoint. {@code null} or empty values are ignored, leaving the current
   * breakpoint unchanged.
   *
   * @param breakpoint the breakpoint CSS value to set (e.g. {@code "480px"})
   */
  public void setBreakpoint(String breakpoint) {
    if (breakpoint != null && !breakpoint.isEmpty()) {
      this.breakpoint = breakpoint;
    }
  }

  /**
   * Returns the responsive breakpoint parsed as an integer pixel value. If parsing fails, defaults
   * to {@code 480}.
   *
   * @return the breakpoint in pixels
   */
  public int getBreakpointPx() {
    return CssUnitParser.parsePixels(breakpoint, 480);
  }

  /**
   * Returns the container width in pixels.
   *
   * @return the container width
   */
  public int getContainerWidth() {
    return containerWidth;
  }

  /**
   * Sets the container width in pixels.
   *
   * @param containerWidth the container width to set
   */
  public void setContainerWidth(int containerWidth) {
    this.containerWidth = containerWidth;
  }

  /**
   * Returns the body background color CSS value.
   *
   * @return the body background color, never {@code null}
   */
  public String getBodyBackgroundColor() {
    return bodyBackgroundColor;
  }

  /**
   * Sets the body background color. A {@code null} value is normalized to an empty string.
   *
   * @param bodyBackgroundColor the body background color CSS value to set
   */
  public void setBodyBackgroundColor(String bodyBackgroundColor) {
    this.bodyBackgroundColor = bodyBackgroundColor != null ? bodyBackgroundColor : "";
  }

  /**
   * Adds a comment found in the {@code <mj-head>} section to the list of head comments.
   *
   * @param comment the comment text to add
   */
  public void addHeadComment(String comment) {
    headComments.add(comment);
  }

  /**
   * Returns an unmodifiable list of comments found in the {@code <mj-head>} section.
   *
   * @return an unmodifiable list of head comment strings
   */
  public List<String> getHeadComments() {
    return Collections.unmodifiableList(headComments);
  }

  /**
   * Adds content that should appear at the start of the rendered file. {@code null} or empty values
   * are ignored.
   *
   * @param content the file-start content to add
   */
  public void addFileStartContent(String content) {
    if (content != null && !content.isEmpty()) {
      fileStartContent.add(content);
    }
  }

  /**
   * Returns an unmodifiable list of content strings that should appear at the start of the rendered
   * file.
   *
   * @return an unmodifiable list of file-start content strings
   */
  public List<String> getFileStartContent() {
    return Collections.unmodifiableList(fileStartContent);
  }
}
