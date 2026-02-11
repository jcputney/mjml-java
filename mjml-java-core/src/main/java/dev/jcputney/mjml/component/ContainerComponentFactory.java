package dev.jcputney.mjml.component;

import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;

/**
 * Functional interface for creating container component instances that need access to the {@link
 * ComponentRegistry} in order to instantiate and render child components.
 *
 * <p>Use this with {@link
 * dev.jcputney.mjml.MjmlConfiguration.Builder#registerContainerComponent(String,
 * ContainerComponentFactory)} for components that render nested MJML children (similar to {@code
 * mj-section} or {@code mj-column}).
 *
 * @see ComponentFactory
 */
@FunctionalInterface
public interface ContainerComponentFactory {

  /**
   * Creates a container component instance from the given parsed node.
   *
   * @param node the parsed MJML node for the component
   * @param globalContext the document-wide context gathered during head processing
   * @param renderContext the current rendering context (container width, position, etc.)
   * @param registry the component registry used to create and render child components
   * @return the created container component instance
   */
  BaseComponent create(
      MjmlNode node,
      GlobalContext globalContext,
      RenderContext renderContext,
      ComponentRegistry registry);
}
