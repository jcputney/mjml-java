
package dev.jcputney.mjml.component.body;

import dev.jcputney.mjml.component.BaseComponent;
import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.component.ComponentRegistry;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import dev.jcputney.mjml.util.CssBoxModel;
import dev.jcputney.mjml.util.HtmlBuilder;
import java.util.LinkedHashMap;
import java.util.Map;
import static dev.jcputney.mjml.util.HtmlBuilder.attrs;

/**
 * The column component (&lt;mj-column&gt;). Renders as a table-based column with vertical-align,
 * padding, and borders. Content components (text, image, button, etc.) are rendered as table rows.
 */
public class MjColumn extends BodyComponent {

  private static final Map<String, String> DEFAULTS = Map.ofEntries(
    Map.entry("background-color", ""),
    Map.entry("border", "none"),
    Map.entry("border-bottom", ""),
    Map.entry("border-left", ""),
    Map.entry("border-radius", ""),
    Map.entry("border-right", ""),
    Map.entry("border-top", ""),
    Map.entry("direction", "ltr"),
    Map.entry("inner-background-color", ""),
    Map.entry("inner-border", ""),
    Map.entry("inner-border-bottom", ""),
    Map.entry("inner-border-left", ""),
    Map.entry("inner-border-radius", ""),
    Map.entry("inner-border-right", ""),
    Map.entry("inner-border-top", ""),
    Map.entry("padding", ""),
    Map.entry("padding-bottom", ""),
    Map.entry("padding-left", ""),
    Map.entry("padding-right", ""),
    Map.entry("padding-top", ""),
    Map.entry("vertical-align", "top"),
    Map.entry("width", ""));
  private static final String[] PADDING_ATTRS = {
      "padding", "padding-bottom", "padding-left", "padding-right", "padding-top"
  };
  private final ComponentRegistry registry;

  public MjColumn(
    MjmlNode node, GlobalContext globalContext, RenderContext renderContext, ComponentRegistry registry) {
    super(node, globalContext, renderContext);
    this.registry = registry;
  }

  @Override
  public String getTagName() {
    return "mj-column";
  }

  @Override
  public Map<String, String> getDefaultAttributes() {
    return DEFAULTS;
  }

  @Override
  public String render() {
    double columnWidth = renderContext.getContainerWidth();
    String widthSpec = renderContext.getColumnWidthSpec();
    String responsiveClass = buildResponsiveClass(widthSpec);

    String cssClass = getAttribute("css-class", "");
    String divClass =
      responsiveClass + " mj-outlook-group-fix" + (cssClass.isEmpty() ? "" : " " + escapeAttr(cssClass));

    HtmlBuilder html = new HtmlBuilder();
    html.wrap("div", attrs("class", divClass, "style", buildOuterStyle()), () -> {
      if (hasGutter()) {
        var outerAttrs = new LinkedHashMap<>(HtmlBuilder.PRESENTATION_TABLE_ATTRS);
        if (hasBorderRadius()) {
          outerAttrs.put("style", "border-collapse:separate;");
        }
        outerAttrs.put("width", "100%");

        html.wrap("table", attrs(outerAttrs, "style"),
          () -> html.wrap("tbody",
            () -> html.wrap("tr",
              () -> html.wrap("td", attrs("style", buildGutterTdStyle()),
                () -> html.table(Map.of("style", buildInnerTableStyle(), "width", "100%"),
                  () -> html.wrap("tbody",
                    () -> renderContentChildren(html, columnWidth)))))));
      } else {
        html.table(Map.of("style", buildStyle(buildNoGutterTableStyle()), "width", "100%"),
          () -> html.wrap("tbody",
            () -> renderContentChildren(html, columnWidth)));
      }
    });

    registerMediaQuery(responsiveClass, widthSpec);

    return html.toString();
  }

  /**
   * Returns true if any padding attribute is set on this column. When true, uses nested table
   * structure (gutter pattern).
   */
  private boolean hasGutter() {
    for (String attr : PADDING_ATTRS) {
      String val = getAttribute(attr, "");
      if (!val.isEmpty()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if the "border-radius" attribute is set and non-empty for the current component.
   *
   * @return true if the "border-radius" attribute has a value; false otherwise
   */
  private boolean hasBorderRadius() {
    String borderRadius = getAttribute("border-radius", "");
    return !borderRadius.isEmpty();
  }

  /**
   * Renders the child content of an MJML column by iterating over its children, creating component instances, and
   * appending rendered HTML to the provided HtmlBuilder.
   *
   * @param html        the HtmlBuilder object used to construct the HTML output
   * @param columnWidth the width of the column in pixels, used to calculate content width
   */
  private void renderContentChildren(HtmlBuilder html, double columnWidth) {
    CssBoxModel box = getBoxModel();
    double contentWidth = columnWidth - box.horizontalSpacing();
    RenderContext childContext = renderContext.withWidth(contentWidth);

    var children = node.getChildren();
    for (int i = 0; i < children.size(); i++) {
      MjmlNode child = children.get(i);
      if (child.getTagName().startsWith("#")) {
        continue;
      }

      RenderContext itemContext = childContext.withPosition(i, i == 0, i == children.size() - 1);
      BaseComponent component = registry.createComponent(child, globalContext, itemContext);
      if (component instanceof BodyComponent bodyComponent) {
        renderChildComponent(html, child, bodyComponent);
      }
    }
  }

  private void renderChildComponent(HtmlBuilder html, MjmlNode child, BodyComponent bodyComponent) {
    // mj-raw content passes through without a wrapping tr/td
    if ("mj-raw".equals(child.getTagName())) {
      html.rawVerbatim(bodyComponent.render());
      return;
    }

    var tdAttrMap = new LinkedHashMap<String, String>();
    tdAttrMap.put("align", escapeAttr(bodyComponent.getAttribute("align", "")));
    tdAttrMap.put("class", escapeAttr(bodyComponent.getAttribute("css-class", "")));
    tdAttrMap.put("style", buildTdStyle(bodyComponent));

    html.wrap("tr",
      () -> html.wrap("td", attrs(tdAttrMap), () -> {
        html.rawVerbatim(bodyComponent.render());
        html.newline();
      }));
  }

  /**
   * Constructs and returns a CSS style string for the outer container of an MJML column. The style includes properties
   * such as font size, text alignment, direction, display type, vertical alignment, and width. If the column is inside
   * a group, the width is set based on the column width specification; otherwise, a default width of 100% is used.
   *
   * @return a string representing the CSS styles for the outer container
   */
  private String buildOuterStyle() {
    Map<String, String> styles = new LinkedHashMap<>();
    styles.put("font-size", "0px");
    styles.put("text-align", "left");
    styles.put("direction", getAttribute("direction", "ltr"));
    styles.put("display", "inline-block");
    styles.put("vertical-align", getAttribute("vertical-align", "top"));
    // Inside a group, use the actual percentage width; otherwise always 100%
    if (renderContext.isInsideGroup()) {
      String widthSpec = renderContext.getColumnWidthSpec();
      if (widthSpec != null && !widthSpec.endsWith("px")) {
        styles.put("width", widthSpec + "%");
      } else {
        styles.put("width", "100%");
      }
    } else {
      styles.put("width", "100%");
    }
    return buildStyle(styles);
  }

  /**
   * Builds a CSS style string for the gutter `<td>` element in an MJML column.
   * The style includes padding properties such as `padding`, `padding-bottom`,
   * `padding-left`, `padding-right`, and `padding-top`.
   *
   * @return a string representing the CSS styles for the gutter `<td>` element
   */
  private String buildGutterTdStyle() {
    Map<String, String> styles = buildNoGutterTableStyle();
    // Padding on the gutter td
    addIfPresent(styles, "padding");
    addIfPresent(styles, "padding-bottom");
    addIfPresent(styles, "padding-left");
    addIfPresent(styles, "padding-right");
    addIfPresent(styles, "padding-top");
    return buildStyle(styles);
  }

  /**
   * Constructs and returns a CSS style string for the inner table of an MJML column.
   * The style includes properties such as background color, border styles, and border radius if specified.
   * If a border radius is applied, the border-collapse property is set to "separate" to accommodate the radius.
   * The method leverages helper methods to conditionally add styles based on present attributes.
   *
   * @return a string representing the CSS styles for the inner table
   */
  private String buildInnerTableStyle() {
    Map<String, String> styles = new LinkedHashMap<>();
    addIfPresent(styles, "background-color", "inner-background-color");
    // Inner border properties
    addBorderStyles(
      styles,
      "inner-border",
      "inner-border-bottom",
      "inner-border-left",
      "inner-border-right",
      "inner-border-top");
    String innerRadius = getAttribute("inner-border-radius", "");
    if (!innerRadius.isEmpty()) {
      styles.put("border-radius", innerRadius);
      styles.put("border-collapse", "separate");
    }
    return buildStyle(styles);
  }

  /**
   * Builds a CSS style map for a table with no gutter in an MJML column.
   * The method includes styles for background color, borders, border radius,
   * and vertical alignment. If a border radius is applied, the style map also
   * includes the `border-collapse` property set to "separate".
   * Leverages helper methods to add styles conditionally based on attribute presence.
   *
   * @return a map containing the CSS styles for a table with no gutter
   */
  private Map<String, String> buildNoGutterTableStyle() {
    Map<String, String> styles = new LinkedHashMap<>();
    addIfPresent(styles, "background-color");
    addBorderStyles(styles, "border", "border-bottom", "border-left", "border-right", "border-top");
    addIfPresent(styles, "border-radius");
    styles.put("vertical-align", getAttribute("vertical-align", "top"));
    if (hasBorderRadius()) {
      styles.put("border-collapse", "separate");
    }
    return styles;
  }

  /**
   * Constructs a CSS style string for a table cell (`<td>`) in an MJML column based on the attributes of the provided
   * child component. Styles include background color, font size, padding, and content word-break properties. Individual
   * padding attributes (top, right, bottom, left) are included if specified.
   *
   * @param childComponent the child component from which to retrieve attribute values for constructing the CSS styles
   * @return a string representing the CSS styles for the `<td>` element
   */
  private String buildTdStyle(BodyComponent childComponent) {
    Map<String, String> styles = new LinkedHashMap<>();

    // container-background-color goes first (as "background")
    String containerBg = childComponent.getAttribute("container-background-color", "");
    if (!containerBg.isEmpty()) {
      styles.put("background", containerBg);
    }

    // font-size:0px is always first after background
    styles.put("font-size", "0px");

    String padding = childComponent.getAttribute("padding", "");
    if (!padding.isEmpty()) {
      styles.put("padding", padding);
    }

    // Also check individual padding attributes
    String pt = childComponent.getAttribute("padding-top", "");
    String pr = childComponent.getAttribute("padding-right", "");
    String pb = childComponent.getAttribute("padding-bottom", "");
    String pl = childComponent.getAttribute("padding-left", "");

    if (!pt.isEmpty()) {
      styles.put("padding-top", pt);
    }
    if (!pr.isEmpty()) {
      styles.put("padding-right", pr);
    }
    if (!pb.isEmpty()) {
      styles.put("padding-bottom", pb);
    }
    if (!pl.isEmpty()) {
      styles.put("padding-left", pl);
    }

    // Word-break for content
    styles.put("word-break", "break-word");

    return buildStyle(styles);
  }
}
