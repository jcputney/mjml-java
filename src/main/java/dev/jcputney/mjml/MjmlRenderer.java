package dev.jcputney.mjml;

import dev.jcputney.mjml.render.RenderPipeline;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Public API entry point for rendering MJML templates to HTML.
 *
 * <p><strong>Thread safety:</strong> The static {@code render()} methods are thread-safe.
 * Each call creates its own {@link dev.jcputney.mjml.render.RenderPipeline} and
 * {@link dev.jcputney.mjml.context.GlobalContext}, so concurrent calls do not share
 * mutable state. The provided {@link MjmlConfiguration} is immutable and safe to share.</p>
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

  /**
   * Renders an MJML file to HTML using default configuration.
   * Automatically configures a file system include resolver using the file's parent directory.
   *
   * @param mjmlFile path to the MJML file
   * @return the rendered HTML string
   * @throws MjmlException if reading, parsing, or rendering fails
   */
  public static String render(Path mjmlFile) {
    MjmlConfiguration config = MjmlConfiguration.builder()
        .includeResolver(new FileSystemIncludeResolver(mjmlFile.toAbsolutePath().getParent()))
        .build();
    return render(mjmlFile, config).html();
  }

  /**
   * Renders an MJML file to HTML with the given configuration.
   * If no include resolver is configured, one is automatically created using
   * the file's parent directory.
   *
   * @param mjmlFile      path to the MJML file
   * @param configuration the rendering configuration
   * @return the render result containing HTML and metadata
   * @throws MjmlException if reading, parsing, or rendering fails
   */
  public static MjmlRenderResult render(Path mjmlFile, MjmlConfiguration configuration) {
    try {
      String mjml = Files.readString(mjmlFile);
      // Auto-configure include resolver if not set
      if (configuration.getIncludeResolver() == null) {
        configuration = configuration.toBuilder()
            .includeResolver(new FileSystemIncludeResolver(mjmlFile.toAbsolutePath().getParent()))
            .build();
      }
      return render(mjml, configuration);
    } catch (IOException e) {
      throw new MjmlException("Failed to read MJML file: " + mjmlFile, e);
    }
  }
}
