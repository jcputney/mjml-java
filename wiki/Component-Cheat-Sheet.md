# Component Cheat Sheet

All 33 MJML v4 components supported by mjml-java.

## Head Components (8)

| Component | Description | MJML Docs |
|-----------|-------------|-----------|
| `mj-head` | Container for head elements | [Docs](https://documentation.mjml.io/) |
| `mj-title` | Sets the document `<title>` | [Docs](https://documentation.mjml.io/) |
| `mj-preview` | Email preview text (preheader) | [Docs](https://documentation.mjml.io/#mj-preview) |
| `mj-font` | Registers a web font | [Docs](https://documentation.mjml.io/#mj-font) |
| `mj-breakpoint` | Sets responsive breakpoint width | [Docs](https://documentation.mjml.io/#mj-breakpoint) |
| `mj-style` | Adds CSS to `<head>` or inlines it | [Docs](https://documentation.mjml.io/#mj-style) |
| `mj-attributes` | Default attribute values | [Docs](https://documentation.mjml.io/#mj-attributes) |
| `mj-html-attributes` | Custom HTML attributes | [Docs](https://documentation.mjml.io/#mj-html-attributes) |

## Body Layout Components (5)

| Component | Description | MJML Docs |
|-----------|-------------|-----------|
| `mj-body` | Root body container | [Docs](https://documentation.mjml.io/#mj-body) |
| `mj-section` | Full-width row for columns | [Docs](https://documentation.mjml.io/#mj-section) |
| `mj-column` | Column within a section | [Docs](https://documentation.mjml.io/#mj-column) |
| `mj-group` | Groups columns (prevents mobile stacking) | [Docs](https://documentation.mjml.io/#mj-group) |
| `mj-wrapper` | Wraps multiple sections | [Docs](https://documentation.mjml.io/#mj-wrapper) |

## Content Components (7)

| Component | Description | MJML Docs |
|-----------|-------------|-----------|
| `mj-text` | Rich text block | [Docs](https://documentation.mjml.io/#mj-text) |
| `mj-image` | Responsive image | [Docs](https://documentation.mjml.io/#mj-image) |
| `mj-button` | Call-to-action button | [Docs](https://documentation.mjml.io/#mj-button) |
| `mj-divider` | Horizontal rule | [Docs](https://documentation.mjml.io/#mj-divider) |
| `mj-spacer` | Vertical spacing | [Docs](https://documentation.mjml.io/#mj-spacer) |
| `mj-table` | HTML table passthrough | [Docs](https://documentation.mjml.io/#mj-table) |
| `mj-raw` | Raw HTML passthrough | [Docs](https://documentation.mjml.io/#mj-raw) |

## Interactive Components (13)

| Component | Description | MJML Docs |
|-----------|-------------|-----------|
| `mj-hero` | Hero section with background | [Docs](https://documentation.mjml.io/#mj-hero) |
| `mj-accordion` | Expandable content sections | [Docs](https://documentation.mjml.io/#mj-accordion) |
| `mj-accordion-element` | Single accordion item | [Docs](https://documentation.mjml.io/#mj-accordion) |
| `mj-accordion-title` | Accordion item title | [Docs](https://documentation.mjml.io/#mj-accordion) |
| `mj-accordion-text` | Accordion item content | [Docs](https://documentation.mjml.io/#mj-accordion) |
| `mj-carousel` | Image carousel | [Docs](https://documentation.mjml.io/#mj-carousel) |
| `mj-carousel-image` | Single carousel image | [Docs](https://documentation.mjml.io/#mj-carousel) |
| `mj-navbar` | Navigation bar | [Docs](https://documentation.mjml.io/#mj-navbar) |
| `mj-navbar-link` | Single navbar link | [Docs](https://documentation.mjml.io/#mj-navbar) |
| `mj-social` | Social media icon group | [Docs](https://documentation.mjml.io/#mj-social) |
| `mj-social-element` | Single social icon/link | [Docs](https://documentation.mjml.io/#mj-social) |

## Component Hierarchy

```
<mjml>
  <mj-head>
    <mj-title />
    <mj-preview />
    <mj-font />
    <mj-breakpoint />
    <mj-style />
    <mj-attributes>
      <mj-all />, <mj-class />, <mj-{tag} />
    </mj-attributes>
    <mj-html-attributes>
      <mj-selector />
    </mj-html-attributes>
  </mj-head>
  <mj-body>
    <mj-section>
      <mj-column>
        <mj-text />, <mj-image />, <mj-button />,
        <mj-divider />, <mj-spacer />, <mj-table />,
        <mj-raw />, <mj-social />, <mj-accordion />
      </mj-column>
      <mj-group>
        <mj-column> ... </mj-column>
      </mj-group>
    </mj-section>
    <mj-wrapper>
      <mj-section> ... </mj-section>
    </mj-wrapper>
    <mj-hero>
      <mj-text />, <mj-button />, <mj-image />, ...
    </mj-hero>
  </mj-body>
</mjml>
```

## Include Support

```xml
<mj-include path="./header.mjml" />
<mj-include path="./styles.mjml" type="css" />
<mj-include path="./styles.mjml" type="css" css-inline="inline" />
```
