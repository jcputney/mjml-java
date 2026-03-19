
package dev.jcputney.mjml.component.body;

import static dev.jcputney.mjml.util.HtmlBuilder.unsortedAttrs;

import dev.jcputney.mjml.component.BaseComponent;
import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.component.ComponentRegistry;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import dev.jcputney.mjml.util.HtmlBuilder;
import java.util.LinkedHashMap;
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

    var divAttrs = buildDivAttributes(title, cssClass, style, lang);

    HtmlBuilder html = new HtmlBuilder();
    html.open("div", unsortedAttrs(divAttrs));

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

  /**
   * Builds a map of attributes for a <div> element with various accessibility, styling, and language properties.
   *
   * @param title the title or description of the div, used as the value for the "aria-label" attribute
   * @param cssClass the class name(s) for the div, used as the value for the "class" attribute
   * @param style the inline style for the div, used as the value for the "style" attribute
   * @param lang the language of the content, used as the value for the "lang" attribute
   * @return a map containing the generated attributes and their corresponding values for the div element
   */
  private LinkedHashMap<String, String> buildDivAttributes(String title, String cssClass, String style,
    String lang) {
    // Body div attrs — non-alphabetical order matches MJML reference
    var divAttrs = new LinkedHashMap<String, String>();
    if (title != null && !title.isEmpty()) {
      divAttrs.put("aria-label", escapeAttr(title));
    }
    divAttrs.put("aria-roledescription", "email");
    if (!cssClass.isEmpty()) {
      divAttrs.put("class", escapeAttr(cssClass));
    }
    divAttrs.put("style", style);
    divAttrs.put("role", "article");
    divAttrs.put("lang", escapeAttr(lang));
    divAttrs.put("dir", "auto");
    return divAttrs;
  }
}
