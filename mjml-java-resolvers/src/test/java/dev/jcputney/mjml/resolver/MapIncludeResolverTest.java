package dev.jcputney.mjml.resolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.jcputney.mjml.MjmlIncludeException;
import dev.jcputney.mjml.ResolverContext;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MapIncludeResolverTest {

  private static final ResolverContext CTX = ResolverContext.root("mjml");

  @Test
  void resolveExistingKey() {
    var resolver = new MapIncludeResolver(Map.of("header.mjml", "<mj-text>Header</mj-text>"));
    assertEquals("<mj-text>Header</mj-text>", resolver.resolve("header.mjml", CTX));
  }

  @Test
  void resolveMissingKeyThrows() {
    var resolver = new MapIncludeResolver(Map.of("header.mjml", "content"));
    assertThrows(MjmlIncludeException.class, () -> resolver.resolve("missing.mjml", CTX));
  }

  @Test
  void emptyMapAlwaysThrows() {
    var resolver = new MapIncludeResolver(Map.of());
    assertThrows(MjmlIncludeException.class, () -> resolver.resolve("any.mjml", CTX));
  }

  @Test
  void ofFactoryWithPairs() {
    var resolver = MapIncludeResolver.of(
        "a.mjml", "content-a",
        "b.mjml", "content-b"
    );
    assertEquals("content-a", resolver.resolve("a.mjml", CTX));
    assertEquals("content-b", resolver.resolve("b.mjml", CTX));
  }

  @Test
  void ofFactoryOddArgumentsThrows() {
    assertThrows(IllegalArgumentException.class,
        () -> MapIncludeResolver.of("a.mjml"));
  }

  @Test
  void ofFactoryEmptyIsValid() {
    var resolver = MapIncludeResolver.of();
    assertThrows(MjmlIncludeException.class, () -> resolver.resolve("any.mjml", CTX));
  }

  @Test
  void builderPutAndBuild() {
    var resolver = MapIncludeResolver.builder()
        .put("one.mjml", "ONE")
        .put("two.mjml", "TWO")
        .build();
    assertEquals("ONE", resolver.resolve("one.mjml", CTX));
    assertEquals("TWO", resolver.resolve("two.mjml", CTX));
  }

  @Test
  void builderEmptyBuild() {
    var resolver = MapIncludeResolver.builder().build();
    assertThrows(MjmlIncludeException.class, () -> resolver.resolve("any.mjml", CTX));
  }

  @Test
  void defensiveCopyPreventsExternalModification() {
    var map = new java.util.HashMap<String, String>();
    map.put("a.mjml", "content");
    var resolver = new MapIncludeResolver(map);
    map.put("b.mjml", "more");
    // b.mjml should not be resolvable since we made a defensive copy
    assertThrows(MjmlIncludeException.class, () -> resolver.resolve("b.mjml", CTX));
  }

  @Test
  void resolverNotNull() {
    assertNotNull(MapIncludeResolver.builder());
  }
}
