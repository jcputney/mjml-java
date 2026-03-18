
package dev.jcputney.mjml.component.body;

import static dev.jcputney.mjml.util.HtmlBuilder.attrIf;
import static dev.jcputney.mjml.util.HtmlBuilder.attrs;

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
    boolean hasGutter = hasGutter();
    boolean hasBorder = hasBorderRadius();

    String cssClass = getAttribute("css-class", "");
    String divClass =
      responsiveClass + " mj-outlook-group-fix" + (cssClass.isEmpty() ? "" : " " + escapeAttr(cssClass));

    HtmlBuilder html = new HtmlBuilder();
    html.open("div", attrs("class", divClass, "style", buildOuterStyle()));

    if (hasGutter) {
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
          "width",
          "100%")
          + (hasBorder ? " style=\"border-collapse:separate;\"" : ""));
      html.open("tbody");
      html.open("tr");
      html.open("td", attrs("style", buildGutterTdStyle()));
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
          buildInnerTableStyle(),
          "width",
          "100%"));
      html.open("tbody");

      renderContentChildren(html, columnWidth);

      html.close("tbody");
      html.close("table");
      html.close("td");
      html.close("tr");
      html.close("tbody");
      html.close("table");
    } else {
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
          buildNoGutterTableStyle(),
          "width",
          "100%"));
      html.open("tbody");

      renderContentChildren(html, columnWidth);

      html.close("tbody");
      html.close("table");
    }

    html.close("div");

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

  private boolean hasBorderRadius() {
    String borderRadius = getAttribute("border-radius", "");
    return !borderRadius.isEmpty();
  }

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
        if ("mj-raw".equals(child.getTagName())) {
          html.rawVerbatim(bodyComponent.render());
          continue;
        }

        String align = bodyComponent.getAttribute("align", "");
        String tdCssClass = bodyComponent.getAttribute("css-class", "");
        String tdStyle = buildTdStyle(bodyComponent);

        html.open("tr");
        html.open(
          "td",
          attrIf("align", escapeAttr(align))
            + attrIf("class", escapeAttr(tdCssClass))
            + attrIf("style", tdStyle));
        html.rawVerbatim(bodyComponent.render());
        html.newline();
        html.close("td");
        html.close("tr");
      }
    }
  }

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
   * Style for the outer gutter td (when hasGutter=true). Contains: background-color, padding,
   * vertical-align.
   */
  private String buildGutterTdStyle() {
    Map<String, String> styles = new LinkedHashMap<>();
    addIfPresent(styles, "background-color");
    // Border on gutter td
    addBorderStyles(styles, "border", "border-bottom", "border-left", "border-right", "border-top");
    addIfPresent(styles, "border-radius");
    styles.put("vertical-align", getAttribute("vertical-align", "top"));
    // border-collapse:separate when border is present
    if (hasBorderRadius()) {
      styles.put("border-collapse", "separate");
    }
    // Padding on the gutter td
    addIfPresent(styles, "padding");
    addIfPresent(styles, "padding-bottom");
    addIfPresent(styles, "padding-left");
    addIfPresent(styles, "padding-right");
    addIfPresent(styles, "padding-top");
    return buildStyle(styles);
  }

  /**
   * Style for the inner table (when hasGutter=true). Contains: inner-background-color,
   * inner-border-*, border-radius.
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
   * Style for the single table (when hasGutter=false). Contains: background-color, border-*,
   * border-radius, vertical-align.
   */
  private String buildNoGutterTableStyle() {
    Map<String, String> styles = new LinkedHashMap<>();
    addIfPresent(styles, "background-color");
    addBorderStyles(styles, "border", "border-bottom", "border-left", "border-right", "border-top");
    addIfPresent(styles, "border-radius");
    styles.put("vertical-align", getAttribute("vertical-align", "top"));
    if (hasBorderRadius()) {
      styles.put("border-collapse", "separate");
    }
    return buildStyle(styles);
  }

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
