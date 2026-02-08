package dev.jcputney.mjml.component.head;

import dev.jcputney.mjml.component.HeadComponent;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;

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
