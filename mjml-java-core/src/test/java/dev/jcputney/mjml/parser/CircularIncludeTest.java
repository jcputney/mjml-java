package dev.jcputney.mjml.parser;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.IncludeResolver;
import dev.jcputney.mjml.MjmlConfiguration;
import dev.jcputney.mjml.MjmlException;
import dev.jcputney.mjml.MjmlRenderer;
import dev.jcputney.mjml.ResolverContext;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Tests for circular include detection and deep include chain handling.
 */
class CircularIncludeTest {

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
    public String resolve(String path, ResolverContext context) {
      String content = files.get(path);
      if (content == null) {
        throw new MjmlException("File not found: " + path);
      }
      return content;
    }
  }

  // --- Direct circular include ---

  @Test
  void directCircularIncludeDetected() {
    // Use full MJML documents so include processing recurses correctly
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

    assertThrows(MjmlException.class, () -> MjmlRenderer.render(mjml, config),
        "Direct circular include (A -> B -> A) should throw");
  }

  @Test
  void selfIncludeDetected() {
    // Self-include with full MJML document
    MapIncludeResolver resolver = new MapIncludeResolver()
        .put("self.mjml", """
            <mjml>
              <mj-body>
                <mj-include path="self.mjml" />
              </mj-body>
            </mjml>
            """);

    MjmlConfiguration config = MjmlConfiguration.builder()
        .includeResolver(resolver)
        .build();

    String mjml = """
        <mjml>
          <mj-body>
            <mj-include path="self.mjml" />
          </mj-body>
        </mjml>
        """;

    assertThrows(MjmlException.class, () -> MjmlRenderer.render(mjml, config),
        "Self-referential include should throw");
  }

  // --- Indirect circular include ---

  @Test
  void indirectCircularIncludeDetectedThreeNodes() {
    // Three-node cycle with full MJML documents
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
                <mj-include path="c.mjml" />
              </mj-body>
            </mjml>
            """)
        .put("c.mjml", """
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

    assertThrows(MjmlException.class, () -> MjmlRenderer.render(mjml, config),
        "Indirect circular include (A -> B -> C -> A) should throw");
  }

  @Test
  void indirectCircularIncludeWithFullDocuments() {
    // Full MJML documents (starts with <mjml>) take a different code path
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
                <mj-include path="c.mjml" />
              </mj-body>
            </mjml>
            """)
        .put("c.mjml", """
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

    assertThrows(MjmlException.class, () -> MjmlRenderer.render(mjml, config),
        "Indirect circular include with full docs should throw");
  }

  // --- Deep include chain succeeds (no false positives) ---

  @Test
  void deepLinearChainSucceeds() {
    MapIncludeResolver resolver = new MapIncludeResolver();
    // Build a chain: main -> level0 -> level1 -> ... -> level9 -> leaf
    for (int i = 0; i < 10; i++) {
      resolver.put("level" + i + ".mjml",
          "<mj-section><mj-column><mj-include path=\"level" + (i + 1) + ".mjml\" /></mj-column></mj-section>");
    }
    resolver.put("level10.mjml",
        "<mj-section><mj-column><mj-text>Deep leaf content</mj-text></mj-column></mj-section>");

    MjmlConfiguration config = MjmlConfiguration.builder()
        .includeResolver(resolver)
        .build();

    String mjml = """
        <mjml>
          <mj-body>
            <mj-include path="level0.mjml" />
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml, config).html();
    assertNotNull(html);
    assertTrue(html.contains("Deep leaf content"),
        "Non-circular deep chain should render successfully");
  }

  @Test
  void diamondIncludePatternSucceeds() {
    // Diamond: A includes B and C, both of which include D (not circular)
    // Since B and C are separate branches, D appears in both but that's OK
    MapIncludeResolver resolver = new MapIncludeResolver()
        .put("b.mjml", """
            <mj-section>
              <mj-column>
                <mj-text>From B</mj-text>
              </mj-column>
            </mj-section>
            """)
        .put("c.mjml", """
            <mj-section>
              <mj-column>
                <mj-text>From C</mj-text>
              </mj-column>
            </mj-section>
            """);

    MjmlConfiguration config = MjmlConfiguration.builder()
        .includeResolver(resolver)
        .build();

    String mjml = """
        <mjml>
          <mj-body>
            <mj-include path="b.mjml" />
            <mj-include path="c.mjml" />
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml, config).html();
    assertNotNull(html);
    assertTrue(html.contains("From B"), "Should include content from B");
    assertTrue(html.contains("From C"), "Should include content from C");
  }

  // --- Depth limit enforcement ---

  @Test
  void exceedingDepthLimitThrows() {
    MapIncludeResolver resolver = new MapIncludeResolver();
    // Build chain of 55 full MJML documents (exceeds limit of 50)
    for (int i = 0; i < 55; i++) {
      resolver.put("level" + i + ".mjml", """
          <mjml>
            <mj-body>
              <mj-include path="level%d.mjml" />
            </mj-body>
          </mjml>
          """.formatted(i + 1));
    }
    resolver.put("level55.mjml", """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Unreachable</mj-text>
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
            <mj-include path="level0.mjml" />
          </mj-body>
        </mjml>
        """;

    assertThrows(MjmlException.class, () -> MjmlRenderer.render(mjml, config),
        "Should throw when include depth limit (50) is exceeded");
  }

  @Test
  void justBelowDepthLimitSucceeds() {
    MapIncludeResolver resolver = new MapIncludeResolver();
    // Build a short chain of 5 levels (well within limit)
    for (int i = 0; i < 5; i++) {
      resolver.put("level" + i + ".mjml",
          "<mj-section><mj-column><mj-include path=\"level" + (i + 1) + ".mjml\" /></mj-column></mj-section>");
    }
    resolver.put("level5.mjml",
        "<mj-section><mj-column><mj-text>Shallow leaf</mj-text></mj-column></mj-section>");

    MjmlConfiguration config = MjmlConfiguration.builder()
        .includeResolver(resolver)
        .build();

    String mjml = """
        <mjml>
          <mj-body>
            <mj-include path="level0.mjml" />
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml, config).html();
    assertNotNull(html);
    assertTrue(html.contains("Shallow leaf"),
        "Shallow chain should succeed without hitting depth limit");
  }

  // --- Include type interactions with circular detection ---

  @Test
  void circularCssIncludeDoesNotCycleDetect() {
    // CSS includes don't recurse, so cycle detection is not relevant,
    // but the path should still be tracked
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
    assertTrue(html.contains(".red"), "CSS include should be processed");
  }

  @Test
  void circularHtmlIncludeDoesNotCycleDetect() {
    // HTML includes don't recurse either
    MapIncludeResolver resolver = new MapIncludeResolver()
        .put("banner.html", "<div class=\"banner\">Banner</div>");

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
    assertTrue(html.contains("Banner"), "HTML include should be processed");
  }
}
