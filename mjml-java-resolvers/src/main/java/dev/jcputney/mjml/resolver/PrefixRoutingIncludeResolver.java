package dev.jcputney.mjml.resolver;

import dev.jcputney.mjml.IncludeResolver;
import dev.jcputney.mjml.MjmlIncludeException;
import dev.jcputney.mjml.ResolverContext;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An {@link IncludeResolver} that routes include paths to different resolvers based on prefix
 * matching. The prefix is stripped before delegation.
 *
 * <p>Example: with route "classpath:" → classpathResolver, the path
 * "classpath:templates/header.mjml" is resolved by calling
 * classpathResolver.resolve("templates/header.mjml", context).</p>
 */
public final class PrefixRoutingIncludeResolver implements IncludeResolver {

  private final LinkedHashMap<String, IncludeResolver> routes;
  private final IncludeResolver defaultResolver;

  private PrefixRoutingIncludeResolver(LinkedHashMap<String, IncludeResolver> routes,
      IncludeResolver defaultResolver) {
    this.routes = routes;
    this.defaultResolver = defaultResolver;
  }

  /**
   * Returns a new builder.
   *
   * @return builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  @Override
  public String resolve(String path, ResolverContext context) {
    for (Map.Entry<String, IncludeResolver> entry : routes.entrySet()) {
      if (path.startsWith(entry.getKey())) {
        String strippedPath = path.substring(entry.getKey().length());
        return entry.getValue().resolve(strippedPath, context);
      }
    }

    if (defaultResolver != null) {
      return defaultResolver.resolve(path, context);
    }

    throw new MjmlIncludeException("No resolver matched prefix for path: " + path);
  }

  /**
   * Builder for {@link PrefixRoutingIncludeResolver}.
   */
  public static final class Builder {

    private final LinkedHashMap<String, IncludeResolver> routes = new LinkedHashMap<>();
    private IncludeResolver defaultResolver;

    private Builder() {}

    /**
     * Adds a prefix-to-resolver route. Prefixes are matched in insertion order.
     *
     * @param prefix   the prefix to match (e.g. "classpath:", "https://")
     * @param resolver the resolver to delegate to
     * @return this builder
     */
    public Builder route(String prefix, IncludeResolver resolver) {
      routes.put(prefix, resolver);
      return this;
    }

    /**
     * Sets the default resolver for paths that don't match any prefix.
     *
     * @param resolver the fallback resolver
     * @return this builder
     */
    public Builder defaultResolver(IncludeResolver resolver) {
      this.defaultResolver = resolver;
      return this;
    }

    /**
     * Builds the prefix routing resolver.
     *
     * @return a new {@link PrefixRoutingIncludeResolver}
     */
    public PrefixRoutingIncludeResolver build() {
      return new PrefixRoutingIncludeResolver(new LinkedHashMap<>(routes), defaultResolver);
    }
  }
}
