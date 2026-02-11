package dev.jcputney.mjml;

import dev.jcputney.mjml.render.RenderPipeline;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Public API entry point for rendering MJML templates to HTML.
 *
 * <p><strong>Thread safety:</strong> Both the static {@code render()} methods and the instance API
 * are thread-safe. Each render call creates its own {@link
 * dev.jcputney.mjml.context.GlobalContext}, so concurrent calls do not share mutable state. The
 * provided {@link MjmlConfiguration} is immutable and safe to share.
 *
 * <h2>Usage</h2>
 *
 * <pre>{@code
 * // One-liner with defaults (convenience, creates a new pipeline each time)
 * MjmlRenderResult result = MjmlRenderer.render(mjmlString);
 * String html = result.html();
 *
 * // Instance API (preferred for repeated rendering — reuses the pipeline)
 * MjmlRenderer renderer = MjmlRenderer.create(config);
 * MjmlRenderResult r1 = renderer.renderTemplate(mjml1);
 * MjmlRenderResult r2 = renderer.renderTemplate(mjml2);
 * }</pre>
 */
public final class MjmlRenderer {

  private final RenderPipeline pipeline;
  private final MjmlConfiguration configuration;

  private MjmlRenderer(MjmlConfiguration configuration) {
    this.configuration = configuration;
    this.pipeline = new RenderPipeline(configuration);
  }

  /**
   * Creates a reusable renderer instance with the given configuration. The instance caches and
   * reuses the internal {@link RenderPipeline} and {@link
   * dev.jcputney.mjml.component.ComponentRegistry}, making it significantly faster for repeated
   * rendering than the static convenience methods.
   *
   * <p>This is the <strong>preferred API</strong> when rendering multiple templates with the same
   * configuration.
   *
   * @param configuration the rendering configuration
   * @return a reusable renderer instance
   */
  public static MjmlRenderer create(MjmlConfiguration configuration) {
    return new MjmlRenderer(configuration);
  }

  /**
   * Creates a reusable renderer instance with default configuration.
   *
   * @return a reusable renderer instance
   */
  public static MjmlRenderer create() {
    return create(MjmlConfiguration.defaults());
  }

  /**
   * Renders an MJML template to HTML using default configuration.
   *
   * <p>For repeated rendering, prefer the instance API via {@link #create(MjmlConfiguration)}.
   *
   * @param mjml the MJML source string
   * @return the render result containing HTML and metadata
   * @throws MjmlException if parsing or rendering fails
   */
  public static MjmlRenderResult render(String mjml) {
    return render(mjml, MjmlConfiguration.defaults());
  }

  /**
   * Renders an MJML template to HTML with the given configuration.
   *
   * <p>For repeated rendering, prefer the instance API via {@link #create(MjmlConfiguration)}.
   *
   * @param mjml the MJML source string
   * @param configuration the rendering configuration
   * @return the render result containing HTML and metadata
   * @throws MjmlException if parsing or rendering fails
   */
  public static MjmlRenderResult render(String mjml, MjmlConfiguration configuration) {
    RenderPipeline pipeline = new RenderPipeline(configuration);
    return pipeline.render(mjml);
  }

  /**
   * Renders an MJML file to HTML using default configuration. Automatically configures a file
   * system include resolver using the file's parent directory.
   *
   * <p>For repeated rendering, prefer the instance API via {@link #create(MjmlConfiguration)}.
   *
   * @param mjmlFile path to the MJML file
   * @return the render result containing HTML and metadata
   * @throws MjmlException if reading, parsing, or rendering fails
   */
  public static MjmlRenderResult render(Path mjmlFile) {
    MjmlConfiguration config =
        MjmlConfiguration.builder()
            .includeResolver(new FileSystemIncludeResolver(mjmlFile.toAbsolutePath().getParent()))
            .build();
    return render(mjmlFile, config);
  }

  // ---- Static convenience methods (create a new pipeline each time) ----

  /**
   * Renders an MJML file to HTML with the given configuration. If no include resolver is
   * configured, one is automatically created using the file's parent directory.
   *
   * <p>For repeated rendering, prefer the instance API via {@link #create(MjmlConfiguration)}.
   *
   * @param mjmlFile path to the MJML file
   * @param configuration the rendering configuration
   * @return the render result containing HTML and metadata
   * @throws MjmlException if reading, parsing, or rendering fails
   */
  public static MjmlRenderResult render(Path mjmlFile, MjmlConfiguration configuration) {
    try {
      String mjml = Files.readString(mjmlFile);
      // Auto-configure include resolver if not set
      if (configuration.getIncludeResolver() == null) {
        configuration =
            configuration.toBuilder()
                .includeResolver(
                    new FileSystemIncludeResolver(mjmlFile.toAbsolutePath().getParent()))
                .build();
      }
      return render(mjml, configuration);
    } catch (IOException e) {
      throw new MjmlException("Failed to read MJML file: " + mjmlFile, e);
    }
  }

  /**
   * Renders an MJML template to HTML using this instance's configuration.
   *
   * @param mjml the MJML source string
   * @return the render result containing HTML and metadata
   * @throws MjmlException if parsing or rendering fails
   */
  public MjmlRenderResult renderTemplate(String mjml) {
    return pipeline.render(mjml);
  }

  /**
   * Renders an MJML file to HTML using this instance's configuration. If no include resolver is
   * configured, one is automatically created using the file's parent directory.
   *
   * @param mjmlFile path to the MJML file
   * @return the render result containing HTML and metadata
   * @throws MjmlException if reading, parsing, or rendering fails
   */
  public MjmlRenderResult renderTemplate(Path mjmlFile) {
    try {
      String mjml = Files.readString(mjmlFile);
      if (configuration.getIncludeResolver() == null) {
        // Need a one-off pipeline with the file-based resolver
        MjmlConfiguration fileConfig =
            configuration.toBuilder()
                .includeResolver(
                    new FileSystemIncludeResolver(mjmlFile.toAbsolutePath().getParent()))
                .build();
        return new RenderPipeline(fileConfig).render(mjml);
      }
      return pipeline.render(mjml);
    } catch (IOException e) {
      throw new MjmlException("Failed to read MJML file: " + mjmlFile, e);
    }
  }

  /**
   * Renders an MJML template to HTML with a specific include resolver, overriding the instance's
   * configured resolver for this call only.
   *
   * @param mjml the MJML source string
   * @param resolver the include resolver to use
   * @return the render result containing HTML and metadata
   * @throws MjmlException if parsing or rendering fails
   */
  public MjmlRenderResult renderTemplate(String mjml, IncludeResolver resolver) {
    MjmlConfiguration resolverConfig = configuration.toBuilder().includeResolver(resolver).build();
    return new RenderPipeline(resolverConfig).render(mjml);
  }
}
