
package dev.jcputney.mjml.component.body;

import static dev.jcputney.mjml.util.HtmlBuilder.attrs;

import dev.jcputney.mjml.component.BaseComponent;
import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.component.ComponentRegistry;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import dev.jcputney.mjml.util.ColumnWidthCalculator;
import dev.jcputney.mjml.util.CssUnitParser;
import dev.jcputney.mjml.util.HtmlBuilder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The group component (&lt;mj-group&gt;). Groups columns together so they don't stack on mobile.
 * Renders as a single MSO table containing multiple columns.
 */
public class MjGroup extends BodyComponent {

  private static final Map<String, String> DEFAULTS = Map.of(
    "background-color", "",
    "direction", "ltr",
    "vertical-align", "top",
    "width", "");
  private static final Set<String> COLUMN_TAGS = Set.of("mj-column");
  private final ComponentRegistry registry;

  public MjGroup(
    MjmlNode node, GlobalContext globalContext, RenderContext renderContext, ComponentRegistry registry) {
    super(node, globalContext, renderContext);
    this.registry = registry;
  }

  @Override
  public String getTagName() {
    return "mj-group";
  }

  @Override
  public Map<String, String> getDefaultAttributes() {
    return DEFAULTS;
  }

  @Override
  public String render() {
    double groupWidth = renderContext.getContainerWidth();
    String direction = getAttribute("direction", "ltr");

    String widthSpec = renderContext.getColumnWidthSpec();
    String responsiveClass = buildResponsiveClass(widthSpec);

    registerMediaQuery(responsiveClass, widthSpec);

    List<MjmlNode> columns = getColumnChildren();
    double[] widths = ColumnWidthCalculator.calculatePixelWidths(columns, groupWidth, false);
    String[] widthSpecs = ColumnWidthCalculator.calculateWidthSpecs(columns);

    HtmlBuilder html = new HtmlBuilder();
    html.wrap("div", attrs("class", responsiveClass + " mj-outlook-group-fix", "style", buildOuterStyle(direction)),
      () -> {
        // Delay conditional markup so adjacent columns share a block, while raw children
        // are emitted outside column cells without changing the column index or sizing.
        String pendingMso = "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" ><tr>";
        int i = 0;
        boolean hasChildren = false;
        for (MjmlNode col : node.getChildren()) {
          if ("mj-raw".equals(col.getTagName())) {
            hasChildren = true;
            if (!pendingMso.isEmpty()) {
              html.mso(pendingMso);
              pendingMso = "";
            }
            BaseComponent raw = registry.createComponent(col, globalContext, renderContext);
            if (raw instanceof BodyComponent bodyComponent) {
              html.rawVerbatim(bodyComponent.render());
            }
            continue;
          }
          if (!COLUMN_TAGS.contains(col.getTagName())) {
            continue;
          }
          hasChildren = true;
          String msoTdStyle = "vertical-align:top;width:" + CssUnitParser.formatInt(widths[i]) + "px;";
          html.mso(pendingMso + "<td style=\"" + msoTdStyle + "\" >");
          pendingMso = "</td>";

          RenderContext colContext = renderContext
            .withColumnWidth(widths[i], widthSpecs[i])
            .withPosition(i, i == 0, i == columns.size() - 1)
            .withInsideGroup(true);

          BaseComponent component = registry.createComponent(col, globalContext, colContext);
          if (component instanceof BodyComponent bodyComponent) {
            html.rawVerbatim(bodyComponent.render());
          }
          i++;
        }

        if (hasChildren) {
          html.mso(pendingMso + "</tr></table>");
        }
      });

    return html.toString();
  }

  private String buildOuterStyle(String direction) {
    Map<String, String> styles = new LinkedHashMap<>();
    styles.put("font-size", "0");
    styles.put("line-height", "0");
    styles.put("text-align", "left");
    styles.put("display", "inline-block");
    styles.put("width", "100%");
    styles.put("direction", direction);
    addIfPresent(styles, "background-color");
    return buildStyle(styles);
  }

  private List<MjmlNode> getColumnChildren() {
    return getChildrenByTags(COLUMN_TAGS);
  }
}
