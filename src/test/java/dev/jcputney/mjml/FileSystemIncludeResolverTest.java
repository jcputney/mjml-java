package dev.jcputney.mjml;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for FileSystemIncludeResolver including path traversal prevention.
 */
class FileSystemIncludeResolverTest {

  @TempDir
  Path tempDir;

  @Test
  void resolvesFileInBaseDirectory() throws IOException {
    Files.writeString(tempDir.resolve("header.mjml"), """
        <mj-section>
          <mj-column>
            <mj-text>Included header</mj-text>
          </mj-column>
        </mj-section>
        """);

    FileSystemIncludeResolver resolver = new FileSystemIncludeResolver(tempDir);
    String content = resolver.resolve("header.mjml");

    assertNotNull(content);
    assertTrue(content.contains("Included header"));
  }

  @Test
  void resolvesFileInSubdirectory() throws IOException {
    Path subDir = Files.createDirectory(tempDir.resolve("partials"));
    Files.writeString(subDir.resolve("footer.mjml"), "<mj-text>Footer</mj-text>");

    FileSystemIncludeResolver resolver = new FileSystemIncludeResolver(tempDir);
    String content = resolver.resolve("partials/footer.mjml");

    assertNotNull(content);
    assertTrue(content.contains("Footer"));
  }

  @Test
  void preventsPathTraversalOutsideBaseDir() {
    FileSystemIncludeResolver resolver = new FileSystemIncludeResolver(tempDir);

    assertThrows(MjmlException.class,
        () -> resolver.resolve("../../../etc/passwd"),
        "Should reject path traversal outside base directory");
  }

  @Test
  void preventsPathTraversalWithDotDot() {
    FileSystemIncludeResolver resolver = new FileSystemIncludeResolver(tempDir);

    assertThrows(MjmlException.class,
        () -> resolver.resolve("subdir/../../outside.mjml"),
        "Should reject path traversal even when starting within base dir");
  }

  @Test
  void throwsOnNonExistentFile() {
    FileSystemIncludeResolver resolver = new FileSystemIncludeResolver(tempDir);

    assertThrows(MjmlException.class,
        () -> resolver.resolve("nonexistent.mjml"),
        "Should throw on non-existent file");
  }

  @Test
  void throwsOnEmptyPath() {
    FileSystemIncludeResolver resolver = new FileSystemIncludeResolver(tempDir);

    assertThrows(MjmlException.class,
        () -> resolver.resolve(""),
        "Should throw on empty path");
  }

  @Test
  void throwsOnNullPath() {
    FileSystemIncludeResolver resolver = new FileSystemIncludeResolver(tempDir);

    assertThrows(MjmlException.class,
        () -> resolver.resolve(null),
        "Should throw on null path");
  }

  @Test
  void integrationWithRenderer() throws IOException {
    Files.writeString(tempDir.resolve("component.mjml"), """
        <mj-section>
          <mj-column>
            <mj-text>From file system</mj-text>
          </mj-column>
        </mj-section>
        """);

    MjmlConfiguration config = MjmlConfiguration.builder()
        .includeResolver(new FileSystemIncludeResolver(tempDir))
        .build();

    String mjml = """
        <mjml>
          <mj-body>
            <mj-include path="component.mjml" />
          </mj-body>
        </mjml>
        """;

    String html = MjmlRenderer.render(mjml, config).html();
    assertNotNull(html);
    assertTrue(html.contains("From file system"));
  }
}
