package dev.jcputney.mjml.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlConfiguration;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.GlobalContext.FontDef;
import org.junit.jupiter.api.Test;

class DefaultFontRegistryTest {

  @Test
  void registersKnownFontWhenUsed() {
    GlobalContext ctx = new GlobalContext(MjmlConfiguration.defaults());
    DefaultFontRegistry.registerUsedFonts("Ubuntu, Helvetica, Arial, sans-serif", ctx);

    assertTrue(ctx.getFonts().stream().anyMatch(f -> "Ubuntu".equals(f.name())));
  }

  @Test
  void registersMultipleKnownFonts() {
    GlobalContext ctx = new GlobalContext(MjmlConfiguration.defaults());
    DefaultFontRegistry.registerUsedFonts("Open Sans, Lato, sans-serif", ctx);

    assertTrue(ctx.getFonts().stream().anyMatch(f -> "Open Sans".equals(f.name())));
    assertTrue(ctx.getFonts().stream().anyMatch(f -> "Lato".equals(f.name())));
  }

  @Test
  void fontUrlOverrideRespected() {
    GlobalContext ctx = new GlobalContext(MjmlConfiguration.defaults());
    ctx.registerFontOverride("Ubuntu", "https://custom.fonts/ubuntu.css");
    DefaultFontRegistry.registerUsedFonts("Ubuntu, sans-serif", ctx);

    FontDef ubuntu = ctx.getFonts().stream()
        .filter(f -> "Ubuntu".equals(f.name()))
        .findFirst()
        .orElse(null);
    assertTrue(ubuntu != null);
    assertEquals("https://custom.fonts/ubuntu.css", ubuntu.href());
  }

  @Test
  void unknownFontIgnored() {
    GlobalContext ctx = new GlobalContext(MjmlConfiguration.defaults());
    DefaultFontRegistry.registerUsedFonts("Comic Sans MS, cursive", ctx);

    assertTrue(ctx.getFonts().isEmpty());
  }

  @Test
  void nullInputHandled() {
    GlobalContext ctx = new GlobalContext(MjmlConfiguration.defaults());
    DefaultFontRegistry.registerUsedFonts(null, ctx);
    assertTrue(ctx.getFonts().isEmpty());
  }

  @Test
  void emptyInputHandled() {
    GlobalContext ctx = new GlobalContext(MjmlConfiguration.defaults());
    DefaultFontRegistry.registerUsedFonts("", ctx);
    assertTrue(ctx.getFonts().isEmpty());
  }

  @Test
  void doesNotRegisterDuplicate() {
    GlobalContext ctx = new GlobalContext(MjmlConfiguration.defaults());
    DefaultFontRegistry.registerUsedFonts("Ubuntu, sans-serif", ctx);
    DefaultFontRegistry.registerUsedFonts("Ubuntu, sans-serif", ctx);

    long count = ctx.getFonts().stream()
        .filter(f -> "Ubuntu".equals(f.name()))
        .count();
    assertEquals(1, count, "Should not register duplicate fonts");
  }

  @Test
  void registersCustomMjFontWhenUsedInFontFamily() {
    GlobalContext ctx = new GlobalContext(MjmlConfiguration.defaults());
    ctx.registerFontOverride("CustomFont", "https://example.com/custom.css");
    DefaultFontRegistry.registerUsedFonts("CustomFont, sans-serif", ctx);

    assertTrue(ctx.getFonts().stream().anyMatch(f -> "CustomFont".equals(f.name())));
  }
}
