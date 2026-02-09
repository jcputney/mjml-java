package dev.jcputney.mjml.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the MJML renderer.
 */
@ConfigurationProperties(prefix = "spring.mjml")
public class MjmlProperties {

  private String language = "und";
  private String direction = "auto";
  private boolean sanitizeOutput = true;
  private int maxInputSize = 1_048_576;
  private int maxNestingDepth = 100;
  private String templateLocation = "classpath:mjml/";
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

  public String getTemplateLocation() {
    return templateLocation;
  }

  public void setTemplateLocation(String templateLocation) {
    this.templateLocation = templateLocation;
  }

  public Boolean getThymeleafEnabled() {
    return thymeleafEnabled;
  }

  public void setThymeleafEnabled(Boolean thymeleafEnabled) {
    this.thymeleafEnabled = thymeleafEnabled;
  }
}
