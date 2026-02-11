package dev.jcputney.mjml.resolver;

import dev.jcputney.mjml.IncludeResolver;
import dev.jcputney.mjml.MjmlIncludeException;
import dev.jcputney.mjml.ResolverContext;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An {@link IncludeResolver} backed by an in-memory {@link Map}. Useful for testing or embedding
 * templates directly in code.
 */
public final class MapIncludeResolver implements IncludeResolver {

  private final Map<String, String> templates;

  /**
   * Creates a resolver backed by a defensive copy of the given map.
   *
   * @param templates map of path to content
   */
  public MapIncludeResolver(Map<String, String> templates) {
    this.templates = Map.copyOf(templates);
  }

  /**
   * Creates a resolver from alternating path/content pairs.
   *
   * @param entries alternating path, content, path, content, ...
   * @return a new resolver
   * @throws IllegalArgumentException if an odd number of arguments is supplied
   */
  public static MapIncludeResolver of(String... entries) {
    if (entries.length % 2 != 0) {
      throw new IllegalArgumentException("Entries must be alternating path/content pairs");
    }
    Map<String, String> map = new LinkedHashMap<>();
    for (int i = 0; i < entries.length; i += 2) {
      map.put(entries[i], entries[i + 1]);
    }
    return new MapIncludeResolver(map);
  }

  /**
   * Returns a new builder.
   *
   * @return builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Resolves include content from the backing map.
   *
   * @throws MjmlIncludeException when no entry exists for the requested path
   */
  @Override
  public String resolve(String path, ResolverContext context) {
    String content = templates.get(path);
    if (content == null) {
      throw new MjmlIncludeException("Template not found in map: " + path);
    }
    return content;
  }

  /** Builder for {@link MapIncludeResolver}. */
  public static final class Builder {

    private final Map<String, String> map = new LinkedHashMap<>();

    private Builder() {}

    /**
     * Adds a path/content mapping.
     *
     * @param path the include path
     * @param content the template content
     * @return this builder
     */
    public Builder put(String path, String content) {
      map.put(path, content);
      return this;
    }

    /**
     * Builds the resolver.
     *
     * @return a new {@link MapIncludeResolver}
     */
    public MapIncludeResolver build() {
      return new MapIncludeResolver(map);
    }
  }
}
