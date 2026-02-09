package dev.jcputney.mjml.spring.autoconfigure;

import dev.jcputney.mjml.IncludeResolver;
import dev.jcputney.mjml.MjmlConfiguration;
import dev.jcputney.mjml.MjmlRenderer;
import dev.jcputney.mjml.spring.MjmlProperties;
import dev.jcputney.mjml.spring.MjmlService;
import dev.jcputney.mjml.spring.SpringResourceIncludeResolver;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ResourceLoader;

/**
 * Auto-configuration for mjml-java.
 * Provides default beans for {@link IncludeResolver}, {@link MjmlConfiguration},
 * and {@link MjmlService}.
 */
@AutoConfiguration
@EnableConfigurationProperties(MjmlProperties.class)
@ConditionalOnClass(MjmlRenderer.class)
public class MjmlAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public IncludeResolver mjmlIncludeResolver(ResourceLoader resourceLoader,
      MjmlProperties properties) {
    return new SpringResourceIncludeResolver(resourceLoader, properties.getTemplateLocation());
  }

  @Bean
  @ConditionalOnMissingBean
  public MjmlConfiguration mjmlConfiguration(MjmlProperties properties,
      IncludeResolver includeResolver) {
    return MjmlConfiguration.builder()
        .language(properties.getLanguage())
        .direction(properties.getDirection())
        .sanitizeOutput(properties.isSanitizeOutput())
        .maxInputSize(properties.getMaxInputSize())
        .maxNestingDepth(properties.getMaxNestingDepth())
        .includeResolver(includeResolver)
        .build();
  }

  @Bean
  @ConditionalOnMissingBean
  public MjmlService mjmlService(MjmlConfiguration configuration) {
    return new MjmlService(configuration);
  }
}
