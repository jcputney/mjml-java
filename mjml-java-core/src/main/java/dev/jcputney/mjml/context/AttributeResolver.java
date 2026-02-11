package dev.jcputney.mjml.context;

import dev.jcputney.mjml.parser.MjmlNode;
import java.util.Map;

/**
 * Resolves attributes for a component using the 5-level cascade:
 *
 * <ol>
 *   <li>Inline attributes (on the element itself)
 *   <li>mj-class attributes
 *   <li>Tag-specific default attributes (mj-attributes > mj-section, etc.)
 *   <li>mj-all default attributes
 *   <li>Component hardcoded defaults
 * </ol>
 */
public final class AttributeResolver {

  private AttributeResolver() {}

  /**
   * Resolves an attribute value using the 5-level cascade.
   *
   * @param node the element node
   * @param attributeName the attribute to resolve
   * @param globalContext the document-wide context
   * @param componentDefaults the component's hardcoded default values
   * @return the resolved attribute value, or null if not found at any level
   */
  public static String resolve(
      MjmlNode node,
      String attributeName,
      GlobalContext globalContext,
      Map<String, String> componentDefaults) {

    // Level 1: Inline attribute
    String value = node.getAttribute(attributeName);
    if (value != null) {
      return value;
    }

    // Level 2: mj-class attributes
    String mjClass = node.getAttribute("mj-class");
    if (mjClass != null) {
      for (String className : dev.jcputney.mjml.util.CssUnitParser.WHITESPACE.split(mjClass)) {
        Map<String, String> classAttrs = globalContext.attributes().getClassAttributes(className);
        value = classAttrs.get(attributeName);
        if (value != null) {
          return value;
        }
      }
    }

    // Level 3: Tag-specific defaults
    Map<String, String> tagDefaults =
        globalContext.attributes().getDefaultAttributes(node.getTagName());
    value = tagDefaults.get(attributeName);
    if (value != null) {
      return value;
    }

    // Level 4: mj-all defaults
    Map<String, String> allDefaults = globalContext.attributes().getAllDefaults();
    value = allDefaults.get(attributeName);
    if (value != null) {
      return value;
    }

    // Level 5: Component hardcoded defaults
    return componentDefaults.get(attributeName);
  }
}
