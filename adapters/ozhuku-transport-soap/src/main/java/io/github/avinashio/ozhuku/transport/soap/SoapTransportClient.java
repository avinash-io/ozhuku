package io.github.avinashio.ozhuku.transport.soap;

import io.github.avinashio.ozhuku.foundation.validation.Validation;
import io.github.avinashio.ozhuku.transport.HttpClient;
import io.github.avinashio.ozhuku.transport.HttpRequest;
import io.github.avinashio.ozhuku.transport.HttpResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * SOAP transport client that sends SOAP messages over HTTP.
 *
 * <p>This client wraps an Ozhuku {@link HttpClient} and adds SOAP-specific
 * message handling. It builds SOAP envelopes, sets SOAPAction and Content-Type
 * headers, and returns SOAP responses.</p>
 *
 * <p>The client delegates HTTP operations to the injected
 * {@link HttpClient} implementation, keeping transport concerns separate
 * from SOAP protocol concerns.</p>
 *
 * <p>Usage:
 * <pre>
 * SoapTransportClient soapClient = new SoapTransportClient(httpClient);
 * SoapResponse response = soapClient.call(
 *     "http://example.com/service",
 *     SoapMessage.soap11(envelope, "processOrder"),
 *     Map.of());
 * </pre></p>
 */
public final class SoapTransportClient {

    private final HttpClient httpClient;

    /**
     * Creates a SOAP transport client wrapping the given HTTP client.
     *
     * @param httpClient the HTTP client to use for SOAP message transport
     */
    public SoapTransportClient(
            final HttpClient httpClient) {

        this.httpClient = Objects.requireNonNull(
                httpClient,
                "httpClient must not be null");
    }

    /**
     * Sends a SOAP message via HTTP POST and returns the response.
     *
     * @param endpoint the SOAP endpoint URI
     * @param message  the SOAP message to send
     * @param headers  additional HTTP headers (may be {@code null} or empty)
     * @return the SOAP response
     * @throws IOException if the HTTP request fails
     */
    public SoapResponse call(
            final URI endpoint,
            final SoapMessage message,
            final Map<String, String> headers) throws IOException {

        Validation.requireNonNull(
                endpoint,
                "endpoint must not be null");
        Validation.requireNonNull(
                message,
                "message must not be null");

        final Map<String, String> allHeaders = new HashMap<>();
        if (headers != null) {
            allHeaders.putAll(headers);
        }

        if (message.soapAction() != null && !message.soapAction().isEmpty()) {
            allHeaders.put("SOAPAction", message.soapAction());
        }

        final HttpRequest request = HttpRequest.to(
                endpoint,
                allHeaders);

        final byte[] bodyBytes = message.envelope()
                .getBytes(StandardCharsets.UTF_8);

        try (InputStream content = new ByteArrayInputStream(bodyBytes)) {
            try (HttpResponse response = httpClient.post(
                    request,
                    message.contentType(),
                    content)) {

                final String responseBody = response.body() != null
                        ? new String(
                                response.body().readAllBytes(),
                                StandardCharsets.UTF_8)
                        : "";

                return new SoapResponse(
                        response.statusCode(),
                        responseBody,
                        response.headers(),
                        response.isSuccessful());
            }
        }
    }

    /**
     * Sends a SOAP message via HTTP POST without additional headers.
     *
     * @param endpoint the SOAP endpoint URI
     * @param message  the SOAP message to send
     * @return the SOAP response
     * @throws IOException if the HTTP request fails
     */
    public SoapResponse call(
            final URI endpoint,
            final SoapMessage message) throws IOException {

        return call(endpoint, message, null);
    }

    /**
     * Represents the response from a SOAP service invocation.
     */
    public static final class SoapResponse {

        private final int statusCode;
        private final String envelope;
        private final Map<String, String> headers;
        private final boolean successful;

        SoapResponse(
                final int statusCode,
                final String envelope,
                final Map<String, String> headers,
                final boolean successful) {

            this.statusCode = statusCode;
            this.envelope = envelope;
            this.headers = headers;
            this.successful = successful;
        }

        /**
         * Returns the HTTP status code.
         *
         * @return status code
         */
        public int statusCode() {
            return statusCode;
        }

        /**
         * Returns the SOAP envelope content from the response body.
         *
         * @return envelope XML, or empty string if no body
         */
        public String envelope() {
            return envelope;
        }

        /**
         * Returns the response headers.
         *
         * @return headers map
         */
        public Map<String, String> headers() {
            return headers;
        }

        /**
         * Returns {@code true} if the response indicates a SOAP-level success.
         *
         * <p>Success is determined by the HTTP status code being in the 2xx
         * range. SOAP faults are reported as HTTP 500 by most SOAP
         * implementations, so a 500 response is not considered successful
         * at the transport level.</p>
         *
         * @return {@code true} if the HTTP status is 2xx
         */
        public boolean isSuccessful() {
            return successful;
        }

        /**
         * Returns the SOAP fault string from the response body, or
         * {@code null} if the response is not a SOAP fault.
         *
         * <p>This is a convenience method that searches for a
         * {@code <faultstring>} element in the response envelope.</p>
         *
         * @return the fault string, or {@code null} if not a fault
         */
        public String faultString() {
            if (envelope == null || envelope.isEmpty()) {
                return null;
            }

            final int start = envelope.indexOf("<faultstring");
            if (start < 0) {
                return null;
            }

            final int valueStart = envelope.indexOf('>', start);
            if (valueStart < 0) {
                return null;
            }

            final int valueEnd = envelope.indexOf('<', valueStart + 1);
            if (valueEnd < 0) {
                return null;
            }

            return envelope.substring(valueStart + 1, valueEnd).trim();
        }

        @Override
        public String toString() {
            return "SoapResponse{"
                    + "statusCode=" + statusCode
                    + ", successful=" + successful
                    + '}';
        }
    }

    @Override
    public String toString() {
        return "SoapTransportClient{}";
    }
}
