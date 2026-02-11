package dev.jcputney.mjml.component;

import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Registry mapping MJML tag names to component factories. Supports registration of custom
 * components.
 */
public class ComponentRegistry {

  private static final Logger LOG = Logger.getLogger(ComponentRegistry.class.getName());

  private final Map<String, ComponentFactory> factories = new LinkedHashMap<>();
  private boolean frozen = false;

  /** Creates a new, empty component registry. */
  public ComponentRegistry() {}

  /**
   * Registers a component factory for the given tag name.
   *
   * @param tagName the MJML tag name to register (e.g. "mj-section")
   * @param factory the factory used to create component instances for the tag
   * @throws IllegalStateException if the registry has been frozen
   */
  public void register(String tagName, ComponentFactory factory) {
    if (frozen) {
      throw new IllegalStateException(
          "ComponentRegistry is frozen; cannot register tag: " + tagName);
    }
    factories.put(tagName, factory);
  }

  /** Freezes this registry, preventing further registrations. */
  public void freeze() {
    this.frozen = true;
  }

  /**
   * Creates a component instance for the given node. Returns null if no factory is registered for
   * the tag.
   *
   * @param node the parsed MJML node to create a component for
   * @param globalContext the document-wide context gathered during head processing
   * @param renderContext the current rendering context (container width, position, etc.)
   * @return the created component instance, or {@code null} if no factory is registered
   */
  public BaseComponent createComponent(
      MjmlNode node, GlobalContext globalContext, RenderContext renderContext) {
    ComponentFactory factory = factories.get(node.getTagName());
    if (factory == null) {
      LOG.warning(() -> "Unknown MJML tag: " + node.getTagName());
      return null;
    }
    return factory.create(node, globalContext, renderContext);
  }
}
