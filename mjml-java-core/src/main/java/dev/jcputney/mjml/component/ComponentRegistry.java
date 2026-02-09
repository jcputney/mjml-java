package dev.jcputney.mjml.component;

import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Registry mapping MJML tag names to component factories.
 * Supports registration of custom components.
 */
public class ComponentRegistry {

  private static final Logger LOG = Logger.getLogger(ComponentRegistry.class.getName());

  private final Map<String, ComponentFactory> factories = new LinkedHashMap<>();
  private boolean frozen = false;

  /**
   * Registers a component factory for the given tag name.
   *
   * @throws IllegalStateException if the registry has been frozen
   */
  public void register(String tagName, ComponentFactory factory) {
    if (frozen) {
      throw new IllegalStateException(
          "ComponentRegistry is frozen; cannot register tag: " + tagName);
    }
    factories.put(tagName, factory);
  }

  /**
   * Freezes this registry, preventing further registrations.
   */
  public void freeze() {
    this.frozen = true;
  }

  /**
   * Creates a component instance for the given node.
   * Returns null if no factory is registered for the tag.
   */
  public BaseComponent createComponent(MjmlNode node, GlobalContext globalContext,
      RenderContext renderContext) {
    ComponentFactory factory = factories.get(node.getTagName());
    if (factory == null) {
      LOG.warning(() -> "Unknown MJML tag: " + node.getTagName());
      return null;
    }
    return factory.create(node, globalContext, renderContext);
  }

}
