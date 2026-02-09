package dev.jcputney.mjml;

/**
 * Context information passed to {@link IncludeResolver} when resolving includes.
 * Provides metadata about the include chain: which file is doing the including,
 * what type of include it is, and how deep in the include chain we are.
 *
 * @param includingPath the path of the file that contains the mj-include, or null for the root document
 * @param includeType   the include type (e.g. "mjml", "html", "css", "css-inline")
 * @param depth         the current nesting depth (0 for top-level includes)
 */
public record ResolverContext(String includingPath, String includeType, int depth) {

  /**
   * Creates a root context for top-level includes.
   *
   * @param includeType the include type
   * @return a new context with null includingPath and depth 0
   */
  public static ResolverContext root(String includeType) {
    return new ResolverContext(null, includeType, 0);
  }

  /**
   * Creates a nested context for includes within included files.
   *
   * @param newIncludingPath the path of the file that contains the nested include
   * @return a new context with incremented depth
   */
  public ResolverContext nested(String newIncludingPath) {
    return new ResolverContext(newIncludingPath, includeType, depth + 1);
  }
}
