package dev.jcputney.javamjml.css;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Lightweight HTML element representation for CSS selector matching and style inlining.
 * This is NOT a full DOM - it's a minimal model that supports the operations needed
 * by the CSS inliner: tag name, attributes, parent/child/sibling navigation.
 */
public final class HtmlElement {

  private final String tagName;
  private final Map<String, String> attributes;
  private final List<HtmlElement> children;
  private HtmlElement parent;

  // Position in the original HTML string for in-place style modification
  private int tagStart = -1;   // position of '<'
  private int tagEnd = -1;     // position after '>'
  private int styleAttrStart = -1;  // position of style="
  private int styleAttrEnd = -1;    // position after closing "

  public HtmlElement(String tagName, Map<String, String> attributes) {
    this.tagName = tagName.toLowerCase();
    this.attributes = new LinkedHashMap<>(attributes);
    this.children = new ArrayList<>();
  }

  public String getTagName() {
    return tagName;
  }

  public String getAttribute(String name) {
    return attributes.get(name.toLowerCase());
  }

  public Map<String, String> getAttributes() {
    return Collections.unmodifiableMap(attributes);
  }

  public String getId() {
    return attributes.get("id");
  }

  public Set<String> getClassNames() {
    String cls = attributes.get("class");
    if (cls == null || cls.isBlank()) {
      return Set.of();
    }
    return Set.of(cls.trim().split("\\s+"));
  }

  public String getStyle() {
    return attributes.get("style");
  }

  public void setStyle(String style) {
    attributes.put("style", style);
  }

  public HtmlElement getParent() {
    return parent;
  }

  public List<HtmlElement> getChildren() {
    return children;
  }

  public void addChild(HtmlElement child) {
    child.parent = this;
    children.add(child);
  }

  /**
   * Returns the index of this element among its parent's children.
   */
  public int indexInParent() {
    if (parent == null) {
      return 0;
    }
    return parent.children.indexOf(this);
  }

  /**
   * Returns the previous sibling element, or null if this is the first child.
   */
  public HtmlElement previousSibling() {
    if (parent == null) {
      return null;
    }
    int idx = parent.children.indexOf(this);
    if (idx <= 0) {
      return null;
    }
    return parent.children.get(idx - 1);
  }

  /**
   * Checks if this element is a descendant of the given ancestor.
   */
  public boolean isDescendantOf(HtmlElement ancestor) {
    HtmlElement current = this.parent;
    while (current != null) {
      if (current == ancestor) {
        return true;
      }
      current = current.parent;
    }
    return false;
  }

  /**
   * Returns all descendant elements in document order (depth-first).
   */
  public List<HtmlElement> allDescendants() {
    List<HtmlElement> result = new ArrayList<>();
    collectDescendants(this, result);
    return result;
  }

  private static void collectDescendants(HtmlElement element, List<HtmlElement> result) {
    for (HtmlElement child : element.children) {
      result.add(child);
      collectDescendants(child, result);
    }
  }

  // --- Position tracking for in-place modification ---

  public int getTagStart() {
    return tagStart;
  }

  public void setTagStart(int tagStart) {
    this.tagStart = tagStart;
  }

  public int getTagEnd() {
    return tagEnd;
  }

  public void setTagEnd(int tagEnd) {
    this.tagEnd = tagEnd;
  }

  public int getStyleAttrStart() {
    return styleAttrStart;
  }

  public void setStyleAttrStart(int styleAttrStart) {
    this.styleAttrStart = styleAttrStart;
  }

  public int getStyleAttrEnd() {
    return styleAttrEnd;
  }

  public void setStyleAttrEnd(int styleAttrEnd) {
    this.styleAttrEnd = styleAttrEnd;
  }

  /**
   * Whether this element has position information for in-place style modification.
   */
  public boolean hasPositionInfo() {
    return tagStart >= 0 && tagEnd >= 0;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("<").append(tagName);
    for (Map.Entry<String, String> attr : attributes.entrySet()) {
      sb.append(" ").append(attr.getKey()).append("=\"").append(attr.getValue()).append("\"");
    }
    sb.append(">");
    return sb.toString();
  }
}
