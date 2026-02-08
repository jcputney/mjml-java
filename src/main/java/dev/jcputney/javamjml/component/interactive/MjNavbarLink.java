package dev.jcputney.javamjml.component.interactive;

import dev.jcputney.javamjml.component.BodyComponent;
import dev.jcputney.javamjml.context.GlobalContext;
import dev.jcputney.javamjml.context.RenderContext;
import dev.jcputney.javamjml.parser.MjmlNode;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A single link in a navbar ({@code <mj-navbar-link>}).
 * Renders a plain anchor tag ({@code <a>}) with text styling.
 * The parent {@link MjNavbar} wraps links with MSO table conditionals.
 */
public class MjNavbarLink extends BodyComponent {

  private static final Map<String, String> DEFAULTS = Map.ofEntries(
      Map.entry("color", "#000000"),
      Map.entry("font-family", "Ubuntu, Helvetica, Arial, sans-serif"),
      Map.entry("font-size", "13px"),
      Map.entry("font-style", ""),
      Map.entry("font-weight", ""),
      Map.entry("href", "#"),
      Map.entry("letter-spacing", ""),
      Map.entry("line-height", "22px"),
      Map.entry("padding", "15px 10px"),
      Map.entry("padding-bottom", ""),
      Map.entry("padding-left", ""),
      Map.entry("padding-right", ""),
      Map.entry("padding-top", ""),
      Map.entry("rel", ""),
      Map.entry("target", ""),
      Map.entry("text-decoration", "none"),
      Map.entry("text-transform", "uppercase")
  );

  public MjNavbarLink(MjmlNode node, GlobalContext globalContext,
      RenderContext renderContext) {
    super(node, globalContext, renderContext);
  }

  @Override
  public String getTagName() {
    return "mj-navbar-link";
  }

  @Override
  public Map<String, String> getDefaultAttributes() {
    return DEFAULTS;
  }

  @Override
  public String render() {
    String href = getAttribute("href", "#");
    String color = getAttribute("color", "#000000");
    String fontFamily = getAttribute("font-family", "Ubuntu, Helvetica, Arial, sans-serif");
    String fontSize = getAttribute("font-size", "13px");
    String fontStyle = getAttribute("font-style", "");
    String fontWeight = getAttribute("font-weight", "");
    String letterSpacing = getAttribute("letter-spacing", "");
    String lineHeight = getAttribute("line-height", "22px");
    String textDecoration = getAttribute("text-decoration", "none");
    String textTransform = getAttribute("text-transform", "uppercase");
    String padding = getAttribute("padding", "15px 10px");

    Map<String, String> anchorStyles = new LinkedHashMap<>();
    anchorStyles.put("display", "inline-block");
    anchorStyles.put("color", color);
    anchorStyles.put("font-family", fontFamily);
    anchorStyles.put("font-size", fontSize);
    if (!fontStyle.isEmpty()) {
      anchorStyles.put("font-style", fontStyle);
    }
    if (!fontWeight.isEmpty()) {
      anchorStyles.put("font-weight", fontWeight);
    }
    if (!letterSpacing.isEmpty()) {
      anchorStyles.put("letter-spacing", letterSpacing);
    }
    anchorStyles.put("line-height", lineHeight);
    anchorStyles.put("text-decoration", textDecoration);
    anchorStyles.put("text-transform", textTransform);
    anchorStyles.put("padding", padding);

    StringBuilder sb = new StringBuilder();
    sb.append("<a class=\"mj-link\" href=\"").append(href).append("\"");
    sb.append(" target=\"_blank\"");
    sb.append(" style=\"").append(buildStyle(anchorStyles)).append("\">");
    // Space before and after text content, matching official MJML output
    sb.append(" ").append(node.getInnerHtml().trim()).append(" ");
    sb.append("</a>");

    return sb.toString();
  }
}
