package dev.jcputney.mjml.component.content;

import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import java.util.Map;

/**
 * The divider component (&lt;mj-divider&gt;). Renders a horizontal rule using a styled paragraph
 * element with a border-top. Includes MSO conditional comments for Outlook compatibility.
 */
public class MjDivider extends BodyComponent {

  private static final Map<String, String> DEFAULTS =
      Map.ofEntries(
          Map.entry("align", "center"),
          Map.entry("border-color", "#000000"),
          Map.entry("border-style", "solid"),
          Map.entry("border-width", "4px"),
          Map.entry("container-background-color", ""),
          Map.entry("padding", "10px 25px"),
          Map.entry("width", "100%"));

  /**
   * Creates a new MjDivider component.
   *
   * @param node the parsed MJML node for this component
   * @param globalContext the global rendering context
   * @param renderContext the current render context
   */
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

    // Compute margin based on alignment
    String align = getAttribute("align", "center");
    String margin =
        switch (align) {
          case "left" -> "0px";
          case "right" -> "0px 0px 0px auto";
          default -> "0px auto"; // center
        };

    String dividerStyle =
        buildStyle(
            orderedMap(
                "border-top", borderTop,
                "font-size", "1px",
                "margin", margin,
                "width", width));

    // Standard divider <p> FIRST

    String sb =
        "                        <p style=\""
            + dividerStyle
            + "\">\n"
            + "                        </p>\n"
            +

            // MSO conditional AFTER
            "                        <!--[if mso | IE]><table align=\""
            + align
            + "\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\""
            + " style=\""
            + buildStyle(
                orderedMap(
                    "border-top",
                    borderTop,
                    "font-size",
                    "1px",
                    "margin",
                    margin,
                    "width",
                    msoWidth + "px"))
            + "\""
            + " role=\"presentation\" width=\""
            + msoWidth
            + "px\""
            + " ><tr><td style=\"height:0;line-height:0;\"> &nbsp;\n"
            + "</td></tr></table><![endif]-->";

    return sb;
  }
}
