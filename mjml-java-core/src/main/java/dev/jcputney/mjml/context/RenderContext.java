package dev.jcputney.mjml.context;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Per-component render context that carries the current container width as the rendering pipeline
 * descends the component tree. Width narrows: body(600) -> section -> column -> content component.
 */
public class RenderContext {

  private final double containerWidth;
  private final String columnWidthSpec;
  private final int index;
  private final boolean first;
  private final boolean last;
  private final boolean insideWrapper;
  private final boolean insideGroup;
  private final AtomicInteger idCounter;

  /**
   * Creates a new root render context with the given container width. The context starts with
   * default values: index 0, first and last both true, not inside a wrapper or group, and a fresh
   * ID counter.
   *
   * @param containerWidth the initial container width in pixels (e.g. 600)
   */
  public RenderContext(double containerWidth) {
    this(containerWidth, null, 0, true, true, false, false, new AtomicInteger(0));
  }

  private RenderContext(
      double containerWidth,
      String columnWidthSpec,
      int index,
      boolean first,
      boolean last,
      boolean insideWrapper,
      boolean insideGroup,
      AtomicInteger idCounter) {
    this.containerWidth = containerWidth;
    this.columnWidthSpec = columnWidthSpec;
    this.index = index;
    this.first = first;
    this.last = last;
    this.insideWrapper = insideWrapper;
    this.insideGroup = insideGroup;
    this.idCounter = idCounter;
  }

  /** Creates a copy of this context, sharing the same idCounter. */
  private RenderContext copy() {
    return new RenderContext(
        containerWidth, columnWidthSpec, index, first, last, insideWrapper, insideGroup, idCounter);
  }

  /**
   * Returns the current container width in pixels.
   *
   * @return the container width in pixels
   */
  public double getContainerWidth() {
    return containerWidth;
  }

  /**
   * Returns the column width specification as computed by the parent section. For percentage
   * widths: "33.33" (just the number) For pixel widths: "150px" (with px suffix) For auto columns:
   * "100" or "33.333333333333336" (full precision)
   *
   * @return the column width specification string, or {@code null} if not set
   */
  public String getColumnWidthSpec() {
    return columnWidthSpec;
  }

  /**
   * Returns the zero-based index of this component among its siblings.
   *
   * @return the sibling index
   */
  public int getIndex() {
    return index;
  }

  /**
   * Returns whether this component is the first among its siblings.
   *
   * @return {@code true} if this is the first sibling
   */
  public boolean isFirst() {
    return first;
  }

  /**
   * Returns whether this component is the last among its siblings.
   *
   * @return {@code true} if this is the last sibling
   */
  public boolean isLast() {
    return last;
  }

  /**
   * Returns whether this component is nested inside an {@code mj-wrapper}.
   *
   * @return {@code true} if inside a wrapper
   */
  public boolean isInsideWrapper() {
    return insideWrapper;
  }

  /**
   * Returns whether this component is nested inside an {@code mj-group}.
   *
   * @return {@code true} if inside a group
   */
  public boolean isInsideGroup() {
    return insideGroup;
  }

  /**
   * Returns a deterministic unique ID with the given prefix. The counter is shared across all child
   * contexts derived from the same root.
   *
   * @param prefix the prefix for the generated ID
   * @return a unique ID in the form "{prefix}-{counter}"
   */
  public String nextUniqueId(String prefix) {
    return prefix + "-" + idCounter.getAndIncrement();
  }

  /**
   * Creates a child context with a narrower container width.
   *
   * @param width the new container width in pixels
   * @return a new render context with the updated width
   */
  public RenderContext withWidth(double width) {
    return new RenderContext(
        width, columnWidthSpec, index, first, last, insideWrapper, insideGroup, idCounter);
  }

  /**
   * Creates a child context with container width and column width specification.
   *
   * @param width the new container width in pixels
   * @param columnWidthSpec the column width specification string (e.g. "33.33" or "150px")
   * @return a new render context with the updated width and column width spec
   */
  public RenderContext withColumnWidth(double width, String columnWidthSpec) {
    return new RenderContext(
        width, columnWidthSpec, index, first, last, insideWrapper, insideGroup, idCounter);
  }

  /**
   * Creates a child context with positioning info for column rendering.
   *
   * @param index the zero-based index of the column among its siblings
   * @param first whether this is the first sibling
   * @param last whether this is the last sibling
   * @return a new render context with the updated position info
   */
  public RenderContext withPosition(int index, boolean first, boolean last) {
    return new RenderContext(
        containerWidth, columnWidthSpec, index, first, last, insideWrapper, insideGroup, idCounter);
  }

  /**
   * Creates a child context indicating the component is inside a wrapper.
   *
   * @param insideWrapper whether the component is inside an {@code mj-wrapper}
   * @return a new render context with the updated wrapper flag
   */
  public RenderContext withInsideWrapper(boolean insideWrapper) {
    return new RenderContext(
        containerWidth, columnWidthSpec, index, first, last, insideWrapper, insideGroup, idCounter);
  }

  /**
   * Creates a child context indicating the component is inside a group.
   *
   * @param insideGroup whether the component is inside an {@code mj-group}
   * @return a new render context with the updated group flag
   */
  public RenderContext withInsideGroup(boolean insideGroup) {
    return new RenderContext(
        containerWidth, columnWidthSpec, index, first, last, insideWrapper, insideGroup, idCounter);
  }
}
