package dev.jcputney.mjml.context;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class StyleContextTest {

  @Test
  void defaultValues() {
    StyleContext ctx = new StyleContext();
    assertTrue(ctx.getFonts().isEmpty());
    assertTrue(ctx.getFontUrlOverrides().isEmpty());
    assertTrue(ctx.getStyles().isEmpty());
    assertTrue(ctx.getComponentStyles().isEmpty());
    assertTrue(ctx.getInlineStyles().isEmpty());
    assertTrue(ctx.getMediaQueries().isEmpty());
    assertFalse(ctx.isFluidOnMobileUsed());
  }

  @Test
  void addFont() {
    StyleContext ctx = new StyleContext();
    ctx.addFont("Open Sans", "https://fonts.googleapis.com/css?family=Open+Sans");
    assertEquals(1, ctx.getFonts().size());
    var font = ctx.getFonts().iterator().next();
    assertEquals("Open Sans", font.name());
    assertEquals("https://fonts.googleapis.com/css?family=Open+Sans", font.href());
  }

  @Test
  void addDuplicateFontNoop() {
    StyleContext ctx = new StyleContext();
    ctx.addFont("Open Sans", "https://example.com");
    ctx.addFont("Open Sans", "https://example.com");
    assertEquals(1, ctx.getFonts().size());
  }

  @Test
  void fontsUnmodifiable() {
    StyleContext ctx = new StyleContext();
    assertThrows(UnsupportedOperationException.class,
        () -> ctx.getFonts().add(new GlobalContext.FontDef("x", "y")));
  }

  @Test
  void fontUrlOverrides() {
    StyleContext ctx = new StyleContext();
    ctx.registerFontOverride("Open Sans", "https://custom.com/font.css");
    assertEquals("https://custom.com/font.css", ctx.getFontUrlOverride("Open Sans"));
    assertNull(ctx.getFontUrlOverride("Missing"));
    assertEquals(1, ctx.getFontUrlOverrides().size());
  }

  @Test
  void fontUrlOverridesUnmodifiable() {
    StyleContext ctx = new StyleContext();
    assertThrows(UnsupportedOperationException.class,
        () -> ctx.getFontUrlOverrides().put("x", "y"));
  }

  @Test
  void addStyle() {
    StyleContext ctx = new StyleContext();
    ctx.addStyle("body { margin: 0; }");
    assertEquals(1, ctx.getStyles().size());
    assertEquals("body { margin: 0; }", ctx.getStyles().get(0));
  }

  @Test
  void addStyleSkipsNullAndBlank() {
    StyleContext ctx = new StyleContext();
    ctx.addStyle(null);
    ctx.addStyle("");
    ctx.addStyle("   ");
    assertTrue(ctx.getStyles().isEmpty());
  }

  @Test
  void addStyleOnce() {
    StyleContext ctx = new StyleContext();
    assertTrue(ctx.addStyleOnce("nav", "nav { display:block; }"));
    assertFalse(ctx.addStyleOnce("nav", "nav { display:block; }"));
    assertEquals(1, ctx.getStyles().size());
  }

  @Test
  void addComponentStyle() {
    StyleContext ctx = new StyleContext();
    ctx.addComponentStyle(".hamburger { display:block; }");
    assertEquals(1, ctx.getComponentStyles().size());
  }

  @Test
  void addComponentStyleSkipsNullAndBlank() {
    StyleContext ctx = new StyleContext();
    ctx.addComponentStyle(null);
    ctx.addComponentStyle("   ");
    assertTrue(ctx.getComponentStyles().isEmpty());
  }

  @Test
  void addInlineStyle() {
    StyleContext ctx = new StyleContext();
    ctx.addInlineStyle("p { color: red; }");
    assertEquals(1, ctx.getInlineStyles().size());
  }

  @Test
  void addInlineStyleSkipsNullAndBlank() {
    StyleContext ctx = new StyleContext();
    ctx.addInlineStyle(null);
    ctx.addInlineStyle("");
    assertTrue(ctx.getInlineStyles().isEmpty());
  }

  @Test
  void mediaQueries() {
    StyleContext ctx = new StyleContext();
    ctx.addMediaQuery("col-100", "100", "%");
    ctx.addMediaQuery("col-200", "200", "px");
    assertEquals(2, ctx.getMediaQueries().size());
  }

  @Test
  void mediaQueriesDeduplicated() {
    StyleContext ctx = new StyleContext();
    ctx.addMediaQuery("col-100", "100", "%");
    ctx.addMediaQuery("col-100", "100", "%");
    assertEquals(1, ctx.getMediaQueries().size());
  }

  @Test
  void mediaQueriesUnmodifiable() {
    StyleContext ctx = new StyleContext();
    assertThrows(UnsupportedOperationException.class,
        () -> ctx.getMediaQueries().add(new GlobalContext.MediaQuery("x", "1", "%")));
  }

  @Test
  void fluidOnMobile() {
    StyleContext ctx = new StyleContext();
    assertFalse(ctx.isFluidOnMobileUsed());
    ctx.setFluidOnMobileUsed(true);
    assertTrue(ctx.isFluidOnMobileUsed());
  }

  @Test
  void stylesUnmodifiable() {
    StyleContext ctx = new StyleContext();
    ctx.addStyle("test");
    assertThrows(UnsupportedOperationException.class, () -> ctx.getStyles().add("x"));
  }

  @Test
  void componentStylesUnmodifiable() {
    StyleContext ctx = new StyleContext();
    ctx.addComponentStyle("test");
    assertThrows(UnsupportedOperationException.class, () -> ctx.getComponentStyles().add("x"));
  }

  @Test
  void inlineStylesUnmodifiable() {
    StyleContext ctx = new StyleContext();
    ctx.addInlineStyle("test");
    assertThrows(UnsupportedOperationException.class, () -> ctx.getInlineStyles().add("x"));
  }
}
