package dev.jcputney.mjml.context;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Per-component render context that carries the current container width
 * as the rendering pipeline descends the component tree.
 * Width narrows: body(600) -> section -> column -> content component.
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

  public RenderContext(double containerWidth) {
    this(containerWidth, null, 0, true, true, false, false, new AtomicInteger(0));
  }

  private RenderContext(double containerWidth, String columnWidthSpec,
      int index, boolean first, boolean last, boolean insideWrapper, boolean insideGroup,
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

  public double getContainerWidth() {
    return containerWidth;
  }

  /**
   * Returns the column width specification as computed by the parent section.
   * For percentage widths: "33.33" (just the number)
   * For pixel widths: "150px" (with px suffix)
   * For auto columns: "100" or "33.333333333333336" (full precision)
   */
  public String getColumnWidthSpec() {
    return columnWidthSpec;
  }

  public int getIndex() {
    return index;
  }

  public boolean isFirst() {
    return first;
  }

  public boolean isLast() {
    return last;
  }

  public boolean isInsideWrapper() {
    return insideWrapper;
  }

  public boolean isInsideGroup() {
    return insideGroup;
  }

  /**
   * Returns a deterministic unique ID with the given prefix.
   * The counter is shared across all child contexts derived from the same root.
   */
  public String nextUniqueId(String prefix) {
    return prefix + "-" + idCounter.getAndIncrement();
  }

  /**
   * Creates a child context with a narrower container width.
   */
  public RenderContext withWidth(double width) {
    return new RenderContext(width, columnWidthSpec, index, first, last, insideWrapper, insideGroup, idCounter);
  }

  /**
   * Creates a child context with container width and column width percentage.
   */
  public RenderContext withColumnWidth(double width, String columnWidthSpec) {
    return new RenderContext(width, columnWidthSpec, index, first, last, insideWrapper, insideGroup, idCounter);
  }

  /**
   * Creates a child context with positioning info for column rendering.
   */
  public RenderContext withPosition(int index, boolean first, boolean last) {
    return new RenderContext(containerWidth, columnWidthSpec, index, first, last, insideWrapper, insideGroup, idCounter);
  }

  /**
   * Creates a child context indicating the component is inside a wrapper.
   */
  public RenderContext withInsideWrapper(boolean insideWrapper) {
    return new RenderContext(containerWidth, columnWidthSpec, index, first, last, insideWrapper, insideGroup, idCounter);
  }

  /**
   * Creates a child context indicating the component is inside a group.
   */
  public RenderContext withInsideGroup(boolean insideGroup) {
    return new RenderContext(containerWidth, columnWidthSpec, index, first, last, insideWrapper, insideGroup, idCounter);
  }
}
