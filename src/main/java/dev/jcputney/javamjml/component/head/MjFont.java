package dev.jcputney.javamjml.component.head;

import dev.jcputney.javamjml.component.HeadComponent;
import dev.jcputney.javamjml.context.GlobalContext;
import dev.jcputney.javamjml.context.RenderContext;
import dev.jcputney.javamjml.parser.MjmlNode;

/**
 * Registers a web font from mj-font attributes (name, href).
 */
public class MjFont extends HeadComponent {

  public MjFont(MjmlNode node, GlobalContext globalContext, RenderContext renderContext) {
    super(node, globalContext, renderContext);
  }

  @Override
  public String getTagName() {
    return "mj-font";
  }

  @Override
  public void process() {
    String name = node.getAttribute("name");
    String href = node.getAttribute("href");
    if (name != null && href != null) {
      globalContext.registerFontOverride(name, href);
    }
  }
}
