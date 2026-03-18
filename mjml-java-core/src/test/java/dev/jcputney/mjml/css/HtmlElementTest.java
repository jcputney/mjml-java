package dev.jcputney.mjml.css;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

/** Tests for HtmlElement attribute manipulation, tree navigation, and position tracking. */
class HtmlElementTest {

    // --- getAttribute / getAttributes ---

    @Test
    void getAttributeReturnsNullForAbsentAttribute() {
        HtmlElement el = new HtmlElement("div", Map.of());
        assertNull(el.getAttribute("class"), "Absent attribute should return null");
    }

    @Test
    void getAttributeRoundtrip() {
        HtmlElement el = new HtmlElement("div", Map.of("id", "main", "class", "container"));
        assertEquals("main", el.getAttribute("id"));
        assertEquals("container", el.getAttribute("class"));
    }

    @Test
    void getAttributeIsCaseInsensitive() {
        HtmlElement el = new HtmlElement("div", Map.of("data-value", "42"));
        assertEquals("42", el.getAttribute("data-value"));
        // getAttribute lowercases the name lookup
        assertEquals("42", el.getAttribute("DATA-VALUE"));
    }

    @Test
    void getAttributesReturnsUnmodifiableMap() {
        HtmlElement el = new HtmlElement("div", Map.of("id", "test"));
        Map<String, String> attrs = el.getAttributes();
        assertNotNull(attrs);
        assertEquals("test", attrs.get("id"));
        assertThrows(
                UnsupportedOperationException.class,
                () -> attrs.put("new-attr", "value"),
                "getAttributes() should return unmodifiable map");
    }

    @Test
    void setStyleUpdatesStyleAndInvalidatesCachedAttributes() {
        HtmlElement el = new HtmlElement("div", Map.of("style", "color: red;"));
        assertEquals("color: red;", el.getStyle());

        // Grab cached unmodifiable map
        Map<String, String> firstAttrs = el.getAttributes();
        assertEquals("color: red;", firstAttrs.get("style"));

        // Update style
        el.setStyle("color: blue;");
        assertEquals("color: blue;", el.getStyle());

        // getAttributes() should reflect the new style (cache invalidated)
        Map<String, String> secondAttrs = el.getAttributes();
        assertEquals("color: blue;", secondAttrs.get("style"));
    }

    @Test
    void multipleAttributesMaintainCorrectState() {
        HtmlElement el = new HtmlElement(
                "td",
                Map.of(
                        "class", "col",
                        "style", "padding: 10px;",
                        "align", "center",
                        "valign", "top"));
        assertEquals("col", el.getAttribute("class"));
        assertEquals("padding: 10px;", el.getAttribute("style"));
        assertEquals("center", el.getAttribute("align"));
        assertEquals("top", el.getAttribute("valign"));
        assertEquals(4, el.getAttributes().size());
    }

    // --- Tag name ---

    @Test
    void tagNameIsLowercased() {
        HtmlElement el = new HtmlElement("DIV", Map.of());
        assertEquals("div", el.getTagName());
    }

    // --- Class names ---

    @Test
    void getClassNamesReturnsEmptySetWhenNoClassAttribute() {
        HtmlElement el = new HtmlElement("div", Map.of());
        assertTrue(el.getClassNames().isEmpty());
    }

    @Test
    void getClassNamesParsesMultipleClasses() {
        HtmlElement el = new HtmlElement("div", Map.of("class", "foo bar baz"));
        assertEquals(3, el.getClassNames().size());
        assertTrue(el.getClassNames().contains("foo"));
        assertTrue(el.getClassNames().contains("bar"));
        assertTrue(el.getClassNames().contains("baz"));
    }

    // --- Parent/child/sibling navigation ---

    @Test
    void addChildSetsParentAndIndex() {
        HtmlElement parent = new HtmlElement("div", Map.of());
        HtmlElement child1 = new HtmlElement("span", Map.of());
        HtmlElement child2 = new HtmlElement("p", Map.of());

        parent.addChild(child1);
        parent.addChild(child2);

        assertEquals(parent, child1.getParent());
        assertEquals(parent, child2.getParent());
        assertEquals(0, child1.indexInParent());
        assertEquals(1, child2.indexInParent());
        assertEquals(2, parent.getChildren().size());
    }

    @Test
    void previousSiblingReturnsNullForFirstChild() {
        HtmlElement parent = new HtmlElement("div", Map.of());
        HtmlElement child = new HtmlElement("span", Map.of());
        parent.addChild(child);

        assertNull(child.previousSibling());
    }

    @Test
    void previousSiblingReturnsPrecedingChild() {
        HtmlElement parent = new HtmlElement("div", Map.of());
        HtmlElement child1 = new HtmlElement("span", Map.of());
        HtmlElement child2 = new HtmlElement("p", Map.of());
        parent.addChild(child1);
        parent.addChild(child2);

        assertEquals(child1, child2.previousSibling());
    }

    @Test
    void isDescendantOfWalksAncestorChain() {
        HtmlElement grandparent = new HtmlElement("div", Map.of());
        HtmlElement parent = new HtmlElement("table", Map.of());
        HtmlElement child = new HtmlElement("td", Map.of());
        grandparent.addChild(parent);
        parent.addChild(child);

        assertTrue(child.isDescendantOf(grandparent));
        assertTrue(child.isDescendantOf(parent));
        assertFalse(grandparent.isDescendantOf(child));
    }

    @Test
    void allDescendantsReturnsDepthFirstOrder() {
        HtmlElement root = new HtmlElement("div", Map.of());
        HtmlElement a = new HtmlElement("span", Map.of());
        HtmlElement b = new HtmlElement("p", Map.of());
        HtmlElement c = new HtmlElement("em", Map.of());
        root.addChild(a);
        root.addChild(b);
        a.addChild(c);

        var descendants = root.allDescendants();
        assertEquals(3, descendants.size());
        // Depth-first: a, c (child of a), then b
        assertEquals(a, descendants.get(0));
        assertEquals(c, descendants.get(1));
        assertEquals(b, descendants.get(2));
    }

    // --- Position tracking ---

    @Test
    void hasPositionInfoReturnsFalseByDefault() {
        HtmlElement el = new HtmlElement("div", Map.of());
        assertFalse(el.hasPositionInfo());
    }

    @Test
    void positionInfoRoundtrip() {
        HtmlElement el = new HtmlElement("div", Map.of());
        el.setTagStart(10);
        el.setTagEnd(25);
        el.setStyleAttrStart(15);
        el.setStyleAttrEnd(22);

        assertTrue(el.hasPositionInfo());
        assertEquals(10, el.getTagStart());
        assertEquals(25, el.getTagEnd());
        assertEquals(15, el.getStyleAttrStart());
        assertEquals(22, el.getStyleAttrEnd());
    }

    // --- toString ---

    @Test
    void toStringFormatsAsOpeningTag() {
        HtmlElement el = new HtmlElement("div", Map.of("class", "main"));
        String str = el.toString();
        assertTrue(str.startsWith("<div"));
        assertTrue(str.contains("class=\"main\""));
        assertTrue(str.endsWith(">"));
    }

    // --- getId ---

    @Test
    void getIdReturnsNullWhenNoId() {
        HtmlElement el = new HtmlElement("div", Map.of());
        assertNull(el.getId());
    }

    @Test
    void getIdReturnsIdAttribute() {
        HtmlElement el = new HtmlElement("div", Map.of("id", "header"));
        assertEquals("header", el.getId());
    }

    // --- Root element edge cases ---

    @Test
    void indexInParentReturnsZeroForRootElement() {
        HtmlElement el = new HtmlElement("html", Map.of());
        assertEquals(0, el.indexInParent());
    }

    @Test
    void previousSiblingReturnsNullForRootElement() {
        HtmlElement el = new HtmlElement("html", Map.of());
        assertNull(el.previousSibling());
    }
}
