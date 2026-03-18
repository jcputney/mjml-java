package dev.jcputney.mjml.component.body;

import static dev.jcputney.mjml.util.HtmlBuilder.attrs;

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
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The section component (&lt;mj-section&gt;). Renders a table-based row containing columns with
 * responsive behavior. Handles full-width sections, background colors/images, padding, and MSO
 * conditional column layout.
 */
public class MjSection extends AbstractSectionComponent {

  private static final Map<String, String> DEFAULTS =
      Map.ofEntries(
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
      MjmlNode node,
      GlobalContext globalContext,
      RenderContext renderContext,
      ComponentRegistry registry) {
    super(node, globalContext, renderContext, registry);
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

  private String renderNormal() {
    String bgUrl = getAttribute("background-url", "");
    String bgColor = getAttribute("background-color");
    String vmlRect =
        hasBackgroundUrl()
            ? buildVmlRect(globalContext.metadata().getContainerWidth() + "px", bgUrl, bgColor)
            : "";
    return renderNormalScaffold(vmlRect, renderColumnChildren(), getAttribute("css-class", ""));
  }

  /**
   * Renders this section when inside a wrapper component. No outer MSO table or background div —
   * the wrapper provides those. Just renders: div (max-width) -> inner table -> inner td (padding)
   * -> columns.
   */
  private String renderInsideWrapper() {
    int wrapperInnerWidth = (int) renderContext.getContainerWidth();

    HtmlBuilder html = new HtmlBuilder();
    html.open("div", attrs("style", "margin:0px auto;max-width:" + wrapperInnerWidth + "px;"));
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
    html.rawVerbatim(renderColumnChildren());
    html.close("td");
    html.close("tr");
    html.close("tbody");
    html.close("table");
    html.close("div");

    return html.toString();
  }

  /**
   * Renders a full-width section. Outer: real HTML table with width:100%. With bg image: adds VML
   * rect, line-height wrapper, and background CSS.
   */
  private String renderFullWidth() {
    int containerWidth = globalContext.metadata().getContainerWidth();
    String bgColor = getAttribute("background-color");
    boolean hasBg = bgColor != null && !bgColor.isEmpty();
    boolean hasBgUrl = hasBackgroundUrl();
    String bgUrl = getAttribute("background-url", "");

    // Build outer table style
    String outerStyle;
    if (hasBgUrl) {
      outerStyle = buildBgImageTableStyle();
    } else {
      outerStyle =
          (hasBg ? "background:" + bgColor + ";background-color:" + bgColor + ";" : "")
              + "width:100%;";
    }

    HtmlBuilder html = new HtmlBuilder();

    // Outer full-width table
    html.open(
        "table",
        attrs("align", "center")
            + (hasBgUrl ? attrs("background", escapeAttr(bgUrl)) : "")
            + attrs(
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

    // MSO conditional + VML
    html.rawVerbatim(
        MsoHelper.conditionalStart()
            + (hasBgUrl ? buildVmlRect("mso-width-percent:1000;", bgUrl, bgColor) : "")
            + MsoHelper.msoTableOpening(
                containerWidth,
                escapeAttr(getCssClass()),
                hasBg ? escapeAttr(bgColor) : null,
                MsoHelper.MSO_TD_STYLE)
            + MsoHelper.conditionalEnd()
            + "\n");

    html.open("div", attrs("style", "margin:0px auto;max-width:" + containerWidth + "px;"));

    if (hasBgUrl) {
      html.open("div", attrs("style", "line-height:0;font-size:0;"));
    }

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

    html.rawVerbatim(renderColumnChildren());

    html.close("td");
    html.close("tr");
    html.close("tbody");
    html.close("table");

    if (hasBgUrl) {
      html.close("div");
    }
    html.close("div");

    // Close MSO
    if (hasBgUrl) {
      html.raw(
          MsoHelper.conditionalStart()
              + MsoHelper.msoTableClosing()
              + "</v:textbox></v:rect>"
              + MsoHelper.conditionalEnd());
    } else {
      html.raw(MsoHelper.msoConditionalTableClosing());
    }

    html.close("td");
    html.close("tr");
    html.close("tbody");
    html.close("table");

    return html.toString();
  }

  private String buildVmlRect(String widthStyle, String bgUrl, String bgColor) {
    return VmlHelper.buildSectionVmlRect(
        widthStyle,
        bgUrl,
        bgColor,
        resolveBackgroundPosition(),
        getAttribute("background-size", "auto"),
        getAttribute("background-repeat", "repeat"));
  }

  private String renderColumnChildren() {
    HtmlBuilder html = new HtmlBuilder();
    List<MjmlNode> columns = getColumnChildren();

    if (columns.isEmpty()) {
      html.raw(
          "<!--[if mso | IE]><table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr></tr></table><![endif]-->");
      return html.toString();
    }

    double contentWidth = getContentWidth();
    double[] widths = ColumnWidthCalculator.calculatePixelWidths(columns, contentWidth, true);
    String[] widthSpecs = ColumnWidthCalculator.calculateWidthSpecs(columns);

    html.rawVerbatim(
        "<!--[if mso | IE]><table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>");

    for (int i = 0; i < columns.size(); i++) {
      MjmlNode col = columns.get(i);
      boolean isGroup = "mj-group".equals(col.getTagName());

      String colCssClass = col.getAttribute("css-class", "");
      String msoColClass = colCssClass.isEmpty() ? "" : colCssClass + "-outlook";
      String msoStyle =
          (!isGroup
                  ? "vertical-align:" + escapeAttr(col.getAttribute("vertical-align", "top")) + ";"
                  : "")
              + "width:"
              + CssUnitParser.formatPxWidth(widths[i])
              + "px;";

      html.rawVerbatim(
          "<td class=\""
              + escapeAttr(msoColClass)
              + "\" style=\""
              + msoStyle
              + "\" ><![endif]-->\n");

      RenderContext colContext =
          renderContext
              .withColumnWidth(widths[i], widthSpecs[i])
              .withPosition(i, i == 0, i == columns.size() - 1);

      BaseComponent component = registry.createComponent(col, globalContext, colContext);
      if (component instanceof BodyComponent bodyComponent) {
        html.rawVerbatim(bodyComponent.render());
      }

      html.rawVerbatim("<!--[if mso | IE]></td>");
      if (i == columns.size() - 1) {
        html.rawVerbatim("</tr></table><![endif]-->\n");
      }
    }

    return html.toString();
  }

  private List<MjmlNode> getColumnChildren() {
    return getChildrenByTags(COLUMN_TAGS);
  }

  @Override
  public double getContentWidth() {
    double containerWidth = renderContext.getContainerWidth();
    CssBoxModel box = getBoxModel();
    return containerWidth
        - box.paddingLeft()
        - box.paddingRight()
        - box.borderLeftWidth()
        - box.borderRightWidth();
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
