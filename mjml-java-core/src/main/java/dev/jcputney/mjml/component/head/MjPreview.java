package dev.jcputney.mjml.component.head;

import dev.jcputney.mjml.component.HeadComponent;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;

/** Sets the preview text (preheader) from mj-preview content. */
public class MjPreview extends HeadComponent {

  /**
   * Creates a new MjPreview component.
   *
   * @param node the parsed MJML node for this component
   * @param globalContext the global rendering context
   * @param renderContext the current render context
   */
  public MjPreview(MjmlNode node, GlobalContext globalContext, RenderContext renderContext) {
    super(node, globalContext, renderContext);
  }

  @Override
  public String getTagName() {
    return "mj-preview";
  }

  @Override
  public void process() {
    String content = node.getInnerHtml();
    if (content != null && !content.isBlank()) {
      globalContext.metadata().setPreviewText(content.trim());
    }
  }
}
