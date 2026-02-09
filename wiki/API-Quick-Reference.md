# API Quick Reference

Single-page reference for all public API methods in mjml-java.

## MjmlRenderer

The main entry point for rendering MJML to HTML. All methods are static and thread-safe.

| Method | Returns | Description |
|--------|---------|-------------|
| `render(String mjml)` | `MjmlRenderResult` | Renders MJML to HTML using default configuration |
| `render(String mjml, MjmlConfiguration config)` | `MjmlRenderResult` | Renders MJML to HTML with custom configuration |
| `render(Path mjmlFile)` | `MjmlRenderResult` | Renders an MJML file, auto-configuring a `FileSystemIncludeResolver` |
| `render(Path mjmlFile, MjmlConfiguration config)` | `MjmlRenderResult` | Renders an MJML file with custom configuration |

```java
// Simple
MjmlRenderResult result = MjmlRenderer.render(mjmlString);
String html = result.html();

// With configuration
MjmlRenderResult result = MjmlRenderer.render(mjmlString, config);

// From file
MjmlRenderResult result = MjmlRenderer.render(Path.of("/templates/email.mjml"));
```

## MjmlRenderResult

Record returned by `render(String, MjmlConfiguration)`.

| Method | Returns | Description |
|--------|---------|-------------|
| `html()` | `String` | The rendered HTML document |
| `title()` | `String` | Document title from `mj-title` (empty string if not set) |
| `previewText()` | `String` | Preview text from `mj-preview` (empty string if not set) |

## MjmlConfiguration

Immutable configuration object. Create via `builder()` or use `defaults()`.

| Method | Returns | Description |
|--------|---------|-------------|
| `builder()` | `Builder` | Creates a new configuration builder |
| `defaults()` | `MjmlConfiguration` | Returns a configuration with all defaults |
| `getLanguage()` | `String` | HTML `lang` attribute (default: `"und"`) |
| `getDirection()` | `Direction` | Text direction enum: `LTR`, `RTL`, `AUTO` (default: `AUTO`) |
| `getIncludeResolver()` | `IncludeResolver` | Resolver for `mj-include` paths (default: `null`) |
| `getCustomComponents()` | `Map<String, ComponentFactory>` | Registered custom components |
| `isSanitizeOutput()` | `boolean` | Whether to HTML-escape attribute values (default: `true`) |
| `getMaxInputSize()` | `int` | Maximum input size in characters (default: `1_048_576` / ~1 MB) |
| `getContentSanitizer()` | `ContentSanitizer` | Optional content sanitizer for inner HTML (default: `null`) |
| `getMaxNestingDepth()` | `int` | Maximum element nesting depth (default: `100`) |
| `getMaxIncludeDepth()` | `int` | Maximum nested include depth (default: `50`) |

### MjmlConfiguration.Builder

| Method | Returns | Description |
|--------|---------|-------------|
| `language(String)` | `Builder` | Sets the HTML `lang` attribute |
| `direction(String)` | `Builder` | Sets text direction (`"auto"`, `"ltr"`, `"rtl"`) |
| `includeResolver(IncludeResolver)` | `Builder` | Sets the include resolver |
| `registerComponent(String, ComponentFactory)` | `Builder` | Registers a custom component |
| `sanitizeOutput(boolean)` | `Builder` | Enables/disables HTML escaping in attribute values |
| `maxInputSize(int)` | `Builder` | Sets maximum allowed input size |
| `maxNestingDepth(int)` | `Builder` | Sets maximum allowed nesting depth |
| `maxIncludeDepth(int)` | `Builder` | Sets maximum allowed include nesting depth |
| `contentSanitizer(ContentSanitizer)` | `Builder` | Sets optional content sanitizer |
| `build()` | `MjmlConfiguration` | Builds the immutable configuration |

```java
MjmlConfiguration config = MjmlConfiguration.builder()
    .language("en")
    .direction("ltr")
    .includeResolver(new FileSystemIncludeResolver(Path.of("/templates")))
    .sanitizeOutput(true)
    .maxInputSize(2_097_152)  // 2 MB
    .maxNestingDepth(50)
    .build();
```

## CssInliner

Standalone CSS inlining engine. Can be used independently of the MJML renderer.

| Method | Returns | Description |
|--------|---------|-------------|
| `inline(String html)` | `String` | Inlines CSS from `<style>` blocks into element `style` attributes |
| `inline(String html, String additionalCss)` | `String` | Inlines CSS from `<style>` blocks plus additional CSS |
| `inlineAdditionalOnly(String html, String css)` | `String` | Inlines only the provided CSS without extracting `<style>` blocks |

```java
import dev.jcputney.mjml.css.CssInliner;

String inlined = CssInliner.inline(html);
String inlined = CssInliner.inline(html, extraCss);
```

## IncludeResolver

Functional interface for resolving `mj-include` paths to content.

| Method | Returns | Description |
|--------|---------|-------------|
| `resolve(String path, ResolverContext context)` | `String` | Resolves an include path to its content |

## ResolverContext

Record providing include chain metadata to resolvers.

| Method | Returns | Description |
|--------|---------|-------------|
| `includingPath()` | `String` | Path of the file containing the `mj-include`, or `null` for root |
| `includeType()` | `String` | Include type: `"mjml"`, `"html"`, `"css"`, `"css-inline"` |
| `depth()` | `int` | Current nesting depth (0 for top-level) |

## Resolver Module Notes

If you use `mjml-java-resolvers`:

- `UrlIncludeResolver` requires `allowedHosts(...)` for hostname URLs and blocks local/private address ranges for SSRF hardening.
- `CachingIncludeResolver` caches by `path + ResolverContext` dimensions (`includingPath`, `includeType`) so context-sensitive resolvers remain correct.

## FileSystemIncludeResolver

Built-in implementation that resolves paths from the file system.

| Constructor | Description |
|-------------|-------------|
| `FileSystemIncludeResolver(Path baseDir)` | Resolves paths relative to `baseDir` with path traversal protection |

```java
IncludeResolver resolver = new FileSystemIncludeResolver(Path.of("/templates"));
```

## ComponentFactory

Functional interface for creating component instances.

| Method | Returns | Description |
|--------|---------|-------------|
| `create(MjmlNode, GlobalContext, RenderContext)` | `BaseComponent` | Creates a component from a parsed node |

## BaseComponent

Sealed abstract base for all MJML components. Permits `BodyComponent` and `HeadComponent`.

| Method | Returns | Description |
|--------|---------|-------------|
| `getTagName()` | `String` | The MJML tag name (e.g., `"mj-text"`) |
| `getDefaultAttributes()` | `Map<String, String>` | Default attribute values for this component |
| `getAttribute(String name)` | `String` | Resolves attribute via the 5-level cascade |
| `getAttribute(String name, String defaultValue)` | `String` | Resolves attribute with a fallback default |

### Attribute Cascade Order

1. Inline attribute on the element
2. `mj-class` attribute values
3. Tag-specific defaults from `mj-attributes`
4. `mj-all` defaults from `mj-attributes`
5. Component's `getDefaultAttributes()` values

## BodyComponent

Abstract base for components that render to HTML. Extends `BaseComponent`.

| Method | Returns | Description |
|--------|---------|-------------|
| `render()` | `String` | Renders this component to an HTML string |
| `getContentWidth()` | `double` | Content width after subtracting padding and borders |
| `getBoxModel()` | `CssBoxModel` | Box model (padding + border) for this component |

## HeadComponent

Abstract base for components that process metadata. Extends `BaseComponent`.

| Method | Returns | Description |
|--------|---------|-------------|
| `process()` | `void` | Processes this component, updating the global context |

## Exceptions

All exceptions extend `MjmlException` (unchecked).

| Exception | Thrown When |
|-----------|------------|
| `MjmlException` | General rendering errors |
| `MjmlRenderException` | Unexpected error during the render phase |
| `MjmlValidationException` | Input exceeds `maxInputSize` or `maxNestingDepth` |
| `MjmlParseException` | Malformed MJML (invalid XML) |
| `MjmlIncludeException` | Include path cannot be resolved |
