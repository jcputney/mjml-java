# API Usability Review: mjml-java

## Executive Summary

The mjml-java public API is **well-designed for its primary use case** — converting MJML strings to HTML with minimal ceremony. The one-liner `MjmlRenderer.render(mjml)` is as simple as it gets, and the builder-pattern configuration follows Java conventions well. The library achieves its "zero external dependencies" goal, keeping the consumer's classpath clean.

However, the API surface is **significantly over-exported**: the `module-info.java` exports 6 packages (including `context`, `parser`, `util`, and `component`) when consumers really only need the core `dev.jcputney.mjml` package for standard usage, plus `dev.jcputney.mjml.component` for custom component authors. This leaks implementation details and creates a backward-compatibility burden.

**Overall grade: B+** — Excellent for the "happy path," but the over-exported internals, flat exception hierarchy, and a few missing convenience methods hold it back from an A.

---

## "Getting Started" Experience (First 5 Minutes)

### Minimal Usage (1 line)

```java
String html = MjmlRenderer.render(mjmlString);
```

**Verdict: Excellent.** This is the gold standard for a rendering library. No instantiation, no configuration, no builder — just call and get HTML. Comparable to the Node.js MJML library's `mjml2html()`.

### Configured Usage (3-4 lines)

```java
MjmlConfiguration config = MjmlConfiguration.builder()
    .language("en")
    .build();
MjmlRenderResult result = MjmlRenderer.render(mjmlString, config);
String html = result.html();
String title = result.title();
```

**Verdict: Good.** Builder pattern is idiomatic Java. The `MjmlRenderResult` record provides structured output. The dual-return approach (simple `String` for defaults, `MjmlRenderResult` for configured) is pragmatic.

### Include Resolver Setup (5-6 lines)

```java
MjmlConfiguration config = MjmlConfiguration.builder()
    .includeResolver(new FileSystemIncludeResolver(Path.of("/templates")))
    .build();
MjmlRenderResult result = MjmlRenderer.render(mjmlString, config);
```

**Verdict: Good.** The `IncludeResolver` interface is clean (single method), and `FileSystemIncludeResolver` is a useful built-in. Path traversal protection is a nice security touch.

### Custom Component (15-20 lines)

```java
class MjGreeting extends BodyComponent {
    MjGreeting(MjmlNode node, GlobalContext ctx, RenderContext rctx) {
        super(node, ctx, rctx);
    }
    @Override public String getTagName() { return "mj-greeting"; }
    @Override public Map<String, String> getDefaultAttributes() { return Map.of("name", "World"); }
    @Override public String render() { return "<div>Hello, " + getAttribute("name") + "!</div>"; }
}

MjmlConfiguration config = MjmlConfiguration.builder()
    .registerComponent("mj-greeting", MjGreeting::new)
    .build();
```

**Verdict: Acceptable but leaky.** Custom component authors must work with `MjmlNode`, `GlobalContext`, and `RenderContext` — all internal types. The `ComponentFactory` functional interface enabling `MjGreeting::new` is a nice touch, but the SPI could be more abstracted.

---

## API Surface Inventory

### Package: `dev.jcputney.mjml` (core — must export)

| Class | Role | Public Methods |
|-------|------|----------------|
| `MjmlRenderer` | Entry point | `render(String)`, `render(String, MjmlConfiguration)` |
| `MjmlConfiguration` | Config holder | `getLanguage()`, `getIncludeResolver()`, `getCustomComponents()`, `isSanitizeOutput()`, `getMaxInputSize()`, `getMaxNestingDepth()`, `builder()`, `defaults()` |
| `MjmlConfiguration.Builder` | Fluent builder | `language()`, `includeResolver()`, `registerComponent()`, `sanitizeOutput()`, `maxInputSize()`, `maxNestingDepth()`, `build()` |
| `MjmlRenderResult` | Result record | `html()`, `title()` |
| `MjmlException` | Exception | Standard `RuntimeException` constructors |
| `IncludeResolver` | SPI interface | `resolve(String)` |
| `FileSystemIncludeResolver` | Built-in resolver | Constructor `(Path)`, `resolve(String)` |

### Package: `dev.jcputney.mjml.component` (needed for custom components)

| Class | Role | Key Methods |
|-------|------|-------------|
| `BaseComponent` (sealed) | Abstract base | `getTagName()`, `getDefaultAttributes()`, `getAttribute()`, `getNode()` |
| `BodyComponent` (non-sealed) | Renderable base | `render()`, `getContentWidth()`, `getBoxModel()`, protected helpers |
| `HeadComponent` (non-sealed) | Head processing base | `process()` |
| `ComponentFactory` | Functional interface | `create(MjmlNode, GlobalContext, RenderContext)` |
| `ComponentRegistry` | Tag-to-factory map | `register()`, `freeze()`, `createComponent()` |

### Package: `dev.jcputney.mjml.context` (leaked internals)

| Class | Role | Concern |
|-------|------|---------|
| `GlobalContext` | Mutable document state | 40+ public methods, 2 inner records |
| `RenderContext` | Immutable render state | 12 public methods |
| `AttributeResolver` | Static cascade resolver | 1 static method |

### Package: `dev.jcputney.mjml.parser` (leaked internals)

| Class | Role | Concern |
|-------|------|---------|
| `MjmlNode` | Mutable tree node | 15+ public methods |
| `MjmlDocument` | Document wrapper | `getRoot()`, `getHead()`, `getBody()` |
| `MjmlParser` | Static parser | `parse(String)` |
| `MjmlPreprocessor` | Static preprocessor | `preprocess(String)` |
| `IncludeProcessor` | Include resolver | `process(MjmlDocument)` |
| `EntityTable` | HTML entity map | `replaceEntities(String)` |

### Package: `dev.jcputney.mjml.css` (leaked internals)

| Class | Role | Concern |
|-------|------|---------|
| `CssInliner` | CSS inlining engine | 3 public methods |
| `CssParser` | CSS parser | Internal |
| `CssSelector` | Selector model | Internal |
| `CssSelectorParser` | Selector parser | Internal |
| `CssSelectorMatcher` | Selector matching | Internal |
| `CssSpecificity` | Specificity calc | Internal |
| `CssDeclaration` | CSS declaration | Internal |
| `CssRule` | CSS rule model | Internal |
| `StyleAttribute` | Style attribute | Internal |
| `HtmlDocumentParser` | HTML parser | Internal |
| `HtmlElement` | HTML element | Internal |

### Package: `dev.jcputney.mjml.util` (leaked internals)

| Class | Role | Concern |
|-------|------|---------|
| `CssUnitParser` | Unit conversion | Internal (exposes `WHITESPACE` Pattern) |
| `CssBoxModel` | Box model record | Internal |
| `HtmlEscaper` | HTML escaping | Internal |
| `SocialNetworkRegistry` | Social icons | Internal |
| `BackgroundPositionHelper` | VML helpers | Internal |
| `BackgroundCssHelper` | Background CSS | Internal |
| `ColumnWidthCalculator` | Column math | Internal |

---

## Usability Findings

### Critical (Should Fix Before 1.0)

#### 1. Over-exported Module Packages
**File:** `module-info.java`

The module exports 6 packages but consumers only need 2:
- `dev.jcputney.mjml` — the core API
- `dev.jcputney.mjml.component` — for custom component authors

The other 4 packages (`css`, `context`, `parser`, `util`) are implementation details. Exporting them:
- Creates a massive backward-compatibility surface (100+ public methods across ~25 classes)
- Misleads users into depending on internals
- Makes it harder to refactor (any public method in an exported package is a contract)

**Recommendation:** Remove exports for `css`, `context`, `parser`, and `util`. If custom components need `MjmlNode`, `GlobalContext`, and `RenderContext`, keep `parser` and `context` exported but consider using `exports ... to dev.jcputney.mjml.component` (qualified exports) or creating an SPI package with only the types custom components need.

#### 2. Flat Exception Hierarchy
**File:** `MjmlException.java`

A single `MjmlException` is used for all failures:
- Parse errors (`"Failed to parse MJML"`)
- Include resolution errors (`"Include file not found"`)
- Input validation errors (`"Input size exceeds maximum"`)
- Nesting depth errors (`"Maximum nesting depth exceeded"`)
- Circular include errors (`"Circular include detected"`)

Callers cannot catch specific failure types without string-matching on the message.

**Recommendation:** Add specific exception subclasses:
```java
MjmlParseException extends MjmlException
MjmlIncludeException extends MjmlException
MjmlValidationException extends MjmlException
```

### High (Strong Recommendations)

#### 3. No Overload for Rendering from InputStream/Path/Reader
**File:** `MjmlRenderer.java`

The renderer only accepts `String`. Real-world usage often starts with files, classpath resources, or streams. Users must write boilerplate to read-then-render.

**Recommendation:** Add convenience overloads:
```java
MjmlRenderer.render(Path mjmlFile)
MjmlRenderer.render(Path mjmlFile, MjmlConfiguration config)
MjmlRenderer.render(InputStream is, Charset charset)
```

#### 4. `sanitizeOutput` Defaults to `false`
**File:** `MjmlConfiguration.java:88`

The default being `false` means XSS-vulnerable output by default. While documented, this is a pit-of-failure default — new users who don't read docs carefully get insecure output.

**Recommendation:** Default `sanitizeOutput` to `true`, and note in migration guide. Or at minimum, add a prominent Javadoc warning on `render()` methods.

#### 5. `MjmlRenderResult` Could Carry More Metadata
**File:** `MjmlRenderResult.java`

The result record only has `html` and `title`. Potentially useful additions:
- `previewText` — extracted from `mj-preview`
- `fonts` — list of fonts used (for CSP headers)
- `warnings` — non-fatal issues encountered during rendering

**Recommendation:** Add at least `previewText` to the result since it's already computed.

#### 6. `ComponentFactory` Leaks Internal Types
**File:** `ComponentFactory.java`

The `create(MjmlNode, GlobalContext, RenderContext)` signature forces custom component authors to depend on 3 internal types. If these change, all custom components break.

**Recommendation:** Consider a `ComponentContext` facade that wraps these three, providing only the methods custom components actually need (attribute resolution, container width, etc.). This would insulate custom components from internal refactors.

### Medium (Worth Considering)

#### 7. No Validation-Only Mode
Users may want to validate MJML without rendering (for editors, CI checks, etc.). Currently they must call `render()` and discard the output.

**Recommendation:** Add `MjmlRenderer.validate(String mjml)` returning validation results.

#### 8. Configuration is Not Copyable
**File:** `MjmlConfiguration.java`

There's no `toBuilder()` method. Users who want to create variations of an existing config must rebuild from scratch.

**Recommendation:** Add `MjmlConfiguration.toBuilder()` that pre-populates the builder with current values.

#### 9. `GlobalContext` Has 40+ Public Methods
**File:** `GlobalContext.java`

This class is a "god object" for document state. While the Javadoc acknowledges this and proposes a decomposition, the current API makes it confusing for custom component authors — they receive a `GlobalContext` but should only read from it, not write.

**Recommendation:** As noted in the class Javadoc, decompose into focused sub-contexts, or at minimum provide a read-only view to custom components.

#### 10. `language` Defaults to `"und"` (Undetermined)
**File:** `MjmlConfiguration.java:85`

While technically correct per BCP 47, `"und"` in the `lang` attribute is unusual and might confuse users inspecting output. Most email templates target `"en"`.

**Recommendation:** Document the default prominently and consider whether `"en"` is a more pragmatic default, or keep `"und"` but explain in Javadoc why.

#### 11. No Classpath-Based Include Resolver
Only `FileSystemIncludeResolver` is provided. Many Java applications load templates from the classpath (JARs). Users must implement `IncludeResolver` themselves.

**Recommendation:** Add `ClasspathIncludeResolver` as a built-in.

### Low (Nice-to-Have)

#### 12. Builder Doesn't Validate Input
**File:** `MjmlConfiguration.java` (Builder)

- `maxInputSize(-1)` is accepted silently
- `maxNestingDepth(0)` would cause immediate failure
- `language(null)` would cause NPE downstream

**Recommendation:** Add validation in `build()` or in setters.

#### 13. Thread Safety Documented but Not Enforced
**File:** `MjmlRenderer.java`

Thread safety is documented in Javadoc (good!), but `MjmlConfiguration` is not declared `final`/records — its mutability depends on convention, not enforcement. The class itself is technically immutable via private constructor + builder, but a subclass could add mutable state.

**Recommendation:** Make `MjmlConfiguration` `final` (it already has a private constructor, so non-subclassable from outside the package, but `final` makes intent explicit).

**Note:** `MjmlConfiguration` is already effectively final (package-private constructor), but the class declaration lacks the `final` keyword.

#### 14. No `toString()` on Configuration
`MjmlConfiguration` has no `toString()`, making debugging harder. Users logging their config see the default `Object.toString()`.

**Recommendation:** Add `toString()` with the configuration values.

#### 15. Inconsistent Return Types: `String` vs `MjmlRenderResult`
**File:** `MjmlRenderer.java`

`render(String)` returns `String`, `render(String, MjmlConfiguration)` returns `MjmlRenderResult`. This dual-return style is pragmatic but mildly surprising — some users will expect both to return the same type.

**Recommendation:** This is a reasonable design trade-off. Keep it but ensure Javadoc clearly explains the difference.

---

## Comparison with Industry Best Practices

### vs. Thymeleaf (Java Template Engine)
| Aspect | mjml-java | Thymeleaf |
|--------|-----------|-----------|
| Entry point | Static methods (simple) | Engine + Context objects (complex) |
| Configuration | Builder pattern | Engine properties |
| Template loading | String only | Template resolvers (classpath, file, string) |
| Exception hierarchy | Flat (1 class) | Rich (10+ classes) |
| Result type | String/Record | String |
| Thread safety | Documented, stateless | Thread-safe engine |

**Takeaway:** mjml-java's entry point is simpler (better for its narrower scope), but Thymeleaf's template resolver system and exception hierarchy are more mature.

### vs. FreeMarker (Java Template Engine)
| Aspect | mjml-java | FreeMarker |
|--------|-----------|------------|
| Configuration | Immutable builder | Mutable Configuration object |
| Instantiation | Static methods | Instance-based |
| Dependencies | Zero | Zero (similar) |

**Takeaway:** mjml-java's immutable configuration is more modern and safer than FreeMarker's mutable approach.

### vs. Node.js MJML (Reference Implementation)
| Aspect | mjml-java | mjml (Node.js) |
|--------|-----------|----------------|
| Basic usage | `MjmlRenderer.render(mjml)` | `mjml2html(mjml)` |
| Options | Builder pattern | Options object |
| Result | `MjmlRenderResult` record | `{ html, errors }` |
| Custom components | Extend `BodyComponent` | Register component class |
| Validation mode | Not available | `validationLevel` option |

**Takeaway:** mjml-java closely mirrors the Node.js API's simplicity, which is excellent. The missing validation mode and error collection in results are the main gaps.

---

## Specific Improvement Recommendations

### Priority 1: Tighten Module Exports (Pre-1.0)

```java
module dev.jcputney.mjml {
    requires java.xml;
    requires java.logging;

    exports dev.jcputney.mjml;
    exports dev.jcputney.mjml.component;
    // Remove: exports dev.jcputney.mjml.css;
    // Remove: exports dev.jcputney.mjml.context;
    // Remove: exports dev.jcputney.mjml.parser;
    // Remove: exports dev.jcputney.mjml.util;
}
```

**Problem:** Custom components currently need `MjmlNode` (parser), `GlobalContext` (context), and `RenderContext` (context). These types flow through `ComponentFactory` and `BodyComponent`.

**Solution options:**
1. **Qualified exports:** `exports dev.jcputney.mjml.context to dev.jcputney.mjml.component;` — but this doesn't help third-party custom components.
2. **SPI package:** Create `dev.jcputney.mjml.spi` with only the types custom components need, and export only that.
3. **Accept the leak for now:** Export `context` and `parser` but not `css` and `util`. Document that only `MjmlNode`, `GlobalContext`, and `RenderContext` are stable API.

Recommendation: Option 3 is most pragmatic for v1.0. Remove `css` and `util` exports immediately. Add `@ApiStatus.Internal` or equivalent Javadoc annotations to classes in `context` and `parser` that aren't meant for external use.

### Priority 2: Exception Hierarchy

```java
public class MjmlException extends RuntimeException { ... }
public class MjmlParseException extends MjmlException { ... }
public class MjmlIncludeException extends MjmlException { ... }
public class MjmlValidationException extends MjmlException { ... }
```

### Priority 3: Convenience Render Methods

```java
public static String render(Path mjmlFile) { ... }
public static MjmlRenderResult render(Path mjmlFile, MjmlConfiguration config) { ... }
```

### Priority 4: Enrich `MjmlRenderResult`

```java
public record MjmlRenderResult(
    String html,
    String title,
    String previewText,
    List<String> warnings
) { }
```

### Priority 5: Builder Validation

```java
public MjmlConfiguration build() {
    if (maxInputSize <= 0) throw new IllegalArgumentException("maxInputSize must be positive");
    if (maxNestingDepth <= 0) throw new IllegalArgumentException("maxNestingDepth must be positive");
    return new MjmlConfiguration(this);
}
```

---

## Artifact & Dependency Analysis

### Maven Coordinates
```xml
<groupId>dev.jcputney</groupId>
<artifactId>mjml-java</artifactId>
<version>1.0.0-SNAPSHOT</version>
```

**Assessment:** Good. The group ID follows reverse-domain convention, the artifact ID is clear and searchable.

### Dependencies
- **Runtime:** Zero (only `java.xml` and `java.logging` from the JDK)
- **Test:** JUnit Jupiter 5.10.2

**Assessment: Excellent.** Zero external runtime dependencies is a significant competitive advantage. Consumers won't face version conflicts or transitive dependency bloat.

### Missing POM Elements (for Maven Central)
- `<scm>` — source control metadata
- `<developers>` — developer info
- `<distributionManagement>` — deployment config
- `<issueManagement>` — issue tracker link

These are required for Maven Central publication.

---

## Summary of Recommendations by Severity

| # | Severity | Recommendation | Effort |
|---|----------|---------------|--------|
| 1 | Critical | Tighten module exports (remove `css`, `util`) | Low |
| 2 | Critical | Add exception subclasses | Medium |
| 3 | High | Add `render(Path)` convenience methods | Low |
| 4 | High | Default `sanitizeOutput` to `true` | Low |
| 5 | High | Enrich `MjmlRenderResult` with `previewText` | Low |
| 6 | High | Create `ComponentContext` facade for SPI | High |
| 7 | Medium | Add validation-only mode | Medium |
| 8 | Medium | Add `toBuilder()` to configuration | Low |
| 9 | Medium | Decompose `GlobalContext` | High |
| 10 | Medium | Document `"und"` default language choice | Low |
| 11 | Medium | Add `ClasspathIncludeResolver` | Low |
| 12 | Low | Validate builder inputs | Low |
| 13 | Low | Make `MjmlConfiguration` explicitly `final` | Trivial |
| 14 | Low | Add `toString()` to `MjmlConfiguration` | Trivial |
| 15 | Low | Document dual-return convention | Trivial |
