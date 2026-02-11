# mjml-java

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![Java 17+](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://openjdk.org/)
[![Build](https://img.shields.io/github/actions/workflow/status/jcputney/mjml-java/ci.yml?branch=main)](https://github.com/jcputney/mjml-java/actions)

**Pure Java MJML-to-HTML renderer -- zero dependencies, all 31 MJML components, JPMS-ready.**

mjml-java converts [MJML](https://mjml.io/) email markup into responsive HTML entirely
in Java. No Node.js runtime, no native binaries, no external libraries -- just add the
JAR and call `MjmlRenderer.render()`.

## Features

- **Zero runtime dependencies** -- only the JDK standard library
- **All 31 MJML components** -- full parity with MJML 4
- **Thread-safe static API** -- safe for concurrent use in web servers
- **JPMS module** -- `dev.jcputney.mjml`
- **Standalone CSS inliner** -- `CssInliner.inline()` works on any HTML
- **`mj-include` support** -- with pluggable `IncludeResolver`
- **Custom components** -- register your own tags via `ComponentFactory`
- **Security defaults** -- output sanitization, input size limits, nesting depth limits

## Modules

| Module | Artifact ID | Description |
|---|---|---|
| **Core** | `mjml-java-core` | MJML renderer, all 31 components, CSS inliner. Zero external dependencies. |
| **Resolvers** | `mjml-java-resolvers` | Additional `IncludeResolver` implementations: URL, caching, composite, map, prefix-routing. `UrlIncludeResolver` includes SSRF protections and explicit hostname allowlisting. Zero external dependencies (JDK `java.net.http`). |
| **Spring** | `mjml-java-spring` | Spring Boot auto-configuration, `MjmlService`, Thymeleaf integration. |
| **BOM** | `mjml-java-bom` | Bill of Materials for consistent version management across modules. |

Most users only need `mjml-java-core`. Add `mjml-java-resolvers` if you need URL-based or caching resolvers, and `mjml-java-spring` for Spring Boot integration.

## Quick Start

### Maven

```xml
<dependency>
    <groupId>dev.jcputney</groupId>
    <artifactId>mjml-java-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Gradle

```groovy
implementation 'dev.jcputney:mjml-java-core:1.0.0-SNAPSHOT'
```

### Usage

```java
import dev.jcputney.mjml.MjmlRenderer;
import dev.jcputney.mjml.MjmlRenderResult;

// One-liner with defaults
MjmlRenderResult result = MjmlRenderer.render(mjmlString);
String html = result.html();
```

For more control, use `MjmlConfiguration`:

```java
import dev.jcputney.mjml.MjmlConfiguration;
import dev.jcputney.mjml.MjmlRenderResult;
import dev.jcputney.mjml.MjmlRenderer;
import dev.jcputney.mjml.FileSystemIncludeResolver;

MjmlConfiguration config = MjmlConfiguration.builder()
    .language("en")
    .direction("ltr")
    .includeResolver(new FileSystemIncludeResolver(Path.of("/templates")))
    .sanitizeOutput(true)
    .build();

MjmlRenderResult result = MjmlRenderer.render(mjmlString, config);
String html = result.html();
String title = result.title();
String preview = result.previewText();
```

### Instance API (Recommended for Production)

For repeated rendering, the instance API reuses the internal pipeline and component registry cache:

```java
MjmlRenderer renderer = MjmlRenderer.create(config);
MjmlRenderResult result = renderer.renderTemplate(mjmlString);
String html = result.html();
```

### Rendering a File

```java
import dev.jcputney.mjml.MjmlRenderer;
import java.nio.file.Path;

// Renders a file, automatically setting up a FileSystemIncludeResolver
MjmlRenderResult result = MjmlRenderer.render(Path.of("/templates/email.mjml"));
```

### Standalone CSS Inliner

```java
import dev.jcputney.mjml.css.CssInliner;

// Inline <style> blocks into element style attributes
String inlined = CssInliner.inline(html);

// Inline additional CSS alongside <style> blocks
String inlined = CssInliner.inline(html, additionalCss);
```

## Supported Components

| Category | Components |
|---|---|
| **Head** | `mj-head` `mj-title` `mj-preview` `mj-font` `mj-breakpoint` `mj-style` `mj-attributes` `mj-html-attributes` |
| **Body&nbsp;Layout** | `mj-body` `mj-section` `mj-column` `mj-group` `mj-wrapper` |
| **Content** | `mj-text` `mj-image` `mj-button` `mj-divider` `mj-spacer` `mj-table` `mj-raw` |
| **Interactive** | `mj-hero` `mj-accordion` `mj-accordion-element` `mj-accordion-title` `mj-accordion-text` `mj-carousel` `mj-carousel-image` `mj-navbar` `mj-navbar-link` `mj-social` `mj-social-element` |

See the [MJML documentation](https://documentation.mjml.io/) for component details and attributes.

## Documentation

Full documentation is available at **[jcputney.github.io/mjml-java](https://jcputney.github.io/mjml-java/)**.

## Requirements

- Java 17 or later
- No external dependencies

## License

[MIT](LICENSE)
