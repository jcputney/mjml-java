package dev.jcputney.mjml.context;

import dev.jcputney.mjml.MjmlConfiguration;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Document-wide context gathered during head processing.
 * Contains fonts, styles, attributes, title, preview text, breakpoint,
 * and media queries that apply across the entire document.
 *
 * <p>This class delegates to three focused sub-contexts:
 * <ul>
 *   <li>{@link MetadataContext} — title, previewText, breakpoint,
 *       containerWidth, bodyBackgroundColor, headComments, fileStartContent</li>
 *   <li>{@link StyleContext} — fonts, fontUrlOverrides, styles, componentStyles,
 *       inlineStyles, mediaQueries, fluidOnMobileUsed, registeredStyleKeys</li>
 *   <li>{@link AttributeContext} — defaultAttributes, classAttributes,
 *       htmlAttributes</li>
 * </ul>
 *
 * <p>All existing public methods are preserved as delegates for backward compatibility.
 * New code can access sub-contexts directly via {@link #metadata()}, {@link #styles()},
 * and {@link #attributes()}.
 *
 * <p><strong>Thread safety:</strong> This class is <em>not</em> thread-safe. Each
 * {@link dev.jcputney.mjml.render.RenderPipeline} creates its own instance, so
 * concurrent renders do not share a GlobalContext.</p>
 */
public class GlobalContext {

  private final MjmlConfiguration configuration;
  private final MetadataContext metadata;
  private final StyleContext styleContext;
  private final AttributeContext attributeContext;

  public GlobalContext(MjmlConfiguration configuration) {
    this.configuration = configuration;
    this.metadata = new MetadataContext();
    this.styleContext = new StyleContext();
    this.attributeContext = new AttributeContext();
  }

  // --- Sub-context accessors ---

  /**
   * Returns the document metadata sub-context (title, preview, breakpoint, etc.).
   */
  public MetadataContext metadata() {
    return metadata;
  }

  /**
   * Returns the style sub-context (fonts, CSS, media queries, etc.).
   */
  public StyleContext styles() {
    return styleContext;
  }

  /**
   * Returns the attribute cascade sub-context (defaults, classes, HTML attributes).
   */
  public AttributeContext attributes() {
    return attributeContext;
  }

  // --- Configuration ---

  public MjmlConfiguration getConfiguration() {
    return configuration;
  }

  // --- Metadata delegates ---

  public String getTitle() {
    return metadata.getTitle();
  }

  public void setTitle(String title) {
    metadata.setTitle(title);
  }

  public String getPreviewText() {
    return metadata.getPreviewText();
  }

  public void setPreviewText(String previewText) {
    metadata.setPreviewText(previewText);
  }

  public String getBreakpoint() {
    return metadata.getBreakpoint();
  }

  public int getBreakpointPx() {
    return metadata.getBreakpointPx();
  }

  public void setBreakpoint(String breakpoint) {
    metadata.setBreakpoint(breakpoint);
  }

  public int getContainerWidth() {
    return metadata.getContainerWidth();
  }

  public void setContainerWidth(int containerWidth) {
    metadata.setContainerWidth(containerWidth);
  }

  public String getBodyBackgroundColor() {
    return metadata.getBodyBackgroundColor();
  }

  public void setBodyBackgroundColor(String bodyBackgroundColor) {
    metadata.setBodyBackgroundColor(bodyBackgroundColor);
  }

  public void addHeadComment(String comment) {
    metadata.addHeadComment(comment);
  }

  public List<String> getHeadComments() {
    return metadata.getHeadComments();
  }

  public void addFileStartContent(String content) {
    metadata.addFileStartContent(content);
  }

  public List<String> getFileStartContent() {
    return metadata.getFileStartContent();
  }

  // --- Style delegates ---

  public void addFont(String name, String href) {
    styleContext.addFont(name, href);
  }

  public Set<FontDef> getFonts() {
    return styleContext.getFonts();
  }

  public void registerFontOverride(String name, String href) {
    styleContext.registerFontOverride(name, href);
  }

  public String getFontUrlOverride(String name) {
    return styleContext.getFontUrlOverride(name);
  }

  public Map<String, String> getFontUrlOverrides() {
    return styleContext.getFontUrlOverrides();
  }

  public void addStyle(String css) {
    styleContext.addStyle(css);
  }

  /**
   * Adds a style block only if a block with the given key hasn't been registered yet.
   * Returns true if the style was added, false if it was already registered.
   */
  public boolean addStyleOnce(String key, String css) {
    return styleContext.addStyleOnce(key, css);
  }

  /**
   * Adds a component-level style (e.g., hamburger CSS) that goes in the
   * fluid-on-mobile style block rather than as a separate style block.
   */
  public void addComponentStyle(String css) {
    styleContext.addComponentStyle(css);
  }

  public List<String> getComponentStyles() {
    return styleContext.getComponentStyles();
  }

  public void addInlineStyle(String css) {
    styleContext.addInlineStyle(css);
  }

  public List<String> getStyles() {
    return styleContext.getStyles();
  }

  public List<String> getInlineStyles() {
    return styleContext.getInlineStyles();
  }

  public void addMediaQuery(String className, String widthValue, String widthUnit) {
    styleContext.addMediaQuery(className, widthValue, widthUnit);
  }

  public Set<MediaQuery> getMediaQueries() {
    return styleContext.getMediaQueries();
  }

  public boolean isFluidOnMobileUsed() {
    return styleContext.isFluidOnMobileUsed();
  }

  public void setFluidOnMobileUsed(boolean fluidOnMobileUsed) {
    styleContext.setFluidOnMobileUsed(fluidOnMobileUsed);
  }

  // --- Attribute delegates ---

  public void setDefaultAttributes(String tagName, Map<String, String> attrs) {
    attributeContext.setDefaultAttributes(tagName, attrs);
  }

  public Map<String, String> getDefaultAttributes(String tagName) {
    return attributeContext.getDefaultAttributes(tagName);
  }

  public Map<String, String> getAllDefaults() {
    return attributeContext.getAllDefaults();
  }

  public void setClassAttributes(String className, Map<String, String> attrs) {
    attributeContext.setClassAttributes(className, attrs);
  }

  public Map<String, String> getClassAttributes(String className) {
    return attributeContext.getClassAttributes(className);
  }

  public void setHtmlAttributes(String selector, Map<String, String> attrs) {
    attributeContext.setHtmlAttributes(selector, attrs);
  }

  public Map<String, Map<String, String>> getHtmlAttributes() {
    return attributeContext.getHtmlAttributes();
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
