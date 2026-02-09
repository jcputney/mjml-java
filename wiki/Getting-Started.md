# Getting Started

## Installation

### Maven

```xml
<dependency>
    <groupId>dev.jcputney</groupId>
    <artifactId>mjml-java-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Gradle (Groovy DSL)

```groovy
implementation 'dev.jcputney:mjml-java-core:1.0.0-SNAPSHOT'
```

### Gradle (Kotlin DSL)

```kotlin
implementation("dev.jcputney:mjml-java-core:1.0.0-SNAPSHOT")
```

### JPMS (module-info.java)

```java
module my.app {
    requires dev.jcputney.mjml;
}
```

## Quick Start

### Basic Rendering

```java
import dev.jcputney.mjml.MjmlRenderer;
import dev.jcputney.mjml.MjmlRenderResult;

String mjml = """
    <mjml>
      <mj-body>
        <mj-section>
          <mj-column>
            <mj-text font-size="20px" color="#333333">
              Welcome to mjml-java!
            </mj-text>
            <mj-button href="https://example.com">
              Click Me
            </mj-button>
          </mj-column>
        </mj-section>
      </mj-body>
    </mjml>
    """;

// One-liner: returns MjmlRenderResult with HTML, title, and preview text
MjmlRenderResult result = MjmlRenderer.render(mjml);
String html = result.html();
```

### With Configuration

```java
import dev.jcputney.mjml.MjmlRenderer;
import dev.jcputney.mjml.MjmlConfiguration;
import dev.jcputney.mjml.MjmlRenderResult;

MjmlConfiguration config = MjmlConfiguration.builder()
    .language("en")
    .direction("ltr")
    .build();

MjmlRenderResult result = MjmlRenderer.render(mjml, config);
String html = result.html();
String title = result.title();         // from <mj-title>
String preview = result.previewText(); // from <mj-preview>
```

### With Include Support

```java
import dev.jcputney.mjml.MjmlConfiguration;
import dev.jcputney.mjml.FileSystemIncludeResolver;
import java.nio.file.Path;

MjmlConfiguration config = MjmlConfiguration.builder()
    .includeResolver(new FileSystemIncludeResolver(Path.of("/templates")))
    .build();

// MJML files can now use: <mj-include path="./header.mjml" />
MjmlRenderResult result = MjmlRenderer.render(mjml, config);
```

## Error Handling

```java
import dev.jcputney.mjml.MjmlException;
import dev.jcputney.mjml.MjmlRenderException;
import dev.jcputney.mjml.MjmlValidationException;
import dev.jcputney.mjml.MjmlParseException;

try {
    MjmlRenderResult result = MjmlRenderer.render(mjml);
} catch (MjmlValidationException e) {
    // Input too large or exceeds nesting depth
} catch (MjmlParseException e) {
    // Malformed MJML (invalid XML)
} catch (MjmlRenderException e) {
    // Unexpected error during the render phase
} catch (MjmlException e) {
    // Other rendering errors
}
```

## Next Steps

- [API Quick Reference](API-Quick-Reference) -- all public methods at a glance
- [Component Cheat Sheet](Component-Cheat-Sheet) -- all 33 components in one table
- [Full Documentation](https://jcputney.github.io/mjml-java/) -- guides, architecture, and more
