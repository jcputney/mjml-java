---
sidebar_position: 2
title: MjmlRenderer
---

# MjmlRenderer

`dev.jcputney.mjml.MjmlRenderer` is the main entry point for converting MJML templates to HTML. It is a `final` class with a private constructor -- all methods are static.

## Methods

### render(String mjml)

Renders an MJML template to HTML using default configuration.

```java
public static String render(String mjml)
```

**Parameters:**
- `mjml` -- the MJML source string

**Returns:** the rendered HTML string

**Throws:** `MjmlException` if parsing or rendering fails

**Example:**

```java
String mjml = """
    <mjml>
      <mj-body>
        <mj-section>
          <mj-column>
            <mj-text>Hello, world!</mj-text>
          </mj-column>
        </mj-section>
      </mj-body>
    </mjml>
    """;

String html = MjmlRenderer.render(mjml);
```

### render(String mjml, MjmlConfiguration config)

Renders an MJML template to HTML with the given configuration, returning a result object with metadata.

```java
public static MjmlRenderResult render(String mjml, MjmlConfiguration configuration)
```

**Parameters:**
- `mjml` -- the MJML source string
- `configuration` -- the rendering configuration (see [MjmlConfiguration](./mjml-configuration.md))

**Returns:** an `MjmlRenderResult` record

**Throws:** `MjmlException` if parsing or rendering fails

**Example:**

```java
MjmlConfiguration config = MjmlConfiguration.builder()
    .language("en")
    .direction("ltr")
    .build();

MjmlRenderResult result = MjmlRenderer.render(mjml, config);

String html = result.html();           // The rendered HTML
String title = result.title();         // From <mj-title>, or ""
String preview = result.previewText(); // From <mj-preview>, or ""
```

## MjmlRenderResult

A Java `record` returned by the two-argument `render()` method.

```java
public record MjmlRenderResult(String html, String title, String previewText) {}
```

| Accessor | Type | Description |
|----------|------|-------------|
| `html()` | `String` | The fully rendered HTML document |
| `title()` | `String` | Document title from `<mj-title>`, or empty string if not set |
| `previewText()` | `String` | Email preview text from `<mj-preview>`, or empty string if not set |

## Thread Safety

The static `render()` methods are thread-safe. Each call creates its own `RenderPipeline` and `GlobalContext`, so concurrent calls do not share mutable state. The provided `MjmlConfiguration` is immutable and safe to share across threads.

```java
// Safe: shared config, concurrent rendering
MjmlConfiguration config = MjmlConfiguration.builder()
    .language("en")
    .build();

ExecutorService pool = Executors.newFixedThreadPool(4);
List<Future<String>> futures = templates.stream()
    .map(tmpl -> pool.submit(() -> MjmlRenderer.render(tmpl, config).html()))
    .toList();
```

## Error Handling

All exceptions extend `MjmlException`, which is an unchecked `RuntimeException`. See the [Package Overview](./index.md#exception-hierarchy) for the full hierarchy.

```java
try {
    String html = MjmlRenderer.render(mjml, config);
} catch (MjmlValidationException e) {
    // Input rejected before parsing (too large, too deeply nested)
    log.warn("Validation failed: {}", e.getMessage());
} catch (MjmlParseException e) {
    // Malformed MJML (bad XML, missing root element)
    log.warn("Parse error: {}", e.getMessage());
} catch (MjmlIncludeException e) {
    // Include resolution failed
    log.warn("Include error: {}", e.getMessage());
} catch (MjmlException e) {
    // Catch-all
    log.error("Render failed: {}", e.getMessage(), e);
}
```
