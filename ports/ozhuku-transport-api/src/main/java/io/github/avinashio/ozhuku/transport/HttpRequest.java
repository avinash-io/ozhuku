package io.github.avinashio.ozhuku.transport;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/**
 * Represents an HTTP request specification.
 *
 * <p>Immutable value object containing the target URI, optional headers,
 * and optional timeout. Headers may be {@code null} for requests with no
 * custom headers.</p>
 */
public final class HttpRequest {

    private final URI uri;
    private final Map<String, String> headers;
    private final Duration timeout;

    private HttpRequest(
            final URI uri,
            final Map<String, String> headers,
            final Duration timeout) {

        this.uri = Objects.requireNonNull(
                uri,
                "uri must not be null");
        this.headers = headers;
        this.timeout = timeout;
    }

    /**
     * Creates an HTTP request for the given URI with a default timeout.
     *
     * @param uri the request target URI
     * @return the request
     */
    public static HttpRequest to(final URI uri) {
        return new HttpRequest(
                uri,
                null,
                Duration.ofSeconds(30));
    }

    /**
     * Creates an HTTP request for the given URI with custom headers
     * and a default timeout.
     *
     * @param uri     the request target URI
     * @param headers the request headers
     * @return the request
     */
    public static HttpRequest to(
            final URI uri,
            final Map<String, String> headers) {

        return new HttpRequest(
                uri,
                headers,
                Duration.ofSeconds(30));
    }

    /**
     * Returns the request URI.
     *
     * @return request URI
     */
    public URI uri() {
        return uri;
    }

    /**
     * Returns the request headers, or {@code null} if none were set.
     *
     * @return headers map or {@code null}
     */
    public Map<String, String> headers() {
        return headers;
    }

    /**
     * Returns the request timeout.
     *
     * @return timeout duration
     */
    public Duration timeout() {
        return timeout;
    }
}
