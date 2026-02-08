package dev.jcputney.javamjml.component.head;

import dev.jcputney.javamjml.component.HeadComponent;
import dev.jcputney.javamjml.context.GlobalContext;
import dev.jcputney.javamjml.context.RenderContext;
import dev.jcputney.javamjml.parser.MjmlNode;

/**
 * The mj-head container component. Processing is handled by the pipeline
 * which iterates over its children directly.
 */
public class MjHead extends HeadComponent {

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
