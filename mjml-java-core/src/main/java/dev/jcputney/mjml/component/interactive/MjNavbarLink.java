package dev.jcputney.mjml.component.interactive;

import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A single link in a navbar ({@code <mj-navbar-link>}). Renders a plain anchor tag ({@code <a>})
 * with text styling. The parent {@link MjNavbar} wraps links with MSO table conditionals.
 */
public class MjNavbarLink extends BodyComponent {

  private static final Map<String, String> DEFAULTS =
      Map.ofEntries(
          Map.entry("color", "#000000"),
          Map.entry("font-family", "Ubuntu, Helvetica, Arial, sans-serif"),
          Map.entry("font-size", "13px"),
          Map.entry("font-style", ""),
          Map.entry("font-weight", "normal"),
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
          Map.entry("text-transform", "uppercase"));

  /**
   * Creates a new MjNavbarLink component.
   *
   * @param node the parsed MJML node for this component
   * @param globalContext the global rendering context
   * @param renderContext the current render context
   */
  public MjNavbarLink(MjmlNode node, GlobalContext globalContext, RenderContext renderContext) {
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
    String rawHref = getAttribute("href", "#");
    // Prepend base-url from parent mj-navbar if present
    String baseUrl = resolveParentAttr("base-url", "");
    if (!baseUrl.isEmpty() && !rawHref.startsWith("http") && !rawHref.startsWith("#")) {
      rawHref = baseUrl + rawHref;
    }
    String href = sanitizeHref(rawHref);
    String padding = getAttribute("padding", "15px 10px");

    Map<String, String> anchorStyles = new LinkedHashMap<>();
    anchorStyles.put("display", "inline-block");
    anchorStyles.put("color", getAttribute("color", "#000000"));
    anchorStyles.put(
        "font-family", getAttribute("font-family", "Ubuntu, Helvetica, Arial, sans-serif"));
    anchorStyles.put("font-size", getAttribute("font-size", "13px"));
    addIfPresent(anchorStyles, "font-style");
    anchorStyles.put("font-weight", getAttribute("font-weight", "normal"));
    addIfPresent(anchorStyles, "letter-spacing");
    anchorStyles.put("line-height", getAttribute("line-height", "22px"));
    anchorStyles.put("text-decoration", getAttribute("text-decoration", "none"));
    anchorStyles.put("text-transform", getAttribute("text-transform", "uppercase"));
    anchorStyles.put("padding", padding);

    StringBuilder sb = new StringBuilder();
    sb.append("<a class=\"mj-link\" href=\"").append(escapeHref(href)).append("\"");
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
    sb.append(" ").append(sanitizeContent(node.getInnerHtml().trim())).append(" ");
    sb.append("</a>");

    return sb.toString();
  }

  /**
   * Resolves an attribute from the parent mj-navbar node.
   *
   * @param name the attribute name
   * @param fallback the default value if not found
   * @return the resolved attribute value
   */
  private String resolveParentAttr(String name, String fallback) {
    MjmlNode parent = node.getParent();
    if (parent != null) {
      String val = parent.getAttribute(name);
      if (val != null && !val.isEmpty()) {
        return val;
      }
    }
    return fallback;
  }
}
