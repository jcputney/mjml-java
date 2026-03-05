package dev.jcputney.mjml;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Additional coverage tests for under-tested code paths. */
class AdditionalCoverageTest {

  // ─── #4. mj-body with direct content (no section wrapper) ─────────────────

  @Nested
  class MjBodyDirectContent {

    @Test
    void bodyWithTextDirectlyInBody() {
      String mjml =
          // language=MJML
          """
          <mjml>
            <mj-body>
              <mj-text>Direct content</mj-text>
            </mj-body>
          </mjml>
          """;

      String html = assertDoesNotThrow(() -> MjmlRenderer.render(mjml).html());
      assertNotNull(html);
      assertTrue(html.contains("<!doctype html>"), "Should produce valid HTML structure");
    }
  }

  // ─── #5. Many columns in a section ─────────────────────────────────────────

  @Nested
  class ManyColumnsInSection {

    @Test
    void fiveColumnsRender() {
      String mjml = buildSectionWithColumns(5);
      String html = assertDoesNotThrow(() -> MjmlRenderer.render(mjml).html());
      for (int i = 1; i <= 5; i++) {
        assertTrue(html.contains("Column " + i), "Column " + i + " should be rendered");
      }
    }

    @Test
    void sixColumnsRender() {
      String mjml = buildSectionWithColumns(6);
      String html = assertDoesNotThrow(() -> MjmlRenderer.render(mjml).html());
      for (int i = 1; i <= 6; i++) {
        assertTrue(html.contains("Column " + i), "Column " + i + " should be rendered");
      }
    }

    @Test
    void tenColumnsRender() {
      String mjml = buildSectionWithColumns(10);
      String html = assertDoesNotThrow(() -> MjmlRenderer.render(mjml).html());
      for (int i = 1; i <= 10; i++) {
        assertTrue(html.contains("Column " + i), "Column " + i + " should be rendered");
      }
    }

    private String buildSectionWithColumns(int count) {
      StringBuilder sb = new StringBuilder();
      sb.append("<mjml><mj-body><mj-section>");
      for (int i = 1; i <= count; i++) {
        sb.append("<mj-column><mj-text>Column ").append(i).append("</mj-text></mj-column>");
      }
      sb.append("</mj-section></mj-body></mjml>");
      return sb.toString();
    }
  }

  // ─── #6. Zero-width column ────────────────────────────────────────────────

  @Nested
  class ZeroWidthColumn {

    @Test
    void zeroPercentWidthDoesNotThrow() {
      String mjml =
          // language=MJML
          """
          <mjml>
            <mj-body>
              <mj-section>
                <mj-column width="0%">
                  <mj-text>Zero percent</mj-text>
                </mj-column>
                <mj-column>
                  <mj-text>Normal</mj-text>
                </mj-column>
              </mj-section>
            </mj-body>
          </mjml>
          """;

      String html = assertDoesNotThrow(() -> MjmlRenderer.render(mjml).html());
      assertNotNull(html);
      assertTrue(html.contains("Normal"), "Normal column should still render");
    }

    @Test
    void zeroPxWidthDoesNotThrow() {
      String mjml =
          // language=MJML
          """
          <mjml>
            <mj-body>
              <mj-section>
                <mj-column width="0px">
                  <mj-text>Zero px</mj-text>
                </mj-column>
                <mj-column>
                  <mj-text>Normal</mj-text>
                </mj-column>
              </mj-section>
            </mj-body>
          </mjml>
          """;

      String html = assertDoesNotThrow(() -> MjmlRenderer.render(mjml).html());
      assertNotNull(html);
      assertTrue(html.contains("Normal"), "Normal column should still render");
    }
  }

  // ─── #7. Include resolver returning empty string ──────────────────────────

  @Nested
  class IncludeResolverEmptyString {

    @Test
    void emptyStringIncludeRendersWithoutNpe() {
      MjmlConfiguration config =
          MjmlConfiguration.builder().includeResolver((path, ctx) -> "").build();

      String mjml =
          // language=MJML
          """
          <mjml>
            <mj-body>
              <mj-section>
                <mj-column>
                  <mj-text>Before</mj-text>
                </mj-column>
              </mj-section>
              <mj-include path="empty.mjml" />
              <mj-section>
                <mj-column>
                  <mj-text>After</mj-text>
                </mj-column>
              </mj-section>
            </mj-body>
          </mjml>
          """;

      String html = assertDoesNotThrow(() -> MjmlRenderer.render(mjml, config).html());
      assertNotNull(html);
      assertTrue(html.contains("Before"), "Content before include should render");
      assertTrue(html.contains("After"), "Content after include should render");
    }
  }

  // ─── #8. Include resolver returning null ──────────────────────────────────

  @Nested
  class IncludeResolverNull {

    @Test
    void nullIncludeThrowsMeaningfulException() {
      MjmlConfiguration config =
          MjmlConfiguration.builder().includeResolver((path, ctx) -> null).build();

      String mjml =
          // language=MJML
          """
          <mjml>
            <mj-body>
              <mj-include path="null-resource.mjml" />
            </mj-body>
          </mjml>
          """;

      // Null from resolver should cause an exception (NPE in IncludeProcessor)
      assertThrows(Exception.class, () -> MjmlRenderer.render(mjml, config));
    }
  }

  // ─── #9. Direction.AUTO output verification ───────────────────────────────

  @Nested
  class DirectionAutoOutput {

    @Test
    void directionAutoRendersExpectedHtml() {
      MjmlConfiguration config = MjmlConfiguration.builder().direction(Direction.AUTO).build();

      String mjml =
          // language=MJML
          """
          <mjml>
            <mj-body>
              <mj-section>
                <mj-column>
                  <mj-text>Auto direction</mj-text>
                </mj-column>
              </mj-section>
            </mj-body>
          </mjml>
          """;

      String html = MjmlRenderer.render(mjml, config).html();
      assertNotNull(html);
      // AUTO is the default direction; it should set dir="auto" on the html element
      assertTrue(html.contains("dir=\"auto\""), "Direction.AUTO should produce dir=\"auto\"");
    }

    @Test
    void directionAutoIsDefault() {
      MjmlConfiguration config = MjmlConfiguration.defaults();

      String mjml =
          // language=MJML
          """
          <mjml>
            <mj-body>
              <mj-section>
                <mj-column>
                  <mj-text>Default direction</mj-text>
                </mj-column>
              </mj-section>
            </mj-body>
          </mjml>
          """;

      String html = MjmlRenderer.render(mjml, config).html();
      // Default config uses Direction.AUTO
      assertTrue(html.contains("dir=\"auto\""), "Default configuration should use dir=\"auto\"");
    }
  }

  // ─── Direction enum branch coverage ──────────────────────────────────────

  @Nested
  class DirectionEnumBranches {

    @Test
    void directionLtrSetsExplicitDir() {
      MjmlConfiguration config = MjmlConfiguration.builder().direction(Direction.LTR).build();

      String mjml =
          // language=MJML
          """
          <mjml>
            <mj-body>
              <mj-section>
                <mj-column>
                  <mj-text>LTR content</mj-text>
                </mj-column>
              </mj-section>
            </mj-body>
          </mjml>
          """;

      String html = MjmlRenderer.render(mjml, config).html();
      assertNotNull(html);
      assertTrue(
          html.contains("dir=\"ltr\""), "Direction.LTR should produce dir=\"ltr\" on html element");
    }

    @Test
    void directionRtlSetsExplicitDir() {
      MjmlConfiguration config = MjmlConfiguration.builder().direction(Direction.RTL).build();

      String mjml =
          // language=MJML
          """
          <mjml>
            <mj-body>
              <mj-section>
                <mj-column>
                  <mj-text>RTL content</mj-text>
                </mj-column>
              </mj-section>
            </mj-body>
          </mjml>
          """;

      String html = MjmlRenderer.render(mjml, config).html();
      assertNotNull(html);
      assertTrue(
          html.contains("dir=\"rtl\""), "Direction.RTL should produce dir=\"rtl\" on html element");
    }

    @Test
    void directionAutoDoesNotSetLtrOrRtl() {
      MjmlConfiguration config = MjmlConfiguration.builder().direction(Direction.AUTO).build();

      String mjml =
          // language=MJML
          """
          <mjml>
            <mj-body>
              <mj-section>
                <mj-column>
                  <mj-text>Auto direction</mj-text>
                </mj-column>
              </mj-section>
            </mj-body>
          </mjml>
          """;

      String html = MjmlRenderer.render(mjml, config).html();
      assertNotNull(html);
      assertTrue(html.contains("dir=\"auto\""), "Direction.AUTO should produce dir=\"auto\"");
      // Ensure it doesn't set dir="ltr" or dir="rtl"
      assertFalse(html.contains("dir=\"ltr\""), "Direction.AUTO should not produce dir=\"ltr\"");
      assertFalse(html.contains("dir=\"rtl\""), "Direction.AUTO should not produce dir=\"rtl\"");
    }

    @Test
    void directionOfParsesStrings() {
      assertEquals(Direction.LTR, Direction.of("ltr"));
      assertEquals(Direction.RTL, Direction.of("rtl"));
      assertEquals(Direction.AUTO, Direction.of("auto"));
    }

    @Test
    void directionOfCaseInsensitive() {
      assertEquals(Direction.LTR, Direction.of("LTR"));
      assertEquals(Direction.RTL, Direction.of("Rtl"));
      assertEquals(Direction.AUTO, Direction.of("AUTO"));
    }

    @Test
    void directionOfRejectsNull() {
      assertThrows(IllegalArgumentException.class, () -> Direction.of(null));
    }

    @Test
    void directionOfRejectsUnknown() {
      assertThrows(IllegalArgumentException.class, () -> Direction.of("bidi"));
    }

    @Test
    void directionStringBuilderOverload() {
      MjmlConfiguration config = MjmlConfiguration.builder().direction("rtl").build();
      assertEquals(Direction.RTL, config.getDirection());
    }
  }

  // ─── #10. sanitizeOutput=false with contentSanitizer ──────────────────────

  @Nested
  class SanitizeOutputFalseWithContentSanitizer {

    @Test
    void contentSanitizerAppliesEvenWhenSanitizeOutputDisabled() {
      MjmlConfiguration config =
          MjmlConfiguration.builder()
              .sanitizeOutput(false)
              .contentSanitizer(html -> html.replace("MARKER", "REPLACED"))
              .build();

      String mjml =
          // language=MJML
          """
          <mjml>
            <mj-body>
              <mj-section>
                <mj-column>
                  <mj-text>MARKER content</mj-text>
                </mj-column>
              </mj-section>
            </mj-body>
          </mjml>
          """;

      String html = MjmlRenderer.render(mjml, config).html();
      assertNotNull(html);
      assertFalse(
          html.contains("MARKER"),
          "Content sanitizer should apply even when sanitizeOutput is false");
      assertTrue(html.contains("REPLACED"), "Content sanitizer replacement should be present");
    }
  }

  // ─── #11. Null bytes in content ───────────────────────────────────────────

  @Nested
  class NullBytesInContent {

    @Test
    void nullBytesInTextThrowsParseException() {
      String mjml =
          // language=MJML
          """
          <mjml>
            <mj-body>
              <mj-section>
                <mj-column>
                  <mj-text>Before\u0000After</mj-text>
                </mj-column>
              </mj-section>
            </mj-body>
          </mjml>
          """;

      // Null bytes are invalid XML characters; the parser rejects them with a clear exception
      MjmlParseException ex =
          assertThrows(
              MjmlParseException.class,
              () -> MjmlRenderer.render(mjml),
              "Null bytes should cause a parse exception, not an NPE or silent corruption");
      assertTrue(
          ex.getMessage().contains("invalid XML character")
              || ex.getMessage().contains("Unicode: 0x0"),
          "Exception message should mention the invalid character");
    }
  }
}
