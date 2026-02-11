package dev.jcputney.mjml.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Tests for MjmlNode tree manipulation and serialization. */
class MjmlNodeTest {

  @Test
  void addChildSetsParent() {
    MjmlNode parent = new MjmlNode("mj-section");
    MjmlNode child = new MjmlNode("mj-column");

    parent.addChild(child);

    assertSame(parent, child.getParent(), "addChild should set the child's parent");
    assertEquals(1, parent.getChildren().size());
  }

  @Test
  void addChildInvalidatesUnmodifiableCache() {
    MjmlNode parent = new MjmlNode("mj-body");
    MjmlNode child1 = new MjmlNode("mj-section");
    parent.addChild(child1);

    // Access children to populate the unmodifiable cache
    List<MjmlNode> firstSnapshot = parent.getChildren();
    assertEquals(1, firstSnapshot.size());

    // Add another child - the cache should be invalidated
    MjmlNode child2 = new MjmlNode("mj-section");
    parent.addChild(child2);

    List<MjmlNode> secondSnapshot = parent.getChildren();
    assertEquals(
        2, secondSnapshot.size(), "Adding a child should invalidate the unmodifiable cache");
  }

  @Test
  void replaceWithInParent() {
    MjmlNode parent = new MjmlNode("mj-body");
    MjmlNode original = new MjmlNode("mj-include");
    MjmlNode replacement1 = new MjmlNode("mj-section");
    MjmlNode replacement2 = new MjmlNode("mj-section");
    parent.addChild(original);

    original.replaceWith(List.of(replacement1, replacement2));

    assertEquals(2, parent.getChildren().size(), "Parent should have 2 children after replacement");
    assertSame(replacement1, parent.getChildren().get(0));
    assertSame(replacement2, parent.getChildren().get(1));
    assertSame(parent, replacement1.getParent(), "Replacements should have their parent set");
    assertSame(parent, replacement2.getParent());
  }

  @Test
  void replaceWithNoParentIsNoOp() {
    MjmlNode orphan = new MjmlNode("mj-include");
    MjmlNode replacement = new MjmlNode("mj-section");

    // Should not throw
    orphan.replaceWith(List.of(replacement));

    assertNull(orphan.getParent());
    assertNull(
        replacement.getParent(), "Replacement should not have parent when original had no parent");
  }

  @Test
  void replaceWithEmptyListRemovesNode() {
    MjmlNode parent = new MjmlNode("mj-body");
    MjmlNode child1 = new MjmlNode("mj-section");
    MjmlNode child2 = new MjmlNode("mj-section");
    parent.addChild(child1);
    parent.addChild(child2);

    child1.replaceWith(Collections.emptyList());

    assertEquals(
        1, parent.getChildren().size(), "Node should be removed when replaced with empty list");
    assertSame(child2, parent.getChildren().get(0));
  }

  @Test
  void getChildrenByTagFiltersCorrectly() {
    MjmlNode parent = new MjmlNode("mj-body");
    MjmlNode section1 = new MjmlNode("mj-section");
    MjmlNode wrapper = new MjmlNode("mj-wrapper");
    MjmlNode section2 = new MjmlNode("mj-section");
    parent.addChild(section1);
    parent.addChild(wrapper);
    parent.addChild(section2);

    List<MjmlNode> sections = parent.getChildrenByTag("mj-section");
    assertEquals(2, sections.size(), "Should return only mj-section children");
    assertSame(section1, sections.get(0));
    assertSame(section2, sections.get(1));

    List<MjmlNode> wrappers = parent.getChildrenByTag("mj-wrapper");
    assertEquals(1, wrappers.size());

    List<MjmlNode> none = parent.getChildrenByTag("mj-column");
    assertTrue(none.isEmpty(), "Should return empty list for non-matching tag");
  }

  @Test
  void getInnerHtmlReturnsCdataContent() {
    MjmlNode text = new MjmlNode("mj-text");
    text.setTextContent("<p>Hello <b>world</b></p>");

    String inner = text.getInnerHtml();
    assertEquals(
        "<p>Hello <b>world</b></p>",
        inner,
        "getInnerHtml should return textContent when there are no children");
  }

  @Test
  void getOuterHtmlSerializesSelfClosing() {
    MjmlNode img = new MjmlNode("mj-image");
    img.setAttribute("src", "https://example.com/photo.jpg");
    img.setAttribute("alt", "Photo");

    String outer = img.getOuterHtml();
    assertTrue(outer.contains("<mj-image"), "Should start with the tag");
    assertTrue(
        outer.contains("src=\"https://example.com/photo.jpg\""), "Should contain src attribute");
    assertTrue(outer.contains("alt=\"Photo\""), "Should contain alt attribute");
    assertTrue(outer.endsWith("/>"), "Self-closing tag should end with />");
  }
}
