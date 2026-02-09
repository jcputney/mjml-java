---
sidebar_position: 7
title: "Supported Components"
---

# Supported Components

mjml-java implements all 31 top-level MJML v4 renderable components. Each component listed below links to the official MJML documentation for attribute details, behavior, and visual examples.

## Head Components

| Component | Description | MJML Docs |
|-----------|-------------|-----------|
| `mj-head` | Container for head elements (title, fonts, styles, attributes) | [MJML Docs](https://documentation.mjml.io/) |
| `mj-title` | Sets the document `<title>` tag | [MJML Docs](https://documentation.mjml.io/) |
| `mj-preview` | Sets the email preview text (preheader) | [MJML Docs](https://documentation.mjml.io/#mj-preview) |
| `mj-font` | Registers a web font via `@import` or `<link>` | [MJML Docs](https://documentation.mjml.io/#mj-font) |
| `mj-breakpoint` | Sets the responsive breakpoint width | [MJML Docs](https://documentation.mjml.io/#mj-breakpoint) |
| `mj-style` | Adds CSS to the document `<head>` or inlines it | [MJML Docs](https://documentation.mjml.io/#mj-style) |
| `mj-attributes` | Defines default attribute values for components | [MJML Docs](https://documentation.mjml.io/#mj-attributes) |
| `mj-html-attributes` | Adds custom HTML attributes to rendered elements | [MJML Docs](https://documentation.mjml.io/#mj-html-attributes) |

## Body Layout Components

| Component | Description | MJML Docs |
|-----------|-------------|-----------|
| `mj-body` | Root body container; sets the email content width | [MJML Docs](https://documentation.mjml.io/#mj-body) |
| `mj-section` | Full-width row container for columns | [MJML Docs](https://documentation.mjml.io/#mj-section) |
| `mj-column` | Column within a section (auto-sized or explicit width) | [MJML Docs](https://documentation.mjml.io/#mj-column) |
| `mj-group` | Groups columns to prevent stacking on mobile | [MJML Docs](https://documentation.mjml.io/#mj-group) |
| `mj-wrapper` | Full-width wrapper around multiple sections | [MJML Docs](https://documentation.mjml.io/#mj-wrapper) |

## Content Components

| Component | Description | MJML Docs |
|-----------|-------------|-----------|
| `mj-text` | Rich text content block | [MJML Docs](https://documentation.mjml.io/#mj-text) |
| `mj-image` | Responsive image with optional link | [MJML Docs](https://documentation.mjml.io/#mj-image) |
| `mj-button` | Call-to-action button with link | [MJML Docs](https://documentation.mjml.io/#mj-button) |
| `mj-divider` | Horizontal rule / separator line | [MJML Docs](https://documentation.mjml.io/#mj-divider) |
| `mj-spacer` | Vertical spacing element | [MJML Docs](https://documentation.mjml.io/#mj-spacer) |
| `mj-table` | HTML table passthrough | [MJML Docs](https://documentation.mjml.io/#mj-table) |
| `mj-raw` | Raw HTML passthrough (not processed by MJML) | [MJML Docs](https://documentation.mjml.io/#mj-raw) |

## Interactive Components

| Component | Description | MJML Docs |
|-----------|-------------|-----------|
| `mj-hero` | Hero section with background image and overlay content | [MJML Docs](https://documentation.mjml.io/#mj-hero) |
| `mj-accordion` | Expandable/collapsible content sections | [MJML Docs](https://documentation.mjml.io/#mj-accordion) |
| `mj-accordion-element` | Single item within an accordion | [MJML Docs](https://documentation.mjml.io/#mj-accordion) |
| `mj-accordion-title` | Title (clickable header) of an accordion element | [MJML Docs](https://documentation.mjml.io/#mj-accordion) |
| `mj-accordion-text` | Body content of an accordion element | [MJML Docs](https://documentation.mjml.io/#mj-accordion) |
| `mj-carousel` | Image carousel / slideshow | [MJML Docs](https://documentation.mjml.io/#mj-carousel) |
| `mj-carousel-image` | Single image within a carousel | [MJML Docs](https://documentation.mjml.io/#mj-carousel) |
| `mj-navbar` | Horizontal navigation bar | [MJML Docs](https://documentation.mjml.io/#mj-navbar) |
| `mj-navbar-link` | Single link within a navbar | [MJML Docs](https://documentation.mjml.io/#mj-navbar) |
| `mj-social` | Social media icon group | [MJML Docs](https://documentation.mjml.io/#mj-social) |
| `mj-social-element` | Single social media icon/link | [MJML Docs](https://documentation.mjml.io/#mj-social) |

## Include Support

In addition to the 31 components above, mjml-java supports `mj-include` for splitting templates across files:

```xml
<mj-include path="./header.mjml" />
<mj-include path="./styles.mjml" type="css" />
<mj-include path="./styles.mjml" type="css" css-inline="inline" />
```

Include resolution requires configuring an `IncludeResolver`:

```java
MjmlConfiguration config = MjmlConfiguration.builder()
    .includeResolver(new FileSystemIncludeResolver(Path.of("/templates")))
    .build();
```

## Custom Components

You can register custom components alongside the built-in ones:

```java
MjmlConfiguration config = MjmlConfiguration.builder()
    .registerComponent("mj-custom", (node, globalCtx, renderCtx) -> {
        return new MyCustomComponent(node, globalCtx, renderCtx);
    })
    .build();
```

Custom body components extend `BodyComponent` and implement the `render()` method. Custom head components extend `HeadComponent` and implement the `process()` method.

Note: MJML control/helper tags such as `mj-all`, `mj-class`, `mj-selector`, and `mj-html-attribute` are supported as part of attribute processing, but they are not counted as top-level renderable components.

For component attributes, behavior, and visual examples, see the [official MJML documentation](https://documentation.mjml.io/).
