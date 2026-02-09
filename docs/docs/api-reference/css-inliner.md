---
sidebar_position: 5
title: CssInliner
---

# CssInliner

`dev.jcputney.mjml.css.CssInliner` is a standalone CSS inlining engine that can be used independently of the MJML renderer. It extracts CSS from `<style>` blocks, matches selectors against HTML elements, and merges the styles into inline `style=""` attributes.

## Methods

### inline(String html)

Extracts CSS from `<style>` blocks in the HTML, removes those blocks, and inlines the styles into matching elements. Non-inlineable rules are preserved in a single `<style>` block in the `<head>`.

```java
public static String inline(String html)
```

**Example:**

```java
String html = """
    <html>
    <head>
      <style>
        .greeting { color: red; font-size: 16px; }
        .greeting:hover { color: blue; }
      </style>
    </head>
    <body>
      <p class="greeting">Hello</p>
    </body>
    </html>
    """;

String result = CssInliner.inline(html);
// <p class="greeting" style="color: red; font-size: 16px;">Hello</p>
// :hover rule preserved in <style> block
```

### inline(String html, String additionalCss)

Same as `inline(html)`, but also inlines additional CSS provided as a string. Both the extracted `<style>` block CSS and the additional CSS are combined and inlined together.

```java
public static String inline(String html, String additionalCss)
```

**Example:**

```java
String additionalCss = ".greeting { font-weight: bold; }";
String result = CssInliner.inline(html, additionalCss);
// Combines styles from <style> blocks AND additionalCss
```

### inlineAdditionalOnly(String html, String css)

Inlines only the provided CSS into matching elements **without** extracting or removing existing `<style>` blocks from the HTML. This is useful when you want to add inline styles while preserving existing stylesheet blocks.

```java
public static String inlineAdditionalOnly(String html, String css)
```

**Example:**

```java
String html = """
    <html>
    <head>
      <style>
        /* This style block is preserved as-is */
        body { margin: 0; }
      </style>
    </head>
    <body>
      <p class="highlight">Important</p>
    </body>
    </html>
    """;

String css = ".highlight { background: yellow; }";
String result = CssInliner.inlineAdditionalOnly(html, css);
// <style> block remains untouched
// <p> gets style="background: yellow;" added
```

## What Gets Inlined vs. Preserved

| CSS Feature | Behavior |
|-------------|----------|
| Type selectors (`p`, `div`) | Inlined |
| Class selectors (`.foo`) | Inlined |
| ID selectors (`#bar`) | Inlined |
| Attribute selectors (`[type="text"]`) | Inlined |
| Combinators (`div > p`, `div p`, `div + p`, `div ~ p`) | Inlined |
| Pseudo-classes (`:hover`, `:focus`, `:nth-child()`) | Preserved in `<style>` block |
| Pseudo-elements (`::before`, `::after`) | Preserved in `<style>` block |
| `@media` queries | Preserved in `<style>` block |
| `@font-face` | Preserved in `<style>` block |
| `@keyframes` | Preserved in `<style>` block |

## Specificity and Cascade

The inliner respects CSS specificity rules. When multiple selectors match the same element, declarations are applied in order of specificity. Existing inline styles on elements take precedence over stylesheet rules, matching standard CSS cascade behavior.

## Standalone Usage

The `CssInliner` has no dependency on the MJML renderer and can be used with any HTML:

```java
import dev.jcputney.mjml.css.CssInliner;

// Works with any HTML, not just MJML output
String emailHtml = loadEmailTemplate();
String inlinedHtml = CssInliner.inline(emailHtml);
```

This is useful for email clients that strip `<style>` blocks and only support inline styles.

## HTML Parser Notes

The CSS inliner uses a lightweight, custom HTML parser (not the JDK XML parser). This is necessary because real-world HTML email templates often contain MSO (Microsoft Office) conditional comments (`<!--[if mso]>`) that are not valid XML. The custom parser handles these constructs without errors.
