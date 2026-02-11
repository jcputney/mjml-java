package dev.jcputney.mjml.component;

import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;

/** Functional interface for creating component instances from parsed nodes. */
@FunctionalInterface
public interface ComponentFactory {

  /**
   * Creates a component instance from the given parsed node.
   *
   * @param node the parsed MJML node for the component
   * @param globalContext the document-wide context gathered during head processing
   * @param renderContext the current rendering context (container width, position, etc.)
   * @return the created component instance
   */
  BaseComponent create(MjmlNode node, GlobalContext globalContext, RenderContext renderContext);
}
