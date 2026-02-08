package dev.jcputney.javamjml.component.head;

import dev.jcputney.javamjml.component.HeadComponent;
import dev.jcputney.javamjml.context.GlobalContext;
import dev.jcputney.javamjml.context.RenderContext;
import dev.jcputney.javamjml.parser.MjmlNode;

/**
 * Adds CSS styles from mj-style content.
 * Supports inline="inline" attribute for styles that should be inlined.
 */
public class MjStyle extends HeadComponent {

  public MjStyle(MjmlNode node, GlobalContext globalContext, RenderContext renderContext) {
    super(node, globalContext, renderContext);
  }

  @Override
  public String getTagName() {
    return "mj-style";
  }

  @Override
  public void process() {
    String content = node.getInnerHtml().trim();
    if (content.isEmpty()) {
      return;
    }

    String inline = node.getAttribute("inline");
    if ("inline".equals(inline)) {
      globalContext.addInlineStyle(content);
    } else {
      globalContext.addStyle(content);
    }
  }
}
