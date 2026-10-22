package io.github.avinashio.ozhuku.transport.grpc;

import io.github.avinashio.ozhuku.foundation.validation.Validation;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.grpc.stub.ClientCalls;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * gRPC transport client for Ozhuku.
 *
 * <p>This client manages a gRPC {@link ManagedChannel} and provides a
 * generic unary call interface that can invoke any gRPC method by name
 * with opaque serialized protobuf payloads. This allows Ozhuku to
 * interact with gRPC services without having generated stubs in the
 * core domain.</p>
 *
 * <p>The client creates a new channel per {@link GrpcChannelConfig} and
 * manages its lifecycle. The channel is shut down when the client is
 * closed.</p>
 *
 * <p>Usage:
 * <pre>
 * GrpcChannelConfig config = GrpcChannelConfig.builder("localhost:50051")
 *     .build();
 *
 * try (GrpcTransportClient client = new GrpcTransportClient(config)) {
 *     GrpcCallRequest request = GrpcCallRequest.to(
 *         "/package.Service/Method",
 *         serializedPayload);
 *     GrpcCallResponse response = client.call(request);
 * }
 * </pre></p>
 */
public final class GrpcTransportClient
        implements AutoCloseable {

    private final ManagedChannel channel;

    private final Duration defaultTimeout;

    /**
     * Creates a gRPC transport client with the given channel configuration.
     *
     * @param config the channel configuration
     */
    public GrpcTransportClient(
            final GrpcChannelConfig config) {

        Validation.requireNonNull(
                config,
                "config must not be null");

        final ManagedChannelBuilder<?> builder =
                ManagedChannelBuilder.forTarget(config.target());

        if (!config.useTls()) {
            builder.usePlaintext();
        }

        this.channel = builder.build();
        this.defaultTimeout = config.connectTimeout() != null
                ? config.connectTimeout()
                : Duration.ofSeconds(30);
    }

    /**
     * Creates a gRPC transport client with a pre-configured managed channel.
     *
     * @param channel        the gRPC managed channel
     * @param defaultTimeout the default call timeout (may be {@code null})
     */
    public GrpcTransportClient(
            final ManagedChannel channel,
            final Duration defaultTimeout) {

        this.channel = Objects.requireNonNull(
                channel,
                "channel must not be null");
        this.defaultTimeout = defaultTimeout;
    }

    /**
     * Returns the underlying gRPC managed channel.
     *
     * @return the managed channel
     */
    public ManagedChannel channel() {
        return channel;
    }

    /**
     * Invokes a gRPC method with the given request and returns the response.
     *
     * <p>This method uses a generic unary call. The request payload is
     * sent as a single serialized protobuf message, and the response is
     * received as a single serialized protobuf message.</p>
     *
     * @param request the gRPC call request
     * @return the gRPC call response
     */
    public GrpcCallResponse call(
            final GrpcCallRequest request) {

        Validation.requireNonNull(
                request,
                "request must not be null");

        final MethodDescriptor<byte[], byte[]> method =
                MethodDescriptor.<byte[], byte[]>newBuilder()
                        .setFullMethodName(request.methodName())
                        .setType(MethodDescriptor.MethodType.UNARY)
                        .setRequestMarshaller(
                                GrpcMarshallers.BYTE_ARRAY_REQUEST_MARSHALLER)
                        .setResponseMarshaller(
                                GrpcMarshallers.BYTE_ARRAY_RESPONSE_MARSHALLER)
                        .build();

        final Metadata metadata = toMetadata(request.metadata());

        io.grpc.CallOptions callOptions =
                io.grpc.CallOptions.DEFAULT;

        final Duration timeout = request.timeout() != null
                ? request.timeout()
                : defaultTimeout;

        if (timeout != null) {
            callOptions = callOptions
                    .withDeadlineAfter(
                            timeout.toMillis(),
                            TimeUnit.MILLISECONDS);
        }

        try {
            final byte[] responseBytes =
                    ClientCalls.blockingUnaryCall(
                            channel,
                            method,
                            callOptions,
                            request.serializedRequest());

            return GrpcCallResponse.success(
                    Status.OK.getCode().value(),
                    responseBytes,
                    fromMetadata(metadata));
        } catch (final io.grpc.StatusRuntimeException exception) {
            return GrpcCallResponse.failure(
                    exception.getStatus().getCode().value(),
                    exception.getMessage(),
                    fromMetadata(metadata));
        }
    }

    private static Metadata toMetadata(
            final Map<String, String> headers) {

        final Metadata metadata = new Metadata();
        if (headers != null) {
            headers.forEach((key, value) -> {
                metadata.put(
                        Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER),
                        value);
            });
        }
        return metadata;
    }

    private static Map<String, String> fromMetadata(
            final Metadata metadata) {

        if (metadata == null) {
            return Map.of();
        }

        final Map<String, String> result = new HashMap<>();
        for (final String key : metadata.keys()) {
            final var metadataKey =
                    Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER);
            final String value = metadata.get(metadataKey);
            if (value != null) {
                result.put(key, value);
            }
        }
        return result;
    }

    @Override
    public void close() {
        channel.shutdownNow();
        try {
            if (!channel.awaitTermination(5, TimeUnit.SECONDS)) {
                channel.shutdownNow();
            }
        } catch (final InterruptedException exception) {
            Thread.currentThread().interrupt();
            channel.shutdownNow();
        }
    }

    /**
     * Checks whether the channel is currently connected.
     *
     * <p>Note: a channel may not be in the READY state immediately after
     * creation; it may take time for the channel to connect.</p>
     *
     * @return {@code true} if the channel is in the READY state
     */
    public boolean isConnected() {
        return channel.getState(false) == ConnectivityState.READY;
    }
}
