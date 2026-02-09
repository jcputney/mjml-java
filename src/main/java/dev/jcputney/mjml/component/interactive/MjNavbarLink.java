package dev.jcputney.mjml.component.interactive;

import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
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
    String padding = getAttribute("padding", "15px 10px");

    Map<String, String> anchorStyles = new LinkedHashMap<>();
    anchorStyles.put("display", "inline-block");
    anchorStyles.put("color", getAttribute("color", "#000000"));
    anchorStyles.put("font-family", getAttribute("font-family", "Ubuntu, Helvetica, Arial, sans-serif"));
    anchorStyles.put("font-size", getAttribute("font-size", "13px"));
    addIfPresent(anchorStyles, "font-style");
    addIfPresent(anchorStyles, "font-weight");
    addIfPresent(anchorStyles, "letter-spacing");
    anchorStyles.put("line-height", getAttribute("line-height", "22px"));
    anchorStyles.put("text-decoration", getAttribute("text-decoration", "none"));
    anchorStyles.put("text-transform", getAttribute("text-transform", "uppercase"));
    anchorStyles.put("padding", padding);

    StringBuilder sb = new StringBuilder();
    sb.append("<a class=\"mj-link\" href=\"").append(escapeAttr(href)).append("\"");
    String rel = getAttribute("rel", "");
    if (!rel.isEmpty()) {
      sb.append(" rel=\"").append(escapeAttr(rel)).append("\"");
    }
    String target = getAttribute("target", "");
    if (target.isEmpty()) {
      target = "_blank";
    }
    sb.append(" target=\"").append(escapeAttr(target)).append("\"");
    sb.append(" style=\"").append(buildStyle(anchorStyles)).append("\">");
    // Space before and after text content, matching official MJML output
    sb.append(" ").append(node.getInnerHtml().trim()).append(" ");
    sb.append("</a>");

    return sb.toString();
  }
}
