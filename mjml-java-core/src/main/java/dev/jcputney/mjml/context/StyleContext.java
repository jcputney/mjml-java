package dev.jcputney.mjml.context;

import dev.jcputney.mjml.context.GlobalContext.FontDef;
import dev.jcputney.mjml.context.GlobalContext.MediaQuery;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * CSS and font state gathered during head processing.
 * Contains fonts, font URL overrides, styles, component styles, inline styles,
 * media queries, and fluid-on-mobile tracking.
 *
 * <p>This is one of three focused sub-contexts extracted from {@link GlobalContext}.
 * See also {@link MetadataContext} and {@link AttributeContext}.
 *
 * <p><strong>Thread safety:</strong> This class is <em>not</em> thread-safe.
 * Each render pipeline creates its own instance.</p>
 */
public class StyleContext {

  private final Set<FontDef> fonts = new LinkedHashSet<>();
  private final Map<String, String> fontUrlOverrides = new LinkedHashMap<>();
  private final List<String> styles = new ArrayList<>();
  private final List<String> componentStyles = new ArrayList<>();
  private final List<String> inlineStyles = new ArrayList<>();
  private final LinkedHashSet<MediaQuery> mediaQueries = new LinkedHashSet<>();
  private boolean fluidOnMobileUsed = false;
  private final Set<String> registeredStyleKeys = new HashSet<>();

  // Fonts
  public void addFont(String name, String href) {
    fonts.add(new FontDef(name, href));
  }

  public Set<FontDef> getFonts() {
    return Collections.unmodifiableSet(fonts);
  }

  public void registerFontOverride(String name, String href) {
    fontUrlOverrides.put(name, href);
  }

  public String getFontUrlOverride(String name) {
    return fontUrlOverrides.get(name);
  }

  public Map<String, String> getFontUrlOverrides() {
    return Collections.unmodifiableMap(fontUrlOverrides);
  }

  // Styles
  public void addStyle(String css) {
    if (css != null && !css.isBlank()) {
      styles.add(css);
    }
  }

  public boolean addStyleOnce(String key, String css) {
    if (registeredStyleKeys.add(key)) {
      addStyle(css);
      return true;
    }
    return false;
  }

  public void addComponentStyle(String css) {
    if (css != null && !css.isBlank()) {
      componentStyles.add(css);
    }
  }

  public List<String> getComponentStyles() {
    return Collections.unmodifiableList(componentStyles);
  }

  public void addInlineStyle(String css) {
    if (css != null && !css.isBlank()) {
      inlineStyles.add(css);
    }
  }

  public List<String> getStyles() {
    return Collections.unmodifiableList(styles);
  }

  public List<String> getInlineStyles() {
    return Collections.unmodifiableList(inlineStyles);
  }

  // Media queries
  public void addMediaQuery(String className, String widthValue, String widthUnit) {
    mediaQueries.add(new MediaQuery(className, widthValue, widthUnit));
  }

  public Set<MediaQuery> getMediaQueries() {
    return Collections.unmodifiableSet(mediaQueries);
  }

  public boolean isFluidOnMobileUsed() {
    return fluidOnMobileUsed;
  }

  public void setFluidOnMobileUsed(boolean fluidOnMobileUsed) {
    this.fluidOnMobileUsed = fluidOnMobileUsed;
  }
}
