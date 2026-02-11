package dev.jcputney.mjml.component.head;

import dev.jcputney.mjml.component.HeadComponent;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import java.util.regex.Pattern;

/**
 * Adds CSS styles from {@code <mj-style>} content to the rendered HTML.
 * Supports {@code inline="inline"} attribute for styles that should be inlined.
 *
 * <p><strong>Security note:</strong> {@code mj-style} passes CSS content through to the
 * rendered HTML with minimal filtering (only {@code </style>} tag injection is stripped).
 * Untrusted CSS can enable data exfiltration via {@code url()} references or attribute
 * selectors that leak content. When processing untrusted MJML input, configure a
 * {@link dev.jcputney.mjml.ContentSanitizer} to validate or strip dangerous CSS constructs
 * before rendering.</p>
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
      globalContext.styles().addInlineStyle(content);
    } else {
      globalContext.styles().addStyle(content);
    }
  }
}
