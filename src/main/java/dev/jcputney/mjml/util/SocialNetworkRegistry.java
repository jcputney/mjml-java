package dev.jcputney.mjml.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Registry of known social networks with default icon URLs, colors, and share URLs.
 * Used by {@code MjSocialElement} to resolve network-specific defaults from the
 * {@code name} attribute.
 */
public final class SocialNetworkRegistry {

  private SocialNetworkRegistry() {
  }

  /**
   * Describes a social network's display defaults.
   *
   * @param name            the human-readable display name
   * @param backgroundColor the default background hex color
   * @param shareUrl        the sharing URL pattern with {@code [[URL]]} placeholder
   */
  public record NetworkInfo(String name, String backgroundColor, String shareUrl) {
  }

  private static final String ICON_BASE = "https://www.mailjet.com/images/theme/v1/icons/ico-social/";

  /** Maps base network name to the icon filename (without .png extension) when they differ. */
  private static final Map<String, String> ICON_NAMES = Map.of(
      "google", "google-plus"
  );

  private static final Map<String, NetworkInfo> NETWORKS;

  static {
    Map<String, NetworkInfo> map = new LinkedHashMap<>();
    map.put("facebook", new NetworkInfo(
        "Facebook", "#3b5998",
        "https://www.facebook.com/sharer/sharer.php?u=[[URL]]"));
    map.put("twitter", new NetworkInfo(
        "Twitter", "#55acee",
        "https://twitter.com/intent/tweet?url=[[URL]]"));
    map.put("x", new NetworkInfo(
        "X", "#000000",
        "https://twitter.com/intent/tweet?url=[[URL]]"));
    map.put("google", new NetworkInfo(
        "Google", "#dc4e41",
        "https://plus.google.com/share?url=[[URL]]"));
    map.put("pinterest", new NetworkInfo(
        "Pinterest", "#bd081c",
        "https://pinterest.com/pin/create/button/?url=[[URL]]&media=&description="));
    map.put("linkedin", new NetworkInfo(
        "LinkedIn", "#0077b5",
        "https://www.linkedin.com/shareArticle?mini=true&url=[[URL]]&title=&summary=&source="));
    map.put("tumblr", new NetworkInfo(
        "Tumblr", "#35465c",
        "https://www.tumblr.com/widgets/share/tool?canonicalUrl=[[URL]]"));
    map.put("xing", new NetworkInfo(
        "Xing", "#296366",
        "https://www.xing.com/app/user?op=share&url=[[URL]]"));
    map.put("github", new NetworkInfo(
        "GitHub", "#000000",
        "[[URL]]"));
    map.put("instagram", new NetworkInfo(
        "Instagram", "#3f729b",
        "[[URL]]"));
    map.put("web", new NetworkInfo(
        "Web", "#4BADE9",
        "[[URL]]"));
    map.put("snapchat", new NetworkInfo(
        "Snapchat", "#FFFA54",
        "[[URL]]"));
    map.put("youtube", new NetworkInfo(
        "YouTube", "#EB3323",
        "[[URL]]"));
    map.put("vimeo", new NetworkInfo(
        "Vimeo", "#53B4E7",
        "[[URL]]"));
    map.put("medium", new NetworkInfo(
        "Medium", "#000000",
        "[[URL]]"));
    map.put("soundcloud", new NetworkInfo(
        "SoundCloud", "#EF7F31",
        "[[URL]]"));
    map.put("dribbble", new NetworkInfo(
        "Dribbble", "#EA4C89",
        "[[URL]]"));
    map.put("tiktok", new NetworkInfo(
        "TikTok", "#000000",
        "[[URL]]"));
    map.put("whatsapp", new NetworkInfo(
        "WhatsApp", "#25D366",
        "https://api.whatsapp.com/send?text=[[URL]]"));
    NETWORKS = Collections.unmodifiableMap(map);
  }

  /**
   * Returns the {@link NetworkInfo} for the given network name, or {@code null}
   * if the name is not recognized. The name may include a variant suffix such as
   * {@code "facebook-noshare"} &mdash; the base name is extracted by splitting on {@code '-'}.
   *
   * @param name the network identifier (case-insensitive, e.g. "facebook", "twitter", "linkedin")
   * @return the network info, or null if unknown
   */
  public static NetworkInfo getNetwork(String name) {
    if (name == null || name.isEmpty()) {
      return null;
    }
    String key = name.toLowerCase();
    NetworkInfo info = NETWORKS.get(key);
    if (info != null) {
      return info;
    }
    // Try the base name before any variant suffix (e.g. "facebook-noshare" -> "facebook")
    int dash = key.indexOf('-');
    if (dash > 0) {
      return NETWORKS.get(key.substring(0, dash));
    }
    return null;
  }

  /**
   * Returns the default icon URL for the given network name.
   * The URL follows the pattern:
   * {@code https://www.mailjet.com/images/theme/v1/icons/ico-social/{baseName}.png}
   *
   * @param name the network identifier
   * @return the icon URL, or an empty string if the name is null/empty
   */
  public static String getIconUrl(String name) {
    if (name == null || name.isEmpty()) {
      return "";
    }
    String baseName = name.toLowerCase();
    int dash = baseName.indexOf('-');
    if (dash > 0) {
      baseName = baseName.substring(0, dash);
    }
    String iconName = ICON_NAMES.getOrDefault(baseName, baseName);
    return ICON_BASE + iconName + ".png";
  }

  /**
   * Returns the unmodifiable map of all registered networks.
   */
  public static Map<String, NetworkInfo> getAllNetworks() {
    return NETWORKS;
  }
}
