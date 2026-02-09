# Code Quality Review: mjml-java

**Reviewer:** code-quality-agent (thorough re-review)
**Date:** 2026-02-08
**Scope:** All source files in `src/main/java/dev/jcputney/mjml/` (~71 files)
**Severity Scale:** Critical > High > Medium > Low > Info

---

## Executive Summary

The mjml-java codebase demonstrates strong overall code quality for a Java 17 project. It makes excellent use of modern Java features (sealed classes, records, enhanced switch, text blocks, pattern matching), follows consistent naming conventions, and maintains clean separation of concerns. The public API surface is well-designed with comprehensive Javadoc and security-conscious parsing (XXE disabled, path traversal guards, CDATA injection escaping).

Key strengths: immutable data model types (records), defensive copies on collection getters, a well-structured sealed component hierarchy, pre-compiled regex patterns, and a clean builder-based configuration API.

Areas for improvement include: code duplication between CssInliner methods, inconsistent null-checking patterns across components, a few missing HTML attribute escapes in rendered output, some swallowed exceptions during font scanning, and the `GlobalContext` class acting as a catch-all for document state (which the code's own Javadoc already identifies for future decomposition).

**Overall Assessment: B+ / Good — solid codebase with minor issues, no critical defects.**

---

## 1. Naming Conventions

**Rating: Strong**

### Positive Patterns
- Class names follow Java conventions consistently (`MjmlRenderer`, `CssInliner`, `HtmlDocumentParser`)
- Component classes match their MJML tags (`MjText` → `<mj-text>`, `MjSection` → `<mj-section>`)
- Record names are descriptive (`FontDef`, `MediaQuery`, `StyleChange`, `ParsedRule`, `NetworkInfo`)
- Utility classes use standard naming (`CssUnitParser`, `CssBoxModel`, `HtmlEscaper`)
- Constants use `UPPER_SNAKE_CASE` consistently throughout
- Package organization is clean: `component/body/`, `component/content/`, `component/head/`, `component/interactive/`

### Findings

| # | Severity | File | Lines | Finding |
|---|----------|------|-------|---------|
| N1 | Low | `CssUnitParser.java` | 16 | `NON_NUMERIC` pattern name is vague — it's used specifically to strip non-numeric characters from pixel values. A name like `PX_STRIP_PATTERN` would be clearer. |
| N2 | Low | `RenderPipeline.java` | 161, 371-372 | `dummyContext` and `dummyGlobalContext` are informal names for contexts used during head processing and font scanning. Consider `headRenderContext` / `fontScanContext`. |
| N3 | Info | `MjNavbar.java` | 23-39 | Attribute names like `ico-close`, `ico-open` are abbreviated. These match the official MJML spec, so this is intentional and correct. |

---

## 2. Error Handling

**Rating: Good**

### Positive Patterns
- `MjmlException` (unchecked) is used consistently for all parsing/rendering errors
- `MjmlParser.parseXml()` (lines 62-66) correctly re-throws `MjmlException` and wraps other exceptions with context messages
- `FileSystemIncludeResolver.resolve()` wraps `IOException` in `MjmlException` with the original path for context
- `IncludeProcessor` enforces depth limits (line 59) and cycle detection (line 70-71) with clear error messages
- `MjmlParser.parse()` validates null/blank input immediately (line 33-35)
- `MjmlParser` validates root element tag name (line 55-57)
- `ComponentRegistry.register()` throws `IllegalStateException` when frozen (lines 27-30)

### Findings

| # | Severity | File | Lines | Finding |
|---|----------|------|-------|---------|
| E1 | Medium | `RenderPipeline.java` | 373-379 | `getComponentDefaults()` catches `Exception` broadly (line 377) and silently returns `Map.of()`. This swallows potentially important errors during font scanning. At minimum, a `LOG.fine()` call should record the caught exception for debuggability. |
| E2 | Low | `ComponentRegistry.java` | 45-53 | `createComponent()` returns `null` for unknown tags with a `LOG.warning()`. Callers then do `instanceof BodyComponent` checks that silently skip null. This is correct behavior for tolerant parsing, but the implicit null handling across multiple call sites is fragile — a new caller might forget the null check. |
| E3 | Low | `CssUnitParser.java` | 154-160 | `parseNumber()` silently returns 0 on `NumberFormatException`. Reasonable for CSS value parsing (invalid values should degrade gracefully), but no logging makes it hard to diagnose template issues. |
| E4 | Low | `CssUnitParser.java` | 104-113 | `parseIntPx()` strips ALL non-numeric/non-minus chars via regex. Input like `"10.5px"` becomes `"105"` (the decimal point is stripped). This is an edge case but could produce wrong values. |
| E5 | Low | `HtmlDocumentParser.java` | 260-269 | `popUntilTag()` pops non-matching elements on its way to the target tag (tolerant parsing). Mismatched HTML silently loses elements from the stack. Logging at FINE level would aid debugging. |

---

## 3. Null Safety

**Rating: Adequate — inconsistent patterns**

### Positive Patterns
- `GlobalContext` setters guard against null (`setTitle()`, `setPreviewText()`, `setBodyBackgroundColor()` all normalize null to `""`)
- `MjmlNode.setTextContent()` normalizes null to `""` (line 91)
- `AttributeResolver` clearly documents its null return (line 26: "or null if not found at any level")
- `MjmlNode.getAttributes()` returns unmodifiable map (line 46)

### Findings

| # | Severity | File | Lines | Finding |
|---|----------|------|-------|---------|
| NL1 | Medium | `MjmlPreprocessor.java` | 62-63 | `preprocess()` returns `null` if input is null (`return mjml`). But `MjmlParser.parse()` throws on null input. This is inconsistent — if `preprocess()` were called directly, null would flow through silently. |
| NL2 | Medium | `BaseComponent.java` | 39-41 | `getAttribute(name)` can return `null` when no value is found at any cascade level. Callers inconsistently handle this — some use `getAttribute(name, "")` for safety, others call `getAttribute(name)` and check for null after. Standardizing on the 2-arg form with explicit defaults would be more robust. |
| NL3 | Medium | Multiple components | - | Inconsistent null/empty-check idiom: some locations use `getAttribute("background-color")` then check `!= null && !isEmpty()`, while others use `getAttribute("background-color", "")` then check `!isEmpty()`. The second pattern is cleaner. Found in: `MjSection.java` (lines 77-78), `MjWrapper.java` (lines 70-71), `AbstractSectionComponent.java` (lines 96-97, 115-116). |
| NL4 | Low | `MjmlConfiguration.java` | 83-125 | Builder doesn't validate inputs. `language(null)` would flow through (handled defensively in `HtmlSkeleton`), `maxInputSize(-1)` would cause unexpected behavior, `maxNestingDepth(0)` would prevent any parsing. |
| NL5 | Low | `MjBody.java` | 65-68 | `globalContext.getTitle()` result checked for null, but `getTitle()` initializes to `""` and `setTitle()` guards null. The null check is redundant (harmless). |

---

## 4. Java 17 Features Usage

**Rating: Excellent**

### Good Usage

| Feature | Locations | Assessment |
|---------|-----------|------------|
| **Sealed classes** | `BaseComponent` sealed to `BodyComponent \| HeadComponent` | Compile-time hierarchy control |
| **Sealed interfaces** | `CssSelector` with rich sealed hierarchy of records | Excellent type-safe selector model |
| **Records** | `MjmlRenderResult`, `CssBoxModel`, `FontDef`, `MediaQuery`, `CssRule`, `CssDeclaration`, `CssSpecificity`, `CssSelector.*`, `StyleChange`, `StyleExtractionResult`, `ParseResult`, `NetworkInfo` | Pervasive use for immutable data |
| **Local records** | `CssInliner.inline()` — `ParsedRule`, `AppliedStyle` | Great use of method-scoped types |
| **Enhanced switch** | `MjmlParser` (line 93), `HtmlEscaper` (line 35), `CssSelector.ComplexSelector` (line 76), `CssUnitParser` (line 73), `IncludeProcessor` (line 81) | Consistently used |
| **Text blocks** | `HtmlSkeleton` (line 165), `MjAccordion` (line 33), `MjNavbar` (line 190) | Used for multi-line CSS/HTML |
| **Pattern matching instanceof** | Throughout body components | `component instanceof BodyComponent bodyComponent` used consistently |
| **`Map.of()` / `Map.ofEntries()`** | All component DEFAULTS maps | Truly immutable default attribute maps |
| **`Set.of()`** | `ENDING_TAGS`, `VOID_ELEMENTS`, `RAW_TEXT_ELEMENTS` | Immutable constant sets |

### Findings

| # | Severity | File | Lines | Finding |
|---|----------|------|-------|---------|
| J1 | Low | `RenderContext.java` | 10-38 | Good candidate for a record with `with*` copy methods. Currently a class with a 9-parameter private constructor. A record would make immutability explicit. However, the complexity is borderline — the current approach is reasonable. |
| J2 | Low | `MjmlDocument.java` | 7-32 | Could be a record: `record MjmlDocument(MjmlNode root)` with `getHead()` and `getBody()` as regular methods. Minor improvement. |

---

## 5. Code Duplication

**Rating: Adequate — notable duplication in CSS inliner and section/wrapper**

### Positive Patterns
- `AbstractSectionComponent` successfully deduplicates shared logic between `MjSection` and `MjWrapper` (background handling, style building, box model, position resolution)
- `BodyComponent` provides reusable utilities (`buildStyle`, `buildAttributes`, `orderedMap`, `addBorderStyles`, `addIfPresent`)
- `CssUnitParser` centralizes all CSS value parsing

### Findings

| # | Severity | File | Lines | Finding |
|---|----------|------|-------|---------|
| D1 | Medium | `CssInliner.java` | 54-170, 182-257 | `inline()` and `inlineAdditionalOnly()` share ~50 lines of near-identical logic: parse selectors, filter pseudo rules, match elements, sort by specificity, merge styles. The local records `ParsedRule` and `AppliedStyle` are redeclared identically in both methods. A private `matchAndApplyStyles(List<CssRule>, HtmlElement root)` helper would eliminate this duplication. |
| D2 | Low | `MjSection.java` / `MjWrapper.java` | 354-386 / 260-278 | `buildInnerTdStyle()` exists in both classes with slightly different implementations (Section handles individual padding-* overrides; Wrapper doesn't). Could be unified in `AbstractSectionComponent`. |
| D3 | Low | `StyleAttribute.java` / `CssParser.java` | 105-139 / 184-217 | Both contain nearly identical semicolon-splitting logic handling quotes and parentheses. Could extract a shared splitter utility. |
| D4 | Low | Multiple components | - | Default font family `"Ubuntu, Helvetica, Arial, sans-serif"` repeated in 8+ DEFAULTS maps. Could be a shared constant in `BodyComponent`, but this matches the MJML spec approach of independent per-component defaults. |

---

## 6. Encapsulation & Visibility

**Rating: Good**

### Positive Patterns
- Utility classes are `final` with private constructors: `MjmlParser`, `MjmlPreprocessor`, `EntityTable`, `CssInliner`, `HtmlEscaper`, `AttributeResolver`, `HtmlSkeleton`, `CssParser`, `CssSelectorParser`
- `MjmlRenderer` is `final` with private constructor (pure static API)
- `MjmlConfiguration` uses builder pattern with private constructor
- `BaseComponent` is `sealed` — type hierarchy is controlled
- All collection getters in `GlobalContext` return `Collections.unmodifiable*()` wrappers

### Findings

| # | Severity | File | Lines | Finding |
|---|----------|------|-------|---------|
| V1 | Low | `MjAccordion.java` | 18, 33 | `DEFAULTS` and `ACCORDION_CSS` are package-private (no access modifier). They appear to only be used within the class — should be `private`. |
| V2 | Low | `CssParser.java` | 178 | `parseDeclarations()` is package-private. Should be `private` unless used by tests. |
| V3 | Low | `RenderPipeline.java` | 74 | `RenderPipeline` is `public` but not `final`. Could be subclassed unintentionally. Consider `final`. |
| V4 | Low | `MjmlNode.java` | 13 | Fully mutable public class (`setAttribute()`, `addChild()`, `replaceWith()`, `setTextContent()`). Mutation is necessary for the parser and include processor, but downstream consumers could accidentally mutate the tree. A read-only interface would improve safety. |
| V5 | Low | `module-info.java` | 1-11 | Module exports all packages including implementation details like `parser`, `context`, `util`. The `component` package is needed for custom components, but `parser` and `context` could be restricted. However, users may want `GlobalContext` access for advanced use cases. |

---

## 7. Output Escaping / XSS Safety

**Rating: Adequate — gaps when sanitizeOutput is off (default)**

### Positive Patterns
- `buildAttributes()` in `BodyComponent` (line 70-81) supports `HtmlEscaper.escapeAttributeValue()` when `sanitizeOutput` is enabled
- `HtmlSkeleton.escapeHtml()` is used for title and preview text
- `HtmlSkeleton` strips `--` from head comments to prevent comment injection (line 112)

### Findings

| # | Severity | File | Lines | Finding |
|---|----------|------|-------|---------|
| XSS1 | Medium | `MjBody.java` | 66-68 | `title` from `globalContext.getTitle()` is rendered into `aria-label` without HTML attribute escaping. A title containing `"` would break the HTML attribute. |
| XSS2 | Medium | `MjImage.java` | 78-98 | `alt`, `src`, `title`, `href`, `srcset`, `sizes` attribute values rendered without escaping. The `buildAttributes()` helper supports sanitization, but image attributes are built inline with direct `sb.append()`. |
| XSS3 | Medium | `MjButton.java` | 136-142 | `href`, `rel` rendered without escaping. Same pattern as MjImage. |
| XSS4 | Info | General | - | When `sanitizeOutput=false` (default), this matches MJML v4 behavior which also doesn't escape. MJML templates are typically trusted input. However, the `sanitizeOutput` flag only applies to `buildAttributes()` calls, not to direct `sb.append()` constructions — so even with `sanitizeOutput=true`, some attributes remain unescaped. |

---

## 8. Thread Safety

**Rating: Good**

### Positive Patterns
- `MjmlRenderer` is stateless (static methods only) — inherently thread-safe
- Javadoc explicitly documents thread safety: `MjmlRenderer` (lines 8-11), `GlobalContext` (lines 33-35)
- `RenderPipeline` creates its own `GlobalContext` per render — no shared mutable state
- `RenderContext` uses `AtomicInteger` for unique ID generation (line 20) — correctly shared across child contexts
- `MjmlConfiguration` is effectively immutable after construction
- Default attribute maps are truly immutable (`Map.of()`)
- `RenderPipeline.defaultsCache` uses `ConcurrentHashMap` (line 367) — correct concurrent map type

### Findings

| # | Severity | File | Lines | Finding |
|---|----------|------|-------|---------|
| T1 | Low | `ComponentRegistry.java` | 19 | `frozen` flag is a plain `boolean` (not `volatile`). If a registry were shared across threads, the freeze might not be visible. Low risk since registries are created per-pipeline. |
| T2 | Info | `RenderContext.java` | 20 | `AtomicInteger` for ID counter is slightly over-engineered for single-threaded rendering, but causes no harm and provides correct guarantees if the pipeline ever becomes concurrent. |

---

## 9. Resource Management

**Rating: Good — minimal resource usage**

### Positive Patterns
- All processing is in-memory (StringBuilders, DOM trees)
- `FileSystemIncludeResolver` uses `Files.readString()` which handles resource cleanup internally
- No database connections, network calls, or thread pools

### Findings

| # | Severity | File | Lines | Finding |
|---|----------|------|-------|---------|
| R1 | Low | `MjmlParser.java` | 52 | `StringReader` used with `InputSource` is not explicitly closed. `StringReader` doesn't hold external resources so this is technically safe, but `try-with-resources` would be cleaner. |

---

## 10. Logging

**Rating: Good — uses java.util.logging consistently**

### Positive Patterns
- `java.util.logging.Logger` used in: `ComponentRegistry`, `MjmlPreprocessor`, `RenderPipeline`, `CssInliner`, `CssUnitParser`, `IncludeProcessor`
- Lazy message construction with lambdas: `LOG.fine(() -> "message " + value)` (e.g., `MjmlPreprocessor` line 132, `CssInliner` lines 85, 102)
- `FINE` level used appropriately for debug-level messages

### Findings

| # | Severity | File | Lines | Finding |
|---|----------|------|-------|---------|
| L1 | Medium | `RenderPipeline.java` | 377 | `catch (Exception e)` silently returns `Map.of()` with no logging in `getComponentDefaults()`. Should at minimum `LOG.fine(() -> "Failed to get defaults for " + tag + ": " + e.getMessage())`. |
| L2 | Low | `HtmlDocumentParser.java` | 260-269 | `popUntilTag()` tolerantly pops mismatched elements with no logging. A FINE-level log would help debug malformed HTML issues. |

---

## 11. Collection Usage & Immutability

**Rating: Strong**

### Positive Patterns
- Default attribute maps use `Map.of()` / `Map.ofEntries()` — truly immutable
- `GlobalContext` getters return `Collections.unmodifiable*()` wrappers for all list/set/map fields
- `MjmlNode.getAttributes()` returns unmodifiable map (line 46)
- `MjmlNode.getChildren()` uses cached unmodifiable wrapper with invalidation on mutation (lines 49-54, 56-59)
- `MjmlConfiguration.customComponents` uses `Map.copyOf()` (line 29) — deep defensive copy
- `HtmlElement` constructor takes defensive copy of attributes map (line 33)
- `SocialNetworkRegistry.NETWORKS` wrapped in `Collections.unmodifiableMap()` (line 95)

### Findings

| # | Severity | File | Lines | Finding |
|---|----------|------|-------|---------|
| C1 | Info | `EntityTable.java` | 13, 156 | `ENTITIES` and `ENTITY_BY_NAME` use `HashMap` but are write-once in `static {}` blocks. Could use `Map.of()` for true immutability, but the 150+ entries exceed `Map.of()` limits. Current approach is fine. |
| C2 | Info | `GlobalContext.java` | 50 | `mediaQueries` typed as `LinkedHashSet<MediaQuery>` (concrete type) rather than `Set<MediaQuery>`. Minor field declaration preference. |

---

## 12. Documentation

**Rating: Good**

### Positive Patterns
- Public API classes have comprehensive Javadoc with usage examples: `MjmlRenderer`, `MjmlConfiguration`, `CssInliner`, `IncludeResolver`, `FileSystemIncludeResolver`
- Thread safety explicitly documented on `MjmlRenderer` (lines 8-11) and `GlobalContext` (lines 33-35)
- `GlobalContext` includes future decomposition plan in Javadoc (lines 19-31)
- `AttributeResolver` documents the 5-level cascade (lines 7-14)
- `RenderPipeline` documents the 7-phase pipeline (lines 62-72)
- `IncludeResolver` includes SSRF security warning (lines 7-10)
- `CssSelector` hierarchy is well-documented (lines 6-18)
- All component classes have class-level Javadoc explaining purpose

### Findings

| # | Severity | File | Lines | Finding |
|---|----------|------|-------|---------|
| DOC1 | Low | `BodyComponent.java` | 107-115 | `orderedMap()` doesn't document that it expects key-value pairs and silently drops the last element if an odd number of arguments is passed. |
| DOC2 | Low | `RenderContext.java` | 10-38 | Fields like `insideWrapper`, `insideGroup`, `columnWidthSpec` have non-obvious semantics explained only through usage context in other files. |

---

## 13. Method Complexity

### Findings

| # | Severity | File | Lines | Finding |
|---|----------|------|-------|---------|
| MC1 | Medium | `CssInliner.java` | 54-170 | `inline()` is 116 lines with 7 numbered phases. Well-commented but long. The inner matching loop (lines 114-151) could be extracted. |
| MC2 | Low | `MjSection.java` | 74-138 | `renderNormal()` is 64 lines with conditional branches for background URL handling. At the upper limit of comfortable readability. |
| MC3 | Low | `MjColumn.java` | 64-136 | `render()` is 72 lines with branching for gutter/no-gutter. Manageable. |
| MC4 | Info | `HtmlDocumentParser.java` | 43-185 | `parse()` is 142 lines but is inherently a state machine — difficult to decompose without losing clarity. The skip/comment/closing/opening sections are each well-bounded. |

---

## 14. Miscellaneous

| # | Severity | File | Lines | Finding |
|---|----------|------|-------|---------|
| M1 | Low | `MjAccordion.java` | 122 | Checking `globalContext.getStyles().contains(ACCORDION_CSS)` does a linear scan. The style list is small (<10), so performance is fine, but a boolean flag would be more idiomatic. |
| M2 | Low | `DefaultFontRegistry.java` | 45, 59 | `ctx.getFonts().stream().anyMatch(...)` is called for each font name during recursive tree scanning. Fonts set is typically <5 elements, so fine, but could cache a `Set<String>` of registered names. |
| M3 | Low | `MjmlConfiguration.java` | 83-125 | Builder doesn't validate `maxInputSize > 0` or `maxNestingDepth > 0`. Negative values would cause unexpected behavior. |
| M4 | Low | `MjText.java` | 10 | Unused import: `java.util.regex.Pattern` is imported but never used. |
| M5 | Info | `HtmlSkeleton.java` | 22 | `new StringBuilder(32768)` — magic initial capacity. Could be a named constant, but the value is a reasonable pre-allocation for HTML documents and is self-explanatory. |

---

## Summary of Findings by Severity

| Severity | Count | Key Issues |
|----------|-------|------------|
| **Critical** | 0 | None |
| **Medium** | 8 | E1 (swallowed exception in font scanning), NL1-NL3 (null inconsistency), D1 (CSS inliner duplication), XSS1-XSS3 (unescaped attributes), L1 (missing logging) |
| **Low** | 22 | Naming nits, visibility, encapsulation, documentation gaps, input validation, resource management, complexity |
| **Info** | 8 | Observations, positive patterns, minor style preferences |

---

## Top 5 Recommended Improvements

1. **Fix unescaped attribute values in component render methods.** `MjBody.java:67` (aria-label), `MjImage.java:78+` (src, alt, title, href), and `MjButton.java:136+` (href) render user-provided values without HTML attribute escaping. When `sanitizeOutput` is enabled, these should use `HtmlEscaper.escapeAttributeValue()` consistently. Impact: fixes incomplete sanitization.

2. **Extract shared CSS matching/merging logic in CssInliner.** `inline()` and `inlineAdditionalOnly()` share ~50 lines of identical selector parsing, matching, sorting, and merging code. Extract a `matchAndApplyStyles()` private helper. Impact: eliminates meaningful duplication and reduces maintenance risk.

3. **Log the swallowed exception in `RenderPipeline.getComponentDefaults()`.** Line 377 catches all exceptions silently. Add `LOG.fine(() -> "..." + e.getMessage())` at minimum. Impact: prevents silent debugging nightmares.

4. **Standardize null-handling in component attribute access.** Adopt a consistent pattern across all components: use `getAttribute(name, "")` instead of `getAttribute(name)` + null check. This is already the dominant pattern — the remaining inconsistencies in `MjSection`, `MjWrapper`, and `AbstractSectionComponent` just need cleanup. Impact: reduces cognitive load and prevents NPE risks.

5. **Validate builder inputs in `MjmlConfiguration.Builder`.** Add guards for `maxInputSize > 0` and `maxNestingDepth > 0`. Impact: fail-fast on invalid configuration.

---

## Notable Positive Patterns Worth Preserving

1. **Sealed class hierarchy** — `BaseComponent permits BodyComponent, HeadComponent` provides compile-time guarantees about component types
2. **Pervasive records** — Used for DTOs, CSS model types, local method types. Eliminates boilerplate and enforces immutability
3. **Local records in methods** — `CssInliner` uses `ParsedRule` and `AppliedStyle` as local records, keeping scope tight
4. **Security-conscious parsing** — XXE disabled in `MjmlParser`, path traversal prevention in `FileSystemIncludeResolver`, CDATA injection escaping in `MjmlPreprocessor`, comment injection prevention in `HtmlSkeleton`
5. **Defensive collection copies** — All collection getters in `GlobalContext`, `MjmlNode`, and `HtmlElement` return unmodifiable views
6. **Builder pattern** — `MjmlConfiguration` provides a clean, fluent builder API with sensible defaults
7. **Pre-compiled regex patterns** — `MjmlPreprocessor.TAG_PATTERNS`, `CssUnitParser.WHITESPACE` avoid recompilation
8. **Lazy logging with lambdas** — `LOG.fine(() -> "...")` avoids string concatenation when logging is disabled
