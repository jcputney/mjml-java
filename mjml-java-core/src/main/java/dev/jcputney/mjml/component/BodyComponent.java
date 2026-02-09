package dev.jcputney.mjml.component;

import dev.jcputney.mjml.ContentSanitizer;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import dev.jcputney.mjml.util.CssBoxModel;
import dev.jcputney.mjml.util.CssUnitParser;
import dev.jcputney.mjml.util.HtmlEscaper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Abstract base for body components that render to HTML.
 * Provides common utilities for width calculation, padding,
 * and style building.
 */
public abstract non-sealed class BodyComponent extends BaseComponent {

  private CssBoxModel cachedBoxModel;

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
   * The result is cached for repeated access within the same render call.
   */
  public CssBoxModel getBoxModel() {
    if (cachedBoxModel == null) {
      cachedBoxModel = CssBoxModel.fromAttributes(
          getAttribute("padding", "0"),
          getAttribute("border", "none"),
          getAttribute("border-left", ""),
          getAttribute("border-right", "")
      );
    }
    return cachedBoxModel;
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
   * When sanitizeOutput is enabled, attribute values are HTML-escaped.
   */
  protected String buildAttributes(Map<String, String> attrs) {
    boolean sanitize = globalContext.getConfiguration().isSanitizeOutput();
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, String> entry : attrs.entrySet()) {
      if (entry.getValue() != null) {
        String value = sanitize ? HtmlEscaper.escapeAttributeValue(entry.getValue())
            : entry.getValue();
        sb.append(' ').append(entry.getKey()).append("=\"").append(value).append('"');
      }
    }
    return sb.toString();
  }

  /**
   * Escapes an attribute value for safe HTML interpolation when sanitizeOutput is enabled.
   * Use this for attribute values that are directly interpolated into HTML via sb.append()
   * rather than going through buildAttributes().
   */
  protected String escapeAttr(String value) {
    if (value == null) {
      return "";
    }
    return globalContext.getConfiguration().isSanitizeOutput()
        ? HtmlEscaper.escapeAttributeValue(value) : value;
  }

  /**
   * Sanitizes a URL by blocking dangerous URI schemes (javascript:, vbscript:, data:text/html)
   * when sanitizeOutput is enabled. Returns "#" for blocked URIs.
   */
  protected String sanitizeHref(String href) {
    if (href == null || href.isEmpty()) {
      return href;
    }
    if (!globalContext.getConfiguration().isSanitizeOutput()) {
      return href;
    }
    String check = href.trim().toLowerCase();
    if (check.startsWith("javascript:") || check.startsWith("vbscript:")
        || check.startsWith("data:text/html") || check.startsWith("data:image/svg+xml")) {
      return "#";
    }
    return href;
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
   * Adds border styles from the given attribute names to the style map.
   * Skips empty values and "none". Strips "inner-" prefix for CSS property names.
   */
  protected void addBorderStyles(Map<String, String> styles, String... attrNames) {
    for (String attr : attrNames) {
      String val = getAttribute(attr, "");
      if (!val.isEmpty() && !"none".equals(val)) {
        String cssName = attr.startsWith("inner-") ? attr.substring(6) : attr;
        styles.put(cssName, val);
      }
    }
  }

  /**
   * Adds an attribute value to the style map if the attribute is non-empty.
   * The CSS property name defaults to the attribute name.
   */
  protected void addIfPresent(Map<String, String> styles, String attrName) {
    String val = getAttribute(attrName, "");
    if (!val.isEmpty()) {
      styles.put(attrName, val);
    }
  }

  /**
   * Adds an attribute value to the style map under the given CSS property name
   * if the attribute is non-empty.
   */
  protected void addIfPresent(Map<String, String> styles, String cssName, String attrName) {
    String val = getAttribute(attrName, "");
    if (!val.isEmpty()) {
      styles.put(cssName, val);
    }
  }

  /**
   * Parses a CSS unit value to pixels, using the container width for percentages.
   */
  protected double parseWidth(String value) {
    return CssUnitParser.toPixels(value, renderContext.getContainerWidth());
  }

  /**
   * Returns child nodes whose tag names are in the given set.
   */
  protected List<MjmlNode> getChildrenByTags(Set<String> tags) {
    List<MjmlNode> result = new ArrayList<>();
    for (MjmlNode child : node.getChildren()) {
      if (tags.contains(child.getTagName())) {
        result.add(child);
      }
    }
    return result;
  }

  /**
   * Builds a responsive CSS class name from a column width specification.
   * Returns "mj-column-px-{value}" for pixel widths or "mj-column-per-{value}" for percentages.
   */
  protected static String buildResponsiveClass(String widthSpec) {
    if (widthSpec == null) {
      widthSpec = "100";
    }
    if (widthSpec.endsWith("px")) {
      return "mj-column-px-" + widthSpec.replace("px", "");
    }
    return "mj-column-per-" + widthSpec.replace(".", "-");
  }

  /**
   * Registers a responsive media query for the given CSS class name and width specification.
   */
  protected void registerMediaQuery(String responsiveClass, String widthSpec) {
    if (widthSpec != null && widthSpec.endsWith("px")) {
      globalContext.addMediaQuery(responsiveClass, widthSpec, "");
    } else {
      globalContext.addMediaQuery(responsiveClass, widthSpec != null ? widthSpec : "100", "%");
    }
  }

  /**
   * Applies the configured {@link ContentSanitizer} to the given content, if one is set.
   * Returns the content unchanged when no sanitizer is configured.
   */
  protected String sanitizeContent(String content) {
    ContentSanitizer sanitizer = globalContext.getConfiguration().getContentSanitizer();
    return sanitizer != null ? sanitizer.sanitize(content) : content;
  }
}
