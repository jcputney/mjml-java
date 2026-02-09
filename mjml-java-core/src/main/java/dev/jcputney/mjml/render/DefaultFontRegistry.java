package dev.jcputney.mjml.render;

import dev.jcputney.mjml.context.GlobalContext;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Registry of well-known web fonts that MJML auto-imports when used in font-family.
 * When a component uses one of these fonts, it is automatically registered in the
 * GlobalContext even without an explicit {@code <mj-font>} tag.
 */
public final class DefaultFontRegistry {

  private static final Map<String, String> DEFAULT_FONTS = new LinkedHashMap<>();

  static {
    DEFAULT_FONTS.put("Open Sans",
        "https://fonts.googleapis.com/css?family=Open+Sans:300,400,500,700");
    DEFAULT_FONTS.put("Droid Sans",
        "https://fonts.googleapis.com/css?family=Droid+Sans:300,400,500,700");
    DEFAULT_FONTS.put("Lato",
        "https://fonts.googleapis.com/css?family=Lato:300,400,500,700");
    DEFAULT_FONTS.put("Roboto",
        "https://fonts.googleapis.com/css?family=Roboto:300,400,500,700");
    DEFAULT_FONTS.put("Ubuntu",
        "https://fonts.googleapis.com/css?family=Ubuntu:300,400,500,700");
  }

  private DefaultFontRegistry() {
  }

  /**
   * Checks the given font-family string against known default fonts and registers
   * any matches in the GlobalContext (unless already registered by an explicit mj-font).
   */
  public static void registerUsedFonts(String fontFamily, GlobalContext ctx) {
    if (fontFamily == null || fontFamily.isEmpty()) {
      return;
    }

    // Check built-in default fonts
    for (Map.Entry<String, String> entry : DEFAULT_FONTS.entrySet()) {
      String fontName = entry.getKey();
      if (fontFamily.contains(fontName)) {
        boolean alreadyRegistered = ctx.getFonts().stream()
            .anyMatch(f -> f.name().equals(fontName));
        if (!alreadyRegistered) {
          String overrideUrl = ctx.getFontUrlOverride(fontName);
          String href = overrideUrl != null ? overrideUrl : entry.getValue();
          ctx.addFont(fontName, href);
        }
      }
    }

    // Check mj-font declared fonts (not in default registry)
    for (Map.Entry<String, String> override : ctx.getFontUrlOverrides().entrySet()) {
      String fontName = override.getKey();
      if (!DEFAULT_FONTS.containsKey(fontName) && fontFamily.contains(fontName)) {
        boolean alreadyRegistered = ctx.getFonts().stream()
            .anyMatch(f -> f.name().equals(fontName));
        if (!alreadyRegistered) {
          ctx.addFont(fontName, override.getValue());
        }
      }
    }
  }
}
