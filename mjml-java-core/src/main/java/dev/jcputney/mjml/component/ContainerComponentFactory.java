package dev.jcputney.mjml.component;

import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;

/**
 * Functional interface for creating container component instances that need access to the
 * {@link ComponentRegistry} in order to instantiate and render child components.
 *
 * <p>Use this with
 * {@link dev.jcputney.mjml.MjmlConfiguration.Builder#registerContainerComponent(String, ContainerComponentFactory)}
 * for components that render nested MJML children (similar to {@code mj-section} or
 * {@code mj-column}).</p>
 *
 * @see ComponentFactory
 */
@FunctionalInterface
public interface ContainerComponentFactory {

  BaseComponent create(MjmlNode node, GlobalContext globalContext,
      RenderContext renderContext, ComponentRegistry registry);
}
