package dev.jcputney.mjml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for the {@link MjmlRenderer} instance API: create(), renderTemplate().
 */
class MjmlRendererInstanceApiTest {

  private static final String SIMPLE_MJML = """
      <mjml>
        <mj-body>
          <mj-section>
            <mj-column>
              <mj-text>Hello Instance API</mj-text>
            </mj-column>
          </mj-section>
        </mj-body>
      </mjml>
      """;

  private static final String TITLED_MJML = """
      <mjml>
        <mj-head>
          <mj-title>Instance Title</mj-title>
        </mj-head>
        <mj-body>
          <mj-section>
            <mj-column>
              <mj-text>Content</mj-text>
            </mj-column>
          </mj-section>
        </mj-body>
      </mjml>
      """;

  // -- create() with defaults --

  @Test
  void createWithDefaultsProducesValidRenderer() {
    MjmlRenderer renderer = MjmlRenderer.create();
    assertNotNull(renderer);
  }

  @Test
  void createWithDefaultsRendersSimpleTemplate() {
    MjmlRenderer renderer = MjmlRenderer.create();
    MjmlRenderResult result = renderer.renderTemplate(SIMPLE_MJML);

    assertNotNull(result);
    assertNotNull(result.html());
    assertTrue(result.html().contains("Hello Instance API"));
    assertTrue(result.html().contains("<!doctype html>"));
  }

  // -- create(config) --

  @Test
  void createWithConfigRendersWithLanguage() {
    MjmlConfiguration config = MjmlConfiguration.builder()
        .language("fr")
        .build();
    MjmlRenderer renderer = MjmlRenderer.create(config);
    MjmlRenderResult result = renderer.renderTemplate(SIMPLE_MJML);

    assertNotNull(result);
    assertTrue(result.html().contains("lang=\"fr\""),
        "Instance API should respect language configuration");
  }

  @Test
  void createWithConfigRespectsSanitization() {
    MjmlConfiguration config = MjmlConfiguration.builder()
        .sanitizeOutput(true)
        .build();
    MjmlRenderer renderer = MjmlRenderer.create(config);

    String mjmlWithJsHref = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-button href="javascript:alert(1)">Click</mj-button>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    MjmlRenderResult result = renderer.renderTemplate(mjmlWithJsHref);
    assertFalse(result.html().contains("javascript:"),
        "Instance API should sanitize href values");
  }

  // -- renderTemplate(String) --

  @Test
  void renderTemplateStringWithTitle() {
    MjmlRenderer renderer = MjmlRenderer.create();
    MjmlRenderResult result = renderer.renderTemplate(TITLED_MJML);

    assertEquals("Instance Title", result.title());
    assertTrue(result.html().contains("<title>Instance Title</title>"));
  }

  @Test
  void renderTemplateStringThrowsOnNull() {
    MjmlRenderer renderer = MjmlRenderer.create();
    assertThrows(MjmlException.class, () -> renderer.renderTemplate((String) null),
        "renderTemplate(null) should throw");
  }

  @Test
  void renderTemplateStringThrowsOnEmpty() {
    MjmlRenderer renderer = MjmlRenderer.create();
    assertThrows(MjmlException.class, () -> renderer.renderTemplate(""),
        "renderTemplate('') should throw");
  }

  // -- renderTemplate(Path) --

  @Test
  void renderTemplatePathRendersFileContent(@TempDir Path tempDir) throws IOException {
    Path mjmlFile = tempDir.resolve("test.mjml");
    Files.writeString(mjmlFile, SIMPLE_MJML);

    MjmlRenderer renderer = MjmlRenderer.create();
    MjmlRenderResult result = renderer.renderTemplate(mjmlFile);

    assertNotNull(result);
    assertTrue(result.html().contains("Hello Instance API"),
        "Path-based rendering should produce correct output");
  }

  @Test
  void renderTemplatePathThrowsForMissingFile(@TempDir Path tempDir) {
    Path missingFile = tempDir.resolve("nonexistent.mjml");
    MjmlRenderer renderer = MjmlRenderer.create();
    assertThrows(MjmlException.class, () -> renderer.renderTemplate(missingFile),
        "renderTemplate with missing file should throw");
  }

  // -- Instance reuses pipeline across multiple renders --

  @Test
  void instanceReusesAcrossMultipleRenders() {
    MjmlRenderer renderer = MjmlRenderer.create();

    MjmlRenderResult result1 = renderer.renderTemplate(SIMPLE_MJML);
    MjmlRenderResult result2 = renderer.renderTemplate(TITLED_MJML);

    // Both should produce valid output
    assertTrue(result1.html().contains("Hello Instance API"));
    assertTrue(result2.html().contains("Content"));
    assertEquals("Instance Title", result2.title());
  }

  @Test
  void instanceProducesSameOutputAsStaticApi() {
    MjmlConfiguration config = MjmlConfiguration.builder()
        .language("en")
        .build();

    // Static API
    String staticHtml = MjmlRenderer.render(SIMPLE_MJML, config).html();

    // Instance API
    MjmlRenderer renderer = MjmlRenderer.create(config);
    String instanceHtml = renderer.renderTemplate(SIMPLE_MJML).html();

    // Output should be identical
    assertEquals(staticHtml, instanceHtml,
        "Instance API should produce identical output to static API");
  }

  @Test
  void multipleRendersWithSameInstanceProduceSameOutput() {
    MjmlRenderer renderer = MjmlRenderer.create();

    String html1 = renderer.renderTemplate(SIMPLE_MJML).html();
    String html2 = renderer.renderTemplate(SIMPLE_MJML).html();

    assertEquals(html1, html2,
        "Multiple renders of the same template should produce identical output");
  }

  // -- renderTemplate(String, IncludeResolver) --

  @Test
  void renderTemplateWithResolverOverride(@TempDir Path tempDir) throws IOException {
    Path partialFile = tempDir.resolve("partial.mjml");
    Files.writeString(partialFile, """
        <mj-text>Included Content</mj-text>
        """);

    String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-include path="partial.mjml" />
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    MjmlRenderer renderer = MjmlRenderer.create();
    IncludeResolver resolver = new FileSystemIncludeResolver(tempDir);
    MjmlRenderResult result = renderer.renderTemplate(mjml, resolver);

    assertNotNull(result);
    assertTrue(result.html().contains("Included Content"),
        "Resolver override should resolve includes correctly");
  }
}
