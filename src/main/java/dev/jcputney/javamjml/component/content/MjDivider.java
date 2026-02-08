package dev.jcputney.javamjml.component.content;

import dev.jcputney.javamjml.component.BodyComponent;
import dev.jcputney.javamjml.context.GlobalContext;
import dev.jcputney.javamjml.context.RenderContext;
import dev.jcputney.javamjml.parser.MjmlNode;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The divider component (&lt;mj-divider&gt;).
 * Renders a horizontal rule using a styled paragraph element with a border-top.
 * Includes MSO conditional comments for Outlook compatibility.
 */
public class MjDivider extends BodyComponent {

  private static final Map<String, String> DEFAULTS = Map.ofEntries(
      Map.entry("align", "center"),
      Map.entry("border-color", "#000000"),
      Map.entry("border-style", "solid"),
      Map.entry("border-width", "4px"),
      Map.entry("container-background-color", ""),
      Map.entry("padding", "10px 25px"),
      Map.entry("width", "100%")
  );

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
    // The MSO width is based on the container width minus the divider's own horizontal padding
    double containerWidth = renderContext.getContainerWidth();
    double paddingLeft = getBoxModel().paddingLeft();
    double paddingRight = getBoxModel().paddingRight();
    double availableWidth = containerWidth - paddingLeft - paddingRight;
    int msoWidth;
    if (width.endsWith("%")) {
      double pct = Double.parseDouble(width.replace("%", ""));
      msoWidth = (int) (availableWidth * pct / 100.0);
    } else {
      msoWidth = (int) parseWidth(width);
    }

    String dividerStyle = buildStyle(orderedMap(
        "border-top", borderTop,
        "font-size", "1px",
        "margin", "0px auto",
        "width", width
    ));

    StringBuilder sb = new StringBuilder();

    // Standard divider <p> FIRST
    sb.append("                        <p style=\"").append(dividerStyle).append("\">\n");
    sb.append("                        </p>\n");

    // MSO conditional AFTER
    sb.append("                        <!--[if mso | IE]><table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"");
    sb.append(" style=\"").append(buildStyle(orderedMap(
        "border-top", borderTop,
        "font-size", "1px",
        "margin", "0px auto",
        "width", msoWidth + "px"
    ))).append("\"");
    sb.append(" role=\"presentation\" width=\"").append(msoWidth).append("px\"");
    sb.append(" ><tr><td style=\"height:0;line-height:0;\"> &nbsp;\n");
    sb.append("</td></tr></table><![endif]-->");

    return sb.toString();
  }
}
