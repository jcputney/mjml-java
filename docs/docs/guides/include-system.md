---
sidebar_position: 2
title: Include System
---

# Include System

mjml-java supports `mj-include` for splitting MJML templates across multiple files. The include system resolves external content during rendering, letting you reuse headers, footers, styles, and other template fragments.

## Basic Usage

Use the `mj-include` tag with a `path` attribute to include external content:

```xml
<mjml>
  <mj-head>
    <mj-include path="partials/styles.mjml" />
  </mj-head>
  <mj-body>
    <mj-include path="partials/header.mjml" />
    <mj-section>
      <mj-column>
        <mj-text>Main content</mj-text>
      </mj-column>
    </mj-section>
    <mj-include path="partials/footer.mjml" />
  </mj-body>
</mjml>
```

## Include Types

The `type` attribute controls how the included content is interpreted. There are four include types (`mjml`, `html`, `css`, and `css-inline`):

### MJML Includes (default)

When `type` is omitted or set to `"mjml"`, the included file is parsed as MJML. The children of the included content replace the `mj-include` element.

```xml
<mj-include path="partials/header.mjml" />
```

The included file can be a full MJML document or a fragment:

```xml
<!-- partials/header.mjml (fragment) -->
<mj-section>
  <mj-column>
    <mj-image src="logo.png" />
  </mj-column>
</mj-section>
```

If the included file is a full `<mjml>` document, mjml-java extracts the appropriate section: head children are used when the include is inside `mj-head`, and body children are used otherwise.

### HTML Includes

Set `type="html"` to include raw HTML. The content is wrapped in an `mj-raw` element:

```xml
<mj-include path="partials/tracking-pixel.html" type="html" />
```

### CSS Includes

Set `type="css"` to include a CSS stylesheet. The content becomes an `mj-style` element:

```xml
<mj-include path="styles/theme.css" type="css" />
```

For inline CSS (applied directly to matching elements via the CSS inliner), add the `css-inline` attribute:

```xml
<mj-include path="styles/inline.css" type="css" css-inline="inline" />
```

## Configuring the Include Resolver

Includes require an `IncludeResolver` to be configured. Without one, `mj-include` tags are left unresolved and a warning is logged during rendering.

### FileSystemIncludeResolver

The built-in `FileSystemIncludeResolver` reads files from a base directory:

```java
import dev.jcputney.mjml.FileSystemIncludeResolver;

MjmlConfiguration config = MjmlConfiguration.builder()
    .includeResolver(new FileSystemIncludeResolver(Path.of("/templates")))
    .build();

String mjml = Files.readString(Path.of("/templates/email.mjml"));
String html = MjmlRenderer.render(mjml, config).html();
```

Paths in `mj-include` are resolved relative to the base directory. For example, if the base directory is `/templates` and the include path is `partials/header.mjml`, the resolver reads `/templates/partials/header.mjml`.

### Path Traversal Protection

`FileSystemIncludeResolver` prevents path traversal attacks. Any resolved path that escapes the base directory is rejected:

```xml
<!-- These are rejected with MjmlIncludeException -->
<mj-include path="../../../etc/passwd" />
<mj-include path="partials/../../secret.txt" />
```

The resolver normalizes paths before checking, so encoded traversal sequences are also caught.

## Custom Resolvers

Implement the `IncludeResolver` interface to load templates from any source:

```java
import dev.jcputney.mjml.IncludeResolver;
import dev.jcputney.mjml.ResolverContext;

@FunctionalInterface
public interface IncludeResolver {
    String resolve(String path, ResolverContext context);
}
```

The `ResolverContext` provides metadata about the include chain: the including file path, include type (`"mjml"`, `"html"`, `"css"`, `"css-inline"`), and nesting depth.

### Classpath Resolver

Load templates from the Java classpath (useful for bundled application templates):

```java
IncludeResolver classpathResolver = (path, context) -> {
    try (InputStream is = getClass().getResourceAsStream("/templates/" + path)) {
        if (is == null) {
            throw new MjmlException("Template not found: " + path);
        }
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
        throw new MjmlException("Failed to read template: " + path, e);
    }
};

MjmlConfiguration config = MjmlConfiguration.builder()
    .includeResolver(classpathResolver)
    .build();
```

### Database Resolver

Load templates stored in a database:

```java
IncludeResolver dbResolver = (path, context) -> {
    String content = templateRepository.findByPath(path);
    if (content == null) {
        throw new MjmlException("Template not found: " + path);
    }
    return content;
};
```

### HTTP Resolver (with Security Caveats)

:::warning SSRF Risk
An HTTP-based resolver is vulnerable to **Server-Side Request Forgery (SSRF)**. If an attacker controls the MJML input, they can use `mj-include` paths to probe internal network resources. Only use HTTP resolvers with trusted input, and validate/restrict allowed hosts. Consider using the built-in `UrlIncludeResolver` from the `mjml-java-resolvers` module, which includes SSRF protection out of the box.

For hostname URLs (for example `https://cdn.example.com/template.mjml`), configure an explicit host allowlist with `UrlIncludeResolver.builder().allowedHosts(...)`.
:::

```java
IncludeResolver httpResolver = (path, context) -> {
    // Validate the URL against an allowlist
    URI uri = URI.create(path);
    if (!ALLOWED_HOSTS.contains(uri.getHost())) {
        throw new MjmlException("Host not allowed: " + uri.getHost());
    }

    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
        .uri(uri)
        .timeout(Duration.ofSeconds(5))
        .build();
    HttpResponse<String> response = client.send(request,
        HttpResponse.BodyHandlers.ofString());
    return response.body();
};
```

## Nested Includes

Included files can themselves contain `mj-include` tags. The processor resolves includes recursively up to a maximum depth of 50 levels (configurable via `MjmlConfiguration.builder().maxIncludeDepth()`).

```xml
<!-- layout.mjml -->
<mj-section>
  <mj-include path="partials/nav.mjml" />
</mj-section>

<!-- partials/nav.mjml -->
<mj-column>
  <mj-include path="partials/nav-links.mjml" />
</mj-column>
```

### Circular Include Detection

The processor tracks visited paths and throws `MjmlIncludeException` if a circular include is detected:

```xml
<!-- a.mjml includes b.mjml, which includes a.mjml -->
<!-- Throws: "Circular include detected for path: a.mjml" -->
```

## Processing Order

Include resolution is phase 3 of the 7-phase rendering pipeline:

1. Preprocess (CDATA wrapping)
2. Parse (XML to node tree)
3. **Resolve includes** -- `mj-include` elements are replaced with resolved content
4. Process head (fonts, styles, attributes)
5. Resolve attributes (cascade applied during rendering)
6. Render body
7. Assemble skeleton + CSS inlining

This means included content is fully integrated into the document tree before head processing and attribute resolution occur. Included `mj-attributes`, `mj-font`, and `mj-style` elements work exactly as if they were written inline.
