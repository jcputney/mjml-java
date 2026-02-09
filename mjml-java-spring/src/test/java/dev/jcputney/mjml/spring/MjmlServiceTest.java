package dev.jcputney.mjml.spring;

import static org.assertj.core.api.Assertions.assertThat;

import dev.jcputney.mjml.MjmlConfiguration;
import dev.jcputney.mjml.MjmlRenderResult;
import org.junit.jupiter.api.Test;

class MjmlServiceTest {

  private static final String SIMPLE_MJML = """
      <mjml>
        <mj-head>
          <mj-title>Test</mj-title>
          <mj-preview>Preview</mj-preview>
        </mj-head>
        <mj-body>
          <mj-section>
            <mj-column>
              <mj-text>Hello</mj-text>
            </mj-column>
          </mj-section>
        </mj-body>
      </mjml>
      """;

  private final MjmlService service = new MjmlService(MjmlConfiguration.defaults());

  @Test
  void renderReturnsHtml() {
    String html = service.render(SIMPLE_MJML);

    assertThat(html).contains("<!doctype html>");
    assertThat(html).contains("Hello");
  }

  @Test
  void renderResultReturnsFullResult() {
    MjmlRenderResult result = service.renderResult(SIMPLE_MJML);

    assertThat(result.html()).contains("<!doctype html>");
    assertThat(result.title()).isEqualTo("Test");
    assertThat(result.previewText()).isEqualTo("Preview");
  }

  @Test
  void getConfigurationReturnsConfig() {
    MjmlConfiguration config = MjmlConfiguration.builder().language("en").build();
    MjmlService svc = new MjmlService(config);

    assertThat(svc.getConfiguration()).isSameAs(config);
    assertThat(svc.getConfiguration().getLanguage()).isEqualTo("en");
  }

  @Test
  void renderUsesConfiguredLanguage() {
    MjmlConfiguration config = MjmlConfiguration.builder().language("fr").build();
    MjmlService svc = new MjmlService(config);

    String html = svc.render(SIMPLE_MJML);
    assertThat(html).contains("lang=\"fr\"");
  }
}
