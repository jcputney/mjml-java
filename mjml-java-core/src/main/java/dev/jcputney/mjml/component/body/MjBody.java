
package dev.jcputney.mjml.component.body;

import static dev.jcputney.mjml.util.HtmlBuilder.attrIf;
import static dev.jcputney.mjml.util.HtmlBuilder.attrs;

import dev.jcputney.mjml.component.BaseComponent;
import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.component.ComponentRegistry;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import dev.jcputney.mjml.util.HtmlBuilder;
import java.util.Map;

/**
 * The root body component (&lt;mj-body&gt;). Sets the overall container width and background color,
 * then renders all child sections.
 */
public class MjBody extends BodyComponent {

  private static final Map<String, String> DEFAULTS = Map.of("width", "600px");

  private final ComponentRegistry registry;

  public MjBody(MjmlNode node, GlobalContext globalContext, RenderContext renderContext, ComponentRegistry registry) {
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
    globalContext.metadata().setContainerWidth(containerWidth);

    // Store body background color for the <body> tag in HtmlSkeleton
    if (!bgColor.isEmpty()) {
      globalContext.metadata().setBodyBackgroundColor(bgColor);
    }

    String lang = globalContext.getConfiguration().getLanguage();
    if (lang == null || lang.isEmpty()) {
      lang = "und";
    }

    String title = globalContext.metadata().getTitle();
    String cssClass = getAttribute("css-class", "");
    String style = bgColor.isEmpty() ? "" : "background-color:" + escapeAttr(bgColor) + ";";

    HtmlBuilder html = new HtmlBuilder();
    // style="" must always be present even when empty
    html.open(
      "div",
      attrIf("aria-label", title != null && !title.isEmpty() ? escapeAttr(title) : null)
        + attrs("aria-roledescription", "email")
        + attrIf("class", escapeAttr(cssClass))
        + " style=\""
        + style
        + "\""
        + attrs("role", "article", "lang", escapeAttr(lang), "dir", "auto"));

    RenderContext bodyContext = new RenderContext(containerWidth);
    var children = node.getChildren();
    for (int i = 0; i < children.size(); i++) {
      MjmlNode child = children.get(i);
      if (child.getTagName().startsWith("#")) {
        if ("#comment".equals(child.getTagName())) {
          html.line("<!-- " + child.getTextContent().trim().replace("--", "") + " -->");
        }
        continue;
      }
      RenderContext childContext = bodyContext.withPosition(i, i == 0, i == children.size() - 1);
      BaseComponent component = registry.createComponent(child, globalContext, childContext);
      if (component instanceof BodyComponent bodyComponent) {
        html.rawVerbatim(bodyComponent.render());
      }
    }

    html.close("div");

    return html.toString();
  }
}
