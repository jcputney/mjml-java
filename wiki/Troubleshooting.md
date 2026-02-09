# Troubleshooting

Common issues and their solutions when using mjml-java.

## MjmlValidationException: Input size exceeds maximum

**Symptom:**
```
MjmlValidationException: Input size 2097153 exceeds maximum allowed size 1048576
```

**Cause:** The MJML input string exceeds the default 1 MB limit.

**Solution:** Increase `maxInputSize` in your configuration:

```java
MjmlConfiguration config = MjmlConfiguration.builder()
    .maxInputSize(5_242_880) // 5 MB
    .build();

MjmlRenderResult result = MjmlRenderer.render(mjml, config);
```

## MjmlParseException: Malformed MJML

**Symptom:**
```
MjmlParseException: Failed to parse MJML input
```

**Cause:** The MJML input is not valid XML. Common issues include:
- Unclosed tags
- Mismatched opening/closing tags
- Unescaped `&` characters in text content (use `&amp;`)
- Unescaped `<` or `>` in attribute values

**Solution:** Validate your MJML is well-formed XML. MJML components like `mj-text` and `mj-button` can contain HTML, which mjml-java handles via CDATA preprocessing, but the outer MJML structure must be valid XML.

```xml
<!-- Correct -->
<mj-text>Tom &amp; Jerry</mj-text>

<!-- Incorrect - will cause parse error -->
<mj-text>Tom & Jerry</mj-text>
```

## MjmlIncludeException: File not found

**Symptom:**
```
MjmlIncludeException: Include file not found: ./header.mjml
```

**Cause:** The `mj-include` path cannot be resolved.

**Solution:**
1. Ensure you have configured an `IncludeResolver`:
   ```java
   MjmlConfiguration config = MjmlConfiguration.builder()
       .includeResolver(new FileSystemIncludeResolver(Path.of("/templates")))
       .build();
   ```
2. Verify the path is relative to the base directory
3. Check that the file exists at the expected location

## MjmlIncludeException: Include path escapes base directory

**Symptom:**
```
MjmlIncludeException: Include path escapes base directory
```

**Cause:** The include path uses `../` to traverse outside the configured base directory. This is blocked by the `FileSystemIncludeResolver` as a security measure.

**Solution:** Ensure all included files are within the base directory, or use an absolute path as the base directory that encompasses all template files.

## Module not found: dev.jcputney.mjml

**Symptom:**
```
error: module not found: dev.jcputney.mjml
```

**Cause:** You are using JPMS (Java Platform Module System) and haven't declared the dependency.

**Solution:** Add the `requires` directive to your `module-info.java`:

```java
module my.app {
    requires dev.jcputney.mjml;
}
```

If you also need the CSS inliner directly:

```java
module my.app {
    requires dev.jcputney.mjml;
    // CssInliner is in the dev.jcputney.mjml.css package,
    // which is exported by the module
}
```

## Output differs from official MJML

**Symptom:** The HTML output does not match what https://mjml.io/try-it-live produces.

**Possible causes:**

1. **MJML version mismatch:** mjml-java targets MJML v4. The online editor may use a different version.
2. **CSS inlining differences:** mjml-java uses its own CSS inlining engine, which may produce slightly different output than the `juice` npm package used by the official toolchain.
3. **Whitespace differences:** Minor whitespace differences in the output are cosmetic and do not affect email rendering.

**Solution:** If you encounter a significant rendering difference, please file an issue with the MJML input and both outputs (mjml-java and official MJML).

## Empty output / no body rendered

**Symptom:** The rendered HTML contains the skeleton (`<html>`, `<head>`, `<body>`) but no email content.

**Cause:** The MJML input is missing the `<mj-body>` element or the body has no child sections.

**Solution:** Ensure your MJML has the required structure:

```xml
<mjml>
  <mj-body>
    <mj-section>
      <mj-column>
        <mj-text>Content here</mj-text>
      </mj-column>
    </mj-section>
  </mj-body>
</mjml>
```

## Thread safety concerns

**Question:** Is `MjmlRenderer.render()` safe to call from multiple threads?

**Answer:** Yes. The `render()` methods are fully thread-safe. Each call creates its own internal `RenderPipeline` and `GlobalContext`, so concurrent calls do not share mutable state. The `MjmlConfiguration` object is immutable and safe to share across threads.

```java
// Safe: shared config, concurrent rendering
MjmlConfiguration config = MjmlConfiguration.defaults();

executor.submit(() -> MjmlRenderer.render(template1, config));
executor.submit(() -> MjmlRenderer.render(template2, config));
```

## Getting Help

If your issue is not listed here:

1. Check the [full documentation](https://jcputney.github.io/mjml-java/)
2. Search [existing issues](https://github.com/jcputney/mjml-java/issues)
3. Open a new issue with your MJML input, expected output, and actual output
