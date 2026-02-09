package dev.jcputney.mjml.component;

import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import java.util.Map;

/**
 * Abstract base for head components (mj-attributes, mj-font, etc.).
 * Head components process metadata and don't produce HTML output directly.
 */
public abstract non-sealed class HeadComponent extends BaseComponent {

  protected HeadComponent(MjmlNode node, GlobalContext globalContext,
      RenderContext renderContext) {
    super(node, globalContext, renderContext);
  }

  /**
   * Processes this head component, updating the global context
   * with extracted metadata (fonts, styles, attributes, etc.).
   */
  public abstract void process();

  @Override
  public Map<String, String> getDefaultAttributes() {
    return Map.of();
  }
}
