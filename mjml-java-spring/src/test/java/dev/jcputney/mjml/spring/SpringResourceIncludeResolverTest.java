package dev.jcputney.mjml.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.jcputney.mjml.MjmlIncludeException;
import dev.jcputney.mjml.ResolverContext;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

class SpringResourceIncludeResolverTest {

  private final ResourceLoader resourceLoader = new DefaultResourceLoader();

  @Test
  void resolvesClasspathResource() {
    SpringResourceIncludeResolver resolver =
        new SpringResourceIncludeResolver(resourceLoader, "classpath:mjml/");

    String content = resolver.resolve("test-template.mjml", ResolverContext.root("mjml"));

    assertThat(content).contains("<mj-text>Hello, World!</mj-text>");
  }

  @Test
  void resolvesWithAbsoluteClasspathPrefix() {
    SpringResourceIncludeResolver resolver =
        new SpringResourceIncludeResolver(resourceLoader, "classpath:other/");

    // Absolute classpath path should bypass base location
    String content = resolver.resolve(
        "classpath:mjml/test-template.mjml", ResolverContext.root("mjml"));

    assertThat(content).contains("<mj-text>Hello, World!</mj-text>");
  }

  @Test
  void throwsOnMissingResource() {
    SpringResourceIncludeResolver resolver =
        new SpringResourceIncludeResolver(resourceLoader, "classpath:mjml/");

    assertThatThrownBy(() ->
        resolver.resolve("nonexistent.mjml", ResolverContext.root("mjml")))
        .isInstanceOf(MjmlIncludeException.class)
        .hasMessageContaining("Cannot resolve include path");
  }

  @Test
  void normalizesBaseLocationTrailingSlash() {
    SpringResourceIncludeResolver resolver =
        new SpringResourceIncludeResolver(resourceLoader, "classpath:mjml");

    // Should still work — base location gets trailing slash appended
    String content = resolver.resolve("test-template.mjml", ResolverContext.root("mjml"));
    assertThat(content).contains("<mj-text>Hello, World!</mj-text>");
  }

  @Test
  void defaultConstructorUsesClasspathMjml() {
    SpringResourceIncludeResolver resolver =
        new SpringResourceIncludeResolver(resourceLoader);

    String content = resolver.resolve("test-template.mjml", ResolverContext.root("mjml"));
    assertThat(content).contains("<mj-text>Hello, World!</mj-text>");
  }
}
