---
sidebar_position: 3
title: MjmlConfiguration
---

# MjmlConfiguration

`dev.jcputney.mjml.MjmlConfiguration` controls how MJML templates are rendered. It is immutable once built and safe to share across threads.

## Creating a Configuration

Use the builder pattern:

```java
MjmlConfiguration config = MjmlConfiguration.builder()
    .language("en")
    .direction("ltr")
    .sanitizeOutput(true)
    .maxInputSize(512_000)
    .build();
```

Or use the defaults:

```java
MjmlConfiguration config = MjmlConfiguration.defaults();
```

## Builder Methods

| Method | Type | Default | Description |
|--------|------|---------|-------------|
| `language(String)` | `String` | `"und"` | HTML `lang` attribute value |
| `direction(String)` | `String` | `"auto"` | Text direction: `"auto"`, `"ltr"`, or `"rtl"` |
| `includeResolver(IncludeResolver)` | `IncludeResolver` | `null` | Resolver for `<mj-include>` paths (see [IncludeResolver](./include-resolver.md)) |
| `registerComponent(String, ComponentFactory)` | `String`, `ComponentFactory` | (none) | Register a custom component tag name and its factory |
| `sanitizeOutput(boolean)` | `boolean` | `true` | Escape HTML special characters in attribute values to prevent XSS |
| `maxInputSize(int)` | `int` | `1_048_576` (1 MB) | Maximum allowed input size in bytes; inputs exceeding this are rejected before parsing |
| `maxNestingDepth(int)` | `int` | `100` | Maximum allowed nesting depth for MJML elements |

## Validation

The `build()` method validates the configuration:

- `maxInputSize` must be positive (throws `IllegalArgumentException`)
- `maxNestingDepth` must be positive (throws `IllegalArgumentException`)

```java
// Throws IllegalArgumentException: maxInputSize must be positive, got: -1
MjmlConfiguration.builder().maxInputSize(-1).build();
```

## Immutability

After `build()` returns, the configuration cannot be changed. The custom components map is copied via `Map.copyOf()`, so modifications to the original builder map do not affect the built configuration.

## Getters

| Method | Returns |
|--------|---------|
| `getLanguage()` | The configured language string |
| `getDirection()` | The configured text direction |
| `getIncludeResolver()` | The include resolver, or `null` |
| `getCustomComponents()` | Unmodifiable map of tag name to `ComponentFactory` |
| `isSanitizeOutput()` | Whether output sanitization is enabled |
| `getMaxInputSize()` | Maximum input size in bytes |
| `getMaxNestingDepth()` | Maximum nesting depth |

## Custom Components

Register custom MJML tags using `registerComponent()`:

```java
MjmlConfiguration config = MjmlConfiguration.builder()
    .registerComponent("mj-custom", (node, globalCtx, renderCtx) -> {
        return new MyCustomComponent(node, globalCtx, renderCtx);
    })
    .build();
```

The `ComponentFactory` interface has a single method:

```java
public interface ComponentFactory {
    BaseComponent create(MjmlNode node, GlobalContext globalContext, RenderContext renderContext);
}
```

Your custom component must extend `BaseComponent` (or one of its subclasses like `BodyComponent`).

## Full Example

```java
MjmlConfiguration config = MjmlConfiguration.builder()
    .language("en")
    .direction("ltr")
    .includeResolver(new FileSystemIncludeResolver(Path.of("/templates")))
    .sanitizeOutput(true)
    .maxInputSize(2_097_152) // 2 MB
    .maxNestingDepth(50)
    .build();

// Use across multiple render calls (thread-safe)
String html1 = MjmlRenderer.render(template1, config).html();
String html2 = MjmlRenderer.render(template2, config).html();
```
