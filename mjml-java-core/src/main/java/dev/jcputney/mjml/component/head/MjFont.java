package dev.jcputney.mjml.component.head;

import dev.jcputney.mjml.component.HeadComponent;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import java.util.logging.Logger;

/**
 * Registers a web font from mj-font attributes (name, href).
 */
public class MjFont extends HeadComponent {

  private static final Logger LOG = Logger.getLogger(MjFont.class.getName());

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
      String trimmed = href.trim().toLowerCase();
      if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
        LOG.warning("mj-font href must start with http:// or https://, skipping: " + name);
        return;
      }
      globalContext.registerFontOverride(name, href);
    }
  }
}
