package dev.jcputney.mjml.resolver;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.jcputney.mjml.MjmlIncludeException;
import dev.jcputney.mjml.ResolverContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import org.junit.jupiter.api.Test;

class UrlIncludeResolverTest {

  private static final ResolverContext CTX = ResolverContext.root("mjml");

  @Test
  void httpsOnlyRejectsHttp() {
    var resolver = UrlIncludeResolver.builder().build();
    var ex =
        assertThrows(
            MjmlIncludeException.class,
            () -> resolver.resolve("http://example.com/template.mjml", CTX));
    assertTrue(ex.getMessage().contains("Only HTTPS"));
  }

  @Test
  void invalidUrlThrows() {
    var resolver = UrlIncludeResolver.builder().httpsOnly(false).build();
    assertThrows(MjmlIncludeException.class, () -> resolver.resolve("not a url at all [}", CTX));
  }

  @Test
  void noSchemeThrows() {
    var resolver = UrlIncludeResolver.builder().httpsOnly(false).build();
    // URI with no scheme parses but has null scheme
    var ex =
        assertThrows(MjmlIncludeException.class, () -> resolver.resolve("//example.com/path", CTX));
    assertTrue(ex.getMessage().contains("no scheme"));
  }

  @Test
  void unsupportedSchemeThrows() {
    var resolver = UrlIncludeResolver.builder().httpsOnly(false).build();
    var ex =
        assertThrows(
            MjmlIncludeException.class, () -> resolver.resolve("ftp://example.com/file", CTX));
    assertTrue(ex.getMessage().contains("Unsupported URL scheme"));
  }

  @Test
  void ssrfBlocksLoopback127() {
    var resolver = UrlIncludeResolver.builder().httpsOnly(false).build();
    var ex =
        assertThrows(
            MjmlIncludeException.class,
            () -> resolver.resolve("http://127.0.0.1/template.mjml", CTX));
    assertTrue(ex.getMessage().contains("SSRF"));
  }

  @Test
  void ssrfBlocksLocalhost() {
    var resolver = UrlIncludeResolver.builder().httpsOnly(false).build();
    var ex =
        assertThrows(
            MjmlIncludeException.class,
            () -> resolver.resolve("http://localhost/template.mjml", CTX));
    assertTrue(ex.getMessage().contains("SSRF"));
  }

  @Test
  void ssrfBlocks10PrivateRange() {
    var resolver = UrlIncludeResolver.builder().httpsOnly(false).build();
    // 10.0.0.1 is site-local
    var ex =
        assertThrows(
            MjmlIncludeException.class,
            () -> resolver.resolve("http://10.0.0.1/template.mjml", CTX));
    assertTrue(ex.getMessage().contains("SSRF"));
  }

  @Test
  void ssrfBlocks192168PrivateRange() {
    var resolver = UrlIncludeResolver.builder().httpsOnly(false).build();
    var ex =
        assertThrows(
            MjmlIncludeException.class,
            () -> resolver.resolve("http://192.168.1.1/template.mjml", CTX));
    assertTrue(ex.getMessage().contains("SSRF"));
  }

  @Test
  void ssrfBlocksLinkLocal() {
    var resolver = UrlIncludeResolver.builder().httpsOnly(false).build();
    // 169.254.x.x is link-local
    var ex =
        assertThrows(
            MjmlIncludeException.class,
            () -> resolver.resolve("http://169.254.169.254/metadata", CTX));
    assertTrue(ex.getMessage().contains("SSRF"));
  }

  @Test
  void hostDenylistBlocks() {
    var resolver = UrlIncludeResolver.builder().httpsOnly(false).deniedHosts("evil.com").build();
    var ex =
        assertThrows(
            MjmlIncludeException.class,
            () -> resolver.resolve("http://evil.com/template.mjml", CTX));
    assertTrue(ex.getMessage().contains("denied"));
  }

  @Test
  void hostDenylistIsCaseInsensitive() {
    var resolver = UrlIncludeResolver.builder().httpsOnly(false).deniedHosts("evil.com").build();
    var ex =
        assertThrows(
            MjmlIncludeException.class,
            () -> resolver.resolve("http://EVIL.COM/template.mjml", CTX));
    // URI.getHost() lowercases domain names
    assertTrue(ex.getMessage().contains("denied"));
  }

  @Test
  void hostAllowlistBlocksUnlisted() {
    var resolver =
        UrlIncludeResolver.builder().httpsOnly(false).allowedHosts("trusted.com").build();
    var ex =
        assertThrows(
            MjmlIncludeException.class,
            () -> resolver.resolve("http://other.com/template.mjml", CTX));
    assertTrue(ex.getMessage().contains("not in allowlist"));
  }

  @Test
  void hostnameRequiresExplicitAllowlist() {
    var resolver = UrlIncludeResolver.builder().httpsOnly(false).build();
    var ex =
        assertThrows(
            MjmlIncludeException.class,
            () -> resolver.resolve("http://example.com/template.mjml", CTX));
    assertTrue(ex.getMessage().contains("require explicit allowlist"));
  }

  @Test
  void allowlistNormalizationTrimsAndLowercases() {
    var resolver =
        UrlIncludeResolver.builder()
            .httpsOnly(false)
            .allowedHosts(" THIS-HOST-DOES-NOT-EXIST-XYZ123.INVALID ")
            .build();
    var ex =
        assertThrows(
            MjmlIncludeException.class,
            () ->
                resolver.resolve(
                    "http://this-host-does-not-exist-xyz123.invalid/template.mjml", CTX));
    assertTrue(ex.getMessage().contains("Cannot resolve host"));
  }

  @Test
  void denylistNormalizationTrimsAndLowercases() {
    var resolver = UrlIncludeResolver.builder().httpsOnly(false).deniedHosts(" EVIL.COM ").build();
    var ex =
        assertThrows(
            MjmlIncludeException.class,
            () -> resolver.resolve("http://evil.com/template.mjml", CTX));
    assertTrue(ex.getMessage().contains("denied"));
  }

  @Test
  void noHostThrows() {
    var resolver = UrlIncludeResolver.builder().httpsOnly(false).build();
    var ex = assertThrows(MjmlIncludeException.class, () -> resolver.resolve("http:///path", CTX));
    assertTrue(ex.getMessage().contains("no host"));
  }

  @Test
  void builderDefaultsToHttpsOnly() {
    var resolver = UrlIncludeResolver.builder().build();
    var ex =
        assertThrows(
            MjmlIncludeException.class, () -> resolver.resolve("http://example.com/x", CTX));
    assertTrue(ex.getMessage().contains("Only HTTPS"));
  }

  @Test
  void customHttpClientAccepted() {
    var client =
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(1))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();
    var resolver = UrlIncludeResolver.builder().httpClient(client).httpsOnly(false).build();
    // Loopback SSRF will block this, confirming SSRF still works with custom client
    assertThrows(
        MjmlIncludeException.class, () -> resolver.resolve("http://127.0.0.1/template.mjml", CTX));
  }

  @Test
  void unresolvedHostThrows() {
    var resolver =
        UrlIncludeResolver.builder()
            .httpsOnly(false)
            .allowedHosts("this-host-does-not-exist-xyz123.invalid")
            .build();
    var ex =
        assertThrows(
            MjmlIncludeException.class,
            () -> resolver.resolve("http://this-host-does-not-exist-xyz123.invalid/path", CTX));
    assertTrue(ex.getMessage().contains("Cannot resolve host"));
  }

  @Test
  void timeoutConfigurationAccepted() {
    var resolver =
        UrlIncludeResolver.builder()
            .connectTimeout(Duration.ofSeconds(1))
            .readTimeout(Duration.ofSeconds(2))
            .build();
    // Just verify it builds; actual timeout behavior requires real HTTP calls
    assertThrows(MjmlIncludeException.class, () -> resolver.resolve("http://example.com/x", CTX));
  }

  @Test
  void maxResponseSizeConfigurationAccepted() {
    var resolver = UrlIncludeResolver.builder().maxResponseSize(512).build();
    // Just verify it builds
    assertThrows(MjmlIncludeException.class, () -> resolver.resolve("http://example.com/x", CTX));
  }

  @Test
  void ssrfBlocksIPv6Loopback() {
    var resolver = UrlIncludeResolver.builder().httpsOnly(false).build();
    var ex =
        assertThrows(
            MjmlIncludeException.class, () -> resolver.resolve("http://[::1]/template.mjml", CTX));
    assertTrue(ex.getMessage().contains("SSRF"));
  }

  @Test
  void ssrfBlocksZeroAddress() {
    var resolver = UrlIncludeResolver.builder().httpsOnly(false).build();
    var ex =
        assertThrows(
            MjmlIncludeException.class,
            () -> resolver.resolve("http://0.0.0.0/template.mjml", CTX));
    assertTrue(ex.getMessage().contains("SSRF"));
  }

  @Test
  void ssrfBlocksMulticastIPv4() {
    var resolver = UrlIncludeResolver.builder().httpsOnly(false).build();
    // 224.0.0.1 is a multicast address (224.0.0.0/4 range)
    var ex =
        assertThrows(
            MjmlIncludeException.class,
            () -> resolver.resolve("http://224.0.0.1/template.mjml", CTX));
    assertTrue(
        ex.getMessage().contains("SSRF"),
        "Multicast IPv4 address should be blocked by SSRF protection");
  }

  @Test
  void ssrfBlocksMulticastIPv6() {
    var resolver = UrlIncludeResolver.builder().httpsOnly(false).build();
    // ff02::1 is a well-known IPv6 multicast address (all nodes)
    var ex =
        assertThrows(
            MjmlIncludeException.class,
            () -> resolver.resolve("http://[ff02::1]/template.mjml", CTX));
    assertTrue(
        ex.getMessage().contains("SSRF"),
        "Multicast IPv6 address should be blocked by SSRF protection");
  }

  @Test
  void ssrfBlocksIpv6UniqueLocalAddress() {
    var resolver =
        UrlIncludeResolver.builder()
            .httpClient(new StubHttpClient(200, "ok".getBytes(StandardCharsets.UTF_8)))
            .httpsOnly(false)
            .build();
    var ex =
        assertThrows(
            MjmlIncludeException.class,
            () -> resolver.resolve("http://[fd00::1]/template.mjml", CTX));
    assertTrue(
        ex.getMessage().contains("SSRF"),
        "IPv6 unique local addresses should be blocked by SSRF protection");
  }

  @Test
  void enforcesMaxResponseSizeInBytes() {
    byte[] body = "ééé".getBytes(StandardCharsets.UTF_8); // 3 chars, 6 bytes
    var resolver =
        UrlIncludeResolver.builder()
            .httpClient(new StubHttpClient(200, body))
            .httpsOnly(false)
            .maxResponseSize(5)
            .build();

    var ex =
        assertThrows(
            MjmlIncludeException.class,
            () -> resolver.resolve("http://93.184.216.34/template.mjml", CTX));
    assertTrue(ex.getMessage().contains("bytes"));
  }

  @Test
  void non200StatusCode404ThrowsIncludeException() {
    byte[] body = "Not Found".getBytes(StandardCharsets.UTF_8);
    var resolver =
        UrlIncludeResolver.builder()
            .httpClient(new StubHttpClient(404, body))
            .httpsOnly(false)
            .build();

    var ex =
        assertThrows(
            MjmlIncludeException.class,
            () -> resolver.resolve("http://93.184.216.34/template.mjml", CTX));
    assertTrue(ex.getMessage().contains("404"), "Error message should include HTTP status code");
  }

  @Test
  void non200StatusCode500ThrowsIncludeException() {
    byte[] body = "Internal Server Error".getBytes(StandardCharsets.UTF_8);
    var resolver =
        UrlIncludeResolver.builder()
            .httpClient(new StubHttpClient(500, body))
            .httpsOnly(false)
            .build();

    var ex =
        assertThrows(
            MjmlIncludeException.class,
            () -> resolver.resolve("http://93.184.216.34/template.mjml", CTX));
    assertTrue(ex.getMessage().contains("500"), "Error message should include HTTP status code");
  }

  @Test
  void returnsResponseWithinByteLimit() {
    byte[] body = "ééé".getBytes(StandardCharsets.UTF_8); // 6 bytes
    var resolver =
        UrlIncludeResolver.builder()
            .httpClient(new StubHttpClient(200, body))
            .httpsOnly(false)
            .maxResponseSize(6)
            .build();

    String content = resolver.resolve("http://93.184.216.34/template.mjml", CTX);
    assertTrue(content.contains("ééé"));
  }

  private static final class StubHttpClient extends HttpClient {

    private final int statusCode;
    private final byte[] body;

    private StubHttpClient(int statusCode, byte[] body) {
      this.statusCode = statusCode;
      this.body = body;
    }

    @Override
    public Optional<CookieHandler> cookieHandler() {
      return Optional.empty();
    }

    @Override
    public Optional<Duration> connectTimeout() {
      return Optional.empty();
    }

    @Override
    public Redirect followRedirects() {
      return Redirect.NEVER;
    }

    @Override
    public Optional<ProxySelector> proxy() {
      return Optional.empty();
    }

    @Override
    public SSLContext sslContext() {
      return null;
    }

    @Override
    public SSLParameters sslParameters() {
      return new SSLParameters();
    }

    @Override
    public Optional<Authenticator> authenticator() {
      return Optional.empty();
    }

    @Override
    public Version version() {
      return Version.HTTP_1_1;
    }

    @Override
    public Optional<Executor> executor() {
      return Optional.empty();
    }

    @Override
    public <T> HttpResponse<T> send(
        HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler)
        throws IOException, InterruptedException {
      @SuppressWarnings("unchecked")
      T responseBody = (T) new ByteArrayInputStream(body);
      return new StubHttpResponse<>(statusCode, request, responseBody);
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(
        HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
      throw new UnsupportedOperationException("sendAsync not used in tests");
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(
        HttpRequest request,
        HttpResponse.BodyHandler<T> responseBodyHandler,
        HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
      throw new UnsupportedOperationException("sendAsync not used in tests");
    }
  }

  private record StubHttpResponse<T>(int statusCode, HttpRequest request, T body)
      implements HttpResponse<T> {

    @Override
    public Optional<HttpResponse<T>> previousResponse() {
      return Optional.empty();
    }

    @Override
    public HttpHeaders headers() {
      return HttpHeaders.of(Map.of(), (k, v) -> true);
    }

    @Override
    public Optional<SSLSession> sslSession() {
      return Optional.empty();
    }

    @Override
    public URI uri() {
      return request.uri();
    }

    @Override
    public Version version() {
      return Version.HTTP_1_1;
    }
  }
}
