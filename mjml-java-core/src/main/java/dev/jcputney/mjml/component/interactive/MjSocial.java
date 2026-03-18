package dev.jcputney.mjml.component.interactive;

import static dev.jcputney.mjml.util.HtmlBuilder.attrs;

import dev.jcputney.mjml.component.BaseComponent;
import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.component.ComponentRegistry;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import dev.jcputney.mjml.util.HtmlBuilder;
import java.util.List;
import java.util.Map;

/**
 * The social component ({@code <mj-social>}). Renders a row of social media icon elements. Supports
 * two layout modes:
 *
 * <ul>
 *   <li>{@code horizontal} &mdash; all icons inline, each in its own table
 *   <li>{@code vertical} &mdash; all icons stacked in a single table
 * </ul>
 */
public class MjSocial extends BodyComponent {

  private static final Map<String, String> DEFAULTS =
      Map.ofEntries(
          Map.entry("align", "center"),
          Map.entry("border-radius", "3px"),
          Map.entry("color", "#333333"),
          Map.entry("container-background-color", ""),
          Map.entry("font-family", "Ubuntu, Helvetica, Arial, sans-serif"),
          Map.entry("font-size", "13px"),
          Map.entry("font-style", ""),
          Map.entry("font-weight", ""),
          Map.entry("icon-height", ""),
          Map.entry("icon-padding", ""),
          Map.entry("icon-size", "20px"),
          Map.entry("inner-padding", ""),
          Map.entry("line-height", "22px"),
          Map.entry("mode", "horizontal"),
          Map.entry("padding", "10px 25px"),
          Map.entry("text-decoration", "none"),
          Map.entry("text-padding", "4px 4px 4px 0"));

  private final ComponentRegistry registry;

  public MjSocial(
      MjmlNode node,
      GlobalContext globalContext,
      RenderContext renderContext,
      ComponentRegistry registry) {
    super(node, globalContext, renderContext);
    this.registry = registry;
  }

  @Override
  public String getTagName() {
    return "mj-social";
  }

  @Override
  public Map<String, String> getDefaultAttributes() {
    return DEFAULTS;
  }

  @Override
  public String render() {
    String mode = getAttribute("mode", "horizontal");
    String align = getAttribute("align", "center");

    List<MjmlNode> elements = node.getChildrenByTag("mj-social-element");
    if (elements.isEmpty()) {
      return "";
    }

    HtmlBuilder html = new HtmlBuilder();

    if ("horizontal".equals(mode)) {
      renderHorizontal(html, elements, align);
    } else {
      renderVertical(html, elements, align);
    }

    return html.toString();
  }

  private void renderHorizontal(HtmlBuilder html, List<MjmlNode> elements, String align) {
    html.raw(
        "<!--[if mso | IE]><table"
            + attrs(
                "align",
                align,
                "border",
                "0",
                "cellpadding",
                "0",
                "cellspacing",
                "0",
                "role",
                "presentation")
            + " ><tr><td><![endif]-->");

    for (int i = 0; i < elements.size(); i++) {
      MjmlNode elem = elements.get(i);
      RenderContext childContext = renderContext.withPosition(i, i == 0, i == elements.size() - 1);
      BaseComponent component = registry.createComponent(elem, globalContext, childContext);
      if (component instanceof MjSocialElement socialElement) {
        html.rawVerbatim(socialElement.renderHorizontal(this));
      }

      if (i < elements.size() - 1) {
        html.raw("<!--[if mso | IE]></td><td><![endif]-->");
      }
    }

    html.raw("<!--[if mso | IE]></td></tr></table><![endif]-->");
  }

  private void renderVertical(HtmlBuilder html, List<MjmlNode> elements, String align) {
    html.open(
        "table",
        attrs(
            "border",
            "0",
            "cellpadding",
            "0",
            "cellspacing",
            "0",
            "role",
            "presentation",
            "style",
            "margin:0px;"));
    html.open("tbody");

    for (int i = 0; i < elements.size(); i++) {
      MjmlNode elem = elements.get(i);
      RenderContext childContext = renderContext.withPosition(i, i == 0, i == elements.size() - 1);
      BaseComponent component = registry.createComponent(elem, globalContext, childContext);
      if (component instanceof MjSocialElement socialElement) {
        html.rawVerbatim(socialElement.renderVertical(this));
      }
    }

    html.close("tbody");
    html.close("table");
  }
}
