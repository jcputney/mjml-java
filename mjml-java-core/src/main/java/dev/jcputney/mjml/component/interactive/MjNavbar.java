package dev.jcputney.mjml.component.interactive;

import static dev.jcputney.mjml.util.HtmlBuilder.attrs;

import dev.jcputney.mjml.component.BaseComponent;
import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.component.ComponentRegistry;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import dev.jcputney.mjml.render.DefaultFontRegistry;
import dev.jcputney.mjml.util.HtmlBuilder;
import dev.jcputney.mjml.util.MsoHelper;
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

    HtmlBuilder html = new HtmlBuilder();
    String uniqueId = renderContext.nextUniqueId("navbar");

    if (hasHamburger) {
      html.rawVerbatim(
          "<!--[if !mso]><!-->"
              + "<input"
              + attrs("type", "checkbox", "id", uniqueId, "class", "mj-menu-checkbox")
              + " style=\"display:none !important; max-height:0; visibility:hidden;\" />"
              + "<!--<![endif]-->\n");
      renderHamburgerTrigger(html, uniqueId);
    }

    // Inline links container — style="" must be present even when empty
    html.rawVerbatim("<div class=\"mj-inline-links\" style=\"\">\n");

    // MSO table wrapper around all links
    html.mso(
        () -> {
          html.rawVerbatim(
              "<table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\"><tr>");
          if (!linkPaddings.isEmpty()) {
            html.rawVerbatim(
                "<td style=\"padding:" + linkPaddings.get(0) + ";\" class=\"\" >");
          }
        });

    for (int i = 0; i < renderedLinks.size(); i++) {
      html.rawVerbatim(renderedLinks.get(i) + "\n");

      if (i < renderedLinks.size() - 1) {
        html.mso(
            "</td><td style=\"padding:" + linkPaddings.get(i + 1) + ";\" class=\"\" >");
      }
    }

    html.mso(MsoHelper.msoTableClosing());
    html.rawVerbatim("</div>\n");

    return html.toString();
  }

  private void renderHamburgerTrigger(HtmlBuilder html, String uniqueId) {
    String icoColor = getAttribute("ico-color", "000000");
    String icoFontFamily = getAttribute("ico-font-family", "Ubuntu, Helvetica, Arial, sans-serif");
    String icoFontSize = getAttribute("ico-font-size", "30px");
    String icoLineHeight = getAttribute("ico-line-height", "30px");
    String icoPadding = getAttribute("ico-padding", "10px");
    String icoTextTransform = getAttribute("ico-text-transform", "uppercase");
    String icoAlign = getAttribute("ico-align", "center");

    String colorValue = icoColor.startsWith("#") ? icoColor : "#" + icoColor;

    String triggerStyle =
        buildStyle(
            orderedMap(
                "display", "none",
                "max-height", "0px",
                "max-width", "0px",
                "font-size", "0px",
                "overflow", "hidden"));
    html.open("div", attrs("class", "mj-menu-trigger", "style", triggerStyle));

    String labelStyle =
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
                icoPadding));
    html.open(
        "label",
        attrs("for", uniqueId, "class", "mj-menu-label", "style", labelStyle)
            + " align=\""
            + icoAlign
            + "\"");

    String icoOpen = encodeNonAscii(getAttribute("ico-open", "&#9776;"));
    html.rawVerbatim(
        "<span class=\"mj-menu-icon-open\" style=\"mso-hide:all;\"> " + icoOpen + " </span>\n");
    String icoClose = encodeNonAscii(getAttribute("ico-close", "&#8855;"));
    html.rawVerbatim(
        "<span class=\"mj-menu-icon-close\" style=\"display:none;mso-hide:all;\"> "
            + icoClose
            + " </span>\n");

    html.close("label");
    html.close("div");
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
