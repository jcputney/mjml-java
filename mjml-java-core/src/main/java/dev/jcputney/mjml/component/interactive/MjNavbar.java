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
 * The navbar component ({@code <mj-navbar>}). Renders a horizontal navigation bar with anchor links
 * and MSO conditional table wrappers. When the {@code hamburger} attribute is set, a CSS checkbox
 * hack is used to show a mobile hamburger menu that expands/collapses the navigation links on small
 * screens.
 */
public class MjNavbar extends BodyComponent {

  private static final Map<String, String> DEFAULTS =
      Map.ofEntries(
          Map.entry("align", "center"),
          Map.entry("base-url", ""),
          Map.entry("hamburger", ""),
          Map.entry("ico-align", "center"),
          Map.entry("ico-close", "&#8855;"),
          Map.entry("ico-color", "000000"),
          Map.entry("ico-font-family", "Ubuntu, Helvetica, Arial, sans-serif"),
          Map.entry("ico-font-size", "30px"),
          Map.entry("ico-line-height", "30px"),
          Map.entry("ico-open", "&#9776;"),
          Map.entry("ico-padding", "10px"),
          Map.entry("ico-padding-bottom", "10px"),
          Map.entry("ico-padding-left", "10px"),
          Map.entry("ico-padding-right", "10px"),
          Map.entry("ico-padding-top", "10px"),
          Map.entry("ico-text-decoration", "none"),
          Map.entry("ico-text-font-size", "26px"),
          Map.entry("ico-text-transform", "uppercase"));

  private final ComponentRegistry registry;

  /**
   * Creates a new MjNavbar component.
   *
   * @param node the parsed MJML node for this component
   * @param globalContext the global rendering context
   * @param renderContext the current render context
   * @param registry the component registry for resolving child components
   */
  public MjNavbar(
      MjmlNode node,
      GlobalContext globalContext,
      RenderContext renderContext,
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
      globalContext.styles().addComponentStyle(buildHamburgerCss());
      // Register the hamburger icon font (ico-font-family may use a web font like Ubuntu)
      String icoFontFamily =
          getAttribute("ico-font-family", "Ubuntu, Helvetica, Arial, sans-serif");
      DefaultFontRegistry.registerUsedFonts(icoFontFamily, globalContext);
    }

    // Collect rendered links and their paddings from child mj-navbar-link nodes
    List<MjmlNode> linkNodes = node.getChildrenByTag("mj-navbar-link");
    List<String> renderedLinks = new ArrayList<>();
    List<String> linkPaddings = new ArrayList<>();

    for (int i = 0; i < linkNodes.size(); i++) {
      MjmlNode linkNode = linkNodes.get(i);
      RenderContext childContext = renderContext.withPosition(i, i == 0, i == linkNodes.size() - 1);
      BaseComponent component = registry.createComponent(linkNode, globalContext, childContext);
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
      sb.append("<input type=\"checkbox\" id=\"")
          .append(uniqueId)
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
    sb.append(
        "<table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\"><tr>");
    if (!linkPaddings.isEmpty()) {
      sb.append("<td style=\"padding:").append(linkPaddings.get(0)).append(";\" class=\"\" >");
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
    String icoFontFamily = getAttribute("ico-font-family", "Ubuntu, Helvetica, Arial, sans-serif");
    String icoFontSize = getAttribute("ico-font-size", "30px");
    String icoLineHeight = getAttribute("ico-line-height", "30px");
    String icoPadding = getAttribute("ico-padding", "10px");
    String icoTextTransform = getAttribute("ico-text-transform", "uppercase");
    String icoAlign = getAttribute("ico-align", "center");

    // Ensure color has exactly one # prefix
    String colorValue = icoColor.startsWith("#") ? icoColor : "#" + icoColor;

    sb.append("<div class=\"mj-menu-trigger\" style=\"");
    sb.append(
        buildStyle(
            orderedMap(
                "display", "none",
                "max-height", "0px",
                "max-width", "0px",
                "font-size", "0px",
                "overflow", "hidden")));
    sb.append("\">\n");

    // Label with mj-menu-label class
    sb.append("<label for=\"").append(uniqueId).append("\" class=\"mj-menu-label\" style=\"");
    sb.append(
        buildStyle(
            orderedMap(
                "display",
                "block",
                "cursor",
                "pointer",
                "mso-hide",
                "all",
                "-moz-user-select",
                "none",
                "user-select",
                "none",
                "color",
                colorValue,
                "font-size",
                icoFontSize,
                "font-family",
                icoFontFamily,
                "text-transform",
                icoTextTransform,
                "text-decoration",
                "none",
                "line-height",
                icoLineHeight,
                "padding",
                icoPadding)));
    sb.append("\" align=\"").append(icoAlign).append("\">\n");

    // Open icon (hamburger) — from ico-open attribute
    // Re-encode non-ASCII chars as HTML entities (XML parser decodes them)
    String icoOpen = encodeNonAscii(getAttribute("ico-open", "&#9776;"));
    sb.append("<span class=\"mj-menu-icon-open\" style=\"mso-hide:all;\"> ")
        .append(icoOpen)
        .append(" </span>\n");
    // Close icon (X) — from ico-close attribute
    String icoClose = encodeNonAscii(getAttribute("ico-close", "&#8855;"));
    sb.append("<span class=\"mj-menu-icon-close\" style=\"display:none;mso-hide:all;\"> ")
        .append(icoClose)
        .append(" </span>\n");

    sb.append("</label>\n");
    sb.append("</div>\n");
  }

  private String buildHamburgerCss() {
    int breakpoint = globalContext.metadata().getBreakpointPx() - 1;
    return """
        noinput.mj-menu-checkbox {
          display: block !important;
          max-height: none !important;
          visibility: visible !important;
        }

        @media only screen and (max-width:%dpx) {
          .mj-menu-checkbox[type="checkbox"]~.mj-inline-links {
            display: none !important;
          }

          .mj-menu-checkbox[type="checkbox"]:checked~.mj-inline-links,
          .mj-menu-checkbox[type="checkbox"]~.mj-menu-trigger {
            display: block !important;
            max-width: none !important;
            max-height: none !important;
            font-size: inherit !important;
          }

          .mj-menu-checkbox[type="checkbox"]~.mj-inline-links>a {
            display: block !important;
          }

          .mj-menu-checkbox[type="checkbox"]:checked~.mj-menu-trigger .mj-menu-icon-close {
            display: block !important;
          }

          .mj-menu-checkbox[type="checkbox"]:checked~.mj-menu-trigger .mj-menu-icon-open {
            display: none !important;
          }
        }
        """
        .formatted(breakpoint);
  }

  /**
   * Encodes non-ASCII characters as HTML numeric entities. The XML parser decodes character
   * references (like {@code &#9776;}) into Unicode characters during parsing. This method
   * re-encodes them to preserve the entity reference form in the HTML output.
   */
  private static String encodeNonAscii(String value) {
    if (value == null) {
      return "";
    }
    // If already an entity reference (from defaults), return as-is
    if (value.contains("&#")) {
      return value;
    }
    StringBuilder sb = new StringBuilder(value.length());
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (c > 127) {
        sb.append("&#").append((int) c).append(';');
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }
}
