package dev.jcputney.mjml;

import dev.jcputney.mjml.component.ComponentFactory;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Configuration for the MJML renderer.
 * Use the {@link #builder()} method to create instances.
 */
public final class MjmlConfiguration {

  /** Default maximum input size: 1 MB. */
  public static final int DEFAULT_MAX_INPUT_SIZE = 1_048_576;

  /** Default maximum nesting depth. */
  public static final int DEFAULT_MAX_NESTING_DEPTH = 100;

  private final String language;
  private final String direction;
  private final IncludeResolver includeResolver;
  private final Map<String, ComponentFactory> customComponents;
  private final boolean sanitizeOutput;
  private final int maxInputSize;
  private final int maxNestingDepth;

  private MjmlConfiguration(Builder builder) {
    this.language = builder.language;
    this.direction = builder.direction;
    this.includeResolver = builder.includeResolver;
    this.customComponents = Map.copyOf(builder.customComponents);
    this.sanitizeOutput = builder.sanitizeOutput;
    this.maxInputSize = builder.maxInputSize;
    this.maxNestingDepth = builder.maxNestingDepth;
  }

  public String getLanguage() {
    return language;
  }

  /**
   * The text direction for the HTML document. Defaults to "auto".
   * Common values: "auto", "ltr", "rtl".
   */
  public String getDirection() {
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
   * Maximum allowed input size in bytes. Inputs exceeding this limit are rejected
   * before processing. Default is 1 MB.
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
        + '}';
  }

  public static class Builder {

    private String language = "und";
    private String direction = "auto";
    private IncludeResolver includeResolver;
    private final Map<String, ComponentFactory> customComponents = new LinkedHashMap<>();
    private boolean sanitizeOutput = true;
    private int maxInputSize = DEFAULT_MAX_INPUT_SIZE;
    private int maxNestingDepth = DEFAULT_MAX_NESTING_DEPTH;

    public Builder language(String language) {
      this.language = language;
      return this;
    }

    public Builder direction(String direction) {
      this.direction = direction;
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

    public Builder sanitizeOutput(boolean sanitize) {
      this.sanitizeOutput = sanitize;
      return this;
    }

    public Builder maxInputSize(int maxInputSize) {
      this.maxInputSize = maxInputSize;
      return this;
    }

    public Builder maxNestingDepth(int maxNestingDepth) {
      this.maxNestingDepth = maxNestingDepth;
      return this;
    }

    public MjmlConfiguration build() {
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
