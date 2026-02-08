package dev.jcputney.javamjml.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lightweight mutable tree node representing an MJML element.
 * Each node has a tag name, attributes, text content, and children.
 */
public class MjmlNode {

  private final String tagName;
  private final Map<String, String> attributes;
  private final List<MjmlNode> children;
  private String textContent;
  private MjmlNode parent;

  public MjmlNode(String tagName) {
    this.tagName = tagName;
    this.attributes = new LinkedHashMap<>();
    this.children = new ArrayList<>();
    this.textContent = "";
  }

  public String getTagName() {
    return tagName;
  }

  public String getAttribute(String name) {
    return attributes.get(name);
  }

  public String getAttribute(String name, String defaultValue) {
    return attributes.getOrDefault(name, defaultValue);
  }

  public void setAttribute(String name, String value) {
    attributes.put(name, value);
  }

  public boolean hasAttribute(String name) {
    return attributes.containsKey(name);
  }

  public Map<String, String> getAttributes() {
    return Collections.unmodifiableMap(attributes);
  }

  public List<MjmlNode> getChildren() {
    return Collections.unmodifiableList(children);
  }

  public void addChild(MjmlNode child) {
    child.parent = this;
    children.add(child);
  }

  /**
   * Replaces this node in its parent's children list with the given nodes.
   * Used by include resolution to replace mj-include with resolved content.
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
    }
  }

  /**
   * Inserts a child at the given index.
   */
  public void insertChild(int index, MjmlNode child) {
    child.parent = this;
    children.add(index, child);
  }

  public MjmlNode getParent() {
    return parent;
  }

  public String getTextContent() {
    return textContent;
  }

  public void setTextContent(String textContent) {
    this.textContent = textContent != null ? textContent : "";
  }

  public void appendTextContent(String text) {
    if (text != null) {
      this.textContent += text;
    }
  }

  /**
   * Returns direct children with the specified tag name.
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
   * Returns the first direct child with the specified tag name, or null.
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
   * Recursively finds all descendants with the specified tag name.
   */
  public List<MjmlNode> findAll(String tag) {
    List<MjmlNode> result = new ArrayList<>();
    findAllRecursive(tag, result);
    return result;
  }

  private void findAllRecursive(String tag, List<MjmlNode> result) {
    for (MjmlNode child : children) {
      if (child.tagName.equals(tag)) {
        result.add(child);
      }
      child.findAllRecursive(tag, result);
    }
  }

  /**
   * Returns the inner HTML content - the serialized content of all children.
   * For ending tags with CDATA-wrapped content, this returns the raw HTML.
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
        sb.append(' ').append(attr.getKey()).append("=\"").append(attr.getValue()).append('"');
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
