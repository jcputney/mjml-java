package dev.jcputney.mjml.spring;

import dev.jcputney.mjml.IncludeResolver;
import dev.jcputney.mjml.MjmlIncludeException;
import dev.jcputney.mjml.ResolverContext;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * {@link IncludeResolver} that uses Spring's {@link ResourceLoader} to resolve include paths.
 * Supports classpath:, file:, and http: prefixes.
 */
public class SpringResourceIncludeResolver implements IncludeResolver {

  private final ResourceLoader resourceLoader;
  private final String baseLocation;

  /**
   * Creates a resolver with the given resource loader and base location.
   *
   * @param resourceLoader the Spring resource loader
   * @param baseLocation   the base location for relative paths (e.g. "classpath:mjml/")
   */
  public SpringResourceIncludeResolver(ResourceLoader resourceLoader, String baseLocation) {
    this.resourceLoader = resourceLoader;
    this.baseLocation = normalizeBaseLocation(baseLocation);
  }

  /**
   * Creates a resolver with the given resource loader and default base location.
   *
   * @param resourceLoader the Spring resource loader
   */
  public SpringResourceIncludeResolver(ResourceLoader resourceLoader) {
    this(resourceLoader, "classpath:mjml/");
  }

  @Override
  public String resolve(String path, ResolverContext context) {
    String resourcePath;
    if (path.contains("://") || path.startsWith("classpath:")) {
      resourcePath = path;
    } else {
      resourcePath = baseLocation + path;
    }

    Resource resource = resourceLoader.getResource(resourcePath);
    if (!resource.exists() || !resource.isReadable()) {
      throw new MjmlIncludeException("Cannot resolve include path: " + path
          + " (resolved to: " + resourcePath + ")");
    }

    try (InputStream is = resource.getInputStream()) {
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new MjmlIncludeException("Failed to read include: " + path, e);
    }
  }

  private static String normalizeBaseLocation(String location) {
    if (location == null || location.isEmpty()) {
      return "";
    }
    return location.endsWith("/") ? location : location + "/";
  }
}
