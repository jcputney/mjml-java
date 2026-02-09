package dev.jcputney.mjml.spring;

import dev.jcputney.mjml.IncludeResolver;
import dev.jcputney.mjml.MjmlIncludeException;
import dev.jcputney.mjml.ResolverContext;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * {@link IncludeResolver} that uses Spring's {@link ResourceLoader} to resolve include paths.
 * Supports classpath and file prefixes by default.
 */
public class SpringResourceIncludeResolver implements IncludeResolver {

  private final ResourceLoader resourceLoader;
  private final String baseLocation;
  private final Set<String> allowedSchemes;

  /**
   * Creates a resolver with the given resource loader and base location.
   *
   * @param resourceLoader the Spring resource loader
   * @param baseLocation   the base location for relative paths (e.g. "classpath:mjml/")
   */
  public SpringResourceIncludeResolver(ResourceLoader resourceLoader, String baseLocation) {
    this(resourceLoader, baseLocation, Set.of("classpath", "file"));
  }

  /**
   * Creates a resolver with the given resource loader, base location, and allowed schemes.
   *
   * @param resourceLoader the Spring resource loader
   * @param baseLocation   the base location for relative paths (e.g. "classpath:mjml/")
   * @param allowedSchemes allowed resource schemes (e.g. classpath, file)
   */
  public SpringResourceIncludeResolver(ResourceLoader resourceLoader, String baseLocation,
      Set<String> allowedSchemes) {
    this.resourceLoader = resourceLoader;
    this.baseLocation = normalizeBaseLocation(baseLocation);
    this.allowedSchemes = normalizeAllowedSchemes(allowedSchemes);
  }

  /**
   * Creates a resolver with the given resource loader and default base location.
   *
   * @param resourceLoader the Spring resource loader
   */
  public SpringResourceIncludeResolver(ResourceLoader resourceLoader) {
    this(resourceLoader, "classpath:mjml/", Set.of("classpath", "file"));
  }

  /**
   * Resolves include content via Spring resource locations.
   * Relative paths are resolved against {@code baseLocation}.
   *
   * @throws MjmlIncludeException on disallowed scheme, missing resource, or read failure
   */
  @Override
  public String resolve(String path, ResolverContext context) {
    String resourcePath;
    if (extractScheme(path) != null) {
      resourcePath = path;
    } else {
      resourcePath = baseLocation + path;
    }

    String scheme = extractScheme(resourcePath);
    if (scheme != null && !allowedSchemes.contains(scheme)) {
      throw new MjmlIncludeException("Include scheme not allowed: " + scheme);
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

  private static Set<String> normalizeAllowedSchemes(Set<String> schemes) {
    if (schemes == null || schemes.isEmpty()) {
      return Set.of("classpath", "file");
    }
    return schemes.stream()
        .filter(s -> s != null && !s.isBlank())
        .map(s -> s.toLowerCase(Locale.ROOT))
        .collect(java.util.stream.Collectors.toUnmodifiableSet());
  }

  private static String extractScheme(String resourcePath) {
    int colonIndex = resourcePath.indexOf(':');
    if (colonIndex <= 0) {
      return null;
    }
    String candidate = resourcePath.substring(0, colonIndex);
    if (!candidate.matches("[A-Za-z][A-Za-z0-9+.-]*")) {
      return null;
    }
    return candidate.toLowerCase(Locale.ROOT);
  }
}
