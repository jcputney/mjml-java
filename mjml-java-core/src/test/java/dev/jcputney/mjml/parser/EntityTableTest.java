package dev.jcputney.mjml.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/** Tests for the EntityTable HTML entity replacement logic. */
class EntityTableTest {

  @Test
  void replaceEntitiesReturnsNullForNullInput() {
    assertNull(EntityTable.replaceEntities(null));
  }

  @Test
  void returnsUnchangedWhenNoAmpersandPresent() {
    String input = "Hello world, no entities here!";
    assertEquals(input, EntityTable.replaceEntities(input));
  }

  @Test
  void replacesNbsp() {
    assertEquals("foo&#160;bar", EntityTable.replaceEntities("foo&nbsp;bar"));
  }

  @Test
  void replacesCopy() {
    assertEquals("&#169; 2024", EntityTable.replaceEntities("&copy; 2024"));
  }

  @Test
  void replacesEuro() {
    assertEquals("Price: &#8364;100", EntityTable.replaceEntities("Price: &euro;100"));
  }

  @Test
  void replacesMultipleEntitiesInOneString() {
    String input = "&nbsp;Hello&copy;World&euro;";
    String expected = "&#160;Hello&#169;World&#8364;";
    assertEquals(expected, EntityTable.replaceEntities(input));
  }

  @Test
  void preservesXmlEntitiesUnchanged() {
    String input = "&amp; &lt; &gt; &apos; &quot;";
    assertEquals(input, EntityTable.replaceEntities(input));
  }

  @Test
  void handlesUnknownEntityKeptAsIs() {
    String input = "foo &foobar; baz";
    assertEquals(input, EntityTable.replaceEntities(input));
  }

  @Test
  void handlesBareAmpersandWithoutSemicolon() {
    String input = "Tom & Jerry";
    assertEquals(input, EntityTable.replaceEntities(input));
  }

  @Test
  void handlesAmpersandWithDistantSemicolon() {
    // More than 12 chars between & and ; should not be treated as entity
    String input = "&thisisaverylongname;";
    assertEquals(input, EntityTable.replaceEntities(input));
  }

  @Test
  void handlesEntityAtEndOfString() {
    String input = "end&nbsp;";
    assertEquals("end&#160;", EntityTable.replaceEntities(input));
  }

  @Test
  void handlesConsecutiveEntities() {
    String input = "&nbsp;&nbsp;&nbsp;";
    assertEquals("&#160;&#160;&#160;", EntityTable.replaceEntities(input));
  }

  @Test
  void handlesEmptyString() {
    assertEquals("", EntityTable.replaceEntities(""));
  }

  @Test
  void handlesMixedContentAndEntities() {
    String input = "Hello &amp; welcome! Price: &euro;50 &mdash; enjoy &copy; 2024";
    String expected = "Hello &amp; welcome! Price: &#8364;50 &#8212; enjoy &#169; 2024";
    assertEquals(expected, EntityTable.replaceEntities(input));
  }

  @Test
  void spotCheckRepresentativeEntitiesFromEachCategory() {
    // Latin: &Agrave; -> &#192;
    assertEquals("&#192;", EntityTable.replaceEntities("&Agrave;"));
    // Typographic: &ldquo; -> &#8220;
    assertEquals("&#8220;", EntityTable.replaceEntities("&ldquo;"));
    // Math/symbols: &larr; -> &#8592;
    assertEquals("&#8592;", EntityTable.replaceEntities("&larr;"));
    // Spacing: &ensp; -> &#8194;
    assertEquals("&#8194;", EntityTable.replaceEntities("&ensp;"));
  }
}
