package dev.jcputney.mjml.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * CSS and font state gathered during head processing. Contains fonts, font URL overrides, styles,
 * component styles, inline styles, media queries, and fluid-on-mobile tracking.
 *
 * <p>This is one of three focused sub-contexts extracted from {@link GlobalContext}. See also
 * {@link MetadataContext} and {@link AttributeContext}.
 *
 * <p><strong>Thread safety:</strong> This class is <em>not</em> thread-safe. Each render pipeline
 * creates its own instance.
 */
public class StyleContext {

  private final Set<FontDef> fonts = new LinkedHashSet<>();
  private final Map<String, String> fontUrlOverrides = new LinkedHashMap<>();
  private final List<String> styles = new ArrayList<>();
  private final List<String> componentStyles = new ArrayList<>();
  private final List<String> inlineStyles = new ArrayList<>();
  private final LinkedHashSet<MediaQuery> mediaQueries = new LinkedHashSet<>();
  private final Set<String> registeredStyleKeys = new HashSet<>();
  private boolean fluidOnMobileUsed = false;

  // Cached unmodifiable views (lazily initialized, invalidated on mutation)
  private Set<FontDef> unmodifiableFonts;
  private Map<String, String> unmodifiableFontUrlOverrides;
  private List<String> unmodifiableStyles;
  private List<String> unmodifiableComponentStyles;
  private List<String> unmodifiableInlineStyles;
  private Set<MediaQuery> unmodifiableMediaQueries;

  /** Creates a new empty style context with no fonts, styles, or media queries registered. */
  public StyleContext() {}

  /**
   * Registers a font with the given name and URL.
   *
   * @param name the font family name
   * @param href the URL to the font resource
   */
  public void addFont(String name, String href) {
    fonts.add(new FontDef(name, href));
    unmodifiableFonts = null;
  }

  /**
   * Returns an unmodifiable set of all registered font definitions.
   *
   * @return the registered fonts
   */
  public Set<FontDef> getFonts() {
    if (unmodifiableFonts == null) {
      unmodifiableFonts = Collections.unmodifiableSet(fonts);
    }
    return unmodifiableFonts;
  }

  /**
   * Registers a font URL override, replacing the default URL for the given font name.
   *
   * @param name the font family name to override
   * @param href the overriding URL for the font resource
   */
  public void registerFontOverride(String name, String href) {
    fontUrlOverrides.put(name, href);
    unmodifiableFontUrlOverrides = null;
  }

  /**
   * Returns the overridden URL for the given font name, or {@code null} if no override exists.
   *
   * @param name the font family name to look up
   * @return the overridden URL, or {@code null} if not overridden
   */
  public String getFontUrlOverride(String name) {
    return fontUrlOverrides.get(name);
  }

  /**
   * Returns an unmodifiable map of all font URL overrides, keyed by font name.
   *
   * @return the font URL overrides map
   */
  public Map<String, String> getFontUrlOverrides() {
    if (unmodifiableFontUrlOverrides == null) {
      unmodifiableFontUrlOverrides = Collections.unmodifiableMap(fontUrlOverrides);
    }
    return unmodifiableFontUrlOverrides;
  }

  /**
   * Adds a CSS style block. Blank or {@code null} values are ignored.
   *
   * @param css the CSS style string to add
   */
  public void addStyle(String css) {
    if (css != null && !css.isBlank()) {
      styles.add(css);
      unmodifiableStyles = null;
    }
  }

  /**
   * Adds a CSS style block only if a style with the given key has not already been registered. This
   * prevents duplicate style blocks for the same component type.
   *
   * @param key the unique key identifying this style block
   * @param css the CSS style string to add
   * @return {@code true} if the style was added, {@code false} if the key was already registered
   */
  public boolean addStyleOnce(String key, String css) {
    if (registeredStyleKeys.add(key)) {
      addStyle(css);
      return true;
    }
    return false;
  }

  /**
   * Adds a component-specific CSS style block. Blank or {@code null} values are ignored.
   *
   * @param css the component CSS style string to add
   */
  public void addComponentStyle(String css) {
    if (css != null && !css.isBlank()) {
      componentStyles.add(css);
      unmodifiableComponentStyles = null;
    }
  }

  /**
   * Returns an unmodifiable list of all registered component-specific CSS style blocks.
   *
   * @return the component styles
   */
  public List<String> getComponentStyles() {
    if (unmodifiableComponentStyles == null) {
      unmodifiableComponentStyles = Collections.unmodifiableList(componentStyles);
    }
    return unmodifiableComponentStyles;
  }

  /**
   * Adds an inline CSS style block. Blank or {@code null} values are ignored.
   *
   * @param css the inline CSS style string to add
   */
  public void addInlineStyle(String css) {
    if (css != null && !css.isBlank()) {
      inlineStyles.add(css);
      unmodifiableInlineStyles = null;
    }
  }

  /**
   * Returns an unmodifiable list of all registered CSS style blocks.
   *
   * @return the styles
   */
  public List<String> getStyles() {
    if (unmodifiableStyles == null) {
      unmodifiableStyles = Collections.unmodifiableList(styles);
    }
    return unmodifiableStyles;
  }

  /**
   * Returns an unmodifiable list of all registered inline CSS style blocks.
   *
   * @return the inline styles
   */
  public List<String> getInlineStyles() {
    if (unmodifiableInlineStyles == null) {
      unmodifiableInlineStyles = Collections.unmodifiableList(inlineStyles);
    }
    return unmodifiableInlineStyles;
  }

  /**
   * Adds a media query for responsive column width targeting the given CSS class.
   *
   * @param className the CSS class name for the responsive column
   * @param widthValue the numeric width value
   * @param widthUnit the unit for the width value (e.g. "%" or "px")
   */
  public void addMediaQuery(String className, String widthValue, String widthUnit) {
    mediaQueries.add(new MediaQuery(className, widthValue, widthUnit));
    unmodifiableMediaQueries = null;
  }

  /**
   * Returns an unmodifiable set of all registered media queries.
   *
   * @return the media queries
   */
  public Set<MediaQuery> getMediaQueries() {
    if (unmodifiableMediaQueries == null) {
      unmodifiableMediaQueries = Collections.unmodifiableSet(mediaQueries);
    }
    return unmodifiableMediaQueries;
  }

  /**
   * Returns whether any component has indicated that fluid-on-mobile behavior is used.
   *
   * @return {@code true} if fluid-on-mobile is used
   */
  public boolean isFluidOnMobileUsed() {
    return fluidOnMobileUsed;
  }

  /**
   * Sets whether fluid-on-mobile behavior is used in the current render.
   *
   * @param fluidOnMobileUsed {@code true} if fluid-on-mobile is used
   */
  public void setFluidOnMobileUsed(boolean fluidOnMobileUsed) {
    this.fluidOnMobileUsed = fluidOnMobileUsed;
  }

  /**
   * Font definition record.
   *
   * @param name the font family name
   * @param href the URL to the font resource
   */
  public record FontDef(String name, String href) {}

  /**
   * Media query definition for responsive column widths. widthValue is the numeric value, widthUnit
   * is "%" or "px".
   *
   * @param className the CSS class name for the responsive column
   * @param widthValue the numeric width value
   * @param widthUnit the unit for the width value (e.g. "%" or "px")
   */
  public record MediaQuery(String className, String widthValue, String widthUnit) {}
}
