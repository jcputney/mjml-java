package dev.jcputney.mjml.component.content;

import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import java.util.Map;

/**
 * The raw component ({@code <mj-raw>}).
 * Passes through raw HTML content without any wrapping or transformation.
 *
 * <p><strong>Security note:</strong> {@code mj-raw} passes arbitrary HTML directly into the
 * rendered output, including when {@code position="file-start"} is used to inject content
 * before the DOCTYPE. This makes it an XSS vector when processing untrusted MJML input.
 * Configure a {@link dev.jcputney.mjml.ContentSanitizer} to sanitize the inner HTML before
 * it reaches the final output when handling user-supplied MJML.</p>
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
      globalContext.metadata().addFileStartContent(sanitizeContent(node.getInnerHtml()));
      return "";
    }
    return sanitizeContent(node.getInnerHtml());
  }
}
