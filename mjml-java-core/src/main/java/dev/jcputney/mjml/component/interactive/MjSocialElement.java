
package dev.jcputney.mjml.component.interactive;

import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import dev.jcputney.mjml.util.CssUnitParser;
import dev.jcputney.mjml.util.HtmlBuilder;
import dev.jcputney.mjml.util.SocialNetworkRegistry;
import dev.jcputney.mjml.util.SocialNetworkRegistry.NetworkInfo;
import java.util.LinkedHashMap;
import java.util.Map;
import static dev.jcputney.mjml.util.HtmlBuilder.attrs;

/**
 * A single social element ({@code <mj-social-element>}). Renders a table containing a social icon
 * image and an optional text label. When used in horizontal mode, each element is wrapped in its
 * own inline table. When used in vertical mode, each element is a row in the parent's shared table.
 */
public class MjSocialElement extends BodyComponent {

  private static final Map<String, String> DEFAULTS = Map.ofEntries(
    Map.entry("align", "left"),
    Map.entry("alt", ""),
    Map.entry("background-color", ""),
    Map.entry("border-radius", "3px"),
    Map.entry("color", "#000"),
    Map.entry("css-class", ""),
    Map.entry("font-family", "Ubuntu, Helvetica, Arial, sans-serif"),
    Map.entry("font-size", "13px"),
    Map.entry("font-style", ""),
    Map.entry("font-weight", ""),
    Map.entry("href", ""),
    Map.entry("icon-height", ""),
    Map.entry("icon-padding", ""),
    Map.entry("icon-position", "left"),
    Map.entry("icon-size", "20px"),
    Map.entry("inner-padding", "4px 4px"),
    Map.entry("line-height", "1"),
    Map.entry("name", ""),
    Map.entry("padding", "4px"),
    Map.entry("padding-bottom", ""),
    Map.entry("padding-left", ""),
    Map.entry("padding-right", ""),
    Map.entry("padding-top", ""),
    Map.entry("sizes", ""),
    Map.entry("src", ""),
    Map.entry("srcset", ""),
    Map.entry("target", "_blank"),
    Map.entry("title", ""),
    Map.entry("text-decoration", "none"),
    Map.entry("text-padding", "4px 4px 4px 0"),
    Map.entry("vertical-align", "middle"));

  public MjSocialElement(MjmlNode node, GlobalContext globalContext, RenderContext renderContext) {
    super(node, globalContext, renderContext);
  }

  /** Normalizes CSS padding shorthand: "4px 4px" -> "4px", "4px 4px 4px 4px" -> "4px". */
  private static String normalizePadding(String padding) {
    if (padding == null || padding.isEmpty()) {
      return padding;
    }
    String[] parts = CssUnitParser.WHITESPACE.split(padding.trim());
    if (parts.length == 2 && parts[0].equals(parts[1])) {
      return parts[0];
    }
    if (parts.length == 4 && parts[0].equals(parts[1]) && parts[1].equals(parts[2]) && parts[2].equals(parts[3])) {
      return parts[0];
    }
    return padding;
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
   *
   * @param parent the parent MjSocial component, or {@code null} if rendering standalone
   * @return the rendered HTML string for this social element in horizontal layout
   */
  public String renderHorizontal(MjSocial parent) {
    String align = getInheritedAttribute(parent, "align", "left");

    var tableMap = new LinkedHashMap<>(HtmlBuilder.PRESENTATION_TABLE_ATTRS);
    tableMap.put("align", align);
    tableMap.put("style", "float:none;display:inline-table;");

    HtmlBuilder html = new HtmlBuilder();
    html.wrap("table", attrs(tableMap),
      () -> html.wrap("tbody",
        () -> html.wrap("tr",
          () -> appendIconAndTextCells(html, parent))));

    return html.toString();
  }

  /**
   * Renders this element in vertical mode (a single row in the parent's shared table).
   *
   * @param parent the parent MjSocial component, or {@code null} if rendering standalone
   * @return the rendered HTML string for this social element in vertical layout
   */
  public String renderVertical(MjSocial parent) {
    HtmlBuilder html = new HtmlBuilder();
    html.wrap("tr",
      () -> appendIconAndTextCells(html, parent));

    return html.toString();
  }

  /**
   * Appends HTML cells containing an icon and text to the provided HtmlBuilder.
   *
   * @param html   the HtmlBuilder instance used to construct the HTML content
   * @param parent the parent MjSocial component for inheriting attributes
   */
  private void appendIconAndTextCells(HtmlBuilder html, MjSocial parent) {
    String name = getAttribute("name", "");
    NetworkInfo networkInfo = SocialNetworkRegistry.getNetwork(name);

    String src = resolveIconSrc(name);
    String backgroundColor = resolveBackgroundColor(networkInfo);
    String href = resolveHref(name, networkInfo);
    boolean hasHref = !href.isEmpty();

    String target = getAttribute("target", "_blank");
    String verticalAlign = getAttribute("vertical-align", "middle");
    String borderRadius = getInheritedAttribute(parent, "border-radius", "3px");
    String iconSize = getInheritedAttribute(parent, "icon-size", "20px");

    appendIconCell(html, parent, src, backgroundColor, href, hasHref, target,
      getAttribute("alt", ""), borderRadius, iconSize, verticalAlign);

    String textContent = sanitizeContent(node.getInnerHtml().trim());
    if (!textContent.isEmpty()) {
      appendTextCell(html, parent, href, hasHref, target, verticalAlign, textContent);
    }
  }

  /**
   * Resolves the source URL for an icon image based on the provided element name and existing attributes. The method
   * first checks the "src" attribute for a URL. If this attribute is not set or is empty, it attempts to retrieve a
   * default URL from the {@code SocialNetworkRegistry} based on the provided name.
   *
   * @param name the name of the social element, used to look up a default icon URL if the "src" attribute is not set
   * @return the resolved icon source URL as a string; returns an empty string if no URL can be resolved
   */
  private String resolveIconSrc(String name) {
    String src = getAttribute("src", "");
    if (src.isEmpty() && !name.isEmpty()) {
      src = SocialNetworkRegistry.getIconUrl(name);
    }
    return src;
  }

  /**
   * Resolves the background color for the current element. This method first
   * attempts to retrieve the "background-color" attribute value using {@code getAttribute}.
   * If the attribute is not set or is empty, it then falls back to the default
   * background color provided by the {@code NetworkInfo} record, if available.
   *
   * @param networkInfo an optional parameter containing the social network's
   *                    default background color information; may be {@code null}
   * @return a string containing the resolved background color in CSS format;
   *         returns an empty string if no background color is explicitly defined
   *         or available from the {@code NetworkInfo}
   */
  private String resolveBackgroundColor(NetworkInfo networkInfo) {
    String bg = getAttribute("background-color", "");
    if (bg.isEmpty() && networkInfo != null) {
      bg = networkInfo.backgroundColor();
    }
    return bg;
  }

  /**
   * Resolves the hyperlink reference (href) for the social element based on the provided
   * element name and network information. If the element name does not indicate a "no share"
   * configuration and valid network information is supplied, the href value is updated using
   * the network's sharing URL pattern. Finally, the resolved href is sanitized before returning.
   *
   * @param name        the name of the social element, determining sharing behavior
   * @param networkInfo information about the social network, including sharing URL and defaults;
   *                    may be {@code null} to indicate no network specifics
   * @return the resolved and sanitized href string
   */
  private String resolveHref(String name, NetworkInfo networkInfo) {
    String href = getAttribute("href", "");
    if (!href.isEmpty() && !name.contains("-noshare") && networkInfo != null) {
      String shareUrl = networkInfo.shareUrl();
      if (shareUrl != null && !shareUrl.isEmpty()) {
        href = shareUrl.replace("[[URL]]", href);
      }
    }
    return sanitizeHref(href);
  }

  /**
   * Appends an icon cell to the provided HTML builder. This method generates a table cell
   * containing a styled icon, with optional link wrapping, based on the provided attributes.
   *
   * @param html            the {@code HtmlBuilder} used to construct the HTML output
   * @param parent          the parent {@code MjSocial} component, which is used to inherit styles
   * @param src             the source URL of the icon image
   * @param backgroundColor the background color of the icon, defined as a CSS color value
   * @param href            the hyperlink reference to wrap the icon; can be {@code null} or empty if no link is
   *                        required
   * @param hasHref         a flag indicating whether the icon should be wrapped in a hyperlink
   * @param target          the target attribute for the hyperlink, e.g., "_blank"
   * @param alt             the alt text for the icon image, used for accessibility
   * @param borderRadius    the CSS border-radius value for rounding the icon's corners
   * @param iconSize        the size of the icon, defined as a CSS dimension (e.g., "24px")
   * @param verticalAlign   the vertical alignment of the cell, as a CSS value
   */
  private void appendIconCell(HtmlBuilder html, MjSocial parent, String src, String backgroundColor,
    String href, boolean hasHref, String target, String alt, String borderRadius, String iconSize,
    String verticalAlign) {
    String iconSizeNum = iconSize.replace("px", "");
    String iconPadding = getExplicitAttribute(parent, "icon-padding");

    // Outer td padding — inherit from parent inner-padding if not explicitly set
    String outerPadding = resolveOuterPadding(parent);
    Map<String, String> tdStyles = new LinkedHashMap<>();
    tdStyles.put("padding", normalizePadding(outerPadding));
    addIfPresent(tdStyles, "padding-top");
    addIfPresent(tdStyles, "padding-right");
    addIfPresent(tdStyles, "padding-bottom");
    addIfPresent(tdStyles, "padding-left");
    tdStyles.put("vertical-align", verticalAlign);

    // Inner icon table
    String innerTableStyle = (backgroundColor.isEmpty() ? "" : "background:" + backgroundColor + ";")
      + "border-radius:" + borderRadius + ";width:" + iconSize + ";";
    var iconTableMap = new LinkedHashMap<>(HtmlBuilder.PRESENTATION_TABLE_ATTRS);
    iconTableMap.put("style", innerTableStyle);

    String iconTdStyle = (iconPadding != null && !iconPadding.isEmpty() ? "padding:" + iconPadding + ";" : "")
      + "font-size:0;height:" + iconSize + ";vertical-align:" + verticalAlign + ";width:" + iconSize + ";";

    var imgAttrs = new LinkedHashMap<String, String>();
    imgAttrs.put("alt", escapeAttr(alt));
    imgAttrs.put("src", escapeAttr(src));
    imgAttrs.put("style", "border-radius:" + borderRadius + ";display:block;");
    imgAttrs.put("width", iconSizeNum);
    String imgAttrStr = attrs(imgAttrs);

    html.wrap("td", attrs("style", buildStyle(tdStyles)),
      () -> html.wrap("table", attrs(iconTableMap),
        () -> html.wrap("tbody",
          () -> html.wrap("tr",
            () -> html.wrap("td", attrs("style", iconTdStyle), () -> {
              if (hasHref) {
                html.wrap("a", attrs("href", escapeHref(href), "target", escapeAttr(target)),
                  () -> html.selfClose("img", imgAttrStr));
              } else {
                html.selfClose("img", imgAttrStr);
              }
            })))));
  }

  /**
   * Resolves the outer padding for the current element. If the parent {@code MjSocial} component
   * specifies an inner-padding, and the current element does not specify its own explicit padding,
   * the parent's inner-padding is used as the outer padding for the current element. Otherwise, the
   * default or explicitly set padding is used.
   *
   * @param parent the parent {@code MjSocial} component, or {@code null} if the current element
   *               is standalone
   * @return a string representing the resolved outer padding value
   */
  private String resolveOuterPadding(MjSocial parent) {
    String outerPadding = getAttribute("padding", "4px");
    if (parent != null) {
      String parentInnerPadding = parent.getNode().getAttribute("inner-padding");
      if (parentInnerPadding != null && !parentInnerPadding.isEmpty()) {
        String childExplicit = node.getAttribute("padding");
        if (childExplicit == null || childExplicit.isEmpty()) {
          outerPadding = parentInnerPadding;
        }
      }
    }
    return outerPadding;
  }

  /**
   * Appends a text cell to the provided HTML builder. This method generates a table cell
   * with appropriate styles and adds the text content, optionally wrapped in a hyperlink
   * if a valid href is provided.
   *
   * @param html          the {@code HtmlBuilder} used to construct the HTML output
   * @param parent        the parent {@code MjSocial} component, which is used to inherit styles
   * @param href          the hyperlink reference; can be {@code null} or empty if no link is required
   * @param hasHref       a flag indicating whether the text content should be wrapped in a hyperlink
   * @param target        the target attribute for the hyperlink, e.g., "_blank"
   * @param verticalAlign the vertical alignment of the cell, as a CSS value
   * @param textContent   the actual text content to display inside the cell
   */
  private void appendTextCell(HtmlBuilder html, MjSocial parent, String href, boolean hasHref,
    String target, String verticalAlign, String textContent) {
    String textPadding = getInheritedAttribute(parent, "text-padding", "4px 4px 4px 0");
    String textTdStyle = "vertical-align:" + verticalAlign + ";padding:" + textPadding + ";text-align:left;";

    String textStyle = buildStyle(orderedMap(
      "color", getInheritedAttribute(parent, "color", "#000"),
      "font-size", getInheritedAttribute(parent, "font-size", "13px"),
      "font-family", getInheritedAttribute(parent, "font-family", "Ubuntu, Helvetica, Arial, sans-serif"),
      "line-height", getInheritedAttribute(parent, "line-height", "1"),
      "text-decoration", getInheritedAttribute(parent, "text-decoration", "none")));

    html.wrap("td", attrs("style", textTdStyle), () -> {
      if (hasHref) {
        html.openInline("a", attrs("href", escapeHref(href), "style", textStyle, "target", escapeAttr(target)))
          .text(" " + textContent + " ")
          .closeInlineLn("a");
      } else {
        html.openInline("span", attrs("style", textStyle))
          .text(" " + textContent + " ")
          .closeInlineLn("span");
      }
    });
  }

  /**
   * Gets an attribute value only if explicitly set on the element or its parent. Returns null if
   * neither has it set.
   *
   * <p>This custom cascade is needed because mj-social-element uses a parent-child inheritance
   * model that differs from the standard mj-attributes cascade. The standard cascade resolves
   * attributes via mj-class/tag-defaults/mj-all, but social elements also need to inherit
   * presentational attributes (icon-size, border-radius, colors, etc.) directly from their
   * enclosing mj-social parent. This two-level lookup (element -> parent -> defaults) lets the
   * parent act as a shared configuration for all its child elements without requiring
   * mj-attributes.
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
   * Gets an attribute value, falling back to the parent MjSocial's value if the element doesn't
   * have it set explicitly. See {@link #getExplicitAttribute} for why this custom cascade exists
   * alongside the standard mj-attributes cascade.
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
