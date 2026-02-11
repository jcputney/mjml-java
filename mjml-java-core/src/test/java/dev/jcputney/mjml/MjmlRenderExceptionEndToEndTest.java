package dev.jcputney.mjml;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Tests that a RuntimeException thrown during render() is wrapped in MjmlRenderException. */
class MjmlRenderExceptionEndToEndTest {

  @Test
  void runtimeExceptionInCustomComponentWrappedInMjmlRenderException() {
    MjmlConfiguration config =
        MjmlConfiguration.builder()
            .registerComponent("mj-exploding", ExplodingComponent::new)
            .build();

    String mjml =
        // language=MJML
        """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-exploding />
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    MjmlRenderException ex =
        assertThrows(MjmlRenderException.class, () -> MjmlRenderer.render(mjml, config));
    assertNotNull(ex.getCause(), "MjmlRenderException should wrap the original exception");
    assertInstanceOf(
        IllegalStateException.class,
        ex.getCause(),
        "Original IllegalStateException should be the cause");
  }

  /** A custom component whose render() method always throws a RuntimeException. */
  static class ExplodingComponent extends BodyComponent {

    ExplodingComponent(MjmlNode node, GlobalContext globalContext, RenderContext renderContext) {
      super(node, globalContext, renderContext);
    }

    @Override
    public String getTagName() {
      return "mj-exploding";
    }

    @Override
    public Map<String, String> getDefaultAttributes() {
      return Map.of();
    }

    @Override
    public String render() {
      throw new IllegalStateException("Boom! Component exploded during render");
    }
  }
}
