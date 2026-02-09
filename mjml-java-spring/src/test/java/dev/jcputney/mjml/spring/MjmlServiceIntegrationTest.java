package dev.jcputney.mjml.spring;

import static org.assertj.core.api.Assertions.assertThat;

import dev.jcputney.mjml.MjmlConfiguration;
import dev.jcputney.mjml.MjmlRenderResult;
import dev.jcputney.mjml.spring.autoconfigure.MjmlAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;

@SpringBootTest(classes = MjmlServiceIntegrationTest.TestConfig.class)
class MjmlServiceIntegrationTest {

  @Configuration(proxyBeanMethods = false)
  @ImportAutoConfiguration(MjmlAutoConfiguration.class)
  static class TestConfig {
  }

  @Autowired
  private MjmlService mjmlService;

  @Autowired
  private MjmlConfiguration mjmlConfiguration;

  @Test
  void serviceIsWired() {
    assertThat(mjmlService).isNotNull();
    assertThat(mjmlConfiguration).isNotNull();
  }

  @Test
  void rendersSimpleMjml() {
    String mjml = """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Integration test content</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = mjmlService.render(mjml);
    assertThat(html).contains("<!doctype html>");
    assertThat(html).contains("Integration test content");
  }

  @Test
  void rendersWithMetadata() {
    String mjml = """
        <mjml>
          <mj-head>
            <mj-title>Integration Title</mj-title>
            <mj-preview>Integration Preview</mj-preview>
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

    MjmlRenderResult result = mjmlService.renderResult(mjml);
    assertThat(result.title()).isEqualTo("Integration Title");
    assertThat(result.previewText()).isEqualTo("Integration Preview");
    assertThat(result.html()).contains("Content");
  }
}
