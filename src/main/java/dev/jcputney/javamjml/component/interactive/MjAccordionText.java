package dev.jcputney.javamjml.component.interactive;

import dev.jcputney.javamjml.component.BodyComponent;
import dev.jcputney.javamjml.context.AttributeResolver;
import dev.jcputney.javamjml.context.GlobalContext;
import dev.jcputney.javamjml.context.RenderContext;
import dev.jcputney.javamjml.parser.MjmlNode;
import java.util.Map;

/**
 * The accordion text component ({@code <mj-accordion-text>}).
 * Renders the collapsible content body of an accordion element as a table
 * with configurable text styling.
 */
public class MjAccordionText extends BodyComponent {

  private static final Map<String, String> DEFAULTS = Map.ofEntries(
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
      Map.entry("padding-top", "")
  );

  public MjAccordionText(MjmlNode node, GlobalContext globalContext,
      RenderContext renderContext) {
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
    String content = node.getInnerHtml().trim().replaceAll("\\s+", " ");

    sb.append("<table cellspacing=\"0\" cellpadding=\"0\" style=\"");
    sb.append(buildStyle(orderedMap(
        "width", "100%",
        "border-bottom", border
    )));
    sb.append("\">\n");
    sb.append("<tbody>\n");
    sb.append("<tr>\n");

    // TD style uses "background:" (short form), not "background-color:"
    sb.append("<td style=\"");
    sb.append(buildStyle(orderedMap(
        "background", backgroundColor,
        "font-size", fontSize,
        "font-family", fontFamily,
        "line-height", lineHeight,
        "color", color,
        "padding", padding
    )));
    sb.append("\">");
    sb.append(" ").append(content).append(" ");
    sb.append("</td>\n");

    sb.append("</tr>\n");
    sb.append("</tbody>\n");
    sb.append("</table>\n");

    return sb.toString();
  }

  /**
   * Resolves an attribute by walking up the node tree to find a value
   * set on an ancestor (typically the mj-accordion-element or mj-accordion),
   * using the full attribute cascade (inline, mj-class, tag defaults, mj-all).
   */
  private String resolveAncestorAttr(String name, String fallback) {
    MjmlNode current = node.getParent();
    while (current != null) {
      Map<String, String> defaults = getDefaultsForTag(current.getTagName());
      String value = AttributeResolver.resolve(current, name, globalContext, defaults);
      if (value != null && !value.isEmpty()) {
        return value;
      }
      current = current.getParent();
    }
    return fallback;
  }

  /**
   * Returns the appropriate default attributes map for a given tag name.
   */
  private static Map<String, String> getDefaultsForTag(String tagName) {
    if ("mj-accordion".equals(tagName)) {
      return MjAccordion.DEFAULTS;
    }
    if ("mj-accordion-element".equals(tagName)) {
      return MjAccordionElement.DEFAULTS;
    }
    return Map.of();
  }
}
