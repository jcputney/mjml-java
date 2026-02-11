package dev.jcputney.mjml.context;

import dev.jcputney.mjml.MjmlConfiguration;
import dev.jcputney.mjml.util.CssUnitParser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Document-level metadata gathered during head processing.
 * Contains title, preview text, breakpoint, container width, body background color,
 * head comments, and file-start content.
 *
 * <p>This is one of three focused sub-contexts extracted from {@link GlobalContext}.
 * See also {@link StyleContext} and {@link AttributeContext}.
 *
 * <p><strong>Thread safety:</strong> This class is <em>not</em> thread-safe.
 * Each render pipeline creates its own instance.</p>
 */
public class MetadataContext {

  private String title = "";
  private String previewText = "";
  private String breakpoint = "480px";
  private int containerWidth = MjmlConfiguration.DEFAULT_CONTAINER_WIDTH;
  private String bodyBackgroundColor = "";
  private final List<String> headComments = new ArrayList<>();
  private final List<String> fileStartContent = new ArrayList<>();

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title != null ? title : "";
  }

  public String getPreviewText() {
    return previewText;
  }

  public void setPreviewText(String previewText) {
    this.previewText = previewText != null ? previewText : "";
  }

  public String getBreakpoint() {
    return breakpoint;
  }

  public int getBreakpointPx() {
    return CssUnitParser.parsePixels(breakpoint, 480);
  }

  public void setBreakpoint(String breakpoint) {
    if (breakpoint != null && !breakpoint.isEmpty()) {
      this.breakpoint = breakpoint;
    }
  }

  public int getContainerWidth() {
    return containerWidth;
  }

  public void setContainerWidth(int containerWidth) {
    this.containerWidth = containerWidth;
  }

  public String getBodyBackgroundColor() {
    return bodyBackgroundColor;
  }

  public void setBodyBackgroundColor(String bodyBackgroundColor) {
    this.bodyBackgroundColor = bodyBackgroundColor != null ? bodyBackgroundColor : "";
  }

  public void addHeadComment(String comment) {
    headComments.add(comment);
  }

  public List<String> getHeadComments() {
    return Collections.unmodifiableList(headComments);
  }

  public void addFileStartContent(String content) {
    if (content != null && !content.isEmpty()) {
      fileStartContent.add(content);
    }
  }

  public List<String> getFileStartContent() {
    return Collections.unmodifiableList(fileStartContent);
  }
}
