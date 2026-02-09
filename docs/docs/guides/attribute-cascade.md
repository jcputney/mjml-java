---
sidebar_position: 3
title: Attribute Cascade
---

# Attribute Cascade

mjml-java resolves component attributes through a 5-level cascade, similar to how CSS specificity works. When a component calls `getAttribute("color")`, the value is looked up at each level in order, and the first non-null result wins.

## The 5 Levels

| Priority | Level | Source |
|----------|-------|--------|
| 1 (highest) | Inline attributes | Set directly on the element |
| 2 | mj-class attributes | Defined via `mj-class` on the element |
| 3 | Tag-specific defaults | Defined in `mj-attributes` for a tag type |
| 4 | mj-all defaults | Defined in `mj-attributes` under `mj-all` |
| 5 (lowest) | Component defaults | Hardcoded in `getDefaultAttributes()` |

### Level 1: Inline Attributes

Attributes set directly on the element have the highest priority:

```xml
<mj-text color="#ff0000" font-size="20px">Hello</mj-text>
```

### Level 2: mj-class Attributes

The `mj-class` attribute references named classes defined in `mj-attributes`. Multiple classes can be space-separated; the first class with a matching attribute wins:

```xml
<mjml>
  <mj-head>
    <mj-attributes>
      <mj-class name="highlight" color="#ff0000" font-weight="bold" />
      <mj-class name="large" font-size="24px" />
    </mj-attributes>
  </mj-head>
  <mj-body>
    <mj-section>
      <mj-column>
        <mj-text mj-class="highlight large">Styled text</mj-text>
        <!-- color=#ff0000, font-weight=bold from "highlight" -->
        <!-- font-size=24px from "large" -->
      </mj-column>
    </mj-section>
  </mj-body>
</mjml>
```

### Level 3: Tag-Specific Defaults

Define defaults for all instances of a specific tag in `mj-attributes`:

```xml
<mj-head>
  <mj-attributes>
    <mj-text font-size="16px" color="#333333" />
    <mj-section background-color="#f4f4f4" />
    <mj-button background-color="#336699" color="#ffffff" />
  </mj-attributes>
</mj-head>
```

Every `mj-text` in the document will use `font-size="16px"` and `color="#333333"` unless overridden by a higher-priority level.

### Level 4: mj-all Defaults

The `mj-all` element sets defaults that apply to every component:

```xml
<mj-head>
  <mj-attributes>
    <mj-all font-family="Arial, sans-serif" padding="0" />
  </mj-attributes>
</mj-head>
```

These are lower priority than tag-specific defaults, so `mj-text` settings override `mj-all` settings.

### Level 5: Component Defaults

Every component defines hardcoded defaults through `getDefaultAttributes()`. These are the fallback when no other level provides a value:

```java
@Override
public Map<String, String> getDefaultAttributes() {
    return Map.of(
        "font-size", "13px",
        "color", "#000000",
        "font-family", "Ubuntu, Helvetica, Arial, sans-serif"
    );
}
```

## Resolution Example

Given this template:

```xml
<mjml>
  <mj-head>
    <mj-attributes>
      <mj-all font-family="Arial, sans-serif" />
      <mj-text color="#333333" font-size="16px" />
      <mj-class name="brand" color="#336699" />
    </mj-attributes>
  </mj-head>
  <mj-body>
    <mj-section>
      <mj-column>
        <mj-text mj-class="brand" font-size="20px">Hello</mj-text>
      </mj-column>
    </mj-section>
  </mj-body>
</mjml>
```

The `mj-text` element resolves attributes as follows:

| Attribute | Resolved Value | Source Level |
|-----------|---------------|--------------|
| `font-size` | `20px` | Level 1 (inline) |
| `color` | `#336699` | Level 2 (mj-class "brand") |
| `font-family` | `Arial, sans-serif` | Level 4 (mj-all) |
| `line-height` | `1` | Level 5 (component default) |

Note that `color="#333333"` from Level 3 (tag default) is **not** used because Level 2 (mj-class) has higher priority.

## How It Works

The cascade is implemented in `AttributeResolver.resolve()`:

```java
public static String resolve(MjmlNode node, String attributeName,
    GlobalContext globalContext, Map<String, String> componentDefaults) {

    // Level 1: Inline attribute
    String value = node.getAttribute(attributeName);
    if (value != null) return value;

    // Level 2: mj-class attributes
    String mjClass = node.getAttribute("mj-class");
    if (mjClass != null) {
        for (String className : mjClass.split("\\s+")) {
            Map<String, String> classAttrs =
                globalContext.getClassAttributes(className);
            value = classAttrs.get(attributeName);
            if (value != null) return value;
        }
    }

    // Level 3: Tag-specific defaults
    value = globalContext.getDefaultAttributes(node.getTagName())
                         .get(attributeName);
    if (value != null) return value;

    // Level 4: mj-all defaults
    value = globalContext.getAllDefaults().get(attributeName);
    if (value != null) return value;

    // Level 5: Component hardcoded defaults
    return componentDefaults.get(attributeName);
}
```

## Custom Components and the Cascade

Custom components automatically participate in the cascade. When you register a component with tag name `"mj-greeting"`, you can set defaults for it in `mj-attributes`:

```xml
<mj-head>
  <mj-attributes>
    <mj-greeting color="#336699" name="Developer" />
  </mj-attributes>
</mj-head>
```

These Level 3 defaults apply to all `mj-greeting` elements in the document, and can be overridden by inline attributes or `mj-class` values. See [Custom Components](./custom-components.md) for more details.

## Practical Patterns

### Brand Theming

Set brand colors and fonts at the `mj-all` and tag-specific levels, then override per-element when needed:

```xml
<mj-attributes>
  <mj-all font-family="Helvetica, Arial, sans-serif" />
  <mj-text color="#2c3e50" font-size="14px" line-height="1.6" />
  <mj-button background-color="#3498db" color="#ffffff" font-size="16px" />
  <mj-section padding="20px 0" />
</mj-attributes>
```

### Reusable Style Classes

Use `mj-class` to create reusable style combinations:

```xml
<mj-attributes>
  <mj-class name="heading" font-size="24px" font-weight="700" color="#1a1a1a" />
  <mj-class name="subheading" font-size="18px" color="#666666" />
  <mj-class name="cta" background-color="#e74c3c" color="#ffffff" font-size="18px" />
</mj-attributes>
```

```xml
<mj-text mj-class="heading">Welcome!</mj-text>
<mj-text mj-class="subheading">Check out what's new</mj-text>
<mj-button mj-class="cta" href="https://example.com">Get Started</mj-button>
```

### Multiple Classes

Combine multiple classes on a single element. For attributes defined in more than one class, the first class listed takes precedence:

```xml
<mj-text mj-class="heading brand">Title</mj-text>
<!-- If both "heading" and "brand" define "color", -->
<!-- the value from "heading" is used -->
```
