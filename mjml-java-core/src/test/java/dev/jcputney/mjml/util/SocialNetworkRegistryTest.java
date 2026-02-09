package dev.jcputney.mjml.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.util.SocialNetworkRegistry.NetworkInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SocialNetworkRegistryTest {

  @ParameterizedTest
  @ValueSource(strings = {
      "facebook", "twitter", "x", "google", "pinterest", "linkedin",
      "tumblr", "xing", "github", "instagram", "web", "snapchat",
      "youtube", "vimeo", "medium", "soundcloud", "dribbble", "tiktok", "whatsapp",
      "mail", "telegram", "reddit", "line"
  })
  void allRegisteredNetworksReturnCorrectInfo(String name) {
    NetworkInfo info = SocialNetworkRegistry.getNetwork(name);
    assertNotNull(info, "Network '" + name + "' should be registered");
    assertNotNull(info.name());
    assertNotNull(info.backgroundColor());
    assertTrue(info.backgroundColor().startsWith("#"));
    assertNotNull(info.shareUrl());
  }

  @Test
  void variantSuffixResolution() {
    NetworkInfo info = SocialNetworkRegistry.getNetwork("facebook-noshare");
    assertNotNull(info);
    assertEquals("Facebook", info.name());
  }

  @Test
  void unknownNetworkReturnsNull() {
    assertNull(SocialNetworkRegistry.getNetwork("nonexistent"));
  }

  @Test
  void nullReturnsNull() {
    assertNull(SocialNetworkRegistry.getNetwork(null));
  }

  @Test
  void emptyReturnsNull() {
    assertNull(SocialNetworkRegistry.getNetwork(""));
  }

  @Test
  void iconUrlGenerationPattern() {
    String url = SocialNetworkRegistry.getIconUrl("facebook");
    assertTrue(url.endsWith("facebook.png"));
    assertTrue(url.startsWith("https://"));
  }

  @Test
  void iconUrlVariantUsesBaseName() {
    String url = SocialNetworkRegistry.getIconUrl("facebook-noshare");
    assertTrue(url.endsWith("facebook.png"));
  }

  @Test
  void iconUrlGoogleMapsToGooglePlus() {
    String url = SocialNetworkRegistry.getIconUrl("google");
    assertTrue(url.endsWith("google-plus.png"));
  }

  @Test
  void iconUrlXMapsToTwitterX() {
    String url = SocialNetworkRegistry.getIconUrl("x");
    assertTrue(url.endsWith("twitter-x.png"));
  }

  @Test
  void iconUrlNullReturnsEmpty() {
    assertEquals("", SocialNetworkRegistry.getIconUrl(null));
  }

  @Test
  void iconUrlEmptyReturnsEmpty() {
    assertEquals("", SocialNetworkRegistry.getIconUrl(""));
  }

  @Test
  void mailNetworkDefaults() {
    NetworkInfo info = SocialNetworkRegistry.getNetwork("mail");
    assertNotNull(info);
    assertEquals("Mail", info.name());
    assertEquals("#000000", info.backgroundColor());
    assertTrue(info.shareUrl().contains("mailto:"));
  }

  @Test
  void telegramNetworkDefaults() {
    NetworkInfo info = SocialNetworkRegistry.getNetwork("telegram");
    assertNotNull(info);
    assertEquals("Telegram", info.name());
    assertEquals("#0088cc", info.backgroundColor());
    assertTrue(info.shareUrl().contains("t.me"));
  }

  @Test
  void redditNetworkDefaults() {
    NetworkInfo info = SocialNetworkRegistry.getNetwork("reddit");
    assertNotNull(info);
    assertEquals("Reddit", info.name());
    assertEquals("#FF4500", info.backgroundColor());
    assertTrue(info.shareUrl().contains("reddit.com"));
  }

  @Test
  void lineNetworkDefaults() {
    NetworkInfo info = SocialNetworkRegistry.getNetwork("line");
    assertNotNull(info);
    assertEquals("Line", info.name());
    assertEquals("#00B900", info.backgroundColor());
    assertTrue(info.shareUrl().contains("line.me"));
  }

  @Test
  void caseInsensitiveLookup() {
    NetworkInfo lower = SocialNetworkRegistry.getNetwork("facebook");
    NetworkInfo upper = SocialNetworkRegistry.getNetwork("Facebook");
    assertNotNull(lower);
    assertNotNull(upper);
    assertEquals(lower.name(), upper.name());
  }
}
