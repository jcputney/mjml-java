package dev.jcputney.mjml.component.interactive;

import static dev.jcputney.mjml.util.HtmlBuilder.attrs;

import dev.jcputney.mjml.component.BaseComponent;
import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.component.ComponentRegistry;
import dev.jcputney.mjml.context.AttributeResolver;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import dev.jcputney.mjml.util.HtmlBuilder;
import java.util.Map;

/**
 * A single accordion item ({@code <mj-accordion-element>}). Renders a label with a hidden checkbox,
 * followed by a div containing the title and collapsible content div, all using the CSS checkbox
 * hack.
 */
public class MjAccordionElement extends BodyComponent {

  static final Map<String, String> DEFAULTS =
      Map.ofEntries(
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
          Map.entry("icon-wrapped-url", ""));

  private final ComponentRegistry registry;

  public MjAccordionElement(
      MjmlNode node,
      GlobalContext globalContext,
      RenderContext renderContext,
      ComponentRegistry registry) {
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
    String fontFamily = resolveAttr("font-family", "Ubuntu, Helvetica, Arial, sans-serif");
    String labelStyle = buildStyle(orderedMap("font-size", "13px", "font-family", fontFamily));

    HtmlBuilder html = new HtmlBuilder();

    // Each accordion element is a <tr><td> wrapping a <label>
    html.open("tr");
    html.open("td", attrs("style", "padding:0px;"));

    // Label with class
    html.open("label", attrs("class", "mj-accordion-element", "style", labelStyle));

    // Hidden checkbox wrapped in MSO conditional
    html.rawVerbatim("<!--[if !mso | IE]><!-->");
    html.rawVerbatim(
        "<input class=\"mj-accordion-checkbox\" type=\"checkbox\" style=\"display:none;\" />");
    html.rawVerbatim("<!--<![endif]-->\n");

    // Wrapper div containing title and content
    html.open("div");

    // Title child
    html.open("div", attrs("class", "mj-accordion-title"));
    renderTitleChild(html);
    html.close("div");

    // Content child
    html.open("div", attrs("class", "mj-accordion-content"));
    renderTextChild(html);
    html.close("div");

    html.close("div");

    html.close("label");
    html.close("td");
    html.close("tr");

    return html.toString();
  }

  /**
   * Resolves an attribute by first checking this element (via the full cascade), then falling back
   * to the parent accordion's cascaded attribute or the provided default.
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
      String parentVal =
          AttributeResolver.resolve(parent, name, globalContext, MjAccordion.DEFAULTS);
      if (parentVal != null && !parentVal.isEmpty()) {
        return parentVal;
      }
    }
    return fallback;
  }

  private void renderTitleChild(HtmlBuilder html) {
    MjmlNode titleNode = node.getFirstChildByTag("mj-accordion-title");
    if (titleNode != null) {
      RenderContext childContext = renderContext.withPosition(0, true, true);
      BaseComponent component = registry.createComponent(titleNode, globalContext, childContext);
      if (component instanceof BodyComponent bodyComponent) {
        html.rawVerbatim(bodyComponent.render());
      }
    }
  }

  private void renderTextChild(HtmlBuilder html) {
    MjmlNode textNode = node.getFirstChildByTag("mj-accordion-text");
    if (textNode != null) {
      RenderContext childContext = renderContext.withPosition(1, false, true);
      BaseComponent component = registry.createComponent(textNode, globalContext, childContext);
      if (component instanceof BodyComponent bodyComponent) {
        html.rawVerbatim(bodyComponent.render());
      }
    }
  }
}
