package dev.jcputney.mjml.parser;

import dev.jcputney.mjml.util.HtmlEscaper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lightweight mutable tree node representing an MJML element. Each node has a tag name, attributes,
 * text content, and children.
 */
public class MjmlNode {

  private final String tagName;
  private final Map<String, String> attributes;
  private final List<MjmlNode> children;
  private List<MjmlNode> unmodifiableChildren;
  private Map<String, String> unmodifiableAttributes;
  private String textContent;
  private MjmlNode parent;

  /**
   * Creates a new MJML node with the given tag name and empty attributes, children, and text.
   *
   * @param tagName the tag name for this node (e.g., "mj-body", "mj-section")
   */
  public MjmlNode(String tagName) {
    this.tagName = tagName;
    this.attributes = new LinkedHashMap<>();
    this.children = new ArrayList<>();
    this.textContent = "";
  }

  /**
   * Returns the tag name of this node.
   *
   * @return the tag name
   */
  public String getTagName() {
    return tagName;
  }

  /**
   * Returns the value of the attribute with the given name, or {@code null} if not present.
   *
   * @param name the attribute name
   * @return the attribute value, or {@code null} if the attribute is not set
   */
  public String getAttribute(String name) {
    return attributes.get(name);
  }

  /**
   * Returns the value of the attribute with the given name, or the default value if not present.
   *
   * @param name the attribute name
   * @param defaultValue the value to return if the attribute is not set
   * @return the attribute value, or {@code defaultValue} if the attribute is not set
   */
  public String getAttribute(String name, String defaultValue) {
    return attributes.getOrDefault(name, defaultValue);
  }

  /**
   * Sets the attribute with the given name to the given value.
   *
   * @param name the attribute name
   * @param value the attribute value
   */
  public void setAttribute(String name, String value) {
    attributes.put(name, value);
    unmodifiableAttributes = null; // invalidate cache
  }

  /**
   * Returns an unmodifiable view of all attributes on this node.
   *
   * @return an unmodifiable map of attribute names to values
   */
  public Map<String, String> getAttributes() {
    if (unmodifiableAttributes == null) {
      unmodifiableAttributes = Collections.unmodifiableMap(attributes);
    }
    return unmodifiableAttributes;
  }

  /**
   * Returns an unmodifiable view of this node's children. The returned list is cached and
   * invalidated when children are added or replaced.
   *
   * @return an unmodifiable list of child nodes
   */
  public List<MjmlNode> getChildren() {
    if (unmodifiableChildren == null) {
      unmodifiableChildren = Collections.unmodifiableList(children);
    }
    return unmodifiableChildren;
  }

  /**
   * Adds a child node to this node and sets the child's parent reference.
   *
   * @param child the child node to add
   */
  public void addChild(MjmlNode child) {
    child.parent = this;
    children.add(child);
    unmodifiableChildren = null; // invalidate cache
  }

  /**
   * Replaces this node in its parent's children list with the given nodes. Used by include
   * resolution to replace mj-include with resolved content.
   *
   * @param replacements the list of nodes to insert in place of this node
   */
  public void replaceWith(List<MjmlNode> replacements) {
    if (parent == null) {
      return;
    }
    int index = parent.children.indexOf(this);
    if (index >= 0) {
      parent.children.remove(index);
      for (int i = 0; i < replacements.size(); i++) {
        MjmlNode replacement = replacements.get(i);
        replacement.parent = parent;
        parent.children.add(index + i, replacement);
      }
      parent.unmodifiableChildren = null; // invalidate cache
    }
  }

  /**
   * Returns the parent node, or {@code null} if this is a root node.
   *
   * @return the parent node, or {@code null}
   */
  public MjmlNode getParent() {
    return parent;
  }

  /**
   * Returns the text content of this node.
   *
   * @return the text content, never {@code null}
   */
  public String getTextContent() {
    return textContent;
  }

  /**
   * Sets the text content of this node. A {@code null} value is treated as empty string.
   *
   * @param textContent the text content to set
   */
  public void setTextContent(String textContent) {
    this.textContent = textContent != null ? textContent : "";
  }

  /**
   * Returns direct children with the specified tag name.
   *
   * @param tag the tag name to match
   * @return a list of matching child nodes (may be empty)
   */
  public List<MjmlNode> getChildrenByTag(String tag) {
    List<MjmlNode> result = new ArrayList<>();
    for (MjmlNode child : children) {
      if (child.tagName.equals(tag)) {
        result.add(child);
      }
    }
    return result;
  }

  /**
   * Returns the first direct child with the specified tag name, or {@code null} if none match.
   *
   * @param tag the tag name to match
   * @return the first matching child node, or {@code null}
   */
  public MjmlNode getFirstChildByTag(String tag) {
    for (MjmlNode child : children) {
      if (child.tagName.equals(tag)) {
        return child;
      }
    }
    return null;
  }

  /**
   * Returns the inner HTML content - the serialized content of all children. For ending tags with
   * CDATA-wrapped content, this returns the raw HTML.
   *
   * @return the inner HTML string
   */
  public String getInnerHtml() {
    if (!children.isEmpty()) {
      StringBuilder sb = new StringBuilder();
      for (MjmlNode child : children) {
        sb.append(child.getOuterHtml());
      }
      return sb.toString();
    }
    return textContent;
  }

  /**
   * Returns the outer HTML of this node including its tag, attributes, and content.
   *
   * @return the outer HTML string
   */
  public String getOuterHtml() {
    StringBuilder sb = new StringBuilder();
    if ("#text".equals(tagName)) {
      sb.append(textContent);
    } else if ("#cdata-section".equals(tagName)) {
      sb.append(textContent);
    } else {
      sb.append('<').append(tagName);
      for (Map.Entry<String, String> attr : attributes.entrySet()) {
        sb.append(' ')
            .append(attr.getKey())
            .append("=\"")
            .append(HtmlEscaper.escapeAttributeValue(attr.getValue()))
            .append('"');
      }
      if (children.isEmpty() && textContent.isEmpty()) {
        sb.append(" />");
      } else {
        sb.append('>');
        sb.append(getInnerHtml());
        sb.append("</").append(tagName).append('>');
      }
    }
    return sb.toString();
  }

  @Override
  public String toString() {
    return "MjmlNode{" + tagName + ", children=" + children.size() + "}";
  }
}
