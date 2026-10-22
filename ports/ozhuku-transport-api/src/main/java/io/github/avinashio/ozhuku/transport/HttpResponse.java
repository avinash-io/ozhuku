package io.github.avinashio.ozhuku.transport;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Represents an HTTP response.
 *
 * <p>Provides access to status code, headers, and a streaming response body.
 * The caller is responsible for closing the body stream when done.</p>
 */
public interface HttpResponse extends AutoCloseable {

    /**
     * Returns the HTTP status code.
     *
     * @return status code
     */
    int statusCode();

    /**
     * Returns the response headers.
     *
     * @return headers map
     */
    Map<String, String> headers();

    /**
     * Returns the response body as a stream.
     *
     * <p>The body may be {@code null} when the response has no content.
     * The caller is responsible for closing the stream.</p>
     *
     * @return the response body stream, or {@code null}
     * @throws IOException if the body cannot be read
     */
    InputStream body() throws IOException;

    /**
     * Returns {@code true} if the response indicates success (2xx).
     *
     * @return {@code true} for success status codes
     */
    boolean isSuccessful();

    @Override
    void close() throws IOException;
}
