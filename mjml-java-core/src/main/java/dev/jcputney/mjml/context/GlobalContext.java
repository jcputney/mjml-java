package dev.jcputney.mjml.context;

import dev.jcputney.mjml.MjmlConfiguration;

/**
 * Document-wide context gathered during head processing. Facade that delegates to three focused
 * sub-contexts:
 *
 * <ul>
 *   <li>{@link MetadataContext} — title, previewText, breakpoint, containerWidth,
 *       bodyBackgroundColor, headComments, fileStartContent
 *   <li>{@link StyleContext} — fonts, fontUrlOverrides, styles, componentStyles, inlineStyles,
 *       mediaQueries, fluidOnMobileUsed, registeredStyleKeys
 *   <li>{@link AttributeContext} — defaultAttributes, classAttributes, htmlAttributes
 * </ul>
 *
 * <p>Access sub-contexts directly via {@link #metadata()}, {@link #styles()}, and {@link
 * #attributes()}.
 *
 * <p><strong>Thread safety:</strong> This class is <em>not</em> thread-safe. Each {@link
 * dev.jcputney.mjml.render.RenderPipeline} creates its own instance, so concurrent renders do not
 * share a GlobalContext.
 */
public class GlobalContext {

  private final MjmlConfiguration configuration;
  private final MetadataContext metadata;
  private final StyleContext styleContext;
  private final AttributeContext attributeContext;

  /**
   * Creates a new global context with the given configuration.
   *
   * @param configuration the MJML configuration for this render
   */
  public GlobalContext(MjmlConfiguration configuration) {
    this.configuration = configuration;
    this.metadata = new MetadataContext();
    this.styleContext = new StyleContext();
    this.attributeContext = new AttributeContext();
  }

  // --- Sub-context accessors ---

  /**
   * Returns the document metadata sub-context (title, preview, breakpoint, etc.).
   *
   * @return the metadata sub-context
   */
  public MetadataContext metadata() {
    return metadata;
  }

  /**
   * Returns the style sub-context (fonts, CSS, media queries, etc.).
   *
   * @return the style sub-context
   */
  public StyleContext styles() {
    return styleContext;
  }

  /**
   * Returns the attribute cascade sub-context (defaults, classes, HTML attributes).
   *
   * @return the attribute cascade sub-context
   */
  public AttributeContext attributes() {
    return attributeContext;
  }

  // --- Configuration ---

  /**
   * Returns the MJML configuration for this render.
   *
   * @return the MJML configuration
   */
  public MjmlConfiguration getConfiguration() {
    return configuration;
  }
}
