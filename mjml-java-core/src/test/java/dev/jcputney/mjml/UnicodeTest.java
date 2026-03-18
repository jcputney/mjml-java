package dev.jcputney.mjml;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Tests that various Unicode content renders correctly through the MJML pipeline. */
class UnicodeTest {

    private String render(String mjml) {
        MjmlRenderResult result = MjmlRenderer.render(mjml);
        assertNotNull(result.html());
        return result.html();
    }

    private String wrapInMjml(String textContent) {
        // language=MJML
        return """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>%s</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """.formatted(textContent);
    }

    @Test
    void chineseCharactersRender() {
        String html = render(wrapInMjml("你好世界"));
        assertTrue(html.contains("你好世界"), "Chinese characters should be preserved in output");
    }

    @Test
    void arabicWithRtlDirection() {
        String mjml =
                // language=MJML
                """
        <mjml>
          <mj-body>
            <mj-section>
              <mj-column>
                <mj-text>مرحبا بالعالم</mj-text>
              </mj-column>
            </mj-section>
          </mj-body>
        </mjml>
        """;
        MjmlConfiguration config =
                MjmlConfiguration.builder().direction(Direction.RTL).build();

        String html = MjmlRenderer.render(mjml, config).html();
        assertNotNull(html);
        assertTrue(html.contains("مرحبا بالعالم"), "Arabic text should be preserved");
        assertTrue(html.contains("dir=\"rtl\""), "RTL direction should appear in output");
    }

    @Test
    void combiningDiacriticsPreserved() {
        // e + combining acute accent = é
        String textWithDiacritics = "caf\u0065\u0301";
        String html = render(wrapInMjml(textWithDiacritics));
        assertTrue(
                html.contains("caf\u0065\u0301") || html.contains("café"), "Combining diacritics should be preserved");
    }

    @Test
    void emojiInMjText() {
        String html = render(wrapInMjml("Hello 🌍🎉"));
        assertTrue(html.contains("🌍") || html.contains("&#127757;"), "Emoji should be preserved or encoded");
        assertTrue(html.contains("🎉") || html.contains("&#127881;"), "Emoji should be preserved or encoded");
    }

    @Test
    void mixedLtrAndRtlContent() {
        String html = render(wrapInMjml("Hello مرحبا World عالم"));
        assertTrue(html.contains("Hello"), "LTR text should be preserved");
        assertTrue(html.contains("مرحبا"), "RTL text should be preserved");
        assertTrue(html.contains("World"), "Interleaved LTR should be preserved");
        assertTrue(html.contains("عالم"), "Interleaved RTL should be preserved");
    }
}
