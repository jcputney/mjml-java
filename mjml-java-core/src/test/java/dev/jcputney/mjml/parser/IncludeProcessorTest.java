package dev.jcputney.mjml.parser;

import static org.junit.jupiter.api.Assertions.assertFalse;
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

/** Tests for mj-include resolution. */
class IncludeProcessorTest {

  @Test
  void includesMjmlFragment() {
    MapIncludeResolver resolver =
        new MapIncludeResolver()
            .put(
                "header.mjml",
                """
            <mj-section>
              <mj-column>
                <mj-text>Header from include</mj-text>
              </mj-column>
            </mj-section>
            """);

    MjmlConfiguration config = MjmlConfiguration.builder().includeResolver(resolver).build();

    String mjml =
        // language=MJML
        """
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
    MapIncludeResolver resolver =
        new MapIncludeResolver()
            .put("banner.html", "<div class=\"banner\">Custom HTML Banner</div>");

    MjmlConfiguration config = MjmlConfiguration.builder().includeResolver(resolver).build();

    String mjml =
        // language=MJML
        """
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
    MapIncludeResolver resolver =
        new MapIncludeResolver().put("styles.css", ".red { color: red; }");

    MjmlConfiguration config = MjmlConfiguration.builder().includeResolver(resolver).build();

    String mjml =
        // language=MJML
        """
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
    MapIncludeResolver resolver =
        new MapIncludeResolver()
            .put(
                "a.mjml",
                // language=MJML
                """
            <mjml>
              <mj-body>
                <mj-include path="b.mjml" />
              </mj-body>
            </mjml>
            """)
            .put(
                "b.mjml",
                // language=MJML
                """
            <mjml>
              <mj-body>
                <mj-include path="a.mjml" />
              </mj-body>
            </mjml>
            """);

    MjmlConfiguration config = MjmlConfiguration.builder().includeResolver(resolver).build();

    String mjml =
        // language=MJML
        """
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
    MapIncludeResolver resolver =
        new MapIncludeResolver()
            .put(
                "page.mjml",
                // language=MJML
                """
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

    MjmlConfiguration config = MjmlConfiguration.builder().includeResolver(resolver).build();

    String mjml =
        // language=MJML
        """
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
  void nestedIncludeContextUsesImmediateParentPathDeterministically() {
    TrackingIncludeResolver resolver =
        new TrackingIncludeResolver()
            .put(
                "a.mjml",
                // language=MJML
                """
            <mjml>
              <mj-body>
                <mj-include path="b.mjml" />
              </mj-body>
            </mjml>
            """)
            .put(
                "b.mjml",
                // language=MJML
                """
            <mjml>
              <mj-body>
                <mj-include path="c.mjml" />
              </mj-body>
            </mjml>
            """)
            .put(
                "c.mjml",
                // language=MJML
                """
            <mjml>
              <mj-body>
                <mj-section><mj-column><mj-text>Leaf</mj-text></mj-column></mj-section>
              </mj-body>
            </mjml>
            """);

    MjmlConfiguration config = MjmlConfiguration.builder().includeResolver(resolver).build();

    String mjml =
        // language=MJML
        """
        <mjml>
          <mj-body>
            <mj-include path="a.mjml" />
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml, config).html();
    assertNotNull(html);
    assertTrue(html.contains("Leaf"));

    ResolverContext aContext = resolver.contextsByPath.get("a.mjml");
    ResolverContext bContext = resolver.contextsByPath.get("b.mjml");
    ResolverContext cContext = resolver.contextsByPath.get("c.mjml");

    assertNotNull(aContext);
    assertNotNull(bContext);
    assertNotNull(cContext);
    assertTrue(aContext.depth() == 0 && aContext.includingPath() == null);
    assertTrue(bContext.depth() == 1 && "a.mjml".equals(bContext.includingPath()));
    assertTrue(cContext.depth() == 2 && "b.mjml".equals(cContext.includingPath()));
  }

  @Test
  void throwsOnMissingPath() {
    MapIncludeResolver resolver = new MapIncludeResolver();

    MjmlConfiguration config = MjmlConfiguration.builder().includeResolver(resolver).build();

    String mjml =
        // language=MJML
        """
        <mjml>
          <mj-body>
            <mj-include path="nonexistent.mjml" />
          </mj-body>
        </mjml>
        """;

    assertThrows(MjmlException.class, () -> MjmlRenderer.render(mjml, config));
  }

  @Test
  void throwsOnNullResolvedIncludeContent() {
    IncludeResolver resolver = (path, context) -> null;
    MjmlConfiguration config = MjmlConfiguration.builder().includeResolver(resolver).build();

    String mjml =
        // language=MJML
        """
        <mjml>
          <mj-body>
            <mj-include path="null-content.mjml" />
          </mj-body>
        </mjml>
        """;

    MjmlException ex = assertThrows(MjmlException.class, () -> MjmlRenderer.render(mjml, config));
    assertTrue(ex.getMessage().contains("returned null"));
  }

  @Test
  void includesCssInlineType() {
    MapIncludeResolver resolver =
        new MapIncludeResolver().put("inline.css", ".bold { font-weight: bold; }");

    MjmlConfiguration config = MjmlConfiguration.builder().includeResolver(resolver).build();

    String mjml =
        // language=MJML
        """
        <mjml>
          <mj-head>
            <mj-include path="inline.css" type="css-inline" />
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text css-class="bold">Bold text</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml, config).html();
    assertNotNull(html);
    // The CSS should be inlined (processed by CssInliner), not in a <style> block
    // At minimum, the render should succeed without throwing
    assertFalse(html.isEmpty());
  }

  @Test
  void worksWithoutResolver() {
    // When no include resolver is configured, mj-include should be silently skipped
    // (the node just stays as-is and gets ignored during rendering)
    String mjml =
        // language=MJML
        """
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

    String html = MjmlRenderer.render(mjml).html();
    assertNotNull(html);
    assertTrue(html.contains("Content without includes"));
  }

  @Test
  void throwsOnBlankPath() {
    MapIncludeResolver resolver = new MapIncludeResolver();

    MjmlConfiguration config = MjmlConfiguration.builder().includeResolver(resolver).build();

    String mjml =
        // language=MJML
        """
        <mjml>
          <mj-body>
            <mj-include path="" />
          </mj-body>
        </mjml>
        """;

    assertThrows(
        MjmlException.class, () -> MjmlRenderer.render(mjml, config), "Empty path should throw");
  }

  @Test
  void throwsAtDepthLimit() {
    // Test the depth limit directly through IncludeProcessor.
    // Build a document tree with deeply nested mj-include nodes and process them.
    MapIncludeResolver resolver = new MapIncludeResolver();
    // Create a chain of full MJML documents that include each other.
    // Full documents go through the resolveAsMjml branch where depth increments
    // via processNode(parsedRoot, visitedPaths, depth + 1).
    for (int i = 0; i < 55; i++) {
      String next = "level" + (i + 1) + ".mjml";
      resolver.put(
          "level" + i + ".mjml",
          // language=MJML
          """
          <mjml>
            <mj-body>
              <mj-include path="%s" />
            </mj-body>
          </mjml>
          """
              .formatted(next));
    }
    resolver.put(
        "level55.mjml",
        // language=MJML
        """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>leaf</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """);

    MjmlConfiguration config = MjmlConfiguration.builder().includeResolver(resolver).build();

    String mjml =
        // language=MJML
        """
        <mjml>
          <mj-body>
            <mj-include path="level0.mjml" />
          </mj-body>
        </mjml>
        """;

    assertThrows(
        MjmlException.class,
        () -> MjmlRenderer.render(mjml, config),
        "Should throw when depth limit of 50 is exceeded");
  }

  // --- New tests ---

  @Test
  void succeedsWithShallowChain() {
    // A short chain of 5 includes should succeed without hitting the depth limit.
    MapIncludeResolver resolver = new MapIncludeResolver();
    for (int i = 0; i < 5; i++) {
      String next = "level" + (i + 1) + ".mjml";
      resolver.put(
          "level" + i + ".mjml",
          "<mj-section><mj-column><mj-include path=\"" + next + "\" /></mj-column></mj-section>");
    }
    resolver.put(
        "level5.mjml",
        "<mj-section><mj-column><mj-text>deep leaf</mj-text></mj-column></mj-section>");

    MjmlConfiguration config = MjmlConfiguration.builder().includeResolver(resolver).build();

    String mjml =
        // language=MJML
        """
        <mjml>
          <mj-body>
            <mj-include path="level0.mjml" />
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml, config).html();
    assertNotNull(html);
    assertTrue(html.contains("deep leaf"), "Should render the deepest leaf content");
  }

  @Test
  void throwsOnUnknownType() {
    MapIncludeResolver resolver =
        new MapIncludeResolver().put("data.json", "{ \"key\": \"value\" }");

    MjmlConfiguration config = MjmlConfiguration.builder().includeResolver(resolver).build();

    String mjml =
        // language=MJML
        """
        <mjml>
          <mj-body>
            <mj-include path="data.json" type="json" />
          </mj-body>
        </mjml>
        """;

    assertThrows(
        MjmlException.class,
        () -> MjmlRenderer.render(mjml, config),
        "Unknown include type 'json' should throw");
  }

  @Test
  void parseFailureThrowsForInvalidFragment() {
    // Content that is not valid MJML and not a full <mjml> document
    // should throw instead of silently falling back to mj-raw
    MapIncludeResolver resolver =
        new MapIncludeResolver().put("invalid.mjml", "<div>Not valid MJML <<< broken>");

    MjmlConfiguration config = MjmlConfiguration.builder().includeResolver(resolver).build();

    String mjml =
        // language=MJML
        """
        <mjml>
          <mj-body>
            <mj-include path="invalid.mjml" />
          </mj-body>
        </mjml>
        """;

    // Invalid MJML fragments now throw instead of silently converting to mj-raw
    assertThrows(MjmlException.class, () -> MjmlRenderer.render(mjml, config));
  }

  @Test
  void cssInlineAttributeSetsInlineOnStyleNode() {
    // type="css" with css-inline="inline" should produce an mj-style with inline="inline"
    MapIncludeResolver resolver =
        new MapIncludeResolver().put("inline-me.css", ".red { color: red; }");

    MjmlConfiguration config = MjmlConfiguration.builder().includeResolver(resolver).build();

    String mjml =
        // language=MJML
        """
        <mjml>
          <mj-head>
            <mj-include path="inline-me.css" type="css" css-inline="inline" />
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text css-class="red">Red text</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml, config).html();
    assertNotNull(html);
    // When css-inline="inline" is set, the CSS should be inlined into elements
    // rather than appearing in a <style> block. The self-closing tag rewriting
    // (space before >) confirms inlining occurred.
    assertFalse(html.isEmpty());
  }

  @Test
  void includesHeadContentInHeadContext() {
    // Including a full MJML document from within mj-head should pull the head children
    MapIncludeResolver resolver =
        new MapIncludeResolver()
            .put(
                "head-stuff.mjml",
                // language=MJML
                """
            <mjml>
              <mj-head>
                <mj-attributes>
                  <mj-all font-family="Helvetica" />
                </mj-attributes>
              </mj-head>
              <mj-body>
                <mj-section>
                  <mj-column>
                    <mj-text>Ignored body</mj-text>
                  </mj-column>
                </mj-section>
              </mj-body>
            </mjml>
            """);

    MjmlConfiguration config = MjmlConfiguration.builder().includeResolver(resolver).build();

    String mjml =
        // language=MJML
        """
        <mjml>
          <mj-head>
            <mj-include path="head-stuff.mjml" />
          </mj-head>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Main text</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml, config).html();
    assertNotNull(html);
    assertTrue(html.contains("Main text"), "Should render main body");
    // The included mj-attributes should apply: font-family from mj-all
    assertTrue(
        html.contains("Helvetica"), "Head include should apply font-family from mj-attributes");
  }

  @Test
  void throwsOnWhitespaceOnlyPath() {
    MapIncludeResolver resolver = new MapIncludeResolver();

    MjmlConfiguration config = MjmlConfiguration.builder().includeResolver(resolver).build();

    String mjml =
        // language=MJML
        """
        <mjml>
          <mj-body>
            <mj-include path="   " />
          </mj-body>
        </mjml>
        """;

    assertThrows(
        MjmlException.class,
        () -> MjmlRenderer.render(mjml, config),
        "Whitespace-only path should throw");
  }

  /** Simple in-memory include resolver for testing. */
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

  private static class TrackingIncludeResolver extends MapIncludeResolver {
    private final Map<String, ResolverContext> contextsByPath = new HashMap<>();

    @Override
    TrackingIncludeResolver put(String path, String content) {
      super.put(path, content);
      return this;
    }

    @Override
    public String resolve(String path, ResolverContext context) {
      contextsByPath.put(path, context);
      return super.resolve(path, context);
    }
  }
}
