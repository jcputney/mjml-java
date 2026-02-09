package dev.jcputney.mjml.resolver;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlIncludeException;
import dev.jcputney.mjml.ResolverContext;
import java.time.Duration;
import java.net.http.HttpClient;
import org.junit.jupiter.api.Test;

class UrlIncludeResolverTest {

  private static final ResolverContext CTX = ResolverContext.root("mjml");

  @Test
  void httpsOnlyRejectsHttp() {
    var resolver = UrlIncludeResolver.builder().build();
    var ex = assertThrows(MjmlIncludeException.class,
        () -> resolver.resolve("http://example.com/template.mjml", CTX));
    assertTrue(ex.getMessage().contains("Only HTTPS"));
  }

  @Test
  void invalidUrlThrows() {
    var resolver = UrlIncludeResolver.builder()
        .httpsOnly(false)
        .build();
    assertThrows(MjmlIncludeException.class,
        () -> resolver.resolve("not a url at all [}", CTX));
  }

  @Test
  void noSchemeThrows() {
    var resolver = UrlIncludeResolver.builder()
        .httpsOnly(false)
        .build();
    // URI with no scheme parses but has null scheme
    var ex = assertThrows(MjmlIncludeException.class,
        () -> resolver.resolve("//example.com/path", CTX));
    assertTrue(ex.getMessage().contains("no scheme"));
  }

  @Test
  void unsupportedSchemeThrows() {
    var resolver = UrlIncludeResolver.builder()
        .httpsOnly(false)
        .build();
    var ex = assertThrows(MjmlIncludeException.class,
        () -> resolver.resolve("ftp://example.com/file", CTX));
    assertTrue(ex.getMessage().contains("Unsupported URL scheme"));
  }

  @Test
  void ssrfBlocksLoopback127() {
    var resolver = UrlIncludeResolver.builder()
        .httpsOnly(false)
        .build();
    var ex = assertThrows(MjmlIncludeException.class,
        () -> resolver.resolve("http://127.0.0.1/template.mjml", CTX));
    assertTrue(ex.getMessage().contains("SSRF"));
  }

  @Test
  void ssrfBlocksLocalhost() {
    var resolver = UrlIncludeResolver.builder()
        .httpsOnly(false)
        .build();
    var ex = assertThrows(MjmlIncludeException.class,
        () -> resolver.resolve("http://localhost/template.mjml", CTX));
    assertTrue(ex.getMessage().contains("SSRF"));
  }

  @Test
  void ssrfBlocks10PrivateRange() {
    var resolver = UrlIncludeResolver.builder()
        .httpsOnly(false)
        .build();
    // 10.0.0.1 is site-local
    var ex = assertThrows(MjmlIncludeException.class,
        () -> resolver.resolve("http://10.0.0.1/template.mjml", CTX));
    assertTrue(ex.getMessage().contains("SSRF"));
  }

  @Test
  void ssrfBlocks192168PrivateRange() {
    var resolver = UrlIncludeResolver.builder()
        .httpsOnly(false)
        .build();
    var ex = assertThrows(MjmlIncludeException.class,
        () -> resolver.resolve("http://192.168.1.1/template.mjml", CTX));
    assertTrue(ex.getMessage().contains("SSRF"));
  }

  @Test
  void ssrfBlocksLinkLocal() {
    var resolver = UrlIncludeResolver.builder()
        .httpsOnly(false)
        .build();
    // 169.254.x.x is link-local
    var ex = assertThrows(MjmlIncludeException.class,
        () -> resolver.resolve("http://169.254.169.254/metadata", CTX));
    assertTrue(ex.getMessage().contains("SSRF"));
  }

  @Test
  void hostDenylistBlocks() {
    var resolver = UrlIncludeResolver.builder()
        .httpsOnly(false)
        .deniedHosts("evil.com")
        .build();
    var ex = assertThrows(MjmlIncludeException.class,
        () -> resolver.resolve("http://evil.com/template.mjml", CTX));
    assertTrue(ex.getMessage().contains("denied"));
  }

  @Test
  void hostDenylistIsCaseInsensitive() {
    var resolver = UrlIncludeResolver.builder()
        .httpsOnly(false)
        .deniedHosts("evil.com")
        .build();
    var ex = assertThrows(MjmlIncludeException.class,
        () -> resolver.resolve("http://EVIL.COM/template.mjml", CTX));
    // URI.getHost() lowercases domain names
    assertTrue(ex.getMessage().contains("denied"));
  }

  @Test
  void hostAllowlistBlocksUnlisted() {
    var resolver = UrlIncludeResolver.builder()
        .httpsOnly(false)
        .allowedHosts("trusted.com")
        .build();
    var ex = assertThrows(MjmlIncludeException.class,
        () -> resolver.resolve("http://other.com/template.mjml", CTX));
    assertTrue(ex.getMessage().contains("not in allowlist"));
  }

  @Test
  void noHostThrows() {
    var resolver = UrlIncludeResolver.builder()
        .httpsOnly(false)
        .build();
    var ex = assertThrows(MjmlIncludeException.class,
        () -> resolver.resolve("http:///path", CTX));
    assertTrue(ex.getMessage().contains("no host"));
  }

  @Test
  void builderDefaultsToHttpsOnly() {
    var resolver = UrlIncludeResolver.builder().build();
    var ex = assertThrows(MjmlIncludeException.class,
        () -> resolver.resolve("http://example.com/x", CTX));
    assertTrue(ex.getMessage().contains("Only HTTPS"));
  }

  @Test
  void customHttpClientAccepted() {
    var client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(1))
        .followRedirects(HttpClient.Redirect.NEVER)
        .build();
    var resolver = UrlIncludeResolver.builder()
        .httpClient(client)
        .httpsOnly(false)
        .build();
    // Loopback SSRF will block this, confirming SSRF still works with custom client
    assertThrows(MjmlIncludeException.class,
        () -> resolver.resolve("http://127.0.0.1/template.mjml", CTX));
  }

  @Test
  void unresolvedHostThrows() {
    var resolver = UrlIncludeResolver.builder()
        .httpsOnly(false)
        .build();
    var ex = assertThrows(MjmlIncludeException.class,
        () -> resolver.resolve("http://this-host-does-not-exist-xyz123.invalid/path", CTX));
    assertTrue(ex.getMessage().contains("Cannot resolve host"));
  }

  @Test
  void timeoutConfigurationAccepted() {
    var resolver = UrlIncludeResolver.builder()
        .connectTimeout(Duration.ofSeconds(1))
        .readTimeout(Duration.ofSeconds(2))
        .build();
    // Just verify it builds; actual timeout behavior requires real HTTP calls
    assertThrows(MjmlIncludeException.class,
        () -> resolver.resolve("http://example.com/x", CTX));
  }

  @Test
  void maxResponseSizeConfigurationAccepted() {
    var resolver = UrlIncludeResolver.builder()
        .maxResponseSize(512)
        .build();
    // Just verify it builds
    assertThrows(MjmlIncludeException.class,
        () -> resolver.resolve("http://example.com/x", CTX));
  }

  @Test
  void ssrfBlocksIPv6Loopback() {
    var resolver = UrlIncludeResolver.builder()
        .httpsOnly(false)
        .build();
    var ex = assertThrows(MjmlIncludeException.class,
        () -> resolver.resolve("http://[::1]/template.mjml", CTX));
    assertTrue(ex.getMessage().contains("SSRF"));
  }

  @Test
  void ssrfBlocksZeroAddress() {
    var resolver = UrlIncludeResolver.builder()
        .httpsOnly(false)
        .build();
    var ex = assertThrows(MjmlIncludeException.class,
        () -> resolver.resolve("http://0.0.0.0/template.mjml", CTX));
    assertTrue(ex.getMessage().contains("SSRF"));
  }
}
