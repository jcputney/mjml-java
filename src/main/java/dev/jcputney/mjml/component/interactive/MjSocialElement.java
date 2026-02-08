package dev.jcputney.mjml.component.interactive;

import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import dev.jcputney.mjml.util.SocialNetworkRegistry;
import dev.jcputney.mjml.util.SocialNetworkRegistry.NetworkInfo;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A single social element ({@code <mj-social-element>}).
 * Renders a table containing a social icon image and an optional text label.
 * When used in horizontal mode, each element is wrapped in its own inline table.
 * When used in vertical mode, each element is a row in the parent's shared table.
 */
public class MjSocialElement extends BodyComponent {

  private static final Map<String, String> DEFAULTS = Map.ofEntries(
      Map.entry("align", "center"),
      Map.entry("alt", ""),
      Map.entry("background-color", ""),
      Map.entry("border-radius", "3px"),
      Map.entry("color", "#333333"),
      Map.entry("css-class", ""),
      Map.entry("font-family", "Ubuntu, Helvetica, Arial, sans-serif"),
      Map.entry("font-size", "13px"),
      Map.entry("font-style", "normal"),
      Map.entry("font-weight", "normal"),
      Map.entry("href", ""),
      Map.entry("icon-height", ""),
      Map.entry("icon-padding", "0px 5px"),
      Map.entry("icon-size", "20px"),
      Map.entry("inner-padding", "4px 4px"),
      Map.entry("line-height", "22px"),
      Map.entry("name", ""),
      Map.entry("padding", "4px 4px"),
      Map.entry("padding-bottom", ""),
      Map.entry("padding-left", ""),
      Map.entry("padding-right", ""),
      Map.entry("padding-top", ""),
      Map.entry("sizes", ""),
      Map.entry("src", ""),
      Map.entry("srcset", ""),
      Map.entry("target", "_blank"),
      Map.entry("text-decoration", "none"),
      Map.entry("text-padding", "4px 4px 4px 0"),
      Map.entry("vertical-align", "middle")
  );

  public MjSocialElement(MjmlNode node, GlobalContext globalContext,
      RenderContext renderContext) {
    super(node, globalContext, renderContext);
  }

  @Override
  public String getTagName() {
    return "mj-social-element";
  }

  @Override
  public Map<String, String> getDefaultAttributes() {
    return DEFAULTS;
  }

  @Override
  public String render() {
    // Default render delegates to horizontal mode with no parent
    return renderHorizontal(null);
  }

  /**
   * Renders this element in horizontal mode (each element in its own inline table).
   */
  public String renderHorizontal(MjSocial parent) {
    StringBuilder sb = new StringBuilder();
    String align = getInheritedAttribute(parent, "align", "center");

    sb.append("<table align=\"").append(align)
        .append("\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\"")
        .append(" style=\"float:none;display:inline-table;\">\n");
    sb.append("<tbody>\n");
    sb.append("<tr>\n");

    appendIconAndTextCells(sb, parent);

    sb.append("</tr>\n");
    sb.append("</tbody>\n");
    sb.append("</table>\n");

    return sb.toString();
  }

  /**
   * Renders this element in vertical mode (a single row in the parent's shared table).
   */
  public String renderVertical(MjSocial parent) {
    StringBuilder sb = new StringBuilder();

    sb.append("<tr>\n");
    appendIconAndTextCells(sb, parent);
    sb.append("</tr>\n");

    return sb.toString();
  }

  private void appendIconAndTextCells(StringBuilder sb, MjSocial parent) {
    String name = getAttribute("name", "");
    NetworkInfo networkInfo = SocialNetworkRegistry.getNetwork(name);
    boolean isNoShare = name.contains("-noshare");

    // Resolve icon source
    String src = getAttribute("src", "");
    if (src.isEmpty() && !name.isEmpty()) {
      src = SocialNetworkRegistry.getIconUrl(name);
    }

    // Resolve background color (only from explicit attribute or network registry)
    String backgroundColor = getAttribute("background-color", "");
    if (backgroundColor.isEmpty() && networkInfo != null) {
      backgroundColor = networkInfo.backgroundColor();
    }

    // Resolve href with share URL
    String href = getAttribute("href", "");
    if (!href.isEmpty() && !isNoShare && networkInfo != null) {
      String shareUrl = networkInfo.shareUrl();
      if (shareUrl != null && !shareUrl.isEmpty()) {
        href = shareUrl.replace("[[URL]]", href);
      }
    }

    String target = getAttribute("target", "_blank");
    String alt = getAttribute("alt", "");

    String borderRadius = getInheritedAttribute(parent, "border-radius", "3px");
    String iconSize = getInheritedAttribute(parent, "icon-size", "20px");
    // icon-padding is only included in style if explicitly set on element or parent
    String iconPadding = getExplicitAttribute(parent, "icon-padding");
    String innerPadding = getInheritedAttribute(parent, "inner-padding", "4px 4px");
    String verticalAlign = getAttribute("vertical-align", "middle");

    String iconSizeNum = iconSize.replace("px", "");
    innerPadding = normalizePadding(innerPadding);

    // Icon cell: outer td with inner-padding, inner table with background
    sb.append("<td style=\"padding:").append(innerPadding)
        .append(";vertical-align:").append(verticalAlign).append(";\">\n");

    sb.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\"");
    sb.append(" style=\"");
    if (!backgroundColor.isEmpty()) {
      sb.append("background:").append(backgroundColor).append(";");
    }
    sb.append("border-radius:").append(borderRadius)
        .append(";width:").append(iconSize).append(";\">\n");
    sb.append("<tbody>\n");
    sb.append("<tr>\n");

    // Icon td with icon-padding (only if explicitly set)
    sb.append("<td style=\"");
    if (iconPadding != null && !iconPadding.isEmpty()) {
      sb.append("padding:").append(iconPadding).append(";");
    }
    sb.append("font-size:0;");
    sb.append("height:").append(iconSize).append(";");
    sb.append("vertical-align:").append(verticalAlign).append(";");
    sb.append("width:").append(iconSize).append(";\">\n");

    // Icon image with link
    boolean hasHref = !href.isEmpty();
    if (hasHref) {
      sb.append("<a href=\"").append(href).append("\"");
      sb.append(" target=\"").append(target).append("\">\n");
    }

    sb.append("<img alt=\"").append(alt)
        .append("\" src=\"").append(src).append("\"");
    sb.append(" style=\"border-radius:").append(borderRadius).append(";display:block;\"");
    sb.append(" width=\"").append(iconSizeNum).append("\"");
    sb.append(" />\n");

    if (hasHref) {
      sb.append("</a>\n");
    }

    sb.append("</td>\n");
    sb.append("</tr>\n");
    sb.append("</tbody>\n");
    sb.append("</table>\n");
    sb.append("</td>\n");

    // Text label (from inner HTML content)
    String textContent = node.getInnerHtml().trim();
    if (!textContent.isEmpty()) {
      String textPadding = getInheritedAttribute(parent, "text-padding", "4px 4px 4px 0");
      String color = getInheritedAttribute(parent, "color", "#333333");
      String fontFamily = getInheritedAttribute(parent, "font-family",
          "Ubuntu, Helvetica, Arial, sans-serif");
      String fontSize = getInheritedAttribute(parent, "font-size", "13px");
      String lineHeight = getInheritedAttribute(parent, "line-height", "22px");
      String textDecoration = getInheritedAttribute(parent, "text-decoration", "none");

      sb.append("<td style=\"vertical-align:").append(verticalAlign)
          .append(";padding:").append(textPadding)
          .append(";text-align:left;\">\n");

      Map<String, String> textStyles = new LinkedHashMap<>();
      textStyles.put("color", color);
      textStyles.put("font-size", fontSize);
      textStyles.put("font-family", fontFamily);
      textStyles.put("line-height", lineHeight);
      textStyles.put("text-decoration", textDecoration);

      if (hasHref) {
        sb.append("<a href=\"").append(href).append("\"");
        sb.append(" style=\"").append(buildStyle(textStyles)).append("\"");
        sb.append(" target=\"").append(target).append("\">");
        sb.append(" ").append(textContent).append(" ");
        sb.append("</a>\n");
      } else {
        sb.append("<span style=\"").append(buildStyle(textStyles)).append("\">");
        sb.append(" ").append(textContent).append(" ");
        sb.append("</span>\n");
      }

      sb.append("</td>\n");
    }
  }

  /**
   * Normalizes CSS padding shorthand: "4px 4px" -> "4px", "4px 4px 4px 4px" -> "4px".
   */
  private static String normalizePadding(String padding) {
    if (padding == null || padding.isEmpty()) {
      return padding;
    }
    String[] parts = padding.trim().split("\\s+");
    if (parts.length == 2 && parts[0].equals(parts[1])) {
      return parts[0];
    }
    if (parts.length == 4 && parts[0].equals(parts[1]) && parts[1].equals(parts[2])
        && parts[2].equals(parts[3])) {
      return parts[0];
    }
    return padding;
  }

  /**
   * Gets an attribute value only if explicitly set on the element or its parent.
   * Returns null if neither has it set.
   */
  private String getExplicitAttribute(MjSocial parent, String attrName) {
    String value = node.getAttribute(attrName);
    if (value != null && !value.isEmpty()) {
      return value;
    }
    if (parent != null) {
      String parentValue = parent.getNode().getAttribute(attrName);
      if (parentValue != null && !parentValue.isEmpty()) {
        return parentValue;
      }
    }
    return null;
  }

  /**
   * Gets an attribute value, falling back to the parent MjSocial's value if the element
   * doesn't have it set explicitly.
   */
  private String getInheritedAttribute(MjSocial parent, String attrName, String defaultValue) {
    // Check if explicitly set on this element
    String value = node.getAttribute(attrName);
    if (value != null && !value.isEmpty()) {
      return value;
    }
    // Fall back to parent's attribute
    if (parent != null) {
      String parentValue = parent.getAttribute(attrName, "");
      if (!parentValue.isEmpty()) {
        return parentValue;
      }
    }
    // Use default
    return getAttribute(attrName, defaultValue);
  }
}
