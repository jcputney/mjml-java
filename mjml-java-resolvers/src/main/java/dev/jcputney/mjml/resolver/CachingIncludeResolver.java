package dev.jcputney.mjml.resolver;

import dev.jcputney.mjml.IncludeResolver;
import dev.jcputney.mjml.ResolverContext;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A caching decorator for {@link IncludeResolver} with TTL-based expiration
 * and configurable maximum entries. Thread-safe.
 *
 * <p>Cache entries are keyed by include path and context dimensions
 * ({@code includingPath} and {@code includeType}) to avoid incorrect reuse
 * for context-sensitive delegates.</p>
 */
public final class CachingIncludeResolver implements IncludeResolver {

  private final IncludeResolver delegate;
  private final Duration ttl;
  private final int maxEntries;
  private final ConcurrentHashMap<CacheKey, CacheEntry> cache = new ConcurrentHashMap<>();

  private CachingIncludeResolver(IncludeResolver delegate, Duration ttl, int maxEntries) {
    this.delegate = delegate;
    this.ttl = ttl;
    this.maxEntries = maxEntries;
  }

  /**
   * Returns a new builder.
   *
   * @return builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  @Override
  public String resolve(String path, ResolverContext context) {
    CacheKey key = CacheKey.from(path, context);
    CacheEntry entry = cache.get(key);
    if (entry != null && !entry.isExpired()) {
      return entry.content;
    }

    // Cache miss or expired — resolve from delegate
    String content = delegate.resolve(path, context);
    evictIfNeeded();
    cache.put(key, new CacheEntry(content, Instant.now().plus(ttl)));
    return content;
  }

  /**
   * Removes all entries from the cache.
   */
  public void invalidateAll() {
    cache.clear();
  }

  /**
   * Removes all cached entries for the given path across all resolver contexts.
   *
   * @param path the path to invalidate
   */
  public void invalidate(String path) {
    cache.keySet().removeIf(key -> key.path().equals(path));
  }

  /**
   * Returns the number of entries currently in the cache (including expired).
   *
   * @return cache size
   */
  public int size() {
    return cache.size();
  }

  private void evictIfNeeded() {
    if (cache.size() < maxEntries) {
      return;
    }

    // First pass: evict expired entries
    cache.entrySet().removeIf(e -> e.getValue().isExpired());

    if (cache.size() < maxEntries) {
      return;
    }

    // Still over limit: evict oldest 25%
    int toEvict = Math.max(1, cache.size() / 4);
    List<Map.Entry<CacheKey, CacheEntry>> entries = new ArrayList<>(cache.entrySet());
    entries.sort(Comparator.comparing(e -> e.getValue().expiresAt));
    for (int i = 0; i < toEvict && i < entries.size(); i++) {
      cache.remove(entries.get(i).getKey());
    }
  }

  private record CacheKey(String path, String includingPath, String includeType) {
    static CacheKey from(String path, ResolverContext context) {
      if (context == null) {
        return new CacheKey(path, null, null);
      }
      return new CacheKey(path, context.includingPath(), context.includeType());
    }
  }

  private static final class CacheEntry {
    final String content;
    final Instant expiresAt;

    CacheEntry(String content, Instant expiresAt) {
      this.content = content;
      this.expiresAt = expiresAt;
    }

    boolean isExpired() {
      return Instant.now().isAfter(expiresAt);
    }
  }

  /**
   * Builder for {@link CachingIncludeResolver}.
   */
  public static final class Builder {

    private IncludeResolver delegate;
    private Duration ttl = Duration.ofMinutes(5);
    private int maxEntries = 256;

    private Builder() {}

    /**
     * Sets the delegate resolver to cache results from.
     *
     * @param delegate the underlying resolver
     * @return this builder
     */
    public Builder delegate(IncludeResolver delegate) {
      this.delegate = delegate;
      return this;
    }

    /**
     * Sets the time-to-live for cached entries.
     * Must be a positive duration.
     *
     * @param ttl the TTL duration
     * @return this builder
     */
    public Builder ttl(Duration ttl) {
      this.ttl = ttl;
      return this;
    }

    /**
     * Sets the maximum number of cached entries.
     * Must be greater than zero.
     *
     * @param maxEntries the max entry count
     * @return this builder
     */
    public Builder maxEntries(int maxEntries) {
      this.maxEntries = maxEntries;
      return this;
    }

    /**
     * Builds the caching resolver.
     *
     * @return a new {@link CachingIncludeResolver}
     * @throws IllegalStateException if delegate is not set
     */
    public CachingIncludeResolver build() {
      if (delegate == null) {
        throw new IllegalStateException("delegate resolver is required");
      }
      if (ttl == null || ttl.isZero() || ttl.isNegative()) {
        throw new IllegalStateException("ttl must be a positive duration");
      }
      if (maxEntries <= 0) {
        throw new IllegalStateException("maxEntries must be greater than 0");
      }
      return new CachingIncludeResolver(delegate, ttl, maxEntries);
    }
  }
}
