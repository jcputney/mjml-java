package dev.jcputney.mjml.context;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MetadataContextTest {

  @Test
  void defaultValues() {
    MetadataContext ctx = new MetadataContext();
    assertEquals("", ctx.getTitle());
    assertEquals("", ctx.getPreviewText());
    assertEquals("480px", ctx.getBreakpoint());
    assertEquals(480, ctx.getBreakpointPx());
    assertEquals(600, ctx.getContainerWidth());
    assertEquals("", ctx.getBodyBackgroundColor());
    assertTrue(ctx.getHeadComments().isEmpty());
    assertTrue(ctx.getFileStartContent().isEmpty());
  }

  @Test
  void titleSetAndGet() {
    MetadataContext ctx = new MetadataContext();
    ctx.setTitle("My Email");
    assertEquals("My Email", ctx.getTitle());
  }

  @Test
  void titleNullSetsEmpty() {
    MetadataContext ctx = new MetadataContext();
    ctx.setTitle("Test");
    ctx.setTitle(null);
    assertEquals("", ctx.getTitle());
  }

  @Test
  void previewTextSetAndGet() {
    MetadataContext ctx = new MetadataContext();
    ctx.setPreviewText("Preview line");
    assertEquals("Preview line", ctx.getPreviewText());
  }

  @Test
  void previewTextNullSetsEmpty() {
    MetadataContext ctx = new MetadataContext();
    ctx.setPreviewText("Test");
    ctx.setPreviewText(null);
    assertEquals("", ctx.getPreviewText());
  }

  @Test
  void breakpointSetAndGet() {
    MetadataContext ctx = new MetadataContext();
    ctx.setBreakpoint("600px");
    assertEquals("600px", ctx.getBreakpoint());
    assertEquals(600, ctx.getBreakpointPx());
  }

  @Test
  void breakpointNullOrEmptyIgnored() {
    MetadataContext ctx = new MetadataContext();
    ctx.setBreakpoint("600px");
    ctx.setBreakpoint(null);
    assertEquals("600px", ctx.getBreakpoint());
    ctx.setBreakpoint("");
    assertEquals("600px", ctx.getBreakpoint());
  }

  @Test
  void containerWidth() {
    MetadataContext ctx = new MetadataContext();
    ctx.setContainerWidth(800);
    assertEquals(800, ctx.getContainerWidth());
  }

  @Test
  void bodyBackgroundColor() {
    MetadataContext ctx = new MetadataContext();
    ctx.setBodyBackgroundColor("#ffffff");
    assertEquals("#ffffff", ctx.getBodyBackgroundColor());
  }

  @Test
  void bodyBackgroundColorNullSetsEmpty() {
    MetadataContext ctx = new MetadataContext();
    ctx.setBodyBackgroundColor("#fff");
    ctx.setBodyBackgroundColor(null);
    assertEquals("", ctx.getBodyBackgroundColor());
  }

  @Test
  void headComments() {
    MetadataContext ctx = new MetadataContext();
    ctx.addHeadComment("<!-- comment 1 -->");
    ctx.addHeadComment("<!-- comment 2 -->");
    assertEquals(2, ctx.getHeadComments().size());
    assertEquals("<!-- comment 1 -->", ctx.getHeadComments().get(0));
  }

  @Test
  void headCommentsUnmodifiable() {
    MetadataContext ctx = new MetadataContext();
    ctx.addHeadComment("test");
    assertThrows(UnsupportedOperationException.class, () -> ctx.getHeadComments().add("x"));
  }

  @Test
  void fileStartContent() {
    MetadataContext ctx = new MetadataContext();
    ctx.addFileStartContent("<style>body{}</style>");
    assertEquals(1, ctx.getFileStartContent().size());
    assertEquals("<style>body{}</style>", ctx.getFileStartContent().get(0));
  }

  @Test
  void fileStartContentSkipsNullAndEmpty() {
    MetadataContext ctx = new MetadataContext();
    ctx.addFileStartContent(null);
    ctx.addFileStartContent("");
    assertTrue(ctx.getFileStartContent().isEmpty());
  }

  @Test
  void fileStartContentUnmodifiable() {
    MetadataContext ctx = new MetadataContext();
    ctx.addFileStartContent("test");
    assertThrows(UnsupportedOperationException.class, () -> ctx.getFileStartContent().add("x"));
  }
}
