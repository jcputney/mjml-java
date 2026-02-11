package dev.jcputney.mjml.component;

import dev.jcputney.mjml.context.AttributeResolver;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import java.util.Map;

/**
 * Abstract base class for all MJML components. Sealed to permit only BodyComponent and
 * HeadComponent subtypes.
 */
public abstract sealed class BaseComponent permits BodyComponent, HeadComponent {

  /** The parsed MJML node backing this component. */
  protected final MjmlNode node;

  /** The global rendering context shared across all components. */
  protected final GlobalContext globalContext;

  /** The render-phase context for the current rendering pass. */
  protected final RenderContext renderContext;

  /**
   * Creates a new component bound to the given node and contexts.
   *
   * @param node the parsed MJML node
   * @param globalContext the global rendering context
   * @param renderContext the render-phase context
   */
  protected BaseComponent(MjmlNode node, GlobalContext globalContext, RenderContext renderContext) {
    this.node = node;
    this.globalContext = globalContext;
    this.renderContext = renderContext;
  }

  /**
   * Returns the MJML tag name this component handles.
   *
   * @return the MJML tag name
   */
  public abstract String getTagName();

  /**
   * Returns the default attribute values for this component.
   *
   * @return a map of attribute names to their default values
   */
  public abstract Map<String, String> getDefaultAttributes();

  /**
   * Resolves an attribute value using the 5-level cascade.
   *
   * @param name the attribute name to resolve
   * @return the resolved attribute value, or {@code null} if not found
   */
  public String getAttribute(String name) {
    return AttributeResolver.resolve(node, name, globalContext, getDefaultAttributes());
  }

  /**
   * Resolves an attribute value, returning the provided default if not found.
   *
   * @param name the attribute name to resolve
   * @param defaultValue the value to return if the attribute is not found
   * @return the resolved attribute value, or {@code defaultValue} if not found
   */
  public String getAttribute(String name, String defaultValue) {
    String value = getAttribute(name);
    return value != null ? value : defaultValue;
  }

  /**
   * Returns the parsed MJML node backing this component.
   *
   * @return the MJML node
   */
  public MjmlNode getNode() {
    return node;
  }
}
