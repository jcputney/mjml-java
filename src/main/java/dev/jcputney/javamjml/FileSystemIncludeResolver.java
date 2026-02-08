package dev.jcputney.javamjml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Resolves mj-include paths from the file system relative to a base directory.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * MjmlConfiguration config = MjmlConfiguration.builder()
 *     .includeResolver(new FileSystemIncludeResolver(Path.of("/templates")))
 *     .build();
 * }</pre>
 */
public class FileSystemIncludeResolver implements IncludeResolver {

  private final Path baseDir;

  /**
   * Creates a resolver that resolves paths relative to the given base directory.
   *
   * @param baseDir the base directory for relative include paths
   */
  public FileSystemIncludeResolver(Path baseDir) {
    this.baseDir = baseDir.toAbsolutePath().normalize();
  }

  @Override
  public String resolve(String path) {
    if (path == null || path.isBlank()) {
      throw new MjmlException("Include path cannot be empty");
    }

    Path resolved = baseDir.resolve(path).normalize();

    // Security: prevent path traversal outside base directory
    if (!resolved.startsWith(baseDir)) {
      throw new MjmlException(
          "Include path escapes base directory: " + path);
    }

    if (!Files.exists(resolved)) {
      throw new MjmlException("Include file not found: " + resolved);
    }

    try {
      return Files.readString(resolved);
    } catch (IOException e) {
      throw new MjmlException("Failed to read include file: " + resolved, e);
    }
  }
}
