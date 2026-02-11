package dev.jcputney.mjml.render;

import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.StyleContext;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Registry of well-known web fonts that MJML auto-imports when used in font-family. When a
 * component uses one of these fonts, it is automatically registered in the GlobalContext even
 * without an explicit {@code <mj-font>} tag.
 */
public final class DefaultFontRegistry {

  private static final Map<String, String> DEFAULT_FONTS = new LinkedHashMap<>();

  static {
    DEFAULT_FONTS.put(
        "Open Sans", "https://fonts.googleapis.com/css?family=Open+Sans:300,400,500,700");
    DEFAULT_FONTS.put(
        "Droid Sans", "https://fonts.googleapis.com/css?family=Droid+Sans:300,400,500,700");
    DEFAULT_FONTS.put("Lato", "https://fonts.googleapis.com/css?family=Lato:300,400,500,700");
    DEFAULT_FONTS.put("Roboto", "https://fonts.googleapis.com/css?family=Roboto:300,400,500,700");
    DEFAULT_FONTS.put("Ubuntu", "https://fonts.googleapis.com/css?family=Ubuntu:300,400,500,700");
  }

  private DefaultFontRegistry() {}

  /**
   * Checks the given font-family string against known default fonts and registers any matches in
   * the GlobalContext (unless already registered by an explicit mj-font).
   *
   * @param fontFamily the CSS font-family string to check for known fonts
   * @param ctx the global context to register discovered fonts in
   */
  public static void registerUsedFonts(String fontFamily, GlobalContext ctx) {
    if (fontFamily == null || fontFamily.isEmpty()) {
      return;
    }

    Set<String> registeredNames = buildRegisteredNameSet(ctx);
    registerUsedFonts(fontFamily, ctx, registeredNames);
  }

  /**
   * Checks the given font-family string against known default fonts and registers any matches in
   * the GlobalContext (unless already registered by an explicit mj-font). This overload accepts a
   * pre-built set of registered font names, avoiding the overhead of rebuilding it on every call.
   * The caller is responsible for keeping the set in sync (the set is mutated in place when fonts
   * are added).
   *
   * @param fontFamily the CSS font-family string to check for known fonts
   * @param ctx the global context to register discovered fonts in
   * @param registeredNames mutable set of already-registered font names for O(1) lookup
   */
  public static void registerUsedFonts(
      String fontFamily, GlobalContext ctx, Set<String> registeredNames) {
    if (fontFamily == null || fontFamily.isEmpty()) {
      return;
    }

    // Check built-in default fonts
    for (Map.Entry<String, String> entry : DEFAULT_FONTS.entrySet()) {
      String fontName = entry.getKey();
      if (fontFamily.contains(fontName) && !registeredNames.contains(fontName)) {
        String overrideUrl = ctx.styles().getFontUrlOverride(fontName);
        String href = overrideUrl != null ? overrideUrl : entry.getValue();
        ctx.styles().addFont(fontName, href);
        registeredNames.add(fontName);
      }
    }

    // Check mj-font declared fonts (not in default registry)
    for (Map.Entry<String, String> override : ctx.styles().getFontUrlOverrides().entrySet()) {
      String fontName = override.getKey();
      if (!DEFAULT_FONTS.containsKey(fontName)
          && fontFamily.contains(fontName)
          && !registeredNames.contains(fontName)) {
        ctx.styles().addFont(fontName, override.getValue());
        registeredNames.add(fontName);
      }
    }
  }

  /**
   * Builds a mutable set of currently registered font names from the given context.
   *
   * @param ctx the global context to read font registrations from
   * @return a mutable set of registered font names
   */
  public static Set<String> buildRegisteredNameSet(GlobalContext ctx) {
    Set<String> registeredNames = new java.util.HashSet<>();
    for (StyleContext.FontDef font : ctx.styles().getFonts()) {
      registeredNames.add(font.name());
    }
    return registeredNames;
  }
}
