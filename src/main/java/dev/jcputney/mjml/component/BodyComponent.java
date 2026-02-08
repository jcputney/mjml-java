package dev.jcputney.mjml.component;

import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import dev.jcputney.mjml.util.CssBoxModel;
import dev.jcputney.mjml.util.CssUnitParser;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Abstract base for body components that render to HTML.
 * Provides common utilities for width calculation, padding,
 * and style building.
 */
public abstract non-sealed class BodyComponent extends BaseComponent {

  protected BodyComponent(MjmlNode node, GlobalContext globalContext,
      RenderContext renderContext) {
    super(node, globalContext, renderContext);
  }

  /**
   * Renders this component to HTML.
   */
  public abstract String render();

  /**
   * Returns the content width after subtracting padding and borders.
   */
  public double getContentWidth() {
    double containerWidth = renderContext.getContainerWidth();
    CssBoxModel box = getBoxModel();
    return containerWidth - box.horizontalSpacing();
  }

  /**
   * Returns the box model for this component based on its padding and border attributes.
   */
  public CssBoxModel getBoxModel() {
    return CssBoxModel.fromAttributes(
        getAttribute("padding", "0"),
        getAttribute("border", "none"),
        getAttribute("border-left", ""),
        getAttribute("border-right", "")
    );
  }

  /**
   * Builds a CSS style string from a map of property/value pairs.
   */
  protected String buildStyle(Map<String, String> styles) {
    if (styles == null || styles.isEmpty()) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, String> entry : styles.entrySet()) {
      if (entry.getValue() != null && !entry.getValue().isEmpty()) {
        sb.append(entry.getKey()).append(':').append(entry.getValue()).append(';');
      }
    }
    return sb.toString();
  }

  /**
   * Helper to build HTML attributes string from a map.
   */
  protected String buildAttributes(Map<String, String> attrs) {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, String> entry : attrs.entrySet()) {
      if (entry.getValue() != null) {
        sb.append(' ').append(entry.getKey()).append("=\"").append(entry.getValue()).append('"');
      }
    }
    return sb.toString();
  }

  /**
   * Renders all child body components and concatenates their output.
   */
  protected String renderChildren(ComponentRegistry registry) {
    StringBuilder sb = new StringBuilder();
    var children = node.getChildren();
    for (int i = 0; i < children.size(); i++) {
      MjmlNode child = children.get(i);
      if (child.getTagName().startsWith("#")) {
        continue; // skip text/cdata nodes when rendering component children
      }
      RenderContext childContext = renderContext.withPosition(i, i == 0,
          i == children.size() - 1);
      BaseComponent component = registry.createComponent(child, globalContext, childContext);
      if (component instanceof BodyComponent bodyComponent) {
        sb.append(bodyComponent.render());
      }
    }
    return sb.toString();
  }

  /**
   * Returns a map with ordered style entries for the given style category.
   * Subclasses override this to define style mappings.
   */
  protected Map<String, String> getStyles(String name) {
    return Map.of();
  }

  /**
   * Builds a style string for the given style category.
   */
  protected String buildStyleString(String name) {
    return buildStyle(getStyles(name));
  }

  /**
   * Creates an ordered map for style building.
   */
  protected Map<String, String> orderedMap(String... pairs) {
    Map<String, String> map = new LinkedHashMap<>();
    for (int i = 0; i < pairs.length - 1; i += 2) {
      if (pairs[i + 1] != null && !pairs[i + 1].isEmpty()) {
        map.put(pairs[i], pairs[i + 1]);
      }
    }
    return map;
  }

  /**
   * Parses a CSS unit value to pixels, using the container width for percentages.
   */
  protected double parseWidth(String value) {
    return CssUnitParser.toPixels(value, renderContext.getContainerWidth());
  }
}
