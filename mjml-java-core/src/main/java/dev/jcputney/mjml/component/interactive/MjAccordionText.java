package dev.jcputney.mjml.component.interactive;

import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import java.util.Map;

/**
 * The accordion text component ({@code <mj-accordion-text>}). Renders the collapsible content body
 * of an accordion element as a table with configurable text styling.
 */
public class MjAccordionText extends BodyComponent {

  private static final Map<String, String> DEFAULTS =
      Map.ofEntries(
          Map.entry("background-color", ""),
          Map.entry("color", "#000000"),
          Map.entry("font-family", "Ubuntu, Helvetica, Arial, sans-serif"),
          Map.entry("font-size", "13px"),
          Map.entry("font-weight", ""),
          Map.entry("letter-spacing", ""),
          Map.entry("line-height", "1"),
          Map.entry("padding", "16px"),
          Map.entry("padding-bottom", ""),
          Map.entry("padding-left", ""),
          Map.entry("padding-right", ""),
          Map.entry("padding-top", ""));

  /**
   * Creates a new MjAccordionText component.
   *
   * @param node the parsed MJML node for this component
   * @param globalContext the global rendering context
   * @param renderContext the current render context
   */
  public MjAccordionText(MjmlNode node, GlobalContext globalContext, RenderContext renderContext) {
    super(node, globalContext, renderContext);
  }

  @Override
  public String getTagName() {
    return "mj-accordion-text";
  }

  @Override
  public Map<String, String> getDefaultAttributes() {
    return DEFAULTS;
  }

  @Override
  public String render() {
    StringBuilder sb = new StringBuilder();

    String backgroundColor = getAttribute("background-color", "");
    String color = getAttribute("color", "#000000");
    String fontFamily = getAttribute("font-family", "Ubuntu, Helvetica, Arial, sans-serif");
    String fontSize = getAttribute("font-size", "13px");
    String lineHeight = getAttribute("line-height", "1");
    String padding = getAttribute("padding", "16px");

    // Border comes from the accordion ancestor (via full cascade)
    String border = resolveAncestorAttr("border", "2px solid black");

    // Collapse whitespace in text content
    String content = sanitizeContent(node.getInnerHtml().trim().replaceAll("\\s+", " "));

    sb.append("<table cellspacing=\"0\" cellpadding=\"0\" style=\"");
    sb.append(buildStyle(orderedMap("width", "100%", "border-bottom", border)));
    sb.append("\">\n");
    sb.append("<tbody>\n");
    sb.append("<tr>\n");

    // TD style uses "background:" (short form), not "background-color:"
    sb.append("<td style=\"");
    sb.append(
        buildStyle(
            orderedMap(
                "background", backgroundColor,
                "font-size", fontSize,
                "font-family", fontFamily,
                "line-height", lineHeight,
                "color", color,
                "padding", padding)));
    sb.append("\">");
    sb.append(" ").append(content).append(" ");
    sb.append("</td>\n");

    sb.append("</tr>\n");
    sb.append("</tbody>\n");
    sb.append("</table>\n");

    return sb.toString();
  }

  private String resolveAncestorAttr(String name, String fallback) {
    return AccordionHelper.resolveAncestorAttr(node, name, globalContext, fallback);
  }
}
