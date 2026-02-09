package dev.jcputney.mjml;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for FileSystemIncludeResolver including path traversal prevention.
 */
class FileSystemIncludeResolverTest {

  private static final ResolverContext TEST_CONTEXT = ResolverContext.root("mjml");

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
    String content = resolver.resolve("header.mjml", TEST_CONTEXT);

    assertNotNull(content);
    assertTrue(content.contains("Included header"));
  }

  @Test
  void resolvesFileInSubdirectory() throws IOException {
    Path subDir = Files.createDirectory(tempDir.resolve("partials"));
    Files.writeString(subDir.resolve("footer.mjml"), "<mj-text>Footer</mj-text>");

    FileSystemIncludeResolver resolver = new FileSystemIncludeResolver(tempDir);
    String content = resolver.resolve("partials/footer.mjml", TEST_CONTEXT);

    assertNotNull(content);
    assertTrue(content.contains("Footer"));
  }

  @Test
  void preventsPathTraversalOutsideBaseDir() {
    FileSystemIncludeResolver resolver = new FileSystemIncludeResolver(tempDir);

    assertThrows(MjmlException.class,
        () -> resolver.resolve("../../../etc/passwd", TEST_CONTEXT),
        "Should reject path traversal outside base directory");
  }

  @Test
  void preventsPathTraversalWithDotDot() {
    FileSystemIncludeResolver resolver = new FileSystemIncludeResolver(tempDir);

    assertThrows(MjmlException.class,
        () -> resolver.resolve("subdir/../../outside.mjml", TEST_CONTEXT),
        "Should reject path traversal even when starting within base dir");
  }

  @Test
  void throwsOnNonExistentFile() {
    FileSystemIncludeResolver resolver = new FileSystemIncludeResolver(tempDir);

    assertThrows(MjmlException.class,
        () -> resolver.resolve("nonexistent.mjml", TEST_CONTEXT),
        "Should throw on non-existent file");
  }

  @Test
  void throwsOnEmptyPath() {
    FileSystemIncludeResolver resolver = new FileSystemIncludeResolver(tempDir);

    assertThrows(MjmlException.class,
        () -> resolver.resolve("", TEST_CONTEXT),
        "Should throw on empty path");
  }

  @Test
  void throwsOnNullPath() {
    FileSystemIncludeResolver resolver = new FileSystemIncludeResolver(tempDir);

    assertThrows(MjmlException.class,
        () -> resolver.resolve(null, TEST_CONTEXT),
        "Should throw on null path");
  }

  @Test
  void preventsSymlinkEscapeOutsideBaseDir() throws IOException {
    Path outsideDir = Files.createTempDirectory("mjml-outside-");
    try {
      Path outsideFile = outsideDir.resolve("outside.mjml");
      Files.writeString(outsideFile, "<mj-text>Outside</mj-text>");

      Path symlink = tempDir.resolve("linked-outside.mjml");
      try {
        Files.createSymbolicLink(symlink, outsideFile);
      } catch (UnsupportedOperationException | SecurityException e) {
        Assumptions.assumeTrue(false, "Symlinks are not supported in this environment");
      } catch (IOException e) {
        Assumptions.assumeTrue(false, "Cannot create symlink in this environment");
      }

      FileSystemIncludeResolver resolver = new FileSystemIncludeResolver(tempDir);
      assertThrows(MjmlException.class,
          () -> resolver.resolve("linked-outside.mjml", TEST_CONTEXT),
          "Should reject symlink targets that escape base directory");
    } finally {
      Files.deleteIfExists(outsideDir.resolve("outside.mjml"));
      Files.deleteIfExists(outsideDir);
    }
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
