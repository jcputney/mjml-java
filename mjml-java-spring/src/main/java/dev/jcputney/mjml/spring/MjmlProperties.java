package dev.jcputney.mjml.spring;

import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Spring Boot configuration properties for mjml-java.
 *
 * <p>Bound from the {@code spring.mjml.*} namespace.</p>
 */
@ConfigurationProperties(prefix = "spring.mjml")
public class MjmlProperties {

  /** HTML {@code lang} attribute used in rendered output. */
  private String language = "und";
  /** Text direction: {@code auto}, {@code ltr}, or {@code rtl}. */
  private String direction = "auto";
  /** Whether to HTML-escape attribute values in rendered output. */
  private boolean sanitizeOutput = true;
  /** Maximum MJML input size in characters. */
  private int maxInputSize = 1_048_576;
  /** Maximum XML nesting depth allowed during parse. */
  private int maxNestingDepth = 100;
  /** Maximum nested include depth allowed during include expansion. */
  private int maxIncludeDepth = 50;
  /** Base location used to resolve relative include paths. */
  private String templateLocation = "classpath:mjml/";
  /** Allowed resource schemes for include paths. */
  private Set<String> includeAllowedSchemes = Set.of("classpath", "file");
  /** Optional toggle for Thymeleaf integration auto-configuration. */
  private Boolean thymeleafEnabled;

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getDirection() {
    return direction;
  }

  public void setDirection(String direction) {
    this.direction = direction;
  }

  public boolean isSanitizeOutput() {
    return sanitizeOutput;
  }

  public void setSanitizeOutput(boolean sanitizeOutput) {
    this.sanitizeOutput = sanitizeOutput;
  }

  public int getMaxInputSize() {
    return maxInputSize;
  }

  public void setMaxInputSize(int maxInputSize) {
    this.maxInputSize = maxInputSize;
  }

  public int getMaxNestingDepth() {
    return maxNestingDepth;
  }

  public void setMaxNestingDepth(int maxNestingDepth) {
    this.maxNestingDepth = maxNestingDepth;
  }

  public int getMaxIncludeDepth() {
    return maxIncludeDepth;
  }

  public void setMaxIncludeDepth(int maxIncludeDepth) {
    this.maxIncludeDepth = maxIncludeDepth;
  }

  public String getTemplateLocation() {
    return templateLocation;
  }

  public void setTemplateLocation(String templateLocation) {
    this.templateLocation = templateLocation;
  }

  public Set<String> getIncludeAllowedSchemes() {
    return includeAllowedSchemes;
  }

  public void setIncludeAllowedSchemes(Set<String> includeAllowedSchemes) {
    this.includeAllowedSchemes = includeAllowedSchemes;
  }

  public Boolean getThymeleafEnabled() {
    return thymeleafEnabled;
  }

  public void setThymeleafEnabled(Boolean thymeleafEnabled) {
    this.thymeleafEnabled = thymeleafEnabled;
  }
}
