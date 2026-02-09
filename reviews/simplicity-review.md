# Code Simplicity Review: mjml-java

**Date:** 2026-02-08
**Scope:** All 71 source files (~10,300 LOC), reviewed after recent refactoring pass
**Focus:** Unnecessary complexity, over-engineering, simplification opportunities

---

## Executive Summary

**Overall Assessment: Impressively Simple for What It Does**

This codebase renders 33 MJML components with full CSS inlining, VML Outlook support, and golden-file compatibility in ~10,300 lines of Java 17. That is remarkably lean. The architecture is straightforward: a sealed hierarchy (`BaseComponent -> BodyComponent | HeadComponent`), a flat component registry, and a linear 7-phase pipeline. There is no Spring, no DI framework, no annotation processing, no reflection magic. Components are plain classes that take data in and return HTML strings out.

A recent refactoring pass has already addressed the most significant simplification opportunities: dead code removal, extraction of `AbstractSectionComponent` to deduplicate MjSection/MjWrapper, consolidation of utility methods into `CssUnitParser`, and removal of unused classes like `HtmlRenderer`. The remaining findings are minor.

**Simplicity Score: 8.5/10** — Well above average for a Java project of this scope. Most remaining complexity is justified by the problem domain.

---

## Findings: Areas That Could Be Simpler

### 1. CssInliner: Duplicated Match/Merge Logic

**File:** `css/CssInliner.java:54-169` (inline) and `182-257` (inlineAdditionalOnly)
**Severity:** Low-Medium | **Effort:** Small

The `inline()` and `inlineAdditionalOnly()` methods share ~60% of their logic. Both:
1. Parse CSS into rules
2. Separate inlineable from pseudo rules
3. Match rules against elements using the same algorithm
4. Sort by specificity and source order
5. Merge styles with existing inline styles

The inner `ParsedRule` and `AppliedStyle` local records are defined identically in both methods. The matching loop (lines 114-151 and 217-249) is nearly identical.

**How to simplify:** Extract the shared "match rules against elements and merge styles" core into a private `applyRulesToElements(List<ParsedRule>, List<HtmlElement>)` method. The two local record types could be promoted to file-level private records.

---

### 2. RenderContext "With" Method Proliferation

**File:** `context/RenderContext.java` (116 lines)
**Severity:** Low | **Effort:** Medium

RenderContext has 8 fields and 5 `with*()` factory methods, each calling the 9-parameter private constructor. The 9-parameter constructor has several adjacent parameters of the same type (`boolean, boolean, boolean, boolean`), making it easy to swap arguments accidentally.

Additionally, the `direction` field is set to `"ltr"` in the public constructor but has no getter and is never read by any code.

**How to simplify:**
- Remove the unused `direction` field (trivial)
- Consider a `toBuilder()` pattern for future safety, though the class is stable enough that the current approach works fine

---

### 3. `getComponentDefaults()` Creates Throwaway Objects for Font Scanning

**File:** `render/RenderPipeline.java:369-381`
**Severity:** Low | **Effort:** Small

To get default attributes for font scanning, `getComponentDefaults()` creates a dummy `MjmlNode`, dummy `GlobalContext`, and dummy `RenderContext`, instantiates a full component, calls `getDefaultAttributes()`, and catches exceptions. The defaults are static final `Map.ofEntries(...)` on each component class — they could be accessed without constructing objects.

The results are cached per tag name via `ConcurrentHashMap.computeIfAbsent()`, so this only runs once per unique tag. Still, it would be cleaner to register default attribute maps alongside component factories:
```java
reg.register("mj-text", MjText::new, MjText.DEFAULTS);
```

---

### 4. MjHero Fixed/Fluid Methods Share ~70% Structure

**File:** `interactive/MjHero.java:74-316`
**Severity:** Low-Medium | **Effort:** Medium

`renderFixedHeight()` (113 lines) and `renderFluidHeight()` (120 lines) produce nearly identical HTML. The shared structure: MSO wrapper table + v:image, outer div, table with vertical-align row, main content td with background styles, MSO inner table, hero content div, inner tables, children rows, closing tags. The differences are:
- Fixed: explicit height on td, no spacer tds
- Fluid: spacer tds with padding-bottom percentage, no explicit height, uses background-height

~80 lines of the HTML scaffolding is duplicated.

**How to simplify:** Extract the shared structure into a private method that accepts the variable parts (height attribute, spacer HTML, etc.) as parameters. This would cut ~60 lines while keeping the logic readable.

---

### 5. Thin Wrapper Methods (Multiple Files)

**Severity:** Trivial | **Effort:** Trivial

Several methods exist only to delegate with no added logic:
- `MjSection.formatPxWidth()` (line 388): wraps `CssUnitParser.formatPxWidth()`
- `MjHero.parseIntPx()` (line 370): wraps `CssUnitParser.parseIntPx()`
- `MjCarousel.parseIntFromPx()` (line 470): wraps `CssUnitParser.parseIntPx()`

These could be replaced with direct calls or static imports. Not worth a dedicated refactoring pass, but good to clean up opportunistically.

---

### 6. MjSection Has Three Render Variants

**File:** `body/MjSection.java:63-273` (renderNormal, renderInsideWrapper, renderFullWidth)
**Severity:** Low (already adequately factored)

Three rendering modes share the pattern of MSO table, optional VML, inner table, padding td, column children. Each variant has different MSO wrapping, nesting depths, and outer elements. The shared parts (buildMsoTableOpen, buildInnerTdStyle, etc.) are already extracted into helper methods.

**Assessment:** The existing factoring via `AbstractSectionComponent` and local helper methods is a reasonable middle ground. Further abstraction would likely make the code harder to follow without meaningful LOC reduction.

---

## Justified Complexity (Appropriately Complex)

### CSS Selector Engine (~730 lines across 3 files)
`CssSelector` (sealed interface with records), `CssSelectorParser` (recursive descent), `CssSelectorMatcher` (pattern matching). Each type is a simple data record, the parser is a clean state machine, the matcher uses instanceof chains. No visitor pattern, no AST framework. Minimal and correct.

### MjCarousel CSS Generation (~200 lines)
`buildCarouselCss()` generates complex sibling-combinator CSS rules that vary by image count. The loops with `siblingChain()` are inherently complex because pure-CSS state management via radio buttons requires N^2 selectors. Well-commented and follows official MJML structure exactly.

### HtmlDocumentParser (386 lines)
Custom lightweight HTML tokenizer needed because JDK's XML parser can't handle MSO conditionals (`<!--[if mso]>...`). The parser is intentionally minimal — not a full HTML5 parser, just enough for CSS inlining. Position-tracking for in-place style modification avoids re-serializing the entire document.

### VmlHelper (221 lines)
Section vs. wrapper VML uses different formulas (mandated by MJML source code). The `cssPositionToPercent` conversions mirror MJML JavaScript exactly. This complexity is dictated by Outlook's VML engine.

### AbstractSectionComponent (129 lines)
Clean extraction of shared logic between MjSection and MjWrapper. Background-image handling, box-model calculations, and style-building methods are factored out without over-abstracting. This is the right level of deduplication.

### MjmlPreprocessor (139 lines)
CDATA wrapping and entity replacement for XML-hostile MJML content. Handles nested CDATA, entity-free zones, and self-closing tags correctly. Precompiled patterns are a good performance choice.

### Sealed Component Hierarchy
`BaseComponent -> BodyComponent | HeadComponent`. No unnecessary intermediate classes, no generic type parameters, no visitor pattern. Components override `render()` or `process()`. This is as simple as a component system can be.

### ComponentRegistry + ComponentFactory
A map of tag names to factory lambdas. No reflection, no classpath scanning, no annotations. Explicit registration in `RenderPipeline.createRegistry()`. The `freeze()` method prevents post-setup modification. Minimal.

### AttributeResolver (68 lines)
The 5-level cascade (inline -> mj-class -> tag defaults -> mj-all -> component defaults) is a linear sequence of null checks. No strategy pattern, no chain of responsibility — just a static method with early returns.

---

## Summary Table

| # | Finding | Severity | Effort | Recommendation |
|---|---------|----------|--------|----------------|
| 1 | CssInliner duplicated match/merge | Low-Med | Small | Extract shared core method |
| 2 | RenderContext with-methods + unused field | Low | Small-Med | Remove `direction` field; consider builder for future |
| 3 | Throwaway objects for font defaults | Low | Small | Register defaults alongside factories |
| 4 | MjHero fixed/fluid duplication | Low-Med | Medium | Extract shared HTML scaffold |
| 5 | Thin wrapper methods | Trivial | Trivial | Replace with direct calls |
| 6 | MjSection three variants | Low | N/A | Already adequately factored |

**Total estimated simplifiable:** ~100-150 lines (~1-1.5% of codebase)

---

## What Changed Since Prior Review

The codebase has already undergone significant simplification:
- `HtmlRenderer.java` was deleted (was entirely unused)
- `AbstractSectionComponent` was extracted to deduplicate MjSection/MjWrapper
- Dead methods removed from `BodyComponent`, `AttributeResolver`, `SocialNetworkRegistry`, `MjmlNode`, `BaseComponent`, `ComponentRegistry`
- `parsePixels()` consolidated into `CssUnitParser` (was duplicated)
- `RenderContext` reduced from multiple public constructors to one public + one private
- `HtmlSkeleton.appendBaseStyles()` converted to text block
- Unused `direction` field, getter, and factory method removed from RenderContext

The prior review estimated ~480 removable lines. Roughly ~350 of those have been addressed. The remaining ~100-150 lines identified above are genuinely minor.

---

## Bottom Line

This codebase has remarkably little unnecessary complexity. The architecture is clean, the class hierarchy is minimal, no design patterns are used for their own sake, and the code reads well top-to-bottom. The remaining simplification opportunities (CssInliner dedup, MjHero dedup, throwaway defaults objects) are straightforward refactorings with low risk. No over-engineering, no god classes, no unnecessary abstractions.
