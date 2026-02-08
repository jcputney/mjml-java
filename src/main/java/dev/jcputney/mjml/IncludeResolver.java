package dev.jcputney.mjml;

/**
 * Interface for resolving mj-include paths to content.
 * Implementations can resolve from the file system, classpath, HTTP, etc.
 */
public interface IncludeResolver {

  /**
   * Resolves the given path to its content.
   *
   * @param path the include path from the mj-include element
   * @return the resolved content string
   * @throws MjmlException if the path cannot be resolved
   */
  String resolve(String path);
}
