package dev.jcputney.mjml.render;

import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.StyleContext.FontDef;
import dev.jcputney.mjml.context.StyleContext.MediaQuery;
import dev.jcputney.mjml.util.CssEscaper;
import dev.jcputney.mjml.util.HtmlEscaper;
import java.util.Set;

/**
 * Generates the complete HTML document skeleton wrapping the rendered body.
 * Includes DOCTYPE, html/head/body tags, CSS resets, font imports,
 * MSO XML settings, media queries, and preview text.
 */
public final class HtmlSkeleton {

  /** Initial capacity for the HTML output StringBuilder. */
  private static final int INITIAL_BUFFER_CAPACITY = 32768;

  /** MSO PixelsPerInch setting for Outlook rendering. */
  private static final int MSO_PIXELS_PER_INCH = 96;

  /** Max-width breakpoint (px) for fluid-on-mobile responsive styles. */
  private static final int FLUID_MOBILE_BREAKPOINT_PX = 479;

  private HtmlSkeleton() {
  }

  /**
   * Assembles the full HTML document from the rendered body content.
   */
  public static String assemble(String bodyContent, GlobalContext ctx) {
    StringBuilder sb = new StringBuilder(INITIAL_BUFFER_CAPACITY);

    String lang = ctx.getConfiguration().getLanguage();
    if (lang == null || lang.isEmpty()) {
      lang = "und";
    }

    // File-start content (mj-raw position="file-start")
    for (String content : ctx.metadata().getFileStartContent()) {
      sb.append(content).append("\n");
    }

    String dir = ctx.getConfiguration().getDirection().value();

    // DOCTYPE + html tag
    sb.append("<!doctype html>\n");
    sb.append("<html lang=\"").append(escapeHtml(lang)).append("\" dir=\"").append(dir).append("\"");
    sb.append(" xmlns=\"http://www.w3.org/1999/xhtml\"");
    sb.append(" xmlns:v=\"urn:schemas-microsoft-com:vml\"");
    sb.append(" xmlns:o=\"urn:schemas-microsoft-com:office:office\">\n");
    sb.append("\n");

    // Head
    sb.append("<head>\n");
    sb.append("  <title>").append(escapeHtml(ctx.metadata().getTitle())).append("</title>\n");

    // Meta tags
    sb.append("  <!--[if !mso]><!-->\n");
    sb.append("  <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n");
    sb.append("  <!--<![endif]-->\n");
    sb.append("  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n");
    sb.append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n");

    // CSS reset styles (BEFORE MSO noscript)
    sb.append("  <style type=\"text/css\">\n");
    appendBaseStyles(sb);
    sb.append("  </style>\n");

    // MSO office settings
    sb.append("  <!--[if mso]>\n");
    sb.append("    <noscript>\n");
    sb.append("    <xml>\n");
    sb.append("    <o:OfficeDocumentSettings>\n");
    sb.append("      <o:AllowPNG/>\n");
    sb.append("      <o:PixelsPerInch>").append(MSO_PIXELS_PER_INCH).append("</o:PixelsPerInch>\n");
    sb.append("    </o:OfficeDocumentSettings>\n");
    sb.append("    </xml>\n");
    sb.append("    </noscript>\n");
    sb.append("    <![endif]-->\n");

    // MSO lte 11 conditional
    sb.append("  <!--[if lte mso 11]>\n");
    sb.append("    <style type=\"text/css\">\n");
    sb.append("      .mj-outlook-group-fix { width:100% !important; }\n");
    sb.append("    </style>\n");
    sb.append("    <![endif]-->\n");

    // Font imports
    appendFonts(sb, ctx);

    // Media queries
    appendMediaQueries(sb, ctx);

    // Fluid-on-mobile responsive styles + component styles (e.g., hamburger CSS)
    // These share a single <style> block when both are present, matching official MJML output
    boolean hasFluid = ctx.styles().isFluidOnMobileUsed();
    boolean hasComponentStyles = !ctx.styles().getComponentStyles().isEmpty();
    if (hasFluid || hasComponentStyles) {
      sb.append("  <style type=\"text/css\">\n");
      if (hasFluid) {
        sb.append("    @media only screen and (max-width:").append(FLUID_MOBILE_BREAKPOINT_PX).append("px) {\n");
        sb.append("      table.mj-full-width-mobile {\n");
        sb.append("        width: 100% !important;\n");
        sb.append("      }\n");
        sb.append("\n");
        sb.append("      td.mj-full-width-mobile {\n");
        sb.append("        width: auto !important;\n");
        sb.append("      }\n");
        sb.append("    }\n");
        sb.append("\n");
      }
      for (String css : ctx.styles().getComponentStyles()) {
        sb.append(reformatCss(css));
      }
      sb.append("  </style>\n");
    }

    // Custom styles from mj-style (after media queries, matching official MJML order)
    for (String css : ctx.styles().getStyles()) {
      sb.append("  <style type=\"text/css\">\n");
      sb.append(reformatCss(css));
      sb.append("  </style>\n");
    }

    // Head comments (preserved from MJML source)
    for (String comment : ctx.metadata().getHeadComments()) {
      // Strip -- sequences to prevent HTML comment injection
      String safeComment = comment.replace("--", "");
      sb.append("  <!-- ").append(safeComment).append(" -->\n");
    }

    sb.append("</head>\n");
    sb.append("\n");

    // Body
    sb.append("<body style=\"word-spacing:normal;");
    String bodyBgColor = ctx.metadata().getBodyBackgroundColor();
    if (bodyBgColor != null && !bodyBgColor.isEmpty()) {
      sb.append("background-color:").append(bodyBgColor).append(";");
    }
    sb.append("\">\n");

    // Preview text
    if (!ctx.metadata().getPreviewText().isEmpty()) {
      sb.append("  <div style=\"display:none;font-size:1px;color:#ffffff;line-height:1px;max-height:0px;max-width:0px;opacity:0;overflow:hidden;\">");
      sb.append(escapeHtml(ctx.metadata().getPreviewText()));
      sb.append("</div>\n");
    }

    // Body content
    sb.append(bodyContent);

    sb.append("</body>\n");
    sb.append("\n");
    sb.append("</html>\n");

    return sb.toString();
  }

  private static void appendFonts(StringBuilder sb, GlobalContext ctx) {
    if (ctx.styles().getFonts().isEmpty()) {
      return;
    }

    // Wrap link tags + @import in non-MSO conditional
    sb.append("  <!--[if !mso]><!-->\n");
    for (FontDef font : ctx.styles().getFonts()) {
      sb.append("  <link href=\"").append(escapeHtml(font.href()))
          .append("\" rel=\"stylesheet\" type=\"text/css\">\n");
    }
    sb.append("  <style type=\"text/css\">\n");
    for (FontDef font : ctx.styles().getFonts()) {
      // CSS-escape the URL to prevent injection via url() breakout
      String safeUrl = CssEscaper.escapeCssUrl(font.href());
      sb.append("    @import url(\"").append(safeUrl).append("\");\n");
    }
    sb.append("\n");
    sb.append("  </style>\n");
    sb.append("  <!--<![endif]-->\n");
  }

  private static void appendBaseStyles(StringBuilder sb) {
    sb.append("""
            #outlook a {
              padding: 0;
            }

            body {
              margin: 0;
              padding: 0;
              -webkit-text-size-adjust: 100%;
              -ms-text-size-adjust: 100%;
            }

            table,
            td {
              border-collapse: collapse;
              mso-table-lspace: 0pt;
              mso-table-rspace: 0pt;
            }

            img {
              border: 0;
              height: auto;
              line-height: 100%;
              outline: none;
              text-decoration: none;
              -ms-interpolation-mode: bicubic;
            }

            p {
              display: block;
              margin: 13px 0;
            }

        """);
  }

  private static void appendMediaQueries(StringBuilder sb, GlobalContext ctx) {
    Set<MediaQuery> queries = ctx.styles().getMediaQueries();
    if (queries.isEmpty()) {
      return;
    }

    // @media block
    MediaQuery[] queryArr = queries.toArray(new MediaQuery[0]);
    sb.append("  <style type=\"text/css\">\n");
    sb.append("    @media only screen and (min-width:")
        .append(ctx.metadata().getBreakpoint()).append(") {\n");
    for (int i = 0; i < queryArr.length; i++) {
      MediaQuery query = queryArr[i];
      String unit = query.widthUnit().isEmpty() ? "" : query.widthUnit();
      sb.append("      .").append(query.className()).append(" {\n");
      sb.append("        width: ").append(query.widthValue()).append(unit).append(" !important;\n");
      sb.append("        max-width: ").append(query.widthValue()).append(unit).append(";\n");
      sb.append("      }\n");
      if (i < queryArr.length - 1) {
        sb.append("\n");
      }
    }
    sb.append("    }\n");
    sb.append("\n");
    sb.append("  </style>\n");

    // Thunderbird-specific styles (flat selectors, not nested)
    sb.append("  <style media=\"screen and (min-width:").append(ctx.metadata().getBreakpoint()).append(")\">\n");
    for (int i = 0; i < queryArr.length; i++) {
      MediaQuery query = queryArr[i];
      String unit = query.widthUnit().isEmpty() ? "" : query.widthUnit();
      sb.append("    .moz-text-html .").append(query.className()).append(" {\n");
      sb.append("      width: ").append(query.widthValue()).append(unit).append(" !important;\n");
      sb.append("      max-width: ").append(query.widthValue()).append(unit).append(";\n");
      sb.append("    }\n");
      if (i < queryArr.length - 1) {
        sb.append("\n");
      }
    }
    sb.append("\n");
    sb.append("  </style>\n");
  }

  /**
   * Reformats CSS content to use consistent indentation inside a style block.
   * Official MJML uses 4-space indent for rules, 6-space for properties.
   */
  private static String reformatCss(String css) {
    StringBuilder out = new StringBuilder();
    String[] lines = css.split("\n");
    int braceDepth = 0;
    for (String rawLine : lines) {
      String trimmed = rawLine.trim();
      if (trimmed.isEmpty()) {
        out.append("\n");
        continue;
      }
      // Closing brace decreases depth before indenting
      if (trimmed.startsWith("}")) {
        braceDepth--;
        if (braceDepth < 0) {
          braceDepth = 0;
        }
      }
      // Indent: base 4 spaces + 2 per brace depth
      out.append("    ");
      for (int i = 0; i < braceDepth; i++) {
        out.append("  ");
      }
      out.append(trimmed).append("\n");
      // Opening brace increases depth after indenting
      if (trimmed.endsWith("{")) {
        braceDepth++;
      }
    }
    out.append("\n");
    return out.toString();
  }

  private static String escapeHtml(String text) {
    if (text == null) {
      return "";
    }
    return HtmlEscaper.escapeAttributeValue(text);
  }
}
