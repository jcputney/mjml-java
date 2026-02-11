package dev.jcputney.mjml;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.component.ComponentRegistry;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Tests for custom component registration and rendering.
 */
class CustomComponentTest {

  /**
   * A simple custom component that renders a greeting.
   */
  static class MjGreeting extends BodyComponent {

    MjGreeting(MjmlNode node, GlobalContext globalContext, RenderContext renderContext) {
      super(node, globalContext, renderContext);
    }

    @Override
    public String getTagName() {
      return "mj-greeting";
    }

    @Override
    public Map<String, String> getDefaultAttributes() {
      return Map.of(
          "name", "World",
          "color", "#000000"
      );
    }

    @Override
    public String render() {
      String name = getAttribute("name", "World");
      String color = getAttribute("color", "#000000");
      return "<div style=\"color:" + color + ";\">Hello, " + name + "!</div>";
    }
  }

  @Test
  void registersAndRendersCustomComponent() {
    MjmlConfiguration config = MjmlConfiguration.builder()
        .registerComponent("mj-greeting", MjGreeting::new)
        .build();

    String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-greeting name="Claude" color="#ff0000" />
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml, config).html();
    assertNotNull(html);
    assertTrue(html.contains("Hello, Claude!"),
        "Custom component should render with provided attributes");
    assertTrue(html.contains("color:#ff0000"),
        "Custom component should use custom color attribute");
  }

  @Test
  void customComponentUsesDefaults() {
    MjmlConfiguration config = MjmlConfiguration.builder()
        .registerComponent("mj-greeting", MjGreeting::new)
        .build();

    String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-greeting />
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml, config).html();
    assertNotNull(html);
    assertTrue(html.contains("Hello, World!"),
        "Custom component should use default name when none provided");
  }

  @Test
  void customComponentCoexistsWithBuiltins() {
    MjmlConfiguration config = MjmlConfiguration.builder()
        .registerComponent("mj-greeting", MjGreeting::new)
        .build();

    String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Standard text</mj-text>
                <mj-greeting name="User" />
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml, config).html();
    assertNotNull(html);
    assertTrue(html.contains("Standard text"), "Built-in components should still work");
    assertTrue(html.contains("Hello, User!"), "Custom component should also render");
  }

  // --- Override built-in tests ---

  /**
   * A custom component that overrides the built-in mj-text.
   */
  static class MjCustomText extends BodyComponent {

    MjCustomText(MjmlNode node, GlobalContext globalContext, RenderContext renderContext) {
      super(node, globalContext, renderContext);
    }

    @Override
    public String getTagName() {
      return "mj-text";
    }

    @Override
    public Map<String, String> getDefaultAttributes() {
      return Map.of();
    }

    @Override
    public String render() {
      String content = node.getInnerHtml();
      return "<p class=\"custom-text\">" + (content != null ? content : "") + "</p>";
    }
  }

  @Test
  void overrideBuiltInComponent() {
    MjmlConfiguration config = MjmlConfiguration.builder()
        .registerComponent("mj-text", MjCustomText::new)
        .build();

    String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Overridden text</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml, config).html();
    assertNotNull(html);
    assertTrue(html.contains("custom-text"),
        "Custom override should replace built-in mj-text rendering");
    assertTrue(html.contains("Overridden text"),
        "Custom override should still render the content");
  }

  // --- Container component tests ---

  /**
   * A container component that renders a card wrapper around its children.
   */
  static class MjCard extends BodyComponent {

    private final ComponentRegistry registry;

    MjCard(MjmlNode node, GlobalContext globalContext, RenderContext renderContext,
        ComponentRegistry registry) {
      super(node, globalContext, renderContext);
      this.registry = registry;
    }

    @Override
    public String getTagName() {
      return "mj-card";
    }

    @Override
    public Map<String, String> getDefaultAttributes() {
      return Map.of("background-color", "#ffffff", "border-radius", "8px");
    }

    @Override
    public String render() {
      String bg = getAttribute("background-color", "#ffffff");
      String radius = getAttribute("border-radius", "8px");
      String children = renderChildren(registry);
      return "<div class=\"mj-card\" style=\"background-color:" + bg
          + ";border-radius:" + radius + ";\">" + children + "</div>";
    }
  }

  @Test
  void containerComponentRendersChildren() {
    MjmlConfiguration config = MjmlConfiguration.builder()
        .registerContainerComponent("mj-card", MjCard::new)
        .build();

    String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-card background-color="#f0f0f0">
                  <mj-text>Card content</mj-text>
                  <mj-image src="https://example.com/img.png" />
                </mj-card>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml, config).html();
    assertNotNull(html);
    assertTrue(html.contains("mj-card"),
        "Container component wrapper should be present");
    assertTrue(html.contains("background-color:#f0f0f0"),
        "Container should use provided background color");
    assertTrue(html.contains("Card content"),
        "Built-in mj-text child should render inside the container");
    assertTrue(html.contains("example.com/img.png"),
        "Built-in mj-image child should render inside the container");
  }

  @Test
  void containerComponentWithMixedChildren() {
    MjmlConfiguration config = MjmlConfiguration.builder()
        .registerComponent("mj-greeting", MjGreeting::new)
        .registerContainerComponent("mj-card", MjCard::new)
        .build();

    String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-card>
                  <mj-text>Built-in child</mj-text>
                  <mj-greeting name="Nested" />
                </mj-card>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml, config).html();
    assertNotNull(html);
    assertTrue(html.contains("Built-in child"),
        "Built-in child should render inside container");
    assertTrue(html.contains("Hello, Nested!"),
        "Custom child should render inside container");
  }

  @Test
  void containerComponentCoexistsWithLeafComponents() {
    MjmlConfiguration config = MjmlConfiguration.builder()
        .registerComponent("mj-greeting", MjGreeting::new)
        .registerContainerComponent("mj-card", MjCard::new)
        .build();

    String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-greeting name="Top" />
                <mj-card>
                  <mj-text>Inside card</mj-text>
                </mj-card>
                <mj-text>After card</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml, config).html();
    assertNotNull(html);
    assertTrue(html.contains("Hello, Top!"), "Leaf custom component should render");
    assertTrue(html.contains("Inside card"), "Container child should render");
    assertTrue(html.contains("After card"), "Content after container should render");
  }

  @Test
  void containerComponentOverridesBuiltIn() {
    // Override mj-section with a custom container
    MjmlConfiguration config = MjmlConfiguration.builder()
        .registerContainerComponent("mj-text", (node, ctx, rctx, reg) -> {
          return new BodyComponent(node, ctx, rctx) {
            @Override
            public String getTagName() {
              return "mj-text";
            }

            @Override
            public Map<String, String> getDefaultAttributes() {
              return Map.of();
            }

            @Override
            public String render() {
              return "<span class=\"overridden-via-container\">"
                  + node.getInnerHtml() + "</span>";
            }
          };
        })
        .build();

    String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Container override</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml, config).html();
    assertNotNull(html);
    assertTrue(html.contains("overridden-via-container"),
        "Container component registration should override built-in");
    assertTrue(html.contains("Container override"),
        "Overridden component should render the text content");
  }
}
