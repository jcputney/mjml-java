package dev.jcputney.mjml.component;

import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Registry mapping MJML tag names to component factories.
 * Supports registration of custom components.
 */
public class ComponentRegistry {

  private final Map<String, ComponentFactory> factories = new LinkedHashMap<>();

  /**
   * Registers a component factory for the given tag name.
   */
  public void register(String tagName, ComponentFactory factory) {
    factories.put(tagName, factory);
  }

  /**
   * Creates a component instance for the given node.
   * Returns null if no factory is registered for the tag.
   */
  public BaseComponent createComponent(MjmlNode node, GlobalContext globalContext,
      RenderContext renderContext) {
    ComponentFactory factory = factories.get(node.getTagName());
    if (factory == null) {
      return null;
    }
    return factory.create(node, globalContext, renderContext);
  }

  /**
   * Returns true if a factory is registered for the given tag name.
   */
  public boolean hasComponent(String tagName) {
    return factories.containsKey(tagName);
  }
}
