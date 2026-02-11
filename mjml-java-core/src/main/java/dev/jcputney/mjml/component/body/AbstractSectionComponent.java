package dev.jcputney.mjml.component.body;

import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.component.ComponentRegistry;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import dev.jcputney.mjml.util.BackgroundCssHelper;
import dev.jcputney.mjml.util.BackgroundPositionHelper;
import dev.jcputney.mjml.util.CssBoxModel;
import dev.jcputney.mjml.util.CssUnitParser;
import dev.jcputney.mjml.util.MsoHelper;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shared base class for {@code <mj-section>} and {@code <mj-wrapper>}. Extracts identical
 * background-image, box-model, and style-building methods so that each subclass only overrides its
 * specific render logic.
 */
public abstract class AbstractSectionComponent extends BodyComponent {

  /** The component registry used to look up child components during rendering. */
  protected final ComponentRegistry registry;

  /**
   * Creates a new section component with the given node, context, and registry.
   *
   * @param node the MJML node representing this component
   * @param globalContext the global rendering context
   * @param renderContext the current render context
   * @param registry the component registry for resolving child components
   */
  protected AbstractSectionComponent(
      MjmlNode node,
      GlobalContext globalContext,
      RenderContext renderContext,
      ComponentRegistry registry) {
    super(node, globalContext, renderContext);
    this.registry = registry;
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
    return new CssBoxModel(
        padTop, padRight, padBottom, padLeft, base.borderLeftWidth(), base.borderRightWidth());
  }

  /**
   * Checks whether this component has a non-empty background-url attribute.
   *
   * @return {@code true} if a background URL is set, {@code false} otherwise
   */
  protected boolean hasBackgroundUrl() {
    String url = getAttribute("background-url", "");
    return !url.isEmpty();
  }

  /**
   * Resolves background position from individual x/y properties or the combined property.
   * Normalizes "top center" to "center top" format.
   *
   * @return the normalized background position string in "x y" format
   */
  protected String resolveBackgroundPosition() {
    String posX = getAttribute("background-position-x", "");
    String posY = getAttribute("background-position-y", "");
    if (!posX.isEmpty() && !posY.isEmpty()) {
      return posX + " " + posY;
    }
    String pos = getAttribute("background-position", "top center");
    return BackgroundPositionHelper.normalize(pos);
  }

  /**
   * Builds the CSS background shorthand string from this component's background attributes.
   *
   * @return the CSS background shorthand value
   */
  protected String buildBackgroundCss() {
    return BackgroundCssHelper.buildBackgroundCss(
        getAttribute("background-color", ""),
        getAttribute("background-url", ""),
        resolveBackgroundPosition(),
        getAttribute("background-size", "auto"),
        getAttribute("background-repeat", "repeat"));
  }

  /**
   * Builds the inline style string for the outer div when a background image is present.
   *
   * @return the inline CSS style string for the background image div
   */
  protected String buildBgImageDivStyle() {
    String bgPosition = resolveBackgroundPosition();
    return buildStyle(
        BackgroundCssHelper.buildBgImageDivStyleMap(
            buildBackgroundCss(),
            bgPosition,
            getAttribute("background-repeat", "repeat"),
            getAttribute("background-size", "auto"),
            globalContext.metadata().getContainerWidth()));
  }

  /**
   * Builds the inline style string for the inner table when a background image is present.
   *
   * @return the inline CSS style string for the background image table
   */
  protected String buildBgImageTableStyle() {
    String bgPosition = resolveBackgroundPosition();
    return buildStyle(
        BackgroundCssHelper.buildBgImageTableStyleMap(
            buildBackgroundCss(),
            bgPosition,
            getAttribute("background-repeat", "repeat"),
            getAttribute("background-size", "auto")));
  }

  /**
   * Returns the value of the css-class attribute for this component.
   *
   * @return the CSS class string, or an empty string if not set
   */
  protected String getCssClass() {
    return getAttribute("css-class", "");
  }

  /**
   * Builds the inline style string for the inner table element, including background color and
   * optional border-collapse for border-radius support.
   *
   * @return the inline CSS style string for the inner table
   */
  protected String buildInnerTableStyle() {
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

  /**
   * Builds the style for the inner td element. Subclasses can override {@link
   * #addInnerTdBorderStyles(Map)} and {@link #addInnerTdExtraStyles(Map)} to customize border
   * handling and add extra properties.
   *
   * @return the inline CSS style string for the inner td element
   */
  protected String buildInnerTdStyle() {
    Map<String, String> styles = new LinkedHashMap<>();
    addInnerTdBorderStyles(styles);
    addIfPresent(styles, "border-radius");
    styles.put("direction", getAttribute("direction", "ltr"));
    addInnerTdExtraStyles(styles);
    styles.put("font-size", "0px");
    addIfPresent(styles, "padding");
    addInnerTdPaddingOverrides(styles);
    styles.put("text-align", getAttribute("text-align", "center"));
    return buildStyle(styles);
  }

  /**
   * Adds border styles to the inner td style map. Default implementation adds only the "border"
   * attribute if non-empty and not "none". Subclasses can override to use the full
   * addBorderStyles() helper.
   *
   * @param styles the mutable style map to add border properties to
   */
  protected void addInnerTdBorderStyles(Map<String, String> styles) {
    String border = getAttribute("border", "");
    if (!border.isEmpty() && !"none".equals(border)) {
      styles.put("border", border);
    }
  }

  /**
   * Adds extra styles between border-radius and font-size. Default is no-op.
   *
   * @param styles the mutable style map to add extra properties to
   */
  protected void addInnerTdExtraStyles(Map<String, String> styles) {
    // Subclasses may override
  }

  /**
   * Adds individual padding overrides. Default is no-op.
   *
   * @param styles the mutable style map to add padding overrides to
   */
  protected void addInnerTdPaddingOverrides(Map<String, String> styles) {
    // Subclasses may override
  }

  /**
   * Renders the normal (non-full-width) scaffold shared by mj-section and mj-wrapper. Both
   * components follow the same MSO table → VML rect → div/table → inner content → close structure;
   * subclasses provide the VML rect string, inner content, and optional css-class on the outer div.
   *
   * @param vmlRect VML rect markup for background images (from VmlHelper)
   * @param innerContent rendered inner content (columns for section, wrapped children for wrapper)
   * @param outerDivClass optional CSS class to add to the outer div (empty string if none)
   * @return the rendered HTML scaffold string
   */
  protected String renderNormalScaffold(String vmlRect, String innerContent, String outerDivClass) {
    StringBuilder sb = new StringBuilder();
    int containerWidth = globalContext.metadata().getContainerWidth();
    String bgColor = getAttribute("background-color");
    boolean hasBg = bgColor != null && !bgColor.isEmpty();
    boolean hasBgUrl = hasBackgroundUrl();
    String bgUrl = getAttribute("background-url", "");

    // MSO wrapper table
    sb.append("    ")
        .append(MsoHelper.conditionalStart())
        .append(
            MsoHelper.msoTableOpening(
                containerWidth,
                escapeAttr(getCssClass()),
                hasBg ? escapeAttr(bgColor) : null,
                MsoHelper.MSO_TD_STYLE));

    if (hasBgUrl) {
      sb.append(vmlRect);
      sb.append("<![endif]-->\n");
      sb.append("    <div style=\"").append(buildBgImageDivStyle()).append("\">\n");
      sb.append("      <div style=\"line-height:0;font-size:0;\">\n");
      sb.append("        <table align=\"center\" background=\"")
          .append(escapeAttr(bgUrl))
          .append("\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\"");
      sb.append(" style=\"").append(buildBgImageTableStyle()).append("\"");
      sb.append(">\n");
    } else {
      sb.append("<![endif]-->\n");
      sb.append("    <div");
      sb.append(" style=\"").append(buildOuterDivStyle()).append("\"");
      if (!outerDivClass.isEmpty()) {
        sb.append(" class=\"").append(escapeAttr(outerDivClass)).append("\"");
      }
      sb.append(">\n");
      sb.append(
          "      <table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\"");
      sb.append(" style=\"").append(buildInnerTableStyle()).append("\"");
      sb.append(">\n");
    }

    String indent = hasBgUrl ? "          " : "        ";
    sb.append(indent).append("<tbody>\n");
    sb.append(indent).append("  <tr>\n");
    sb.append(indent).append("    <td style=\"").append(buildInnerTdStyle()).append("\">\n");
    sb.append(innerContent);
    sb.append(indent).append("    </td>\n");
    sb.append(indent).append("  </tr>\n");
    sb.append(indent).append("</tbody>\n");

    if (hasBgUrl) {
      sb.append("        </table>\n");
      sb.append("      </div>\n");
      sb.append("    </div>\n");
      sb.append("    ")
          .append(MsoHelper.conditionalStart())
          .append("</v:textbox></v:rect>")
          .append(MsoHelper.msoTableClosing())
          .append(MsoHelper.conditionalEnd())
          .append("\n");
    } else {
      sb.append("      </table>\n");
      sb.append("    </div>\n");
      sb.append("    ").append(MsoHelper.msoConditionalTableClosing()).append("\n");
    }

    return sb.toString();
  }

  /**
   * Builds the outer div style with background color, max-width, and optional border-radius. Used
   * by both mj-section (as buildSectionStyle) and mj-wrapper (as buildWrapperStyle).
   *
   * @return the inline CSS style string for the outer div
   */
  protected String buildOuterDivStyle() {
    Map<String, String> styles = new LinkedHashMap<>();
    String bgColor = getAttribute("background-color");
    if (bgColor != null && !bgColor.isEmpty()) {
      styles.put("background", bgColor);
      styles.put("background-color", bgColor);
    }
    styles.put("margin", "0px auto");
    styles.put("max-width", globalContext.metadata().getContainerWidth() + "px");
    String borderRadius = getAttribute("border-radius", "");
    if (!borderRadius.isEmpty()) {
      styles.put("border-radius", borderRadius);
      styles.put("overflow", "hidden");
    }
    return buildStyle(styles);
  }
}
