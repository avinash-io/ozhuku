package io.github.avinashio.ozhuku.transport.http;

import io.github.avinashio.ozhuku.foundation.exception.ValidationException;
import io.github.avinashio.ozhuku.transport.HttpRequest;
import io.github.avinashio.ozhuku.transport.HttpResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdkHttpClientTest {

    private MockWebServer server;

    private JdkHttpClient client;

    @BeforeEach
    void setUp() {
        server = new MockWebServer();
        client = new JdkHttpClient(
                java.net.http.HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(5))
                        .build());
    }

    @AfterEach
    void tearDown() throws IOException {
        server.close();
    }

    @Test
    void shouldExecuteGetRequest() throws IOException, InterruptedException {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("hello-get")
                .addHeader("Content-Type: text/plain")
                .addHeader("X-Custom: test-value"));

        server.start();

        final HttpRequest request = HttpRequest.to(
                URI.create(server.url("/api/data").toString()));

        try (HttpResponse response = client.get(request)) {
            assertEquals(200, response.statusCode());
            assertTrue(response.isSuccessful());
            assertNotNull(response.body());

            final byte[] body = response.body().readAllBytes();
            assertArrayEquals(
                    "hello-get".getBytes(StandardCharsets.UTF_8),
                    body);

            // JDK HttpClient lowercases header names
            assertEquals("text/plain",
                    response.headers().get("content-type"));
            assertEquals("test-value",
                    response.headers().get("x-custom"));
        }
    }

    @Test
    void shouldExecutePostRequestWithBodyAndContentType()
            throws IOException, InterruptedException {

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .setBody("created"));

        server.start();

        final HttpRequest request = HttpRequest.to(
                URI.create(server.url("/api/submit").toString()));

        final byte[] content =
                "post-body".getBytes(StandardCharsets.UTF_8);

        try (InputStream input = new ByteArrayInputStream(content)) {
            try (HttpResponse response = client.post(
                    request,
                    "application/xml",
                    input)) {

                assertEquals(201, response.statusCode());
                assertTrue(response.isSuccessful());

                assertArrayEquals(
                        "created".getBytes(StandardCharsets.UTF_8),
                        response.body().readAllBytes());
            }
        }

        // Verify the request was sent correctly
        final var recordedRequest = server.takeRequest();
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("application/xml",
                recordedRequest.getHeader("Content-Type"));
        assertArrayEquals(
                content,
                recordedRequest.getBody().readByteArray());
    }

    @Test
    void shouldExecutePostRequestWithoutBody() throws IOException {
        server.enqueue(new MockResponse()
                .setResponseCode(204));

        server.start();

        final HttpRequest request = HttpRequest.to(
                URI.create(server.url("/api/trigger").toString()));

        try (HttpResponse response = client.post(request)) {
            assertEquals(204, response.statusCode());
            assertTrue(response.isSuccessful());
        }
    }

    @Test
    void shouldExecutePutRequestWithBodyAndContentType()
            throws IOException, InterruptedException {

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("updated"));

        server.start();

        final HttpRequest request = HttpRequest.to(
                URI.create(server.url("/api/resource/1").toString()));

        final byte[] content =
                "put-body".getBytes(StandardCharsets.UTF_8);

        try (InputStream input = new ByteArrayInputStream(content)) {
            try (HttpResponse response = client.put(
                    request,
                    "text/plain",
                    input)) {

                assertEquals(200, response.statusCode());
                assertTrue(response.isSuccessful());

                assertArrayEquals(
                        "updated".getBytes(StandardCharsets.UTF_8),
                        response.body().readAllBytes());
            }
        }

        final var recordedRequest = server.takeRequest();
        assertEquals("PUT", recordedRequest.getMethod());
        assertEquals("text/plain",
                recordedRequest.getHeader("Content-Type"));
        assertArrayEquals(
                content,
                recordedRequest.getBody().readByteArray());
    }

    @Test
    void shouldIncludeCustomHeaders() throws IOException, InterruptedException {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("ok"));

        server.start();

        final Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer token123");
        headers.put("Accept", "application/json");

        final HttpRequest request = HttpRequest.to(
                URI.create(server.url("/api/secure").toString()),
                headers);

        try (HttpResponse response = client.get(request)) {
            assertEquals(200, response.statusCode());
        }

        final var recordedRequest = server.takeRequest();
        assertEquals("Bearer token123",
                recordedRequest.getHeader("Authorization"));
        assertEquals("application/json",
                recordedRequest.getHeader("Accept"));
    }

    @Test
    void shouldHandleNonSuccessStatusCodes() throws IOException {
        server.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("not found"));

        server.start();

        final HttpRequest request = HttpRequest.to(
                URI.create(server.url("/api/missing").toString()));

        try (HttpResponse response = client.get(request)) {
            assertEquals(404, response.statusCode());
            assertFalse(response.isSuccessful());
            assertEquals("not found",
                    new String(response.body().readAllBytes()));
        }
    }

    @Test
    void shouldHandleServerError() throws IOException {
        server.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("internal error"));

        server.start();

        final HttpRequest request = HttpRequest.to(
                URI.create(server.url("/api/error").toString()));

        try (HttpResponse response = client.get(request)) {
            assertEquals(500, response.statusCode());
            assertFalse(response.isSuccessful());
        }
    }

    @Test
    void shouldApplyTimeout() throws IOException {
        // Use a URI with a non-routable address and a very short
        // connect timeout to force a connection failure.
        final java.net.http.HttpClient httpClient =
                java.net.http.HttpClient.newBuilder()
                        .connectTimeout(Duration.ofMillis(1))
                        .build();

        final JdkHttpClient shortTimeoutClient =
                new JdkHttpClient(httpClient);

        final HttpRequest request = HttpRequest.to(
                URI.create("http://10.255.255.1:1/api"));

        assertThrows(
                IOException.class,
                () -> shortTimeoutClient.get(request));
    }

    @Test
    void shouldRejectNullRequestOnGet() {
        assertThrows(
                ValidationException.class,
                () -> client.get(null));
    }

    @Test
    void shouldRejectNullRequestOnPostWithBody() {
        assertThrows(
                ValidationException.class,
                () -> client.post(
                        null,
                        "text/plain",
                        new ByteArrayInputStream(new byte[0])));
    }

    @Test
    void shouldRejectNullRequestOnPostWithoutBody() {
        assertThrows(
                ValidationException.class,
                () -> client.post(null));
    }

    @Test
    void shouldRejectNullRequestOnPut() {
        assertThrows(
                ValidationException.class,
                () -> client.put(
                        null,
                        "text/plain",
                        new ByteArrayInputStream(new byte[0])));
    }

    @Test
    void shouldRejectNullContentOnPost() throws IOException {
        server.start();
        final HttpRequest request = HttpRequest.to(
                URI.create(server.url("/api").toString()));

        assertThrows(
                ValidationException.class,
                () -> client.post(request, "text/plain", null));
    }

    @Test
    void shouldRejectNullContentOnPut() throws IOException {
        server.start();
        final HttpRequest request = HttpRequest.to(
                URI.create(server.url("/api").toString()));

        assertThrows(
                ValidationException.class,
                () -> client.put(request, "text/plain", null));
    }

    @Test
    void shouldHandleResponseHeaders() throws IOException {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("header-test")
                .addHeader("X-Custom-Header: custom-value")
                .addHeader("Cache-Control: no-cache"));

        server.start();

        final HttpRequest request = HttpRequest.to(
                URI.create(server.url("/api").toString()));

        try (HttpResponse response = client.get(request)) {
            assertEquals("custom-value",
                    response.headers().get("x-custom-header"));
            assertEquals("no-cache",
                    response.headers().get("cache-control"));
        }
    }

    @Test
    void shouldHandleEmptyResponseBody() throws IOException {
        server.enqueue(new MockResponse()
                .setResponseCode(204));

        server.start();

        final HttpRequest request = HttpRequest.to(
                URI.create(server.url("/api").toString()));

        try (HttpResponse response = client.get(request)) {
            assertEquals(204, response.statusCode());
            assertTrue(response.isSuccessful());
            assertNotNull(response.body());
        }
    }

    @Test
    void shouldReuseClientInstance() throws IOException {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("first"));

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("second"));

        server.start();

        final HttpRequest request1 = HttpRequest.to(
                URI.create(server.url("/api/1").toString()));

        final HttpRequest request2 = HttpRequest.to(
                URI.create(server.url("/api/2").toString()));

        try (HttpResponse response1 = client.get(request1)) {
            assertEquals("first",
                    new String(response1.body().readAllBytes()));
        }

        try (HttpResponse response2 = client.get(request2)) {
            assertEquals("second",
                    new String(response2.body().readAllBytes()));
        }
    }
}