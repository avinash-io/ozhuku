package io.github.avinashio.ozhuku.transport.http;

import io.github.avinashio.ozhuku.foundation.validation.Validation;
import io.github.avinashio.ozhuku.transport.HttpClient;
import io.github.avinashio.ozhuku.transport.HttpRequest;
import io.github.avinashio.ozhuku.transport.HttpResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/**
 * HTTP transport adapter built on the JDK {@link java.net.http.HttpClient}.
 *
 * <p>This adapter implements the Ozhuku {@link HttpClient} contract using the
 * standard JDK HTTP client introduced in Java 11. It translates
 * technology-neutral {@link HttpRequest} objects into JDK HTTP requests and
 * wraps the resulting JDK responses in {@link JdkHttpResponse} instances that
 * satisfy the Ozhuku {@link HttpResponse} contract.</p>
 *
 * <p>The JDK HttpClient streams response bodies by default, keeping memory
 * usage bounded regardless of response size.</p>
 */
public final class JdkHttpClient implements HttpClient {

    private final java.net.http.HttpClient delegate;

    /**
     * Creates an HTTP client with a default JDK HttpClient.
     */
    public JdkHttpClient() {
        this(java.net.http.HttpClient.newHttpClient());
    }

    /**
     * Creates an HTTP client wrapping a pre-configured JDK HttpClient.
     *
     * @param delegate the JDK HttpClient to delegate to
     */
    public JdkHttpClient(
            final java.net.http.HttpClient delegate) {

        this.delegate = Objects.requireNonNull(
                delegate,
                "delegate must not be null");
    }

    @Override
    public HttpResponse get(
            final HttpRequest request) throws IOException {

        Validation.requireNonNull(
                request,
                "request must not be null");

        final java.net.http.HttpRequest.Builder builder =
                java.net.http.HttpRequest.newBuilder()
                        .uri(request.uri())
                        .timeout(toTimeout(request.timeout()))
                        .GET();

        applyHeaders(builder, request.headers());

        return execute(builder.build());
    }

    @Override
    public HttpResponse post(
            final HttpRequest request,
            final String contentType,
            final InputStream content) throws IOException {

        Validation.requireNonNull(
                request,
                "request must not be null");
        Validation.requireNonNull(
                content,
                "content must not be null");

        final byte[] bodyBytes = readAllBytes(content);

        final java.net.http.HttpRequest.Builder builder =
                java.net.http.HttpRequest.newBuilder()
                        .uri(request.uri())
                        .timeout(toTimeout(request.timeout()))
                        .POST(java.net.http.HttpRequest.BodyPublishers.ofByteArray(bodyBytes));

        applyHeaders(builder, request.headers());

        if (contentType != null) {
            builder.header("Content-Type", contentType);
        }

        return execute(builder.build());
    }

    @Override
    public HttpResponse post(
            final HttpRequest request) throws IOException {

        Validation.requireNonNull(
                request,
                "request must not be null");

        final java.net.http.HttpRequest.Builder builder =
                java.net.http.HttpRequest.newBuilder()
                        .uri(request.uri())
                        .timeout(toTimeout(request.timeout()))
                        .POST(java.net.http.HttpRequest.BodyPublishers.noBody());

        applyHeaders(builder, request.headers());

        return execute(builder.build());
    }

    @Override
    public HttpResponse put(
            final HttpRequest request,
            final String contentType,
            final InputStream content) throws IOException {

        Validation.requireNonNull(
                request,
                "request must not be null");
        Validation.requireNonNull(
                content,
                "content must not be null");

        final byte[] bodyBytes = readAllBytes(content);

        final java.net.http.HttpRequest.Builder builder =
                java.net.http.HttpRequest.newBuilder()
                        .uri(request.uri())
                        .timeout(toTimeout(request.timeout()))
                        .PUT(java.net.http.HttpRequest.BodyPublishers.ofByteArray(bodyBytes));

        applyHeaders(builder, request.headers());

        if (contentType != null) {
            builder.header("Content-Type", contentType);
        }

        return execute(builder.build());
    }

    private static byte[] readAllBytes(
            final InputStream content) throws IOException {

        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        content.transferTo(buffer);
        return buffer.toByteArray();
    }

    private static void applyHeaders(
            final java.net.http.HttpRequest.Builder builder,
            final Map<String, String> headers) {

        if (headers != null) {
            headers.forEach(builder::header);
        }
    }

    private Duration toTimeout(
            final Duration timeout) {

        return timeout != null
                ? timeout
                : Duration.ofSeconds(30);
    }

    private HttpResponse execute(
            final java.net.http.HttpRequest jdkRequest)
            throws IOException {

        try {
            final java.net.http.HttpResponse<InputStream> jdkResponse =
                    delegate.send(
                            jdkRequest,
                            java.net.http.HttpResponse.BodyHandlers.ofInputStream());

            return new JdkHttpResponse(jdkResponse);
        } catch (final InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IOException(
                    "HTTP request was interrupted",
                    exception);
        } catch (final java.net.http.HttpTimeoutException exception) {
            throw new IOException(
                    "HTTP request timed out",
                    exception);
        }
    }

    /**
     * Wraps a JDK HTTP response to satisfy the Ozhuku
     * {@link HttpResponse} contract.
     */
    private static final class JdkHttpResponse
            implements HttpResponse {

        private final java.net.http.HttpResponse<InputStream> delegate;

        JdkHttpResponse(
                final java.net.http.HttpResponse<InputStream> delegate) {

            this.delegate = Objects.requireNonNull(
                    delegate,
                    "delegate must not be null");
        }

        @Override
        public int statusCode() {
            return delegate.statusCode();
        }

        @Override
        public Map<String, String> headers() {
            return delegate.headers()
                    .map()
                    .entrySet()
                    .stream()
                    .collect(java.util.stream.Collectors.toUnmodifiableMap(
                            Map.Entry::getKey,
                            e -> String.join(",", e.getValue())));
        }

        @Override
        public InputStream body() {
            return delegate.body();
        }

        @Override
        public boolean isSuccessful() {
            final int code = delegate.statusCode();
            return code >= 200 && code < 300;
        }

        @Override
        public void close() {
            // InputStream is closed by the caller per HttpResponse contract.
        }
    }
}
