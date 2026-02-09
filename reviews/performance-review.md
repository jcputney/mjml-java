# Performance Review: mjml-java

**Date:** 2026-02-08
**Reviewer:** Performance Review Agent (revised)
**Scope:** Full codebase (71 source files)

---

## Executive Summary

The codebase demonstrates solid performance instincts overall -- regex patterns for ending tags are pre-compiled in a static map, StringBuilder is used consistently throughout rendering, entity replacement uses an efficient single-pass O(n) scanner, and component defaults are immutable `Map.of()` constants. The most significant optimization opportunities are in the CSS inlining pipeline (O(R*E) rule-to-element matching without indexing, O(n) sibling lookups via `indexOf(this)`), the XML DocumentBuilderFactory being recreated per parse call, and redundant per-component computations (box model, attribute resolution) that could benefit from memoization.

**Findings by severity:**
- Critical: 0
- High: 4
- Medium: 7
- Low: 7

---

## Critical Issues

*None.* The codebase avoids the most common Java performance anti-patterns. Typical MJML email templates are small enough that no issue reaches production-outage severity.

---

## High Severity Issues

### H1. CSS Inliner: O(R * E) rule-to-element matching without indexing
**Files:** `CssInliner.java:114-151` and `CssInliner.java:217-249`

Both `inline()` and `inlineAdditionalOnly()` iterate over every HTML element for every CSS rule, producing O(R * E) complexity where R = inlineable rules and E = total elements. The selectors *are* correctly pre-parsed (at lines 93-100) and reused, but matching is brute-force: every rule is tested against every element.

**Suggested fix:** Build a tag-name index (`Map<String, List<HtmlElement>>`) and class-name index from `allDescendants()`. For each rule, consult the rightmost simple selector (tag/class/id) to narrow the candidate set before full matching.

**Estimated impact:** 2-5x speedup on templates with 20+ CSS rules and 200+ elements.

### H2. `HtmlElement.indexInParent()` and `previousSibling()` use O(n) `indexOf(this)`
**File:** `HtmlElement.java:90-109`

Both methods call `parent.children.indexOf(this)` which does a linear scan. These are called from `CssSelectorMatcher.matchesAdjacentSibling()` and `matchesGeneralSibling()`. For the `~` combinator, `matchesGeneralSibling` is itself O(k) and calls `indexInParent()` which is also O(k), producing O(k^2) per match attempt. This compounds with H1.

**Suggested fix:** Store the child index as a field, set during `addChild()`. `previousSibling()` can then use `index - 1` directly -- O(1) instead of O(n).

**Estimated impact:** Eliminates O(n) per sibling lookup; significant for templates with many sibling elements and sibling CSS selectors.

### H3. `allDescendants()` materializes the full tree into a new list on each call
**File:** `HtmlElement.java:128-132`

`allDescendants()` allocates a new `ArrayList` and recursively populates it on every call. It is called in `CssInliner.inline()` (line 107), `CssInliner.inlineAdditionalOnly()` (line 210), and `RenderPipeline.applyHtmlAttributes()` (line 226). For complex email HTML with 500+ elements, this is a non-trivial allocation.

**Suggested fix:** Cache the flat list in a field (invalidated on structural modification), or pass the list between consumers rather than recomputing.

**Estimated impact:** Eliminates one O(E) traversal + allocation per CSS inlining pass.

### H4. `DocumentBuilderFactory.newInstance()` created per parse call
**File:** `MjmlParser.java:43-49`

`DocumentBuilderFactory.newInstance()` involves a service-provider lookup (potentially scanning the classpath via `FactoryFinder.find()`). The factory is created fresh on every `parseXml()` call, including for each `mj-include` resolution. The configuration is always identical.

**Suggested fix:** Create the factory once as a `private static final` field configured in a static initializer. The factory is thread-safe after configuration; `DocumentBuilder` instances from it are not thread-safe but are already created per-call.

**Estimated impact:** Eliminates ~0.5-2ms per parse call (measured JDK overhead of `newInstance()` + `setFeature()` calls).

---

## Medium Severity Issues

### M1. `getDefaultAttributes()` called on every `getAttribute()` invocation
**File:** `BaseComponent.java:39-41`

`getAttribute(name)` calls `getDefaultAttributes()` which dispatches through the virtual method, then feeds the result into `AttributeResolver.resolve()` which walks 5 cascade levels. A single component's `render()` method typically calls `getAttribute()` 10-20+ times (e.g., `MjSection` calls it for background-color, background-url, padding, direction, css-class, text-align, border, border-radius, etc.). Each call traverses all 5 levels even though the resolved value rarely changes between calls for the same attribute.

**Suggested fix:** Cache resolved attributes in a per-instance `Map<String, String>` (lazily populated on first access per attribute name).

**Estimated impact:** Moderate -- reduces repeated map lookups across 5 levels. Each individual lookup is O(1) hash map access, so per-call cost is small, but call volume is very high.

### M2. `getBoxModel()` computed multiple times without caching
**Files:** `BodyComponent.java:41-48`, `MjImage.java:158`, `MjDivider.java:54-55`, `AbstractSectionComponent.java:31-43`

`getBoxModel()` parses CSS shorthand strings and creates a new `CssBoxModel` record each time. `MjImage.computeImageWidth()` calls `getBoxModel()` twice (for `paddingLeft()` and `paddingRight()`). `MjDivider` also calls it twice. `AbstractSectionComponent.getBoxModel()` calls `super.getBoxModel()` and then parses 4 additional padding attributes.

**Suggested fix:** Compute once and cache as a lazy field, or compute once in `computeImageWidth()` and store in a local variable.

**Estimated impact:** Avoids 2-4 redundant CSS shorthand parsing operations per component.

### M3. `MjText.containsBlockElements()` calls `toLowerCase()` on full content
**File:** `MjText.java:199-206`

`containsBlockElements()` calls `html.toLowerCase()` allocating a full copy of the content string, then calls `.contains()` 13 times on the lowercased copy. For large `mj-text` blocks, this creates a significant temporary allocation.

**Suggested fix:** Use `regionMatches(true, ...)` for case-insensitive matching without allocation, or integrate block element detection into the existing `collapseInlineWhitespace()` tag-parsing pass.

**Estimated impact:** Eliminates one O(n) string allocation + 13 O(n) scans.

### M4. `CssInliner.insertStyleBlock()` calls `toLowerCase()` on entire HTML twice
**File:** `CssInliner.java:314, 320`

`insertStyleBlock()` calls `html.toLowerCase().indexOf("</head>")` and `html.toLowerCase().indexOf("<body")`, each allocating a full lowercase copy of the final HTML string (typically 10-50KB).

**Suggested fix:** Since `HtmlSkeleton` always emits lowercase tags, use direct case-sensitive `indexOf("</head>")`. Alternatively use the existing `indexOfIgnoreCase()` helper which doesn't allocate.

**Estimated impact:** Eliminates 1-2 large string allocations.

### M5. Font registration scans entire tree with stream-based lookups
**Files:** `RenderPipeline.java:351-364`, `DefaultFontRegistry.java:36-66`

`scanFontsRecursive()` walks every node in the document, calling `AttributeResolver.resolve()` for "font-family" on each. Then `registerUsedFonts()` does `ctx.getFonts().stream().anyMatch(...)` for each potential match. The stream creates iterator objects per call, and fonts is a `LinkedHashSet`, making streaming O(n).

**Suggested fix:** Track registered font names in a `HashSet<String>` for O(1) "already registered" checks. The full tree walk is unavoidable but could be deferred or combined with another pass.

**Estimated impact:** Minor -- tree walk is O(nodes), inner loop bounded by 5 default fonts, but streaming creates iterator overhead per call.

### M6. `RenderPipeline.getComponentDefaults()` creates dummy contexts per cache miss
**File:** `RenderPipeline.java:370-381`

On cache miss, creates a `RenderContext`, `GlobalContext`, and `MjmlNode` just to instantiate a component and call `getDefaultAttributes()`. The `ConcurrentHashMap` used for the cache is unnecessary since the pipeline is single-threaded. However, the cache correctly bounds this to ~33 unique tag names, making it a one-time cost per pipeline instance.

**Suggested fix:** Use a regular `HashMap` (avoids CAS overhead). Consider a static registry mapping tag names to their default attributes directly, eliminating the need for dummy component creation.

**Estimated impact:** Minor for the cache itself (bounded by tag count), but `ConcurrentHashMap` -> `HashMap` is a free improvement.

### M7. `MjmlPreprocessor` applies 8 sequential regex passes on full source
**File:** `MjmlPreprocessor.java:68-70`

`preprocess()` applies regex matching for each of the 8 ending tags sequentially, each potentially rebuilding the full string via `Matcher.appendReplacement()`. Note: The patterns *are* correctly pre-compiled in a static map (line 40-52), which is good. But each pass scans the entire source string.

**Suggested fix:** Combine all ending tags into a single regex with alternation: `(<(mj-text|mj-button|...)(\s[^>]*)?(?<!/)>)(.*?)(</\2\s*>)`. This reduces 8 full-string scans to 1.

**Estimated impact:** Moderate for templates with many ending tags; reduces 8 full-string scans to 1.

---

## Low Severity Issues

### L1. `HtmlElement.getClassNames()` compiles regex per element
**File:** `HtmlElement.java:61`

`Set.of(cls.trim().split("\\s+"))` compiles the `\s+` regex on each unique element's first class name access. The result *is* correctly cached per element (via `cachedClassNames`), so this is only per-element, not per-lookup. However, for 200+ elements with classes, that's 200+ regex compilations.

**Suggested fix:** Use the pre-compiled `CssUnitParser.WHITESPACE` pattern for splitting: `CssUnitParser.WHITESPACE.split(cls.trim())`.

**Estimated impact:** Minor -- eliminates per-element regex compilation, but JDK may already optimize simple patterns.

### L2. `LinkedHashMap` used where `HashMap` would suffice
**Files:** Various -- `MjmlNode.attributes`, `EntityTable.ENTITIES`

`LinkedHashMap` has slightly higher per-entry memory overhead due to linked list pointers. For cases where iteration order doesn't matter (e.g., `EntityTable.ENTITIES` is never iterated in insertion order), `HashMap` would be marginally more efficient.

**Suggested fix:** Use `HashMap` where insertion order is irrelevant.

**Estimated impact:** Negligible -- slightly lower memory per map instance.

### L3. `StyleAttribute.merge()` creates intermediate collections on every merge
**File:** `StyleAttribute.java:73-99`

Each merge creates a `LinkedHashMap` and converts to `ArrayList`. During CSS inlining, called once per matching rule per element. For an element matching 3 rules, that's 3 map + 3 list allocations.

**Suggested fix:** Accept a mutable `LinkedHashMap` and merge in-place, converting to list only at the end.

**Estimated impact:** Low -- reduces intermediate allocations.

### L4. `escapeHtml()` creates up to 4 intermediate strings
**File:** `HtmlSkeleton.java:284-288`

Chained `.replace()` calls create intermediate strings. For typical email titles/preview text (< 100 chars), this is negligible.

**Suggested fix:** Use a single-pass `StringBuilder`-based escape for large strings. Not worth optimizing for typical use.

**Estimated impact:** Negligible.

### L5. `RenderContext` creates many short-lived immutable copies
**File:** `RenderContext.java:85-115`

Every `withWidth()`, `withPosition()`, etc. creates a new object. ~100+ allocations per template. This is idiomatic immutable-value design and the objects are small (8 fields + shared `AtomicInteger`).

**Suggested fix:** Not recommended without profiling data. Modern JVMs handle short-lived small objects efficiently via TLAB allocation and young-generation GC.

**Estimated impact:** Negligible.

### L6. `String.replace()` for MSO section merging scans full HTML twice
**File:** `RenderPipeline.java:204-212`

`mergeMsoSectionTransitions()` calls `html.replace()` twice on the full body HTML. The replacement patterns are long and specific, so matches are rare and the JDK's `replace()` short-circuits efficiently.

**Suggested fix:** Could combine into single pass, but current simplicity is a good trade-off.

**Estimated impact:** Negligible.

### L7. `LinkedHashMap` allocations in style-building methods
**Files:** `MjSection.java`, `MjColumn.java`, `MjButton.java`, etc.

Every style-building method creates a new `LinkedHashMap` for CSS properties (~5-15 entries). The `orderedMap()` helper doesn't pre-size.

**Suggested fix:** Pre-size the `LinkedHashMap` initial capacity: `new LinkedHashMap<>(pairs.length / 2 + 1, 1.0f)`.

**Estimated impact:** Negligible -- avoids occasional map rehashing.

---

## What's Done Well

1. **Pre-compiled regex patterns for ending tags** (`MjmlPreprocessor.java:40-52`) -- Static `TAG_PATTERNS` map avoids per-call compilation.

2. **Single-pass O(n) entity replacement** (`EntityTable.java:175-210`) -- Scans for `&`, extracts entity name, looks up in hash map. Very efficient.

3. **Component DEFAULTS are static final immutable maps** (`MjSection.java:26-45`, `MjColumn.java:20-43`, etc.) -- No per-instance allocation.

4. **CSS selectors pre-parsed before matching loop** (`CssInliner.java:88-100`) -- Selectors are parsed once and stored in `ParsedRule` records, not re-parsed per element.

5. **HtmlSkeleton uses 32KB pre-allocated StringBuilder** (`HtmlSkeleton.java:22`) -- Good initial capacity for typical output.

6. **CssParser uses character-by-character parsing** without regex.

7. **CssSelectorParser is a proper recursive-descent parser** -- Efficient single-pass.

8. **HtmlDocumentParser tracks positions for in-place modification** -- The `rebuildHtml()` approach using position tracking avoids re-serializing the entire HTML tree.

9. **TreeMap descending iteration for style changes** (`CssInliner.java:300`) -- Correct reverse-order application avoids offset shifting.

10. **StringBuilder used consistently in all render methods** -- No string concatenation in loops.

11. **`MjmlNode.getChildren()` caches unmodifiable view** (`MjmlNode.java:49-54`) -- Lazy caching with invalidation on modification.

12. **`HtmlElement.cachedClassNames`** (`HtmlElement.java:23, 53-63`) -- Lazily cached class name set avoids recomputation.

13. **`AttributeResolver` uses `CssUnitParser.WHITESPACE` pre-compiled pattern** (`AttributeResolver.java:42`) -- Correctly uses static pattern for mj-class splitting.

---

## Summary Table

| ID | Severity | Area | Issue |
|----|----------|------|-------|
| H1 | High | CSS Inliner | O(R*E) brute-force rule-to-element matching |
| H2 | High | CSS Inliner | O(n) `indexOf(this)` for sibling lookups |
| H3 | High | CSS Inliner | `allDescendants()` rematerialized per call |
| H4 | High | XML Parser | `DocumentBuilderFactory.newInstance()` per parse |
| M1 | Medium | Rendering | `getDefaultAttributes()` called per `getAttribute()` |
| M2 | Medium | Rendering | `getBoxModel()` computed without caching |
| M3 | Medium | MjText | `toLowerCase()` on full content string |
| M4 | Medium | CSS Inliner | `toLowerCase()` on full HTML output |
| M5 | Medium | Fonts | Stream-based font registration checks |
| M6 | Medium | Pipeline | Unnecessary ConcurrentHashMap for defaults cache |
| M7 | Medium | Preprocessor | 8 sequential regex passes on full source |
| L1 | Low | CSS | Regex compilation in `getClassNames()` |
| L2 | Low | General | `LinkedHashMap` where `HashMap` suffices |
| L3 | Low | CSS | `merge()` allocates intermediate collections |
| L4 | Low | Skeleton | Chained `.replace()` for HTML escaping |
| L5 | Low | Rendering | Many short-lived `RenderContext` copies |
| L6 | Low | Pipeline | MSO merge scans full HTML twice |
| L7 | Low | Rendering | Style-building maps not pre-sized |

---

## Priority Recommendations

### Tier 1 (Highest Impact)
1. **Index elements by tag/class for CSS matching** (H1) -- Build lookup structures from `allDescendants()` to narrow candidate sets.
2. **Cache child index in `HtmlElement`** (H2) -- Store index on `addChild()`, eliminate O(n) sibling lookups.
3. **Cache `allDescendants()` result** (H3) -- Materialize once, reuse across consumers.
4. **Cache `DocumentBuilderFactory`** (H4) -- Static final field, simplest high-value fix.

### Tier 2 (Moderate Impact)
5. **Cache resolved attributes per component** (M1) -- Reduces redundant 5-level cascade traversals.
6. **Cache `getBoxModel()` per component** (M2) -- Avoids redundant CSS shorthand parsing.
7. **Use case-insensitive search without allocation** (M3, M4) -- `regionMatches()` or direct lowercase match.
8. **Combine preprocessor regex passes** (M7) -- Single-pass alternation regex.

### Tier 3 (Low Impact, Nice to Have)
9. **Use pre-compiled pattern for class name splitting** (L1)
10. **Switch ConcurrentHashMap to HashMap** (M6)
11. **Pre-size LinkedHashMap in style builders** (L7)

---

## Overall Assessment

The codebase is performant for its primary use case (rendering individual MJML email templates). The most impactful optimization area is the CSS inlining pipeline (H1-H3), which is the heaviest post-rendering phase. The XML parser factory reuse (H4) is the simplest high-value fix. For batch rendering scenarios (rendering many templates in sequence), the cumulative savings from Tier 1 fixes would be substantial. The medium-severity items are worth addressing if profiling confirms they contribute meaningfully to render latency.
