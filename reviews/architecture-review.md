# Architecture Review: mjml-java

**Reviewer:** Architecture Review Agent (deep source analysis)
**Date:** 2026-02-08
**Codebase:** mjml-java v1.0.0-SNAPSHOT
**Scope:** 75 source files across 11 packages, zero external runtime dependencies

---

## Executive Summary

mjml-java is a well-architected pure Java 17 MJML-to-HTML renderer with zero external runtime dependencies. The codebase demonstrates strong separation of concerns across 11 packages organized around a clear 7-phase rendering pipeline. The sealed type hierarchy, immutable configuration, functional factory pattern, and context-passing design are all well-chosen for this domain. The architecture successfully balances fidelity to MJML v4 output with clean Java idioms.

The design achieves thread safety through instance isolation (each render creates fresh state), extensibility through custom component registration and pluggable include resolvers, and correctness through 33 golden file tests that validate byte-for-byte MJML v4 compatibility.

**Overall Assessment: 4.5/5 — Production-quality architecture with a clear evolution path**

| Category | Score | Notes |
|---|---|---|
| Pipeline Design | 4.5/5 | Clear 7-phase architecture; minor post-fixup complexity |
| Separation of Concerns | 4.5/5 | CSS package fully independent; clean layering |
| Sealed Hierarchy | 5/5 | Excellent use of sealed types + records |
| Component Registry | 4/5 | Functional with freeze(); container constructor asymmetry |
| Context Design | 4/5 | RenderContext excellent; GlobalContext self-documents decomposition path |
| Dependency Management | 5/5 | Zero external deps; clean internal DAG |
| Thread Safety | 5/5 | Immutable config; fresh state per render; documented |
| Extensibility | 4.5/5 | Custom components, include resolvers, configuration |
| Testability | 4/5 | 196 tests; golden files are backbone |
| API Design | 4.5/5 | Minimal, clear, well-documented |
| Module System | 4/5 | Broad exports enable extensibility; could narrow |

---

## Architecture Diagram

```
                          +------------------+
                          |   MjmlRenderer   |  (Public API: static facade)
                          +--------+---------+
                                   |
                            MjmlConfiguration (immutable, builder pattern)
                                   |
                          +--------v---------+
                          |  RenderPipeline  |  (Orchestrator: owns 7-phase flow)
                          +--+--+--+--+--+---+
                             |  |  |  |  |
                +------------+  |  |  |  +-------------+
                |               |  |  |                 |
       Phase 1-2|      Phase 3  |  |  | Phase 6        | Phase 7
    +-----------v--+  +---------v-+|  +------v-------+  +--------v---------+
    | MjmlParser   |  | Include   || | ComponentReg  |  | HtmlSkeleton     |
    | Preprocessor |  | Processor || | + Components  |  | CssInliner       |
    +--------------+  +-----------+|  +--------------+  +------------------+
                            Phase 4-5|
                        +----------v---------+
                        | GlobalContext      |
                        | RenderContext      |
                        | AttributeResolver |
                        +--------------------+

Package Structure
=================
dev.jcputney.mjml              Public API (6 types: Renderer, Config, Result, Exception, IncludeResolver, FSResolver)
dev.jcputney.mjml.parser       XML preprocessing + parsing (Preprocessor, Parser, MjmlNode, MjmlDocument, EntityTable, IncludeProcessor)
dev.jcputney.mjml.component    Base hierarchy (BaseComponent sealed, BodyComponent, HeadComponent, ComponentRegistry, ComponentFactory)
  .body                        Layout (MjBody, MjSection, MjColumn, MjGroup, MjWrapper, AbstractSectionComponent)
  .content                     Content (MjText, MjImage, MjButton, MjDivider, MjSpacer, MjTable, MjRaw)
  .head                        Head (MjHead, MjTitle, MjPreview, MjFont, MjBreakpoint, MjStyle, MjAttributes, MjHtmlAttributes)
  .interactive                 Interactive (MjAccordion[4], MjCarousel[2], MjHero, MjNavbar[2], MjSocial[2])
dev.jcputney.mjml.context      Context objects (GlobalContext, RenderContext, AttributeResolver)
dev.jcputney.mjml.css          Standalone CSS engine (CssInliner, HtmlDocumentParser, HtmlElement, CssSelector[sealed], CssParser, etc.)
dev.jcputney.mjml.render       Pipeline + output assembly (RenderPipeline, HtmlSkeleton, VmlHelper, DefaultFontRegistry)
dev.jcputney.mjml.util         Pure utilities (CssUnitParser, CssBoxModel, ColumnWidthCalculator, BackgroundHelpers, HtmlEscaper, SocialNetworkRegistry)

Inter-Package Dependency Flow (DAG - No Cycles)
================================================
API  -->  render  -->  parser, component, context, css, util
component  -->  context, parser(MjmlNode), util
context  -->  parser(MjmlNode), util(CssUnitParser)
css  -->  (SELF-CONTAINED - no MJML dependencies)
render  -->  component, context, parser, css, util
util  -->  (no MJML dependencies; pure utilities)
```

---

## 1. Pipeline Analysis

### The 7-Phase Rendering Pipeline

The pipeline is the backbone of the architecture, orchestrated entirely within `RenderPipeline.render()`:

| Phase | Method | Input | Output | Assessment |
|-------|--------|-------|--------|------------|
| 1-2. Preprocess + Parse | `MjmlParser.parse()` | MJML string | `MjmlDocument` | Stateless; CDATA wrapping + JDK DOM parsing |
| 3. Include Resolution | `IncludeProcessor.process()` | `MjmlDocument` | Mutated `MjmlDocument` | Cycle detection + depth limit (50) |
| 4. Head Processing | `processHead()` | `MjmlDocument` + `GlobalContext` | Side-effects on `GlobalContext` | HeadComponent.process() pattern |
| 4b. Font Registration | `registerDefaultFonts()` | `MjmlDocument` + `GlobalContext` | Side-effects on `GlobalContext` | Scans tree for font-family usage |
| 5-6. Body Rendering | `renderBody()` | `MjmlDocument` + `GlobalContext` | HTML string | Recursive top-down |
| 6a. MSO Merging | `mergeMsoSectionTransitions()` | HTML string | HTML string | String replacement |
| 6b. HTML Attributes | `applyHtmlAttributes()` | HTML string + `GlobalContext` | HTML string | CSS selector matching |
| 7. Skeleton Assembly | `HtmlSkeleton.assemble()` | Body HTML + `GlobalContext` | Full HTML document | DOCTYPE, head, fonts, media queries |
| 7b. CSS Inlining | `CssInliner.inlineAdditionalOnly()` | HTML + inline CSS | Final HTML | Only for `mj-style inline="inline"` |

**Strengths:**
- Phases are cleanly separated with well-defined data flow. The pipeline creates fresh instances per render call — no shared mutable state between concurrent renders.
- Phase 5 (attribute resolution) is demand-driven via `AttributeResolver.resolve()` rather than a separate pre-computation pass, which is efficient and avoids stale state.
- Input validation occurs upfront: `maxInputSize` check before any processing, `maxNestingDepth` enforced during parsing (hardcoded at 100 in `MjmlParser`).

**Observations:**
- Phases 6a/6b/7b are post-processing fixups within `render()`. They are correctly ordered but make the pipeline less purely sequential. These could be extracted into named pipeline stages for clarity.
- The `getComponentDefaults()` method uses a `ConcurrentHashMap` cache and creates dummy contexts/components to extract default attributes. This is a workaround because defaults are instance methods rather than static data. The cache mitigates performance impact.

### Security-Relevant Pipeline Properties

- **XXE protection:** `MjmlParser` disables external DTDs and entities via `DocumentBuilderFactory` features
- **Input size limiting:** `RenderPipeline.render()` checks `configuration.getMaxInputSize()` before any processing
- **Nesting depth:** Both XML parsing (depth 100 in `MjmlParser`) and includes (depth 50 in `IncludeProcessor`) have depth limits
- **Path traversal:** `FileSystemIncludeResolver` validates that resolved paths don't escape the base directory
- **CDATA injection:** `MjmlPreprocessor` escapes `]]>` sequences before CDATA wrapping

---

## 2. Component Hierarchy Analysis

### Sealed Type Hierarchy

```
BaseComponent (sealed)                     [component package]
  |-- getTagName(): String
  |-- getDefaultAttributes(): Map
  |-- getAttribute(name): String           [delegates to AttributeResolver.resolve()]
  |
  +-- BodyComponent (non-sealed, abstract) [component package]
  |     |-- render(): String
  |     |-- getContentWidth(): double
  |     |-- getBoxModel(): CssBoxModel
  |     |-- buildStyle(Map): String
  |     |-- buildAttributes(Map): String
  |     |-- renderChildren(ComponentRegistry): String
  |     |
  |     +-- AbstractSectionComponent (abstract)   [body package]
  |     |     |-- MjSection                       (4-arg: + ComponentRegistry)
  |     |     +-- MjWrapper                       (4-arg: + ComponentRegistry)
  |     +-- MjBody, MjColumn, MjGroup             (4-arg: + ComponentRegistry)
  |     +-- MjText, MjImage, MjButton, ...        (3-arg: standard)
  |     +-- MjAccordion, MjCarousel, MjHero, ...  (4-arg: + ComponentRegistry)
  |
  +-- HeadComponent (non-sealed, abstract) [component package]
        |-- process(): void                [updates GlobalContext]
        |-- getDefaultAttributes(): Map.of()
        |
        +-- MjHead, MjTitle, MjPreview, MjFont, MjBreakpoint
        +-- MjStyle, MjAttributes, MjHtmlAttributes
```

**Strengths:**
- The sealed hierarchy correctly models the MJML domain: components are either head (metadata extraction) or body (HTML rendering). The `sealed` keyword on `BaseComponent` prevents any third category from emerging.
- `non-sealed` on both subclasses is correct — custom components extending `BodyComponent` or `HeadComponent` are the intended extension point.
- `AbstractSectionComponent` successfully extracts ~130 lines of shared background-image, box-model, and style-building logic between `MjSection` and `MjWrapper`.
- Default attributes use `Map.ofEntries()` constants — immutable, allocation-free on repeated access, and clear.

**Observations:**
- **Constructor asymmetry:** Container components take a 4th `ComponentRegistry` parameter while content components use the standard 3-arg constructor. This means `ComponentFactory` (`(MjmlNode, GlobalContext, RenderContext) -> BaseComponent`) doesn't directly fit containers — they require lambda wrappers that capture the registry:
  ```java
  reg.register("mj-text", MjText::new);                                    // Content: method ref
  reg.register("mj-section", (node, ctx, rctx) -> new MjSection(node, ctx, rctx, reg)); // Container: lambda
  ```
  This works but is a convention rather than a type-system enforcement. A `ContainerBodyComponent` abstract class holding the registry field would formalize the distinction.

---

## 3. Component Registry Design

### Implementation

```java
public class ComponentRegistry {
    private final Map<String, ComponentFactory> factories = new LinkedHashMap<>();
    private boolean frozen = false;

    public void register(String tagName, ComponentFactory factory);  // throws if frozen
    public void freeze();                                             // prevents further registration
    public BaseComponent createComponent(MjmlNode, GlobalContext, RenderContext); // returns null if unknown
}
```

**Strengths:**
- Simple, predictable tag-name-to-factory mapping. No reflection, classpath scanning, or annotation magic.
- `freeze()` prevents mutation after initialization — `RenderPipeline` calls `registry.freeze()` immediately after `createRegistry()`.
- Custom component support flows cleanly: `MjmlConfiguration.builder().registerComponent("mj-custom", factory)` -> `RenderPipeline.createRegistry()` -> registry.
- `ComponentFactory` is `@FunctionalInterface`, enabling clean lambda/method-reference registration.

**Observations:**
- `createComponent()` returns `null` for unknown tags and logs a warning. Callers use `instanceof BodyComponent bodyComponent` which safely handles null. This is a graceful degradation pattern — unknown tags are silently skipped.
- The registry is created fresh per `RenderPipeline` (per render call), so even without `freeze()` there's no cross-render contamination. But `freeze()` adds defense-in-depth.
- All 33 built-in components are explicitly imported and registered in `RenderPipeline.createRegistry()` (33 import statements + 33 register calls). At the current scale this is manageable and has the advantage of being explicit, but would benefit from extraction into a `ComponentRegistryBuilder` if the component count grows significantly.

---

## 4. Context Object Analysis

### GlobalContext — Document-Wide State

The class accumulates state during head processing and rendering:

| Category | Fields | Written By | Read By |
|----------|--------|-----------|---------|
| Metadata | title, previewText, breakpoint, containerWidth, bodyBgColor | Head components | HtmlSkeleton |
| Fonts | fonts (Set\<FontDef>), fontUrlOverrides | MjFont, DefaultFontRegistry | HtmlSkeleton |
| Styles | styles, componentStyles, inlineStyles | MjStyle | HtmlSkeleton, RenderPipeline |
| Attributes | defaultAttributes, classAttributes | MjAttributes | AttributeResolver (during render) |
| Media Queries | mediaQueries, fluidOnMobileUsed | Body components (MjColumn, MjImage) | HtmlSkeleton |
| HTML Attributes | htmlAttributes | MjHtmlAttributes | RenderPipeline.applyHtmlAttributes() |
| Comments | headComments | RenderPipeline.processHead() | HtmlSkeleton |

**Assessment:**
- The class itself contains a comprehensive Javadoc decomposition plan (MetadataContext / StyleContext / AttributeContext). This self-awareness is excellent.
- At ~236 lines, GlobalContext is manageable. The decomposition becomes worthwhile when it grows beyond ~300 lines or when API boundaries need to be formalized.
- Key observation: Body components (MjColumn, MjImage, MjNavbar) *write* to GlobalContext during rendering (mediaQueries, componentStyles, fluidOnMobileUsed). This means head processing and body rendering are not fully phase-separated — the style output depends on which body components were rendered.
- Collection getters (`getFonts()`, `getStyles()`, `getMediaQueries()`, etc.) all return `Collections.unmodifiable*()` views. This correctly prevents external mutation.

### RenderContext — Per-Component State

```java
public class RenderContext {
    private final double containerWidth;      // Narrows: 600 -> section -> column -> content
    private final String columnWidthSpec;     // Set by section for column responsive classes
    private final String direction;           // Text direction
    private final int index;                  // Position among siblings
    private final boolean first, last;        // Position flags
    private final boolean insideWrapper;      // Context flag
    private final boolean insideGroup;        // Context flag
    private final AtomicInteger idCounter;    // SHARED across all derived contexts
}
```

**Assessment: Excellent design.**
- Effectively immutable: all `withX()` factory methods create new instances, preserving parent state. The only shared mutable state is `idCounter` (AtomicInteger), which is intentionally shared for deterministic unique ID generation across the render tree.
- Width narrowing pattern (`body(600) -> section(600) -> column(200) -> content(150)`) cleanly models the MJML layout model.
- The private 9-parameter constructor is the only way to create instances (via `withX()` methods or the public 1-arg constructor). This prevents inconsistent state.

### AttributeResolver — 5-Level Cascade

```
Level 1: Inline attributes (on the element itself)
Level 2: mj-class attributes (from GlobalContext.classAttributes)
Level 3: Tag-specific defaults (from GlobalContext.defaultAttributes)
Level 4: mj-all defaults (from GlobalContext.defaultAttributes["mj-all"])
Level 5: Component hardcoded defaults (from getDefaultAttributes())
```

**Assessment:** Clean, correct, and stateless. A pure function: `(MjmlNode, String, GlobalContext, Map) -> String`. This correctly implements the MJML attribute cascade specification.

---

## 5. Parsing Layer Analysis

### MjmlPreprocessor

- Wraps content of 8 "ending tags" (mj-text, mj-button, mj-table, mj-raw, mj-navbar-link, mj-accordion-title, mj-accordion-text, mj-style, mj-html-attribute) in CDATA sections so the JDK XML parser can handle embedded HTML.
- Pre-compiled regex patterns (one per ending tag) stored in a static `Map<String, Pattern>`.
- Handles CDATA injection prevention: replaces `]]>` in content with `]]]]><![CDATA[>`.
- Replaces HTML entities only outside CDATA sections to preserve user content.

**Assessment:** Correct and well-tested. The CDATA wrapping approach is the right solution for parsing MJML (which embeds HTML in XML) without a custom parser.

### MjmlParser

- Uses JDK `DocumentBuilderFactory` with XXE protections (external DTD, external entities, external parameter entities all disabled).
- Converts DOM `Element` tree to lightweight `MjmlNode` tree.
- Enforces nesting depth limit (100) to prevent stack overflow from deeply nested MJML.
- Handles CDATA sections, text nodes, and comments during conversion.

**Assessment:** Clean, stateless, and secure. The DOM-to-MjmlNode conversion is straightforward.

### MjmlNode

- Mutable tree node with tag name, attributes (LinkedHashMap), children (ArrayList), text content, and parent reference.
- `replaceWith(List<MjmlNode>)` enables include resolution (replacing `mj-include` with resolved content).
- `getChildren()` returns a cached `Collections.unmodifiableList()` (invalidated on child addition/replacement).
- `getInnerHtml()` and `getOuterHtml()` serialize the node tree back to HTML — used by content components to access their HTML content.

**Assessment:** Appropriate for the use case. The mutability is necessary for include resolution (Phase 3) and is contained to the parsing phase.

### IncludeProcessor

- Supports three include types: `mjml` (fragment/document), `html` (raw), `css` (style).
- Cycle detection via `Set<String>` of visited paths passed down the recursion.
- Depth limit of 50 to prevent deeply nested includes.
- Full MJML document includes correctly extract body/head children based on parent context.
- Fragment includes wrap in temporary `<mjml><mj-body>` for parsing, falling back to `mj-raw` if parsing fails.

**Assessment:** Well-designed with proper safety checks. The cycle detection and depth limiting are correct.

---

## 6. CSS Engine Analysis

### Independence

The `css` package (11 files) has **zero dependencies** on any other MJML package. It operates entirely on raw HTML strings and CSS text. This is the strongest separation-of-concerns achievement in the codebase — the CSS engine could be extracted as a standalone library.

### Components

| File | Purpose |
|------|---------|
| `CssInliner` | Public API: `inline(html)` and `inlineAdditionalOnly(html, css)` |
| `CssParser` | Tokenizes CSS into `CssRule` + preserved at-rules |
| `CssSelector` | Sealed interface hierarchy: SelectorList, ComplexSelector, CompoundSelector, SimpleSelector* |
| `CssSelectorParser` | Parses selector strings into `CssSelector` AST |
| `CssSelectorMatcher` | Matches `CssSelector` against `HtmlElement` tree |
| `CssSpecificity` | Record implementing `Comparable` for cascade ordering |
| `CssDeclaration` | Record: property + value + important flag |
| `CssRule` | Record: selector text + declarations list |
| `StyleAttribute` | Parse/merge/serialize inline style attributes |
| `HtmlDocumentParser` | Lightweight HTML tokenizer -> `HtmlElement` tree |
| `HtmlElement` | Minimal DOM: tag, attributes, parent/child/sibling, position tracking |

### CssSelector Sealed Hierarchy

```
CssSelector (sealed interface)
  +-- SelectorList (record: List<CssSelector>)           "h1, h2, h3"
  +-- ComplexSelector (record: left + Combinator + right) "div > p"
  +-- CompoundSelector (record: List<SimpleSelector>)     "div.red#main"
  +-- SimpleSelector (sealed interface)
        +-- TypeSelector ("div")
        +-- ClassSelector (".foo")
        +-- IdSelector ("#bar")
        +-- AttributeSelector ("[data-x='y']")
        +-- PseudoClassSelector (":hover")
        +-- PseudoElementSelector ("::before")
        +-- UniversalSelector ("*")
```

**Assessment:** This is a textbook use of sealed types + records for algebraic data types in Java 17. The specificity computation is implemented per-selector-type with correct CSS specificity rules. The `CssSelectorMatcher` handles all combinator types (descendant, child, adjacent sibling, general sibling).

### Dual HTML Representation

The codebase has two HTML tree representations:
1. **`MjmlNode`** (parser package): Used for MJML parsing and the component tree
2. **`HtmlElement`** (css package): Used for CSS inlining and `mj-html-attributes`

This means the rendered HTML (a string from Phase 6) is parsed a second time by `HtmlDocumentParser` in Phase 7. This is architecturally correct — the CSS engine should not depend on MJML internals — but when both `mj-html-attributes` and inline CSS are used, the HTML is parsed twice in Phase 7 alone (`applyHtmlAttributes()` and `CssInliner.inlineAdditionalOnly()` each call `HtmlDocumentParser.parse()`).

**Recommendation:** Cache the `HtmlElement` tree between these two Phase 7 operations to avoid double-parsing. This is a performance optimization, not an architectural flaw.

---

## 7. Strengths

### 1. Zero External Dependencies
The only JDK module dependency is `java.xml` (for DOM parsing). No Guava, no Jackson, no third-party HTML parsers. This makes the library lightweight (~75 source files), avoids version conflicts in consumer projects, and keeps the attack surface minimal.

### 2. Thread Safety by Design
Each `MjmlRenderer.render()` call creates a fresh `RenderPipeline` and `GlobalContext`. `MjmlConfiguration` is immutable (builder pattern, `Map.copyOf()` for collections). The `MjmlRenderResult` is a record. The public API Javadoc explicitly documents thread safety guarantees:
> "The static `render()` methods are thread-safe. Each call creates its own `RenderPipeline` and `GlobalContext`, so concurrent calls do not share mutable state."

### 3. Clean Public API
```java
String html = MjmlRenderer.render(mjml);                     // One-liner with defaults
MjmlRenderResult result = MjmlRenderer.render(mjml, config);  // With configuration
```
The public API surface is minimal: 6 types in the root package (`MjmlRenderer`, `MjmlConfiguration`, `MjmlRenderResult`, `MjmlException`, `IncludeResolver`, `FileSystemIncludeResolver`). Everything else is implementation detail.

### 4. Extensibility Points
- **Custom components:** `MjmlConfiguration.builder().registerComponent("mj-custom", factory)` integrates with the registry and participates in the full pipeline.
- **Include resolution:** The `IncludeResolver` interface allows file system, classpath, HTTP, or any custom source.
- **Configuration:** Builder pattern with sensible defaults, including security controls (`sanitizeOutput`, `maxInputSize`, `maxNestingDepth`).

### 5. Sealed Types Used Correctly
Three uses of sealed types in the codebase:
1. `BaseComponent` sealed to `BodyComponent | HeadComponent` — enforces the head/body dichotomy
2. `CssSelector` sealed interface with 7+ record variants — algebraic data type for CSS selectors
3. `CssSelector.SimpleSelector` sealed sub-interface — further constrains the selector leaf types

### 6. Security-Conscious Design
- XXE protection in `MjmlParser`
- Path traversal prevention in `FileSystemIncludeResolver`
- Input size and nesting depth limits
- HTML comment injection prevention (`--` stripping in `HtmlSkeleton`)
- CDATA injection prevention in `MjmlPreprocessor`
- Optional XSS sanitization via `HtmlEscaper` (enabled by `sanitizeOutput`)
- SSRF warning documented on `IncludeResolver` interface

### 7. Immutable Value Objects
Extensive use of Java records: `MjmlRenderResult`, `FontDef`, `MediaQuery`, `CssDeclaration`, `CssRule`, `CssSpecificity`, `CssBoxModel`, `StyleExtractionResult`, all `CssSelector` variants, and `StyleChange`. This eliminates entire classes of bugs around defensive copying and accidental mutation.

---

## 8. Weaknesses and Improvement Opportunities

### 1. RenderPipeline Has Too Many Responsibilities (Medium Impact)

`RenderPipeline` (383 lines) currently handles:
- Component registry creation (33+ imports, 33 register calls)
- All 7 pipeline phases plus sub-phases (6a, 6b, 7b)
- Font scanning with dummy context creation and `ConcurrentHashMap` caching
- MSO transition merging (string replacement)
- HTML attribute application (CSS selector matching on rendered output)
- Post-CSS-inlining fixups (`style=""` -> `style`, `/>` -> `>`)

**Recommendation:** Extract `createRegistry()` into a `ComponentRegistryBuilder`. Extract font scanning into `DefaultFontRegistry`. Move MSO merging and HTML attribute application into helper classes. This would make `RenderPipeline.render()` a pure orchestrator calling named stages.

### 2. Constructor Asymmetry (Low-Medium Impact)

Container components (9 components: MjBody, MjSection, MjColumn, MjGroup, MjWrapper, MjHero, MjAccordion, MjAccordionElement, MjCarousel, MjNavbar) take `ComponentRegistry` as a 4th constructor parameter. Content components (7 components: MjText, MjImage, MjButton, etc.) do not. This creates two patterns in `createRegistry()` and means the `ComponentFactory` interface doesn't fully describe the container contract.

**Recommendation:** Formalize with an abstract `ContainerBodyComponent` class:
```java
public abstract class ContainerBodyComponent extends BodyComponent {
    protected final ComponentRegistry registry;
    protected ContainerBodyComponent(MjmlNode n, GlobalContext g, RenderContext r, ComponentRegistry reg) { ... }
}
```

### 3. String-Based HTML Rendering (Low Impact, Conscious Tradeoff)

Components build HTML via `StringBuilder` concatenation with hardcoded indentation strings. `MjSection.render()` is ~270 lines of string assembly including 3 render variants (normal, full-width, inside-wrapper). This is verbose and fragile for whitespace-sensitive output.

**Assessment:** This is a deliberate tradeoff for MJML v4 fidelity. The golden test suite validates exact byte-for-byte output matching, and a template or DOM-based approach would be cleaner but harder to match precisely. The current approach is correct for the project's goals.

### 4. GlobalContext Phase Mixing (Low Impact)

Body components write to `GlobalContext` during rendering (Phase 6):
- `MjColumn` adds media queries
- `MjImage` sets `fluidOnMobileUsed`
- `MjNavbar` adds component styles

This means the final HTML skeleton (Phase 7) depends on which body components were rendered, creating an implicit ordering dependency. If body rendering were ever parallelized, these writes would need synchronization.

**Recommendation:** Collect side-effects from body rendering in a separate `RenderOutput` accumulator, then merge into the skeleton assembly. This would fully separate the head-processing and body-rendering phases.

### 5. Module Exports May Be Too Broad (Low Impact)

`module-info.java` exports 6 packages:
```java
exports dev.jcputney.mjml;       // Public API (needed)
exports dev.jcputney.mjml.css;    // CSS inliner (useful standalone)
exports dev.jcputney.mjml.component;  // For custom component development
exports dev.jcputney.mjml.context;    // For custom component development
exports dev.jcputney.mjml.parser;     // For custom component development
exports dev.jcputney.mjml.util;       // For custom component development
```

The last 4 exports are needed for custom component authors to access `BaseComponent`, `BodyComponent`, `MjmlNode`, `GlobalContext`, `RenderContext`, and utility classes. This is a reasonable choice but does expose internal implementation details.

**Recommendation:** Consider whether `exports ... to dev.jcputney.mjml` (qualified exports) would be more appropriate for internal packages, or keep broad exports but document which packages are public API vs SPI.

---

## 9. Recommendations (Prioritized by Impact)

### High Priority
None — the architecture is sound and production-ready for its current scope.

### Medium Priority
1. **Extract RenderPipeline responsibilities** into dedicated helpers (registry builder, font scanner, MSO merger, HTML attribute applier) to reduce the class from 383 lines to a pure orchestrator.
2. **Cache HtmlElement parse tree** when both `mj-html-attributes` and CSS inlining are needed in Phase 7, to avoid double-parsing rendered HTML.
3. **Collect Phase 6 side-effects** in a `RenderOutput` object rather than writing directly to `GlobalContext`, to enable future parallelization and cleaner phase separation.

### Low Priority (Deferred)
4. **Decompose GlobalContext** per its own Javadoc when the class grows beyond ~300 lines.
5. **Formalize container/content distinction** with a `ContainerBodyComponent` abstract class.
6. **Consider SPI (ServiceLoader) for component registration** if the project grows beyond ~50 components or needs plugin support.
7. **Narrow module exports** when public API stability is a priority.

---

## 10. Summary Scorecard

| Dimension | Score | Evidence |
|-----------|-------|----------|
| Separation of Concerns | 4.5/5 | CSS package fully independent; clean package DAG |
| Pipeline Design | 4.5/5 | Clear 7-phase flow; minor post-fixup complexity |
| Component Hierarchy | 4.5/5 | Sealed types well-used; AbstractSectionComponent eliminates duplication |
| Context Management | 4/5 | RenderContext excellent; GlobalContext manageable with documented path |
| Extensibility | 4.5/5 | Custom components, include resolvers, standalone CSS engine |
| Dependency Management | 5/5 | Zero external deps; clean internal DAG; no cycles |
| Security Design | 4.5/5 | XXE, path traversal, size limits, XSS sanitization |
| Thread Safety | 5/5 | Instance isolation by design; documented in API Javadoc |
| API Design | 4.5/5 | Minimal (6 types), clear, builder pattern, well-documented |
| Immutability | 4.5/5 | Records everywhere; config is immutable; context getters return unmodifiable views |
| **Overall** | **4.5/5** | **Production-quality architecture with clear evolution path** |

---

## Conclusion

mjml-java demonstrates mature architectural thinking for a compatibility-focused library. The sealed hierarchy, zero-dependency design, instance-isolated thread safety, and standalone CSS engine are particular highlights. The self-documenting GlobalContext decomposition plan shows the developer has identified the main evolutionary pressure point. The architecture supports the project's primary goal — byte-for-byte MJML v4 output compatibility — without sacrificing extensibility or clean code. The recommended improvements are evolutionary refinements, not structural changes.
