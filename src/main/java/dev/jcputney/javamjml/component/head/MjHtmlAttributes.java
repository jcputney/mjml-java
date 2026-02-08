package dev.jcputney.javamjml.component.head;

import dev.jcputney.javamjml.component.HeadComponent;
import dev.jcputney.javamjml.context.GlobalContext;
import dev.jcputney.javamjml.context.RenderContext;
import dev.jcputney.javamjml.parser.MjmlNode;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Processes mj-html-attributes to add custom HTML attributes to output elements.
 * Children are mj-selector elements, each with a path attribute (CSS selector)
 * and mj-html-attribute children defining the attributes to add.
 */
public class MjHtmlAttributes extends HeadComponent {

  public MjHtmlAttributes(MjmlNode node, GlobalContext globalContext,
      RenderContext renderContext) {
    super(node, globalContext, renderContext);
  }

  @Override
  public String getTagName() {
    return "mj-html-attributes";
  }

  @Override
  public void process() {
    for (MjmlNode selector : node.getChildrenByTag("mj-selector")) {
      String path = selector.getAttribute("path");
      if (path == null || path.isEmpty()) {
        continue;
      }

      Map<String, String> attrs = new LinkedHashMap<>();
      for (MjmlNode attr : selector.getChildrenByTag("mj-html-attribute")) {
        String name = attr.getAttribute("name");
        String value = attr.getInnerHtml().trim();
        if (name != null && !name.isEmpty()) {
          attrs.put(name, value);
        }
      }

      if (!attrs.isEmpty()) {
        globalContext.setHtmlAttributes(path, attrs);
      }
    }
  }
}
