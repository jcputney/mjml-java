package dev.jcputney.mjml.component.body;

import dev.jcputney.mjml.component.BaseComponent;
import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.component.ComponentRegistry;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import dev.jcputney.mjml.render.VmlHelper;
import dev.jcputney.mjml.util.BackgroundCssHelper;
import dev.jcputney.mjml.util.BackgroundPositionHelper;
import dev.jcputney.mjml.util.CssBoxModel;
import dev.jcputney.mjml.util.CssUnitParser;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The wrapper component (&lt;mj-wrapper&gt;).
 * Similar to mj-section but wraps multiple sections together,
 * allowing a shared background color/image across sections.
 * Each child section stacks vertically within the wrapper.
 */
public class MjWrapper extends BodyComponent {

  private static final Map<String, String> DEFAULTS = Map.ofEntries(
      Map.entry("background-color", ""),
      Map.entry("background-position", "top center"),
      Map.entry("background-position-x", ""),
      Map.entry("background-position-y", ""),
      Map.entry("background-repeat", "repeat"),
      Map.entry("background-size", "auto"),
      Map.entry("background-url", ""),
      Map.entry("border", "none"),
      Map.entry("border-bottom", ""),
      Map.entry("border-left", ""),
      Map.entry("border-radius", ""),
      Map.entry("border-right", ""),
      Map.entry("border-top", ""),
      Map.entry("full-width", ""),
      Map.entry("padding", "20px 0"),
      Map.entry("text-align", "center")
  );

  private final ComponentRegistry registry;

  public MjWrapper(MjmlNode node, GlobalContext globalContext, RenderContext renderContext,
      ComponentRegistry registry) {
    super(node, globalContext, renderContext);
    this.registry = registry;
  }

  @Override
  public String getTagName() {
    return "mj-wrapper";
  }

  @Override
  public Map<String, String> getDefaultAttributes() {
    return DEFAULTS;
  }

  @Override
  public CssBoxModel getBoxModel() {
    CssBoxModel base = super.getBoxModel();
    String pt = getAttribute("padding-top", "");
    String pr = getAttribute("padding-right", "");
    String pb = getAttribute("padding-bottom", "");
    String pl = getAttribute("padding-left", "");
    double padTop = !pt.isEmpty() ? CssUnitParser.parsePx(pt, 0) : base.paddingTop();
    double padRight = !pr.isEmpty() ? CssUnitParser.parsePx(pr, 0) : base.paddingRight();
    double padBottom = !pb.isEmpty() ? CssUnitParser.parsePx(pb, 0) : base.paddingBottom();
    double padLeft = !pl.isEmpty() ? CssUnitParser.parsePx(pl, 0) : base.paddingLeft();
    return new CssBoxModel(padTop, padRight, padBottom, padLeft,
        base.borderLeftWidth(), base.borderRightWidth());
  }

  @Override
  public String render() {
    boolean isFullWidth = "full-width".equals(getAttribute("full-width"));
    if (isFullWidth) {
      return renderFullWidth();
    }
    return renderNormal();
  }

  private boolean hasBackgroundUrl() {
    String url = getAttribute("background-url", "");
    return !url.isEmpty();
  }

  private String renderNormal() {
    StringBuilder sb = new StringBuilder();
    int containerWidth = globalContext.getContainerWidth();
    String bgColor = getAttribute("background-color");
    boolean hasBg = bgColor != null && !bgColor.isEmpty();
    boolean hasBgUrl = hasBackgroundUrl();
    String bgUrl = getAttribute("background-url", "");

    // MSO wrapper table
    sb.append("    <!--[if mso | IE]><table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"")
        .append(getCssClass())
        .append("\" role=\"presentation\" style=\"width:").append(containerWidth)
        .append("px;\" width=\"").append(containerWidth).append("\" ");
    if (hasBg) {
      sb.append("bgcolor=\"").append(bgColor).append("\" ");
    }
    sb.append("><tr><td style=\"line-height:0px;font-size:0px;mso-line-height-rule:exactly;\">");

    if (hasBgUrl) {
      sb.append(buildVmlRect(containerWidth + "px", bgUrl, bgColor));
      sb.append("<![endif]-->\n");
      // Outer div with background CSS + line-height wrapper
      sb.append("    <div style=\"").append(buildBgImageDivStyle()).append("\">\n");
      sb.append("      <div style=\"line-height:0;font-size:0;\">\n");
      // Inner table with background attribute
      sb.append("        <table align=\"center\" background=\"").append(bgUrl)
          .append("\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\"");
      sb.append(" style=\"").append(buildBgImageTableStyle()).append("\"");
      sb.append(">\n");
    } else {
      sb.append("<![endif]-->\n");
      // Main wrapper div
      sb.append("    <div style=\"").append(buildWrapperStyle()).append("\">\n");
      // Inner table
      sb.append("      <table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\"");
      sb.append(" style=\"").append(buildInnerTableStyle()).append("\"");
      sb.append(">\n");
    }

    // Inner content (shared)
    String indent = hasBgUrl ? "          " : "        ";
    sb.append(indent).append("<tbody>\n");
    sb.append(indent).append("  <tr>\n");
    sb.append(indent).append("    <td style=\"").append(buildInnerTdStyle()).append("\">\n");
    renderWrappedChildren(sb);
    sb.append(indent).append("    </td>\n");
    sb.append(indent).append("  </tr>\n");
    sb.append(indent).append("</tbody>\n");

    if (hasBgUrl) {
      sb.append("        </table>\n");
      sb.append("      </div>\n");
      sb.append("    </div>\n");
      sb.append("    <!--[if mso | IE]></v:textbox></v:rect></td></tr></table><![endif]-->\n");
    } else {
      sb.append("      </table>\n");
      sb.append("    </div>\n");
      sb.append("    <!--[if mso | IE]></td></tr></table><![endif]-->\n");
    }

    return sb.toString();
  }

  private String renderFullWidth() {
    StringBuilder sb = new StringBuilder();
    int containerWidth = globalContext.getContainerWidth();
    String bgColor = getAttribute("background-color");
    boolean hasBg = bgColor != null && !bgColor.isEmpty();

    // Full-width outer HTML table (real table, not MSO conditional)
    sb.append("    <table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"");
    if (hasBg) {
      sb.append("background:").append(bgColor).append(";background-color:").append(bgColor).append(";");
    }
    sb.append("width:100%;\">\n");
    sb.append("      <tbody>\n");
    sb.append("        <tr>\n");
    sb.append("          <td>\n");

    // MSO inner table for width constraint
    sb.append("            <!--[if mso | IE]><table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"")
        .append(getCssClass())
        .append("\" role=\"presentation\" style=\"width:").append(containerWidth)
        .append("px;\" width=\"").append(containerWidth).append("\" ");
    if (hasBg) {
      sb.append("bgcolor=\"").append(bgColor).append("\" ");
    }
    sb.append("><tr><td style=\"line-height:0px;font-size:0px;mso-line-height-rule:exactly;\"><![endif]-->\n");

    // Inner wrapper div with max-width
    sb.append("            <div style=\"margin:0px auto;max-width:").append(containerWidth).append("px;\">\n");

    // Inner table (NO background — background is on the outer table)
    sb.append("              <table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"width:100%;\">\n");
    sb.append("                <tbody>\n");
    sb.append("                  <tr>\n");

    // Inner td with padding
    sb.append("                    <td style=\"").append(buildInnerTdStyle()).append("\">\n");

    renderWrappedChildren(sb);

    sb.append("                    </td>\n");
    sb.append("                  </tr>\n");
    sb.append("                </tbody>\n");
    sb.append("              </table>\n");
    sb.append("            </div>\n");

    // Close MSO inner table
    sb.append("            <!--[if mso | IE]></td></tr></table><![endif]-->\n");

    sb.append("          </td>\n");
    sb.append("        </tr>\n");
    sb.append("      </tbody>\n");
    sb.append("    </table>\n");

    return sb.toString();
  }

  /**
   * Renders child sections inside the wrapper. Each child gets its own MSO table
   * wrapper pair, with tr/td transitions between them.
   */
  private void renderWrappedChildren(StringBuilder sb) {
    List<MjmlNode> sectionChildren = getSectionChildren();

    if (sectionChildren.isEmpty()) {
      // Empty wrapper emits an empty MSO table
      sb.append("              <!--[if mso | IE]><table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"></table><![endif]-->\n");
      return;
    }

    int containerWidth = globalContext.getContainerWidth();
    CssBoxModel wrapperBox = getBoxModel();
    int innerWidth = (int) (containerWidth - wrapperBox.paddingLeft() - wrapperBox.paddingRight()
        - wrapperBox.borderLeftWidth() - wrapperBox.borderRightWidth());

    for (int i = 0; i < sectionChildren.size(); i++) {
      MjmlNode child = sectionChildren.get(i);
      boolean isFirst = (i == 0);
      boolean isLast = (i == sectionChildren.size() - 1);

      if (isFirst) {
        // Open first MSO wrapper: table > tr > td (container width) > inner table (inner width) > tr > td
        sb.append("              <!--[if mso | IE]><table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr><td class=\"\" width=\"")
            .append(containerWidth).append("px\" >");
        sb.append("<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"\" role=\"presentation\" style=\"width:")
            .append(innerWidth).append("px;\" width=\"").append(innerWidth).append("\" >");
        sb.append("<tr><td style=\"line-height:0px;font-size:0px;mso-line-height-rule:exactly;\"><![endif]-->\n");
      }

      // Render child section in "inside wrapper" mode
      RenderContext childContext = renderContext
          .withWidth(innerWidth)
          .withPosition(i, isFirst, isLast)
          .withInsideWrapper(true);

      BaseComponent component = registry.createComponent(child, globalContext, childContext);
      if (component instanceof BodyComponent bodyComponent) {
        sb.append(bodyComponent.render());
      }

      if (!isLast) {
        // MSO transition between children: close inner table, close td, new tr > td (container width), new inner table (inner width)
        sb.append("              <!--[if mso | IE]></td></tr></table></td></tr><tr><td class=\"\" width=\"")
            .append(containerWidth).append("px\" >");
        sb.append("<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"\" role=\"presentation\" style=\"width:")
            .append(innerWidth).append("px;\" width=\"").append(innerWidth).append("\" >");
        sb.append("<tr><td style=\"line-height:0px;font-size:0px;mso-line-height-rule:exactly;\"><![endif]-->\n");
      } else {
        // Close last MSO wrapper: close inner table, close td, close tr, close outer table
        sb.append("              <!--[if mso | IE]></td></tr></table></td></tr></table><![endif]-->\n");
      }
    }
  }

  private List<MjmlNode> getSectionChildren() {
    List<MjmlNode> sections = new ArrayList<>();
    for (MjmlNode child : node.getChildren()) {
      String tag = child.getTagName();
      if (!tag.startsWith("#")) {
        sections.add(child);
      }
    }
    return sections;
  }

  private String getCssClass() {
    return getAttribute("css-class", "");
  }

  // --- Background image support ---

  private String resolveBackgroundPosition() {
    String posX = getAttribute("background-position-x", "");
    String posY = getAttribute("background-position-y", "");
    if (!posX.isEmpty() && !posY.isEmpty()) {
      return posX + " " + posY;
    }
    String pos = getAttribute("background-position", "top center");
    return BackgroundPositionHelper.normalize(pos);
  }

  private String buildVmlRect(String widthStyle, String bgUrl, String bgColor) {
    return VmlHelper.buildWrapperVmlRect(widthStyle, bgUrl, bgColor,
        resolveBackgroundPosition(),
        getAttribute("background-size", "auto"));
  }

  private String buildBackgroundCss() {
    return BackgroundCssHelper.buildBackgroundCss(
        getAttribute("background-color", ""),
        getAttribute("background-url", ""),
        resolveBackgroundPosition(),
        getAttribute("background-size", "auto"),
        getAttribute("background-repeat", "repeat"));
  }

  private String buildBgImageDivStyle() {
    String bgPosition = resolveBackgroundPosition();
    return buildStyle(BackgroundCssHelper.buildBgImageDivStyleMap(
        buildBackgroundCss(), bgPosition,
        getAttribute("background-repeat", "repeat"),
        getAttribute("background-size", "auto"),
        globalContext.getContainerWidth()));
  }

  private String buildBgImageTableStyle() {
    String bgPosition = resolveBackgroundPosition();
    return buildStyle(BackgroundCssHelper.buildBgImageTableStyleMap(
        buildBackgroundCss(), bgPosition,
        getAttribute("background-repeat", "repeat"),
        getAttribute("background-size", "auto")));
  }

  // --- Style builders ---

  private String buildWrapperStyle() {
    Map<String, String> styles = new LinkedHashMap<>();
    String bgColor = getAttribute("background-color");
    if (bgColor != null && !bgColor.isEmpty()) {
      styles.put("background", bgColor);
      styles.put("background-color", bgColor);
    }
    styles.put("margin", "0px auto");
    styles.put("max-width", globalContext.getContainerWidth() + "px");
    String borderRadius = getAttribute("border-radius", "");
    if (!borderRadius.isEmpty()) {
      styles.put("border-radius", borderRadius);
      styles.put("overflow", "hidden");
    }
    return buildStyle(styles);
  }

  private String buildInnerTableStyle() {
    Map<String, String> styles = new LinkedHashMap<>();
    String bgColor = getAttribute("background-color");
    if (bgColor != null && !bgColor.isEmpty()) {
      styles.put("background", bgColor);
      styles.put("background-color", bgColor);
    }
    styles.put("width", "100%");
    String borderRadius = getAttribute("border-radius", "");
    if (!borderRadius.isEmpty()) {
      styles.put("border-collapse", "separate");
    }
    return buildStyle(styles);
  }

  private String buildInnerTdStyle() {
    Map<String, String> styles = new LinkedHashMap<>();
    String border = getAttribute("border", "");
    if (!border.isEmpty() && !"none".equals(border)) {
      styles.put("border", border);
    }
    String borderRadius = getAttribute("border-radius", "");
    if (!borderRadius.isEmpty()) {
      styles.put("border-radius", borderRadius);
    }
    styles.put("direction", getAttribute("direction", "ltr"));
    styles.put("font-size", "0px");
    String padding = getAttribute("padding");
    if (padding != null && !padding.isEmpty()) {
      styles.put("padding", padding);
    }
    styles.put("text-align", getAttribute("text-align", "center"));
    return buildStyle(styles);
  }
}
