package dev.jcputney.javamjml.component.head;

import dev.jcputney.javamjml.component.HeadComponent;
import dev.jcputney.javamjml.context.GlobalContext;
import dev.jcputney.javamjml.context.RenderContext;
import dev.jcputney.javamjml.parser.MjmlNode;

/**
 * Sets the responsive breakpoint from mj-breakpoint width attribute.
 */
public class MjBreakpoint extends HeadComponent {

  public MjBreakpoint(MjmlNode node, GlobalContext globalContext, RenderContext renderContext) {
    super(node, globalContext, renderContext);
  }

  @Override
  public String getTagName() {
    return "mj-breakpoint";
  }

  @Override
  public void process() {
    String width = node.getAttribute("width");
    if (width != null && !width.isEmpty()) {
      globalContext.setBreakpoint(width);
    }
  }
}
