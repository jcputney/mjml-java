# mjml-java

**Pure Java MJML-to-HTML renderer with zero external dependencies.**

mjml-java is a complete Java implementation of the [MJML v4](https://mjml.io/) email framework. It converts MJML markup into responsive, email-client-compatible HTML without requiring Node.js, a JavaScript runtime, or any external dependencies beyond the JDK standard library. The library implements all 31 standard MJML components and includes a built-in CSS inlining engine.

## Quick Links

- [Full Documentation Site](https://jcputney.github.io/mjml-java/)
- [Getting Started](Getting-Started)
- [API Quick Reference](API-Quick-Reference)
- [Component Cheat Sheet](Component-Cheat-Sheet)
- [Troubleshooting](Troubleshooting)
- [Changelog](Changelog)

## Key Features

- All 31 MJML v4 components supported
- Zero runtime dependencies (JDK 17+ only)
- JPMS module: `dev.jcputney.mjml`
- Thread-safe rendering API
- Built-in CSS inlining engine
- `mj-include` support with pluggable resolvers
- Custom component registration
- Input validation and security hardening
- GraalVM native image compatible (no reflection)

## Modules

- `mjml-java-core`: renderer, components, CSS inliner
- `mjml-java-resolvers`: URL/caching/composite include resolvers
- `mjml-java-spring`: Spring Boot auto-configuration and services
- `mjml-java-bom`: dependency version alignment across modules

## Installation

**Maven:**
```xml
<dependency>
    <groupId>dev.jcputney</groupId>
    <artifactId>mjml-java-core</artifactId>
    <version>1.0.1-SNAPSHOT</version>
</dependency>
```

**Gradle:**
```groovy
implementation 'dev.jcputney:mjml-java-core:1.0.1-SNAPSHOT'
```

## Minimal Example

```java
import dev.jcputney.mjml.MjmlRenderer;
import dev.jcputney.mjml.MjmlRenderResult;

String mjml = """
    <mjml>
      <mj-body>
        <mj-section>
          <mj-column>
            <mj-text>Hello from mjml-java!</mj-text>
          </mj-column>
        </mj-section>
      </mj-body>
    </mjml>
    """;

MjmlRenderResult result = MjmlRenderer.render(mjml);
String html = result.html();
```

## License

MIT
