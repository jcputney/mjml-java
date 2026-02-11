package dev.jcputney.mjml.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.jcputney.mjml.MjmlIncludeException;
import dev.jcputney.mjml.ResolverContext;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
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
    String content =
        resolver.resolve("classpath:mjml/test-template.mjml", ResolverContext.root("mjml"));

    assertThat(content).contains("<mj-text>Hello, World!</mj-text>");
  }

  @Test
  void throwsOnMissingResource() {
    SpringResourceIncludeResolver resolver =
        new SpringResourceIncludeResolver(resourceLoader, "classpath:mjml/");

    assertThatThrownBy(() -> resolver.resolve("nonexistent.mjml", ResolverContext.root("mjml")))
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
    SpringResourceIncludeResolver resolver = new SpringResourceIncludeResolver(resourceLoader);

    String content = resolver.resolve("test-template.mjml", ResolverContext.root("mjml"));
    assertThat(content).contains("<mj-text>Hello, World!</mj-text>");
  }

  @Test
  void rejectsHttpSchemeByDefault() {
    SpringResourceIncludeResolver resolver =
        new SpringResourceIncludeResolver(resourceLoader, "classpath:mjml/");

    assertThatThrownBy(
            () ->
                resolver.resolve("http://example.com/template.mjml", ResolverContext.root("mjml")))
        .isInstanceOf(MjmlIncludeException.class)
        .hasMessageContaining("scheme not allowed");
  }

  @Test
  void rejectsDisallowedSchemeFromBaseLocation() {
    SpringResourceIncludeResolver resolver =
        new SpringResourceIncludeResolver(resourceLoader, "http://example.com/templates/");

    assertThatThrownBy(() -> resolver.resolve("header.mjml", ResolverContext.root("mjml")))
        .isInstanceOf(MjmlIncludeException.class)
        .hasMessageContaining("scheme not allowed");
  }

  @Test
  void allowsHttpWhenExplicitlyConfigured() {
    ResourceLoader stubLoader =
        new ResourceLoader() {
          @Override
          public Resource getResource(String location) {
            return new ByteArrayResource(
                "<mj-text>Remote</mj-text>".getBytes(StandardCharsets.UTF_8));
          }

          @Override
          public ClassLoader getClassLoader() {
            return getClass().getClassLoader();
          }
        };

    SpringResourceIncludeResolver resolver =
        new SpringResourceIncludeResolver(
            stubLoader,
            "http://example.com/templates/",
            Set.of("http", "https", "classpath", "file"));

    String content = resolver.resolve("header.mjml", ResolverContext.root("mjml"));
    assertThat(content).contains("Remote");
  }
}
