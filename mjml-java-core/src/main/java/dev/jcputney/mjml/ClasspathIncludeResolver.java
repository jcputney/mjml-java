package dev.jcputney.mjml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Resolves mj-include paths from the classpath.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * MjmlConfiguration config = MjmlConfiguration.builder()
 *     .includeResolver(new ClasspathIncludeResolver())
 *     .build();
 * }</pre>
 */
public final class ClasspathIncludeResolver implements IncludeResolver {

  private final ClassLoader classLoader;

  /**
   * Creates a resolver that uses the thread's context class loader.
   */
  public ClasspathIncludeResolver() {
    this(Thread.currentThread().getContextClassLoader());
  }

  /**
   * Creates a resolver that uses the given class loader.
   *
   * @param classLoader the class loader to use for resource lookups
   */
  public ClasspathIncludeResolver(ClassLoader classLoader) {
    if (classLoader == null) {
      throw new IllegalArgumentException("classLoader cannot be null");
    }
    this.classLoader = classLoader;
  }

  @Override
  public String resolve(String path, ResolverContext context) {
    if (path == null || path.isBlank()) {
      throw new MjmlIncludeException("Include path cannot be empty");
    }

    // Security: reject null bytes that could bypass path checks
    if (path.indexOf('\0') >= 0) {
      throw new MjmlIncludeException("Include path contains null bytes");
    }

    // Security: normalize and prevent path traversal
    String normalized = Path.of(path).normalize().toString();
    if (normalized.startsWith("..")) {
      throw new MjmlIncludeException("Include path cannot traverse above root: " + path);
    }

    // Strip leading slash for classloader compatibility
    String resourcePath = normalized.startsWith("/") ? normalized.substring(1) : normalized;

    try (InputStream is = classLoader.getResourceAsStream(resourcePath)) {
      if (is == null) {
        throw new MjmlIncludeException("Include resource not found on classpath: " + path);
      }
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new MjmlIncludeException("Failed to read include resource: " + path, e);
    }
  }
}
