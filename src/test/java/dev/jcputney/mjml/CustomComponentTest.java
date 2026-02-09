package dev.jcputney.mjml;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.component.BodyComponent;
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
}
