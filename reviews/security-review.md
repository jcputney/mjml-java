# Security Review: mjml-java

**Reviewer:** Security Review Agent (Opus)
**Date:** 2026-02-08
**Scope:** Full codebase security audit of the mjml-java MJML-to-HTML rendering library
**Commit:** 0af813f
**Files Reviewed:** 30+ source files across parser, renderer, CSS inliner, and component layers

---

## Executive Summary

The mjml-java codebase demonstrates **strong security awareness** in its highest-risk areas. The XML parser has robust XXE protections, the file include resolver has correct path traversal guards, include depth is bounded, CDATA injection is properly handled, input size limits exist, and nesting depth is enforced. The library also provides an opt-in `sanitizeOutput` mode for HTML attribute escaping and has a dedicated `SecurityTest` suite.

The remaining security gaps are **medium-to-low severity** and center on: (1) output escaping being opt-out by default, (2) some rendering paths bypassing the escaping mechanism, (3) font URL injection in the HTML skeleton, and (4) unescaped values in `mj-html-attributes` application.

**Overall Security Posture: GOOD — suitable for trusted input; needs minor hardening for untrusted input**

| Severity | Count |
|----------|-------|
| Critical | 0     |
| High     | 1     |
| Medium   | 4     |
| Low      | 4     |
| Info     | 5     |

---

## Findings

### HIGH-1: XSS via Direct String Interpolation in Component Rendering (Bypasses sanitizeOutput)

**Severity:** HIGH
**CWE:** CWE-79 (Improper Neutralization of Input During Web Page Generation)
**CVSS:** 7.1

**Description:** While the codebase has a `sanitizeOutput` configuration option and `buildAttributes()` respects it (`BodyComponent.java:71-80`), multiple components construct HTML output by directly interpolating attribute values via `StringBuilder.append()` rather than going through `buildAttributes()`. These direct interpolations bypass the `sanitizeOutput` escaping entirely.

**Affected Locations (direct string interpolation, not through buildAttributes):**

- `MjImage.java:78-79` — `alt`, `src` attributes directly appended:
  ```java
  img.append(" alt=\"").append(getAttribute("alt", "")).append("\"");
  img.append(" src=\"").append(getAttribute("src", "")).append("\"");
  ```
- `MjImage.java:85-86` — `srcset` attribute directly appended
- `MjImage.java:90` — `sizes` attribute directly appended
- `MjImage.java:97` — `title` attribute directly appended
- `MjImage.java:127` — `href` attribute on anchor tag directly appended
- `MjImage.java:132` — `target` attribute directly appended
- `MjButton.java:136` — `href` attribute directly appended
- `MjButton.java:142` — `target` attribute directly appended
- `MjSection.java:97-98` — `background-url` (`bgUrl`) directly appended as `background="..."` attribute
- `MjSection.java:107` — `css-class` attribute directly appended as `class="..."` attribute
- `MjSection.java:131` — `bgcolor` directly appended
- `MjNavbar.java:96` — generated `uniqueId` appended to `id` attribute (low risk — ID is generated, not user-controlled)
- `HtmlSkeleton.java:152-153` — font `href` directly appended into `<link>` and `@import`

**Attack Scenario:**
```xml
<mjml><mj-body><mj-section><mj-column>
  <mj-image src='x" onload="alert(1)' alt="test" />
</mj-column></mj-section></mj-body></mjml>
```
Even with `sanitizeOutput(true)`, this XSS payload passes through because MjImage uses direct string interpolation rather than `buildAttributes()`.

**Impact:** If the library processes untrusted MJML and the output is rendered in a browser or webmail client, an attacker can inject event handlers, break out of attributes, or inject arbitrary HTML. This is especially concerning because a security-conscious user who enables `sanitizeOutput(true)` would reasonably expect full protection but would not get it for these rendering paths.

**Remediation:**
1. Refactor all components to use `buildAttributes()` (or a similar escaping wrapper) for all HTML attribute output — never use direct string interpolation for user-controlled values.
2. Or, apply `HtmlEscaper.escapeAttributeValue()` consistently to all attribute values at the point of emission.
3. Consider making `sanitizeOutput(true)` the default, with a `sanitizeOutput(false)` opt-out for backward compatibility.

---

### MEDIUM-1: Font URL Injection in HtmlSkeleton

**Severity:** MEDIUM
**CWE:** CWE-79 (Cross-Site Scripting via Content Injection)
**CVSS:** 5.4

**Description:** Font URLs from `<mj-font>` elements are emitted directly into `<link href="...">` tags and CSS `@import url(...)` statements in `HtmlSkeleton.java` without any escaping or validation.

**Affected Location:** `HtmlSkeleton.java:152-158`
```java
for (FontDef font : ctx.getFonts()) {
    sb.append("  <link href=\"").append(font.href())
        .append("\" rel=\"stylesheet\" type=\"text/css\">\n");
}
// ...
for (FontDef font : ctx.getFonts()) {
    sb.append("    @import url(").append(font.href()).append(");\n");
}
```

**Attack Scenario:**
```xml
<mjml><mj-head>
  <mj-font name="Evil" href='"><script>alert(1)</script><link href="' />
</mj-head><mj-body>...</mj-body></mjml>
```
This breaks out of the `href` attribute and injects arbitrary HTML into the `<head>`.

**Impact:** XSS via the font import mechanism, or forcing the email client to load arbitrary external resources (CSS exfiltration, tracking pixels).

**Remediation:**
- Apply `escapeHtml()` (the method already exists in HtmlSkeleton) to `font.href()` before output.
- Optionally validate that font URLs are well-formed `https://` URLs.

---

### MEDIUM-2: mj-html-attributes Values Not Escaped

**Severity:** MEDIUM
**CWE:** CWE-79 (Cross-Site Scripting)
**CVSS:** 5.4

**Description:** The `applyHtmlAttributes()` method in `RenderPipeline.java:253-256` inserts attribute values from `mj-html-attributes` directly into the HTML output without escaping.

**Affected Location:** `RenderPipeline.java:254-256`
```java
for (Map.Entry<String, String> attr : attrs.entrySet()) {
    attrStr.append(' ').append(attr.getKey())
        .append("=\"").append(attr.getValue()).append('"');
}
```

**Attack Scenario:** If MJML input contains `<mj-html-attributes>` with attacker-controlled values, those values are inserted verbatim into the rendered HTML elements matched by the CSS selector.

**Impact:** Attribute injection in arbitrary elements of the rendered output.

**Remediation:**
- Apply `HtmlEscaper.escapeAttributeValue()` to `attr.getValue()` before insertion.
- The attribute *name* should also be validated (alphanumeric + hyphens only) to prevent attribute name injection.

---

### MEDIUM-3: IncludeResolver Interface Encourages SSRF-Vulnerable Implementations

**Severity:** MEDIUM
**CWE:** CWE-918 (Server-Side Request Forgery)
**CVSS:** 5.9

**Description:** The `IncludeResolver` interface Javadoc states: *"Implementations can resolve from the file system, classpath, HTTP, etc."* — actively suggesting HTTP-based implementations. While the provided `FileSystemIncludeResolver` is properly secured with path traversal prevention, a naive HTTP resolver would enable SSRF attacks through `<mj-include path="http://169.254.169.254/latest/meta-data/">`.

**Positive Note:** The interface *does* already include a security warning in its Javadoc (lines 8-10):
```java
 * <p><strong>Security warning:</strong> Implementations that resolve paths over HTTP or
 * other network protocols are vulnerable to Server-Side Request Forgery (SSRF) attacks.
```

**Impact:** Indirect — the risk materializes only if a consumer implements an HTTP-based resolver without proper URL validation.

**Remediation:**
- The existing Javadoc warning is good. Consider additionally:
  - Providing a secure `HttpIncludeResolver` reference implementation with URL allowlisting.
  - Adding validation guidance to the Javadoc (e.g., "restrict to specific domains" or "use allowlists").

---

### MEDIUM-4: Head Comment `--` Sanitization Is Weak

**Severity:** MEDIUM
**CWE:** CWE-79 (HTML Injection)
**CVSS:** 4.7

**Description:** `HtmlSkeleton.java:112` strips `--` sequences from head comments to prevent HTML comment injection:
```java
String safeComment = comment.replace("--", "");
```

However, this approach has two issues:
1. It replaces all `--` sequences, not just `-->`, which mutates legitimate content unnecessarily.
2. The replacement is applied after the XML parser has already processed the comment. The XML parser itself rejects `--` inside comments (per XML spec), so comment content reaching this point will never contain `--`. This means the sanitization, while well-intentioned, is redundant for normal flow — but it correctly guards against future changes that might introduce a different comment extraction path.

**Positive Note:** The `SecurityTest.java:59-83` includes a test for this case, demonstrating security awareness.

**Impact:** Low practical impact because the XML parser already rejects `--` in comments. The risk is if a future code change introduces a path that bypasses XML parsing for comments.

**Remediation:**
- The current approach is adequate as defense-in-depth. For completeness, also strip `>` after any remaining `--` pair, though this is unlikely to be needed given the XML parser's strict comment handling.

---

### LOW-1: Error Messages May Leak Internal Paths

**Severity:** LOW
**CWE:** CWE-209 (Generation of Error Message Containing Sensitive Information)
**CVSS:** 2.7

**Description:** Several error messages include internal file paths or input details that could reveal server filesystem structure:

- `FileSystemIncludeResolver.java:41`: `"Include path escapes base directory"` (does not leak the path — good)
- `FileSystemIncludeResolver.java:44`: `"Include file not found: " + path` (leaks the requested path)
- `MjmlParser.java:65`: `"Failed to parse MJML: " + e.getMessage()` (may expose XML parser internals)
- `IncludeProcessor.java:71`: `"Circular include detected for path: " + path` (exposes the path)

**Positive Note:** `FileSystemIncludeResolver.java:41` correctly does NOT include the resolved absolute path in the error message, only the user-supplied relative path at line 44.

**Impact:** In a web application, these could leak partial filesystem structure to attackers.

**Remediation:**
- Use generic error messages in exceptions; log details at DEBUG level.

---

### LOW-2: No Validation of CSS-Class Attribute Values

**Severity:** LOW
**CWE:** CWE-79
**CVSS:** 2.5

**Description:** The `css-class` attribute from MJML elements is output directly as a `class="..."` attribute in `MjSection.java:107`:
```java
sb.append(" class=\"").append(cssClass).append("\"");
```

A value like `foo" onclick="alert(1)` would break out of the class attribute.

**Impact:** Low — this requires the attacker to control the `css-class` attribute in the MJML source, which implies they already control the template (at which point they have broader XSS vectors).

**Remediation:**
- Validate that `css-class` values contain only valid CSS class characters (alphanumeric, hyphens, underscores).
- Or apply attribute escaping.

---

### LOW-3: MjmlNode.getOuterHtml() Does Not Escape Attribute Values

**Severity:** LOW
**CWE:** CWE-79
**CVSS:** 2.3

**Description:** `MjmlNode.getOuterHtml()` at line 145 outputs attribute values without escaping:
```java
sb.append(' ').append(attr.getKey()).append("=\"").append(attr.getValue()).append('"');
```

This method is used primarily for serializing CDATA-wrapped content back to HTML (i.e., content inside `mj-text`, `mj-button`, etc.), where the content is intentionally raw HTML. However, it could become a vector if used in other contexts.

**Impact:** Low — the method is used for pass-through HTML content where escaping would be incorrect.

**Remediation:**
- Document that this method is for internal serialization of HTML content and should not be used for untrusted attribute values.
- The current behavior is actually correct for its use case (HTML pass-through).

---

### LOW-4: Regex Patterns in Preprocessor Are Correctly Pre-compiled

**Severity:** LOW (Informational — Positive Finding)

**Description:** The existing review noted regex compilation as an issue, but the current code at `MjmlPreprocessor.java:40-52` correctly pre-compiles all patterns in a static initializer block:
```java
private static final Map<String, Pattern> TAG_PATTERNS = new LinkedHashMap<>();
static {
    for (String tag : ENDING_TAGS) {
        Pattern pattern = Pattern.compile(..., Pattern.DOTALL);
        TAG_PATTERNS.put(tag, pattern);
    }
}
```

**ReDoS Assessment:** The pattern `(<tagName(\s[^>]*)?(?<!/)>)(.*?)(</tagName\s*>)` uses:
- `(\s[^>]*)?` — bounded by `>`, cannot backtrack catastrophically
- `(.*?)` with `DOTALL` — non-greedy, bounded by the specific closing tag
- **Verdict: No ReDoS vulnerability.** The patterns are safe.

---

## Positive Security Findings

### INFO-1: XXE Prevention Is Correct and Complete

**File:** `MjmlParser.java:43-49`

The XML parser correctly disables all three external entity features:
```java
factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
```

This prevents XXE attacks, billion laughs entity expansion, and DTD-based attacks. The configuration matches OWASP recommendations.

---

### INFO-2: Path Traversal Protection Is Correct

**File:** `FileSystemIncludeResolver.java:27-41`

```java
this.baseDir = baseDir.toAbsolutePath().normalize();
// ...
Path resolved = baseDir.resolve(path).normalize();
if (!resolved.startsWith(baseDir)) {
    throw new MjmlException("Include path escapes base directory");
}
```

The `normalize()` before `startsWith()` correctly prevents `../` bypass attacks. Both the base directory and resolved path are normalized.

---

### INFO-3: Include Cycle Detection and Depth Limiting Are Correct

**File:** `IncludeProcessor.java:27,59-79`

- `MAX_INCLUDE_DEPTH = 50` prevents runaway recursion
- `visitedPaths` Set prevents circular includes
- The visited set is correctly **copied** per branch (`new HashSet<>(visitedPaths)`) at line 78, preventing cross-branch pollution

---

### INFO-4: CDATA Injection Is Properly Handled

**File:** `MjmlPreprocessor.java:128-131`

The preprocessor correctly escapes `]]>` sequences before CDATA wrapping:
```java
String safeContent = content.replace("]]>", "]]]]><![CDATA[>");
String replacement = openTag + "<![CDATA[" + safeContent + "]]>" + closeTag;
```

This is the standard CDATA-safe encoding and prevents CDATA breakout injection. The `SecurityTest.java:19-55` includes tests for this specific scenario.

---

### INFO-5: Input Size and Nesting Depth Limits Exist

**Files:** `MjmlConfiguration.java:14-15,60-70`, `RenderPipeline.java:92-96`, `MjmlParser.java:69,76-77`

- **Input size limit:** `DEFAULT_MAX_INPUT_SIZE = 1_048_576` (1 MB), enforced in `RenderPipeline.render()`:
  ```java
  if (mjmlSource != null && mjmlSource.length() > maxSize) {
      throw new MjmlException("Input size ... exceeds maximum allowed size " + maxSize);
  }
  ```
- **Nesting depth limit:** `DEFAULT_MAX_NESTING_DEPTH = 100` in configuration, with parser enforcement:
  ```java
  if (depth > DEFAULT_MAX_DEPTH) {
      throw new MjmlException("Maximum nesting depth exceeded (" + DEFAULT_MAX_DEPTH + ")");
  }
  ```
- Both limits are configurable via `MjmlConfiguration.Builder`.
- `SecurityTest.java:89-151` tests both limits.

---

### INFO-6: HtmlEscaper Utility and sanitizeOutput Mode Exist

**Files:** `HtmlEscaper.java`, `BodyComponent.java:71-80`, `MjmlConfiguration.java:52-54`

The codebase has a proper HTML attribute escaping utility (`HtmlEscaper.escapeAttributeValue()`) and a configuration option (`sanitizeOutput`) that enables escaping in `buildAttributes()`. The issue (HIGH-1) is that not all rendering paths use `buildAttributes()`.

---

### INFO-7: CSS and Selector Parsing Use Hand-Written Parsers (No Regex-Based ReDoS)

**Files:** `CssParser.java`, `CssSelectorParser.java`

Both the CSS parser and CSS selector parser are hand-written recursive-descent parsers that iterate character-by-character. They do not use regex at all, eliminating ReDoS risk in these components. The CSS parser correctly handles nested braces, quoted strings, and at-rules.

---

### INFO-8: EntityTable Uses Efficient Single-Pass Replacement

**File:** `EntityTable.java:175-209`

The entity replacement is implemented as a single-pass O(n) algorithm that scans for `&` characters and performs hash map lookups. This is efficient and not vulnerable to algorithmic complexity attacks.

---

## Threat Model Summary

| Threat | Mitigation Status |
|--------|------------------|
| XXE (XML External Entity) | **MITIGATED** — Parser features correctly disabled |
| Billion Laughs DoS | **MITIGATED** — External entities disabled |
| Path Traversal (includes) | **MITIGATED** — normalize + startsWith check |
| Include Cycles | **MITIGATED** — Cycle detection + depth limit |
| Input Size DoS | **MITIGATED** — Configurable max input size (default 1MB) |
| Nesting Depth DoS | **MITIGATED** — Configurable max depth (default 100) |
| CDATA Injection | **MITIGATED** — `]]>` properly escaped before wrapping |
| SSRF (includes) | **PARTIALLY MITIGATED** — FileSystem resolver safe; interface warns about HTTP resolvers |
| XSS (output injection) | **PARTIALLY MITIGATED** — `sanitizeOutput` mode exists but not all rendering paths use it |
| Font URL Injection | **NOT MITIGATED** — Font URLs emitted without escaping |
| mj-html-attributes Injection | **NOT MITIGATED** — Values inserted without escaping |
| ReDoS | **NOT VULNERABLE** — All parsers use bounded patterns or hand-written tokenizers |
| CSS Injection | **BY DESIGN** — CSS is intentionally passed through (consistent with MJML behavior) |

---

## Recommendations by Priority

### Must Fix (for untrusted input scenarios)
1. **Ensure all HTML attribute output uses escaping** — refactor components to route all attribute values through `buildAttributes()` or `HtmlEscaper.escapeAttributeValue()` (HIGH-1)
2. **Escape font URLs** in HtmlSkeleton before emitting into `<link>` and `@import` (MEDIUM-1)
3. **Escape mj-html-attributes values** before insertion into rendered HTML (MEDIUM-2)

### Should Fix
4. **Add SSRF guidance** — enhance IncludeResolver Javadoc with concrete validation patterns (MEDIUM-3)
5. **Consider making `sanitizeOutput(true)` the default** — the current `false` default means users must opt in to security (HIGH-1)
6. **Sanitize error messages** to avoid path leakage in production (LOW-1)

### Nice to Have
7. **Validate css-class attribute values** against a safe character set (LOW-2)
8. **Document the trust model** clearly in public API Javadoc — state explicitly whether MJML input is expected to be trusted or untrusted (LOW-3)

---

## Comparison with Previous Review

This review corrects several findings from the earlier security review:

| Previous Finding | Status | Correction |
|-----------------|--------|------------|
| HIGH-2: CDATA injection (]]> not escaped) | **Already fixed** | Code at `MjmlPreprocessor.java:129` correctly escapes `]]>` as `]]]]><![CDATA[>` |
| MEDIUM-1: No input size limits | **Already fixed** | `MjmlConfiguration.DEFAULT_MAX_INPUT_SIZE = 1_048_576` enforced in `RenderPipeline.render()` |
| MEDIUM-2: No nesting depth limit | **Already fixed** | `MjmlParser.convertElement()` enforces `DEFAULT_MAX_DEPTH = 100` |
| LOW-1: Regex not pre-compiled | **Already fixed** | `MjmlPreprocessor.TAG_PATTERNS` is a static pre-compiled map |
| LOW-2: EntityTable quadratic complexity | **Already fixed** | `EntityTable.replaceEntities()` is single-pass O(n) with hash map lookups |

These were likely addressed between the previous review and the current code state. The remaining gap is **output escaping coverage** — the `sanitizeOutput` mechanism exists but doesn't cover all rendering paths.

---

## Conclusion

The mjml-java library has **strong defensive foundations**: XXE prevention, path traversal protection, cycle detection, CDATA injection handling, input size limits, and nesting depth limits are all correctly implemented. The codebase also demonstrates good security testing practices with a dedicated `SecurityTest` suite.

The primary remaining gap is **inconsistent output escaping**: the `sanitizeOutput` configuration option and `HtmlEscaper` utility exist, but several component rendering paths bypass them by using direct string interpolation. For **trusted template** use (the typical MJML use case), the current posture is good. For **untrusted input** processing, the output escaping paths should be unified to ensure `sanitizeOutput(true)` provides comprehensive protection.
