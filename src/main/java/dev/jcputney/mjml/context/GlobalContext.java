package dev.jcputney.mjml.context;

import dev.jcputney.mjml.MjmlConfiguration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Document-wide context gathered during head processing.
 * Contains fonts, styles, attributes, title, preview text, breakpoint,
 * and media queries that apply across the entire document.
 *
 * <h3>Future decomposition target</h3>
 * <p>This class currently acts as a catch-all for all document-level state.
 * A future refactoring could split it into three focused contexts:
 * <ul>
 *   <li><b>MetadataContext</b> — title, previewText, breakpoint, language,
 *       containerWidth, bodyBackgroundColor, headComments, configuration</li>
 *   <li><b>StyleContext</b> — fonts, fontUrlOverrides, styles, componentStyles,
 *       inlineStyles, mediaQueries, fluidOnMobileUsed</li>
 *   <li><b>AttributeContext</b> — defaultAttributes, classAttributes,
 *       htmlAttributes</li>
 * </ul>
 * <p>This decomposition would clarify which concerns each component depends on
 * and prevent head components from accidentally coupling to rendering state.
 * GlobalContext would become a thin facade delegating to the three sub-contexts.
 */
public class GlobalContext {

  private final MjmlConfiguration configuration;
  private String title = "";
  private String previewText = "";
  private String breakpoint = "480px";
  private final Set<FontDef> fonts = new LinkedHashSet<>();
  private final Map<String, String> fontUrlOverrides = new LinkedHashMap<>();
  private final List<String> styles = new ArrayList<>();
  private final List<String> componentStyles = new ArrayList<>();
  private final List<String> inlineStyles = new ArrayList<>();
  private final Map<String, Map<String, String>> defaultAttributes = new LinkedHashMap<>();
  private final Map<String, Map<String, String>> classAttributes = new LinkedHashMap<>();
  private final LinkedHashSet<MediaQuery> mediaQueries = new LinkedHashSet<>();
  private boolean fluidOnMobileUsed = false;
  private final Map<String, Map<String, String>> htmlAttributes = new LinkedHashMap<>();
  private int containerWidth = 600;
  private String bodyBackgroundColor = "";
  private final List<String> headComments = new ArrayList<>();

  public GlobalContext(MjmlConfiguration configuration) {
    this.configuration = configuration;
  }

  public MjmlConfiguration getConfiguration() {
    return configuration;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title != null ? title : "";
  }

  public String getPreviewText() {
    return previewText;
  }

  public void setPreviewText(String previewText) {
    this.previewText = previewText != null ? previewText : "";
  }

  public String getBreakpoint() {
    return breakpoint;
  }

  public int getBreakpointPx() {
    return parsePixels(breakpoint, 480);
  }

  public void setBreakpoint(String breakpoint) {
    if (breakpoint != null && !breakpoint.isEmpty()) {
      this.breakpoint = breakpoint;
    }
  }

  public int getContainerWidth() {
    return containerWidth;
  }

  public void setContainerWidth(int containerWidth) {
    this.containerWidth = containerWidth;
  }

  public String getBodyBackgroundColor() {
    return bodyBackgroundColor;
  }

  public void setBodyBackgroundColor(String bodyBackgroundColor) {
    this.bodyBackgroundColor = bodyBackgroundColor != null ? bodyBackgroundColor : "";
  }

  // Fonts
  public void addFont(String name, String href) {
    fonts.add(new FontDef(name, href));
  }

  public Set<FontDef> getFonts() {
    return fonts;
  }

  public void registerFontOverride(String name, String href) {
    fontUrlOverrides.put(name, href);
  }

  public String getFontUrlOverride(String name) {
    return fontUrlOverrides.get(name);
  }

  public Map<String, String> getFontUrlOverrides() {
    return fontUrlOverrides;
  }

  // Styles
  public void addStyle(String css) {
    if (css != null && !css.isBlank()) {
      styles.add(css);
    }
  }

  /**
   * Adds a component-level style (e.g., hamburger CSS) that goes in the
   * fluid-on-mobile style block rather than as a separate style block.
   */
  public void addComponentStyle(String css) {
    if (css != null && !css.isBlank()) {
      componentStyles.add(css);
    }
  }

  public List<String> getComponentStyles() {
    return componentStyles;
  }

  public void addInlineStyle(String css) {
    if (css != null && !css.isBlank()) {
      inlineStyles.add(css);
    }
  }

  public List<String> getStyles() {
    return styles;
  }

  public List<String> getInlineStyles() {
    return inlineStyles;
  }

  // Default attributes (mj-all, mj-section, etc.)
  public void setDefaultAttributes(String tagName, Map<String, String> attrs) {
    defaultAttributes.computeIfAbsent(tagName, k -> new LinkedHashMap<>()).putAll(attrs);
  }

  public Map<String, String> getDefaultAttributes(String tagName) {
    return defaultAttributes.getOrDefault(tagName, Map.of());
  }

  public Map<String, String> getAllDefaults() {
    return defaultAttributes.getOrDefault("mj-all", Map.of());
  }

  // Class attributes
  public void setClassAttributes(String className, Map<String, String> attrs) {
    classAttributes.computeIfAbsent(className, k -> new LinkedHashMap<>()).putAll(attrs);
  }

  public Map<String, String> getClassAttributes(String className) {
    return classAttributes.getOrDefault(className, Map.of());
  }

  // Media queries
  public void addMediaQuery(String className, String widthValue, String widthUnit) {
    mediaQueries.add(new MediaQuery(className, widthValue, widthUnit));
  }

  public Set<MediaQuery> getMediaQueries() {
    return mediaQueries;
  }

  public boolean isFluidOnMobileUsed() {
    return fluidOnMobileUsed;
  }

  public void setFluidOnMobileUsed(boolean fluidOnMobileUsed) {
    this.fluidOnMobileUsed = fluidOnMobileUsed;
  }

  // Head comments
  public void addHeadComment(String comment) {
    headComments.add(comment);
  }

  public List<String> getHeadComments() {
    return headComments;
  }

  // HTML attributes
  public void setHtmlAttributes(String selector, Map<String, String> attrs) {
    htmlAttributes.computeIfAbsent(selector, k -> new LinkedHashMap<>()).putAll(attrs);
  }

  public Map<String, Map<String, String>> getHtmlAttributes() {
    return htmlAttributes;
  }

  // Helpers
  private static int parsePixels(String value, int defaultValue) {
    if (value == null || value.isEmpty()) {
      return defaultValue;
    }
    try {
      return Integer.parseInt(value.replace("px", "").trim());
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  /**
   * Font definition record.
   */
  public record FontDef(String name, String href) {
  }

  /**
   * Media query definition for responsive column widths.
   * widthValue is the numeric value, widthUnit is "%" or "px".
   */
  public record MediaQuery(String className, String widthValue, String widthUnit) {
  }
}
