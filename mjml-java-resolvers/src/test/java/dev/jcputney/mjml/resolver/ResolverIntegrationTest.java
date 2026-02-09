package dev.jcputney.mjml.resolver;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlConfiguration;
import dev.jcputney.mjml.MjmlRenderer;
import dev.jcputney.mjml.MjmlRenderResult;
import org.junit.jupiter.api.Test;

/**
 * Integration test that wires resolvers into MjmlConfiguration and renders MJML with mj-include.
 */
class ResolverIntegrationTest {

  @Test
  void mapResolverWithMjInclude() {
    var resolver = MapIncludeResolver.of(
        "header.mjml",
        "<mj-section><mj-column><mj-text>Included Header</mj-text></mj-column></mj-section>"
    );

    var config = MjmlConfiguration.builder()
        .includeResolver(resolver)
        .build();

    String mjml = """
        <mjml>
          <mj-body>
            <mj-include path="header.mjml" />
            <mj-section>
              <mj-column>
                <mj-text>Body Content</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    MjmlRenderResult result = MjmlRenderer.render(mjml, config);
    assertNotNull(result);
    assertTrue(result.html().contains("Included Header"));
    assertTrue(result.html().contains("Body Content"));
  }

  @Test
  void compositeResolverWithMjInclude() {
    var primary = MapIncludeResolver.of("header.mjml",
        "<mj-section><mj-column><mj-text>Primary Header</mj-text></mj-column></mj-section>");
    var fallback = MapIncludeResolver.of("footer.mjml",
        "<mj-section><mj-column><mj-text>Fallback Footer</mj-text></mj-column></mj-section>");

    var composite = CompositeIncludeResolver.of(primary, fallback);

    var config = MjmlConfiguration.builder()
        .includeResolver(composite)
        .build();

    String mjml = """
        <mjml>
          <mj-body>
            <mj-include path="header.mjml" />
            <mj-include path="footer.mjml" />
          </mj-body>
        </mjml>
        """;

    MjmlRenderResult result = MjmlRenderer.render(mjml, config);
    assertTrue(result.html().contains("Primary Header"));
    assertTrue(result.html().contains("Fallback Footer"));
  }

  @Test
  void cachingResolverWithMjInclude() {
    var delegate = MapIncludeResolver.of("header.mjml",
        "<mj-section><mj-column><mj-text>Cached Header</mj-text></mj-column></mj-section>");
    var caching = CachingIncludeResolver.builder()
        .delegate(delegate)
        .build();

    var config = MjmlConfiguration.builder()
        .includeResolver(caching)
        .build();

    String mjml = """
        <mjml>
          <mj-body>
            <mj-include path="header.mjml" />
          </mj-body>
        </mjml>
        """;

    MjmlRenderResult result = MjmlRenderer.render(mjml, config);
    assertTrue(result.html().contains("Cached Header"));

    // Render again to exercise cache hit
    MjmlRenderResult result2 = MjmlRenderer.render(mjml, config);
    assertTrue(result2.html().contains("Cached Header"));
  }

  @Test
  void prefixRoutingResolverWithMjInclude() {
    var memResolver = MapIncludeResolver.of(
        "templates/header.mjml",
        "<mj-section><mj-column><mj-text>From Memory</mj-text></mj-column></mj-section>"
    );

    var router = PrefixRoutingIncludeResolver.builder()
        .route("mem:", memResolver)
        .build();

    var config = MjmlConfiguration.builder()
        .includeResolver(router)
        .build();

    String mjml = """
        <mjml>
          <mj-body>
            <mj-include path="mem:templates/header.mjml" />
          </mj-body>
        </mjml>
        """;

    MjmlRenderResult result = MjmlRenderer.render(mjml, config);
    assertTrue(result.html().contains("From Memory"));
  }

  @Test
  void cssInlineIncludeWithMapResolver() {
    var resolver = MapIncludeResolver.of(
        "styles.css",
        ".red { color: red; }"
    );

    var config = MjmlConfiguration.builder()
        .includeResolver(resolver)
        .build();

    String mjml = """
        <mjml>
          <mj-head>
            <mj-include path="styles.css" type="css-inline" />
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

    MjmlRenderResult result = MjmlRenderer.render(mjml, config);
    assertNotNull(result);
    assertTrue(result.html().contains("Red text"));
  }
}
