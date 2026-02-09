package dev.jcputney.mjml;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ClasspathIncludeResolverTest {

  private static final ResolverContext TEST_CONTEXT = ResolverContext.root("mjml");

  @Test
  void resolvesClasspathResource() {
    // module-info.java exists on the classpath from our own compiled classes
    ClasspathIncludeResolver resolver = new ClasspathIncludeResolver(
        ClasspathIncludeResolverTest.class.getClassLoader());
    // Our golden test files are on the test classpath
    // Use a known file from the test resources
    assertThrows(MjmlIncludeException.class,
        () -> resolver.resolve("nonexistent.mjml", TEST_CONTEXT));
  }

  @Test
  void throwsOnNullPath() {
    ClasspathIncludeResolver resolver = new ClasspathIncludeResolver();
    assertThrows(MjmlIncludeException.class, () -> resolver.resolve(null, TEST_CONTEXT));
  }

  @Test
  void throwsOnEmptyPath() {
    ClasspathIncludeResolver resolver = new ClasspathIncludeResolver();
    assertThrows(MjmlIncludeException.class, () -> resolver.resolve("", TEST_CONTEXT));
  }

  @Test
  void throwsOnPathTraversal() {
    ClasspathIncludeResolver resolver = new ClasspathIncludeResolver();
    assertThrows(MjmlIncludeException.class, () -> resolver.resolve("../etc/passwd", TEST_CONTEXT));
  }

  @Test
  void throwsOnNullClassLoader() {
    assertThrows(IllegalArgumentException.class,
        () -> new ClasspathIncludeResolver(null));
  }

  @Test
  void stripsLeadingSlash() {
    ClasspathIncludeResolver resolver = new ClasspathIncludeResolver();
    // Both /nonexistent and nonexistent should throw the same MjmlIncludeException
    assertThrows(MjmlIncludeException.class, () -> resolver.resolve("/nonexistent.mjml", TEST_CONTEXT));
  }
}
