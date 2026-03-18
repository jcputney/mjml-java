package dev.jcputney.mjml.component.interactive;

import static dev.jcputney.mjml.util.HtmlBuilder.attrs;

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

        HtmlBuilder html = new HtmlBuilder();
        html.open(
                "table",
                attrs(
                        "align",
                        align,
                        "border",
                        "0",
                        "cellpadding",
                        "0",
                        "cellspacing",
                        "0",
                        "role",
                        "presentation",
                        "style",
                        "float:none;display:inline-table;"));
        html.open("tbody");
        html.open("tr");
        appendIconAndTextCells(html, parent);
        html.close("tr");
        html.close("tbody");
        html.close("table");

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
        html.open("tr");
        appendIconAndTextCells(html, parent);
        html.close("tr");

        return html.toString();
    }

    private void appendIconAndTextCells(HtmlBuilder html, MjSocial parent) {
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
        href = sanitizeHref(href);

        String target = getAttribute("target", "_blank");
        String alt = getAttribute("alt", "");

        String borderRadius = getInheritedAttribute(parent, "border-radius", "3px");
        String iconSize = getInheritedAttribute(parent, "icon-size", "20px");
        String iconPadding = getExplicitAttribute(parent, "icon-padding");
        String verticalAlign = getAttribute("vertical-align", "middle");

        String iconSizeNum = iconSize.replace("px", "");

        // Icon cell: outer td with padding from cascade
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
        Map<String, String> tdStyles = new LinkedHashMap<>();
        tdStyles.put("padding", normalizePadding(outerPadding));
        addIfPresent(tdStyles, "padding-top");
        addIfPresent(tdStyles, "padding-right");
        addIfPresent(tdStyles, "padding-bottom");
        addIfPresent(tdStyles, "padding-left");
        tdStyles.put("vertical-align", verticalAlign);
        html.open("td", attrs("style", buildStyle(tdStyles)));

        // Inner table with icon
        String innerTableStyle = (backgroundColor.isEmpty() ? "" : "background:" + backgroundColor + ";")
                + "border-radius:"
                + borderRadius
                + ";width:"
                + iconSize
                + ";";
        html.open(
                "table",
                attrs(
                        "border",
                        "0",
                        "cellpadding",
                        "0",
                        "cellspacing",
                        "0",
                        "role",
                        "presentation",
                        "style",
                        innerTableStyle));
        html.open("tbody");
        html.open("tr");

        // Icon td with icon-padding (only if explicitly set)
        String iconTdStyle = (iconPadding != null && !iconPadding.isEmpty() ? "padding:" + iconPadding + ";" : "")
                + "font-size:0;height:"
                + iconSize
                + ";vertical-align:"
                + verticalAlign
                + ";width:"
                + iconSize
                + ";";
        html.open("td", attrs("style", iconTdStyle));

        boolean hasHref = !href.isEmpty();
        if (hasHref) {
            html.open("a", attrs("href", escapeHref(href), "target", escapeAttr(target)));
        }

        html.rawVerbatim("<img"
                + attrs("alt", escapeAttr(alt), "src", escapeAttr(src))
                + " style=\"border-radius:"
                + borderRadius
                + ";display:block;\""
                + " width=\""
                + iconSizeNum
                + "\" />\n");

        if (hasHref) {
            html.close("a");
        }

        html.close("td");
        html.close("tr");
        html.close("tbody");
        html.close("table");
        html.close("td");

        // Text label (from inner HTML content)
        String textContent = sanitizeContent(node.getInnerHtml().trim());
        if (!textContent.isEmpty()) {
            String textPadding = getInheritedAttribute(parent, "text-padding", "4px 4px 4px 0");
            String color = getInheritedAttribute(parent, "color", "#000");
            String fontFamily = getInheritedAttribute(parent, "font-family", "Ubuntu, Helvetica, Arial, sans-serif");
            String fontSize = getInheritedAttribute(parent, "font-size", "13px");
            String lineHeight = getInheritedAttribute(parent, "line-height", "1");
            String textDecoration = getInheritedAttribute(parent, "text-decoration", "none");

            String textTdStyle = "vertical-align:" + verticalAlign + ";padding:" + textPadding + ";text-align:left;";
            html.open("td", attrs("style", textTdStyle));

            String textStyle = buildStyle(orderedMap(
                    "color", color,
                    "font-size", fontSize,
                    "font-family", fontFamily,
                    "line-height", lineHeight,
                    "text-decoration", textDecoration));

            if (hasHref) {
                html.rawVerbatim("<a"
                        + attrs("href", escapeHref(href), "style", textStyle, "target", escapeAttr(target))
                        + "> "
                        + textContent
                        + " </a>\n");
            } else {
                html.rawVerbatim("<span" + attrs("style", textStyle) + "> " + textContent + " </span>\n");
            }

            html.close("td");
        }
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
