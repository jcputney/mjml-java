package dev.jcputney.mjml.component.interactive;

import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import java.util.Map;

/**
 * The accordion title component ({@code <mj-accordion-title>}).
 * Renders the title bar of an accordion element as a table containing
 * the title text cell and a toggle icon cell.
 */
public class MjAccordionTitle extends BodyComponent {

  private static final Map<String, String> DEFAULTS = Map.ofEntries(
      Map.entry("background-color", ""),
      Map.entry("color", ""),
      Map.entry("font-family", "Ubuntu, Helvetica, Arial, sans-serif"),
      Map.entry("font-size", "13px"),
      Map.entry("font-weight", ""),
      Map.entry("padding", "16px")
  );

  public MjAccordionTitle(MjmlNode node, GlobalContext globalContext,
      RenderContext renderContext) {
    super(node, globalContext, renderContext);
  }

  @Override
  public String getTagName() {
    return "mj-accordion-title";
  }

  @Override
  public Map<String, String> getDefaultAttributes() {
    return DEFAULTS;
  }

  @Override
  public String render() {
    StringBuilder sb = new StringBuilder();

    String backgroundColor = getAttribute("background-color", "");
    String color = getAttribute("color", "");
    String fontFamily = getAttribute("font-family", "Ubuntu, Helvetica, Arial, sans-serif");
    String fontSize = getAttribute("font-size", "13px");
    String padding = getAttribute("padding", "16px");

    // Resolve icon attributes from the accordion ancestor (via full cascade)
    String iconAlign = resolveAncestorAttr("icon-align", "middle");
    String iconWidth = resolveAncestorAttr("icon-width", "32px");
    String iconHeight = resolveAncestorAttr("icon-height", "32px");
    String iconPosition = resolveAncestorAttr("icon-position", "right");
    String iconUnwrappedUrl = resolveAncestorAttr("icon-unwrapped-url",
        "https://i.imgur.com/w4uTygT.png");
    String iconUnwrappedAlt = resolveAncestorAttr("icon-unwrapped-alt", "-");
    String iconWrappedUrl = resolveAncestorAttr("icon-wrapped-url",
        "https://i.imgur.com/bIXv1bk.png");
    String iconWrappedAlt = resolveAncestorAttr("icon-wrapped-alt", "+");

    // Border comes from the accordion (grandparent or higher)
    String border = resolveAncestorAttr("border", "2px solid black");

    // Collapse whitespace in title content
    String content = sanitizeContent(node.getInnerHtml().trim().replaceAll("\\s+", " "));

    sb.append("<table cellspacing=\"0\" cellpadding=\"0\" style=\"");
    sb.append(buildStyle(orderedMap(
        "width", "100%",
        "border-bottom", border
    )));
    sb.append("\">\n");
    sb.append("<tbody>\n");
    sb.append("<tr>\n");

    // Icon on the left if icon-position is "left"
    if ("left".equals(iconPosition)) {
      renderIcon(sb, iconAlign, backgroundColor, iconWidth, iconHeight,
          iconUnwrappedUrl, iconUnwrappedAlt, iconWrappedUrl, iconWrappedAlt);
    }

    // Title cell - note: width:100% is the first property
    sb.append("<td style=\"");
    sb.append(buildStyle(orderedMap(
        "width", "100%",
        "background-color", backgroundColor,
        "color", color,
        "font-size", fontSize,
        "font-family", fontFamily,
        "padding", padding
    )));
    sb.append("\">");
    sb.append(" ").append(content).append(" ");
    sb.append("</td>\n");

    // Icon on the right if icon-position is "right" (default)
    if (!"left".equals(iconPosition)) {
      renderIcon(sb, iconAlign, backgroundColor, iconWidth, iconHeight,
          iconUnwrappedUrl, iconUnwrappedAlt, iconWrappedUrl, iconWrappedAlt);
    }

    sb.append("</tr>\n");
    sb.append("</tbody>\n");
    sb.append("</table>\n");

    return sb.toString();
  }

  private void renderIcon(StringBuilder sb, String align, String backgroundColor,
      String width, String height,
      String unwrappedUrl, String unwrappedAlt,
      String wrappedUrl, String wrappedAlt) {

    // Icon td is wrapped in MSO conditional
    sb.append("<!--[if !mso | IE]><!-->\n");
    sb.append("<td class=\"mj-accordion-ico\" style=\"");
    sb.append(buildStyle(orderedMap(
        "padding", "16px",
        "background", backgroundColor,
        "vertical-align", align
    )));
    sb.append("\">");

    // "more" image (shown when collapsed): uses WRAPPED URL, unwrapped alt
    sb.append("<img src=\"").append(escapeAttr(wrappedUrl)).append("\"");
    sb.append(" alt=\"").append(escapeAttr(unwrappedAlt)).append("\"");
    sb.append(" class=\"mj-accordion-more\"");
    sb.append(" style=\"");
    sb.append(buildStyle(orderedMap(
        "display", "none",
        "width", width,
        "height", height
    )));
    sb.append("\" />");

    // "less" image (shown when expanded): uses UNWRAPPED URL, wrapped alt
    sb.append("<img src=\"").append(escapeAttr(unwrappedUrl)).append("\"");
    sb.append(" alt=\"").append(escapeAttr(wrappedAlt)).append("\"");
    sb.append(" class=\"mj-accordion-less\"");
    sb.append(" style=\"");
    sb.append(buildStyle(orderedMap(
        "display", "none",
        "width", width,
        "height", height
    )));
    sb.append("\" />");

    sb.append("</td>");
    sb.append("<!--<![endif]-->\n");
  }

  private String resolveAncestorAttr(String name, String fallback) {
    return AccordionHelper.resolveAncestorAttr(node, name, globalContext, fallback);
  }
}
