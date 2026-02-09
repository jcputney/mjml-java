---
sidebar_position: 1
title: Installation
---

# Installation

mjml-java is a pure Java MJML-to-HTML renderer with **zero external dependencies**. It requires Java 17 or later.

## Maven

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>dev.jcputney</groupId>
    <artifactId>mjml-java</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Gradle

Add the following to your `build.gradle`:

```groovy
implementation 'dev.jcputney:mjml-java:1.0.0-SNAPSHOT'
```

Or for Kotlin DSL (`build.gradle.kts`):

```kotlin
implementation("dev.jcputney:mjml-java:1.0.0-SNAPSHOT")
```

## JPMS (Java Module System)

mjml-java is a fully modular library. If your project uses the Java Platform Module System, add the following to your `module-info.java`:

```java
module your.module {
    requires dev.jcputney.mjml;
}
```

The `dev.jcputney.mjml` module requires `java.xml` and `java.logging`, which are included in all standard JDK distributions.

### Exported Packages

The module exports the following packages:

| Package | Description |
|---|---|
| `dev.jcputney.mjml` | Public API: `MjmlRenderer`, `MjmlConfiguration`, `MjmlRenderResult` |
| `dev.jcputney.mjml.css` | CSS inlining engine |
| `dev.jcputney.mjml.component` | Component base classes and registry |
| `dev.jcputney.mjml.context` | Rendering context and attribute resolution |
| `dev.jcputney.mjml.parser` | MJML parser and preprocessor |
| `dev.jcputney.mjml.util` | Utility classes (CSS parsing, social icons, etc.) |

## Requirements

- **Java 17** or later
- No external dependencies -- the library uses only the `java.xml` and `java.logging` modules from the JDK
