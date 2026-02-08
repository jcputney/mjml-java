package dev.jcputney.mjml.parser;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.IncludeResolver;
import dev.jcputney.mjml.MjmlConfiguration;
import dev.jcputney.mjml.MjmlException;
import dev.jcputney.mjml.MjmlRenderer;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Tests for mj-include resolution.
 */
class IncludeProcessorTest {

  /**
   * Simple in-memory include resolver for testing.
   */
  private static class MapIncludeResolver implements IncludeResolver {

    private final Map<String, String> files = new HashMap<>();

    MapIncludeResolver put(String path, String content) {
      files.put(path, content);
      return this;
    }

    @Override
    public String resolve(String path) {
      String content = files.get(path);
      if (content == null) {
        throw new MjmlException("File not found: " + path);
      }
      return content;
    }
  }

  @Test
  void includesMjmlFragment() {
    MapIncludeResolver resolver = new MapIncludeResolver()
        .put("header.mjml", """
            <mj-section>
              <mj-column>
                <mj-text>Header from include</mj-text>
              </mj-column>
            </mj-section>
            """);

    MjmlConfiguration config = MjmlConfiguration.builder()
        .includeResolver(resolver)
        .build();

    String mjml = """
        <mjml>
          <mj-body>
            <mj-include path="header.mjml" />
            <mj-section>
              <mj-column>
                <mj-text>Main content</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml, config).html();
    assertNotNull(html);
    assertTrue(html.contains("Header from include"), "Should contain included header");
    assertTrue(html.contains("Main content"), "Should contain main content");
  }

  @Test
  void includesHtmlRaw() {
    MapIncludeResolver resolver = new MapIncludeResolver()
        .put("banner.html", "<div class=\"banner\">Custom HTML Banner</div>");

    MjmlConfiguration config = MjmlConfiguration.builder()
        .includeResolver(resolver)
        .build();

    String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-include path="banner.html" type="html" />
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml, config).html();
    assertNotNull(html);
    assertTrue(html.contains("Custom HTML Banner"), "Should contain included HTML");
  }

  @Test
  void includesCss() {
    MapIncludeResolver resolver = new MapIncludeResolver()
        .put("styles.css", ".red { color: red; }");

    MjmlConfiguration config = MjmlConfiguration.builder()
        .includeResolver(resolver)
        .build();

    String mjml = """
        <mjml>
          <mj-head>
            <mj-include path="styles.css" type="css" />
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

    String html = MjmlRenderer.render(mjml, config).html();
    assertNotNull(html);
    assertTrue(html.contains(".red"), "Should include CSS in style block");
  }

  @Test
  void detectsCircularIncludes() {
    MapIncludeResolver resolver = new MapIncludeResolver()
        .put("a.mjml", """
            <mjml>
              <mj-body>
                <mj-include path="b.mjml" />
              </mj-body>
            </mjml>
            """)
        .put("b.mjml", """
            <mjml>
              <mj-body>
                <mj-include path="a.mjml" />
              </mj-body>
            </mjml>
            """);

    MjmlConfiguration config = MjmlConfiguration.builder()
        .includeResolver(resolver)
        .build();

    String mjml = """
        <mjml>
          <mj-body>
            <mj-include path="a.mjml" />
          </mj-body>
        </mjml>
        """;

    assertThrows(MjmlException.class, () -> MjmlRenderer.render(mjml, config));
  }

  @Test
  void includesFullMjmlDocument() {
    MapIncludeResolver resolver = new MapIncludeResolver()
        .put("page.mjml", """
            <mjml>
              <mj-body>
                <mj-section>
                  <mj-column>
                    <mj-text>Full document include</mj-text>
                  </mj-column>
                </mj-section>
              </mj-body>
            </mjml>
            """);

    MjmlConfiguration config = MjmlConfiguration.builder()
        .includeResolver(resolver)
        .build();

    String mjml = """
        <mjml>
          <mj-body>
            <mj-include path="page.mjml" />
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml, config).html();
    assertNotNull(html);
    assertTrue(html.contains("Full document include"));
  }

  @Test
  void throwsOnMissingPath() {
    MapIncludeResolver resolver = new MapIncludeResolver();

    MjmlConfiguration config = MjmlConfiguration.builder()
        .includeResolver(resolver)
        .build();

    String mjml = """
        <mjml>
          <mj-body>
            <mj-include path="nonexistent.mjml" />
          </mj-body>
        </mjml>
        """;

    assertThrows(MjmlException.class, () -> MjmlRenderer.render(mjml, config));
  }

  @Test
  void worksWithoutResolver() {
    // When no include resolver is configured, mj-include should be silently skipped
    // (the node just stays as-is and gets ignored during rendering)
    String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Content without includes</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml);
    assertNotNull(html);
    assertTrue(html.contains("Content without includes"));
  }
}
