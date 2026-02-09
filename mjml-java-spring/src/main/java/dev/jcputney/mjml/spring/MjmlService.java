package dev.jcputney.mjml.spring;

import dev.jcputney.mjml.MjmlConfiguration;
import dev.jcputney.mjml.MjmlRenderResult;
import dev.jcputney.mjml.MjmlRenderer;

/**
 * Spring-managed service for rendering MJML templates to HTML.
 */
public class MjmlService {

  private final MjmlConfiguration configuration;

  public MjmlService(MjmlConfiguration configuration) {
    this.configuration = configuration;
  }

  /**
   * Renders an MJML template to HTML.
   *
   * @param mjml the MJML source string
   * @return the rendered HTML string
   */
  public String render(String mjml) {
    return MjmlRenderer.render(mjml, configuration).html();
  }

  /**
   * Renders an MJML template and returns the full result including metadata.
   *
   * @param mjml the MJML source string
   * @return the render result containing HTML, title, and preview text
   */
  public MjmlRenderResult renderResult(String mjml) {
    return MjmlRenderer.render(mjml, configuration);
  }

  /**
   * Returns the configuration used by this service.
   */
  public MjmlConfiguration getConfiguration() {
    return configuration;
  }
}
