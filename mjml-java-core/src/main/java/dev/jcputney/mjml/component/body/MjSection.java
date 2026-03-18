
package dev.jcputney.mjml.component.body;

import dev.jcputney.mjml.component.BaseComponent;
import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.component.ComponentRegistry;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import dev.jcputney.mjml.render.VmlHelper;
import dev.jcputney.mjml.util.ColumnWidthCalculator;
import dev.jcputney.mjml.util.CssBoxModel;
import dev.jcputney.mjml.util.CssUnitParser;
import dev.jcputney.mjml.util.HtmlBuilder;
import dev.jcputney.mjml.util.MsoHelper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static dev.jcputney.mjml.util.HtmlBuilder.attrs;

/**
 * The section component (&lt;mj-section&gt;). Renders a table-based row containing columns with
 * responsive behavior. Handles full-width sections, background colors/images, padding, and MSO
 * conditional column layout.
 */
public class MjSection extends AbstractSectionComponent {

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
    Map.entry("direction", "ltr"),
    Map.entry("full-width", ""),
    Map.entry("padding", "20px 0"),
    Map.entry("text-align", "center"),
    Map.entry("text-padding", "4px 4px 4px 0"));
  private static final Set<String> COLUMN_TAGS = Set.of("mj-column", "mj-group");

  public MjSection(
    MjmlNode node, GlobalContext globalContext, RenderContext renderContext, ComponentRegistry registry) {
    super(node, globalContext, renderContext, registry);
  }

  @Override
  public double getContentWidth() {
    double containerWidth = renderContext.getContainerWidth();
    CssBoxModel box = getBoxModel();
    return containerWidth - box.paddingLeft() - box.paddingRight() - box.borderLeftWidth() - box.borderRightWidth();
  }

  @Override
  public String getTagName() {
    return "mj-section";
  }

  @Override
  public Map<String, String> getDefaultAttributes() {
    return DEFAULTS;
  }

  @Override
  public String render() {
    if (renderContext.isInsideWrapper()) {
      return renderInsideWrapper();
    }
    boolean isFullWidth = "full-width".equals(getAttribute("full-width"));
    if (isFullWidth) {
      return renderFullWidth();
    }
    return renderNormal();
  }

  /**
   * Renders the section in its normal layout (non-full-width), using the column-based children and an optional
   * background. Builds VML markup for background images if a `background-url` is provided, and styles the section with
   * attributes such as `background-color` and `css-class`. The rendered output includes the inner content and maintains
   * the defined structure for MSO compatibility.
   *
   * @return the complete HTML string for the rendered section in normal layout
   */
  private String renderNormal() {
    String bgUrl = getAttribute("background-url", "");
    String bgColor = getAttribute("background-color");
    String vmlRect = hasBackgroundUrl() ? buildVmlRect(globalContext.metadata().getContainerWidth() + "px", bgUrl,
      bgColor) : "";
    return renderNormalScaffold(vmlRect, renderColumnChildren(), getAttribute("css-class", ""));
  }

  /**
   * Renders this section when inside a wrapper component. No outer MSO table or background div —
   * the wrapper provides those. Just renders: div (max-width) -> inner table -> inner td (padding)
   * -> columns.
   */
  private String renderInsideWrapper() {
    int wrapperInnerWidth = (int) renderContext.getContainerWidth();
    String tableAttrs = attrs("align", "center", "border", "0", "cellpadding", "0", "cellspacing", "0",
      "role", "presentation", "style", "width:100%;");

    HtmlBuilder html = new HtmlBuilder();
    html.wrap("div", attrs("style", "margin:0px auto;max-width:" + wrapperInnerWidth + "px;"),
      () -> html.wrap("table", tableAttrs,
        () -> html.wrap("tbody",
          () -> html.wrap("tr",
            () -> html.wrap("td", attrs("style", buildInnerTdStyle()),
              () -> html.rawVerbatim(renderColumnChildren()))))));

    return html.toString();
  }

  /**
   * Renders a full-width section. Outer: real HTML table with width:100%. With bg image: adds VML
   * rect, line-height wrapper, and background CSS.
   */
  private String renderFullWidth() {
    HtmlBuilder html = new HtmlBuilder();
    html.wrap("table", buildFullWidthOuterAttrs(),
      () -> html.wrap("tbody",
        () -> html.wrap("tr",
          () -> html.wrap("td",
            () -> renderFullWidthContent(html)))));

    return html.toString();
  }

  /**
   * Builds the HTML attributes for the outer table of a full-width section. This method constructs a map of attributes,
   * including alignment, style, and optional background properties, to represent the full-width section. If a
   * `background-url` is present, it adds VML background support. Otherwise, it applies a background color and ensures
   * the table spans the full width of the layout.
   *
   * @return the assembled string of HTML attributes for the outer table
   */
  private String buildFullWidthOuterAttrs() {
    String bgColor = getAttribute("background-color");
    boolean hasBg = bgColor != null && !bgColor.isEmpty();

    String backgroundStyles = hasBg ? "background:" + bgColor + ";background-color:" + bgColor + ";" : "";
    String outerStyle = hasBackgroundUrl() ? buildBgImageTableStyle() : backgroundStyles + "width:100%;";

    var outerTableMap = new LinkedHashMap<>(HtmlBuilder.PRESENTATION_TABLE_ATTRS);
    outerTableMap.put("align", "center");
    if (hasBackgroundUrl()) {
      outerTableMap.put("background", escapeAttr(getAttribute("background-url", "")));
    }
    outerTableMap.put("style", outerStyle);
    return attrs(outerTableMap);
  }

  /**
   * Renders the content of a section in full-width layout. It constructs the HTML and VML (for Microsoft Office
   * support) structure, applying styles such as background color or background image if provided. The method ensures
   * proper alignment and wrapping, adapting for both standard clients and MSO rendering contexts.
   *
   * @param html the {@code HtmlBuilder} instance used to construct the HTML and VML output
   */
  private void renderFullWidthContent(HtmlBuilder html) {
    int containerWidth = globalContext.metadata().getContainerWidth();
    String bgColor = getAttribute("background-color");
    boolean hasBg = bgColor != null && !bgColor.isEmpty();
    boolean hasBgUrl = hasBackgroundUrl();
    String bgUrl = getAttribute("background-url", "");

    // MSO conditional + VML
    html.mso(() -> {
      if (hasBgUrl) {
        html.rawVerbatim(buildVmlRect("mso-width-percent:1000;", bgUrl, bgColor));
      }
      html.rawVerbatim(MsoHelper.msoTableOpening(
        containerWidth, escapeAttr(getCssClass()),
        hasBg ? escapeAttr(bgColor) : null, MsoHelper.MSO_TD_STYLE));
    });

    // Inner content table
    var innerTableMap = new LinkedHashMap<>(HtmlBuilder.PRESENTATION_TABLE_ATTRS);
    innerTableMap.put("align", "center");
    innerTableMap.put("style", "width:100%;");

    Runnable innerTable = () -> html.wrap("table", attrs(innerTableMap),
      () -> html.wrap("tbody",
        () -> html.wrap("tr",
          () -> html.wrap("td", attrs("style", buildInnerTdStyle()),
            () -> html.rawVerbatim(renderColumnChildren())))));

    html.wrap("div", attrs("style", "margin:0px auto;max-width:" + containerWidth + "px;"), () -> {
      if (hasBgUrl) {
        html.wrap("div", attrs("style", "line-height:0;font-size:0;"), innerTable);
      } else {
        innerTable.run();
      }
    });

    // Close MSO
    html.mso(MsoHelper.msoTableClosing() + (hasBgUrl ? "</v:textbox></v:rect>" : ""));
  }

  /**
   * Builds a VML rectangle markup for rendering background images or colors in Microsoft Office environments. This
   * method is used to ensure compatibility with MSO rendering by generating the appropriate VML markup based on the
   * provided attributes.
   *
   * @param widthStyle the width style for the VML rectangle
   * @param bgUrl      the background image URL
   * @param bgColor    the background color
   * @return the constructed VML rectangle string
   */
  private String buildVmlRect(String widthStyle, String bgUrl, String bgColor) {
    return VmlHelper.buildSectionVmlRect(
      widthStyle,
      bgUrl,
      bgColor,
      resolveBackgroundPosition(),
      getAttribute("background-size", "auto"),
      getAttribute("background-repeat", "repeat"));
  }

  /**
   * Renders the column-based children of the section, generating the necessary HTML and VML structures to ensure proper
   * layout and MSO compatibility. It handles the creation of table-based columns, calculating their widths and applying
   * styles to maintain responsive design while supporting custom CSS classes and alignment attributes.
   * <p>
   * If no columns exist, it produces an empty table structure. For each column, it constructs the appropriate HTML and
   * styles, taking into account whether the column is a group. The method ensures that columns are rendered seamlessly
   * for both standard clients and Microsoft Office environments by including conditional MSO markup.
   *
   * @return the rendered HTML string representing the section's column-based children
   */
  private String renderColumnChildren() {
    HtmlBuilder html = new HtmlBuilder();
    List<MjmlNode> columns = getColumnChildren();

    if (columns.isEmpty()) {
      html.mso(() -> html.wrap("table",
        attrs("role", "presentation", "border", "0", "cellpadding", "0", "cellspacing", "0"),
        () -> html.wrap("tr", () -> {
        })));
      return html.toString();
    }

    double contentWidth = getContentWidth();
    double[] widths = ColumnWidthCalculator.calculatePixelWidths(columns, contentWidth, true);
    String[] widthSpecs = ColumnWidthCalculator.calculateWidthSpecs(columns);

    for (int i = 0; i < columns.size(); i++) {
      MjmlNode col = columns.get(i);
      boolean isGroup = "mj-group".equals(col.getTagName());

      String colCssClass = col.getAttribute("css-class", "");
      String msoColClass = colCssClass.isEmpty() ? "" : colCssClass + "-outlook";
      String msoStyle =
        (!isGroup ? "vertical-align:" + escapeAttr(col.getAttribute("vertical-align", "top")) + ";" : "")
          + "width:"
          + CssUnitParser.formatPxWidth(widths[i])
          + "px;";

      String msoTdOpen = "<td class=\"" + escapeAttr(msoColClass) + "\" style=\"" + msoStyle + "\" >";

      // MSO: open table (first) or close-prev + open-next (subsequent)
      if (i == 0) {
        html.mso("<table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>"
          + msoTdOpen);
      } else {
        // Close previous td + open this td in one conditional (no split)
        html.mso("</td>" + msoTdOpen);
      }

      RenderContext colContext = renderContext
        .withColumnWidth(widths[i], widthSpecs[i])
        .withPosition(i, i == 0, i == columns.size() - 1);

      BaseComponent component = registry.createComponent(col, globalContext, colContext);
      if (component instanceof BodyComponent bodyComponent) {
        html.rawVerbatim(bodyComponent.render());
      }
    }

    // Close last td + table after the loop
    if (!columns.isEmpty()) {
      html.mso("</td></tr></table>");
    }

    return html.toString();
  }

  /**
   * Retrieves the child nodes of the current section that are categorized as column elements. This method filters the
   * children of the section based on predefined column tags and returns only those that match, preserving the
   * structural hierarchy of the MJML components.
   *
   * @return a list of {@code MjmlNode} objects representing the column-type children of the section
   */
  private List<MjmlNode> getColumnChildren() {
    return getChildrenByTags(COLUMN_TAGS);
  }

  @Override
  protected void addInnerTdBorderStyles(Map<String, String> styles) {
    addBorderStyles(styles, "border", "border-bottom", "border-left", "border-right", "border-top");
  }

  @Override
  protected void addInnerTdPaddingOverrides(Map<String, String> styles) {
    addIfPresent(styles, "padding-bottom");
    addIfPresent(styles, "padding-left");
    addIfPresent(styles, "padding-right");
    addIfPresent(styles, "padding-top");
  }
}
