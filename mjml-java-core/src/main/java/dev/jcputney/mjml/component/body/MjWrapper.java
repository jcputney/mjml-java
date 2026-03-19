
package dev.jcputney.mjml.component.body;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static dev.jcputney.mjml.util.HtmlBuilder.attrs;

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

  /**
   * Renders the normal layout of the current component, including background styles and wrapped child components.
   * <p>
   * The method determines the background properties (URL and color) and constructs a VML rectangle for rendering the
   * background if necessary. It also renders the child components wrapped within the appropriate HTML structure.
   *
   * @return a {@code String} representing the rendered HTML content of the component with its normal layout.
   */
  private String renderNormal() {
    String bgUrl = getAttribute("background-url", "");
    String bgColor = getAttribute("background-color");
    String vmlRect = hasBackgroundUrl() ? buildVmlRect(globalContext.metadata().getContainerWidth() + "px", bgUrl,
      bgColor) : "";
    HtmlBuilder innerContent = new HtmlBuilder();
    renderWrappedChildren(innerContent);
    return renderNormalScaffold(vmlRect, innerContent.toString(), "");
  }

  /**
   * Renders the full-width layout of the current component, including background styles
   * and wrapped child components. This method constructs a nested HTML table structure
   * to ensure full-width rendering, aligned to the container width and styled with the
   * appropriate background and alignment properties.
   * <p>
   * The outer structure wraps the inner content with an HTML table at 100% width. If a
   * background color is specified, it is applied to the outer structure. The inner table
   * contains the rendered child components appropriately formatted and styled to fit
   * within the defined container width.
   *
   * @return a {@code String} representing the rendered full-width HTML content of the
   *         component.
   */
  private String renderFullWidth() {
    int containerWidth = globalContext.metadata().getContainerWidth();
    String bgColor = getAttribute("background-color");
    boolean hasBg = bgColor != null && !bgColor.isEmpty();

    String outerStyle =
      (hasBg ? "background:" + bgColor + ";background-color:" + bgColor + ";" : "") + "width:100%;";

    var outerTableMap = new LinkedHashMap<>(HtmlBuilder.PRESENTATION_TABLE_ATTRS);
    outerTableMap.put("align", "center");
    outerTableMap.put("style", outerStyle);

    var innerTableMap = new LinkedHashMap<>(HtmlBuilder.PRESENTATION_TABLE_ATTRS);
    innerTableMap.put("align", "center");
    innerTableMap.put("style", "width:100%;");

    HtmlBuilder html = new HtmlBuilder();
    html.wrap("table", attrs(outerTableMap),
      () -> html.wrap("tbody",
        () -> html.wrap("tr",
          () -> html.wrap("td", () -> {
            html.mso(MsoHelper.msoTableOpening(
              containerWidth,
              escapeAttr(getCssClass()),
              hasBg ? escapeAttr(bgColor) : null,
              MsoHelper.MSO_TD_STYLE));

            html.wrap("div", attrs("style", "margin:0px auto;max-width:" + containerWidth + "px;"),
              () -> html.wrap("table", attrs(innerTableMap),
                () -> html.wrap("tbody",
                  () -> html.wrap("tr",
                    () -> html.wrap("td", attrs("style", buildInnerTdStyle()),
                      () -> renderWrappedChildren(html))))));

            html.raw(MsoHelper.msoConditionalTableClosing());
          }))));

    return html.toString();
  }

  /**
   * Renders the child components wrapped within the appropriate HTML structure.
   * This method applies necessary formatting and layout adjustments, including gap settings
   * and container width calculations, for rendering the child components.
   * <p>
   * If the section has no children, it renders an empty table for fallback purposes.
   * Otherwise, it iterates over the child nodes, applying special formatting for the first
   * and last children, and handles gaps between child components when specified.
   * The method also utilizes context propagation to properly adjust child rendering
   * based on their position and wrapper-specific properties.
   *
   * @param html the {@code HtmlBuilder} instance used to construct the rendered HTML output
   */
  private void renderWrappedChildren(HtmlBuilder html) {
    List<MjmlNode> sectionChildren = getSectionChildren();

    if (sectionChildren.isEmpty()) {
      html.mso(
        () -> html.wrap("table", attrs("role", "presentation", "border", "0", "cellpadding", "0", "cellspacing", "0"),
          () -> {
          }));
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
          String gapStyle = "font-size:0;line-height:" + gapPx + "px;height:" + gapPx + "px;";
          html.openInline("div", attrs("style", gapStyle))
            .text(" ")
            .closeInlineLn("div");
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

  /**
   * Retrieves the child nodes of the current node that do not have tag names
   * starting with "#". These nodes represent valid sections within the component.
   *
   * @return a list of {@code MjmlNode} objects representing the section children
   *         of the current node.
   */
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

  /**
   * Constructs a VML rectangle definition based on the provided width style, background URL,
   * and background color. The method uses utility functionality to generate the VML representation
   * with resolved background position and size attributes.
   *
   * @param widthStyle the computed width style to be applied to the VML rectangle
   * @param bgUrl      the URL of the background image to be applied to the rectangle
   * @param bgColor    the background color to be applied to the rectangle
   * @return a String representing the constructed VML rectangle definition
   */
  private String buildVmlRect(String widthStyle, String bgUrl, String bgColor) {
    return VmlHelper.buildWrapperVmlRect(
      widthStyle, bgUrl, bgColor, resolveBackgroundPosition(), getAttribute("background-size", "auto"));
  }
}
