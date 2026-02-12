package dev.jcputney.mjml.css;

import dev.jcputney.mjml.util.CssUnitParser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Lightweight HTML element representation for CSS selector matching and style inlining. This is NOT
 * a full DOM - it's a minimal model that supports the operations needed by the CSS inliner: tag
 * name, attributes, parent/child/sibling navigation.
 */
public final class HtmlElement {

  private final String tagName;
  private final Map<String, String> attributes;
  private final List<HtmlElement> children;
  private HtmlElement parent;

  // Cached class names set (computed lazily)
  private Set<String> cachedClassNames;
  // Cached index in parent's children list (set during addChild)
  private int cachedIndex = -1;
  // Cached list of all descendants (computed lazily, tree is immutable after parse)
  private List<HtmlElement> cachedDescendants;

  // Position in the original HTML string for in-place style modification
  private int tagStart = -1; // position of '<'
  private int tagEnd = -1; // position after '>'
  private int styleAttrStart = -1; // position of style="
  private int styleAttrEnd = -1; // position after closing "

  /**
   * Creates a new HTML element with the given tag name and attributes.
   *
   * @param tagName the tag name (will be lowercased)
   * @param attributes the element attributes
   */
  public HtmlElement(String tagName, Map<String, String> attributes) {
    this.tagName = tagName.toLowerCase();
    this.attributes = new LinkedHashMap<>(attributes);
    this.children = new ArrayList<>();
  }

  private static void collectDescendants(HtmlElement element, List<HtmlElement> result) {
    for (HtmlElement child : element.children) {
      result.add(child);
      collectDescendants(child, result);
    }
  }

  /**
   * Returns the tag name of this element (always lowercase).
   *
   * @return the tag name
   */
  public String getTagName() {
    return tagName;
  }

  /**
   * Returns the value of the attribute with the given name, or {@code null} if not present.
   *
   * @param name the attribute name (case-insensitive)
   * @return the attribute value, or {@code null} if the attribute is not set
   */
  public String getAttribute(String name) {
    return attributes.get(name.toLowerCase());
  }

  /**
   * Returns an unmodifiable view of all attributes on this element.
   *
   * @return an unmodifiable map of attribute names to values
   */
  public Map<String, String> getAttributes() {
    return Collections.unmodifiableMap(attributes);
  }

  /**
   * Returns the value of the {@code id} attribute, or {@code null} if not set.
   *
   * @return the element ID, or {@code null}
   */
  public String getId() {
    return attributes.get("id");
  }

  /**
   * Returns the set of CSS class names on this element, parsed from the {@code class} attribute.
   * The result is cached after the first call.
   *
   * @return an immutable set of class names, or an empty set if no classes are defined
   */
  public Set<String> getClassNames() {
    if (cachedClassNames != null) {
      return cachedClassNames;
    }
    String cls = attributes.get("class");
    if (cls == null || cls.isBlank()) {
      cachedClassNames = Set.of();
    } else {
      cachedClassNames = Set.of(CssUnitParser.WHITESPACE.split(cls.trim()));
    }
    return cachedClassNames;
  }

  /**
   * Returns the value of the {@code style} attribute, or {@code null} if not set.
   *
   * @return the inline style string, or {@code null}
   */
  public String getStyle() {
    return attributes.get("style");
  }

  /**
   * Sets the inline {@code style} attribute on this element.
   *
   * @param style the new style string
   */
  public void setStyle(String style) {
    attributes.put("style", style);
  }

  /**
   * Returns the parent element, or {@code null} if this is the root element.
   *
   * @return the parent element, or {@code null}
   */
  public HtmlElement getParent() {
    return parent;
  }

  /**
   * Returns the list of child elements.
   *
   * @return the mutable list of children
   */
  public List<HtmlElement> getChildren() {
    return children;
  }

  /**
   * Adds a child element to this element, setting its parent reference and caching its index.
   *
   * @param child the child element to add
   */
  public void addChild(HtmlElement child) {
    child.parent = this;
    child.cachedIndex = children.size();
    children.add(child);
  }

  /**
   * Returns the index of this element among its parent's children.
   *
   * @return the zero-based index within the parent's children, or 0 if there is no parent
   */
  public int indexInParent() {
    if (parent == null) {
      return 0;
    }
    return cachedIndex >= 0 ? cachedIndex : parent.children.indexOf(this);
  }

  /**
   * Returns the previous sibling element, or null if this is the first child.
   *
   * @return the previous sibling, or {@code null} if none exists
   */
  public HtmlElement previousSibling() {
    if (parent == null) {
      return null;
    }
    int idx = indexInParent();
    if (idx <= 0) {
      return null;
    }
    return parent.children.get(idx - 1);
  }

  /**
   * Checks if this element is a descendant of the given ancestor.
   *
   * @param ancestor the potential ancestor element
   * @return {@code true} if this element is a descendant of the given ancestor
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
   * Returns all descendant elements in document order (depth-first). The result is cached since the
   * tree is immutable after parsing.
   *
   * @return a list of all descendant elements in depth-first order
   */
  public List<HtmlElement> allDescendants() {
    if (cachedDescendants != null) {
      return cachedDescendants;
    }
    List<HtmlElement> result = new ArrayList<>();
    collectDescendants(this, result);
    cachedDescendants = result;
    return result;
  }

  // --- Position tracking for in-place modification ---

  /**
   * Returns the position of the opening {@code <} character in the original HTML string, or {@code
   * -1} if not set.
   *
   * @return the tag start position, or {@code -1}
   */
  public int getTagStart() {
    return tagStart;
  }

  /**
   * Sets the position of the opening {@code <} character in the original HTML string.
   *
   * @param tagStart the tag start position
   */
  public void setTagStart(int tagStart) {
    this.tagStart = tagStart;
  }

  /**
   * Returns the position immediately after the closing {@code >} character in the original HTML
   * string, or {@code -1} if not set.
   *
   * @return the tag end position, or {@code -1}
   */
  public int getTagEnd() {
    return tagEnd;
  }

  /**
   * Sets the position immediately after the closing {@code >} character in the original HTML
   * string.
   *
   * @param tagEnd the tag end position
   */
  public void setTagEnd(int tagEnd) {
    this.tagEnd = tagEnd;
  }

  /**
   * Returns the position of the {@code style="} substring in the original HTML string, or {@code
   * -1} if not set.
   *
   * @return the style attribute start position, or {@code -1}
   */
  public int getStyleAttrStart() {
    return styleAttrStart;
  }

  /**
   * Sets the position of the {@code style="} substring in the original HTML string.
   *
   * @param styleAttrStart the style attribute start position
   */
  public void setStyleAttrStart(int styleAttrStart) {
    this.styleAttrStart = styleAttrStart;
  }

  /**
   * Returns the position immediately after the closing quote of the {@code style} attribute in the
   * original HTML string, or {@code -1} if not set.
   *
   * @return the style attribute end position, or {@code -1}
   */
  public int getStyleAttrEnd() {
    return styleAttrEnd;
  }

  /**
   * Sets the position immediately after the closing quote of the {@code style} attribute in the
   * original HTML string.
   *
   * @param styleAttrEnd the style attribute end position
   */
  public void setStyleAttrEnd(int styleAttrEnd) {
    this.styleAttrEnd = styleAttrEnd;
  }

  /**
   * Whether this element has position information for in-place style modification.
   *
   * @return {@code true} if both tag start and tag end positions are set
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
