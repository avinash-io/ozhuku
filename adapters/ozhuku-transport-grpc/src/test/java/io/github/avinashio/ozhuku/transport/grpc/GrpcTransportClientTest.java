package io.github.avinashio.ozhuku.transport.grpc;

import io.github.avinashio.ozhuku.foundation.exception.ValidationException;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Server;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerServiceDefinition;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GrpcTransportClientTest {

    private static final MethodDescriptor<byte[], byte[]> METHOD_OK =
            MethodDescriptor.<byte[], byte[]>newBuilder()
                    .setFullMethodName("test.Service/Method")
                    .setType(MethodDescriptor.MethodType.UNARY)
                    .setRequestMarshaller(
                            GrpcMarshallers.BYTE_ARRAY_REQUEST_MARSHALLER)
                    .setResponseMarshaller(
                            GrpcMarshallers.BYTE_ARRAY_RESPONSE_MARSHALLER)
                    .build();

    private static final MethodDescriptor<byte[], byte[]> METHOD_NOT_FOUND =
            MethodDescriptor.<byte[], byte[]>newBuilder()
                    .setFullMethodName("test.Service/NotFound")
                    .setType(MethodDescriptor.MethodType.UNARY)
                    .setRequestMarshaller(
                            GrpcMarshallers.BYTE_ARRAY_REQUEST_MARSHALLER)
                    .setResponseMarshaller(
                            GrpcMarshallers.BYTE_ARRAY_RESPONSE_MARSHALLER)
                    .build();

    private static final MethodDescriptor<byte[], byte[]> METHOD_INTERNAL_ERROR =
            MethodDescriptor.<byte[], byte[]>newBuilder()
                    .setFullMethodName("test.Service/InternalError")
                    .setType(MethodDescriptor.MethodType.UNARY)
                    .setRequestMarshaller(
                            GrpcMarshallers.BYTE_ARRAY_REQUEST_MARSHALLER)
                    .setResponseMarshaller(
                            GrpcMarshallers.BYTE_ARRAY_RESPONSE_MARSHALLER)
                    .build();

    private static final MethodDescriptor<byte[], byte[]> METHOD_SLOW =
            MethodDescriptor.<byte[], byte[]>newBuilder()
                    .setFullMethodName("test.Service/SlowMethod")
                    .setType(MethodDescriptor.MethodType.UNARY)
                    .setRequestMarshaller(
                            GrpcMarshallers.BYTE_ARRAY_REQUEST_MARSHALLER)
                    .setResponseMarshaller(
                            GrpcMarshallers.BYTE_ARRAY_RESPONSE_MARSHALLER)
                    .build();

    private static final String OK_METHOD = "test.Service/Method";

    private static final String NOT_FOUND_METHOD = "test.Service/NotFound";

    private static final String INTERNAL_ERROR_METHOD =
            "test.Service/InternalError";

    private static final String SLOW_METHOD = "test.Service/SlowMethod";

    private static final String SERVICE_NAME = "test.Service";

    private Server server;

    private ManagedChannel channel;

    private GrpcTransportClient client;

    /**
     * Creates a unary server call handler that delegates to the given logic.
     *
     * <p>The handler receives the request message via the call listener
     * and then invokes the provided BiConsumer with the message and a
     * StreamObserver for sending the response.</p>
     */
    private static ServerCallHandler<byte[], byte[]> unaryHandler(
            final BiConsumer<byte[], StreamObserver<byte[]>> handler) {

        return new ServerCallHandler<>() {
            @Override
            public ServerCall.Listener<byte[]> startCall(
                    final ServerCall<byte[], byte[]> call,
                    final Metadata headers) {

                call.request(1);
                call.sendHeaders(new Metadata());

                final StreamObserver<byte[]> responseObserver =
                        new StreamObserver<>() {
                    private boolean halfClosed = false;

                    @Override
                    public void onNext(final byte[] value) {
                        call.sendMessage(value);
                    }

                    @Override
                    public void onCompleted() {
                        if (halfClosed) {
                            return;
                        }
                        halfClosed = true;
                        call.close(Status.OK, headers);
                    }

                    @Override
                    public void onError(final Throwable throwable) {
                        halfClosed = true;
                        if (throwable instanceof StatusRuntimeException) {
                            final var status =
                                    ((StatusRuntimeException) throwable)
                                            .getStatus();
                            call.close(status, new Metadata());
                        } else {
                            call.close(
                                    Status.UNKNOWN
                                            .withDescription(
                                                    throwable.getMessage()),
                                    new Metadata());
                        }
                    }
                };

                return new ServerCall.Listener<>() {
                    @Override
                    public void onMessage(final byte[] message) {
                        try {
                            handler.accept(message, responseObserver);
                        } catch (final Throwable throwable) {
                            responseObserver.onError(throwable);
                        }
                    }

                    @Override
                    public void onComplete() {
                        // Unary calls are handled in onMessage.
                    }
                };
            }
        };
    }

    @BeforeEach
    void setUp() throws Exception {
        server = InProcessServerBuilder.forName("test-server")
                .addService(ServerServiceDefinition.builder(SERVICE_NAME)
                        .addMethod(METHOD_OK, unaryHandler(
                                (request, observer) -> {
                                    observer.onNext(
                                            ("RECEIVED:"
                                                    + new String(request,
                                                    StandardCharsets.UTF_8))
                                                    .getBytes(StandardCharsets.UTF_8));
                                    observer.onCompleted();
                                }))
                        .addMethod(METHOD_NOT_FOUND, unaryHandler(
                                (request, observer) -> {
                                    observer.onError(
                                            new StatusRuntimeException(
                                                    Status.NOT_FOUND));
                                }))
                        .addMethod(METHOD_INTERNAL_ERROR, unaryHandler(
                                (request, observer) -> {
                                    observer.onError(
                                            new StatusRuntimeException(
                                                    Status.INTERNAL));
                                }))
                        .addMethod(METHOD_SLOW, unaryHandler(
                                (request, observer) -> {
                                    try {
                                        Thread.sleep(500);
                                    } catch (final InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                    }
                                    observer.onError(
                                            new StatusRuntimeException(
                                                    Status.DEADLINE_EXCEEDED));
                                }))
                        .build())
                .build()
                .start();

        channel = InProcessChannelBuilder.forName("test-server")
                .usePlaintext()
                .build();

        client = new GrpcTransportClient(channel, Duration.ofSeconds(10));
    }

    @AfterEach
    void tearDown() {
        if (client != null) {
            client.close();
        }
        if (channel != null) {
            channel.shutdownNow();
        }
        if (server != null) {
            server.shutdownNow();
        }
    }

    @Test
    void shouldExecuteUnaryCall() {
        final GrpcCallRequest request = GrpcCallRequest.to(
                OK_METHOD,
                "hello-grpc".getBytes(StandardCharsets.UTF_8));

        final GrpcCallResponse response = client.call(request);

        assertTrue(response.isSuccessful(),
                "Response should be successful. Status code: "
                        + response.statusCode()
                        + ", error: " + response.errorMessage());
        assertEquals(Status.OK.getCode().value(), response.statusCode());
        assertNotNull(response.serializedResponse());
        assertArrayEquals(
                "RECEIVED:hello-grpc".getBytes(StandardCharsets.UTF_8),
                response.serializedResponse());
    }

    @Test
    void shouldIncludeMetadataInRequest() {
        final GrpcCallRequest request = GrpcCallRequest.to(
                OK_METHOD,
                "payload".getBytes(StandardCharsets.UTF_8),
                Map.of("X-Custom", "custom-value"),
                Duration.ofSeconds(5));

        final GrpcCallResponse response = client.call(request);

        assertTrue(response.isSuccessful());
    }

    @Test
    void shouldHandleNotFoundStatus() {
        final GrpcCallRequest request = GrpcCallRequest.to(
                NOT_FOUND_METHOD,
                "error".getBytes(StandardCharsets.UTF_8));

        final GrpcCallResponse response = client.call(request);

        assertFalse(response.isSuccessful());
        assertEquals(Status.NOT_FOUND.getCode().value(),
                response.statusCode());
        assertNotNull(response.errorMessage());
    }

    @Test
    void shouldHandleInternalError() {
        final GrpcCallRequest request = GrpcCallRequest.to(
                INTERNAL_ERROR_METHOD,
                "error".getBytes(StandardCharsets.UTF_8));

        final GrpcCallResponse response = client.call(request);

        assertFalse(response.isSuccessful());
        assertEquals(Status.INTERNAL.getCode().value(),
                response.statusCode());
    }

    @Test
    void shouldApplyTimeout() {
        final GrpcCallRequest request = GrpcCallRequest.to(
                SLOW_METHOD,
                "data".getBytes(StandardCharsets.UTF_8),
                null,
                Duration.ofMillis(1));

        final GrpcCallResponse response = client.call(request);

        assertFalse(response.isSuccessful());
        assertEquals(Status.DEADLINE_EXCEEDED.getCode().value(),
                response.statusCode());
    }

    @Test
    void shouldRejectNullRequest() {
        assertThrows(
                ValidationException.class,
                () -> client.call(null));
    }

    @Test
    void shouldCreateChannelFromConfig() {
        final GrpcChannelConfig config = GrpcChannelConfig.builder(
                "localhost:50051")
                .useTls(false)
                .build();

        try (GrpcTransportClient newClient =
                     new GrpcTransportClient(config)) {
            assertNotNull(newClient.channel());
            assertNotNull(newClient);
        }
    }

    @Test
    void shouldConfigureChannelWithTls() {
        final GrpcChannelConfig config = GrpcChannelConfig.builder(
                "localhost:443")
                .useTls(true)
                .build();

        try (GrpcTransportClient tlsClient =
                     new GrpcTransportClient(config)) {
            assertNotNull(tlsClient.channel());
        }
    }

    @Test
    void shouldConfigureChannelWithConnectTimeout() {
        final GrpcChannelConfig config = GrpcChannelConfig.builder(
                "localhost:50051")
                .useTls(false)
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        try (GrpcTransportClient configuredClient =
                     new GrpcTransportClient(config)) {
            assertNotNull(configuredClient.channel());
            assertEquals(Duration.ofSeconds(30),
                    config.connectTimeout());
        }
    }

    @Test
    void shouldCloseChannelOnClose() {
        client.close();
        assertTrue(channel.isShutdown());
    }

    @Test
    void shouldCreateGrpcCallRequestWithoutMetadata() {
        final GrpcCallRequest request = GrpcCallRequest.to(
                OK_METHOD,
                new byte[0]);

        assertEquals(OK_METHOD, request.methodName());
        assertNotNull(request.serializedRequest());
        assertNotNull(request.metadata());
        assertEquals(Duration.ofSeconds(30), request.timeout());
    }

    @Test
    void shouldCreateGrpcCallRequestWithMetadata() {
        final Map<String, String> metadata = new HashMap<>();
        metadata.put("key", "value");

        final GrpcCallRequest request = GrpcCallRequest.to(
                OK_METHOD,
                new byte[0],
                metadata);

        assertEquals(metadata, request.metadata());
    }

    @Test
    void shouldCreateGrpcCallRequestWithTimeout() {
        final GrpcCallRequest request = GrpcCallRequest.to(
                OK_METHOD,
                new byte[0],
                null,
                Duration.ofMinutes(2));

        assertEquals(Duration.ofMinutes(2), request.timeout());
    }

    @Test
    void shouldCreateSuccessfulGrpcCallResponse() {
        final GrpcCallResponse response = GrpcCallResponse.success(
                0,
                new byte[]{1, 2, 3},
                Map.of("key", "value"));

        assertTrue(response.isSuccessful());
        assertEquals(0, response.statusCode());
        assertArrayEquals(new byte[]{1, 2, 3},
                response.serializedResponse());
        assertNotNull(response.metadata());
        assertNull(response.errorMessage());
    }

    @Test
    void shouldCreateFailedGrpcCallResponse() {
        final GrpcCallResponse response = GrpcCallResponse.failure(
                13,
                "Internal error",
                Map.of("key", "value"));

        assertFalse(response.isSuccessful());
        assertEquals(13, response.statusCode());
        assertEquals("Internal error", response.errorMessage());
        assertArrayEquals(new byte[0],
                response.serializedResponse());
    }

    @Test
    void shouldGrpcCallResponseEqualsAndHashCode() {
        final GrpcCallResponse response1 =
                GrpcCallResponse.success(0, new byte[]{1}, Map.of());

        final GrpcCallResponse response2 =
                GrpcCallResponse.success(0, new byte[]{1}, Map.of());

        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void shouldGrpcCallResponseNotEqualWithDifferentStatusCode() {
        final GrpcCallResponse response1 =
                GrpcCallResponse.success(0, new byte[0], Map.of());

        final GrpcCallResponse response2 =
                GrpcCallResponse.failure(1, "error", Map.of());

        assertFalse(response1.equals(response2));
    }
}
