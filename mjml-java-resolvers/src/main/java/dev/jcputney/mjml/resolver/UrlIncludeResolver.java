package dev.jcputney.mjml.resolver;

import dev.jcputney.mjml.IncludeResolver;
import dev.jcputney.mjml.MjmlIncludeException;
import dev.jcputney.mjml.ResolverContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * An {@link IncludeResolver} that fetches content via HTTP/HTTPS using the JDK {@link HttpClient}.
 * Includes SSRF protection by blocking requests to private/loopback addresses and supporting host
 * allowlists and denylists.
 *
 * <p>For hostname-based URLs (for example {@code https://cdn.example.com/template.mjml}), configure
 * {@code allowedHosts(...)}. Hostname requests without an explicit allowlist are rejected to reduce
 * SSRF and DNS-rebinding risk.
 */
public final class UrlIncludeResolver implements IncludeResolver {

  private static final int DEFAULT_MAX_RESPONSE_SIZE = 1024 * 1024; // 1 MB
  private static final Pattern IPV4_PATTERN = Pattern.compile("^\\d+\\.\\d+\\.\\d+\\.\\d+$");

  private final HttpClient httpClient;
  private final Set<String> allowedHosts;
  private final Set<String> deniedHosts;
  private final Duration connectTimeout;
  private final Duration readTimeout;
  private final int maxResponseSize;
  private final boolean httpsOnly;

  private UrlIncludeResolver(
      HttpClient httpClient,
      Set<String> allowedHosts,
      Set<String> deniedHosts,
      Duration connectTimeout,
      Duration readTimeout,
      int maxResponseSize,
      boolean httpsOnly) {
    this.httpClient = httpClient;
    this.allowedHosts = allowedHosts;
    this.deniedHosts = deniedHosts;
    this.connectTimeout = connectTimeout;
    this.readTimeout = readTimeout;
    this.maxResponseSize = maxResponseSize;
    this.httpsOnly = httpsOnly;
  }

  /**
   * Returns a new builder.
   *
   * @return builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  private static boolean isHostname(String host) {
    return !host.contains(":") && !IPV4_PATTERN.matcher(host).matches();
  }

  @Override
  public String resolve(String path, ResolverContext context) {
    URI uri;
    try {
      uri = new URI(path);
    } catch (URISyntaxException e) {
      throw new MjmlIncludeException("Invalid URL: " + path, e);
    }

    String scheme = uri.getScheme();
    if (scheme == null) {
      throw new MjmlIncludeException("URL has no scheme: " + path);
    }

    if (httpsOnly && !"https".equalsIgnoreCase(scheme)) {
      throw new MjmlIncludeException("Only HTTPS URLs are allowed: " + path);
    }

    if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
      throw new MjmlIncludeException("Unsupported URL scheme: " + scheme);
    }

    String host = uri.getHost();
    if (host == null || host.isEmpty()) {
      throw new MjmlIncludeException("URL has no host: " + path);
    }
    String normalizedHost = host.toLowerCase(Locale.ROOT);

    // Host denylist check
    if (!deniedHosts.isEmpty() && deniedHosts.contains(normalizedHost)) {
      throw new MjmlIncludeException("Host is denied: " + host);
    }

    // To reduce DNS-rebinding risk, hostname-based URLs require explicit allowlisting.
    // Keep localhost on the SSRF path so it is rejected as a local address.
    if (isHostname(normalizedHost)
        && !"localhost".equals(normalizedHost)
        && allowedHosts.isEmpty()) {
      throw new MjmlIncludeException(
          "Hostname URLs require explicit allowlist configuration: " + host);
    }

    // Host allowlist check
    if (!allowedHosts.isEmpty() && !allowedHosts.contains(normalizedHost)) {
      throw new MjmlIncludeException("Host is not in allowlist: " + host);
    }

    // SSRF protection: check resolved IP addresses
    checkSsrf(host);

    try {
      HttpRequest request = HttpRequest.newBuilder().uri(uri).timeout(readTimeout).GET().build();

      HttpResponse<InputStream> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

      if (response.statusCode() != 200) {
        throw new MjmlIncludeException("HTTP " + response.statusCode() + " for URL: " + path);
      }

      return readBodyWithLimit(response.body(), path);
    } catch (MjmlIncludeException e) {
      throw e;
    } catch (IOException e) {
      throw new MjmlIncludeException("Failed to fetch URL: " + path, e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new MjmlIncludeException("Request interrupted for URL: " + path, e);
    }
  }

  private String readBodyWithLimit(InputStream responseBody, String path) throws IOException {
    byte[] buffer = new byte[8192];
    int initialCapacity = Math.min(maxResponseSize, buffer.length);
    ByteArrayOutputStream out = new ByteArrayOutputStream(initialCapacity);
    long totalRead = 0;

    try (InputStream in = responseBody) {
      int read;
      while ((read = in.read(buffer)) != -1) {
        totalRead += read;
        if (totalRead > maxResponseSize) {
          throw new MjmlIncludeException(
              "Response exceeds maximum size (" + maxResponseSize + " bytes): " + path);
        }
        out.write(buffer, 0, read);
      }
    }

    return out.toString(StandardCharsets.UTF_8);
  }

  private void checkSsrf(String host) {
    try {
      InetAddress[] addresses = InetAddress.getAllByName(host);
      for (InetAddress addr : addresses) {
        if (isBlockedAddress(addr)) {
          throw new MjmlIncludeException(
              "SSRF protection: host resolves to private/local address: " + host);
        }
      }
    } catch (UnknownHostException e) {
      throw new MjmlIncludeException("Cannot resolve host: " + host, e);
    }
  }

  private static boolean isBlockedAddress(InetAddress addr) {
    return addr.isLoopbackAddress()
        || addr.isSiteLocalAddress()
        || addr.isLinkLocalAddress()
        || addr.isAnyLocalAddress()
        || addr.isMulticastAddress()
        || isIpv6UniqueLocal(addr)
        || isIpv6DeprecatedSiteLocal(addr);
  }

  private static boolean isIpv6UniqueLocal(InetAddress addr) {
    if (!(addr instanceof Inet6Address inet6)) {
      return false;
    }
    byte[] bytes = inet6.getAddress();
    // Unique local addresses: fc00::/7 (first 7 bits are 1111110x)
    return (bytes[0] & 0xFE) == 0xFC;
  }

  private static boolean isIpv6DeprecatedSiteLocal(InetAddress addr) {
    if (!(addr instanceof Inet6Address inet6)) {
      return false;
    }
    byte[] bytes = inet6.getAddress();
    // Deprecated site-local addresses: fec0::/10 (first 10 bits are 1111111011)
    return (bytes[0] & 0xFF) == 0xFE && (bytes[1] & 0xC0) == 0xC0;
  }

  /** Builder for {@link UrlIncludeResolver}. */
  public static final class Builder {

    private HttpClient httpClient;
    private Set<String> allowedHosts = Set.of();
    private Set<String> deniedHosts = Set.of();
    private Duration connectTimeout = Duration.ofSeconds(5);
    private Duration readTimeout = Duration.ofSeconds(10);
    private int maxResponseSize = DEFAULT_MAX_RESPONSE_SIZE;
    private boolean httpsOnly = true;

    private Builder() {}

    private static Set<String> normalizeHosts(String... hosts) {
      if (hosts == null) {
        throw new IllegalArgumentException("hosts cannot be null");
      }

      LinkedHashSet<String> normalized = new LinkedHashSet<>();
      Arrays.stream(hosts)
          .forEach(
              host -> {
                if (host == null) {
                  throw new IllegalArgumentException("host cannot be null");
                }
                String value = host.trim().toLowerCase(Locale.ROOT);
                if (value.isEmpty()) {
                  throw new IllegalArgumentException("host cannot be blank");
                }
                normalized.add(value);
              });
      return Set.copyOf(normalized);
    }

    /**
     * Sets a custom HttpClient (useful for testing).
     *
     * @param httpClient the HTTP client to use
     * @return this builder
     */
    public Builder httpClient(HttpClient httpClient) {
      this.httpClient = httpClient;
      return this;
    }

    /**
     * Sets the allowed hosts. Hostname-based URLs require an explicit allowlist; when configured,
     * only listed hosts are permitted. Host values are normalized by trimming and lowercasing.
     *
     * @param hosts allowed host names
     * @return this builder
     */
    public Builder allowedHosts(String... hosts) {
      this.allowedHosts = normalizeHosts(hosts);
      return this;
    }

    /**
     * Sets the denied hosts. These hosts are always blocked. Host values are normalized by trimming
     * and lowercasing.
     *
     * @param hosts denied host names
     * @return this builder
     */
    public Builder deniedHosts(String... hosts) {
      this.deniedHosts = normalizeHosts(hosts);
      return this;
    }

    /**
     * Sets the connection timeout.
     *
     * @param connectTimeout the timeout duration
     * @return this builder
     */
    public Builder connectTimeout(Duration connectTimeout) {
      this.connectTimeout = connectTimeout;
      return this;
    }

    /**
     * Sets the read/request timeout.
     *
     * @param readTimeout the timeout duration
     * @return this builder
     */
    public Builder readTimeout(Duration readTimeout) {
      this.readTimeout = readTimeout;
      return this;
    }

    /**
     * Sets the maximum response body size in bytes.
     *
     * @param maxResponseSize the max size
     * @return this builder
     */
    public Builder maxResponseSize(int maxResponseSize) {
      this.maxResponseSize = maxResponseSize;
      return this;
    }

    /**
     * Sets whether only HTTPS URLs are allowed.
     *
     * @param httpsOnly true to restrict to HTTPS
     * @return this builder
     */
    public Builder httpsOnly(boolean httpsOnly) {
      this.httpsOnly = httpsOnly;
      return this;
    }

    /**
     * Builds the URL resolver.
     *
     * @return a new {@link UrlIncludeResolver}
     */
    public UrlIncludeResolver build() {
      HttpClient client = this.httpClient;
      if (client == null) {
        client =
            HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
      }
      return new UrlIncludeResolver(
          client,
          allowedHosts,
          deniedHosts,
          connectTimeout,
          readTimeout,
          maxResponseSize,
          httpsOnly);
    }
  }
}
