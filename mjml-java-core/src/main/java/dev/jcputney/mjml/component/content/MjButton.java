
package dev.jcputney.mjml.component.content;

import static dev.jcputney.mjml.util.CssUnitParser.WHITESPACE;
import static dev.jcputney.mjml.util.HtmlBuilder.attrs;

import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import dev.jcputney.mjml.util.CssUnitParser;
import dev.jcputney.mjml.util.HtmlBuilder;
import java.util.LinkedHashMap;
import java.util.Map;
import static dev.jcputney.mjml.util.HtmlBuilder.unsortedAttrs;

/**
 * The button component (&lt;mj-button&gt;). Renders an anchor styled as a button with configurable
 * background color, border radius, font styles, and padding.
 */
public class MjButton extends BodyComponent {

  private static final Map<String, String> DEFAULTS = Map.ofEntries(
    Map.entry("align", "center"),
    Map.entry("background-color", "#414141"),
    Map.entry("border", "none"),
    Map.entry("border-bottom", ""),
    Map.entry("border-left", ""),
    Map.entry("border-radius", "3px"),
    Map.entry("border-right", ""),
    Map.entry("border-top", ""),
    Map.entry("color", "#ffffff"),
    Map.entry("container-background-color", ""),
    Map.entry("font-family", "Ubuntu, Helvetica, Arial, sans-serif"),
    Map.entry("font-size", "13px"),
    Map.entry("font-style", ""),
    Map.entry("font-weight", "normal"),
    Map.entry("height", ""),
    Map.entry("href", ""),
    Map.entry("inner-padding", "10px 25px"),
    Map.entry("line-height", "120%"),
    Map.entry("letter-spacing", ""),
    Map.entry("name", ""),
    Map.entry("padding", "10px 25px"),
    Map.entry("rel", ""),
    Map.entry("title", ""),
    Map.entry("text-align", "center"),
    Map.entry("text-decoration", "none"),
    Map.entry("text-transform", "none"),
    Map.entry("target", "_blank"),
    Map.entry("vertical-align", "middle"),
    Map.entry("width", ""));

  public MjButton(MjmlNode node, GlobalContext globalContext, RenderContext renderContext) {
    super(node, globalContext, renderContext);
  }

  @Override
  public String getTagName() {
    return "mj-button";
  }

  @Override
  public Map<String, String> getDefaultAttributes() {
    return DEFAULTS;
  }

  @Override
  public String render() {
    String backgroundColor = getAttribute("background-color", "#414141");
    String borderRadius = getAttribute("border-radius", "3px");
    String href = sanitizeHref(getAttribute("href", ""));
    boolean hasHref = !href.isEmpty();
    String innerPadding = getAttribute("inner-padding", "10px 25px");
    String verticalAlign = getAttribute("vertical-align", "middle");
    Map<String, String> tdStyles = new LinkedHashMap<>();
    tdStyles.put("border", getAttribute("border", "none"));
    addBorderStyles(tdStyles, "border-bottom", "border-left", "border-right", "border-top");
    tdStyles.put("border-radius", borderRadius);
    tdStyles.put("cursor", "auto");
    addIfPresent(tdStyles, "font-style");
    tdStyles.put("mso-padding-alt", innerPadding);
    tdStyles.put("background", backgroundColor);

    String innerTableStyle = buildStyle(tdStyles);

    Map<String, String> anchorStyles = new LinkedHashMap<>();
    anchorStyles.put("display", "inline-block");

    String widthAttr = getAttribute("width", "");
    if (!widthAttr.isEmpty() && !widthAttr.endsWith("%")) {
      double totalWidth = parseWidth(widthAttr);
      double hPadding = calculateHorizontalPadding(innerPadding);
      int innerWidth = (int) (totalWidth - hPadding);
      anchorStyles.put("width", innerWidth + "px");
    }

    anchorStyles.put("background", backgroundColor);
    anchorStyles.put("color", getAttribute("color", "#ffffff"));
    anchorStyles.put("font-family", getAttribute("font-family"));
    anchorStyles.put("font-size", getAttribute("font-size", "13px"));
    addIfPresent(anchorStyles, "font-style");
    anchorStyles.put("font-weight", getAttribute("font-weight", "normal"));
    anchorStyles.put("line-height", getAttribute("line-height", "120%"));
    addIfPresent(anchorStyles, "letter-spacing");
    anchorStyles.put("margin", "0");
    anchorStyles.put("text-decoration", getAttribute("text-decoration", "none"));
    anchorStyles.put("text-transform", getAttribute("text-transform", "none"));
    anchorStyles.put("padding", innerPadding);
    anchorStyles.put("mso-padding-alt", "0px");
    anchorStyles.put("border-radius", borderRadius);

    String anchorStyle = buildStyle(anchorStyles);

    Map<String, String> outerTableStyles = new LinkedHashMap<>();
    outerTableStyles.put("border-collapse", "separate");
    if (!widthAttr.isEmpty()) {
      outerTableStyles.put("width", widthAttr);
    }
    outerTableStyles.put("line-height", "100%");

    var tableAttrMap = new LinkedHashMap<>(HtmlBuilder.PRESENTATION_TABLE_ATTRS);
    tableAttrMap.put("style", buildStyle(outerTableStyles));

    var tdAttrMap = new LinkedHashMap<String, String>();
    tdAttrMap.put("align", "center");
    tdAttrMap.put("bgcolor", escapeAttr(backgroundColor));
    tdAttrMap.put("role", "presentation");
    tdAttrMap.put("style", innerTableStyle);
    tdAttrMap.put("valign", escapeAttr(verticalAlign));

    String buttonContent =
      WHITESPACE.matcher(node.getInnerHtml()).replaceAll(" ").trim();

    HtmlBuilder html = new HtmlBuilder(24);
    html.wrap("table", attrs(tableAttrMap),
      () -> html.wrap("tbody",
        () -> html.wrap("tr",
          () -> html.wrap("td", attrs(tdAttrMap), () -> {
            if (hasHref) {
              var anchorAttrMap = new LinkedHashMap<String, String>();
              anchorAttrMap.put("href", escapeHref(href));
              anchorAttrMap.put("rel", escapeAttr(getAttribute("rel", "")));
              anchorAttrMap.put("style", anchorStyle);
              anchorAttrMap.put("target", escapeAttr(getAttribute("target", "_blank")));
              html.openInline("a", attrs(anchorAttrMap))
                .text(" " + sanitizeContent(buttonContent) + " ")
                .closeInlineLn("a");
            } else {
              html.openInline("p", attrs("style", anchorStyle))
                .text(" " + sanitizeContent(buttonContent) + " ")
                .closeInlineLn("p");
            }
          }))));

    return html.toString();
  }

  private double calculateHorizontalPadding(String padding) {
    double[] pad = CssUnitParser.parseShorthand(padding);
    return pad[1] + pad[3];
  }
}
