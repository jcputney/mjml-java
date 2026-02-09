package dev.jcputney.mjml.component.content;

import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The spacer component (&lt;mj-spacer&gt;).
 * Renders vertical whitespace of a configurable height.
 */
public class MjSpacer extends BodyComponent {

  private static final Map<String, String> DEFAULTS = Map.ofEntries(
      Map.entry("height", "0px"),
      Map.entry("container-background-color", ""),
      Map.entry("padding", ""),
      Map.entry("vertical-align", "")
  );

  public MjSpacer(MjmlNode node, GlobalContext globalContext, RenderContext renderContext) {
    super(node, globalContext, renderContext);
  }

  @Override
  public String getTagName() {
    return "mj-spacer";
  }

  @Override
  public Map<String, String> getDefaultAttributes() {
    return DEFAULTS;
  }

  @Override
  public String render() {
    String height = getAttribute("height", "0px");

    String style = buildStyle(orderedMap(
        "height", height,
        "line-height", height
    ));

    return "                        <div style=\"" + style + "\">&#8202;</div>";
  }
}
