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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shared base class for {@code <mj-section>} and {@code <mj-wrapper>}.
 * Extracts identical background-image, box-model, and style-building
 * methods so that each subclass only overrides its specific render logic.
 */
public abstract class AbstractSectionComponent extends BodyComponent {

  protected final ComponentRegistry registry;

  protected AbstractSectionComponent(MjmlNode node, GlobalContext globalContext,
      RenderContext renderContext, ComponentRegistry registry) {
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
    return new CssBoxModel(padTop, padRight, padBottom, padLeft,
        base.borderLeftWidth(), base.borderRightWidth());
  }

  protected boolean hasBackgroundUrl() {
    String url = getAttribute("background-url", "");
    return !url.isEmpty();
  }

  /**
   * Resolves background position from individual x/y properties or the combined property.
   * Normalizes "top center" to "center top" format.
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

  protected String buildBackgroundCss() {
    return BackgroundCssHelper.buildBackgroundCss(
        getAttribute("background-color", ""),
        getAttribute("background-url", ""),
        resolveBackgroundPosition(),
        getAttribute("background-size", "auto"),
        getAttribute("background-repeat", "repeat"));
  }

  protected String buildBgImageDivStyle() {
    String bgPosition = resolveBackgroundPosition();
    return buildStyle(BackgroundCssHelper.buildBgImageDivStyleMap(
        buildBackgroundCss(), bgPosition,
        getAttribute("background-repeat", "repeat"),
        getAttribute("background-size", "auto"),
        globalContext.getContainerWidth()));
  }

  protected String buildBgImageTableStyle() {
    String bgPosition = resolveBackgroundPosition();
    return buildStyle(BackgroundCssHelper.buildBgImageTableStyleMap(
        buildBackgroundCss(), bgPosition,
        getAttribute("background-repeat", "repeat"),
        getAttribute("background-size", "auto")));
  }

  protected String getCssClass() {
    return getAttribute("css-class", "");
  }

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
   * Builds the outer div style with background color, max-width, and optional border-radius.
   * Used by both mj-section (as buildSectionStyle) and mj-wrapper (as buildWrapperStyle).
   */
  protected String buildOuterDivStyle() {
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
}
