
package dev.jcputney.mjml.component.content;

import static dev.jcputney.mjml.util.HtmlBuilder.attrs;

import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import dev.jcputney.mjml.util.HtmlBuilder;
import java.util.Map;

/**
 * The table component (&lt;mj-table&gt;). Renders an HTML table with the component's styles
 * applied, containing the inner HTML content directly (table rows and cells come from the MJML
 * source).
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
    Map.entry("role", ""),
    Map.entry("table-layout", "auto"),
    Map.entry("width", "100%"));

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
      "border", getAttribute("border", "none")));

    HtmlBuilder html = new HtmlBuilder();
    html.open(
      "table",
      attrs(
        "cellpadding", escapeAttr(getAttribute("cellpadding", "0")),
        "cellspacing", escapeAttr(getAttribute("cellspacing", "0")),
        "width", escapeAttr(getAttribute("width", "100%")),
        "border", "0",
        "style", tableStyle));
    html.rawVerbatim(sanitizeContent(node.getInnerHtml()));
    html.close("table");

    return html.toString();
  }
}
