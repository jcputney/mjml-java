package dev.jcputney.javamjml.component.head;

import dev.jcputney.javamjml.component.HeadComponent;
import dev.jcputney.javamjml.context.GlobalContext;
import dev.jcputney.javamjml.context.RenderContext;
import dev.jcputney.javamjml.parser.MjmlNode;

/**
 * Sets the preview text (preheader) from mj-preview content.
 */
public class MjPreview extends HeadComponent {

  public MjPreview(MjmlNode node, GlobalContext globalContext, RenderContext renderContext) {
    super(node, globalContext, renderContext);
  }

  @Override
  public String getTagName() {
    return "mj-preview";
  }

  @Override
  public void process() {
    String content = node.getInnerHtml().trim();
    globalContext.setPreviewText(content);
  }
}
