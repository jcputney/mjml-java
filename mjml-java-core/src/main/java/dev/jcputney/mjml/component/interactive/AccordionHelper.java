package dev.jcputney.mjml.component.interactive;

import dev.jcputney.mjml.context.AttributeResolver;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.parser.MjmlNode;
import java.util.Map;

/**
 * Shared utility for accordion sub-components ({@code mj-accordion-title},
 * {@code mj-accordion-text}) that need to resolve attributes from ancestor
 * accordion nodes via the full attribute cascade.
 */
final class AccordionHelper {

  private AccordionHelper() {
  }

  /**
   * Resolves an attribute by walking up the node tree to find a value
   * set on an ancestor (typically the mj-accordion-element or mj-accordion),
   * using the full attribute cascade (inline, mj-class, tag defaults, mj-all).
   */
  static String resolveAncestorAttr(MjmlNode node, String name, GlobalContext globalContext,
      String fallback) {
    MjmlNode current = node.getParent();
    while (current != null) {
      Map<String, String> defaults = getDefaultsForTag(current.getTagName());
      String value = AttributeResolver.resolve(current, name, globalContext, defaults);
      if (value != null && !value.isEmpty()) {
        return value;
      }
      current = current.getParent();
    }
    return fallback;
  }

  /**
   * Returns the appropriate default attributes map for a given tag name.
   */
  private static Map<String, String> getDefaultsForTag(String tagName) {
    if ("mj-accordion".equals(tagName)) {
      return MjAccordion.DEFAULTS;
    }
    if ("mj-accordion-element".equals(tagName)) {
      return MjAccordionElement.DEFAULTS;
    }
    return Map.of();
  }
}
