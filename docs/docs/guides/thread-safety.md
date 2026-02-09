---
sidebar_position: 5
title: Thread Safety
---

# Thread Safety

mjml-java is designed for safe concurrent use. The static `MjmlRenderer.render()` methods can be called from multiple threads simultaneously without synchronization.

## Why It's Thread-Safe

Each call to `MjmlRenderer.render()` creates its own isolated state:

```java
public static MjmlRenderResult render(String mjml, MjmlConfiguration configuration) {
    RenderPipeline pipeline = new RenderPipeline(configuration);
    return pipeline.render(mjml);
}
```

Every render call gets:

- A new `RenderPipeline` instance
- A new `GlobalContext` (document-level state: fonts, styles, attributes)
- A new `ComponentRegistry` (component factories)
- New component instances for each MJML element

No mutable state is shared between concurrent render calls.

## Immutable Configuration

`MjmlConfiguration` is immutable. The builder creates a defensive copy of all collections:

```java
// In MjmlConfiguration constructor:
this.customComponents = Map.copyOf(builder.customComponents);
```

This means a single configuration can be shared across all threads without risk:

```java
// Create once, share everywhere
MjmlConfiguration config = MjmlConfiguration.builder()
    .language("en")
    .sanitizeOutput(true)
    .registerComponent("mj-greeting", MjGreeting::new)
    .build();

// Safe to use from any thread
ExecutorService executor = Executors.newFixedThreadPool(8);
for (String template : templates) {
    executor.submit(() -> {
        String html = MjmlRenderer.render(template, config).html();
        // ... use html
    });
}
```

## Concurrent Usage Patterns

### Shared Configuration with Thread Pool

The most common pattern: build a configuration once and reuse it across a thread pool.

```java
public class EmailService {

    private final MjmlConfiguration config;
    private final ExecutorService executor;

    public EmailService() {
        this.config = MjmlConfiguration.builder()
            .language("en")
            .includeResolver(new FileSystemIncludeResolver(
                Path.of("/templates")))
            .build();
        this.executor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors());
    }

    public CompletableFuture<String> renderAsync(String mjml) {
        return CompletableFuture.supplyAsync(
            () -> MjmlRenderer.render(mjml, config).html(),
            executor
        );
    }
}
```

### Different Configurations Per Thread

Each thread can use a different configuration. Since configurations are immutable and render calls are isolated, this works without any coordination:

```java
String[] languages = {"en", "fr", "de", "es"};

for (String lang : languages) {
    executor.submit(() -> {
        MjmlConfiguration config = MjmlConfiguration.builder()
            .language(lang)
            .build();

        String html = MjmlRenderer.render(template, config).html();
        // html will have lang="en", lang="fr", etc.
    });
}
```

### Servlet / Request Handler

In a web application, render templates per-request without synchronization:

```java
@RestController
public class EmailController {

    private final MjmlConfiguration config = MjmlConfiguration.builder()
        .language("en")
        .build();

    @PostMapping("/render")
    public String render(@RequestBody String mjml) {
        // Safe: each request gets its own pipeline
        return MjmlRenderer.render(mjml, config).html();
    }
}
```

### Virtual Threads (Java 21+)

mjml-java works with virtual threads since it has no thread-local state or synchronized blocks in the render path:

```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    List<Future<String>> futures = templates.stream()
        .map(tmpl -> executor.submit(
            () -> MjmlRenderer.render(tmpl, config).html()))
        .toList();

    for (Future<String> future : futures) {
        String html = future.get();
        // ... use html
    }
}
```

## What Is NOT Thread-Safe

### MjmlConfiguration.Builder

The builder itself is not thread-safe. Build the configuration on a single thread, then share the immutable result:

```java
// WRONG: Don't share the builder across threads
MjmlConfiguration.Builder builder = MjmlConfiguration.builder();
// ... don't pass builder to other threads

// RIGHT: Build first, then share the result
MjmlConfiguration config = builder.build();
// config is safe to share
```

### Custom IncludeResolver State

If your custom `IncludeResolver` maintains mutable state (caches, connection pools, etc.), you are responsible for making it thread-safe:

```java
// Thread-safe resolver with a concurrent cache
public class CachingResolver implements IncludeResolver {
    private final ConcurrentHashMap<String, String> cache =
        new ConcurrentHashMap<>();
    private final IncludeResolver delegate;

    public CachingResolver(IncludeResolver delegate) {
        this.delegate = delegate;
    }

    @Override
    public String resolve(String path) {
        return cache.computeIfAbsent(path, delegate::resolve);
    }
}
```

### Custom Component Side Effects

If a custom component's `render()` method modifies shared mutable state, that state must be synchronized externally. Prefer stateless components that derive all values from their attributes and context.

## Performance Considerations

- Each render call creates a new pipeline, component registry, and context. This allocation is intentional and lightweight compared to the actual rendering work.
- `MjmlConfiguration` is created once and reused, so there is no configuration overhead per render.
- The `FileSystemIncludeResolver` reads files via `Files.readString()`, which is safe for concurrent access at the OS level, though disk I/O may become a bottleneck under high concurrency. Consider a caching resolver for production workloads.
