---
sidebar_position: 2
title: Quick Start
---

# Quick Start

## Minimal Example

Render MJML to HTML in a single line:

```java
import dev.jcputney.mjml.MjmlRenderer;
import dev.jcputney.mjml.MjmlRenderResult;

MjmlRenderResult result = MjmlRenderer.render("<mjml><mj-body></mj-body></mjml>");
String html = result.html();
```

All `render()` overloads return an `MjmlRenderResult` record containing the rendered HTML, document title, and preview text.

## Working Example

Here is a complete example that renders an email with a heading and a text block:

```java
import dev.jcputney.mjml.MjmlRenderer;
import dev.jcputney.mjml.MjmlRenderResult;

public class EmailExample {
    public static void main(String[] args) {
        String mjml = """
                <mjml>
                  <mj-body>
                    <mj-section>
                      <mj-column>
                        <mj-text font-size="24px" font-weight="bold">
                          Welcome!
                        </mj-text>
                        <mj-text>
                          This email was rendered with mjml-java.
                        </mj-text>
                      </mj-column>
                    </mj-section>
                  </mj-body>
                </mjml>
                """;

        MjmlRenderResult result = MjmlRenderer.render(mjml);
        System.out.println(result.html());
    }
}
```

## Using Configuration

For more control over rendering, pass an `MjmlConfiguration` object to get back an `MjmlRenderResult`:

```java
import dev.jcputney.mjml.MjmlRenderer;
import dev.jcputney.mjml.MjmlConfiguration;
import dev.jcputney.mjml.MjmlRenderResult;

MjmlConfiguration config = MjmlConfiguration.builder()
        .language("en")
        .build();

MjmlRenderResult result = MjmlRenderer.render(mjml, config);

String html = result.html();   // The rendered HTML
String title = result.title(); // Title from <mj-title>, or empty string
```

`MjmlRenderResult` is a Java record with three accessors:

| Method | Return Type | Description |
|---|---|---|
| `html()` | `String` | The fully rendered HTML string |
| `title()` | `String` | The document title extracted from `<mj-title>`, or an empty string if none was set |
| `previewText()` | `String` | The preview text extracted from `<mj-preview>`, or an empty string if none was set |

## Rendering a File

You can render an MJML file directly from a `Path`. A `FileSystemIncludeResolver` is automatically configured using the file's parent directory:

```java
import dev.jcputney.mjml.MjmlRenderer;
import dev.jcputney.mjml.MjmlRenderResult;
import java.nio.file.Path;

MjmlRenderResult result = MjmlRenderer.render(Path.of("/templates/email.mjml"));
String html = result.html();
```

You can also pass a configuration. If no `IncludeResolver` is set, one is auto-created from the file's parent directory:

```java
MjmlConfiguration config = MjmlConfiguration.builder()
        .language("en")
        .build();

MjmlRenderResult result = MjmlRenderer.render(Path.of("/templates/email.mjml"), config);
```

## Using `mj-include`

To render templates that use `<mj-include>` to reference external files, configure an `IncludeResolver`:

```java
import dev.jcputney.mjml.MjmlRenderer;
import dev.jcputney.mjml.MjmlConfiguration;
import dev.jcputney.mjml.FileSystemIncludeResolver;
import java.nio.file.Path;

MjmlConfiguration config = MjmlConfiguration.builder()
        .includeResolver(new FileSystemIncludeResolver(Path.of("/path/to/templates")))
        .build();

String mjml = """
        <mjml>
          <mj-body>
            <mj-include path="header.mjml" />
            <mj-section>
              <mj-column>
                <mj-text>Body content</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

MjmlRenderResult result = MjmlRenderer.render(mjml, config);
```

`FileSystemIncludeResolver` resolves include paths relative to the given base directory and prevents path traversal outside of it.

## Error Handling

All rendering errors throw `MjmlException` (unchecked):

```java
import dev.jcputney.mjml.MjmlException;

try {
    MjmlRenderResult result = MjmlRenderer.render(mjmlString);
} catch (MjmlException e) {
    System.err.println("Failed to render MJML: " + e.getMessage());
}
```

## Thread Safety

The `MjmlRenderer.render()` methods are thread-safe. Each call creates its own render pipeline and context, so concurrent calls do not share mutable state. `MjmlConfiguration` instances are immutable and safe to share across threads.
