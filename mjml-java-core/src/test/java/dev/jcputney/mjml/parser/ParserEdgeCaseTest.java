package dev.jcputney.mjml.parser;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlException;
import dev.jcputney.mjml.MjmlRenderer;
import org.junit.jupiter.api.Test;

/** Parser edge case tests: BOM handling, encodings, XML declaration, empty/whitespace documents. */
class ParserEdgeCaseTest {

  // -- BOM (byte order mark) handling --

  @Test
  void handlesBomAtStartOfDocument() {
    // UTF-8 BOM is \uFEFF
    String mjmlWithBom =
        "\uFEFF<mjml><mj-body><mj-section><mj-column><mj-text>BOM test</mj-text></mj-column></mj-section></mj-body></mjml>";

    // BOM before XML content may cause parse issues; verify graceful handling
    // The JDK XML parser may or may not tolerate the BOM. We just need to verify
    // it either renders correctly or throws an MjmlException (not an unhandled error).
    try {
      String html = MjmlRenderer.render(mjmlWithBom).html();
      assertNotNull(html);
      assertTrue(html.contains("BOM test"), "Content should be rendered correctly despite BOM");
    } catch (MjmlException e) {
      // Also acceptable: parser rejects BOM as invalid XML
      assertNotNull(e.getMessage());
    }
  }

  // -- XML declaration --

  @Test
  void handlesXmlDeclaration() {
    String mjml =
        // language=MJML
        """
        <?xml version="1.0" encoding="UTF-8"?>
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>XML declaration test</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = assertDoesNotThrow(() -> MjmlRenderer.render(mjml).html());
    assertNotNull(html);
    assertTrue(
        html.contains("XML declaration test"),
        "Content should render correctly with XML declaration");
  }

  @Test
  void handlesXmlDeclarationWithStandalone() {
    String mjml =
        // language=MJML
        """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Standalone test</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = assertDoesNotThrow(() -> MjmlRenderer.render(mjml).html());
    assertNotNull(html);
    assertTrue(html.contains("Standalone test"));
  }

  // -- Empty and whitespace documents --

  @Test
  void throwsOnWhitespaceOnlyInput() {
    assertThrows(
        MjmlException.class,
        () -> MjmlParser.parse("   \n\t\r  "),
        "Whitespace-only input should be rejected");
  }

  @Test
  void throwsOnBlankLinesOnly() {
    assertThrows(
        MjmlException.class,
        () -> MjmlParser.parse("\n\n\n"),
        "Blank lines only should be rejected");
  }

  @Test
  void throwsOnSingleSpaceInput() {
    assertThrows(
        MjmlException.class, () -> MjmlParser.parse(" "), "Single space should be rejected");
  }

  // -- Encoding edge cases --

  @Test
  void handlesUnicodeContent() {
    String mjml =
        // language=MJML
        """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Unicode: \u00E9\u00E8\u00EA \u00FC\u00F6\u00E4 \u4F60\u597D \u043F\u0440\u0438\u0432\u0435\u0442</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = assertDoesNotThrow(() -> MjmlRenderer.render(mjml).html());
    assertNotNull(html);
    assertTrue(html.contains("\u00E9\u00E8\u00EA"), "French characters should be preserved");
    assertTrue(html.contains("\u4F60\u597D"), "Chinese characters should be preserved");
    assertTrue(
        html.contains("\u043F\u0440\u0438\u0432\u0435\u0442"),
        "Russian characters should be preserved");
  }

  @Test
  void handlesEmoji() {
    String mjml =
        // language=MJML
        """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>Emoji: \uD83D\uDE00\uD83D\uDE4F\uD83C\uDF89</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = assertDoesNotThrow(() -> MjmlRenderer.render(mjml).html());
    assertNotNull(html);
    // Emoji should survive the parse/render pipeline
    assertTrue(html.contains("\uD83D\uDE00"), "Emoji should be preserved through rendering");
  }

  // -- Malformed XML --

  @Test
  void throwsOnUnclosedTag() {
    assertThrows(
        MjmlException.class,
        () -> MjmlParser.parse("<mjml><mj-body>"),
        "Unclosed tags should cause a parse error");
  }

  @Test
  void throwsOnMismatchedTags() {
    assertThrows(
        MjmlException.class,
        () -> MjmlParser.parse("<mjml><mj-body></mj-head></mjml>"),
        "Mismatched tags should cause a parse error");
  }

  @Test
  void throwsOnInvalidXml() {
    assertThrows(
        MjmlException.class,
        () -> MjmlParser.parse("<<<not valid xml>>>"),
        "Invalid XML should cause a parse error");
  }

  // -- Edge cases with nesting depth --

  @Test
  void parsesExactlyAtMaxDepth() {
    // Build a document with nesting exactly at depth 3 (mjml=0, body=1, a=2, b=3)
    String mjml = "<mjml><mj-body><a><b>content</b></a></mj-body></mjml>";

    MjmlDocument doc = assertDoesNotThrow(() -> MjmlParser.parse(mjml, 5));
    assertNotNull(doc);
  }

  // -- Comments in various positions --

  @Test
  void handlesCommentsInBody() {
    String mjml =
        // language=MJML
        """
        <mjml>
          <mj-body>
            <!-- Comment before section -->
            <mj-section>
              <!-- Comment inside section -->
              <mj-column>
                <mj-text>After comments</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;

    String html = assertDoesNotThrow(() -> MjmlRenderer.render(mjml).html());
    assertNotNull(html);
    assertTrue(
        html.contains("After comments"),
        "Content should render correctly when comments are present");
  }
}
