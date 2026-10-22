package io.github.avinashio.ozhuku.transport;

import java.io.IOException;
import java.io.InputStream;

/**
 * Technology-neutral HTTP client contract for Ozhuku.
 *
 * <p>This interface abstracts HTTP protocol operations (GET, POST, etc.)
 * so that transport adapters can be swapped without modifying application
 * logic. The core domain never depends on concrete HTTP client libraries;
 * implementations live in adapters.</p>
 */
public interface HttpClient {

    /**
     * Issues an HTTP GET request and returns the response body as a stream.
     *
     * <p>The caller is responsible for closing the returned stream.</p>
     *
     * @param request the GET request specification
     * @return the response containing a streaming body
     * @throws IOException if the request fails
     */
    HttpResponse get(
            HttpRequest request) throws IOException;

    /**
     * Issues an HTTP POST request with the given content.
     *
     * @param request the POST request specification
     * @param contentType the content type of the body
     * @param content the request body content
     * @return the response
     * @throws IOException if the request fails
     */
    HttpResponse post(
            HttpRequest request,
            String contentType,
            InputStream content) throws IOException;

    /**
     * Issues an HTTP POST request without a body.
     *
     * @param request the POST request specification
     * @return the response
     * @throws IOException if the request fails
     */
    HttpResponse post(
            HttpRequest request) throws IOException;

    /**
     * Issues an HTTP PUT request with the given content.
     *
     * @param request the PUT request specification
     * @param contentType the content type of the body
     * @param content the request body content
     * @return the response
     * @throws IOException if the request fails
     */
    HttpResponse put(
            HttpRequest request,
            String contentType,
            InputStream content) throws IOException;
}
