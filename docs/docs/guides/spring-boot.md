---
sidebar_position: 7
title: Spring Boot Integration
---

# Spring Boot Integration

The `mjml-java-spring` module provides Spring Boot auto-configuration for the MJML renderer.

## Installation

Add the Spring module to your project:

```xml
<dependency>
    <groupId>dev.jcputney</groupId>
    <artifactId>mjml-java-spring</artifactId>
    <version>1.0.1-SNAPSHOT</version>
</dependency>
```

This transitively includes `mjml-java-core`. The `mjml-java-resolvers` module is an optional dependency -- add it explicitly if you need advanced resolvers.

## Auto-Configuration

When `mjml-java-spring` is on the classpath, the following beans are auto-configured:

| Bean | Type | Description |
|---|---|---|
| `mjmlIncludeResolver` | `IncludeResolver` | A `SpringResourceIncludeResolver` that resolves includes from the configured template location |
| `mjmlConfiguration` | `MjmlConfiguration` | Configuration built from `spring.mjml.*` properties |
| `mjmlService` | `MjmlService` | Service for rendering MJML templates |
| `thymeleafMjmlService` | `ThymeleafMjmlService` | Thymeleaf + MJML composition service (requires Thymeleaf on classpath and `spring.mjml.thymeleaf-enabled=true`, which is the default) |

All beans use `@ConditionalOnMissingBean`, so you can override any of them by defining your own bean of the same type.

## Configuration Properties

Configure the renderer via `application.properties` or `application.yml`:

```yaml
spring:
  mjml:
    language: en
    direction: ltr
    sanitize-output: true
    max-input-size: 1048576
    max-nesting-depth: 100
    max-include-depth: 50
    template-location: classpath:mjml/
    include-allowed-schemes: classpath,file
    thymeleaf-enabled: true
```

| Property | Default | Description |
|---|---|---|
| `spring.mjml.language` | `"und"` | HTML `lang` attribute |
| `spring.mjml.direction` | `"auto"` | Text direction: `"ltr"`, `"rtl"`, `"auto"` |
| `spring.mjml.sanitize-output` | `true` | Escape HTML special characters in attribute values |
| `spring.mjml.max-input-size` | `1048576` | Maximum input size in characters |
| `spring.mjml.max-nesting-depth` | `100` | Maximum element nesting depth |
| `spring.mjml.max-include-depth` | `50` | Maximum nested include depth |
| `spring.mjml.template-location` | `"classpath:mjml/"` | Base location for template resolution |
| `spring.mjml.include-allowed-schemes` | `classpath,file` | Allowed schemes for include resource paths |
| `spring.mjml.thymeleaf-enabled` | `true` | Enable Thymeleaf integration (auto-detected) |

## MjmlService

`MjmlService` is the primary bean for rendering MJML in a Spring application:

```java
import dev.jcputney.mjml.spring.MjmlService;
import dev.jcputney.mjml.MjmlRenderResult;

@Service
public class EmailService {

    private final MjmlService mjmlService;

    public EmailService(MjmlService mjmlService) {
        this.mjmlService = mjmlService;
    }

    public String renderWelcomeEmail(String userName) {
        String mjml = """
            <mjml>
              <mj-body>
                <mj-section>
                  <mj-column>
                    <mj-text>Welcome, %s!</mj-text>
                  </mj-column>
                </mj-section>
              </mj-body>
            </mjml>
            """.formatted(userName);

        return mjmlService.render(mjml);
    }
}
```

`MjmlService` methods:

| Method | Returns | Description |
|---|---|---|
| `render(String mjml)` | `String` | Renders MJML to HTML (returns just the HTML string) |
| `renderResult(String mjml)` | `MjmlRenderResult` | Renders MJML and returns the full result (HTML, title, preview text) |
| `getConfiguration()` | `MjmlConfiguration` | Returns the configuration used by this service |

## SpringResourceIncludeResolver

The auto-configured include resolver uses Spring's `ResourceLoader` to resolve include paths. By default, only safe local schemes are allowed:

- `classpath:` -- classpath resources
- `file:` -- file system paths

Relative paths are resolved against the configured `template-location`.

To allow remote schemes, configure them explicitly:

```yaml
spring:
  mjml:
    include-allowed-schemes: classpath,file,https
```

```yaml
# Templates in src/main/resources/mjml/
spring:
  mjml:
    template-location: classpath:mjml/
```

```xml
<!-- In your MJML template -->
<mj-include path="partials/header.mjml" />
<!-- Resolves to classpath:mjml/partials/header.mjml -->
```

## Thymeleaf Integration

When Thymeleaf is on the classpath and `spring.mjml.thymeleaf-enabled` is `true` (the default), a `ThymeleafMjmlService` bean is auto-configured. This service processes Thymeleaf expressions first, then renders the resulting MJML to HTML.

```java
import dev.jcputney.mjml.spring.ThymeleafMjmlService;

@Service
public class EmailService {

    private final ThymeleafMjmlService thymeleafMjmlService;

    public EmailService(ThymeleafMjmlService thymeleafMjmlService) {
        this.thymeleafMjmlService = thymeleafMjmlService;
    }

    public String renderWelcomeEmail(String userName) {
        String mjmlTemplate = """
            <mjml>
              <mj-body>
                <mj-section>
                  <mj-column>
                    <mj-text>Welcome, [[${name}]]!</mj-text>
                  </mj-column>
                </mj-section>
              </mj-body>
            </mjml>
            """;

        return thymeleafMjmlService.render(mjmlTemplate,
            Map.of("name", userName));
    }
}
```

### Using Template Files

`ThymeleafMjmlService` can also load templates by name from the Thymeleaf template resolver:

```java
// Loads the template from the Thymeleaf template path (e.g., classpath:templates/welcome)
String html = thymeleafMjmlService.renderTemplate("welcome",
    Map.of("name", "Alice", "activationUrl", "https://example.com/activate"));
```

`ThymeleafMjmlService` methods:

| Method | Description |
|---|---|
| `render(String mjmlTemplate, Map<String, Object> variables)` | Processes an inline MJML string through Thymeleaf, then renders to HTML |
| `renderTemplate(String templateName, Map<String, Object> variables)` | Loads a template by name via Thymeleaf, processes it, then renders as MJML to HTML |

## Overriding Auto-Configuration

Override any auto-configured bean by defining your own:

```java
@Configuration
public class MjmlConfig {

    @Bean
    public IncludeResolver mjmlIncludeResolver() {
        // Use a file system resolver instead of the default classpath resolver
        return new FileSystemIncludeResolver(Path.of("/opt/templates"));
    }

    @Bean
    public MjmlConfiguration mjmlConfiguration(IncludeResolver resolver) {
        return MjmlConfiguration.builder()
            .language("en")
            .direction("ltr")
            .includeResolver(resolver)
            .contentSanitizer(html -> Jsoup.clean(html, Safelist.basic()))
            .build();
    }
}
```
