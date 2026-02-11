package dev.jcputney.mjml.context;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Attribute cascade state gathered during head processing. Contains default attributes (from {@code
 * <mj-attributes>}), class attributes (from {@code <mj-class>}), and HTML attributes (from {@code
 * <mj-html-attributes>}).
 *
 * <p>This is one of three focused sub-contexts extracted from {@link GlobalContext}. See also
 * {@link MetadataContext} and {@link StyleContext}.
 *
 * <p><strong>Thread safety:</strong> This class is <em>not</em> thread-safe. Each render pipeline
 * creates its own instance.
 */
public class AttributeContext {

  private final Map<String, Map<String, String>> defaultAttributes = new LinkedHashMap<>();
  private final Map<String, Map<String, String>> classAttributes = new LinkedHashMap<>();
  private final Map<String, Map<String, String>> htmlAttributes = new LinkedHashMap<>();

  /** Creates a new empty {@code AttributeContext} with no default, class, or HTML attributes. */
  public AttributeContext() {}

  /**
   * Sets default attributes for a given MJML tag name. If attributes already exist for the tag, the
   * new values are merged in.
   *
   * @param tagName the MJML tag name (e.g. "mj-all", "mj-section")
   * @param attrs the attribute name-value pairs to set
   */
  public void setDefaultAttributes(String tagName, Map<String, String> attrs) {
    defaultAttributes.computeIfAbsent(tagName, k -> new LinkedHashMap<>()).putAll(attrs);
  }

  /**
   * Returns the default attributes for a given MJML tag name.
   *
   * @param tagName the MJML tag name to look up
   * @return the attribute map for the tag, or an empty map if none are set
   */
  public Map<String, String> getDefaultAttributes(String tagName) {
    return defaultAttributes.getOrDefault(tagName, Map.of());
  }

  /**
   * Returns the global default attributes defined under the "mj-all" tag.
   *
   * @return the "mj-all" attribute map, or an empty map if none are set
   */
  public Map<String, String> getAllDefaults() {
    return defaultAttributes.getOrDefault("mj-all", Map.of());
  }

  /**
   * Sets class-level attributes for a given MJML class name. If attributes already exist for the
   * class, the new values are merged in.
   *
   * @param className the MJML class name
   * @param attrs the attribute name-value pairs to set
   */
  public void setClassAttributes(String className, Map<String, String> attrs) {
    classAttributes.computeIfAbsent(className, k -> new LinkedHashMap<>()).putAll(attrs);
  }

  /**
   * Returns the class-level attributes for a given MJML class name.
   *
   * @param className the MJML class name to look up
   * @return the attribute map for the class, or an empty map if none are set
   */
  public Map<String, String> getClassAttributes(String className) {
    return classAttributes.getOrDefault(className, Map.of());
  }

  /**
   * Sets HTML attributes for a given CSS selector. If attributes already exist for the selector,
   * the new values are merged in.
   *
   * @param selector the CSS selector identifying target elements
   * @param attrs the attribute name-value pairs to set
   */
  public void setHtmlAttributes(String selector, Map<String, String> attrs) {
    htmlAttributes.computeIfAbsent(selector, k -> new LinkedHashMap<>()).putAll(attrs);
  }

  /**
   * Returns an unmodifiable view of all HTML attributes keyed by CSS selector.
   *
   * @return unmodifiable map of selector to attribute map
   */
  public Map<String, Map<String, String>> getHtmlAttributes() {
    return Collections.unmodifiableMap(htmlAttributes);
  }
}
