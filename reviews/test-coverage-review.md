# Test Coverage Review: mjml-java

## Executive Summary

The mjml-java project has **~196 tests** across **24 test files** covering **71 source files**. The test suite demonstrates solid foundational coverage of the rendering pipeline, CSS subsystem, and parser. Golden file tests (33) provide excellent end-to-end regression coverage against the official MJML renderer output.

**Overall Test Quality: B+**

**Key strengths:**
- Golden file tests (33) provide pixel-perfect compatibility validation against official MJML
- CSS subsystem (parser, selector matcher/parser, inliner, style attribute) is the best-tested area with ~70 tests
- Thread safety, security, custom components, and FileSystemIncludeResolver all have dedicated tests
- Edge cases for null/empty/malformed input are well covered at the renderer level
- Test execution is fast (~2s for all tests)

**Key gaps:**
- No dedicated unit tests for ~25 individual component classes (all body/content/interactive/head components lack isolated unit tests)
- No direct unit tests for core infrastructure: `AttributeResolver`, `GlobalContext`, `RenderContext`, `HtmlSkeleton`, `RenderPipeline`, `HtmlDocumentParser`
- No tests for utility classes: `HtmlEscaper`, `SocialNetworkRegistry`, `DefaultFontRegistry`, `BackgroundCssHelper`, `ColumnWidthCalculator`
- Limited negative/error path testing beyond null/empty input
- No property-based or fuzz testing
- No code coverage tooling (JaCoCo) configured

---

## Test Organization

### 24 Test Classes

| Test File | Tests | Layer |
|-----------|-------|-------|
| **Core / Integration** |||
| `MjmlRendererTest` | 11 | Integration (public API) |
| `EdgeCaseTest` | ~18 | Integration (error/boundary) |
| `AllComponentsRenderTest` | ~23 | Integration (component smoke tests) |
| `GoldenFileTest` | 33 | Regression (golden file comparison) |
| `CustomComponentTest` | 3 | Integration (custom component API) |
| `ThreadSafetyTest` | 2 | Concurrency (parallel renders) |
| `SecurityTest` | 8 | Security (CDATA injection, input limits, nesting depth) |
| `FileSystemIncludeResolverTest` | 8 | Unit + Integration (include resolver) |
| **Parser** |||
| `MjmlParserTest` | 7 | Unit (parser) |
| `MjmlPreprocessorTest` | 8 | Unit (preprocessor) |
| `IncludeProcessorTest` | 7 | Integration (include resolution) |
| **CSS Engine** |||
| `CssInlinerTest` | 14 | Unit (CSS inliner) |
| `CssParserTest` | 11 | Unit (CSS parser) |
| `CssSelectorParserTest` | 17 | Unit (selector parser) |
| `CssSelectorMatcherTest` | 14 | Unit (selector matching) |
| `StyleAttributeTest` | 9 | Unit (style parsing/merging) |
| **Component-Specific** |||
| `MjSectionRenderTest` | 4 | Integration (section render variants) |
| `MjWrapperRenderTest` | 3 | Integration (wrapper render variants) |
| `ColumnWidthTest` | 3 | Integration (column width calc) |
| `MjHtmlAttributesTest` | 5 | Integration (HTML attribute injection) |
| **Utilities** |||
| `CssUnitParserTest` | 14 | Unit (CSS unit parsing) |
| `CssBoxModelTest` | 4 | Unit (box model calc) |
| `BackgroundPositionHelperTest` | 12 | Unit (bg position normalization) |
| `VmlCoordinateTest` | 9 | Unit (VML coordinate math) |

**Strengths:**
- Clear separation between unit tests (CSS engine, parser, utilities) and integration tests (renderer, components)
- Golden file tests use JUnit 5 `@TestFactory` for dynamic test generation
- No test inheritance or complex setup hierarchies
- Text block syntax (Java 17 `"""`) makes MJML templates highly readable

**Observation:** The testing pyramid is integration-heavy. Most component behavior is tested through end-to-end rendering rather than isolated unit tests. This is practical but limits diagnostic precision when failures occur.

---

## Coverage Matrix

### Legend
- **None**: No dedicated tests
- **Minimal**: Tested only indirectly (via golden files or integration tests)
- **Good**: Has dedicated test file with multiple assertions
- **Excellent**: Thorough unit tests covering happy path, edge cases, and error conditions

### Core Infrastructure

| Class | Coverage | Notes |
|-------|----------|-------|
| `MjmlRenderer` | **Good** | 11 tests: basic rendering, title, preview, entities, fonts, styles, error cases |
| `MjmlConfiguration` | **Minimal** | Tested indirectly through renderer/security/custom component tests |
| `MjmlRenderResult` | **Minimal** | Tested indirectly via renderer |
| `MjmlException` | **Minimal** | Used in assertThrows but no direct testing |
| `RenderPipeline` | **None** | No unit tests; only exercised via `MjmlRenderer.render()` |
| `HtmlSkeleton` | **None** | No unit tests; exercised only indirectly |
| `GlobalContext` | **None** | No unit tests for this central state class |
| `RenderContext` | **None** | No unit tests |
| `AttributeResolver` | **None** | No unit tests for the 5-level cascade logic |
| `ComponentRegistry` | **Minimal** | Tested indirectly via custom component tests |
| `DefaultFontRegistry` | **None** | No unit tests for auto-font-registration |

### Parser Layer

| Class | Coverage | Notes |
|-------|----------|-------|
| `MjmlParser` | **Good** | 7 tests: parse, attributes, HTML content, errors |
| `MjmlPreprocessor` | **Good** | 8 tests: CDATA wrapping, entities, edge cases |
| `IncludeProcessor` | **Good** | 7 tests: MJML/HTML/CSS includes, circular, missing |
| `MjmlNode` | **Minimal** | Tested indirectly through parser tests |
| `MjmlDocument` | **Minimal** | Tested indirectly through parser tests |
| `EntityTable` | **Minimal** | Tested indirectly through preprocessor |

### CSS Subsystem

| Class | Coverage | Notes |
|-------|----------|-------|
| `CssParser` | **Excellent** | 11 tests: rules, media queries, keyframes, comments, URLs |
| `CssSelectorParser` | **Excellent** | 17 tests: all selector types, specificity, edge cases |
| `CssSelectorMatcher` | **Excellent** | 14 tests: all combinator types, attributes, pseudo |
| `CssInliner` | **Excellent** | 14 tests: selectors, specificity, !important, media queries |
| `StyleAttribute` | **Good** | 9 tests: parse, serialize, merge, !important |
| `CssSpecificity` | **Good** | Tested through selector parser specificity tests |
| `HtmlDocumentParser` | **None** | No direct unit tests; exercised only through CssInliner |
| `HtmlElement` | **Minimal** | Tested only through CssSelectorMatcher |

### Body Components

| Class | Coverage | Notes |
|-------|----------|-------|
| `MjBody` | **Minimal** | No dedicated tests; exercised via renderer |
| `MjSection` | **Good** | 4 dedicated render tests (normal/full-width +/- bg image) |
| `MjWrapper` | **Good** | 3 dedicated render tests |
| `MjColumn` | **Good** | 3 dedicated width tests + golden files |
| `MjGroup` | **Minimal** | 1 test in AllComponentsRenderTest + golden file |
| `AbstractSectionComponent` | **None** | Shared base class, no direct tests |

### Content Components

| Class | Coverage | Notes |
|-------|----------|-------|
| `MjText` | **Minimal** | Ubiquitous in integration tests; no isolated attribute tests |
| `MjImage` | **Minimal** | 2 tests (basic + href) in AllComponentsRenderTest |
| `MjButton` | **Minimal** | 2 tests in AllComponentsRenderTest |
| `MjDivider` | **Minimal** | 1 test in AllComponentsRenderTest + golden |
| `MjSpacer` | **Minimal** | 1 test in AllComponentsRenderTest + golden |
| `MjTable` | **Minimal** | 1 test in AllComponentsRenderTest + golden |
| `MjRaw` | **Minimal** | 1 test in AllComponentsRenderTest + golden |

### Interactive Components

| Class | Coverage | Notes |
|-------|----------|-------|
| `MjHero` | **Minimal** | 1 test in AllComponentsRenderTest + golden |
| `MjAccordion` | **Minimal** | 1 test in AllComponentsRenderTest + golden |
| `MjAccordionElement` | **None** | Only tested through accordion parent |
| `MjAccordionTitle` | **None** | Only tested through accordion parent |
| `MjAccordionText` | **None** | Only tested through accordion parent |
| `MjCarousel` | **Minimal** | 1 test in AllComponentsRenderTest + golden |
| `MjCarouselImage` | **None** | Only tested through carousel parent |
| `MjNavbar` | **Minimal** | 1 test in AllComponentsRenderTest + golden |
| `MjNavbarLink` | **None** | Only tested through navbar parent |
| `MjSocial` | **Minimal** | 1 test in AllComponentsRenderTest + golden |
| `MjSocialElement` | **None** | Only tested through social parent |

### Head Components

| Class | Coverage | Notes |
|-------|----------|-------|
| `MjHead` | **None** | No direct tests |
| `MjTitle` | **Minimal** | Tested via renderer |
| `MjPreview` | **Minimal** | Tested via renderer/edge cases |
| `MjFont` | **Minimal** | Tested via renderer/edge cases |
| `MjBreakpoint` | **Minimal** | 1 test in AllComponentsRenderTest |
| `MjStyle` | **Minimal** | 1 test in AllComponentsRenderTest + inline CSS integration |
| `MjAttributes` | **Minimal** | 2 tests in AllComponentsRenderTest |
| `MjHtmlAttributes` | **Good** | 5 dedicated tests in MjHtmlAttributesTest |

### Utility Classes

| Class | Coverage | Notes |
|-------|----------|-------|
| `CssUnitParser` | **Excellent** | 14 tests: pixels, percent, shorthand, formatting, parsePx |
| `CssBoxModel` | **Good** | 4 tests: horizontal/vertical spacing, borders, nulls |
| `BackgroundPositionHelper` | **Good** | 12 tests: normalize, isXValue, isYValue |
| `VmlHelper` | **Good** | 9 tests: coordinates, formulas, axis conversion |
| `ColumnWidthCalculator` | **None** | No direct tests |
| `BackgroundCssHelper` | **None** | No direct tests |
| `SocialNetworkRegistry` | **None** | No direct tests |
| `HtmlEscaper` | **None** | No direct tests |

### Cross-Cutting Tests

| Test Class | Coverage | Notes |
|------------|----------|-------|
| `EdgeCaseTest` | **Good** | ~18 tests: null/empty/malformed, deep nesting, large templates, unicode |
| `SecurityTest` | **Good** | 8 tests: CDATA injection, input limits, nesting depth, sanitization, invalid attrs |
| `ThreadSafetyTest` | **Good** | 2 tests: concurrent rendering (8 threads x 10 iters), different configs (4 languages) |
| `CustomComponentTest` | **Good** | 3 tests: registration, defaults, coexistence with built-ins |
| `FileSystemIncludeResolverTest` | **Good** | 8 tests: resolve, subdirectories, path traversal, errors, integration |
| `GoldenFileTest` | **Excellent** | 33 dynamic tests with whitespace-normalized comparison |

---

## Missing Edge Cases by Area

### 1. Attribute Cascade (`AttributeResolver`) - P0

**No direct unit tests at all** for the 5-level cascade. This is the most critical gap because the cascade is central to rendering correctness.

Missing tests:
- Multiple mj-class values with conflicting attributes (which class wins?)
- mj-all defaults vs tag-specific defaults priority verification
- Inline attribute overriding mj-class value
- Empty/whitespace-only mj-class attribute value
- Attribute resolution for unknown tag names
- mj-class referencing non-existent class name
- Cascade with all 5 levels populated simultaneously

### 2. HTML Document Parser (`HtmlDocumentParser`) - P0

**No dedicated unit tests at all** for this custom HTML parser. It's security-sensitive (parses rendered output for CSS inlining and mj-html-attributes injection).

Missing tests:
- Malformed HTML (missing close tags, mismatched tags)
- Tags with unquoted/single-quoted attribute values
- Boolean attributes (e.g., `disabled`)
- MSO conditional comments (already handled but untested directly)
- Style block extraction (`extractStyles()`)
- Self-closing void elements
- CDATA sections, processing instructions
- Deeply nested HTML structures
- Tolerant parsing behavior (implicit close)

### 3. Utility Classes with Zero Tests - P1

**`HtmlEscaper`**: Security-critical utility. Should test null, empty, no-special-chars passthrough, each special character individually (&, ", <, >), mixed content.

**`SocialNetworkRegistry`**: Should test `getNetwork()` for all 18 networks, variant names (`facebook-noshare`), unknown names, null/empty. `getIconUrl()` for correct URL pattern, google-to-google-plus mapping.

**`DefaultFontRegistry`**: Should test auto-registration of known fonts (Open Sans, Roboto, etc.), unknown fonts ignored, URL override respected, no duplicate registration.

**`BackgroundCssHelper`**: Should test CSS background generation for various position/size/repeat combinations.

**`ColumnWidthCalculator`**: Should test width distribution across multiple columns, pixel vs percent vs auto widths, edge cases.

### 4. Preprocessor (`MjmlPreprocessor`) - P2

Missing edge cases:
- Nested CDATA markers (`]]>` inside content about to be CDATA-wrapped)
- `mj-text` with only whitespace content
- `mj-button` with deeply nested HTML (tables, divs)
- Content containing `<mj-text>` string literal
- Self-closing tags in `<mj-attributes>` context (must NOT get CDATA-wrapped)

### 5. CSS Inliner Edge Cases - P2

Missing tests:
- Malformed CSS (unclosed braces, missing colons, missing values)
- CSS with `calc()` expressions
- `@charset` and `@font-face` at-rule preservation
- Very long CSS property values
- Empty selector (e.g., `{ color: red; }`)
- Multiple `<style>` blocks in one document
- CSS with escaped characters

### 6. Component-Specific Edge Cases - P2

**MjText**: Empty content, whitespace-only content, all alignment options, container-background-color

**MjImage**: Missing `src`, missing `alt`, `fluid-on-mobile`, zero dimensions, `srcset`/`sizes`

**MjButton**: Missing `href`, `inner-padding`, `border` shorthand, `text-transform`

**MjDivider**: `align` left/center/right margin differences, dashed/dotted borders, zero width

**MjSection**: `direction="rtl"`, `border-radius`, 4+ columns, padding+border gutter interaction

**MjColumn**: `inner-border-radius` with `border-collapse:separate`, width exceeding 100%, negative padding

**MjHero**: `mode="fluid-height"`, no background image, multiple child components

**MjAccordion**: Single element, many elements (10+), custom border/icon

**MjCarousel**: Single image, many images (10+), custom thumbnails

**MjNavbar**: `hamburger` mode, custom icons, many links

**MjSocial**: Unknown network name, `-noshare` variant, custom icon `src`, horizontal vs vertical mode

### 7. Include Processing - P2

Missing:
- Nested includes (A includes B which includes C, non-circular)
- Include of empty file
- Include with `css-inline="inline"` type
- Include depth limit for deeply nested (non-circular) includes

### 8. HtmlSkeleton - P3

Missing:
- `escapeHtml()` with all special chars
- `reformatCss()` with deeply nested CSS
- Skeleton with many media queries
- Head comments with special characters

---

## Suggested Test Cases (Priority Ranked)

### P0 - Critical (should exist before any release)

1. **`AttributeResolverTest`** - Direct unit tests for 5-level cascade
   - `inlineAttributeWins()` - inline > all other levels
   - `mjClassOverridesTagDefaults()` - level 2 > level 3
   - `tagDefaultsOverrideMjAll()` - level 3 > level 4
   - `mjAllOverridesComponentDefaults()` - level 4 > level 5
   - `componentDefaultsFallback()` - fallback to hardcoded defaults
   - `multipleMjClassesFirstWins()` - first matching class value
   - `returnsNullWhenNotFoundAtAnyLevel()`

2. **`HtmlDocumentParserTest`** - Unit tests for the custom HTML parser
   - `parsesSimpleDocument()` - basic tree structure
   - `parsesSelfClosingVoidElements()` - img, br, hr
   - `parsesAttributesCorrectly()` - quoted, single-quoted, unquoted
   - `skipsMsoConditionalComments()` - `<!--[if mso]>...<![endif]-->`
   - `skipsStyleBlockContent()` - doesn't parse `<style>` innards as HTML
   - `extractStylesRemovesStyleBlocks()` - extracts CSS, removes blocks
   - `toleratesMalformedHtml()` - mismatched tags don't crash

3. **`HtmlEscaperTest`** - Security-critical utility
   - `escapeNull()`, `escapeEmpty()`, `escapeNoSpecialChars()`
   - `escapeAmpersand()`, `escapeQuotes()`, `escapeAngleBrackets()`
   - `escapeMixedContent()`

### P1 - High Priority

4. **`SocialNetworkRegistryTest`**
   - `getNetworkReturnsInfoForAllKnownNetworks()` - all 18 networks
   - `getNetworkHandlesVariantSuffix()` - `facebook-noshare` -> facebook
   - `getNetworkReturnsNullForUnknown()`
   - `getIconUrlGeneratesCorrectPattern()`
   - `getIconUrlGoogleUsesGooglePlus()`

5. **`DefaultFontRegistryTest`**
   - `registersOpenSansWhenUsed()`
   - `registersRobotoWhenUsed()`
   - `ignoresUnknownFont()`
   - `respectsUrlOverride()`
   - `doesNotDuplicateRegistration()`

6. **Individual component attribute tests** - Parameterized tests for each content component verifying specific attribute outputs (alignment, color, padding, font-family)

### P2 - Medium Priority

7. **`GlobalContextTest`** - Verify state management, merging, null safety
8. **`BackgroundCssHelperTest`** - Background CSS for position/size/repeat combinations
9. **`ColumnWidthCalculatorTest`** - Width distribution: auto, pixel, percent, mixed
10. **Negative/error tests** - Components in wrong contexts, invalid nesting, malformed CSS in mj-style, duplicate head components

### P3 - Nice to Have

11. Additional golden file tests: mj-preview, mj-font, mj-style inline, mj-include, deeply nested structures
12. Performance regression tests with time thresholds
13. Property-based testing: random valid MJML should never throw unhandled exceptions
14. Code coverage tooling (JaCoCo) to quantify and track metrics

---

## Golden File Test Assessment

The 33 golden file tests provide excellent regression coverage:

| Category | Golden Tests | Components Covered |
|----------|-------------|-------------------|
| Layout | `simple-text`, `two-columns`, `multi-column-layout`, `full-width-section`, `nested-sections`, `empty-sections`, `column-group`, `column-inner-styles`, `column-vertical-align`, `custom-width`, `direction-rtl` | section, column, group, wrapper |
| Content | `all-content-components`, `text-formatting`, `image-variants`, `button-variants`, `divider-variants`, `spacer-variants`, `table-advanced`, `raw-content`, `html-entities` | All 7 content components |
| Interactive | `hero-component`, `accordion-component`, `navbar-component`, `social-component`, `carousel-component` | All 5 interactive components |
| Styling | `head-attributes`, `head-with-title`, `background-images`, `padding-border`, `css-class-cascade`, `multiple-mj-class`, `section-background-variants`, `wrapper-advanced` | Cascade, backgrounds, VML |

**Missing golden test scenarios:**
- `mj-preview` (preview text rendering)
- `mj-font` (custom web font imports)
- `mj-style inline="inline"` (CSS inlining)
- `mj-include` (requires resolver, but could test with fixture)
- Complex nested structures (wrapper > section > group > column > accordion)
- Template combining all head components

---

## Summary Statistics

| Metric | Count |
|--------|-------|
| Total source files | 71 |
| Total test files | 24 |
| Source files with **good+** dedicated tests | ~18 (25%) |
| Source files with **minimal** (indirect-only) coverage | ~28 (39%) |
| Source files with **no** test coverage at all | ~25 (35%) |
| Classes with zero direct or indirect coverage | ~8 (utilities + infrastructure) |
| Golden file tests | 33 |
| Approximate unit + integration tests | ~163 |

---

## Top 7 Recommendations

1. **Add `AttributeResolverTest`** - The 5-level cascade is central to correctness and has zero direct tests. Most important single test file to add.

2. **Add `HtmlDocumentParserTest`** - This custom HTML parser is security-sensitive (used for CSS inlining and mj-html-attributes injection) and has zero direct tests.

3. **Add `HtmlEscaperTest`** - Security-critical utility for attribute escaping with zero tests. Simple to write, high value.

4. **Add `SocialNetworkRegistryTest` and `DefaultFontRegistryTest`** - Simple data-driven tests for currently untested utility classes.

5. **Expand component tests** - Each component should have at least one test verifying its specific attribute handling (alignment, padding, color) rather than just "does it render without errors."

6. **Add negative tests** - Components in wrong contexts, invalid attribute combinations, boundary conditions (zero-width, negative padding, width >100%).

7. **Add JaCoCo** for code coverage metrics - Currently no way to quantify line/branch coverage. JaCoCo integration would help track coverage improvements and identify untested branches.
