# Changelog

All notable changes to mjml-java are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

## [1.0.0-SNAPSHOT] - 2025

### Added
- Pure Java MJML v4 renderer with zero external dependencies
- All 33 standard MJML components:
  - Head (8): mj-head, mj-title, mj-preview, mj-font, mj-breakpoint, mj-style, mj-attributes, mj-html-attributes
  - Body layout (5): mj-body, mj-section, mj-column, mj-group, mj-wrapper
  - Content (7): mj-text, mj-image, mj-button, mj-divider, mj-spacer, mj-table, mj-raw
  - Interactive (13): mj-hero, mj-accordion, mj-accordion-element, mj-accordion-title, mj-accordion-text, mj-carousel, mj-carousel-image, mj-navbar, mj-navbar-link, mj-social, mj-social-element
- Built-in CSS inlining engine (`CssInliner`) usable independently
- `mj-include` support with `FileSystemIncludeResolver`
- Custom component registration via `MjmlConfiguration.Builder.registerComponent()`
- JPMS module: `dev.jcputney.mjml` with exports for `dev.jcputney.mjml` and `dev.jcputney.mjml.css`
- Input validation: configurable `maxInputSize` (default 1 MB) and `maxNestingDepth` (default 100)
- Output sanitization: HTML-escaping of attribute values (configurable via `sanitizeOutput`)
- Path traversal protection in `FileSystemIncludeResolver`
- Thread-safe static API (`MjmlRenderer.render()`)
- 196 tests: 163 unit tests + 33 golden file compatibility tests
- JaCoCo code coverage reporting

[Unreleased]: https://github.com/jcputney/mjml-java/compare/v1.0.0-SNAPSHOT...HEAD
[1.0.0-SNAPSHOT]: https://github.com/jcputney/mjml-java/releases/tag/v1.0.0-SNAPSHOT
