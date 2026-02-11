package dev.jcputney.mjml.spring;

import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Spring Boot configuration properties for mjml-java.
 *
 * <p>Bound from the {@code spring.mjml.*} namespace.
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

  /** Creates a new {@code MjmlProperties} instance with default values. */
  public MjmlProperties() {}

  /**
   * Returns the HTML {@code lang} attribute used in rendered output.
   *
   * @return the language code
   */
  public String getLanguage() {
    return language;
  }

  /**
   * Sets the HTML {@code lang} attribute used in rendered output.
   *
   * @param language the language code
   */
  public void setLanguage(String language) {
    this.language = language;
  }

  /**
   * Returns the text direction ({@code auto}, {@code ltr}, or {@code rtl}).
   *
   * @return the text direction
   */
  public String getDirection() {
    return direction;
  }

  /**
   * Sets the text direction ({@code auto}, {@code ltr}, or {@code rtl}).
   *
   * @param direction the text direction
   */
  public void setDirection(String direction) {
    this.direction = direction;
  }

  /**
   * Returns whether attribute values are HTML-escaped in rendered output.
   *
   * @return {@code true} if output sanitization is enabled
   */
  public boolean isSanitizeOutput() {
    return sanitizeOutput;
  }

  /**
   * Sets whether attribute values are HTML-escaped in rendered output.
   *
   * @param sanitizeOutput {@code true} to enable output sanitization
   */
  public void setSanitizeOutput(boolean sanitizeOutput) {
    this.sanitizeOutput = sanitizeOutput;
  }

  /**
   * Returns the maximum MJML input size in characters.
   *
   * @return the maximum input size
   */
  public int getMaxInputSize() {
    return maxInputSize;
  }

  /**
   * Sets the maximum MJML input size in characters.
   *
   * @param maxInputSize the maximum input size
   */
  public void setMaxInputSize(int maxInputSize) {
    this.maxInputSize = maxInputSize;
  }

  /**
   * Returns the maximum XML nesting depth allowed during parse.
   *
   * @return the maximum nesting depth
   */
  public int getMaxNestingDepth() {
    return maxNestingDepth;
  }

  /**
   * Sets the maximum XML nesting depth allowed during parse.
   *
   * @param maxNestingDepth the maximum nesting depth
   */
  public void setMaxNestingDepth(int maxNestingDepth) {
    this.maxNestingDepth = maxNestingDepth;
  }

  /**
   * Returns the maximum nested include depth allowed during include expansion.
   *
   * @return the maximum include depth
   */
  public int getMaxIncludeDepth() {
    return maxIncludeDepth;
  }

  /**
   * Sets the maximum nested include depth allowed during include expansion.
   *
   * @param maxIncludeDepth the maximum include depth
   */
  public void setMaxIncludeDepth(int maxIncludeDepth) {
    this.maxIncludeDepth = maxIncludeDepth;
  }

  /**
   * Returns the base location used to resolve relative include paths.
   *
   * @return the template location
   */
  public String getTemplateLocation() {
    return templateLocation;
  }

  /**
   * Sets the base location used to resolve relative include paths.
   *
   * @param templateLocation the template location
   */
  public void setTemplateLocation(String templateLocation) {
    this.templateLocation = templateLocation;
  }

  /**
   * Returns the allowed resource schemes for include paths.
   *
   * @return the set of allowed schemes
   */
  public Set<String> getIncludeAllowedSchemes() {
    return includeAllowedSchemes;
  }

  /**
   * Sets the allowed resource schemes for include paths.
   *
   * @param includeAllowedSchemes the set of allowed schemes
   */
  public void setIncludeAllowedSchemes(Set<String> includeAllowedSchemes) {
    this.includeAllowedSchemes = includeAllowedSchemes;
  }

  /**
   * Returns whether Thymeleaf integration auto-configuration is enabled.
   *
   * @return {@code true} if Thymeleaf integration is enabled, or {@code null} if unset
   */
  public Boolean getThymeleafEnabled() {
    return thymeleafEnabled;
  }

  /**
   * Sets whether Thymeleaf integration auto-configuration is enabled.
   *
   * @param thymeleafEnabled {@code true} to enable Thymeleaf integration
   */
  public void setThymeleafEnabled(Boolean thymeleafEnabled) {
    this.thymeleafEnabled = thymeleafEnabled;
  }
}
