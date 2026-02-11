package dev.jcputney.mjml.component.head;

import dev.jcputney.mjml.component.HeadComponent;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;

/**
 * The mj-head container component. Processing is handled by the pipeline which iterates over its
 * children directly.
 */
public class MjHead extends HeadComponent {

  /**
   * Creates a new MjHead component.
   *
   * @param node the parsed MJML node for this component
   * @param globalContext the global rendering context
   * @param renderContext the current render context
   */
  public MjHead(MjmlNode node, GlobalContext globalContext, RenderContext renderContext) {
    super(node, globalContext, renderContext);
  }

  @Override
  public String getTagName() {
    return "mj-head";
  }

  @Override
  public void process() {
    // Children are processed individually by the pipeline
  }
}
