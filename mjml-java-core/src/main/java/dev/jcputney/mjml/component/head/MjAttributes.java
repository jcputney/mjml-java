package dev.jcputney.mjml.component.head;

import dev.jcputney.mjml.component.ComponentRegistry;
import dev.jcputney.mjml.component.HeadComponent;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Processes mj-attributes to set default attribute values. Children can be mj-all (applies to all
 * tags), mj-class (named classes), or specific tag elements (e.g., mj-text, mj-section).
 */
public class MjAttributes extends HeadComponent {

  private final ComponentRegistry registry;

  /**
   * Creates a new MjAttributes component.
   *
   * @param node the parsed MJML node for this component
   * @param globalContext the global rendering context
   * @param renderContext the current render context
   * @param registry the component registry for creating child components
   */
  public MjAttributes(
      MjmlNode node,
      GlobalContext globalContext,
      RenderContext renderContext,
      ComponentRegistry registry) {
    super(node, globalContext, renderContext);
    this.registry = registry;
  }

  @Override
  public String getTagName() {
    return "mj-attributes";
  }

  @Override
  public void process() {
    for (MjmlNode child : node.getChildren()) {
      String tag = child.getTagName();
      if (tag.startsWith("#")) {
        continue;
      }

      Map<String, String> attrs = extractAttributes(child);

      if ("mj-all".equals(tag)) {
        globalContext.attributes().setDefaultAttributes("mj-all", attrs);
      } else if ("mj-class".equals(tag)) {
        String name = child.getAttribute("name");
        if (name != null) {
          attrs.remove("name");
          globalContext.attributes().setClassAttributes(name, attrs);
        }
      } else {
        // Tag-specific defaults (e.g., mj-text, mj-section)
        globalContext.attributes().setDefaultAttributes(tag, attrs);
      }
    }
  }

  private Map<String, String> extractAttributes(MjmlNode node) {
    Map<String, String> attrs = new LinkedHashMap<>(node.getAttributes());
    // Remove mj-class itself from the attribute map
    attrs.remove("mj-class");
    return attrs;
  }
}
