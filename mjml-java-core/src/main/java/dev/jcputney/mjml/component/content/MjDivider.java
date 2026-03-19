
package dev.jcputney.mjml.component.content;

import static dev.jcputney.mjml.util.HtmlBuilder.attrs;
import static dev.jcputney.mjml.util.HtmlBuilder.unsortedAttrs;

import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import dev.jcputney.mjml.util.CssUnitParser;
import dev.jcputney.mjml.util.HtmlBuilder;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The divider component (&lt;mj-divider&gt;). Renders a horizontal rule using a styled paragraph
 * element with a border-top. Includes MSO conditional comments for Outlook compatibility.
 */
public class MjDivider extends BodyComponent {

  private static final Map<String, String> DEFAULTS = Map.ofEntries(
    Map.entry("align", "center"),
    Map.entry("border-color", "#000000"),
    Map.entry("border-style", "solid"),
    Map.entry("border-width", "4px"),
    Map.entry("container-background-color", ""),
    Map.entry("padding", "10px 25px"),
    Map.entry("width", "100%"));

  public MjDivider(MjmlNode node, GlobalContext globalContext, RenderContext renderContext) {
    super(node, globalContext, renderContext);
  }

  @Override
  public String getTagName() {
    return "mj-divider";
  }

  @Override
  public Map<String, String> getDefaultAttributes() {
    return DEFAULTS;
  }

  @Override
  public String render() {
    String borderColor = getAttribute("border-color", "#000000");
    String borderStyle = getAttribute("border-style", "solid");
    String borderWidth = getAttribute("border-width", "4px");
    String width = getAttribute("width", "100%");

    // Border-top: style width color (not width style color)
    String borderTop = borderStyle + " " + borderWidth + " " + borderColor;

    // Compute actual pixel width for MSO table
    // Use individual padding overrides if present, falling back to shorthand parsing
    double containerWidth = renderContext.getContainerWidth();
    double paddingLeft = resolveShorthandSide("padding", "padding-left", 3);
    double paddingRight = resolveShorthandSide("padding", "padding-right", 1);
    double availableWidth = containerWidth - paddingLeft - paddingRight;
    int msoWidth;
    if (width.endsWith("%")) {
      double pct = CssUnitParser.parsePx(width.replace("%", ""), 100.0);
      msoWidth = (int) (availableWidth * pct / 100.0);
    } else {
      msoWidth = (int) parseWidth(width);
    }

    // Compute margin based on alignment
    String align = getAttribute("align", "center");
    String margin =
      switch (align) {
        case "left" -> "0px";
        case "right" -> "0px 0px 0px auto";
        default -> "0px auto"; // center
      };

    String dividerStyle = buildStyle(orderedMap(
      "border-top", borderTop,
      "font-size", "1px",
      "margin", margin,
      "width", width));

    HtmlBuilder html = new HtmlBuilder();

    // Standard divider <p>
    html.open("p", attrs("style", dividerStyle));
    html.close("p");

    // MSO conditional table
    String msoStyle = buildStyle(
      orderedMap("border-top", borderTop, "font-size", "1px", "margin", margin, "width", msoWidth + "px"));
    var msoTableAttrs = new LinkedHashMap<String, String>();
    msoTableAttrs.put("align", align);
    msoTableAttrs.put("border", "0");
    msoTableAttrs.put("cellpadding", "0");
    msoTableAttrs.put("cellspacing", "0");
    msoTableAttrs.put("style", msoStyle);
    msoTableAttrs.put("role", "presentation");
    msoTableAttrs.put("width", msoWidth + "px");
    html.mso(
      () -> html.table(unsortedAttrs(msoTableAttrs) + " ",
        () -> html.wrap("tr",
          () -> html.wrap("td", attrs("style", "height:0;line-height:0;"),
            () -> html.text(" &nbsp;\n")
          )
        )
      )
    );

    return html.toString();
  }
}
