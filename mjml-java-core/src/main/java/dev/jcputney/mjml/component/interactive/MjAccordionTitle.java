package dev.jcputney.mjml.component.interactive;

import static dev.jcputney.mjml.util.HtmlBuilder.attrs;

import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import dev.jcputney.mjml.util.CssUnitParser;
import dev.jcputney.mjml.util.HtmlBuilder;
import java.util.Map;

/**
 * The accordion title component ({@code <mj-accordion-title>}). Renders the title bar of an
 * accordion element as a table containing the title text cell and a toggle icon cell.
 */
public class MjAccordionTitle extends BodyComponent {

    private static final Map<String, String> DEFAULTS = Map.ofEntries(
            Map.entry("background-color", ""),
            Map.entry("color", ""),
            Map.entry("font-family", "Ubuntu, Helvetica, Arial, sans-serif"),
            Map.entry("font-size", "13px"),
            Map.entry("font-weight", ""),
            Map.entry("padding", "16px"));

    public MjAccordionTitle(MjmlNode node, GlobalContext globalContext, RenderContext renderContext) {
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
        String iconUnwrappedUrl = resolveAncestorAttr("icon-unwrapped-url", "https://i.imgur.com/w4uTygT.png");
        String iconUnwrappedAlt = resolveAncestorAttr("icon-unwrapped-alt", "-");
        String iconWrappedUrl = resolveAncestorAttr("icon-wrapped-url", "https://i.imgur.com/bIXv1bk.png");
        String iconWrappedAlt = resolveAncestorAttr("icon-wrapped-alt", "+");

        // Border comes from the accordion (grandparent or higher)
        String border = resolveAncestorAttr("border", "2px solid black");

        // Collapse whitespace in title content
        String content = sanitizeContent(
                CssUnitParser.WHITESPACE.matcher(node.getInnerHtml().trim()).replaceAll(" "));

        String tableStyle = buildStyle(orderedMap("width", "100%", "border-bottom", border));
        String tdStyle = buildStyle(orderedMap(
                "width", "100%",
                "background-color", backgroundColor,
                "color", color,
                "font-size", fontSize,
                "font-family", fontFamily,
                "padding", padding));

        HtmlBuilder html = new HtmlBuilder();
        html.open("table", attrs("cellspacing", "0", "cellpadding", "0", "style", tableStyle));
        html.open("tbody");
        html.open("tr");

        if ("left".equals(iconPosition)) {
            renderIcon(
                    html,
                    iconAlign,
                    backgroundColor,
                    iconWidth,
                    iconHeight,
                    iconUnwrappedUrl,
                    iconUnwrappedAlt,
                    iconWrappedUrl,
                    iconWrappedAlt);
        }

        html.openInline("td", attrs("style", tdStyle));
        html.text(" " + content + " ");
        html.closeInlineLn("td");

        if (!"left".equals(iconPosition)) {
            renderIcon(
                    html,
                    iconAlign,
                    backgroundColor,
                    iconWidth,
                    iconHeight,
                    iconUnwrappedUrl,
                    iconUnwrappedAlt,
                    iconWrappedUrl,
                    iconWrappedAlt);
        }

        html.close("tr");
        html.close("tbody");
        html.close("table");

        return html.toString();
    }

    private void renderIcon(
            HtmlBuilder html,
            String align,
            String backgroundColor,
            String width,
            String height,
            String unwrappedUrl,
            String unwrappedAlt,
            String wrappedUrl,
            String wrappedAlt) {

        String iconTdStyle =
                buildStyle(orderedMap("padding", "16px", "background", backgroundColor, "vertical-align", align));
        String imgStyle = buildStyle(orderedMap("display", "none", "width", width, "height", height));

        html.rawVerbatim("<!--[if !mso | IE]><!-->\n");
        html.rawVerbatim("<td class=\"mj-accordion-ico\""
                + attrs("style", iconTdStyle)
                + ">"
                + "<img"
                + attrs("src", escapeAttr(wrappedUrl), "alt", escapeAttr(wrappedAlt))
                + " class=\"mj-accordion-more\""
                + attrs("style", imgStyle)
                + " />"
                + "<img"
                + attrs("src", escapeAttr(unwrappedUrl), "alt", escapeAttr(unwrappedAlt))
                + " class=\"mj-accordion-less\""
                + attrs("style", imgStyle)
                + " />"
                + "</td>"
                + "<!--<![endif]-->\n");
    }

    private String resolveAncestorAttr(String name, String fallback) {
        return AccordionHelper.resolveAncestorAttr(node, name, globalContext, fallback);
    }
}
