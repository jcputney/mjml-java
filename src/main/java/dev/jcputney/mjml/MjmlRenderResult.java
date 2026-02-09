package dev.jcputney.mjml;

/**
 * Result of rendering an MJML template to HTML.
 *
 * @param html        the rendered HTML string
 * @param title       the document title extracted from mj-title, or empty string
 * @param previewText the preview text extracted from mj-preview, or empty string
 */
public record MjmlRenderResult(String html, String title, String previewText) {

}
