package dev.jcputney.mjml.context;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.Test;

class AttributeContextTest {

  @Test
  void defaultValues() {
    AttributeContext ctx = new AttributeContext();
    assertTrue(ctx.getDefaultAttributes("mj-text").isEmpty());
    assertTrue(ctx.getAllDefaults().isEmpty());
    assertTrue(ctx.getClassAttributes("some-class").isEmpty());
    assertTrue(ctx.getHtmlAttributes().isEmpty());
  }

  @Test
  void setAndGetDefaultAttributes() {
    AttributeContext ctx = new AttributeContext();
    ctx.setDefaultAttributes("mj-text", Map.of("color", "red", "font-size", "14px"));
    Map<String, String> attrs = ctx.getDefaultAttributes("mj-text");
    assertEquals("red", attrs.get("color"));
    assertEquals("14px", attrs.get("font-size"));
  }

  @Test
  void defaultAttributesMerge() {
    AttributeContext ctx = new AttributeContext();
    ctx.setDefaultAttributes("mj-text", Map.of("color", "red"));
    ctx.setDefaultAttributes("mj-text", Map.of("font-size", "16px"));
    Map<String, String> attrs = ctx.getDefaultAttributes("mj-text");
    assertEquals("red", attrs.get("color"));
    assertEquals("16px", attrs.get("font-size"));
  }

  @Test
  void defaultAttributesOverwrite() {
    AttributeContext ctx = new AttributeContext();
    ctx.setDefaultAttributes("mj-text", Map.of("color", "red"));
    ctx.setDefaultAttributes("mj-text", Map.of("color", "blue"));
    assertEquals("blue", ctx.getDefaultAttributes("mj-text").get("color"));
  }

  @Test
  void getAllDefaults() {
    AttributeContext ctx = new AttributeContext();
    ctx.setDefaultAttributes("mj-all", Map.of("font-family", "Arial"));
    assertEquals("Arial", ctx.getAllDefaults().get("font-family"));
  }

  @Test
  void getAllDefaultsEmptyWhenMjAllNotSet() {
    AttributeContext ctx = new AttributeContext();
    assertTrue(ctx.getAllDefaults().isEmpty());
  }

  @Test
  void missingTagReturnsEmptyMap() {
    AttributeContext ctx = new AttributeContext();
    Map<String, String> result = ctx.getDefaultAttributes("mj-nonexistent");
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void setAndGetClassAttributes() {
    AttributeContext ctx = new AttributeContext();
    ctx.setClassAttributes("highlight", Map.of("background-color", "yellow"));
    assertEquals("yellow", ctx.getClassAttributes("highlight").get("background-color"));
  }

  @Test
  void classAttributesMerge() {
    AttributeContext ctx = new AttributeContext();
    ctx.setClassAttributes("highlight", Map.of("background-color", "yellow"));
    ctx.setClassAttributes("highlight", Map.of("color", "black"));
    Map<String, String> attrs = ctx.getClassAttributes("highlight");
    assertEquals("yellow", attrs.get("background-color"));
    assertEquals("black", attrs.get("color"));
  }

  @Test
  void missingClassReturnsEmptyMap() {
    AttributeContext ctx = new AttributeContext();
    assertTrue(ctx.getClassAttributes("missing").isEmpty());
  }

  @Test
  void setAndGetHtmlAttributes() {
    AttributeContext ctx = new AttributeContext();
    ctx.setHtmlAttributes(".custom-class", Map.of("data-role", "header"));
    Map<String, Map<String, String>> all = ctx.getHtmlAttributes();
    assertEquals(1, all.size());
    assertEquals("header", all.get(".custom-class").get("data-role"));
  }

  @Test
  void htmlAttributesMerge() {
    AttributeContext ctx = new AttributeContext();
    ctx.setHtmlAttributes("#id1", Map.of("data-a", "1"));
    ctx.setHtmlAttributes("#id1", Map.of("data-b", "2"));
    Map<String, String> attrs = ctx.getHtmlAttributes().get("#id1");
    assertEquals("1", attrs.get("data-a"));
    assertEquals("2", attrs.get("data-b"));
  }

  @Test
  void htmlAttributesUnmodifiable() {
    AttributeContext ctx = new AttributeContext();
    ctx.setHtmlAttributes("#x", Map.of("a", "b"));
    assertThrows(
        UnsupportedOperationException.class, () -> ctx.getHtmlAttributes().put("new", Map.of()));
  }
}
