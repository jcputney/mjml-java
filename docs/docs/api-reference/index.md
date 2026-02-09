---
sidebar_position: 1
title: Package Overview
---

# API Reference

mjml-java exports five packages through its Java module `dev.jcputney.mjml`. This section documents the public API surface you interact with directly.

## Module Exports

| Package | Description |
|---------|-------------|
| `dev.jcputney.mjml` | Core API: renderer, configuration, exceptions, include resolver |
| `dev.jcputney.mjml.css` | Standalone CSS inlining engine |
| `dev.jcputney.mjml.component` | Component base classes and factory interface for custom components |
| `dev.jcputney.mjml.context` | Global and render context (advanced/internal use) |
| `dev.jcputney.mjml.parser` | MJML parser and AST node types (advanced/internal use) |

For most use cases, you only need the `dev.jcputney.mjml` package.

## Class Diagram

```mermaid
classDiagram
    class MjmlRenderer {
        +render(String mjml)$ MjmlRenderResult
        +render(String mjml, MjmlConfiguration config)$ MjmlRenderResult
        +render(Path mjmlFile)$ MjmlRenderResult
        +render(Path mjmlFile, MjmlConfiguration config)$ MjmlRenderResult
    }

    class MjmlConfiguration {
        +builder()$ Builder
        +defaults()$ MjmlConfiguration
        +getLanguage() String
        +getDirection() Direction
        +getIncludeResolver() IncludeResolver
        +getCustomComponents() Map
        +isSanitizeOutput() boolean
        +getMaxInputSize() int
        +getMaxNestingDepth() int
        +getMaxIncludeDepth() int
        +getContentSanitizer() ContentSanitizer
    }

    class MjmlRenderResult {
        <<record>>
        +html() String
        +title() String
        +previewText() String
    }

    class IncludeResolver {
        <<interface>>
        +resolve(String path, ResolverContext context) String
    }

    class FileSystemIncludeResolver {
        +FileSystemIncludeResolver(Path baseDir)
        +resolve(String path, ResolverContext context) String
    }

    class CssInliner {
        +inline(String html)$ String
        +inline(String html, String additionalCss)$ String
        +inlineAdditionalOnly(String html, String css)$ String
    }

    MjmlRenderer --> MjmlConfiguration : uses
    MjmlRenderer --> MjmlRenderResult : returns
    MjmlConfiguration --> IncludeResolver : optional
    FileSystemIncludeResolver ..|> IncludeResolver
```

## Exception Hierarchy

All exceptions are unchecked (extend `RuntimeException`), so you are not forced to catch them. Use targeted catch blocks when you need to distinguish between error types.

```mermaid
classDiagram
    class RuntimeException
    class MjmlException {
        +MjmlException(String message)
        +MjmlException(String message, Throwable cause)
    }
    class MjmlParseException {
        Malformed XML, missing root element
    }
    class MjmlValidationException {
        Size limit exceeded, nesting too deep
    }
    class MjmlIncludeException {
        Missing include file, path traversal
    }
    class MjmlRenderException {
        Unexpected error during render phase
    }

    RuntimeException <|-- MjmlException
    MjmlException <|-- MjmlParseException
    MjmlException <|-- MjmlValidationException
    MjmlException <|-- MjmlIncludeException
    MjmlException <|-- MjmlRenderException
```

| Exception | Thrown When |
|-----------|------------|
| `MjmlParseException` | Malformed XML, missing `<mjml>` root element, invalid structure |
| `MjmlValidationException` | Input exceeds `maxInputSize` or nesting exceeds `maxNestingDepth` |
| `MjmlIncludeException` | Include file not found, path traversal attempt, circular includes |
| `MjmlRenderException` | Unexpected error during the render phase |
| `MjmlException` | Base type -- catch this to handle all MJML errors |

```java
try {
    MjmlRenderResult result = MjmlRenderer.render(userInput);
} catch (MjmlParseException e) {
    // Invalid MJML structure
} catch (MjmlValidationException e) {
    // Input too large or too deeply nested
} catch (MjmlIncludeException e) {
    // Include resolution failed
} catch (MjmlRenderException e) {
    // Unexpected error during render phase
} catch (MjmlException e) {
    // Catch-all for any MJML error
}
```
