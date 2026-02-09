package dev.jcputney.mjml.resolver;

import dev.jcputney.mjml.IncludeResolver;
import dev.jcputney.mjml.MjmlIncludeException;
import dev.jcputney.mjml.ResolverContext;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Set;

/**
 * An {@link IncludeResolver} that fetches content via HTTP/HTTPS using the JDK {@link HttpClient}.
 * Includes SSRF protection by blocking requests to private/loopback addresses and supporting
 * host allowlists and denylists.
 */
public final class UrlIncludeResolver implements IncludeResolver {

  private static final int DEFAULT_MAX_RESPONSE_SIZE = 1024 * 1024; // 1 MB

  private final HttpClient httpClient;
  private final Set<String> allowedHosts;
  private final Set<String> deniedHosts;
  private final Duration connectTimeout;
  private final Duration readTimeout;
  private final int maxResponseSize;
  private final boolean httpsOnly;

  private UrlIncludeResolver(HttpClient httpClient, Set<String> allowedHosts,
      Set<String> deniedHosts, Duration connectTimeout, Duration readTimeout,
      int maxResponseSize, boolean httpsOnly) {
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

    // Host denylist check
    if (!deniedHosts.isEmpty() && deniedHosts.contains(host.toLowerCase())) {
      throw new MjmlIncludeException("Host is denied: " + host);
    }

    // Host allowlist check
    if (!allowedHosts.isEmpty() && !allowedHosts.contains(host.toLowerCase())) {
      throw new MjmlIncludeException("Host is not in allowlist: " + host);
    }

    // SSRF protection: check resolved IP addresses
    checkSsrf(host);

    try {
      HttpRequest request = HttpRequest.newBuilder()
          .uri(uri)
          .timeout(readTimeout)
          .GET()
          .build();

      HttpResponse<String> response = httpClient.send(request,
          HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() != 200) {
        throw new MjmlIncludeException(
            "HTTP " + response.statusCode() + " for URL: " + path);
      }

      String body = response.body();
      if (body.length() > maxResponseSize) {
        throw new MjmlIncludeException(
            "Response exceeds maximum size (" + maxResponseSize + " bytes): " + path);
      }

      return body;
    } catch (MjmlIncludeException e) {
      throw e;
    } catch (IOException e) {
      throw new MjmlIncludeException("Failed to fetch URL: " + path, e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new MjmlIncludeException("Request interrupted for URL: " + path, e);
    }
  }

  private void checkSsrf(String host) {
    try {
      InetAddress[] addresses = InetAddress.getAllByName(host);
      for (InetAddress addr : addresses) {
        if (addr.isLoopbackAddress() || addr.isSiteLocalAddress()
            || addr.isLinkLocalAddress() || addr.isAnyLocalAddress()) {
          throw new MjmlIncludeException(
              "SSRF protection: host resolves to private/local address: " + host);
        }
      }
    } catch (UnknownHostException e) {
      throw new MjmlIncludeException("Cannot resolve host: " + host, e);
    }
  }

  /**
   * Builder for {@link UrlIncludeResolver}.
   */
  public static final class Builder {

    private HttpClient httpClient;
    private Set<String> allowedHosts = Set.of();
    private Set<String> deniedHosts = Set.of();
    private Duration connectTimeout = Duration.ofSeconds(5);
    private Duration readTimeout = Duration.ofSeconds(10);
    private int maxResponseSize = DEFAULT_MAX_RESPONSE_SIZE;
    private boolean httpsOnly = true;

    private Builder() {}

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
     * Sets the allowed hosts. If non-empty, only these hosts are permitted.
     *
     * @param hosts allowed host names
     * @return this builder
     */
    public Builder allowedHosts(String... hosts) {
      this.allowedHosts = Set.of(hosts);
      return this;
    }

    /**
     * Sets the denied hosts. These hosts are always blocked.
     *
     * @param hosts denied host names
     * @return this builder
     */
    public Builder deniedHosts(String... hosts) {
      this.deniedHosts = Set.of(hosts);
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
        client = HttpClient.newBuilder()
            .connectTimeout(connectTimeout)
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();
      }
      return new UrlIncludeResolver(client, allowedHosts, deniedHosts,
          connectTimeout, readTimeout, maxResponseSize, httpsOnly);
    }
  }
}
