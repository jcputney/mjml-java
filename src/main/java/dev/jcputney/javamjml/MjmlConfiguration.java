package dev.jcputney.javamjml;

import dev.jcputney.javamjml.component.ComponentFactory;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Configuration for the MJML renderer.
 * Use the {@link #builder()} method to create instances.
 */
public class MjmlConfiguration {

  private final String language;
  private final IncludeResolver includeResolver;
  private final Map<String, ComponentFactory> customComponents;

  private MjmlConfiguration(Builder builder) {
    this.language = builder.language;
    this.includeResolver = builder.includeResolver;
    this.customComponents = Map.copyOf(builder.customComponents);
  }

  public String getLanguage() {
    return language;
  }

  public IncludeResolver getIncludeResolver() {
    return includeResolver;
  }

  public Map<String, ComponentFactory> getCustomComponents() {
    return customComponents;
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * Returns a default configuration.
   */
  public static MjmlConfiguration defaults() {
    return new Builder().build();
  }

  public static class Builder {

    private String language = "und";
    private IncludeResolver includeResolver;
    private final Map<String, ComponentFactory> customComponents = new LinkedHashMap<>();

    public Builder language(String language) {
      this.language = language;
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

    public MjmlConfiguration build() {
      return new MjmlConfiguration(this);
    }
  }
}
