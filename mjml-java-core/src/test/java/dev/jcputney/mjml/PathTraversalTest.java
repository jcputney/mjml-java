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
 * Comprehensive tests for path traversal prevention in FileSystemIncludeResolver.
 * Verifies that the resolver blocks all known path traversal techniques.
 */
class PathTraversalTest {

  private static final ResolverContext TEST_CONTEXT = ResolverContext.root("mjml");

  @TempDir
  Path tempDir;

  // --- Basic ../ traversal ---

  @Test
  void blocksSimpleDotDotSlash() {
    FileSystemIncludeResolver resolver = new FileSystemIncludeResolver(tempDir);

    assertThrows(MjmlException.class,
        () -> resolver.resolve("../outside.mjml", TEST_CONTEXT),
        "Should block ../ traversal");
  }

  @Test
  void blocksMultipleDotDotSlash() {
    FileSystemIncludeResolver resolver = new FileSystemIncludeResolver(tempDir);

    assertThrows(MjmlException.class,
        () -> resolver.resolve("../../../etc/passwd", TEST_CONTEXT),
        "Should block multiple ../ traversal");
  }

  @Test
  void blocksTraversalFromSubdirectory() throws IOException {
    Files.createDirectory(tempDir.resolve("subdir"));
    FileSystemIncludeResolver resolver = new FileSystemIncludeResolver(tempDir);

    assertThrows(MjmlException.class,
        () -> resolver.resolve("subdir/../../outside.mjml", TEST_CONTEXT),
        "Should block subdir/../../ traversal");
  }

  @Test
  void blocksDeepSubdirTraversal() throws IOException {
    Path deep = tempDir.resolve("a/b/c");
    Files.createDirectories(deep);
    FileSystemIncludeResolver resolver = new FileSystemIncludeResolver(tempDir);

    assertThrows(MjmlException.class,
        () -> resolver.resolve("a/b/c/../../../../outside.mjml", TEST_CONTEXT),
        "Should block deep subdirectory traversal");
  }

  // --- Windows-style backslash traversal ---

  @Test
  void blocksBackslashTraversal() {
    FileSystemIncludeResolver resolver = new FileSystemIncludeResolver(tempDir);

    // On Unix, backslashes are valid filename characters but Path.resolve
    // may normalize them. The key is that the resolved path must stay within baseDir.
    assertThrows(MjmlException.class,
        () -> resolver.resolve("..\\outside.mjml", TEST_CONTEXT),
        "Should block ..\\ traversal");
  }

  @Test
  void blocksMultipleBackslashTraversal() {
    FileSystemIncludeResolver resolver = new FileSystemIncludeResolver(tempDir);

    assertThrows(MjmlException.class,
        () -> resolver.resolve("..\\..\\..\\etc\\passwd", TEST_CONTEXT),
        "Should block multiple ..\\ traversal");
  }

  // --- Mixed slash styles ---

  @Test
  void blocksMixedSlashTraversal() {
    FileSystemIncludeResolver resolver = new FileSystemIncludeResolver(tempDir);

    assertThrows(MjmlException.class,
        () -> resolver.resolve("subdir\\..\\..\\outside.mjml", TEST_CONTEXT),
        "Should block mixed slash traversal");
  }

  // --- Dot-dot without slash ---

  @Test
  void dotDotAloneBlocked() {
    FileSystemIncludeResolver resolver = new FileSystemIncludeResolver(tempDir);

    assertThrows(MjmlException.class,
        () -> resolver.resolve("..", TEST_CONTEXT),
        "Should block bare '..'");
  }

  // --- Valid paths still work ---

  @Test
  void allowsFileInBaseDir() throws IOException {
    Files.writeString(tempDir.resolve("valid.mjml"), "<mj-text>Valid</mj-text>");
    FileSystemIncludeResolver resolver = new FileSystemIncludeResolver(tempDir);

    String content = resolver.resolve("valid.mjml", TEST_CONTEXT);
    assertNotNull(content);
    assertTrue(content.contains("Valid"));
  }

  @Test
  void allowsFileInSubdirectory() throws IOException {
    Path subDir = Files.createDirectory(tempDir.resolve("partials"));
    Files.writeString(subDir.resolve("header.mjml"), "<mj-text>Header</mj-text>");
    FileSystemIncludeResolver resolver = new FileSystemIncludeResolver(tempDir);

    String content = resolver.resolve("partials/header.mjml", TEST_CONTEXT);
    assertNotNull(content);
    assertTrue(content.contains("Header"));
  }

  @Test
  void allowsDotSlashPrefix() throws IOException {
    Files.writeString(tempDir.resolve("test.mjml"), "<mj-text>Test</mj-text>");
    FileSystemIncludeResolver resolver = new FileSystemIncludeResolver(tempDir);

    String content = resolver.resolve("./test.mjml", TEST_CONTEXT);
    assertNotNull(content);
    assertTrue(content.contains("Test"));
  }

  @Test
  void allowsTraversalWithinBaseDir() throws IOException {
    Path subA = Files.createDirectory(tempDir.resolve("a"));
    Path subB = Files.createDirectory(tempDir.resolve("b"));
    Files.writeString(subB.resolve("file.mjml"), "<mj-text>In B</mj-text>");
    FileSystemIncludeResolver resolver = new FileSystemIncludeResolver(tempDir);

    // a/../b/file.mjml normalizes to b/file.mjml which is still within tempDir
    String content = resolver.resolve("a/../b/file.mjml", TEST_CONTEXT);
    assertNotNull(content);
    assertTrue(content.contains("In B"),
        "Traversal within base dir should be allowed");
  }

  // --- Edge cases ---

  @Test
  void rejectsNullPath() {
    FileSystemIncludeResolver resolver = new FileSystemIncludeResolver(tempDir);

    assertThrows(MjmlException.class,
        () -> resolver.resolve(null, TEST_CONTEXT),
        "Should reject null path");
  }

  @Test
  void rejectsEmptyPath() {
    FileSystemIncludeResolver resolver = new FileSystemIncludeResolver(tempDir);

    assertThrows(MjmlException.class,
        () -> resolver.resolve("", TEST_CONTEXT),
        "Should reject empty path");
  }

  @Test
  void rejectsWhitespacePath() {
    FileSystemIncludeResolver resolver = new FileSystemIncludeResolver(tempDir);

    assertThrows(MjmlException.class,
        () -> resolver.resolve("   ", TEST_CONTEXT),
        "Should reject whitespace-only path");
  }

  @Test
  void rejectsNonExistentFile() {
    FileSystemIncludeResolver resolver = new FileSystemIncludeResolver(tempDir);

    assertThrows(MjmlException.class,
        () -> resolver.resolve("nonexistent.mjml", TEST_CONTEXT),
        "Should reject non-existent file");
  }

  // --- Integration with renderer ---

  @Test
  void rendererBlocksTraversalViaInclude() throws IOException {
    Files.writeString(tempDir.resolve("safe.mjml"), """
        <mj-section>
          <mj-column>
            <mj-text>Safe</mj-text>
          </mj-column>
        </mj-section>
        """);

    MjmlConfiguration config = MjmlConfiguration.builder()
        .includeResolver(new FileSystemIncludeResolver(tempDir))
        .build();

    String mjml = """
        <mjml>
          <mj-body>
            <mj-include path="../../../etc/passwd" />
          </mj-body>
        </mjml>
        """;

    assertThrows(MjmlException.class, () -> MjmlRenderer.render(mjml, config),
        "Renderer should propagate path traversal exception");
  }
}
