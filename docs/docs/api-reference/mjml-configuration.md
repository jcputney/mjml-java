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
| `direction(String)` | `String` | `"auto"` | Text direction: `"auto"`, `"ltr"`, or `"rtl"`. Accepts a `Direction` enum or string. |
| `includeResolver(IncludeResolver)` | `IncludeResolver` | `null` | Resolver for `<mj-include>` paths (see [IncludeResolver](./include-resolver.md)) |
| `registerComponent(String, ComponentFactory)` | `String`, `ComponentFactory` | (none) | Register a custom component tag name and its factory |
| `sanitizeOutput(boolean)` | `boolean` | `true` | Escape HTML special characters in attribute values to prevent XSS |
| `maxInputSize(int)` | `int` | `1_048_576` (~1 MB for ASCII) | Maximum allowed input size in characters; inputs exceeding this are rejected before parsing |
| `maxNestingDepth(int)` | `int` | `100` | Maximum allowed nesting depth for MJML elements |
| `contentSanitizer(ContentSanitizer)` | `ContentSanitizer` | `null` | Optional sanitizer applied to inner HTML of `mj-text`, `mj-button`, and `mj-raw` elements |

## Validation

The `build()` method validates the configuration:

- `maxInputSize` must be positive (throws `IllegalArgumentException`)
- `maxNestingDepth` must be positive (throws `IllegalArgumentException`)

```java
// Throws IllegalArgumentException: maxInputSize must be positive, got: -1
MjmlConfiguration.builder().maxInputSize(-1).build();
```

## Copying with `toBuilder()`

Use `toBuilder()` to create a new builder pre-populated with an existing configuration's values. This is useful for creating variations of a configuration:

```java
MjmlConfiguration base = MjmlConfiguration.builder()
    .language("en")
    .sanitizeOutput(true)
    .build();

// Create a variant with a different max input size
MjmlConfiguration variant = base.toBuilder()
    .maxInputSize(2_097_152)
    .build();
```

## Immutability

After `build()` returns, the configuration cannot be changed. The custom components map is copied via `Map.copyOf()`, so modifications to the original builder map do not affect the built configuration.

## Getters

| Method | Returns |
|--------|---------|
| `getLanguage()` | The configured language string |
| `getDirection()` | The configured text direction as a `Direction` enum |
| `getContentSanitizer()` | The optional content sanitizer, or `null` |
| `getIncludeResolver()` | The include resolver, or `null` |
| `getCustomComponents()` | Unmodifiable map of tag name to `ComponentFactory` |
| `isSanitizeOutput()` | Whether output sanitization is enabled |
| `getMaxInputSize()` | Maximum input size in characters (not bytes) |
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

## Direction Enum

The `direction()` builder method accepts either a `Direction` enum value or a string. The `Direction` enum has three values:

```java
public enum Direction {
    LTR("ltr"),
    RTL("rtl"),
    AUTO("auto");
}
```

Usage:

```java
import dev.jcputney.mjml.Direction;

// Using the enum directly
MjmlConfiguration config = MjmlConfiguration.builder()
    .direction(Direction.RTL)
    .build();

// Using a string (case-insensitive)
MjmlConfiguration config = MjmlConfiguration.builder()
    .direction("rtl")
    .build();
```

The `Direction` enum also provides:
- `value()` -- returns the lowercase string representation (e.g., `"rtl"`)
- `Direction.of(String)` -- parses a string to a `Direction` (case-insensitive)

## ContentSanitizer

The optional `contentSanitizer` is a `@FunctionalInterface` applied to the inner HTML content of `<mj-text>`, `<mj-button>`, and `<mj-raw>` elements before rendering. Use it to scrub user-supplied HTML.

```java
@FunctionalInterface
public interface ContentSanitizer {
    String sanitize(String html);
}
```

:::warning
By default, MJML passes through inner HTML content as-is (matching the official MJML behavior). If you render user-supplied content, configure a `ContentSanitizer` to prevent XSS.
:::

```java
// Example with Jsoup (add Jsoup as a dependency)
MjmlConfiguration config = MjmlConfiguration.builder()
    .contentSanitizer(html -> Jsoup.clean(html, Safelist.basic()))
    .build();
```

## Full Example

```java
MjmlConfiguration config = MjmlConfiguration.builder()
    .language("en")
    .direction(Direction.LTR)
    .includeResolver(new FileSystemIncludeResolver(Path.of("/templates")))
    .sanitizeOutput(true)
    .maxInputSize(2_097_152) // 2 MB
    .maxNestingDepth(50)
    .contentSanitizer(html -> Jsoup.clean(html, Safelist.basic()))
    .build();

// Use across multiple render calls (thread-safe)
String html1 = MjmlRenderer.render(template1, config).html();
String html2 = MjmlRenderer.render(template2, config).html();
```
