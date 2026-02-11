package dev.jcputney.mjml.css;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class StyleAttributeTest {

  @Test
  void parsesSimpleStyle() {
    List<CssDeclaration> decls = StyleAttribute.parse("color:red;font-size:14px");
    assertEquals(2, decls.size());
    assertEquals("color", decls.get(0).property());
    assertEquals("red", decls.get(0).value());
    assertEquals("font-size", decls.get(1).property());
    assertEquals("14px", decls.get(1).value());
  }

  @Test
  void parsesImportant() {
    List<CssDeclaration> decls = StyleAttribute.parse("color: red !important");
    assertEquals(1, decls.size());
    assertTrue(decls.get(0).important());
  }

  @Test
  void handlesUrl() {
    List<CssDeclaration> decls =
        StyleAttribute.parse("background:url('http://example.com/a;b.jpg');color:red");
    assertEquals(2, decls.size());
    assertEquals("background", decls.get(0).property());
    assertTrue(decls.get(0).value().contains("example.com"));
    assertEquals("color", decls.get(1).property());
  }

  @Test
  void serializesDeclarations() {
    List<CssDeclaration> decls =
        List.of(
            new CssDeclaration("color", "red", false),
            new CssDeclaration("font-size", "14px", true));
    String result = StyleAttribute.serialize(decls);
    assertEquals("color:red;font-size:14px !important", result);
  }

  @Test
  void mergesNewProperty() {
    List<CssDeclaration> existing = StyleAttribute.parse("color:red");
    List<CssDeclaration> incoming = StyleAttribute.parse("font-size:14px");
    List<CssDeclaration> merged = StyleAttribute.merge(existing, incoming, CssSpecificity.ZERO);
    assertEquals(2, merged.size());
  }

  @Test
  void mergeOverridesProperty() {
    List<CssDeclaration> existing = StyleAttribute.parse("color:red");
    List<CssDeclaration> incoming = StyleAttribute.parse("color:blue");
    List<CssDeclaration> merged = StyleAttribute.merge(existing, incoming, CssSpecificity.ZERO);
    assertEquals(1, merged.size());
    assertEquals("blue", merged.get(0).value());
  }

  @Test
  void mergeRespectsExistingImportant() {
    List<CssDeclaration> existing = StyleAttribute.parse("color:red !important");
    List<CssDeclaration> incoming = StyleAttribute.parse("color:blue");
    List<CssDeclaration> merged = StyleAttribute.merge(existing, incoming, CssSpecificity.ZERO);
    assertEquals(1, merged.size());
    assertEquals("red", merged.get(0).value(), "Existing !important should win");
  }

  @Test
  void mergeIncomingImportantWins() {
    List<CssDeclaration> existing = StyleAttribute.parse("color:red");
    List<CssDeclaration> incoming = StyleAttribute.parse("color:blue !important");
    List<CssDeclaration> merged = StyleAttribute.merge(existing, incoming, CssSpecificity.ZERO);
    assertEquals(1, merged.size());
    assertEquals("blue", merged.get(0).value(), "Incoming !important should win");
  }

  @Test
  void handlesEmpty() {
    assertEquals(0, StyleAttribute.parse("").size());
    assertEquals(0, StyleAttribute.parse(null).size());
    assertEquals("", StyleAttribute.serialize(List.of()));
  }
}
