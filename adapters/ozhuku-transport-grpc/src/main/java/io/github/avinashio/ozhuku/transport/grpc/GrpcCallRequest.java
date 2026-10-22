package io.github.avinashio.ozhuku.transport.grpc;

import io.github.avinashio.ozhuku.foundation.validation.Validation;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a gRPC call specification for Ozhuku's gRPC transport.
 *
 * <p>This is a generic request descriptor that can be used with any gRPC
 * service stub. It carries the method name, serialized protobuf request
 * payload, custom metadata headers, and an optional timeout.</p>
 *
 * <p>Since Ozhuku core domain must not depend on gRPC generated classes,
 * the request payload is treated as opaque serialized bytes. The caller
 * is responsible for generating and serializing protobuf messages.</p>
 */
public final class GrpcCallRequest {

    private final String methodName;

    private final byte[] serializedRequest;

    private final Map<String, String> metadata;

    private final Duration timeout;

    private GrpcCallRequest(
            final String methodName,
            final byte[] serializedRequest,
            final Map<String, String> metadata,
            final Duration timeout) {

        this.methodName = Validation.requireNonNull(
                methodName,
                "methodName must not be null");
        this.serializedRequest = Objects.requireNonNull(
                serializedRequest,
                "serializedRequest must not be null");
        this.metadata = metadata != null
                ? Collections.unmodifiableMap(metadata)
                : Collections.emptyMap();
        this.timeout = timeout;
    }

    /**
     * Creates a gRPC call request with a serialized payload and a default
     * timeout.
     *
     * @param methodName       the gRPC method name (e.g. "/package.Service/Method")
     * @param serializedRequest the serialized protobuf request payload
     * @return a gRPC call request
     */
    public static GrpcCallRequest to(
            final String methodName,
            final byte[] serializedRequest) {

        return new GrpcCallRequest(
                methodName,
                serializedRequest,
                null,
                Duration.ofSeconds(30));
    }

    /**
     * Creates a gRPC call request with a serialized payload, custom
     * metadata, and a default timeout.
     *
     * @param methodName        the gRPC method name
     * @param serializedRequest the serialized protobuf request payload
     * @param metadata          custom gRPC metadata headers (key-value)
     * @return a gRPC call request
     */
    public static GrpcCallRequest to(
            final String methodName,
            final byte[] serializedRequest,
            final Map<String, String> metadata) {

        return new GrpcCallRequest(
                methodName,
                serializedRequest,
                metadata,
                Duration.ofSeconds(30));
    }

    /**
     * Creates a gRPC call request with a serialized payload, custom
     * metadata, and an explicit timeout.
     *
     * @param methodName        the gRPC method name
     * @param serializedRequest the serialized protobuf request payload
     * @param metadata          custom gRPC metadata headers (may be {@code null})
     * @param timeout           the call timeout (may be {@code null} for default)
     * @return a gRPC call request
     */
    public static GrpcCallRequest to(
            final String methodName,
            final byte[] serializedRequest,
            final Map<String, String> metadata,
            final Duration timeout) {

        return new GrpcCallRequest(
                methodName,
                serializedRequest,
                metadata,
                timeout);
    }

    /**
     * Returns the gRPC method name.
     *
     * @return method name
     */
    public String methodName() {
        return methodName;
    }

    /**
     * Returns the serialized protobuf request payload.
     *
     * @return serialized request bytes
     */
    public byte[] serializedRequest() {
        return serializedRequest;
    }

    /**
     * Returns the custom metadata headers.
     *
     * @return metadata map (possibly empty)
     */
    public Map<String, String> metadata() {
        return metadata;
    }

    /**
     * Returns the call timeout.
     *
     * @return timeout duration, or {@code null} if unspecified
     */
    public Duration timeout() {
        return timeout;
    }
}
