package dev.jcputney.mjml.component.body;

import dev.jcputney.mjml.component.BaseComponent;
import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.component.ComponentRegistry;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import dev.jcputney.mjml.util.ColumnWidthCalculator;
import dev.jcputney.mjml.util.CssUnitParser;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The group component (&lt;mj-group&gt;).
 * Groups columns together so they don't stack on mobile.
 * Renders as a single MSO table containing multiple columns.
 */
public class MjGroup extends BodyComponent {

  private static final Map<String, String> DEFAULTS = Map.of(
      "background-color", "",
      "direction", "ltr",
      "vertical-align", "top",
      "width", ""
  );

  private final ComponentRegistry registry;

  public MjGroup(MjmlNode node, GlobalContext globalContext, RenderContext renderContext,
      ComponentRegistry registry) {
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
    StringBuilder sb = new StringBuilder();
    double groupWidth = renderContext.getContainerWidth();
    String direction = getAttribute("direction", "ltr");

    // Build responsive class from the column width spec set by the parent section
    String widthSpec = renderContext.getColumnWidthSpec();
    String responsiveClass = buildResponsiveClass(widthSpec);

    // Outer div with responsive class
    sb.append("              <div class=\"").append(responsiveClass).append(" mj-outlook-group-fix\"");
    sb.append(" style=\"").append(buildOuterStyle(direction)).append("\"");
    sb.append(">\n");

    // MSO table open
    sb.append("                <!--[if mso | IE]><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" ><tr>");

    // Render column children
    List<MjmlNode> columns = getColumnChildren();
    double[] widths = ColumnWidthCalculator.calculatePixelWidths(columns, groupWidth, false);
    String[] widthSpecs = ColumnWidthCalculator.calculateWidthSpecs(columns);

    for (int i = 0; i < columns.size(); i++) {
      MjmlNode col = columns.get(i);

      // MSO column td
      sb.append("<td style=\"vertical-align:top;width:")
          .append(CssUnitParser.formatInt(widths[i]))
          .append("px;\" >");

      sb.append("<![endif]-->\n");

      // Column context with proper width spec for responsive class
      RenderContext colContext = renderContext
          .withColumnWidth(widths[i], widthSpecs[i])
          .withPosition(i, i == 0, i == columns.size() - 1)
          .withInsideGroup(true);

      BaseComponent component = registry.createComponent(col, globalContext, colContext);
      if (component instanceof BodyComponent bodyComponent) {
        sb.append(bodyComponent.render());
      }

      sb.append("                <!--[if mso | IE]></td>");

      if (i == columns.size() - 1) {
        sb.append("</tr></table><![endif]-->\n");
      }
    }

    sb.append("              </div>\n");

    // Add responsive media query for this group
    registerMediaQuery(responsiveClass, widthSpec);

    return sb.toString();
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

  private static final Set<String> COLUMN_TAGS = Set.of("mj-column");

  private List<MjmlNode> getColumnChildren() {
    return getChildrenByTags(COLUMN_TAGS);
  }

}
