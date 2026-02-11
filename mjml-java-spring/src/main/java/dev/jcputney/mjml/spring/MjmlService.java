package dev.jcputney.mjml.spring;

import dev.jcputney.mjml.MjmlConfiguration;
import dev.jcputney.mjml.MjmlRenderResult;
import dev.jcputney.mjml.MjmlRenderer;

/**
 * Spring-managed service for rendering MJML templates to HTML.
 *
 * <p>This is a thin facade over a reusable {@link MjmlRenderer} instance created from the injected
 * {@link MjmlConfiguration}.
 */
public class MjmlService {

  private final MjmlConfiguration configuration;
  private final MjmlRenderer renderer;

  /**
   * Creates a service bound to the provided renderer configuration.
   *
   * @param configuration immutable renderer configuration
   */
  public MjmlService(MjmlConfiguration configuration) {
    this.configuration = configuration;
    this.renderer = MjmlRenderer.create(configuration);
  }

  /**
   * Renders an MJML template to HTML.
   *
   * @param mjml the MJML source string
   * @return the rendered HTML string
   */
  public String render(String mjml) {
    return renderer.renderTemplate(mjml).html();
  }

  /**
   * Renders an MJML template and returns the full result including metadata.
   *
   * @param mjml the MJML source string
   * @return the render result containing HTML, title, and preview text
   */
  public MjmlRenderResult renderResult(String mjml) {
    return renderer.renderTemplate(mjml);
  }

  /**
   * Returns the configuration used by this service.
   *
   * @return active renderer configuration
   */
  public MjmlConfiguration getConfiguration() {
    return configuration;
  }
}
