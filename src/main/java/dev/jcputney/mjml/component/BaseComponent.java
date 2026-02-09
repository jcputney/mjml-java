package dev.jcputney.mjml.component;

import dev.jcputney.mjml.context.AttributeResolver;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import java.util.Map;

/**
 * Abstract base class for all MJML components.
 * Sealed to permit only BodyComponent and HeadComponent subtypes.
 */
public abstract sealed class BaseComponent permits BodyComponent, HeadComponent {

  protected final MjmlNode node;
  protected final GlobalContext globalContext;
  protected final RenderContext renderContext;

  protected BaseComponent(MjmlNode node, GlobalContext globalContext,
      RenderContext renderContext) {
    this.node = node;
    this.globalContext = globalContext;
    this.renderContext = renderContext;
  }

  /**
   * Returns the MJML tag name this component handles.
   */
  public abstract String getTagName();

  /**
   * Returns the default attribute values for this component.
   */
  public abstract Map<String, String> getDefaultAttributes();

  /**
   * Resolves an attribute value using the 5-level cascade.
   */
  public String getAttribute(String name) {
    return AttributeResolver.resolve(node, name, globalContext, getDefaultAttributes());
  }

  /**
   * Resolves an attribute value, returning the provided default if not found.
   */
  public String getAttribute(String name, String defaultValue) {
    String value = getAttribute(name);
    return value != null ? value : defaultValue;
  }

  public MjmlNode getNode() {
    return node;
  }
}
