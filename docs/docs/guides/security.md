---
sidebar_position: 4
title: Security
---

# Security

mjml-java includes several security mechanisms to protect against common attack vectors when rendering untrusted MJML input. All protections are enabled by default.

## Input Size Limits

The `maxInputSize` setting rejects MJML input that exceeds a character count threshold **before** any processing occurs. This prevents denial-of-service through extremely large inputs.

```java
// Default: 1,048,576 characters (approximately 1 MB for ASCII)
MjmlConfiguration config = MjmlConfiguration.builder()
    .maxInputSize(512_000)  // 512K characters
    .build();
```

Inputs exceeding the limit throw `MjmlValidationException`:

```java
try {
    MjmlRenderer.render(largeInput, config);
} catch (MjmlValidationException e) {
    // "Input size 2000000 exceeds maximum allowed size 512000"
}
```

The check runs at the very start of the rendering pipeline, so no parsing, preprocessing, or memory allocation occurs for oversized input.

## Nesting Depth Limits

The `maxNestingDepth` setting prevents stack overflow from deeply nested MJML elements. The parser tracks element depth and rejects documents that exceed the limit.

```java
// Default: 100 levels
MjmlConfiguration config = MjmlConfiguration.builder()
    .maxNestingDepth(50)
    .build();
```

Exceeding the depth limit throws `MjmlException` during parsing.

## Output Sanitization (XSS Prevention)

When `sanitizeOutput` is enabled (the default), attribute values in rendered HTML are escaped to prevent cross-site scripting (XSS) attacks. The following characters are escaped:

| Character | Escaped As |
|-----------|-----------|
| `&` | `&amp;` |
| `"` | `&quot;` |
| `<` | `&lt;` |
| `>` | `&gt;` |

### How It Works

The `HtmlEscaper` utility is applied in two places:

1. **`buildAttributes()`** -- escapes all attribute values when building HTML attribute strings
2. **`escapeAttr()`** -- escapes individual values interpolated directly into HTML

This prevents attribute injection attacks like:

```xml
<!-- Attacker input -->
<mj-image src='x" onload="alert(1)' />

<!-- With sanitization (safe) -->
<img src="x&quot; onload=&quot;alert(1)" />

<!-- Without sanitization (vulnerable) -->
<img src="x" onload="alert(1)" />
```

### Disabling Sanitization

Only disable sanitization if you fully control the MJML input and need raw attribute values in the output:

```java
MjmlConfiguration config = MjmlConfiguration.builder()
    .sanitizeOutput(false)  // Only for trusted input!
    .build();
```

:::warning
Disabling sanitization when rendering user-supplied MJML exposes your application to XSS attacks. Keep it enabled (the default) whenever MJML input comes from untrusted sources.
:::

## Path Traversal Protection

The built-in `FileSystemIncludeResolver` prevents path traversal attacks by ensuring resolved paths stay within the configured base directory:

```java
FileSystemIncludeResolver resolver =
    new FileSystemIncludeResolver(Path.of("/templates"));
```

The resolver normalizes paths and rejects any that escape the base directory:

```xml
<!-- Rejected: escapes base directory -->
<mj-include path="../../../etc/passwd" />
<mj-include path="partials/../../secret.txt" />

<!-- Allowed: stays within /templates -->
<mj-include path="partials/header.mjml" />
<mj-include path="styles/theme.css" />
```

Empty and blank paths are also rejected.

## SSRF Prevention

If you implement a custom `IncludeResolver` that fetches content over HTTP or other network protocols, be aware of Server-Side Request Forgery (SSRF) risks. An attacker who controls MJML input could use `mj-include` to:

- Probe internal network services
- Access cloud metadata endpoints (e.g., `http://169.254.169.254/`)
- Scan internal ports

### Mitigations for HTTP Resolvers

The `mjml-java-resolvers` module provides `UrlIncludeResolver` with built-in SSRF protection:

```java
import dev.jcputney.mjml.resolver.UrlIncludeResolver;

IncludeResolver resolver = UrlIncludeResolver.builder()
    .allowedHosts("cdn.example.com", "templates.example.com")
    .httpsOnly(true)
    .connectTimeout(Duration.ofSeconds(5))
    .readTimeout(Duration.ofSeconds(10))
    .maxResponseSize(1_048_576) // 1 MB
    .build();
```

`UrlIncludeResolver` automatically blocks loopback, site-local, and link-local IP addresses.

If you write a custom HTTP resolver instead, implement these safeguards:

```java
IncludeResolver safeHttpResolver = (path, context) -> {
    URI uri = URI.create(path);

    // 1. Allowlist: only permit known hosts
    if (!ALLOWED_HOSTS.contains(uri.getHost())) {
        throw new MjmlException("Host not allowed: " + uri.getHost());
    }

    // 2. Scheme restriction: HTTPS only
    if (!"https".equals(uri.getScheme())) {
        throw new MjmlException("Only HTTPS is allowed");
    }

    // 3. Block private/internal IP ranges
    InetAddress addr = InetAddress.getByName(uri.getHost());
    if (addr.isSiteLocalAddress() || addr.isLoopbackAddress()) {
        throw new MjmlException("Internal addresses not allowed");
    }

    // 4. Timeout to prevent slowloris
    HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();
    // ... fetch and return content
};
```

## XXE Prevention

mjml-java uses the built-in JDK XML parser (`javax.xml.parsers.DocumentBuilder`) for parsing MJML documents. The parser is configured with safe defaults that prevent XML External Entity (XXE) attacks. External entities and DTD processing are not enabled.

## Include Depth Limits

The include processor enforces a maximum recursion depth of 50 levels to prevent stack overflow from deeply nested or circular includes. Circular includes are detected by tracking visited paths:

```xml
<!-- a.mjml includes b.mjml, b.mjml includes a.mjml -->
<!-- Throws: "Circular include detected for path: a.mjml" -->
```

## CDATA Injection

The MJML preprocessor wraps content of certain tags (like `mj-text`, `mj-button`) in CDATA sections for XML parsing. The parser handles CDATA boundary sequences (`]]>`) within content safely, preventing CDATA injection attacks that could corrupt the document structure.

## Content Sanitization

By default, mjml-java passes through the inner HTML content of `<mj-text>`, `<mj-button>`, and `<mj-raw>` elements as-is, matching the official MJML behavior. This is safe when you control the MJML input, but **dangerous when rendering user-supplied content** -- an attacker could inject `<script>` tags or event handlers into the email body.

The `contentSanitizer` option lets you plug in an HTML sanitizer that is applied to inner content before rendering:

```java
// Using Jsoup (add org.jsoup:jsoup as a dependency)
MjmlConfiguration config = MjmlConfiguration.builder()
    .contentSanitizer(html -> Jsoup.clean(html, Safelist.basic()))
    .build();
```

The `ContentSanitizer` interface is a `@FunctionalInterface`:

```java
@FunctionalInterface
public interface ContentSanitizer {
    String sanitize(String html);
}
```

:::warning
If your MJML templates include any user-supplied content (e.g., user names, messages, dynamic text), always configure a `ContentSanitizer`. The built-in `sanitizeOutput` option only escapes attribute values -- it does **not** sanitize element content.
:::

## Security Configuration Summary

| Setting | Default | Description |
|---------|---------|-------------|
| `maxInputSize` | 1,048,576 (~1 MB) | Maximum input size in characters |
| `maxNestingDepth` | 100 | Maximum element nesting depth |
| `sanitizeOutput` | `true` | Escape HTML special characters in attribute values |
| `contentSanitizer` | `null` | Optional sanitizer for inner HTML of `mj-text`, `mj-button`, `mj-raw` |

All settings are configured through the builder:

```java
MjmlConfiguration config = MjmlConfiguration.builder()
    .maxInputSize(2_097_152)   // 2 MB
    .maxNestingDepth(50)
    .sanitizeOutput(true)
    .contentSanitizer(html -> Jsoup.clean(html, Safelist.basic()))
    .build();
```

The builder validates that `maxInputSize` and `maxNestingDepth` are positive integers, throwing `IllegalArgumentException` for zero or negative values.
