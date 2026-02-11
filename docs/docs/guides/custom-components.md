---
sidebar_position: 1
title: Custom Components
---

# Custom Components

mjml-java supports custom components that extend the built-in MJML tag set. You can create your own components that render HTML (body components) or process metadata (head components), and register them through the configuration builder.

## Creating a Body Component

Body components extend `BodyComponent` and produce HTML output. Every body component must implement three methods:

- `getTagName()` -- returns the MJML tag name (e.g., `"mj-greeting"`)
- `getDefaultAttributes()` -- returns a map of default attribute values
- `render()` -- returns the rendered HTML string

```java
import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import java.util.Map;

public class MjGreeting extends BodyComponent {

    public MjGreeting(MjmlNode node, GlobalContext globalContext,
                      RenderContext renderContext) {
        super(node, globalContext, renderContext);
    }

    @Override
    public String getTagName() {
        return "mj-greeting";
    }

    @Override
    public Map<String, String> getDefaultAttributes() {
        return Map.of(
            "name", "World",
            "color", "#000000"
        );
    }

    @Override
    public String render() {
        String name = getAttribute("name", "World");
        String color = getAttribute("color", "#000000");
        return "<div style=\"color:" + color + ";\">Hello, " + name + "!</div>";
    }
}
```

### Constructor Signature

All body components use the same three-argument constructor:

```java
(MjmlNode node, GlobalContext globalContext, RenderContext renderContext)
```

- **`MjmlNode`** -- the parsed XML node for this element, providing access to attributes and children
- **`GlobalContext`** -- document-wide state including fonts, styles, and attribute defaults
- **`RenderContext`** -- rendering state including the current container width and position info

### The Attribute Cascade

When you call `getAttribute("name")` or `getAttribute("name", "default")`, the value is resolved through a 5-level cascade. See the [Attribute Cascade](./attribute-cascade.md) guide for details. This means your custom components automatically participate in `mj-attributes`, `mj-class`, and `mj-all` defaults without any extra work.

## Registering a Component

Register custom components when building the `MjmlConfiguration`:

```java
MjmlConfiguration config = MjmlConfiguration.builder()
    .registerComponent("mj-greeting", MjGreeting::new)
    .build();
```

The second argument is a `ComponentFactory` -- a functional interface with the signature:

```java
@FunctionalInterface
public interface ComponentFactory {
    BaseComponent create(MjmlNode node, GlobalContext globalContext,
                         RenderContext renderContext);
}
```

Any constructor or static method matching `(MjmlNode, GlobalContext, RenderContext) -> BaseComponent` can be used as a method reference.

## Using the Custom Component

Once registered, use the tag in MJML templates like any built-in component:

```xml
<mjml>
  <mj-body>
    <mj-section>
      <mj-column>
        <mj-greeting name="Claude" color="#ff0000" />
      </mj-column>
    </mj-section>
  </mj-body>
</mjml>
```

```java
String html = MjmlRenderer.render(mjml, config).html();
// Output includes: <div style="color:#ff0000;">Hello, Claude!</div>
```

Custom components coexist with built-in components. You can mix `mj-text`, `mj-image`, and your custom tags in the same template.

## Using Default Attributes

When no attribute is provided on the element, your defaults from `getDefaultAttributes()` are used:

```xml
<mj-greeting />
<!-- Renders: <div style="color:#000000;">Hello, World!</div> -->
```

You can also set defaults for your custom component via `mj-attributes`:

```xml
<mjml>
  <mj-head>
    <mj-attributes>
      <mj-greeting color="#336699" />
    </mj-attributes>
  </mj-head>
  <mj-body>
    <mj-section>
      <mj-column>
        <mj-greeting name="User" />
        <!-- Uses color="#336699" from mj-attributes -->
      </mj-column>
    </mj-section>
  </mj-body>
</mjml>
```

## Creating a Head Component

Head components extend `HeadComponent` and process metadata rather than producing HTML. They implement `process()` instead of `render()`:

```java
import dev.jcputney.mjml.component.HeadComponent;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;

public class MjCustomMeta extends HeadComponent {

    public MjCustomMeta(MjmlNode node, GlobalContext globalContext,
                        RenderContext renderContext) {
        super(node, globalContext, renderContext);
    }

    @Override
    public String getTagName() {
        return "mj-custom-meta";
    }

    @Override
    public void process() {
        // Update global context with metadata from this element
        String value = node.getAttribute("value");
        if (value != null) {
            // Add custom processing logic here
        }
    }
}
```

Head components are processed during phase 4 of the rendering pipeline, after parsing and include resolution but before body rendering.

## Container Components

Container components render child MJML components (like `mj-section` renders `mj-column` children). They require a `ComponentRegistry` to instantiate child components.

Use `registerContainerComponent()` with the `ContainerComponentFactory` interface, which provides the registry as a fourth constructor argument:

```java
@FunctionalInterface
public interface ContainerComponentFactory {
    BaseComponent create(MjmlNode node, GlobalContext globalContext,
                         RenderContext renderContext, ComponentRegistry registry);
}
```

Here is a complete container component example:

```java
import dev.jcputney.mjml.component.BodyComponent;
import dev.jcputney.mjml.component.ComponentRegistry;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import java.util.Map;

public class MjCard extends BodyComponent {

    private final ComponentRegistry registry;

    public MjCard(MjmlNode node, GlobalContext globalContext,
                  RenderContext renderContext, ComponentRegistry registry) {
        super(node, globalContext, renderContext);
        this.registry = registry;
    }

    @Override
    public String getTagName() {
        return "mj-card";
    }

    @Override
    public Map<String, String> getDefaultAttributes() {
        return Map.of("background-color", "#ffffff");
    }

    @Override
    public String render() {
        String bg = getAttribute("background-color", "#ffffff");
        String children = renderChildren(registry);
        return "<div style=\"background-color:" + bg + ";\">"
            + children + "</div>";
    }
}
```

Register it with `registerContainerComponent()`:

```java
MjmlConfiguration config = MjmlConfiguration.builder()
    .registerContainerComponent("mj-card", MjCard::new)
    .build();
```

The `renderChildren(registry)` method iterates over child nodes, creates component instances using the registry, and concatenates their rendered HTML. This means your container component can nest any built-in or custom component:

```xml
<mjml>
  <mj-body>
    <mj-section>
      <mj-column>
        <mj-card background-color="#f0f0f0">
          <mj-text>Card title</mj-text>
          <mj-image src="https://example.com/photo.jpg" />
        </mj-card>
      </mj-column>
    </mj-section>
  </mj-body>
</mjml>
```

## Overriding Built-in Components

Registering a custom component with a tag name that matches a built-in component replaces the built-in. This works with both `registerComponent()` and `registerContainerComponent()`:

```java
MjmlConfiguration config = MjmlConfiguration.builder()
    .registerComponent("mj-text", MyCustomText::new)
    .build();
```

Container component registrations are applied after standard component registrations, so `registerContainerComponent()` takes precedence if both register the same tag name.

:::warning
Overriding built-in components may break templates that rely on the default rendering behavior. Test thoroughly when replacing core components like `mj-section`, `mj-column`, or `mj-text`.
:::

## Utility Methods

`BodyComponent` provides several helper methods available to custom components:

| Method | Description |
|--------|-------------|
| `getAttribute(name)` | Resolve attribute through the 5-level cascade |
| `getAttribute(name, default)` | Same, with a fallback if not found at any level |
| `getContentWidth()` | Container width minus padding and borders |
| `getBoxModel()` | Parsed padding/border box model |
| `buildStyle(map)` | Build a CSS style string from key-value pairs |
| `buildAttributes(map)` | Build HTML attributes string (with XSS escaping) |
| `escapeAttr(value)` | Escape a single attribute value for safe HTML output |
| `renderChildren(registry)` | Render all child body components (container components only) |
| `parseWidth(value)` | Parse a CSS unit value (px, %, etc.) to pixels |

The `renderChildren(registry)` method requires a `ComponentRegistry`, which is only available in container components registered via `registerContainerComponent()`.

## Multiple Custom Components

You can register any number of custom components, mixing leaf and container types:

```java
MjmlConfiguration config = MjmlConfiguration.builder()
    .registerComponent("mj-greeting", MjGreeting::new)
    .registerComponent("mj-badge", MjBadge::new)
    .registerContainerComponent("mj-card", MjCard::new)
    .registerContainerComponent("mj-panel", MjPanel::new)
    .build();
```

Custom components do not interfere with built-in components. The configuration is immutable after building, so it is safe to share across threads.
