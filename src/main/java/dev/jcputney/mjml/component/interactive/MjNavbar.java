package dev.jcputney.mjml.component.interactive;

import dev.jcputney.mjml.component.BaseComponent;
import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.component.ComponentRegistry;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import dev.jcputney.mjml.render.DefaultFontRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The navbar component ({@code <mj-navbar>}).
 * Renders a horizontal navigation bar with anchor links and MSO conditional
 * table wrappers. When the {@code hamburger} attribute is set, a CSS checkbox
 * hack is used to show a mobile hamburger menu that expands/collapses the
 * navigation links on small screens.
 */
public class MjNavbar extends BodyComponent {

  private static final Map<String, String> DEFAULTS = Map.ofEntries(
      Map.entry("align", "center"),
      Map.entry("hamburger", ""),
      Map.entry("ico-align", "center"),
      Map.entry("ico-close", "999999"),
      Map.entry("ico-color", "000000"),
      Map.entry("ico-font-family", "Ubuntu, Helvetica, Arial, sans-serif"),
      Map.entry("ico-font-size", "30px"),
      Map.entry("ico-line-height", "30px"),
      Map.entry("ico-open", "999999"),
      Map.entry("ico-padding", "10px"),
      Map.entry("ico-padding-bottom", "10px"),
      Map.entry("ico-padding-left", "10px"),
      Map.entry("ico-padding-right", "10px"),
      Map.entry("ico-padding-top", "10px"),
      Map.entry("ico-text-font-size", "26px"),
      Map.entry("ico-text-transform", "uppercase")
  );

  private final ComponentRegistry registry;

  public MjNavbar(MjmlNode node, GlobalContext globalContext, RenderContext renderContext,
      ComponentRegistry registry) {
    super(node, globalContext, renderContext);
    this.registry = registry;
  }

  @Override
  public String getTagName() {
    return "mj-navbar";
  }

  @Override
  public Map<String, String> getDefaultAttributes() {
    return DEFAULTS;
  }

  @Override
  public String render() {
    String hamburger = getAttribute("hamburger", "");
    boolean hasHamburger = "hamburger".equals(hamburger);

    if (hasHamburger) {
      globalContext.addComponentStyle(buildHamburgerCss());
      // Register the hamburger icon font (ico-font-family may use a web font like Ubuntu)
      String icoFontFamily = getAttribute("ico-font-family",
          "Ubuntu, Helvetica, Arial, sans-serif");
      DefaultFontRegistry.registerUsedFonts(icoFontFamily, globalContext);
    }

    // Collect rendered links and their paddings from child mj-navbar-link nodes
    List<MjmlNode> linkNodes = node.getChildrenByTag("mj-navbar-link");
    List<String> renderedLinks = new ArrayList<>();
    List<String> linkPaddings = new ArrayList<>();

    for (int i = 0; i < linkNodes.size(); i++) {
      MjmlNode linkNode = linkNodes.get(i);
      RenderContext childContext = renderContext.withPosition(
          i, i == 0, i == linkNodes.size() - 1);
      BaseComponent component = registry.createComponent(
          linkNode, globalContext, childContext);
      if (component instanceof MjNavbarLink navbarLink) {
        renderedLinks.add(navbarLink.render());
        linkPaddings.add(navbarLink.getAttribute("padding", "15px 10px"));
      }
    }

    StringBuilder sb = new StringBuilder();
    String uniqueId = renderContext.nextUniqueId("navbar");

    if (hasHamburger) {
      // Checkbox input for hamburger toggle (non-MSO only)
      sb.append("<!--[if !mso]><!-->");
      sb.append("<input type=\"checkbox\" id=\"").append(uniqueId)
          .append("\" class=\"mj-menu-checkbox\"")
          .append(" style=\"display:none !important; max-height:0; visibility:hidden;\"")
          .append(" />");
      sb.append("<!--<![endif]-->\n");

      // Menu trigger div
      renderHamburgerTrigger(sb, uniqueId);
    }

    // Inline links container
    sb.append("<div class=\"mj-inline-links\" style=\"\">\n");

    // MSO table wrapper around all links
    sb.append("<!--[if mso | IE]>");
    sb.append("<table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\"><tr>");
    if (!linkPaddings.isEmpty()) {
      sb.append("<td style=\"padding:").append(linkPaddings.get(0))
          .append(";\" class=\"\" >");
    }
    sb.append("<![endif]-->\n");

    // Render links with MSO td separators between them
    for (int i = 0; i < renderedLinks.size(); i++) {
      sb.append(renderedLinks.get(i)).append("\n");

      if (i < renderedLinks.size() - 1) {
        // MSO separator between adjacent links
        sb.append("<!--[if mso | IE]></td><td style=\"padding:")
            .append(linkPaddings.get(i + 1))
            .append(";\" class=\"\" ><![endif]-->\n");
      }
    }

    // MSO table close
    sb.append("<!--[if mso | IE]></td></tr></table><![endif]-->\n");
    sb.append("</div>\n");

    return sb.toString();
  }

  private void renderHamburgerTrigger(StringBuilder sb, String uniqueId) {
    String icoColor = getAttribute("ico-color", "000000");
    String icoFontFamily = getAttribute("ico-font-family",
        "Ubuntu, Helvetica, Arial, sans-serif");
    String icoFontSize = getAttribute("ico-font-size", "30px");
    String icoLineHeight = getAttribute("ico-line-height", "30px");
    String icoPadding = getAttribute("ico-padding", "10px");
    String icoTextTransform = getAttribute("ico-text-transform", "uppercase");
    String icoAlign = getAttribute("ico-align", "center");

    // Ensure color has exactly one # prefix
    String colorValue = icoColor.startsWith("#") ? icoColor : "#" + icoColor;

    sb.append("<div class=\"mj-menu-trigger\" style=\"");
    sb.append(buildStyle(orderedMap(
        "display", "none",
        "max-height", "0px",
        "max-width", "0px",
        "font-size", "0px",
        "overflow", "hidden"
    )));
    sb.append("\">\n");

    // Label with mj-menu-label class
    sb.append("<label for=\"").append(uniqueId)
        .append("\" class=\"mj-menu-label\" style=\"");
    sb.append(buildStyle(orderedMap(
        "display", "block",
        "cursor", "pointer",
        "mso-hide", "all",
        "-moz-user-select", "none",
        "user-select", "none",
        "color", colorValue,
        "font-size", icoFontSize,
        "font-family", icoFontFamily,
        "text-transform", icoTextTransform,
        "text-decoration", "none",
        "line-height", icoLineHeight,
        "padding", icoPadding
    )));
    sb.append("\" align=\"").append(icoAlign).append("\">\n");

    // Open icon (hamburger)
    sb.append("<span class=\"mj-menu-icon-open\" style=\"mso-hide:all;\"> &#9776; </span>\n");
    // Close icon (X)
    sb.append("<span class=\"mj-menu-icon-close\" style=\"display:none;mso-hide:all;\"> &#10005; </span>\n");

    sb.append("</label>\n");
    sb.append("</div>\n");
  }

  private String buildHamburgerCss() {
    int breakpoint = globalContext.getBreakpointPx() - 1;
    return "noinput.mj-menu-checkbox {\n"
        + "  display: block !important;\n"
        + "  max-height: none !important;\n"
        + "  visibility: visible !important;\n"
        + "}\n"
        + "\n"
        + "@media only screen and (max-width:" + breakpoint + "px) {\n"
        + "  .mj-menu-checkbox[type=\"checkbox\"]~.mj-inline-links {\n"
        + "    display: none !important;\n"
        + "  }\n"
        + "\n"
        + "  .mj-menu-checkbox[type=\"checkbox\"]:checked~.mj-inline-links,\n"
        + "  .mj-menu-checkbox[type=\"checkbox\"]~.mj-menu-trigger {\n"
        + "    display: block !important;\n"
        + "    max-width: none !important;\n"
        + "    max-height: none !important;\n"
        + "    font-size: inherit !important;\n"
        + "  }\n"
        + "\n"
        + "  .mj-menu-checkbox[type=\"checkbox\"]~.mj-inline-links>a {\n"
        + "    display: block !important;\n"
        + "  }\n"
        + "\n"
        + "  .mj-menu-checkbox[type=\"checkbox\"]:checked~.mj-menu-trigger .mj-menu-icon-close {\n"
        + "    display: block !important;\n"
        + "  }\n"
        + "\n"
        + "  .mj-menu-checkbox[type=\"checkbox\"]:checked~.mj-menu-trigger .mj-menu-icon-open {\n"
        + "    display: none !important;\n"
        + "  }\n"
        + "}\n";
  }
}
