---
sidebar_position: 1
title: Installation
---

# Installation

mjml-java is a pure Java MJML-to-HTML renderer with **zero external dependencies** (for the core module). It requires Java 17 or later.

## Modules

mjml-java is a multi-module project. Most users only need the core module:

| Module | Artifact ID | Description |
|---|---|---|
| **Core** | `mjml-java-core` | MJML renderer, all 31 components, CSS inliner. Zero external dependencies. |
| **Resolvers** | `mjml-java-resolvers` | Additional `IncludeResolver` implementations: URL, caching, composite, map, prefix-routing. `UrlIncludeResolver` includes SSRF protections and explicit hostname allowlisting. Zero external dependencies (JDK `java.net.http`). |
| **Spring** | `mjml-java-spring` | Spring Boot 3.2+ auto-configuration, `MjmlService`, Thymeleaf integration. |
| **BOM** | `mjml-java-bom` | Bill of Materials for consistent version management. |

## Core Module

### Maven

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>dev.jcputney</groupId>
    <artifactId>mjml-java-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Gradle

Add the following to your `build.gradle`:

```groovy
implementation 'dev.jcputney:mjml-java-core:1.0.0-SNAPSHOT'
```

Or for Kotlin DSL (`build.gradle.kts`):

```kotlin
implementation("dev.jcputney:mjml-java-core:1.0.0-SNAPSHOT")
```

## Using the BOM

To manage versions across multiple modules, import the BOM:

### Maven

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>dev.jcputney</groupId>
            <artifactId>mjml-java-bom</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>dev.jcputney</groupId>
        <artifactId>mjml-java-core</artifactId>
    </dependency>
    <dependency>
        <groupId>dev.jcputney</groupId>
        <artifactId>mjml-java-resolvers</artifactId>
    </dependency>
</dependencies>
```

### Gradle

```groovy
dependencies {
    implementation platform('dev.jcputney:mjml-java-bom:1.0.0-SNAPSHOT')
    implementation 'dev.jcputney:mjml-java-core'
    implementation 'dev.jcputney:mjml-java-resolvers'
}
```

## JPMS (Java Module System)

The core module is fully JPMS-ready. Add the following to your `module-info.java`:

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

## Requirements

- **Java 17** or later
- Core and Resolvers modules: zero external dependencies (JDK standard library only)
- Spring module: Spring Boot 3.2+, optional Thymeleaf 3.1+
