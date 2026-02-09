package dev.jcputney.mjml.render;

import dev.jcputney.mjml.MjmlConfiguration;
import dev.jcputney.mjml.component.BaseComponent;
import dev.jcputney.mjml.component.ComponentRegistry;
import dev.jcputney.mjml.context.AttributeResolver;
import dev.jcputney.mjml.context.GlobalContext;
import dev.jcputney.mjml.context.RenderContext;
import dev.jcputney.mjml.parser.MjmlDocument;
import dev.jcputney.mjml.parser.MjmlNode;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Scans the MJML document tree for font-family attributes and auto-registers
 * default fonts used by components (e.g., Google Fonts).
 */
final class FontScanner {

  private static final Logger LOG = Logger.getLogger(FontScanner.class.getName());

  private final MjmlConfiguration configuration;
  private final ComponentRegistry registry;
  private final Map<String, Map<String, String>> defaultsCache = new HashMap<>();

  FontScanner(MjmlConfiguration configuration, ComponentRegistry registry) {
    this.configuration = configuration;
    this.registry = registry;
  }

  /**
   * Scans the body of the document for font-family attributes and registers
   * any default fonts found.
   */
  void registerDefaultFonts(MjmlDocument document, GlobalContext globalContext) {
    MjmlNode body = document.getBody();
    if (body == null) {
      return;
    }
    scanFontsRecursive(body, globalContext);
  }

  private void scanFontsRecursive(MjmlNode node, GlobalContext globalContext) {
    String tagName = node.getTagName();
    if (tagName != null && !tagName.startsWith("#")) {
      Map<String, String> defaults = getComponentDefaults(tagName);
      String fontFamily = AttributeResolver.resolve(node, "font-family", globalContext, defaults);
      if (fontFamily != null && !fontFamily.isEmpty()) {
        DefaultFontRegistry.registerUsedFonts(fontFamily, globalContext);
      }
    }
    for (MjmlNode child : node.getChildren()) {
      scanFontsRecursive(child, globalContext);
    }
  }

  private Map<String, String> getComponentDefaults(String tagName) {
    return defaultsCache.computeIfAbsent(tagName, tag -> {
      RenderContext dummyContext = new RenderContext(600);
      GlobalContext dummyGlobalContext = new GlobalContext(configuration);
      try {
        BaseComponent component = registry.createComponent(
            new MjmlNode(tag), dummyGlobalContext, dummyContext);
        return component != null ? component.getDefaultAttributes() : Map.of();
      } catch (Exception e) {
        LOG.warning(() -> "Could not get defaults for tag <" + tag + ">: " + e.getMessage());
        return Map.of();
      }
    });
  }
}
