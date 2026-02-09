package dev.jcputney.mjml.component.content;

import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import java.util.Map;

/**
 * The raw component (&lt;mj-raw&gt;).
 * Passes through raw HTML content without any wrapping or transformation.
 * This is a simple pass-through component.
 */
public class MjRaw extends BodyComponent {

  private static final Map<String, String> DEFAULTS = Map.of(
      "position", ""
  );

  public MjRaw(MjmlNode node, GlobalContext globalContext, RenderContext renderContext) {
    super(node, globalContext, renderContext);
  }

  @Override
  public String getTagName() {
    return "mj-raw";
  }

  @Override
  public Map<String, String> getDefaultAttributes() {
    return DEFAULTS;
  }

  @Override
  public String render() {
    String position = getAttribute("position", "");
    if ("file-start".equals(position)) {
      globalContext.addFileStartContent(sanitizeContent(node.getInnerHtml()));
      return "";
    }
    return sanitizeContent(node.getInnerHtml());
  }
}
