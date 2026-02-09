package dev.jcputney.mjml.component.interactive;

import dev.jcputney.mjml.component.BaseComponent;
import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.component.ComponentRegistry;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import java.util.List;
import java.util.Map;

/**
 * The social component ({@code <mj-social>}).
 * Renders a row of social media icon elements. Supports two layout modes:
 * <ul>
 *   <li>{@code horizontal} &mdash; all icons inline, each in its own table</li>
 *   <li>{@code vertical} &mdash; all icons stacked in a single table</li>
 * </ul>
 */
public class MjSocial extends BodyComponent {

  private static final Map<String, String> DEFAULTS = Map.ofEntries(
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
      Map.entry("text-padding", "4px 4px 4px 0")
  );

  private final ComponentRegistry registry;

  public MjSocial(MjmlNode node, GlobalContext globalContext, RenderContext renderContext,
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

    StringBuilder sb = new StringBuilder();

    if ("horizontal".equals(mode)) {
      renderHorizontal(sb, elements, align);
    } else {
      renderVertical(sb, elements, align);
    }

    return sb.toString();
  }

  private void renderHorizontal(StringBuilder sb, List<MjmlNode> elements, String align) {
    // MSO opening
    sb.append("<!--[if mso | IE]><table align=\"").append(align)
        .append("\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" ><tr><td><![endif]-->\n");

    for (int i = 0; i < elements.size(); i++) {
      MjmlNode elem = elements.get(i);
      RenderContext childContext = renderContext.withPosition(i, i == 0,
          i == elements.size() - 1);
      BaseComponent component = registry.createComponent(elem, globalContext, childContext);
      if (component instanceof MjSocialElement socialElement) {
        sb.append(socialElement.renderHorizontal(this));
      }

      // MSO separator between elements
      if (i < elements.size() - 1) {
        sb.append("<!--[if mso | IE]></td><td><![endif]-->\n");
      }
    }

    // MSO closing
    sb.append("<!--[if mso | IE]></td></tr></table><![endif]-->\n");
  }

  private void renderVertical(StringBuilder sb, List<MjmlNode> elements, String align) {
    sb.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\"")
        .append(" style=\"margin:0px;\">\n");
    sb.append("<tbody>\n");

    for (int i = 0; i < elements.size(); i++) {
      MjmlNode elem = elements.get(i);
      RenderContext childContext = renderContext.withPosition(i, i == 0,
          i == elements.size() - 1);
      BaseComponent component = registry.createComponent(elem, globalContext, childContext);
      if (component instanceof MjSocialElement socialElement) {
        sb.append(socialElement.renderVertical(this));
      }
    }

    sb.append("</tbody>\n");
    sb.append("</table>\n");
  }
}
