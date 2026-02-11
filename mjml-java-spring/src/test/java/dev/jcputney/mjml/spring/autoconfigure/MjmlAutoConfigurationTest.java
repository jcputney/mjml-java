package dev.jcputney.mjml.spring.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import dev.jcputney.mjml.IncludeResolver;
import dev.jcputney.mjml.MjmlConfiguration;
import dev.jcputney.mjml.spring.MjmlProperties;
import dev.jcputney.mjml.spring.MjmlService;
import dev.jcputney.mjml.spring.SpringResourceIncludeResolver;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

class MjmlAutoConfigurationTest {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(MjmlAutoConfiguration.class));

  @Test
  void autoConfigurationLoadsBeans() {
    contextRunner.run(
        context -> {
          assertThat(context).hasSingleBean(IncludeResolver.class);
          assertThat(context).hasSingleBean(MjmlConfiguration.class);
          assertThat(context).hasSingleBean(MjmlService.class);
          assertThat(context).hasSingleBean(MjmlProperties.class);
        });
  }

  @Test
  void defaultIncludeResolverIsSpringResourceBased() {
    contextRunner.run(
        context -> {
          IncludeResolver resolver = context.getBean(IncludeResolver.class);
          assertThat(resolver).isInstanceOf(SpringResourceIncludeResolver.class);
        });
  }

  @Test
  void propertiesBindCorrectly() {
    contextRunner
        .withPropertyValues(
            "spring.mjml.language=en",
            "spring.mjml.direction=rtl",
            "spring.mjml.sanitize-output=false",
            "spring.mjml.max-input-size=500000",
            "spring.mjml.max-nesting-depth=50",
            "spring.mjml.max-include-depth=25",
            "spring.mjml.include-allowed-schemes=classpath,file",
            "spring.mjml.template-location=classpath:templates/email/")
        .run(
            context -> {
              MjmlConfiguration config = context.getBean(MjmlConfiguration.class);
              assertThat(config.getLanguage()).isEqualTo("en");
              assertThat(config.getDirection().value()).isEqualTo("rtl");
              assertThat(config.isSanitizeOutput()).isFalse();
              assertThat(config.getMaxInputSize()).isEqualTo(500000);
              assertThat(config.getMaxNestingDepth()).isEqualTo(50);
              assertThat(config.getMaxIncludeDepth()).isEqualTo(25);
            });
  }

  @Test
  void customIncludeResolverTakesPrecedence() {
    contextRunner
        .withUserConfiguration(CustomResolverConfig.class)
        .run(
            context -> {
              IncludeResolver resolver = context.getBean(IncludeResolver.class);
              assertThat(resolver).isNotInstanceOf(SpringResourceIncludeResolver.class);
            });
  }

  @Test
  void customMjmlConfigurationTakesPrecedence() {
    contextRunner
        .withUserConfiguration(CustomConfigConfig.class)
        .run(
            context -> {
              MjmlConfiguration config = context.getBean(MjmlConfiguration.class);
              assertThat(config.getLanguage()).isEqualTo("fr");
            });
  }

  @Test
  void customMjmlServiceTakesPrecedence() {
    contextRunner
        .withUserConfiguration(CustomServiceConfig.class)
        .run(
            context -> {
              assertThat(context).hasSingleBean(MjmlService.class);
              MjmlService service = context.getBean(MjmlService.class);
              assertThat(service.getConfiguration().getLanguage()).isEqualTo("de");
            });
  }

  @Configuration(proxyBeanMethods = false)
  static class CustomResolverConfig {
    @Bean
    IncludeResolver includeResolver() {
      return (path, ctx) -> "<mjml><mj-body></mj-body></mjml>";
    }
  }

  @Configuration(proxyBeanMethods = false)
  static class CustomConfigConfig {
    @Bean
    MjmlConfiguration mjmlConfiguration() {
      return MjmlConfiguration.builder().language("fr").build();
    }
  }

  @Configuration(proxyBeanMethods = false)
  static class CustomServiceConfig {
    @Bean
    MjmlService mjmlService() {
      return new MjmlService(MjmlConfiguration.builder().language("de").build());
    }
  }
}
