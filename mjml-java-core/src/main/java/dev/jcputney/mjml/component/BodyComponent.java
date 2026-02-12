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
 * Abstract base for body components that render to HTML. Provides common utilities for width
 * calculation, padding, and style building.
 */
public abstract non-sealed class BodyComponent extends BaseComponent {

  private CssBoxModel cachedBoxModel;

  /**
   * Creates a new body component.
   *
   * @param node the parsed MJML node for this component
   * @param globalContext the document-wide context gathered during head processing
   * @param renderContext the current rendering context (container width, position, etc.)
   */
  protected BodyComponent(MjmlNode node, GlobalContext globalContext, RenderContext renderContext) {
    super(node, globalContext, renderContext);
  }

  private static String stripControlChars(String value) {
    StringBuilder sb = null;
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (c <= 0x1F || c == 0x7F) {
        if (sb == null) {
          sb = new StringBuilder(value.length());
          sb.append(value, 0, i);
        }
      } else if (sb != null) {
        sb.append(c);
      }
    }
    return sb != null ? sb.toString() : value;
  }

  /**
   * Builds a responsive CSS class name from a column width specification. Returns
   * "mj-column-px-{value}" for pixel widths or "mj-column-per-{value}" for percentages.
   *
   * @param widthSpec the width specification (e.g. "200px" or "50")
   * @return the responsive CSS class name
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
   * Renders this component to HTML.
   *
   * @return the rendered HTML string
   */
  public abstract String render();

  /**
   * Returns the content width after subtracting padding and borders.
   *
   * @return the content width in pixels
   */
  public double getContentWidth() {
    double containerWidth = renderContext.getContainerWidth();
    CssBoxModel box = getBoxModel();
    return containerWidth - box.horizontalSpacing();
  }

  /**
   * Returns the box model for this component based on its padding and border attributes. The result
   * is cached for repeated access within the same render call.
   *
   * @return the CSS box model for this component
   */
  public CssBoxModel getBoxModel() {
    if (cachedBoxModel == null) {
      cachedBoxModel =
          CssBoxModel.fromAttributes(
              getAttribute("padding", "0"),
              getAttribute("border", "none"),
              getAttribute("border-left", ""),
              getAttribute("border-right", ""));
    }
    return cachedBoxModel;
  }

  /**
   * Resolves a single side of a CSS shorthand property. Checks the individual override attribute
   * first (e.g., padding-left), then falls back to extracting that side from the shorthand (e.g.,
   * padding). Matches MJML's {@code getShorthandAttrValue()} behavior.
   *
   * @param shorthandAttr the shorthand attribute name (e.g., "padding")
   * @param sideAttr the individual side attribute (e.g., "padding-left")
   * @param sideIndex the index within the shorthand (0=top, 1=right, 2=bottom, 3=left)
   * @return the resolved pixel value for that side
   */
  protected double resolveShorthandSide(String shorthandAttr, String sideAttr, int sideIndex) {
    String sideVal = getAttribute(sideAttr, "");
    if (!sideVal.isEmpty()) {
      return CssUnitParser.parsePx(sideVal, 0);
    }
    String shorthand = getAttribute(shorthandAttr, "0");
    double[] parts = CssUnitParser.parseShorthand(shorthand);
    return parts[sideIndex];
  }

  /**
   * Builds a CSS style string from a map of property/value pairs.
   *
   * @param styles the map of CSS property names to values
   * @return the concatenated CSS style string
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
   * Helper to build HTML attributes string from a map. When sanitizeOutput is enabled, attribute
   * values are HTML-escaped.
   *
   * @param attrs the map of attribute names to values
   * @return the concatenated HTML attributes string
   */
  protected String buildAttributes(Map<String, String> attrs) {
    boolean sanitize = globalContext.getConfiguration().isSanitizeOutput();
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, String> entry : attrs.entrySet()) {
      if (entry.getValue() != null) {
        String value =
            sanitize ? HtmlEscaper.escapeAttributeValue(entry.getValue()) : entry.getValue();
        sb.append(' ').append(entry.getKey()).append("=\"").append(value).append('"');
      }
    }
    return sb.toString();
  }

  /**
   * Escapes an attribute value for safe HTML interpolation when sanitizeOutput is enabled. Use this
   * for attribute values that are directly interpolated into HTML via sb.append() rather than going
   * through buildAttributes().
   *
   * @param value the attribute value to escape
   * @return the escaped value, or an empty string if value is null
   */
  protected String escapeAttr(String value) {
    if (value == null) {
      return "";
    }
    return globalContext.getConfiguration().isSanitizeOutput()
        ? HtmlEscaper.escapeAttributeValue(value)
        : value;
  }

  /**
   * Escapes an href attribute value for safe HTML interpolation. Only escapes the double-quote
   * character to prevent attribute breakout, matching the reference MJML implementation which does
   * not HTML-encode ampersands in URL query parameters.
   *
   * @param href the href value to escape (should already be sanitized via {@link #sanitizeHref})
   * @return the escaped href value, or an empty string if null
   */
  protected static String escapeHref(String href) {
    if (href == null) {
      return "";
    }
    return href.replace("\"", "&quot;");
  }

  /**
   * Sanitizes a URL using an allowlist of safe URI schemes when sanitizeOutput is enabled. Only
   * permits {@code http:}, {@code https:}, {@code mailto:}, {@code tel:}, fragment references
   * ({@code #}), and relative paths ({@code /}). All other schemes are blocked and replaced with
   * {@code "#"}.
   *
   * <p>Control characters and leading/trailing whitespace are stripped before the check to prevent
   * bypass via {@code \tjavascript:} or similar.
   *
   * @param href the URL to sanitize
   * @return the original URL if it uses a safe scheme, or {@code "#"} otherwise
   */
  protected String sanitizeHref(String href) {
    if (href == null || href.isEmpty()) {
      return href;
    }
    if (!globalContext.getConfiguration().isSanitizeOutput()) {
      return href;
    }
    // Strip control characters (U+0000-U+001F, U+007F) and trim whitespace
    String cleaned = stripControlChars(href).trim();
    if (cleaned.isEmpty()) {
      return "#";
    }
    String check = cleaned.toLowerCase();
    // Allow: http(s), mailto, tel schemes; fragment refs (#); relative paths (/)
    if (check.startsWith("http:")
        || check.startsWith("https:")
        || check.startsWith("mailto:")
        || check.startsWith("tel:")
        || cleaned.startsWith("#")
        || cleaned.startsWith("/")) {
      return href;
    }
    // Block everything else (javascript:, vbscript:, data:, blob:, etc.)
    return "#";
  }

  /**
   * Renders all child body components and concatenates their output.
   *
   * @param registry the component registry used to create child components
   * @return the concatenated HTML output of all child body components
   */
  protected String renderChildren(ComponentRegistry registry) {
    StringBuilder sb = new StringBuilder();
    var children = node.getChildren();
    for (int i = 0; i < children.size(); i++) {
      MjmlNode child = children.get(i);
      if (child.getTagName().startsWith("#")) {
        continue; // skip text/cdata nodes when rendering component children
      }
      RenderContext childContext = renderContext.withPosition(i, i == 0, i == children.size() - 1);
      BaseComponent component = registry.createComponent(child, globalContext, childContext);
      if (component instanceof BodyComponent bodyComponent) {
        sb.append(bodyComponent.render());
      }
    }
    return sb.toString();
  }

  /**
   * Creates an ordered map for style building.
   *
   * @param pairs alternating key/value pairs (e.g. "color", "red", "margin", "0")
   * @return a LinkedHashMap containing the non-empty key/value pairs
   */
  protected Map<String, String> orderedMap(String... pairs) {
    int capacity = (pairs.length / 2) + 1;
    Map<String, String> map = new LinkedHashMap<>(capacity);
    for (int i = 0; i < pairs.length - 1; i += 2) {
      if (pairs[i + 1] != null && !pairs[i + 1].isEmpty()) {
        map.put(pairs[i], pairs[i + 1]);
      }
    }
    return map;
  }

  /**
   * Adds border styles from the given attribute names to the style map. Skips empty values and
   * "none". Strips "inner-" prefix for CSS property names.
   *
   * @param styles the style map to add border styles to
   * @param attrNames the border attribute names to look up
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
   * Adds an attribute value to the style map if the attribute is non-empty. The CSS property name
   * defaults to the attribute name.
   *
   * @param styles the style map to add the value to
   * @param attrName the attribute name to look up and use as the CSS property name
   */
  protected void addIfPresent(Map<String, String> styles, String attrName) {
    String val = getAttribute(attrName, "");
    if (!val.isEmpty()) {
      styles.put(attrName, val);
    }
  }

  /**
   * Adds an attribute value to the style map under the given CSS property name if the attribute is
   * non-empty.
   *
   * @param styles the style map to add the value to
   * @param cssName the CSS property name to use in the style map
   * @param attrName the attribute name to look up
   */
  protected void addIfPresent(Map<String, String> styles, String cssName, String attrName) {
    String val = getAttribute(attrName, "");
    if (!val.isEmpty()) {
      styles.put(cssName, val);
    }
  }

  /**
   * Parses a CSS unit value to pixels, using the container width for percentages.
   *
   * @param value the CSS value to parse (e.g. "50%", "200px")
   * @return the computed width in pixels
   */
  protected double parseWidth(String value) {
    return CssUnitParser.toPixels(value, renderContext.getContainerWidth());
  }

  /**
   * Returns child nodes whose tag names are in the given set.
   *
   * @param tags the set of tag names to filter by
   * @return a list of matching child nodes
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
   * Registers a responsive media query for the given CSS class name and width specification.
   *
   * @param responsiveClass the CSS class name for the media query
   * @param widthSpec the width specification (e.g. "200px" or "50")
   */
  protected void registerMediaQuery(String responsiveClass, String widthSpec) {
    if (widthSpec != null && widthSpec.endsWith("px")) {
      globalContext.styles().addMediaQuery(responsiveClass, widthSpec, "");
    } else {
      globalContext
          .styles()
          .addMediaQuery(responsiveClass, widthSpec != null ? widthSpec : "100", "%");
    }
  }

  /**
   * Applies the configured {@link ContentSanitizer} to the given content, if one is set. Returns
   * the content unchanged when no sanitizer is configured.
   *
   * @param content the content string to sanitize
   * @return the sanitized content, or the original content if no sanitizer is configured
   */
  protected String sanitizeContent(String content) {
    ContentSanitizer sanitizer = globalContext.getConfiguration().getContentSanitizer();
    return sanitizer != null ? sanitizer.sanitize(content) : content;
  }
}
