package dev.jcputney.javamjml.component.interactive;

import dev.jcputney.javamjml.component.BaseComponent;
import dev.jcputney.javamjml.component.BodyComponent;
import dev.jcputney.javamjml.component.ComponentRegistry;
import dev.jcputney.javamjml.context.AttributeResolver;
import dev.jcputney.javamjml.context.GlobalContext;
import dev.jcputney.javamjml.context.RenderContext;
import dev.jcputney.javamjml.parser.MjmlNode;
import java.util.Map;

/**
 * A single accordion item ({@code <mj-accordion-element>}).
 * Renders a label with a hidden checkbox, followed by a div containing the title
 * and collapsible content div, all using the CSS checkbox hack.
 */
public class MjAccordionElement extends BodyComponent {

  static final Map<String, String> DEFAULTS = Map.ofEntries(
      Map.entry("background-color", ""),
      Map.entry("border", ""),
      Map.entry("css-class", ""),
      Map.entry("font-family", ""),
      Map.entry("icon-align", ""),
      Map.entry("icon-color", ""),
      Map.entry("icon-height", ""),
      Map.entry("icon-position", ""),
      Map.entry("icon-unwrapped-alt", ""),
      Map.entry("icon-unwrapped-url", ""),
      Map.entry("icon-width", ""),
      Map.entry("icon-wrapped-alt", ""),
      Map.entry("icon-wrapped-url", "")
  );

  private final ComponentRegistry registry;

  public MjAccordionElement(MjmlNode node, GlobalContext globalContext,
      RenderContext renderContext, ComponentRegistry registry) {
    super(node, globalContext, renderContext);
    this.registry = registry;
  }

  @Override
  public String getTagName() {
    return "mj-accordion-element";
  }

  @Override
  public Map<String, String> getDefaultAttributes() {
    return DEFAULTS;
  }

  @Override
  public String render() {
    StringBuilder sb = new StringBuilder();

    String fontFamily = resolveAttr("font-family", "Ubuntu, Helvetica, Arial, sans-serif");

    // Each accordion element is a <tr><td> wrapping a <label>
    sb.append("<tr>\n");
    sb.append("<td style=\"padding:0px;\">\n");

    // Label with class
    sb.append("<label class=\"mj-accordion-element\"");
    sb.append(" style=\"");
    sb.append(buildStyle(orderedMap(
        "font-size", "13px",
        "font-family", fontFamily
    )));
    sb.append("\">\n");

    // Hidden checkbox wrapped in MSO conditional
    sb.append("<!--[if !mso | IE]><!-->");
    sb.append("<input class=\"mj-accordion-checkbox\" type=\"checkbox\" style=\"display:none;\" />");
    sb.append("<!--<![endif]-->\n");

    // Wrapper div containing title and content
    sb.append("<div>\n");

    // Title child
    sb.append("<div class=\"mj-accordion-title\">\n");
    renderTitleChild(sb);
    sb.append("</div>\n");

    // Content child
    sb.append("<div class=\"mj-accordion-content\">\n");
    renderTextChild(sb);
    sb.append("</div>\n");

    sb.append("</div>\n");

    sb.append("</label>\n");
    sb.append("</td>\n");
    sb.append("</tr>\n");

    return sb.toString();
  }

  /**
   * Resolves an attribute by first checking this element (via the full cascade),
   * then falling back to the parent accordion's cascaded attribute or the provided default.
   */
  String resolveAttr(String name, String fallback) {
    // First try this element's own cascade
    String value = getAttribute(name, "");
    if (!value.isEmpty()) {
      return value;
    }
    // Try parent node attributes via the full cascade
    MjmlNode parent = node.getParent();
    if (parent != null) {
      String parentVal = AttributeResolver.resolve(parent, name, globalContext,
          MjAccordion.DEFAULTS);
      if (parentVal != null && !parentVal.isEmpty()) {
        return parentVal;
      }
    }
    return fallback;
  }

  private void renderTitleChild(StringBuilder sb) {
    MjmlNode titleNode = node.getFirstChildByTag("mj-accordion-title");
    if (titleNode != null) {
      RenderContext childContext = renderContext.withPosition(0, true, true);
      BaseComponent component = registry.createComponent(titleNode, globalContext, childContext);
      if (component instanceof BodyComponent bodyComponent) {
        sb.append(bodyComponent.render());
      }
    }
  }

  private void renderTextChild(StringBuilder sb) {
    MjmlNode textNode = node.getFirstChildByTag("mj-accordion-text");
    if (textNode != null) {
      RenderContext childContext = renderContext.withPosition(1, false, true);
      BaseComponent component = registry.createComponent(textNode, globalContext, childContext);
      if (component instanceof BodyComponent bodyComponent) {
        sb.append(bodyComponent.render());
      }
    }
  }
}
