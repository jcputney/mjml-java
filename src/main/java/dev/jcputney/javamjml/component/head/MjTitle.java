package dev.jcputney.javamjml.component.head;

import dev.jcputney.javamjml.component.HeadComponent;
import dev.jcputney.javamjml.context.GlobalContext;
import dev.jcputney.javamjml.context.RenderContext;
import dev.jcputney.javamjml.parser.MjmlNode;

/**
 * Sets the document title from mj-title content.
 */
public class MjTitle extends HeadComponent {

  public MjTitle(MjmlNode node, GlobalContext globalContext, RenderContext renderContext) {
    super(node, globalContext, renderContext);
  }

  @Override
  public String getTagName() {
    return "mj-title";
  }

  @Override
  public void process() {
    String content = node.getInnerHtml().trim();
    globalContext.setTitle(content);
  }
}
