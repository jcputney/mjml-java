package dev.jcputney.mjml;

import dev.jcputney.mjml.component.ComponentFactory;
import dev.jcputney.mjml.component.ContainerComponentFactory;
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
  /** Default maximum include depth. */
  public static final int DEFAULT_MAX_INCLUDE_DEPTH = 50;

  /** Default container width in pixels, matching the MJML v4 default of 600px. */
  public static final int DEFAULT_CONTAINER_WIDTH = 600;

  private final String language;
  private final Direction direction;
  private final IncludeResolver includeResolver;
  private final Map<String, ComponentFactory> customComponents;
  private final Map<String, ContainerComponentFactory> customContainerComponents;
  private final boolean sanitizeOutput;
  private final int maxInputSize;
  private final int maxNestingDepth;
  private final int maxIncludeDepth;
  private final ContentSanitizer contentSanitizer;

  private MjmlConfiguration(Builder builder) {
    this.language = builder.language;
    this.direction = builder.direction;
    this.includeResolver = builder.includeResolver;
    this.customComponents = Map.copyOf(builder.customComponents);
    this.customContainerComponents = Map.copyOf(builder.customContainerComponents);
    this.sanitizeOutput = builder.sanitizeOutput;
    this.maxInputSize = builder.maxInputSize;
    this.maxNestingDepth = builder.maxNestingDepth;
    this.maxIncludeDepth = builder.maxIncludeDepth;
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
   * Returns the custom container component factories registered via
   * {@link Builder#registerContainerComponent(String, ContainerComponentFactory)}.
   * Container components receive the {@link dev.jcputney.mjml.component.ComponentRegistry}
   * as a fourth argument, allowing them to instantiate and render child components.
   */
  public Map<String, ContainerComponentFactory> getCustomContainerComponents() {
    return customContainerComponents;
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
   * Maximum allowed include nesting depth for {@code mj-include}. Exceeding this depth
   * during include resolution throws an exception. Default is 50.
   */
  public int getMaxIncludeDepth() {
    return maxIncludeDepth;
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
    b.customContainerComponents.putAll(this.customContainerComponents);
    b.sanitizeOutput = this.sanitizeOutput;
    b.maxInputSize = this.maxInputSize;
    b.maxNestingDepth = this.maxNestingDepth;
    b.maxIncludeDepth = this.maxIncludeDepth;
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
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MjmlConfiguration that)) {
      return false;
    }
    return sanitizeOutput == that.sanitizeOutput
        && maxInputSize == that.maxInputSize
        && maxNestingDepth == that.maxNestingDepth
        && maxIncludeDepth == that.maxIncludeDepth
        && Objects.equals(language, that.language)
        && direction == that.direction
        && includeResolver == that.includeResolver
        && Objects.equals(customComponents, that.customComponents)
        && Objects.equals(customContainerComponents, that.customContainerComponents)
        && contentSanitizer == that.contentSanitizer;
  }

  @Override
  public int hashCode() {
    int result = Objects.hashCode(language);
    result = 31 * result + Objects.hashCode(direction);
    result = 31 * result + System.identityHashCode(includeResolver);
    result = 31 * result + customComponents.hashCode();
    result = 31 * result + customContainerComponents.hashCode();
    result = 31 * result + Boolean.hashCode(sanitizeOutput);
    result = 31 * result + maxInputSize;
    result = 31 * result + maxNestingDepth;
    result = 31 * result + maxIncludeDepth;
    result = 31 * result + System.identityHashCode(contentSanitizer);
    return result;
  }

  @Override
  public String toString() {
    return "MjmlConfiguration{"
        + "language='" + language + '\''
        + ", direction='" + direction + '\''
        + ", sanitizeOutput=" + sanitizeOutput
        + ", maxInputSize=" + maxInputSize
        + ", maxNestingDepth=" + maxNestingDepth
        + ", maxIncludeDepth=" + maxIncludeDepth
        + ", customComponents=" + customComponents.size()
        + ", customContainerComponents=" + customContainerComponents.size()
        + ", includeResolver=" + (includeResolver != null ? includeResolver.getClass().getSimpleName() : "null")
        + ", contentSanitizer=" + (contentSanitizer != null ? "configured" : "null")
        + '}';
  }

  public static class Builder {

    private String language = "und";
    private Direction direction = Direction.AUTO;
    private IncludeResolver includeResolver;
    private final Map<String, ComponentFactory> customComponents = new LinkedHashMap<>();
    private final Map<String, ContainerComponentFactory> customContainerComponents =
        new LinkedHashMap<>();
    private boolean sanitizeOutput = true;
    private int maxInputSize = DEFAULT_MAX_INPUT_SIZE;
    private int maxNestingDepth = DEFAULT_MAX_NESTING_DEPTH;
    private int maxIncludeDepth = DEFAULT_MAX_INCLUDE_DEPTH;
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
     * Registers a custom container component factory for the given tag name. Container
     * components receive a {@link dev.jcputney.mjml.component.ComponentRegistry} as a fourth
     * argument, allowing them to instantiate and render child MJML components via
     * {@link dev.jcputney.mjml.component.BodyComponent#renderChildren(dev.jcputney.mjml.component.ComponentRegistry)}.
     *
     * <p>Container component registrations are applied after standard custom components,
     * so they can override both built-in and previously registered tags.</p>
     *
     * @param tagName the MJML tag name (e.g., {@code "mj-card"})
     * @param factory the factory that creates component instances
     * @return this builder
     */
    public Builder registerContainerComponent(String tagName,
        ContainerComponentFactory factory) {
      customContainerComponents.put(tagName, factory);
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
     * Sets the maximum allowed include nesting depth for {@code mj-include}.
     */
    public Builder maxIncludeDepth(int maxIncludeDepth) {
      this.maxIncludeDepth = maxIncludeDepth;
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
      if (maxIncludeDepth <= 0) {
        throw new IllegalArgumentException("maxIncludeDepth must be positive, got: " + maxIncludeDepth);
      }
      return new MjmlConfiguration(this);
    }
  }
}
