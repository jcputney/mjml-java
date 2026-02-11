package dev.jcputney.mjml.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import dev.jcputney.mjml.MjmlConfiguration;
import dev.jcputney.mjml.parser.MjmlNode;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AttributeResolverTest {

  private GlobalContext globalContext;
  private Map<String, String> componentDefaults;

  @BeforeEach
  void setUp() {
    globalContext = new GlobalContext(MjmlConfiguration.defaults());
    componentDefaults =
        Map.of(
            "color", "#000000",
            "font-size", "13px",
            "padding", "10px 25px");
  }

  @Test
  void inlineWinsOverAllLevels() {
    // Set up all cascade levels
    globalContext.attributes().setClassAttributes("myclass", Map.of("color", "#111111"));
    globalContext.attributes().setDefaultAttributes("mj-text", Map.of("color", "#222222"));
    globalContext.attributes().setDefaultAttributes("mj-all", Map.of("color", "#333333"));

    MjmlNode node = new MjmlNode("mj-text");
    node.setAttribute("color", "#inline");
    node.setAttribute("mj-class", "myclass");

    String result = AttributeResolver.resolve(node, "color", globalContext, componentDefaults);
    assertEquals("#inline", result);
  }

  @Test
  void mjClassOverridesTagDefaults() {
    globalContext.attributes().setClassAttributes("myclass", Map.of("color", "#class-value"));
    globalContext.attributes().setDefaultAttributes("mj-text", Map.of("color", "#tag-value"));
    globalContext.attributes().setDefaultAttributes("mj-all", Map.of("color", "#all-value"));

    MjmlNode node = new MjmlNode("mj-text");
    node.setAttribute("mj-class", "myclass");

    String result = AttributeResolver.resolve(node, "color", globalContext, componentDefaults);
    assertEquals("#class-value", result);
  }

  @Test
  void tagDefaultsOverrideMjAll() {
    globalContext.attributes().setDefaultAttributes("mj-text", Map.of("color", "#tag-value"));
    globalContext.attributes().setDefaultAttributes("mj-all", Map.of("color", "#all-value"));

    MjmlNode node = new MjmlNode("mj-text");

    String result = AttributeResolver.resolve(node, "color", globalContext, componentDefaults);
    assertEquals("#tag-value", result);
  }

  @Test
  void mjAllOverridesComponentDefaults() {
    globalContext.attributes().setDefaultAttributes("mj-all", Map.of("color", "#all-value"));

    MjmlNode node = new MjmlNode("mj-text");

    String result = AttributeResolver.resolve(node, "color", globalContext, componentDefaults);
    assertEquals("#all-value", result);
  }

  @Test
  void componentDefaultsAsFallback() {
    MjmlNode node = new MjmlNode("mj-text");

    String result = AttributeResolver.resolve(node, "color", globalContext, componentDefaults);
    assertEquals("#000000", result);
  }

  @Test
  void multipleMjClassFirstMatchWins() {
    globalContext.attributes().setClassAttributes("first", Map.of("color", "#first"));
    globalContext.attributes().setClassAttributes("second", Map.of("color", "#second"));

    MjmlNode node = new MjmlNode("mj-text");
    node.setAttribute("mj-class", "first second");

    String result = AttributeResolver.resolve(node, "color", globalContext, componentDefaults);
    assertEquals("#first", result);
  }

  @Test
  void missingAttributeReturnsNull() {
    MjmlNode node = new MjmlNode("mj-text");

    String result =
        AttributeResolver.resolve(node, "nonexistent", globalContext, componentDefaults);
    assertNull(result);
  }

  @Test
  void allFiveLevelsPopulatedInlineWins() {
    globalContext.attributes().setClassAttributes("cls", Map.of("font-size", "#class"));
    globalContext.attributes().setDefaultAttributes("mj-text", Map.of("font-size", "#tag"));
    globalContext.attributes().setDefaultAttributes("mj-all", Map.of("font-size", "#all"));

    MjmlNode node = new MjmlNode("mj-text");
    node.setAttribute("font-size", "#inline");
    node.setAttribute("mj-class", "cls");

    // All levels set: inline should win
    assertEquals(
        "#inline", AttributeResolver.resolve(node, "font-size", globalContext, componentDefaults));
  }

  @Test
  void withoutInlineClassWins() {
    globalContext.attributes().setClassAttributes("cls", Map.of("font-size", "#class"));
    globalContext.attributes().setDefaultAttributes("mj-text", Map.of("font-size", "#tag"));
    globalContext.attributes().setDefaultAttributes("mj-all", Map.of("font-size", "#all"));

    MjmlNode node = new MjmlNode("mj-text");
    node.setAttribute("mj-class", "cls");

    assertEquals(
        "#class", AttributeResolver.resolve(node, "font-size", globalContext, componentDefaults));
  }

  @Test
  void withoutInlineOrClassTagWins() {
    globalContext.attributes().setDefaultAttributes("mj-text", Map.of("font-size", "#tag"));
    globalContext.attributes().setDefaultAttributes("mj-all", Map.of("font-size", "#all"));

    MjmlNode node = new MjmlNode("mj-text");

    assertEquals(
        "#tag", AttributeResolver.resolve(node, "font-size", globalContext, componentDefaults));
  }
}
