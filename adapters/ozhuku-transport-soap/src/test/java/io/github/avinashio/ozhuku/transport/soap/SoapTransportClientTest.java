package io.github.avinashio.ozhuku.transport.soap;

import io.github.avinashio.ozhuku.foundation.exception.ValidationException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SoapTransportClientTest {

    private TestHttpClient httpClient;

    @BeforeEach
    void setUp() {
        httpClient = new TestHttpClient();
    }

    @Test
    void shouldSendSoap11MessageAndReceiveResponse() throws IOException {
        final SoapTransportClient soapClient =
                new SoapTransportClient(httpClient);

        final String envelope =
                "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                + "<soap:Body><input>hello</input></soap:Body>"
                + "</soap:Envelope>";

        final SoapMessage message = SoapMessage.soap11(envelope, "process");

        final SoapTransportClient.SoapResponse response =
                soapClient.call(
                        URI.create("http://example.com/soap"),
                        message);

        assertEquals(200, response.statusCode());
        assertTrue(response.isSuccessful());
        assertTrue(response.envelope().contains("result"));
        assertFalse(httpClient.lastWasPostWithoutBody);
    }

    @Test
    void shouldSendSoap12MessageWithActionInContentType() throws IOException {
        final SoapTransportClient soapClient =
                new SoapTransportClient(httpClient);

        final String envelope =
                "<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\">"
                + "<env:Body><input>hello</input></env:Body>"
                + "</env:Envelope>";

        final SoapMessage message = SoapMessage.soap12(envelope, "doSomething");

        soapClient.call(
                URI.create("http://example.com/soap"),
                message);

        // Verify the SOAPAction header and content type were set
        assertEquals("doSomething",
                httpClient.lastRequestHeaders.get("SOAPAction"));
        assertTrue(httpClient.lastContentType
                .startsWith("application/soap+xml"));
        assertTrue(httpClient.lastContentType.contains("action=\"doSomething\""));
    }

    @Test
    void shouldIncludeAdditionalHeadersInRequest() throws IOException {
        final SoapTransportClient soapClient =
                new SoapTransportClient(httpClient);

        final Map<String, String> extraHeaders = new HashMap<>();
        extraHeaders.put("Authorization", "Bearer token123");
        extraHeaders.put("X-Custom", "custom-value");

        final SoapMessage message = SoapMessage.soap11(
                "<Envelope/>",
                "testAction");

        soapClient.call(
                URI.create("http://example.com/soap"),
                message,
                extraHeaders);

        assertEquals("Bearer token123",
                httpClient.lastRequestHeaders.get("Authorization"));
        assertEquals("custom-value",
                httpClient.lastRequestHeaders.get("X-Custom"));
        assertEquals("testAction",
                httpClient.lastRequestHeaders.get("SOAPAction"));
    }

    @Test
    void shouldExtractFaultStringFromSoapFaultResponse()
            throws IOException {

        httpClient = new TestHttpClient(
                500,
                "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                + "<soap:Body>"
                + "<soap:Fault>"
                + "<faultstring>Invalid input data</faultstring>"
                + "</soap:Fault>"
                + "</soap:Body>"
                + "</soap:Envelope>");

        final SoapTransportClient soapClient =
                new SoapTransportClient(httpClient);

        final SoapMessage message = SoapMessage.soap11(
                "<Envelope/>",
                "testAction");

        final SoapTransportClient.SoapResponse response =
                soapClient.call(
                        URI.create("http://example.com/soap"),
                        message);

        assertEquals(500, response.statusCode());
        assertFalse(response.isSuccessful());
        assertEquals("Invalid input data", response.faultString());
    }

    @Test
    void shouldHandleEmptyResponseBody() throws IOException {
        httpClient = new TestHttpClient(200, "");

        final SoapTransportClient soapClient =
                new SoapTransportClient(httpClient);

        final SoapMessage message = SoapMessage.soap11(
                "<Envelope/>",
                "testAction");

        final SoapTransportClient.SoapResponse response =
                soapClient.call(
                        URI.create("http://example.com/soap"),
                        message);

        assertEquals(200, response.statusCode());
        assertTrue(response.isSuccessful());
        assertNotNull(response.envelope());
    }

    @Test
    void shouldHandleResponseHeaders() throws IOException {
        httpClient = new TestHttpClient(
                200,
                "<result>ok</result>");

        final SoapTransportClient soapClient =
                new SoapTransportClient(httpClient);

        final SoapMessage message = SoapMessage.soap11(
                "<Envelope/>",
                "testAction");

        final SoapTransportClient.SoapResponse response =
                soapClient.call(
                        URI.create("http://example.com/soap"),
                        message);

        assertNotNull(response.headers());
    }

    @Test
    void shouldRejectNullEndpoint() {
        final SoapTransportClient soapClient =
                new SoapTransportClient(httpClient);

        assertThrows(
                ValidationException.class,
                () -> soapClient.call(
                        null,
                        SoapMessage.soap11("<Envelope/>", "action")));
    }

    @Test
    void shouldRejectNullMessage() {
        final SoapTransportClient soapClient =
                new SoapTransportClient(httpClient);

        assertThrows(
                ValidationException.class,
                () -> soapClient.call(
                        URI.create("http://example.com/soap"),
                        null));
    }

    @Test
    void shouldCreateSoap11Message() {
        final SoapMessage message = SoapMessage.soap11(
                "<Envelope/>",
                "testAction");

        assertEquals("<Envelope/>", message.envelope());
        assertEquals("testAction", message.soapAction());
        assertEquals("text/xml; charset=utf-8", message.contentType());
    }

    @Test
    void shouldCreateSoap12MessageWithActionParameter() {
        final SoapMessage message = SoapMessage.soap12(
                "<Envelope/>",
                "doSomething");

        assertEquals("<Envelope/>", message.envelope());
        assertEquals("doSomething", message.soapAction());
        assertEquals(
                "application/soap+xml; charset=utf-8; action=\"doSomething\"",
                message.contentType());
    }

    @Test
    void shouldCreateSoap12MessageWithoutAction() {
        final SoapMessage message = SoapMessage.soap12(
                "<Envelope/>",
                null);

        assertEquals("application/soap+xml; charset=utf-8",
                message.contentType());
        assertNull(message.soapAction());
    }

    @Test
    void shouldCreateMessageWithExplicitContentType() {
        final SoapMessage message = SoapMessage.of(
                "<Envelope/>",
                "action",
                "application/json");

        assertEquals("application/json", message.contentType());
        assertEquals("action", message.soapAction());
    }

    @Test
    void shouldSoapMessageEqualsAndHashCode() {
        final SoapMessage message1 = SoapMessage.soap11(
                "<Envelope/>",
                "action");

        final SoapMessage message2 = SoapMessage.soap11(
                "<Envelope/>",
                "action");

        assertEquals(message1, message2);
        assertEquals(message1.hashCode(), message2.hashCode());
    }

    @Test
    void shouldSoapMessageNotEqualWithDifferentEnvelope() {
        final SoapMessage message1 = SoapMessage.soap11(
                "<Envelope1/>",
                "action");

        final SoapMessage message2 = SoapMessage.soap11(
                "<Envelope2/>",
                "action");

        assertFalse(message1.equals(message2));
    }

    @Test
    void shouldSoapMessageNotEqualWithDifferentContentType() {
        final SoapMessage message1 = SoapMessage.soap11(
                "<Envelope/>",
                "action");

        final SoapMessage message2 = SoapMessage.of(
                "<Envelope/>",
                "action",
                "application/json");

        assertFalse(message1.equals(message2));
    }

    @Test
    void shouldNotAddSoapActionHeaderWhenEmpty() throws IOException {
        final SoapTransportClient soapClient =
                new SoapTransportClient(httpClient);

        final SoapMessage message = SoapMessage.soap11(
                "<Envelope/>",
                "");

        soapClient.call(
                URI.create("http://example.com/soap"),
                message);

        assertNull(httpClient.lastRequestHeaders.get("SOAPAction"));
    }

    @Test
    void shouldCallWithoutAdditionalHeaders() throws IOException {
        final SoapTransportClient soapClient =
                new SoapTransportClient(httpClient);

        final SoapMessage message = SoapMessage.soap11(
                "<Envelope/>",
                "testAction");

        soapClient.call(
                URI.create("http://example.com/soap"),
                message);

        assertEquals("testAction",
                httpClient.lastRequestHeaders.get("SOAPAction"));
    }

    /**
     * Minimal in-memory test HttpClient that captures request details
     * and returns a canned response.
     */
    private static final class TestHttpClient
            implements HttpClient {

        private final int responseCode;
        private final String responseBody;

        private String lastContentType;
        private Map<String, String> lastRequestHeaders;
        private boolean lastWasPostWithoutBody;

        TestHttpClient() {
            this(200, "<result>ok</result>");
        }

        TestHttpClient(
                final int responseCode,
                final String responseBody) {

            this.responseCode = responseCode;
            this.responseBody = responseBody;
        }

        @Override
        public HttpResponse get(
                final HttpRequest request) throws IOException {
            lastRequestHeaders = request.headers();
            return new TestHttpResponse(responseCode, responseBody);
        }

        @Override
        public HttpResponse post(
                final HttpRequest request,
                final String contentType,
                final InputStream content)
                throws IOException {

            lastRequestHeaders = request.headers();
            this.lastContentType = contentType;
            return new TestHttpResponse(responseCode, responseBody);
        }

        @Override
        public HttpResponse post(
                final HttpRequest request) throws IOException {

            lastRequestHeaders = request.headers();
            this.lastWasPostWithoutBody = true;
            return new TestHttpResponse(responseCode, responseBody);
        }

        @Override
        public HttpResponse put(
                final HttpRequest request,
                final String contentType,
                final InputStream content)
                throws IOException {

            lastRequestHeaders = request.headers();
            this.lastContentType = contentType;
            return new TestHttpResponse(responseCode, responseBody);
        }
    }

    /**
     * Minimal HttpResponse for testing.
     */
    private static final class TestHttpResponse
            implements HttpResponse {

        private final int statusCode;
        private final String body;

        TestHttpResponse(
                final int statusCode,
                final String body) {

            this.statusCode = statusCode;
            this.body = body;
        }

        @Override
        public int statusCode() {
            return statusCode;
        }

        @Override
        public Map<String, String> headers() {
            return Map.of("Content-Type", "text/xml");
        }

        @Override
        public InputStream body() {
            return body != null
                    ? new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8))
                    : null;
        }

        @Override
        public boolean isSuccessful() {
            return statusCode >= 200 && statusCode < 300;
        }

        @Override
        public void close() {
            // No-op per HttpResponse contract.
        }
    }
}
