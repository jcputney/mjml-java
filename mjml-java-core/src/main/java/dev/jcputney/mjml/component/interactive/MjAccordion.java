package dev.jcputney.mjml.component.interactive;

import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.component.ComponentRegistry;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import java.util.Map;

/**
 * The accordion component ({@code <mj-accordion>}). Uses a CSS checkbox hack to create
 * expand/collapse sections without JavaScript. Wraps child {@code mj-accordion-element} components
 * in a table and injects the required CSS into the global context.
 */
public class MjAccordion extends BodyComponent {

  static final Map<String, String> DEFAULTS =
      Map.ofEntries(
          Map.entry("border", "2px solid black"),
          Map.entry("container-background-color", ""),
          Map.entry("font-family", "Ubuntu, Helvetica, Arial, sans-serif"),
          Map.entry("icon-align", "middle"),
          Map.entry("icon-color", "#000000"),
          Map.entry("icon-height", "32px"),
          Map.entry("icon-position", "right"),
          Map.entry("icon-unwrapped-alt", "-"),
          Map.entry("icon-unwrapped-url", "https://i.imgur.com/w4uTygT.png"),
          Map.entry("icon-width", "32px"),
          Map.entry("icon-wrapped-alt", "+"),
          Map.entry("icon-wrapped-url", "https://i.imgur.com/bIXv1bk.png"),
          Map.entry("padding", "10px 25px"));

  private static final String ACCORDION_CSS =
      """
      noinput.mj-accordion-checkbox {
        display: block !important;
      }

      @media yahoo,
      only screen and (min-width:0) {
        .mj-accordion-element {
          display: block;
        }

        input.mj-accordion-checkbox,
        .mj-accordion-less {
          display: none !important;
        }

        input.mj-accordion-checkbox+* .mj-accordion-title {
          cursor: pointer;
          touch-action: manipulation;
          -webkit-user-select: none;
          -moz-user-select: none;
          user-select: none;
        }

        input.mj-accordion-checkbox+* .mj-accordion-content {
          overflow: hidden;
          display: none;
        }

        input.mj-accordion-checkbox+* .mj-accordion-more {
          display: block !important;
        }

        input.mj-accordion-checkbox:checked+* .mj-accordion-content {
          display: block;
        }

        input.mj-accordion-checkbox:checked+* .mj-accordion-more {
          display: none !important;
        }

        input.mj-accordion-checkbox:checked+* .mj-accordion-less {
          display: block !important;
        }
      }

      .moz-text-html input.mj-accordion-checkbox+* .mj-accordion-title {
        cursor: auto;
        touch-action: auto;
        -webkit-user-select: auto;
        -moz-user-select: auto;
        user-select: auto;
      }

      .moz-text-html input.mj-accordion-checkbox+* .mj-accordion-content {
        overflow: hidden;
        display: block;
      }

      .moz-text-html input.mj-accordion-checkbox+* .mj-accordion-ico {
        display: none;
      }

      @goodbye {
        @gmail
      }
      """;

  private final ComponentRegistry registry;

  /**
   * Creates a new MjAccordion component.
   *
   * @param node the parsed MJML node for this component
   * @param globalContext the global rendering context
   * @param renderContext the current render context
   * @param registry the component registry for creating child components
   */
  public MjAccordion(
      MjmlNode node,
      GlobalContext globalContext,
      RenderContext renderContext,
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
    globalContext.styles().addStyleOnce("mj-accordion", ACCORDION_CSS);

    StringBuilder sb = new StringBuilder();

    String border = getAttribute("border", "2px solid black");
    String fontFamily = getAttribute("font-family", "Ubuntu, Helvetica, Arial, sans-serif");

    sb.append("<table cellspacing=\"0\" cellpadding=\"0\" class=\"mj-accordion\"");
    sb.append(" style=\"");
    sb.append(
        buildStyle(
            orderedMap(
                "width", "100%",
                "border-collapse", "collapse",
                "border", border,
                "border-bottom", "none",
                "font-family", fontFamily)));
    sb.append("\">\n");
    sb.append("<tbody>\n");

    // Render accordion element children
    sb.append(renderChildren(registry));

    sb.append("</tbody>\n");
    sb.append("</table>\n");

    return sb.toString();
  }
}
