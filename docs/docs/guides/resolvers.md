---
sidebar_position: 6
title: Resolver Types
---

# Resolver Types

mjml-java provides a pluggable `IncludeResolver` system for resolving `<mj-include>` paths. The core module includes basic resolvers, and the `mjml-java-resolvers` module adds advanced implementations.

## Core Resolvers (mjml-java-core)

These resolvers ship with the core module and have zero external dependencies.

### FileSystemIncludeResolver

Resolves include paths relative to a base directory on the file system. Includes path traversal protection.

```java
import dev.jcputney.mjml.FileSystemIncludeResolver;

IncludeResolver resolver = new FileSystemIncludeResolver(Path.of("/templates"));

MjmlConfiguration config = MjmlConfiguration.builder()
    .includeResolver(resolver)
    .build();
```

Given `<mj-include path="partials/header.mjml" />`, the resolver reads `/templates/partials/header.mjml`. Paths that escape the base directory via `../` are rejected.

### ClasspathIncludeResolver

Resolves include paths from the Java classpath (e.g., resources bundled in a JAR).

```java
import dev.jcputney.mjml.ClasspathIncludeResolver;

// Uses the current thread's context class loader
IncludeResolver resolver = new ClasspathIncludeResolver();

// Or specify a class loader explicitly
IncludeResolver resolver = new ClasspathIncludeResolver(MyApp.class.getClassLoader());
```

Includes the same path traversal protections as `FileSystemIncludeResolver`.

## Additional Resolvers (mjml-java-resolvers)

Add the resolvers module to your project:

```xml
<dependency>
    <groupId>dev.jcputney</groupId>
    <artifactId>mjml-java-resolvers</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

All resolvers in this module are in the `dev.jcputney.mjml.resolver` package.

### MapIncludeResolver

An in-memory resolver backed by a `Map<String, String>`. Useful for testing or embedding templates directly in code.

```java
import dev.jcputney.mjml.resolver.MapIncludeResolver;

// From a map
MapIncludeResolver resolver = new MapIncludeResolver(Map.of(
    "header.mjml", "<mj-section><mj-column><mj-text>Header</mj-text></mj-column></mj-section>",
    "footer.mjml", "<mj-section><mj-column><mj-text>Footer</mj-text></mj-column></mj-section>"
));

// From alternating path/content pairs
MapIncludeResolver resolver = MapIncludeResolver.of(
    "header.mjml", "<mj-section>...</mj-section>",
    "footer.mjml", "<mj-section>...</mj-section>"
);

// Using the builder
MapIncludeResolver resolver = MapIncludeResolver.builder()
    .put("header.mjml", "<mj-section>...</mj-section>")
    .put("footer.mjml", "<mj-section>...</mj-section>")
    .build();
```

### CompositeIncludeResolver

Chains multiple resolvers together. The first resolver that succeeds wins; if all fail, the last exception is rethrown.

```java
import dev.jcputney.mjml.resolver.CompositeIncludeResolver;

IncludeResolver resolver = CompositeIncludeResolver.of(
    new ClasspathIncludeResolver(),
    new FileSystemIncludeResolver(Path.of("/templates"))
);
```

Or from a list:

```java
IncludeResolver resolver = new CompositeIncludeResolver(List.of(
    classpathResolver,
    fileSystemResolver,
    httpResolver
));
```

### CachingIncludeResolver

A caching decorator with TTL-based expiration and configurable maximum entries. Thread-safe.

```java
import dev.jcputney.mjml.resolver.CachingIncludeResolver;

IncludeResolver resolver = CachingIncludeResolver.builder()
    .delegate(new FileSystemIncludeResolver(Path.of("/templates")))
    .ttl(Duration.ofMinutes(5))    // Default: 5 minutes
    .maxEntries(256)                // Default: 256
    .build();
```

Cache management methods:

| Method | Description |
|---|---|
| `invalidateAll()` | Removes all entries from the cache |
| `invalidate(String path)` | Removes a single entry |
| `size()` | Returns the current number of cached entries |

When the cache is full, expired entries are evicted first, then the oldest 25% are removed.

### UrlIncludeResolver

Fetches content via HTTP/HTTPS using the JDK `HttpClient`. Includes built-in SSRF protection by blocking requests to private/loopback addresses.

```java
import dev.jcputney.mjml.resolver.UrlIncludeResolver;

IncludeResolver resolver = UrlIncludeResolver.builder()
    .allowedHosts("cdn.example.com", "templates.example.com")
    .httpsOnly(true)                        // Default: true
    .connectTimeout(Duration.ofSeconds(5))   // Default: 5 seconds
    .readTimeout(Duration.ofSeconds(10))     // Default: 10 seconds
    .maxResponseSize(1_048_576)              // Default: 1 MB
    .build();
```

Builder options:

| Method | Default | Description |
|---|---|---|
| `allowedHosts(String...)` | (empty) | If non-empty, only these hosts are permitted |
| `deniedHosts(String...)` | (empty) | These hosts are always blocked |
| `httpsOnly(boolean)` | `true` | Restrict to HTTPS URLs only |
| `connectTimeout(Duration)` | 5 seconds | Connection timeout |
| `readTimeout(Duration)` | 10 seconds | Request/read timeout |
| `maxResponseSize(int)` | 1 MB | Maximum response body size in bytes |
| `httpClient(HttpClient)` | (auto-created) | Custom `HttpClient` (useful for testing) |

:::warning SSRF Protection
`UrlIncludeResolver` automatically blocks requests to loopback, site-local, link-local, and any-local IP addresses. This protects against SSRF attacks where an attacker uses `<mj-include>` to probe internal network resources.
:::

### PrefixRoutingIncludeResolver

Routes include paths to different resolvers based on prefix matching. The prefix is stripped before delegation.

```java
import dev.jcputney.mjml.resolver.PrefixRoutingIncludeResolver;

IncludeResolver resolver = PrefixRoutingIncludeResolver.builder()
    .route("classpath:", new ClasspathIncludeResolver())
    .route("https://", UrlIncludeResolver.builder()
        .allowedHosts("cdn.example.com")
        .build())
    .defaultResolver(new FileSystemIncludeResolver(Path.of("/templates")))
    .build();
```

With this configuration:
- `<mj-include path="classpath:templates/header.mjml" />` resolves via `ClasspathIncludeResolver` with path `templates/header.mjml`
- `<mj-include path="https://cdn.example.com/footer.mjml" />` resolves via `UrlIncludeResolver` with path `cdn.example.com/footer.mjml`
- `<mj-include path="partials/nav.mjml" />` resolves via the default `FileSystemIncludeResolver`

Prefixes are matched in insertion order.

## Custom Resolvers

Implement the `IncludeResolver` functional interface for any custom resolution strategy:

```java
IncludeResolver dbResolver = (path, context) -> {
    // context.includeType() — "mjml", "html", "css", or "css-inline"
    // context.depth() — nesting depth (0 for top-level)
    String content = templateRepository.findByPath(path);
    if (content == null) {
        throw new MjmlIncludeException("Template not found: " + path);
    }
    return content;
};
```

## Combining Resolvers

A common pattern is to combine several resolvers:

```java
// Cache HTTP results, fall back to classpath for local templates
IncludeResolver resolver = CompositeIncludeResolver.of(
    CachingIncludeResolver.builder()
        .delegate(UrlIncludeResolver.builder()
            .allowedHosts("templates.example.com")
            .build())
        .ttl(Duration.ofMinutes(10))
        .build(),
    new ClasspathIncludeResolver()
);
```
