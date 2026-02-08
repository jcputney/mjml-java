package dev.jcputney.javamjml.component;

import dev.jcputney.javamjml.context.GlobalContext;
import dev.jcputney.javamjml.context.RenderContext;
import dev.jcputney.javamjml.parser.MjmlNode;

/**
 * Functional interface for creating component instances from parsed nodes.
 */
@FunctionalInterface
public interface ComponentFactory {

  BaseComponent create(MjmlNode node, GlobalContext globalContext, RenderContext renderContext);
}
