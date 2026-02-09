package dev.jcputney.mjml;

/**
 * Optional hook for sanitizing HTML content within {@code <mj-text>}, {@code <mj-button>},
 * and {@code <mj-raw>} elements.
 *
 * <p>By default, MJML passes through inner HTML content as-is (matching the official MJML
 * behavior). Configure a {@code ContentSanitizer} on {@link MjmlConfiguration} when you
 * need to scrub user-supplied HTML before it appears in the rendered email.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * MjmlConfiguration config = MjmlConfiguration.builder()
 *     .contentSanitizer(html -> Jsoup.clean(html, Safelist.basic()))
 *     .build();
 * }</pre>
 */
@FunctionalInterface
public interface ContentSanitizer {

  /**
   * Sanitizes the given HTML content.
   *
   * @param html the raw HTML content from the MJML element
   * @return sanitized HTML safe for inclusion in the rendered email
   */
  String sanitize(String html);
}
