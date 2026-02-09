package dev.jcputney.mjml.spring.autoconfigure;

import dev.jcputney.mjml.spring.MjmlService;
import dev.jcputney.mjml.spring.ThymeleafMjmlService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;

/**
 * Auto-configuration for Thymeleaf + MJML integration.
 * Only activates when Thymeleaf is on the classpath.
 */
@Configuration
@ConditionalOnClass({TemplateEngine.class, MjmlService.class})
@ConditionalOnProperty(name = "spring.mjml.thymeleaf-enabled", havingValue = "true",
    matchIfMissing = true)
public class MjmlThymeleafAutoConfiguration {

  /**
   * Auto-configures {@link ThymeleafMjmlService} when Thymeleaf is available.
   */
  @Bean
  @ConditionalOnMissingBean
  public ThymeleafMjmlService thymeleafMjmlService(TemplateEngine templateEngine,
      MjmlService mjmlService) {
    return new ThymeleafMjmlService(templateEngine, mjmlService);
  }
}
