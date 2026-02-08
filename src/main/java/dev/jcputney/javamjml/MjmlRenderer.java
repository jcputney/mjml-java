package dev.jcputney.javamjml;

import dev.jcputney.javamjml.render.RenderPipeline;

/**
 * Public API entry point for rendering MJML templates to HTML.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * // One-liner with defaults
 * String html = MjmlRenderer.render(mjmlString);
 *
 * // With configuration
 * MjmlConfiguration config = MjmlConfiguration.builder()
 *     .language("en")
 *     .build();
 * MjmlRenderResult result = MjmlRenderer.render(mjmlString, config);
 * }</pre>
 */
public final class MjmlRenderer {

  private MjmlRenderer() {
  }

  /**
   * Renders an MJML template to HTML using default configuration.
   *
   * @param mjml the MJML source string
   * @return the rendered HTML string
   * @throws MjmlException if parsing or rendering fails
   */
  public static String render(String mjml) {
    return render(mjml, MjmlConfiguration.defaults()).html();
  }

  /**
   * Renders an MJML template to HTML with the given configuration.
   *
   * @param mjml          the MJML source string
   * @param configuration the rendering configuration
   * @return the render result containing HTML and metadata
   * @throws MjmlException if parsing or rendering fails
   */
  public static MjmlRenderResult render(String mjml, MjmlConfiguration configuration) {
    RenderPipeline pipeline = new RenderPipeline(configuration);
    return pipeline.render(mjml);
  }
}
