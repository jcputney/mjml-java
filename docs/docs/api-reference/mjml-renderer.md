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
public static MjmlRenderResult render(String mjml)
```

**Parameters:**
- `mjml` -- the MJML source string

**Returns:** an `MjmlRenderResult` record containing the rendered HTML and metadata

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

MjmlRenderResult result = MjmlRenderer.render(mjml);
String html = result.html();
```

### render(String mjml, MjmlConfiguration config)

Renders an MJML template to HTML with the given configuration.

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

### render(Path mjmlFile)

Renders an MJML file to HTML using default configuration. Automatically configures a `FileSystemIncludeResolver` using the file's parent directory.

```java
public static MjmlRenderResult render(Path mjmlFile)
```

**Parameters:**
- `mjmlFile` -- path to the MJML file

**Returns:** an `MjmlRenderResult` record

**Throws:** `MjmlException` if reading, parsing, or rendering fails

**Example:**

```java
MjmlRenderResult result = MjmlRenderer.render(Path.of("/templates/email.mjml"));
String html = result.html();
```

### render(Path mjmlFile, MjmlConfiguration config)

Renders an MJML file to HTML with the given configuration. If no `IncludeResolver` is configured, one is automatically created using the file's parent directory.

```java
public static MjmlRenderResult render(Path mjmlFile, MjmlConfiguration configuration)
```

**Parameters:**
- `mjmlFile` -- path to the MJML file
- `configuration` -- the rendering configuration

**Returns:** an `MjmlRenderResult` record

**Throws:** `MjmlException` if reading, parsing, or rendering fails

## Instance API

For repeated rendering with the same configuration, the instance API reuses the internal `RenderPipeline` and `ComponentRegistry`, avoiding the setup cost on each call. This is the preferred approach for applications that render many templates.

### create()

Creates a reusable renderer instance with default configuration.

```java
public static MjmlRenderer create()
```

**Returns:** a reusable `MjmlRenderer` instance

### create(MjmlConfiguration config)

Creates a reusable renderer instance with the given configuration.

```java
public static MjmlRenderer create(MjmlConfiguration configuration)
```

**Parameters:**
- `configuration` -- the rendering configuration

**Returns:** a reusable `MjmlRenderer` instance

### renderTemplate(String mjml)

Renders an MJML template to HTML using this instance's configuration.

```java
public MjmlRenderResult renderTemplate(String mjml)
```

**Parameters:**
- `mjml` -- the MJML source string

**Returns:** an `MjmlRenderResult` record

**Throws:** `MjmlException` if parsing or rendering fails

### renderTemplate(Path mjmlFile)

Renders an MJML file to HTML using this instance's configuration. If no include resolver is configured, one is automatically created using the file's parent directory.

```java
public MjmlRenderResult renderTemplate(Path mjmlFile)
```

**Parameters:**
- `mjmlFile` -- path to the MJML file

**Returns:** an `MjmlRenderResult` record

**Throws:** `MjmlException` if reading, parsing, or rendering fails

### renderTemplate(String mjml, IncludeResolver resolver)

Renders an MJML template with a specific include resolver, overriding the instance's configured resolver for this call only.

```java
public MjmlRenderResult renderTemplate(String mjml, IncludeResolver resolver)
```

**Parameters:**
- `mjml` -- the MJML source string
- `resolver` -- the include resolver to use for this render

**Returns:** an `MjmlRenderResult` record

**Throws:** `MjmlException` if parsing or rendering fails

### Example

```java
// Create once, reuse for many renders
MjmlConfiguration config = MjmlConfiguration.builder()
    .language("en")
    .direction("ltr")
    .build();

MjmlRenderer renderer = MjmlRenderer.create(config);

// Each call reuses the cached ComponentRegistry and RenderPipeline
MjmlRenderResult welcome = renderer.renderTemplate(welcomeMjml);
MjmlRenderResult receipt = renderer.renderTemplate(receiptMjml);
MjmlRenderResult newsletter = renderer.renderTemplate(newsletterMjml);
```

## MjmlRenderResult

A Java `record` returned by all `render()` overloads.

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
    MjmlRenderResult result = MjmlRenderer.render(mjml, config);
} catch (MjmlValidationException e) {
    // Input rejected before parsing (too large, too deeply nested)
    log.warn("Validation failed: {}", e.getMessage());
} catch (MjmlParseException e) {
    // Malformed MJML (bad XML, missing root element)
    log.warn("Parse error: {}", e.getMessage());
} catch (MjmlIncludeException e) {
    // Include resolution failed
    log.warn("Include error: {}", e.getMessage());
} catch (MjmlRenderException e) {
    // Unexpected error during the render phase
    log.error("Render phase error: {}", e.getMessage(), e);
} catch (MjmlException e) {
    // Catch-all
    log.error("Render failed: {}", e.getMessage(), e);
}
```
