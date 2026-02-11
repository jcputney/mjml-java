package dev.jcputney.mjml;

/**
 * Interface for resolving mj-include paths to content. Implementations can resolve from the file
 * system, classpath, HTTP, etc.
 *
 * <p><strong>Security warning:</strong> Implementations that resolve paths over HTTP or other
 * network protocols are vulnerable to Server-Side Request Forgery (SSRF) attacks. An attacker who
 * controls MJML input could use {@code <mj-include>} to probe internal network resources.
 * HTTP-based resolvers should validate and restrict allowed hosts/URLs.
 */
@FunctionalInterface
public interface IncludeResolver {

  /**
   * Resolves the given path to its content.
   *
   * @param path the include path from the mj-include element
   * @param context metadata about the include chain (including path, type, depth)
   * @return the resolved content string
   * @throws MjmlException if the path cannot be resolved
   */
  String resolve(String path, ResolverContext context);
}
