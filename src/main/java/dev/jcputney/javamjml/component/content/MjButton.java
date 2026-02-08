package dev.jcputney.javamjml.component.content;

import dev.jcputney.javamjml.component.BodyComponent;
import dev.jcputney.javamjml.context.GlobalContext;
import dev.jcputney.javamjml.context.RenderContext;
import dev.jcputney.javamjml.parser.MjmlNode;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The button component (&lt;mj-button&gt;).
 * Renders an anchor styled as a button with configurable background color,
 * border radius, font styles, and padding.
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
      Map.entry("href", "#"),
      Map.entry("inner-padding", "10px 25px"),
      Map.entry("line-height", "120%"),
      Map.entry("letter-spacing", ""),
      Map.entry("padding", "10px 25px"),
      Map.entry("text-align", ""),
      Map.entry("text-decoration", "none"),
      Map.entry("text-transform", "none"),
      Map.entry("vertical-align", "middle"),
      Map.entry("width", "")
  );

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
    String href = getAttribute("href", "#");
    String innerPadding = getAttribute("inner-padding", "10px 25px");
    String verticalAlign = getAttribute("vertical-align", "middle");
    String fontStyle = getAttribute("font-style", "");

    // Build TD style: border, border-radius, cursor:auto, font-style (if italic),
    // mso-padding-alt, background
    Map<String, String> tdStyles = new LinkedHashMap<>();
    tdStyles.put("border", getAttribute("border", "none"));
    String borderBottom = getAttribute("border-bottom", "");
    if (!borderBottom.isEmpty()) {
      tdStyles.put("border-bottom", borderBottom);
    }
    String borderLeft = getAttribute("border-left", "");
    if (!borderLeft.isEmpty()) {
      tdStyles.put("border-left", borderLeft);
    }
    String borderRight = getAttribute("border-right", "");
    if (!borderRight.isEmpty()) {
      tdStyles.put("border-right", borderRight);
    }
    String borderTop = getAttribute("border-top", "");
    if (!borderTop.isEmpty()) {
      tdStyles.put("border-top", borderTop);
    }
    tdStyles.put("border-radius", borderRadius);
    tdStyles.put("cursor", "auto");
    if (!fontStyle.isEmpty()) {
      tdStyles.put("font-style", fontStyle);
    }
    tdStyles.put("mso-padding-alt", innerPadding);
    tdStyles.put("background", backgroundColor);

    String innerTableStyle = buildStyle(tdStyles);

    // Build anchor style
    Map<String, String> anchorStyles = new LinkedHashMap<>();
    anchorStyles.put("display", "inline-block");

    // If width is set as a pixel value, calculate inner width (width - horizontal inner-padding)
    // Percentage widths do not add a width style to the anchor
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
    if (!fontStyle.isEmpty()) {
      anchorStyles.put("font-style", fontStyle);
    }
    anchorStyles.put("font-weight", getAttribute("font-weight", "normal"));
    anchorStyles.put("line-height", getAttribute("line-height", "120%"));
    String letterSpacing = getAttribute("letter-spacing", "");
    if (!letterSpacing.isEmpty()) {
      anchorStyles.put("letter-spacing", letterSpacing);
    }
    anchorStyles.put("margin", "0");
    anchorStyles.put("text-decoration", getAttribute("text-decoration", "none"));
    anchorStyles.put("text-transform", getAttribute("text-transform", "none"));
    anchorStyles.put("padding", innerPadding);
    anchorStyles.put("mso-padding-alt", "0px");
    anchorStyles.put("border-radius", borderRadius);

    String anchorStyle = buildStyle(anchorStyles);

    // Outer table style
    Map<String, String> outerTableStyles = new LinkedHashMap<>();
    outerTableStyles.put("border-collapse", "separate");
    if (!widthAttr.isEmpty()) {
      outerTableStyles.put("width", widthAttr);
    }
    outerTableStyles.put("line-height", "100%");

    StringBuilder sb = new StringBuilder();

    sb.append("                        <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\"");
    sb.append(" style=\"").append(buildStyle(outerTableStyles)).append("\">\n");
    sb.append("                          <tbody>\n");
    sb.append("                            <tr>\n");
    sb.append("                              <td align=\"center\"");
    sb.append(" bgcolor=\"").append(backgroundColor).append("\"");
    sb.append(" role=\"presentation\"");
    sb.append(" style=\"").append(innerTableStyle).append("\"");
    sb.append(" valign=\"").append(verticalAlign).append("\">\n");

    // Anchor styled as button (no rel attribute)
    sb.append("                                <a href=\"").append(href).append("\"");
    sb.append(" style=\"").append(anchorStyle).append("\"");
    sb.append(" target=\"_blank\">");
    sb.append(" ").append(node.getInnerHtml().replaceAll("\\s+", " ").trim()).append(" ");
    sb.append("</a>\n");

    sb.append("                              </td>\n");
    sb.append("                            </tr>\n");
    sb.append("                          </tbody>\n");
    sb.append("                        </table>");

    return sb.toString();
  }

  private double calculateHorizontalPadding(String padding) {
    String[] parts = padding.trim().split("\\s+");
    if (parts.length == 1) {
      return parseWidth(parts[0]) * 2;
    } else if (parts.length == 2) {
      return parseWidth(parts[1]) * 2;
    } else if (parts.length == 3) {
      return parseWidth(parts[1]) * 2;
    } else if (parts.length == 4) {
      return parseWidth(parts[1]) + parseWidth(parts[3]);
    }
    return 0;
  }
}
