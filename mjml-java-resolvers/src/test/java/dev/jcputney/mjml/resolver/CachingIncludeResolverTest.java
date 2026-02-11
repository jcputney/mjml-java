package dev.jcputney.mjml.resolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlIncludeException;
import dev.jcputney.mjml.ResolverContext;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class CachingIncludeResolverTest {

  private static final ResolverContext CTX = ResolverContext.root("mjml");

  @Test
  void cacheHitReturnsCachedContent() {
    var callCount = new AtomicInteger(0);
    var delegate = new MapIncludeResolver(java.util.Map.of("a.mjml", "content"));
    var countingDelegate =
        (dev.jcputney.mjml.IncludeResolver)
            (path, ctx) -> {
              callCount.incrementAndGet();
              return delegate.resolve(path, ctx);
            };

    var caching =
        CachingIncludeResolver.builder()
            .delegate(countingDelegate)
            .ttl(Duration.ofMinutes(10))
            .build();

    assertEquals("content", caching.resolve("a.mjml", CTX));
    assertEquals("content", caching.resolve("a.mjml", CTX));
    assertEquals(1, callCount.get(), "Delegate should be called only once");
  }

  @Test
  void cacheMissCallsDelegate() {
    var callCount = new AtomicInteger(0);
    var countingDelegate =
        (dev.jcputney.mjml.IncludeResolver)
            (path, ctx) -> {
              callCount.incrementAndGet();
              return "content-" + path;
            };

    var caching = CachingIncludeResolver.builder().delegate(countingDelegate).build();

    assertEquals("content-a.mjml", caching.resolve("a.mjml", CTX));
    assertEquals("content-b.mjml", caching.resolve("b.mjml", CTX));
    assertEquals(2, callCount.get());
  }

  @Test
  void cacheKeyIncludesResolverContext() {
    var callCount = new AtomicInteger(0);
    var countingDelegate =
        (dev.jcputney.mjml.IncludeResolver)
            (path, ctx) -> {
              callCount.incrementAndGet();
              String includer = ctx == null ? "null" : String.valueOf(ctx.includingPath());
              String includeType = ctx == null ? "null" : String.valueOf(ctx.includeType());
              return includer + ":" + includeType;
            };

    var caching =
        CachingIncludeResolver.builder()
            .delegate(countingDelegate)
            .ttl(Duration.ofMinutes(10))
            .build();

    ResolverContext rootMjml = ResolverContext.root("mjml");
    ResolverContext rootHtml = ResolverContext.root("html");
    ResolverContext nestedMjml = rootMjml.nested("parent.mjml");

    assertEquals("null:mjml", caching.resolve("same.mjml", rootMjml));
    assertEquals("null:mjml", caching.resolve("same.mjml", rootMjml));
    assertEquals("null:html", caching.resolve("same.mjml", rootHtml));
    assertEquals("parent.mjml:mjml", caching.resolve("same.mjml", nestedMjml));
    assertEquals(3, callCount.get(), "Different contexts should use distinct cache entries");
  }

  @Test
  void ttlExpirationCausesRefresh() throws InterruptedException {
    var callCount = new AtomicInteger(0);
    var countingDelegate =
        (dev.jcputney.mjml.IncludeResolver)
            (path, ctx) -> {
              return "v" + callCount.incrementAndGet();
            };

    var caching =
        CachingIncludeResolver.builder()
            .delegate(countingDelegate)
            .ttl(Duration.ofMillis(50))
            .build();

    assertEquals("v1", caching.resolve("a.mjml", CTX));
    Thread.sleep(100);
    assertEquals("v2", caching.resolve("a.mjml", CTX));
  }

  @Test
  void invalidateRemovesSingleEntry() {
    var callCount = new AtomicInteger(0);
    var countingDelegate =
        (dev.jcputney.mjml.IncludeResolver)
            (path, ctx) -> {
              return "v" + callCount.incrementAndGet();
            };

    var caching = CachingIncludeResolver.builder().delegate(countingDelegate).build();

    caching.resolve("a.mjml", CTX);
    caching.resolve("b.mjml", CTX);
    assertEquals(2, caching.size());

    caching.invalidate("a.mjml");
    assertEquals(1, caching.size());

    // Should call delegate again for a.mjml
    assertEquals("v3", caching.resolve("a.mjml", CTX));
    assertEquals(3, callCount.get());
  }

  @Test
  void invalidateAllClearsCache() {
    var delegate = MapIncludeResolver.of("a.mjml", "A", "b.mjml", "B");
    var caching = CachingIncludeResolver.builder().delegate(delegate).build();

    caching.resolve("a.mjml", CTX);
    caching.resolve("b.mjml", CTX);
    assertEquals(2, caching.size());

    caching.invalidateAll();
    assertEquals(0, caching.size());
  }

  @Test
  void maxEntriesEvictsOldest() {
    var callCount = new AtomicInteger(0);
    var countingDelegate =
        (dev.jcputney.mjml.IncludeResolver)
            (path, ctx) -> {
              callCount.incrementAndGet();
              return "content-" + path;
            };

    var caching =
        CachingIncludeResolver.builder()
            .delegate(countingDelegate)
            .maxEntries(4)
            .ttl(Duration.ofMinutes(10))
            .build();

    // Fill cache
    for (int i = 0; i < 4; i++) {
      caching.resolve("file" + i + ".mjml", CTX);
    }
    assertEquals(4, caching.size());

    // Adding one more should trigger eviction
    caching.resolve("file4.mjml", CTX);
    // After eviction of 25% (1 entry), size should be 4
    assertEquals(4, caching.size());
  }

  @Test
  void builderRequiresDelegate() {
    assertThrows(IllegalStateException.class, () -> CachingIncludeResolver.builder().build());
  }

  @Test
  void builderRejectsInvalidTtl() {
    var delegate = MapIncludeResolver.of("a.mjml", "A");
    assertTrue(
        assertThrows(
                IllegalStateException.class,
                () ->
                    CachingIncludeResolver.builder().delegate(delegate).ttl(Duration.ZERO).build())
            .getMessage()
            .contains("ttl"));

    assertTrue(
        assertThrows(
                IllegalStateException.class,
                () ->
                    CachingIncludeResolver.builder()
                        .delegate(delegate)
                        .ttl(Duration.ofSeconds(-1))
                        .build())
            .getMessage()
            .contains("ttl"));

    assertTrue(
        assertThrows(
                IllegalStateException.class,
                () -> CachingIncludeResolver.builder().delegate(delegate).ttl(null).build())
            .getMessage()
            .contains("ttl"));
  }

  @Test
  void builderRejectsInvalidMaxEntries() {
    var delegate = MapIncludeResolver.of("a.mjml", "A");
    assertTrue(
        assertThrows(
                IllegalStateException.class,
                () -> CachingIncludeResolver.builder().delegate(delegate).maxEntries(0).build())
            .getMessage()
            .contains("maxEntries"));
    assertTrue(
        assertThrows(
                IllegalStateException.class,
                () -> CachingIncludeResolver.builder().delegate(delegate).maxEntries(-1).build())
            .getMessage()
            .contains("maxEntries"));
  }

  @Test
  void delegateExceptionPropagates() {
    var delegate =
        (dev.jcputney.mjml.IncludeResolver)
            (path, ctx) -> {
              throw new MjmlIncludeException("not found");
            };

    var caching = CachingIncludeResolver.builder().delegate(delegate).build();

    assertThrows(MjmlIncludeException.class, () -> caching.resolve("any.mjml", CTX));
  }

  @Test
  void sizeReflectsCacheState() {
    var delegate = MapIncludeResolver.of("a.mjml", "A");
    var caching = CachingIncludeResolver.builder().delegate(delegate).build();

    assertEquals(0, caching.size());
    caching.resolve("a.mjml", CTX);
    assertEquals(1, caching.size());
  }
}
