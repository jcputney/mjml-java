package dev.jcputney.mjml.component.head;

import dev.jcputney.mjml.component.HeadComponent;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import java.util.regex.Pattern;

/**
 * Adds CSS styles from mj-style content.
 * Supports inline="inline" attribute for styles that should be inlined.
 */
public class MjStyle extends HeadComponent {

  private static final Pattern STYLE_CLOSE_TAG = Pattern.compile("</style", Pattern.CASE_INSENSITIVE);

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

    // Strip any closing style tag injection to prevent CSS breakout
    content = STYLE_CLOSE_TAG.matcher(content).replaceAll("");

    String inline = node.getAttribute("inline");
    if ("inline".equals(inline)) {
      globalContext.addInlineStyle(content);
    } else {
      globalContext.addStyle(content);
    }
  }
}
