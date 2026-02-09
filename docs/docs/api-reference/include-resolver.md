---
sidebar_position: 4
title: IncludeResolver
---

# IncludeResolver

`dev.jcputney.mjml.IncludeResolver` is a functional interface for resolving `<mj-include>` paths to their content. mjml-java ships with one implementation (`FileSystemIncludeResolver`) and makes it straightforward to write your own.

## Interface

```java
public interface IncludeResolver {
    String resolve(String path);
}
```

**Parameters:**
- `path` -- the path from the `<mj-include>` element's `path` attribute

**Returns:** the resolved content as a string

**Throws:** `MjmlIncludeException` if the path cannot be resolved

## FileSystemIncludeResolver

The built-in implementation resolves paths relative to a base directory on the file system.

```java
import dev.jcputney.mjml.FileSystemIncludeResolver;

IncludeResolver resolver = new FileSystemIncludeResolver(Path.of("/templates"));

MjmlConfiguration config = MjmlConfiguration.builder()
    .includeResolver(resolver)
    .build();
```

Given the MJML:

```xml
<mj-include path="./partials/header.mjml" />
```

The resolver will read `/templates/partials/header.mjml`.

### Path Traversal Protection

`FileSystemIncludeResolver` normalizes the resolved path and verifies it stays within the base directory. Attempts to escape via `../` are rejected:

```java
// This include would resolve to /etc/passwd -- rejected
// Throws MjmlIncludeException: "Include path escapes base directory"
```

```xml
<!-- Blocked by path traversal protection -->
<mj-include path="../../../etc/passwd" />
```

### Error Cases

| Condition | Exception |
|-----------|-----------|
| Path is null or blank | `MjmlIncludeException: "Include path cannot be empty"` |
| Path escapes base directory | `MjmlIncludeException: "Include path escapes base directory"` |
| File does not exist | `MjmlIncludeException: "Include file not found: ..."` |
| I/O error reading file | `MjmlIncludeException: "Failed to read include file: ..."` |

## Custom Resolvers

Since `IncludeResolver` is a functional interface, you can implement it with a lambda or method reference.

### Classpath Resolver

Load templates from the classpath (e.g., bundled in a JAR):

```java
IncludeResolver classpathResolver = path -> {
    InputStream is = getClass().getResourceAsStream("/mjml-templates/" + path);
    if (is == null) {
        throw new MjmlIncludeException("Template not found on classpath: " + path);
    }
    try (is) {
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
        throw new MjmlIncludeException("Failed to read classpath template: " + path, e);
    }
};

MjmlConfiguration config = MjmlConfiguration.builder()
    .includeResolver(classpathResolver)
    .build();
```

### HTTP Resolver

:::warning SSRF Risk
An HTTP-based resolver is vulnerable to Server-Side Request Forgery (SSRF) attacks. If an attacker controls the MJML input, they could use `<mj-include>` to probe internal network resources. Always validate and restrict allowed hosts/URLs.
:::

```java
IncludeResolver httpResolver = path -> {
    URI uri = URI.create(path);

    // Validate against an allowlist
    if (!ALLOWED_HOSTS.contains(uri.getHost())) {
        throw new MjmlIncludeException("Host not allowed: " + uri.getHost());
    }

    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
    try {
        HttpResponse<String> response =
            client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new MjmlIncludeException("HTTP " + response.statusCode() + " for: " + path);
        }
        return response.body();
    } catch (IOException | InterruptedException e) {
        throw new MjmlIncludeException("Failed to fetch: " + path, e);
    }
};
```

### No Include Support

If your templates do not use `<mj-include>`, you can omit the resolver entirely. Any `<mj-include>` in the input will throw an `MjmlIncludeException` at render time.

```java
// No includeResolver set -- mj-include will fail
MjmlConfiguration config = MjmlConfiguration.builder()
    .language("en")
    .build();
```
