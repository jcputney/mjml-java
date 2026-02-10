# Changelog

All notable changes to mjml-java are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [1.0.0] - 2026-02-09

### Added
- Multi-module project structure: `mjml-java-core`, `mjml-java-resolvers`, `mjml-java-spring`, `mjml-java-bom`
- `MjmlRenderResult` record returned by all `render()` overloads (breaking: previously some returned `String`)
- `render(Path)` and `render(Path, MjmlConfiguration)` overloads for rendering files directly
- `ResolverContext` parameter on `IncludeResolver.resolve()` (breaking: added second parameter)
- `Direction` enum (`LTR`, `RTL`, `AUTO`) for type-safe direction configuration
- `ContentSanitizer` functional interface for sanitizing inner HTML content
- `MjmlRenderException` for render-phase failures
- `ClasspathIncludeResolver` for resolving includes from the classpath
- Additional resolvers module: `MapIncludeResolver`, `CompositeIncludeResolver`, `CachingIncludeResolver`, `UrlIncludeResolver`, `PrefixRoutingIncludeResolver`
- Spring Boot auto-configuration: `MjmlService`, `ThymeleafMjmlService`, `SpringResourceIncludeResolver`, `MjmlProperties`
- SSRF protection in `UrlIncludeResolver` (blocks loopback/site-local/link-local addresses)
- Href sanitization: blocks `javascript:`, `vbscript:`, `data:text/html` URIs
- Hundreds of tests across all modules, including golden-file compatibility coverage

### Changed
- `UrlIncludeResolver` now requires explicit `allowedHosts(...)` for hostname URLs to further reduce SSRF risk.
- `CachingIncludeResolver` cache key now includes `ResolverContext` dimensions (`includingPath`, `includeType`) to avoid context-mismatch cache hits.
- `CachingIncludeResolver.Builder` now validates `ttl` (must be positive) and `maxEntries` (must be > 0).
- `mjml-java-spring` now generates JaCoCo reports alongside other modules for coverage visibility.

## [1.0.0-SNAPSHOT] - 2025

### Added
- Pure Java MJML v4 renderer with zero external dependencies
- All 31 standard MJML components:
  - Head (8): mj-head, mj-title, mj-preview, mj-font, mj-breakpoint, mj-style, mj-attributes, mj-html-attributes
  - Body layout (5): mj-body, mj-section, mj-column, mj-group, mj-wrapper
  - Content (7): mj-text, mj-image, mj-button, mj-divider, mj-spacer, mj-table, mj-raw
  - Interactive (11): mj-hero, mj-accordion, mj-accordion-element, mj-accordion-title, mj-accordion-text, mj-carousel, mj-carousel-image, mj-navbar, mj-navbar-link, mj-social, mj-social-element
- Built-in CSS inlining engine (`CssInliner`) usable independently
- `mj-include` support with `FileSystemIncludeResolver`
- Custom component registration via `MjmlConfiguration.Builder.registerComponent()`
- JPMS module: `dev.jcputney.mjml` with exports for `dev.jcputney.mjml`, `dev.jcputney.mjml.component`, `dev.jcputney.mjml.context`, `dev.jcputney.mjml.css`, `dev.jcputney.mjml.parser`
- Input validation: configurable `maxInputSize` (default 1 MB) and `maxNestingDepth` (default 100)
- Output sanitization: HTML-escaping of attribute values (configurable via `sanitizeOutput`)
- Path traversal protection in `FileSystemIncludeResolver`
- Thread-safe static API (`MjmlRenderer.render()`)
- Extensive test suite including unit coverage and 33 golden file compatibility tests
- JaCoCo code coverage reporting

[1.0.0]: https://github.com/jcputney/mjml-java/releases/tag/v1.0.0
[1.0.0-SNAPSHOT]: https://github.com/jcputney/mjml-java/releases/tag/v1.0.0-SNAPSHOT
