package dev.jcputney.javamjml.component.content;

import dev.jcputney.javamjml.component.BodyComponent;
import dev.jcputney.javamjml.context.GlobalContext;
import dev.jcputney.javamjml.context.RenderContext;
import dev.jcputney.javamjml.parser.MjmlNode;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The table component (&lt;mj-table&gt;).
 * Renders an HTML table with the component's styles applied, containing
 * the inner HTML content directly (table rows and cells come from the MJML source).
 */
public class MjTable extends BodyComponent {

  private static final Map<String, String> DEFAULTS = Map.ofEntries(
      Map.entry("align", "left"),
      Map.entry("border", "none"),
      Map.entry("cellpadding", "0"),
      Map.entry("cellspacing", "0"),
      Map.entry("color", "#000000"),
      Map.entry("container-background-color", ""),
      Map.entry("font-family", "Ubuntu, Helvetica, Arial, sans-serif"),
      Map.entry("font-size", "13px"),
      Map.entry("line-height", "22px"),
      Map.entry("padding", "10px 25px"),
      Map.entry("table-layout", "auto"),
      Map.entry("width", "100%")
  );

  public MjTable(MjmlNode node, GlobalContext globalContext, RenderContext renderContext) {
    super(node, globalContext, renderContext);
  }

  @Override
  public String getTagName() {
    return "mj-table";
  }

  @Override
  public Map<String, String> getDefaultAttributes() {
    return DEFAULTS;
  }

  @Override
  public String render() {
    String tableStyle = buildStyle(orderedMap(
        "color", getAttribute("color", "#000000"),
        "font-family", getAttribute("font-family"),
        "font-size", getAttribute("font-size", "13px"),
        "line-height", getAttribute("line-height", "22px"),
        "table-layout", getAttribute("table-layout", "auto"),
        "width", getAttribute("width", "100%"),
        "border", getAttribute("border", "none")
    ));

    StringBuilder sb = new StringBuilder();
    sb.append("<table");
    sb.append(" cellpadding=\"").append(getAttribute("cellpadding", "0")).append("\"");
    sb.append(" cellspacing=\"").append(getAttribute("cellspacing", "0")).append("\"");
    sb.append(" width=\"").append(getAttribute("width", "100%")).append("\"");
    sb.append(" border=\"0\"");
    sb.append(" style=\"").append(tableStyle).append("\"");
    sb.append(">\n");
    sb.append(node.getInnerHtml());
    sb.append("</table>\n");

    return sb.toString();
  }
}
