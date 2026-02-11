package dev.jcputney.mjml.component;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.jcputney.mjml.MjmlConfiguration;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlNode;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ComponentRegistry} freeze behavior, duplicate registration, and unknown tags.
 */
class ComponentRegistryTest {

  // -- Freeze behavior --

  @Test
  void registerBeforeFreezeSucceeds() {
    ComponentRegistry registry = new ComponentRegistry();

    assertDoesNotThrow(() -> registry.register("mj-custom", (node, ctx, rctx) -> null));
  }

  @Test
  void registerAfterFreezeThrowsIllegalState() {
    ComponentRegistry registry = new ComponentRegistry();
    registry.register("mj-first", (node, ctx, rctx) -> null);
    registry.freeze();

    assertThrows(
        IllegalStateException.class,
        () -> registry.register("mj-second", (node, ctx, rctx) -> null),
        "Registering after freeze should throw IllegalStateException");
  }

  @Test
  void freezeIsIdempotent() {
    ComponentRegistry registry = new ComponentRegistry();
    registry.register("mj-test", (node, ctx, rctx) -> null);
    registry.freeze();
    // Calling freeze again should not throw
    assertDoesNotThrow(registry::freeze);
    // Registration should still be rejected
    assertThrows(
        IllegalStateException.class,
        () -> registry.register("mj-another", (node, ctx, rctx) -> null));
  }

  @Test
  void frozenRegistryCanStillCreateComponents() {
    ComponentRegistry registry = new ComponentRegistry();
    registry.register(
        "mj-test",
        (node, ctx, rctx) -> {
          // Return a simple mock component - we only need to verify factory is invoked
          return null;
        });
    registry.freeze();

    MjmlNode node = new MjmlNode("mj-test");
    GlobalContext globalContext = new GlobalContext(MjmlConfiguration.defaults());
    RenderContext renderContext = new RenderContext(600);

    // Creating components should still work after freeze
    assertDoesNotThrow(() -> registry.createComponent(node, globalContext, renderContext));
  }

  // -- Unknown tags --

  @Test
  void unknownTagReturnsNull() {
    ComponentRegistry registry = new ComponentRegistry();
    registry.register("mj-known", (node, ctx, rctx) -> null);
    registry.freeze();

    MjmlNode unknownNode = new MjmlNode("mj-unknown");
    GlobalContext globalContext = new GlobalContext(MjmlConfiguration.defaults());
    RenderContext renderContext = new RenderContext(600);

    BaseComponent result = registry.createComponent(unknownNode, globalContext, renderContext);
    assertNull(result, "Unknown tag should return null");
  }

  @Test
  void emptyRegistryReturnsNullForAllTags() {
    ComponentRegistry registry = new ComponentRegistry();
    registry.freeze();

    MjmlNode node = new MjmlNode("mj-anything");
    GlobalContext globalContext = new GlobalContext(MjmlConfiguration.defaults());
    RenderContext renderContext = new RenderContext(600);

    BaseComponent result = registry.createComponent(node, globalContext, renderContext);
    assertNull(result, "Empty frozen registry should return null for any tag");
  }

  // -- Duplicate registration --

  @Test
  void duplicateRegistrationOverwritesPreviousFactory() {
    ComponentRegistry registry = new ComponentRegistry();

    // Register first factory
    registry.register("mj-dup", (node, ctx, rctx) -> null);

    // Register second factory for same tag — should overwrite
    final boolean[] secondFactoryCalled = {false};
    registry.register(
        "mj-dup",
        (node, ctx, rctx) -> {
          secondFactoryCalled[0] = true;
          return null;
        });
    registry.freeze();

    MjmlNode node = new MjmlNode("mj-dup");
    GlobalContext globalContext = new GlobalContext(MjmlConfiguration.defaults());
    RenderContext renderContext = new RenderContext(600);

    registry.createComponent(node, globalContext, renderContext);
    assertNotNull(secondFactoryCalled);
    // The second factory should be the one invoked since it overwrote the first
    assert secondFactoryCalled[0] : "Second factory should have been called (overwrite)";
  }

  @Test
  void registeredComponentCanBeCreated() {
    ComponentRegistry registry = new ComponentRegistry();
    final boolean[] factoryCalled = {false};
    registry.register(
        "mj-custom",
        (node, ctx, rctx) -> {
          factoryCalled[0] = true;
          return null;
        });
    registry.freeze();

    MjmlNode node = new MjmlNode("mj-custom");
    GlobalContext globalContext = new GlobalContext(MjmlConfiguration.defaults());
    RenderContext renderContext = new RenderContext(600);

    registry.createComponent(node, globalContext, renderContext);
    assert factoryCalled[0] : "Factory should have been invoked for registered tag";
  }
}
