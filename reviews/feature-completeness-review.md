# Feature Completeness Review: mjml-java vs MJML v4

**Reviewer:** feature-reviewer (thorough re-review)
**Date:** 2026-02-08
**Scope:** Component coverage, attribute support, default values, mj-include, mj-attributes cascade, CSS inlining, media queries, responsive features, RTL support, golden test coverage

---

## Executive Summary

**Overall Feature Completeness: ~93%**

mjml-java implements all 33 MJML v4 components with high fidelity. The 7-phase rendering pipeline, 5-level attribute cascade, CSS inlining engine, mj-include support, and responsive design (media queries + MSO conditionals) are all present and working. 33/33 golden tests pass, demonstrating strong compatibility with official MJML output.

The remaining ~7% consists of:
- A few attribute bugs in mj-navbar-link and mj-button (target/rel not applied)
- Missing social networks (mail, telegram, reddit)
- No MJML validation/linting mode
- No minification/beautification options
- mj-raw `position` attribute not implemented

**Correction from prior review:** The prior review incorrectly stated that mj-html-attributes are "parsed but not applied." They ARE applied in `RenderPipeline.applyHtmlAttributes()` (lines 219-276), which matches CSS selectors against rendered HTML elements and inserts attributes. Similarly, `rel` IS implemented on mj-image (line 129-131) and mj-button (line 137-139). `container-background-color` IS in mj-image DEFAULTS (line 22).

---

## 1. Component Coverage

### Status: COMPLETE (33/33 components)

All MJML components are implemented and registered in `RenderPipeline.createRegistry()`:

| Category | Components | Status |
|----------|-----------|--------|
| **Head** | mj-head, mj-title, mj-preview, mj-font, mj-breakpoint, mj-style, mj-attributes, mj-html-attributes | All implemented |
| **Body Layout** | mj-body, mj-section, mj-column, mj-group, mj-wrapper | All implemented |
| **Content** | mj-text, mj-image, mj-button, mj-table, mj-divider, mj-spacer, mj-raw | All implemented |
| **Interactive** | mj-hero, mj-accordion, mj-accordion-element, mj-accordion-title, mj-accordion-text, mj-carousel, mj-carousel-image, mj-social, mj-social-element, mj-navbar, mj-navbar-link | All implemented |
| **Include** | mj-include (mjml, html, css types) | All implemented |
| **Attribute System** | mj-all, mj-class (via mj-attributes) | All implemented |

---

## 2. Detailed Attribute Analysis

### 2.1 mj-body - COMPLETE

| Attribute | Implemented | Default | Spec Match |
|-----------|:-----------:|---------|:----------:|
| width | Yes | "600px" | Yes |
| background-color | Yes | (via getAttribute) | Yes |
| css-class | Yes | (via getAttribute) | Yes |

### 2.2 mj-section - COMPLETE

All 17 spec attributes implemented: background-color, background-position, background-position-x, background-position-y, background-repeat, background-size, background-url, border, border-bottom, border-left, border-radius, border-right, border-top, direction, full-width, padding (including individual padding-*), text-align, text-padding, css-class.

### 2.3 mj-column - COMPLETE

All 20+ attributes: background-color, border, border-bottom/left/right/top, border-radius, direction, inner-background-color, inner-border, inner-border-bottom/left/right/top, inner-border-radius, padding, padding-bottom/left/right/top, vertical-align, width, css-class.

### 2.4 mj-group - COMPLETE

| Attribute | Implemented | Default |
|-----------|:-----------:|---------|
| background-color | Yes | "" |
| direction | Yes | "ltr" |
| vertical-align | Yes | "top" |
| width | Yes | "" |
| css-class | Yes | (via attr) |

### 2.5 mj-text - COMPLETE

All 17 spec attributes: align, color, container-background-color, css-class, font-family, font-size, font-style, font-weight, height, letter-spacing, line-height, padding, padding-bottom/left/right/top, text-decoration, text-transform.

### 2.6 mj-image - COMPLETE

All documented MJML v4 attributes present in DEFAULTS:

| Attribute | In DEFAULTS | Rendered |
|-----------|:-----------:|:--------:|
| align | Yes | Yes |
| alt | Yes | Yes |
| border | Yes ("0") | Yes |
| border-radius | Yes | Yes |
| container-background-color | Yes ("") | Yes |
| fluid-on-mobile | Yes | Yes |
| height | Yes ("auto") | Yes |
| href | Yes | Yes |
| padding | Yes ("10px 25px") | Yes |
| rel | Yes ("") | Yes |
| sizes | Yes | Yes |
| src | Yes | Yes |
| srcset | Yes | Yes |
| target | Yes ("_blank") | Yes |
| title | Yes | Yes |
| width | Yes | Yes |

**Missing:** `usemap` (rarely used HTML image map support), `name` (for mj-html-attributes targeting).

### 2.7 mj-button - MOSTLY COMPLETE

All key attributes implemented. `rel` IS supported (line 137-139). `target` IS configurable (line 142 uses `getAttribute("target", "_blank")`).

| Attribute | In DEFAULTS | Rendered | Notes |
|-----------|:-----------:|:--------:|-------|
| align | Yes | Yes | |
| background-color | Yes ("#414141") | Yes | |
| border | Yes ("none") | Yes | |
| border-bottom/left/right/top | Yes | Yes | |
| border-radius | Yes ("3px") | Yes | |
| color | Yes ("#ffffff") | Yes | |
| container-background-color | Yes | Yes | |
| font-family | Yes | Yes | |
| font-size | Yes ("13px") | Yes | |
| font-style | Yes | Yes | |
| font-weight | Yes ("normal") | Yes | |
| height | Yes | Yes | |
| href | Yes ("#") | Yes | |
| inner-padding | Yes ("10px 25px") | Yes | |
| letter-spacing | Yes | Yes | |
| line-height | Yes ("120%") | Yes | |
| padding | Yes ("10px 25px") | Yes | |
| rel | Yes ("") | Yes | |
| target | Yes ("_blank") | Yes | |
| text-align | Yes | Yes | Not rendered as CSS |
| text-decoration | Yes ("none") | Yes | |
| text-transform | Yes ("none") | Yes | |
| vertical-align | Yes ("middle") | Yes | |
| width | Yes | Yes | Pixel calculation with inner-padding |

### 2.8 mj-table - COMPLETE

All spec attributes: align, border, cellpadding, cellspacing, color, container-background-color, font-family, font-size, line-height, padding, table-layout, width, css-class.

### 2.9 mj-divider - COMPLETE

All spec attributes: align, border-color, border-style, border-width, container-background-color, css-class, padding, width.

### 2.10 mj-spacer - COMPLETE

| Attribute | In DEFAULTS |
|-----------|:-----------:|
| height | Yes ("20px") |
| container-background-color | Yes ("") |
| padding | Yes ("") |
| vertical-align | Yes ("") |

### 2.11 mj-social / mj-social-element - COMPLETE

MjSocial supports 17 attributes. MjSocialElement supports 22+ attributes including name, src, href, alt, target, icon-size, icon-padding, inner-padding, text-padding, border-radius, and all typography attributes.

**Social Networks (18 registered):**
facebook, twitter, x, google, pinterest, linkedin, tumblr, xing, github, instagram, web, snapchat, youtube, vimeo, medium, soundcloud, dribbble, tiktok, whatsapp

**Missing networks:** `mail` (commonly used in MJML), `telegram`, `reddit`, `line`.

### 2.12 mj-navbar / mj-navbar-link

**MjNavbar - COMPLETE:** All 16 attributes including all ico-* hamburger attributes.

**MjNavbarLink - BUG:** The `target` attribute is in DEFAULTS but the render method hardcodes `target="_blank"` (line 72) instead of using `getAttribute("target", ...)`. The `rel` attribute is in DEFAULTS but never rendered.

```java
// Line 72 in MjNavbarLink.java - BUG: hardcoded instead of using attribute
sb.append(" target=\"_blank\"");
```

### 2.13 mj-hero - COMPLETE

All 11 attributes: background-color, background-height, background-position, background-url, background-width, border-radius, height, mode (fixed-height/fluid-height), padding, vertical-align, width.

### 2.14 mj-wrapper - COMPLETE

All attributes matching mj-section (background-*, border-*, padding, text-align, full-width, css-class).

### 2.15 mj-raw - MOSTLY COMPLETE

Simple pass-through. **Missing:** `position` attribute (MJML v4 supports `position="file-start"` to output content at the very beginning of the HTML document, before `<!doctype>`).

### 2.16 mj-accordion* - COMPLETE

MjAccordion: 12 attributes. MjAccordionElement: 13 attributes. MjAccordionTitle: 5 attributes. MjAccordionText: 11 attributes. CSS checkbox hack fully implemented. Icon attribute inheritance from parent accordion works correctly.

### 2.17 mj-carousel* - COMPLETE

MjCarousel: 14 attributes. MjCarouselImage: 8 attributes. Full CSS state machine with radio inputs, sibling combinators, thumbnail hover, circular prev/next, OWA rules, Yahoo fallback, noinput fallback.

### 2.18 Head Components - ALL COMPLETE

All 8 head components fully implemented with correct behavior.

---

## 3. Cross-Cutting Feature Analysis

### 3.1 Attribute Cascade - COMPLETE (5-level)

Implemented in `AttributeResolver.java`:
1. Inline attributes (highest priority)
2. mj-class attributes (space-separated, applied in order)
3. Tag-specific defaults (`<mj-attributes><mj-section .../>`)
4. mj-all defaults (`<mj-attributes><mj-all .../>`)
5. Component hardcoded defaults (lowest priority)

### 3.2 mj-include Support - COMPLETE

| Type | Supported | Implementation |
|------|:---------:|---------------|
| mjml (default) | Yes | Parses included MJML, replaces with body/head children |
| html | Yes | Wraps in mj-raw element |
| css | Yes | Creates mj-style element, supports `css-inline` attribute |

Additional features: cycle detection, max depth (50), full/fragment document handling, recursive processing.

### 3.3 CSS Inlining Engine - COMPLETE

- Full CSS selector matching (type, class, ID, attribute, combinators)
- Specificity-based precedence
- Pseudo-class/pseudo-element preservation in `<style>` blocks
- `mj-style inline="inline"` support via `inlineAdditionalOnly()`
- Post-processing for juice compatibility (empty style attrs, self-closing tags)
- Lightweight HTML parser (handles MSO conditional comments)

### 3.4 mj-html-attributes - COMPLETE

Parsed in `MjHtmlAttributes.java`, applied in `RenderPipeline.applyHtmlAttributes()`:
- CSS selector matching against rendered HTML elements
- Attribute insertion at tag boundaries
- Position-aware insertion (handles self-closing tags)
- Multiple attributes per selector supported

### 3.5 Responsive Design / Media Queries - COMPLETE

| Feature | Status |
|---------|--------|
| Column-width media queries | Yes |
| mj-breakpoint customization | Yes (default 480px) |
| Thunderbird .moz-text-html selectors | Yes |
| fluid-on-mobile for images | Yes |
| mj-outlook-group-fix | Yes |
| MSO conditional comments | Yes |
| VML backgrounds (v:rect, v:image) | Yes |
| MSO section transition merging | Yes |

### 3.6 Font System - COMPLETE

- 5 auto-registered default fonts (Open Sans, Droid Sans, Lato, Roboto, Ubuntu)
- `mj-font` overrides default URLs
- Body tree scanning for font-family usage
- Font link tags + @import in non-MSO conditional
- Custom font registration via `mj-font` with arbitrary name/href

### 3.7 HTML Document Skeleton - COMPLETE

DOCTYPE, HTML lang/dir/xmlns attributes, meta tags, CSS resets, MSO noscript/OfficeDocumentSettings, font imports, media queries, component styles, mj-style blocks, head comments, body background-color, preview text, ARIA attributes on wrapper div.

### 3.8 RTL / Direction Support - COMPLETE

- `direction` attribute on mj-section, mj-column, mj-wrapper
- `lang` attribute on `<html>` from configuration
- `dir="auto"` on `<html>` and body wrapper
- Golden test coverage (direction-rtl.mjml)

### 3.9 Security Features - PRESENT (beyond MJML v4 spec)

- Optional HTML sanitization (`sanitizeOutput`)
- Max input size limit (1 MB default, configurable)
- Max nesting depth limit (100 default, configurable)
- Include cycle detection
- Head comment injection prevention
- SSRF warning in IncludeResolver docs

### 3.10 Custom Component Registration - COMPLETE

- `MjmlConfiguration.builder().registerComponent(tagName, factory)`
- ComponentFactory interface
- Registry freezing after initialization
- Custom components participate in full attribute cascade

---

## 4. Golden Test Coverage (33/33 passing)

| # | Test | Components/Features |
|---|------|---------------------|
| 1 | simple-text | mj-text basic |
| 2 | head-with-title | mj-title, mj-preview |
| 3 | two-columns | mj-section, mj-column |
| 4 | all-content-components | text, image, button, divider, spacer, table |
| 5 | multi-column-layout | 3+ columns, width calculation |
| 6 | head-attributes | mj-attributes cascade |
| 7 | background-images | Section/wrapper VML backgrounds |
| 8 | full-width-section | full-width mode |
| 9 | hero-component | mj-hero fixed/fluid height |
| 10 | accordion-component | CSS checkbox hack |
| 11 | navbar-component | Hamburger menu |
| 12 | social-component | Horizontal/vertical modes |
| 13 | carousel-component | Full CSS state machine |
| 14 | nested-sections | Wrapper with sections |
| 15 | text-formatting | Block/inline HTML in mj-text |
| 16 | image-variants | srcset, fluid-on-mobile, href |
| 17 | button-variants | width, border, inner-padding |
| 18 | column-group | mj-group |
| 19 | custom-width | Non-600px container |
| 20 | html-entities | Entity handling |
| 21 | empty-sections | Empty section/column |
| 22 | padding-border | Padding + border combos |
| 23 | direction-rtl | RTL support |
| 24 | column-inner-styles | inner-background/border |
| 25 | css-class-cascade | mj-class resolution |
| 26 | divider-variants | align, border-style |
| 27 | spacer-variants | Various heights |
| 28 | wrapper-advanced | Full-width, bg images |
| 29 | multiple-mj-class | Space-separated mj-class |
| 30 | column-vertical-align | Vertical alignment |
| 31 | section-background-variants | bg position/size/repeat |
| 32 | table-advanced | Complex table content |
| 33 | raw-content | mj-raw pass-through |

---

## 5. Missing Features (by importance)

### High Importance

| # | Gap | Impact | Effort |
|---|-----|--------|--------|
| 1 | **mj-navbar-link `target` attribute ignored** - hardcoded to `_blank` | Users can't set custom link targets | Trivial fix |
| 2 | **mj-navbar-link `rel` attribute not rendered** - in DEFAULTS but not in render() | No rel attribute on navbar links | Trivial fix |
| 3 | **Missing `mail` social network** - very commonly used | Can't render mail social icons | Easy addition |

### Medium Importance

| # | Gap | Impact | Effort |
|---|-----|--------|--------|
| 4 | **No MJML validation mode** | No structured error reporting for invalid MJML | Medium |
| 5 | **Missing social networks** - telegram, reddit, line | Can't render these network icons without custom src | Easy |
| 6 | **No minification option** | Output always readable/indented | Medium |
| 7 | **mj-raw `position="file-start"`** | Can't inject content before DOCTYPE | Low effort |

### Low Importance

| # | Gap | Impact | Effort |
|---|-----|--------|--------|
| 8 | No `beautify` option | Output formatting not configurable | Medium |
| 9 | No `validationLevel` option | Can't configure strict/soft/skip | Medium |
| 10 | No programmatic error collection | Errors throw exceptions vs. accumulate | Medium |
| 11 | `mj-image` missing `usemap` | No image map support | Low |
| 12 | `dir` hardcoded to "auto" | Can't force LTR/RTL | Trivial |

---

## 6. Deviations from Official MJML Behavior

### 6.1 Language attribute default
- **mjml-java:** Defaults to `"und"` (undetermined BCP 47 tag)
- **Official MJML:** No lang attribute if not specified
- **Impact:** More accessible default. Positive deviation.

### 6.2 Unique ID generation
- **mjml-java:** Deterministic counter-based (`carousel-0`, `navbar-1`)
- **Official MJML:** Random hex IDs
- **Impact:** Better for testing/reproducibility. Functional.

### 6.3 CSS formatting
- **mjml-java:** Consistent 4+2-per-depth indentation
- **Official MJML:** Variable formatting via juice/cheerio
- **Impact:** Cosmetic only. Golden tests pass.

### 6.4 Empty style post-processing
- **mjml-java:** `style=""` → `style` and `/>` → `>` only when inline CSS active
- **Official MJML:** Always (via juice round-trip)
- **Impact:** Only matters for mj-style inline="inline" documents. Matches expected output.

---

## 7. Summary

### Strengths
- **100% component coverage** (33/33 MJML v4 components)
- **100% golden test pass rate** (33/33 tests)
- **Full 5-level attribute cascade** with multiple mj-class support
- **Full mj-include support** (3 types + cycle detection)
- **Comprehensive VML/MSO** Outlook compatibility
- **Complex interactive components** (carousel CSS state machine, accordion checkbox hack, navbar hamburger)
- **Full CSS inlining engine** with specificity handling
- **mj-html-attributes** CSS selector matching and application
- **Font auto-registration** with 5 default web fonts
- **Security features** beyond MJML spec (sanitization, size limits, depth limits)

### Actionable Fixes (quick wins)
1. Fix mj-navbar-link to use `getAttribute("target", "_blank")` instead of hardcoded `_blank`
2. Add `rel` attribute rendering to mj-navbar-link
3. Add `mail` (and optionally `telegram`, `reddit`) to SocialNetworkRegistry

### Areas for Future Enhancement
- MJML validation/linting API
- Output minification option
- mj-raw `position="file-start"` support
- Programmatic error collection (vs. exception throwing)

### Verdict
The library provides **production-ready MJML v4 rendering** with excellent spec compliance. The two attribute bugs in mj-navbar-link are the only functional issues. All other gaps are missing optional features (validation, minification) or rarely-used attributes. The implementation quality is high, with particularly impressive handling of VML backgrounds, carousel state machines, and the CSS inlining engine.
