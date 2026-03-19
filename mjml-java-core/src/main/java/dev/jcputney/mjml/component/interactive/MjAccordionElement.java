
package dev.jcputney.mjml.component.interactive;

import dev.jcputney.mjml.component.BaseComponent;
import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.component.ComponentRegistry;
import dev.jcputney.mjml.context.AttributeResolver;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import dev.jcputney.mjml.util.HtmlBuilder;
import java.util.Map;
import static dev.jcputney.mjml.util.HtmlBuilder.attrs;

/**
 * A single accordion item ({@code <mj-accordion-element>}). Renders a label with a hidden checkbox,
 * followed by a div containing the title and collapsible content div, all using the CSS checkbox
 * hack.
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
    Map.entry("icon-wrapped-url", ""));

  private final ComponentRegistry registry;

  public MjAccordionElement(
    MjmlNode node, GlobalContext globalContext, RenderContext renderContext, ComponentRegistry registry) {
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
    String fontFamily = getFontFamily();
    String labelStyle = buildStyle(orderedMap("font-size", "13px", "font-family", fontFamily));

    HtmlBuilder html = new HtmlBuilder();
    html.wrap("tr",
      () -> html.wrap("td", attrs("style", "padding:0px;"),
        () -> html.wrap("label", attrs("class", "mj-accordion-element", "style", labelStyle), () -> {
          // Hidden checkbox — only rendered in non-MSO/IE clients
          html.notMsoIE(() -> html.selfClose("input",
            attrs("class", "mj-accordion-checkbox", "type", "checkbox", "style", "display:none;")));

          html.wrap("div", () -> {
            html.wrap("div", attrs("class", "mj-accordion-title"),
              () -> renderTitleChild(html));
            html.wrap("div", attrs("class", "mj-accordion-content"),
              () -> renderTextChild(html));
          });
        })));

    return html.toString();
  }

  /**
   * Retrieves the font family to be used for this element. The method attempts to
   * resolve the font family attribute by first checking the element's own cascade,
   * then falling back to its parent's attributes within the cascade if not found.
   * If neither source provides a value, a default font family is returned.
   *
   * @return the resolved font family as a string, or the default font family
   *         "Ubuntu, Helvetica, Arial, sans-serif" if not explicitly set.
   */
  String getFontFamily() {
    // First try this element's own cascade
    String value = getAttribute("font-family", "");
    if (!value.isEmpty()) {
      return value;
    }
    // Try parent node attributes via the full cascade
    MjmlNode parent = node.getParent();
    if (parent != null) {
      String parentVal = AttributeResolver.resolve(parent, "font-family", globalContext, MjAccordion.DEFAULTS);
      if (parentVal != null && !parentVal.isEmpty()) {
        return parentVal;
      }
    }
    return "Ubuntu, Helvetica, Arial, sans-serif";
  }

  /**
   * Renders the child element with the tag "mj-accordion-title" as a body component
   * if it exists within the current node. The rendered output is added to the
   * provided {@code HtmlBuilder} instance. This function ensures the correct
   * context is passed to the child components during rendering.
   *
   * @param html the {@code HtmlBuilder} instance used to append the rendered output
   */
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

  /**
   * Renders the child element with the tag "mj-accordion-text" as a body component
   * if it exists within the current node. The rendered output is added to the
   * provided {@code HtmlBuilder} instance. This function ensures the correct
   * context is passed to the child components during rendering.
   *
   * @param html the {@code HtmlBuilder} instance used to append the rendered output
   */
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
