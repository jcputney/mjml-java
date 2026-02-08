package dev.jcputney.javamjml.component.interactive;

import dev.jcputney.javamjml.component.BodyComponent;
import dev.jcputney.javamjml.component.ComponentRegistry;
import dev.jcputney.javamjml.context.GlobalContext;
import dev.jcputney.javamjml.context.RenderContext;
import dev.jcputney.javamjml.parser.MjmlNode;
import java.util.Map;

/**
 * The accordion component ({@code <mj-accordion>}).
 * Uses a CSS checkbox hack to create expand/collapse sections without JavaScript.
 * Wraps child {@code mj-accordion-element} components in a table and injects the
 * required CSS into the global context.
 */
public class MjAccordion extends BodyComponent {

  static final Map<String, String> DEFAULTS = Map.ofEntries(
      Map.entry("border", "2px solid black"),
      Map.entry("font-family", "Ubuntu, Helvetica, Arial, sans-serif"),
      Map.entry("icon-align", "middle"),
      Map.entry("icon-color", "#000000"),
      Map.entry("icon-height", "32px"),
      Map.entry("icon-position", "right"),
      Map.entry("icon-unwrapped-alt", "+"),
      Map.entry("icon-unwrapped-url", "https://i.imgur.com/w4uTygT.png"),
      Map.entry("icon-width", "32px"),
      Map.entry("icon-wrapped-alt", "-"),
      Map.entry("icon-wrapped-url", "https://i.imgur.com/bIXv1bk.png"),
      Map.entry("padding", "10px 25px")
  );

  static final String ACCORDION_CSS =
      "noinput.mj-accordion-checkbox {\n"
      + "  display: block !important;\n"
      + "}\n"
      + "\n"
      + "@media yahoo,\n"
      + "only screen and (min-width:0) {\n"
      + "  .mj-accordion-element {\n"
      + "    display: block;\n"
      + "  }\n"
      + "\n"
      + "  input.mj-accordion-checkbox,\n"
      + "  .mj-accordion-less {\n"
      + "    display: none !important;\n"
      + "  }\n"
      + "\n"
      + "  input.mj-accordion-checkbox+* .mj-accordion-title {\n"
      + "    cursor: pointer;\n"
      + "    touch-action: manipulation;\n"
      + "    -webkit-user-select: none;\n"
      + "    -moz-user-select: none;\n"
      + "    user-select: none;\n"
      + "  }\n"
      + "\n"
      + "  input.mj-accordion-checkbox+* .mj-accordion-content {\n"
      + "    overflow: hidden;\n"
      + "    display: none;\n"
      + "  }\n"
      + "\n"
      + "  input.mj-accordion-checkbox+* .mj-accordion-more {\n"
      + "    display: block !important;\n"
      + "  }\n"
      + "\n"
      + "  input.mj-accordion-checkbox:checked+* .mj-accordion-content {\n"
      + "    display: block;\n"
      + "  }\n"
      + "\n"
      + "  input.mj-accordion-checkbox:checked+* .mj-accordion-more {\n"
      + "    display: none !important;\n"
      + "  }\n"
      + "\n"
      + "  input.mj-accordion-checkbox:checked+* .mj-accordion-less {\n"
      + "    display: block !important;\n"
      + "  }\n"
      + "}\n"
      + "\n"
      + ".moz-text-html input.mj-accordion-checkbox+* .mj-accordion-title {\n"
      + "  cursor: auto;\n"
      + "  touch-action: auto;\n"
      + "  -webkit-user-select: auto;\n"
      + "  -moz-user-select: auto;\n"
      + "  user-select: auto;\n"
      + "}\n"
      + "\n"
      + ".moz-text-html input.mj-accordion-checkbox+* .mj-accordion-content {\n"
      + "  overflow: hidden;\n"
      + "  display: block;\n"
      + "}\n"
      + "\n"
      + ".moz-text-html input.mj-accordion-checkbox+* .mj-accordion-ico {\n"
      + "  display: none;\n"
      + "}\n"
      + "\n"
      + "@goodbye {\n"
      + "  @gmail\n"
      + "}\n";

  private final ComponentRegistry registry;

  public MjAccordion(MjmlNode node, GlobalContext globalContext, RenderContext renderContext,
      ComponentRegistry registry) {
    super(node, globalContext, renderContext);
    this.registry = registry;
  }

  @Override
  public String getTagName() {
    return "mj-accordion";
  }

  @Override
  public Map<String, String> getDefaultAttributes() {
    return DEFAULTS;
  }

  @Override
  public String render() {
    // Inject accordion CSS into global styles (only once, even with multiple accordions)
    if (!globalContext.getStyles().contains(ACCORDION_CSS)) {
      globalContext.addStyle(ACCORDION_CSS);
    }

    StringBuilder sb = new StringBuilder();

    String border = getAttribute("border", "2px solid black");
    String fontFamily = getAttribute("font-family", "Ubuntu, Helvetica, Arial, sans-serif");

    sb.append("<table cellspacing=\"0\" cellpadding=\"0\" class=\"mj-accordion\"");
    sb.append(" style=\"");
    sb.append(buildStyle(orderedMap(
        "width", "100%",
        "border-collapse", "collapse",
        "border", border,
        "border-bottom", "none",
        "font-family", fontFamily
    )));
    sb.append("\">\n");
    sb.append("<tbody>\n");

    // Render accordion element children
    sb.append(renderChildren(registry));

    sb.append("</tbody>\n");
    sb.append("</table>\n");

    return sb.toString();
  }
}
