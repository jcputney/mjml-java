package dev.jcputney.mjml.component.body;

import static dev.jcputney.mjml.util.HtmlBuilder.attrs;

import dev.jcputney.mjml.component.BaseComponent;
import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.component.ComponentRegistry;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import dev.jcputney.mjml.render.VmlHelper;
import dev.jcputney.mjml.util.CssBoxModel;
import dev.jcputney.mjml.util.CssUnitParser;
import dev.jcputney.mjml.util.HtmlBuilder;
import dev.jcputney.mjml.util.MsoHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The wrapper component (&lt;mj-wrapper&gt;). Similar to mj-section but wraps multiple sections
 * together, allowing a shared background color/image across sections. Each child section stacks
 * vertically within the wrapper.
 */
public class MjWrapper extends AbstractSectionComponent {

    private static final Map<String, String> DEFAULTS = Map.ofEntries(
            Map.entry("background-color", ""),
            Map.entry("background-position", "top center"),
            Map.entry("background-position-x", ""),
            Map.entry("background-position-y", ""),
            Map.entry("background-repeat", "repeat"),
            Map.entry("background-size", "auto"),
            Map.entry("background-url", ""),
            Map.entry("border", "none"),
            Map.entry("border-bottom", ""),
            Map.entry("border-left", ""),
            Map.entry("border-radius", ""),
            Map.entry("border-right", ""),
            Map.entry("border-top", ""),
            Map.entry("full-width", ""),
            Map.entry("gap", ""),
            Map.entry("padding", "20px 0"),
            Map.entry("text-align", "center"));

    public MjWrapper(
            MjmlNode node, GlobalContext globalContext, RenderContext renderContext, ComponentRegistry registry) {
        super(node, globalContext, renderContext, registry);
    }

    @Override
    public String getTagName() {
        return "mj-wrapper";
    }

    @Override
    public Map<String, String> getDefaultAttributes() {
        return DEFAULTS;
    }

    @Override
    public String render() {
        boolean isFullWidth = "full-width".equals(getAttribute("full-width"));
        if (isFullWidth) {
            return renderFullWidth();
        }
        return renderNormal();
    }

    private String renderNormal() {
        String bgUrl = getAttribute("background-url", "");
        String bgColor = getAttribute("background-color");
        String vmlRect = hasBackgroundUrl()
                ? buildVmlRect(globalContext.metadata().getContainerWidth() + "px", bgUrl, bgColor)
                : "";
        HtmlBuilder innerContent = new HtmlBuilder();
        renderWrappedChildren(innerContent);
        return renderNormalScaffold(vmlRect, innerContent.toString(), "");
    }

    private String renderFullWidth() {
        int containerWidth = globalContext.metadata().getContainerWidth();
        String bgColor = getAttribute("background-color");
        boolean hasBg = bgColor != null && !bgColor.isEmpty();

        String outerStyle =
                (hasBg ? "background:" + bgColor + ";background-color:" + bgColor + ";" : "") + "width:100%;";

        HtmlBuilder html = new HtmlBuilder();

        html.open(
                "table",
                attrs(
                        "align",
                        "center",
                        "border",
                        "0",
                        "cellpadding",
                        "0",
                        "cellspacing",
                        "0",
                        "role",
                        "presentation",
                        "style",
                        outerStyle));
        html.open("tbody");
        html.open("tr");
        html.open("td");

        html.rawVerbatim(MsoHelper.conditionalStart()
                + MsoHelper.msoTableOpening(
                        containerWidth,
                        escapeAttr(getCssClass()),
                        hasBg ? escapeAttr(bgColor) : null,
                        MsoHelper.MSO_TD_STYLE)
                + MsoHelper.conditionalEnd()
                + "\n");

        html.open("div", attrs("style", "margin:0px auto;max-width:" + containerWidth + "px;"));
        html.open(
                "table",
                attrs(
                        "align",
                        "center",
                        "border",
                        "0",
                        "cellpadding",
                        "0",
                        "cellspacing",
                        "0",
                        "role",
                        "presentation",
                        "style",
                        "width:100%;"));
        html.open("tbody");
        html.open("tr");
        html.open("td", attrs("style", buildInnerTdStyle()));

        renderWrappedChildren(html);

        html.close("td");
        html.close("tr");
        html.close("tbody");
        html.close("table");
        html.close("div");

        html.raw(MsoHelper.msoConditionalTableClosing());

        html.close("td");
        html.close("tr");
        html.close("tbody");
        html.close("table");

        return html.toString();
    }

    private void renderWrappedChildren(HtmlBuilder html) {
        List<MjmlNode> sectionChildren = getSectionChildren();

        if (sectionChildren.isEmpty()) {
            html.mso("<table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"></table>");
            return;
        }

        int containerWidth = globalContext.metadata().getContainerWidth();
        CssBoxModel wrapperBox = getBoxModel();
        int innerWidth = (int) (containerWidth
                - wrapperBox.paddingLeft()
                - wrapperBox.paddingRight()
                - wrapperBox.borderLeftWidth()
                - wrapperBox.borderRightWidth());

        for (int i = 0; i < sectionChildren.size(); i++) {
            MjmlNode child = sectionChildren.get(i);
            boolean isFirst = (i == 0);
            boolean isLast = (i == sectionChildren.size() - 1);

            if (isFirst) {
                html.raw(MsoHelper.msoWrapperNestedOpening(containerWidth, innerWidth));
            }

            String gap = getAttribute("gap", "");
            if (!isFirst && !gap.isEmpty()) {
                int gapPx = CssUnitParser.parsePixels(gap, 0);
                if (gapPx > 0) {
                    html.rawVerbatim(
                            "<div style=\"font-size:0;line-height:" + gapPx + "px;height:" + gapPx + "px;\"> </div>\n");
                }
            }

            RenderContext childContext = renderContext
                    .withWidth(innerWidth)
                    .withPosition(i, isFirst, isLast)
                    .withInsideWrapper(true);

            BaseComponent component = registry.createComponent(child, globalContext, childContext);
            if (component instanceof BodyComponent bodyComponent) {
                html.rawVerbatim(bodyComponent.render());
            }

            if (!isLast) {
                html.raw(MsoHelper.msoWrapperTransition(containerWidth, innerWidth));
            } else {
                html.mso("</td></tr></table></td></tr></table>");
            }
        }
    }

    private List<MjmlNode> getSectionChildren() {
        List<MjmlNode> sections = new ArrayList<>();
        for (MjmlNode child : node.getChildren()) {
            String tag = child.getTagName();
            if (!tag.startsWith("#")) {
                sections.add(child);
            }
        }
        return sections;
    }

    private String buildVmlRect(String widthStyle, String bgUrl, String bgColor) {
        return VmlHelper.buildWrapperVmlRect(
                widthStyle, bgUrl, bgColor, resolveBackgroundPosition(), getAttribute("background-size", "auto"));
    }
}
