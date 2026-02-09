package dev.jcputney.mjml.context;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Attribute cascade state gathered during head processing.
 * Contains default attributes (from {@code <mj-attributes>}), class attributes
 * (from {@code <mj-class>}), and HTML attributes (from {@code <mj-html-attributes>}).
 *
 * <p>This is one of three focused sub-contexts extracted from {@link GlobalContext}.
 * See also {@link MetadataContext} and {@link StyleContext}.
 *
 * <p><strong>Thread safety:</strong> This class is <em>not</em> thread-safe.
 * Each render pipeline creates its own instance.</p>
 */
public class AttributeContext {

  private final Map<String, Map<String, String>> defaultAttributes = new LinkedHashMap<>();
  private final Map<String, Map<String, String>> classAttributes = new LinkedHashMap<>();
  private final Map<String, Map<String, String>> htmlAttributes = new LinkedHashMap<>();

  // Default attributes (mj-all, mj-section, etc.)
  public void setDefaultAttributes(String tagName, Map<String, String> attrs) {
    defaultAttributes.computeIfAbsent(tagName, k -> new LinkedHashMap<>()).putAll(attrs);
  }

  public Map<String, String> getDefaultAttributes(String tagName) {
    return defaultAttributes.getOrDefault(tagName, Map.of());
  }

  public Map<String, String> getAllDefaults() {
    return defaultAttributes.getOrDefault("mj-all", Map.of());
  }

  // Class attributes
  public void setClassAttributes(String className, Map<String, String> attrs) {
    classAttributes.computeIfAbsent(className, k -> new LinkedHashMap<>()).putAll(attrs);
  }

  public Map<String, String> getClassAttributes(String className) {
    return classAttributes.getOrDefault(className, Map.of());
  }

  // HTML attributes
  public void setHtmlAttributes(String selector, Map<String, String> attrs) {
    htmlAttributes.computeIfAbsent(selector, k -> new LinkedHashMap<>()).putAll(attrs);
  }

  public Map<String, Map<String, String>> getHtmlAttributes() {
    return Collections.unmodifiableMap(htmlAttributes);
  }
}
