---
sidebar_position: 3
title: Configuration
---

# Configuration

## Overview

`MjmlConfiguration` controls how mjml-java renders MJML templates. It is created through a builder pattern and is immutable once built.

```java
MjmlConfiguration config = MjmlConfiguration.builder()
        .language("en")
        .direction("ltr")
        .sanitizeOutput(true)
        .build();
```

## Configuration Options

| Method | Type | Default | Description |
|---|---|---|---|
| `language(String)` | `String` | `"und"` | Sets the `lang` attribute on the root `<html>` element. Use a BCP 47 language tag (e.g., `"en"`, `"fr"`, `"ja"`). |
| `direction(String)` or `direction(Direction)` | `String` or `Direction` | `Direction.AUTO` | Sets the `dir` attribute on the root `<html>` element. Accepts a string (`"ltr"`, `"rtl"`, `"auto"`) or a `Direction` enum value. |
| `includeResolver(IncludeResolver)` | `IncludeResolver` | `null` | Provides a resolver for `<mj-include>` elements. When `null`, any `<mj-include>` in the template will cause an error. |
| `registerComponent(String, ComponentFactory)` | -- | -- | Registers a custom component under the given tag name. Can be called multiple times to register several components. |
| `sanitizeOutput(boolean)` | `boolean` | `true` | When `true`, HTML special characters (`"`, `<`, `>`, `&`) in attribute values are escaped in the rendered output to prevent XSS. |
| `maxInputSize(int)` | `int` | `1048576` (1 MB) | Maximum allowed MJML input size in characters. Inputs exceeding this limit are rejected before processing. |
| `maxNestingDepth(int)` | `int` | `100` | Maximum allowed element nesting depth. Exceeding this depth during parsing throws an exception. |
| `contentSanitizer(ContentSanitizer)` | `ContentSanitizer` | `null` | Optional sanitizer applied to inner HTML of `mj-text`, `mj-button`, and `mj-raw` elements. See [Security](../guides/security.md#content-sanitization). |

## Default Configuration

For the common case where no customization is needed, use the shorthand:

```java
MjmlConfiguration config = MjmlConfiguration.defaults();
```

This is equivalent to calling `MjmlConfiguration.builder().build()` and uses all the default values listed in the table above.

When using the one-argument `MjmlRenderer.render(String)` method, the default configuration is applied automatically.

## Include Resolution

The `includeResolver` option lets you handle `<mj-include>` elements in your templates. mjml-java ships with `FileSystemIncludeResolver` for loading includes from the file system:

```java
MjmlConfiguration config = MjmlConfiguration.builder()
        .includeResolver(new FileSystemIncludeResolver(Path.of("/templates")))
        .build();
```

You can also implement the `IncludeResolver` interface for custom resolution strategies (e.g., classpath resources, a database, or an HTTP endpoint). The `resolve()` method receives a `ResolverContext` with metadata about the include chain:

```java
public class MyCustomResolver implements IncludeResolver {
    @Override
    public String resolve(String path, ResolverContext context) {
        // context.includeType() — "mjml", "html", "css", or "css-inline"
        // context.depth() — nesting depth (0 for top-level)
        try (var stream = getClass().getResourceAsStream("/templates/" + path)) {
            if (stream == null) {
                throw new MjmlException("Include not found: " + path);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new MjmlException("Failed to read include: " + path, e);
        }
    }
}
```

:::caution
Implementations that resolve paths over HTTP or other network protocols may be vulnerable to Server-Side Request Forgery (SSRF) attacks. An attacker who controls MJML input could use `<mj-include>` to probe internal network resources. HTTP-based resolvers should validate and restrict allowed hosts and URLs.
:::

## Custom Components

Register custom MJML components to extend the renderer with your own tags:

```java
MjmlConfiguration config = MjmlConfiguration.builder()
        .registerComponent("mj-custom", (node, globalContext, renderContext) ->
                new MyCustomComponent(node, globalContext, renderContext))
        .build();
```

The `ComponentFactory` functional interface receives the parsed `MjmlNode`, the `GlobalContext`, and the `RenderContext`, and must return a `BaseComponent` instance.

## Security Options

### Output Sanitization

The `sanitizeOutput` option (default: `true`) escapes HTML special characters in attribute values in the rendered output. This prevents XSS when attribute values contain untrusted content. Disabling this is not recommended unless you have full control over the MJML input.

### Input Size Limits

The `maxInputSize` option (default: 1 MB) rejects MJML inputs that exceed the specified size before any processing begins. This protects against denial-of-service from excessively large inputs.

### Nesting Depth Limits

The `maxNestingDepth` option (default: 100) limits how deeply elements can be nested. This prevents stack overflow errors from deeply nested or recursive templates.

## Immutability and Thread Safety

`MjmlConfiguration` instances are immutable after construction. The builder's `build()` method creates a defensive copy of all mutable data (such as the custom components map). This means:

- A single `MjmlConfiguration` instance can be safely shared across multiple threads.
- You can store a configuration as a static final field and reuse it for every render call.
- Modifying the builder after calling `build()` does not affect previously built configurations.

```java
// Safe to share across threads
private static final MjmlConfiguration CONFIG = MjmlConfiguration.builder()
        .language("en")
        .sanitizeOutput(true)
        .build();

public String renderEmail(String mjml) {
    return MjmlRenderer.render(mjml, CONFIG).html();
}
```
