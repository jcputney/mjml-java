package dev.jcputney.mjml;

import dev.jcputney.mjml.component.ComponentFactory;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Configuration for the MJML renderer.
 * Use the {@link #builder()} method to create instances.
 */
public final class MjmlConfiguration {

  private static final Logger LOG = Logger.getLogger(MjmlConfiguration.class.getName());

  /** Default maximum input size in characters (approximately 1 MB for ASCII). */
  public static final int DEFAULT_MAX_INPUT_SIZE = 1_048_576;

  /** Default maximum nesting depth. */
  public static final int DEFAULT_MAX_NESTING_DEPTH = 100;

  private final String language;
  private final Direction direction;
  private final IncludeResolver includeResolver;
  private final Map<String, ComponentFactory> customComponents;
  private final boolean sanitizeOutput;
  private final int maxInputSize;
  private final int maxNestingDepth;
  private final ContentSanitizer contentSanitizer;

  private MjmlConfiguration(Builder builder) {
    this.language = builder.language;
    this.direction = builder.direction;
    this.includeResolver = builder.includeResolver;
    this.customComponents = Map.copyOf(builder.customComponents);
    this.sanitizeOutput = builder.sanitizeOutput;
    this.maxInputSize = builder.maxInputSize;
    this.maxNestingDepth = builder.maxNestingDepth;
    this.contentSanitizer = builder.contentSanitizer;
  }

  public String getLanguage() {
    return language;
  }

  /**
   * The text direction for the HTML document. Defaults to {@link Direction#AUTO}.
   */
  public Direction getDirection() {
    return direction;
  }

  public IncludeResolver getIncludeResolver() {
    return includeResolver;
  }

  public Map<String, ComponentFactory> getCustomComponents() {
    return customComponents;
  }

  /**
   * Whether to escape HTML special characters in attribute values in rendered output.
   * When true, characters like {@code "}, {@code <}, {@code >}, {@code &} are escaped
   * in attribute values to prevent XSS. Default is true.
   */
  public boolean isSanitizeOutput() {
    return sanitizeOutput;
  }

  /**
   * Maximum allowed input size in characters. Inputs exceeding this limit are rejected
   * before processing. Default is 1,048,576 (approximately 1 MB for ASCII).
   */
  public int getMaxInputSize() {
    return maxInputSize;
  }

  /**
   * Maximum allowed nesting depth for MJML elements. Exceeding this depth during
   * parsing or rendering throws an exception. Default is 100.
   */
  public int getMaxNestingDepth() {
    return maxNestingDepth;
  }

  /**
   * Returns the optional content sanitizer, or {@code null} if none is configured.
   * When set, the sanitizer is applied to the inner HTML content of {@code <mj-text>},
   * {@code <mj-button>}, and {@code <mj-raw>} elements before rendering.
   */
  public ContentSanitizer getContentSanitizer() {
    return contentSanitizer;
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * Returns a builder pre-populated with this configuration's values.
   */
  public Builder toBuilder() {
    Builder b = new Builder();
    b.language = this.language;
    b.direction = this.direction;
    b.includeResolver = this.includeResolver;
    b.customComponents.putAll(this.customComponents);
    b.sanitizeOutput = this.sanitizeOutput;
    b.maxInputSize = this.maxInputSize;
    b.maxNestingDepth = this.maxNestingDepth;
    b.contentSanitizer = this.contentSanitizer;
    return b;
  }

  /**
   * Returns a default configuration.
   */
  public static MjmlConfiguration defaults() {
    return new Builder().build();
  }

  @Override
  public String toString() {
    return "MjmlConfiguration{"
        + "language='" + language + '\''
        + ", direction='" + direction + '\''
        + ", sanitizeOutput=" + sanitizeOutput
        + ", maxInputSize=" + maxInputSize
        + ", maxNestingDepth=" + maxNestingDepth
        + ", customComponents=" + customComponents.size()
        + ", includeResolver=" + (includeResolver != null ? includeResolver.getClass().getSimpleName() : "null")
        + ", contentSanitizer=" + (contentSanitizer != null ? "configured" : "null")
        + '}';
  }

  public static class Builder {

    private String language = "und";
    private Direction direction = Direction.AUTO;
    private IncludeResolver includeResolver;
    private final Map<String, ComponentFactory> customComponents = new LinkedHashMap<>();
    private boolean sanitizeOutput = true;
    private int maxInputSize = DEFAULT_MAX_INPUT_SIZE;
    private int maxNestingDepth = DEFAULT_MAX_NESTING_DEPTH;
    private ContentSanitizer contentSanitizer;

    public Builder language(String language) {
      this.language = language;
      return this;
    }

    /**
     * Sets the text direction using a {@link Direction} enum value.
     */
    public Builder direction(Direction direction) {
      this.direction = direction;
      return this;
    }

    /**
     * Sets the text direction from a string ("ltr", "rtl", or "auto", case-insensitive).
     */
    public Builder direction(String direction) {
      this.direction = Direction.of(direction);
      return this;
    }

    public Builder includeResolver(IncludeResolver resolver) {
      this.includeResolver = resolver;
      return this;
    }

    public Builder registerComponent(String tagName, ComponentFactory factory) {
      customComponents.put(tagName, factory);
      return this;
    }

    /**
     * Controls whether HTML special characters in attribute values are escaped in rendered output.
     * When enabled (the default), characters like {@code "}, {@code <}, {@code >}, and
     * {@code &} are escaped to prevent cross-site scripting (XSS) attacks.
     *
     * <p><strong>WARNING:</strong> Disabling output sanitization ({@code sanitizeOutput(false)})
     * allows raw, unescaped attribute values in the generated HTML. This creates a significant
     * XSS risk if any MJML attribute values originate from user input or untrusted sources.</p>
     *
     * <p>Only disable sanitization when <em>all</em> attribute values in the MJML template
     * come from trusted, developer-controlled sources (e.g., static templates with no
     * dynamic content).</p>
     *
     * @param sanitize {@code true} to escape attribute values (default), {@code false} to
     *                 output raw attribute values
     * @return this builder
     */
    public Builder sanitizeOutput(boolean sanitize) {
      if (!sanitize) {
        LOG.warning("sanitizeOutput disabled — attribute values will not be HTML-escaped. "
            + "Only disable this if all attribute values are from trusted sources.");
      }
      this.sanitizeOutput = sanitize;
      return this;
    }

    /**
     * Sets the maximum allowed input size in <strong>characters</strong> (not bytes).
     * Inputs exceeding this limit are rejected before processing.
     * Default is {@value #DEFAULT_MAX_INPUT_SIZE} (approximately 1 MB for ASCII).
     *
     * @param maxInputSize maximum number of characters allowed
     * @return this builder
     * @throws IllegalArgumentException if maxInputSize is not positive (validated at build time)
     */
    public Builder maxInputSize(int maxInputSize) {
      this.maxInputSize = maxInputSize;
      return this;
    }

    public Builder maxNestingDepth(int maxNestingDepth) {
      this.maxNestingDepth = maxNestingDepth;
      return this;
    }

    /**
     * Sets an optional content sanitizer that is applied to the inner HTML of
     * {@code <mj-text>}, {@code <mj-button>}, and {@code <mj-raw>} elements.
     * Pass {@code null} to disable (the default).
     */
    public Builder contentSanitizer(ContentSanitizer contentSanitizer) {
      this.contentSanitizer = contentSanitizer;
      return this;
    }

    public MjmlConfiguration build() {
      Objects.requireNonNull(language, "language must not be null");
      Objects.requireNonNull(direction, "direction must not be null");
      if (maxInputSize <= 0) {
        throw new IllegalArgumentException("maxInputSize must be positive, got: " + maxInputSize);
      }
      if (maxNestingDepth <= 0) {
        throw new IllegalArgumentException("maxNestingDepth must be positive, got: " + maxNestingDepth);
      }
      return new MjmlConfiguration(this);
    }
  }
}
