package dev.jcputney.mjml.resolver;

import dev.jcputney.mjml.IncludeResolver;
import dev.jcputney.mjml.MjmlIncludeException;
import dev.jcputney.mjml.ResolverContext;
import java.util.Arrays;
import java.util.List;

/**
 * An {@link IncludeResolver} that chains multiple resolvers together.
 * The first resolver that succeeds wins; if all fail, the last exception is rethrown.
 */
public final class CompositeIncludeResolver implements IncludeResolver {

  private final List<IncludeResolver> resolvers;

  /**
   * Creates a composite resolver from the given list (defensive copy).
   *
   * @param resolvers the resolvers to chain
   * @throws IllegalArgumentException if the list is empty
   */
  public CompositeIncludeResolver(List<IncludeResolver> resolvers) {
    if (resolvers.isEmpty()) {
      throw new IllegalArgumentException("At least one resolver is required");
    }
    this.resolvers = List.copyOf(resolvers);
  }

  /**
   * Creates a composite resolver from varargs.
   *
   * @param resolvers the resolvers to chain
   * @return a new composite resolver
   * @throws IllegalArgumentException if no resolvers are provided
   */
  public static CompositeIncludeResolver of(IncludeResolver... resolvers) {
    return new CompositeIncludeResolver(Arrays.asList(resolvers));
  }

  /**
   * Attempts resolution in configured order and returns the first successful result.
   *
   * @throws MjmlIncludeException if all delegates fail
   */
  @Override
  public String resolve(String path, ResolverContext context) {
    MjmlIncludeException lastException = null;
    for (IncludeResolver resolver : resolvers) {
      try {
        return resolver.resolve(path, context);
      } catch (MjmlIncludeException e) {
        lastException = e;
      }
    }
    throw lastException;
  }
}
