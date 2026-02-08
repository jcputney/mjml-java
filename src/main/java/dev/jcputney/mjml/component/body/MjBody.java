package dev.jcputney.mjml.component.body;

import dev.jcputney.mjml.component.BaseComponent;
import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.component.ComponentRegistry;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import java.util.Map;

/**
 * The root body component (&lt;mj-body&gt;).
 * Sets the overall container width and background color,
 * then renders all child sections.
 */
public class MjBody extends BodyComponent {

  private static final Map<String, String> DEFAULTS = Map.of(
      "width", "600px"
  );

  private final ComponentRegistry registry;

  public MjBody(MjmlNode node, GlobalContext globalContext, RenderContext renderContext,
      ComponentRegistry registry) {
    super(node, globalContext, renderContext);
    this.registry = registry;
  }

  @Override
  public String getTagName() {
    return "mj-body";
  }

  @Override
  public Map<String, String> getDefaultAttributes() {
    return DEFAULTS;
  }

  @Override
  public String render() {
    String bgColor = getAttribute("background-color", "");
    String width = getAttribute("width", "600px");

    // Update container width in global context
    int containerWidth = (int) parseWidth(width);
    globalContext.setContainerWidth(containerWidth);

    // Store body background color for the <body> tag in HtmlSkeleton
    if (!bgColor.isEmpty()) {
      globalContext.setBodyBackgroundColor(bgColor);
    }

    StringBuilder sb = new StringBuilder();

    // Wrapper div with ARIA attributes
    String lang = globalContext.getConfiguration().getLanguage();
    if (lang == null || lang.isEmpty()) {
      lang = "und";
    }

    sb.append("  <div");

    // aria-label from mj-title if set
    String title = globalContext.getTitle();
    if (title != null && !title.isEmpty()) {
      sb.append(" aria-label=\"").append(title).append("\"");
    }

    sb.append(" aria-roledescription=\"email\"");

    // style attribute (always emit, even if empty)
    String style = "";
    if (!bgColor.isEmpty()) {
      style = "background-color:" + bgColor + ";";
    }
    sb.append(" style=\"").append(style).append("\"");

    sb.append(" role=\"article\"");
    sb.append(" lang=\"").append(lang).append("\"");
    sb.append(" dir=\"auto\"");

    String cssClass = getAttribute("css-class", "");
    if (!cssClass.isEmpty()) {
      sb.append(" class=\"").append(cssClass).append("\"");
    }
    sb.append(">\n");

    // Render children with the body's container width
    RenderContext bodyContext = new RenderContext(containerWidth);
    var children = node.getChildren();
    for (int i = 0; i < children.size(); i++) {
      MjmlNode child = children.get(i);
      if (child.getTagName().startsWith("#")) {
        // Check if it's a comment node (#comment) and output it
        if ("#comment".equals(child.getTagName())) {
          sb.append("    <!-- ").append(child.getTextContent().trim()).append(" -->\n");
        }
        continue;
      }
      RenderContext childContext = bodyContext.withPosition(i, i == 0, i == children.size() - 1);
      BaseComponent component = registry.createComponent(child, globalContext, childContext);
      if (component instanceof BodyComponent bodyComponent) {
        sb.append(bodyComponent.render());
      }
    }

    sb.append("  </div>\n");

    // Merge adjacent MSO section transitions: when a section MSO close is
    // immediately followed by a section MSO open (no content between),
    // merge them onto a single conditional line.
    return mergeMsoSectionTransitions(sb.toString());
  }

  /**
   * Merges adjacent MSO conditional section transitions in the rendered body HTML.
   *
   * <p>Each mj-section renders its own self-contained MSO table wrapper:
   * {@code <!--[if mso | IE]><table ...>...<![endif]-->}. When two sections
   * are adjacent, this produces a close-then-open pattern:
   * {@code ...<![endif]-->} followed by {@code <!--[if mso | IE]><table ...>}.
   *
   * <p>The official MJML renderer merges these into a single conditional block
   * by removing the intermediate {@code <![endif]-->...<!--[if mso | IE]>},
   * producing: {@code ...</td></tr></table><table ...>}. This keeps adjacent
   * sections inside a single MSO conditional, which is semantically equivalent
   * but produces cleaner output. Both VML and non-VML transitions are handled.
   */
  private static String mergeMsoSectionTransitions(String html) {
    // Merge simple section transitions (no VML background)
    html = html.replace(
        "<!--[if mso | IE]></td></tr></table><![endif]-->\n    <!--[if mso | IE]><table ",
        "<!--[if mso | IE]></td></tr></table><table "
    );
    // Merge VML background section transitions (section ends with v:rect close)
    html = html.replace(
        "<!--[if mso | IE]></v:textbox></v:rect></td></tr></table><![endif]-->\n    <!--[if mso | IE]><table ",
        "<!--[if mso | IE]></v:textbox></v:rect></td></tr></table><table "
    );
    return html;
  }
}
