package dev.jcputney.mjml.component;

import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;

/**
 * Functional interface for creating component instances from parsed nodes.
 */
@FunctionalInterface
public interface ComponentFactory {

  BaseComponent create(MjmlNode node, GlobalContext globalContext, RenderContext renderContext);
}
