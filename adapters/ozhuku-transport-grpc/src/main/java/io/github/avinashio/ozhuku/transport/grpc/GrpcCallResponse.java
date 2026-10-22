package io.github.avinashio.ozhuku.transport.grpc;

import java.util.Map;
import java.util.Objects;

/**
 * Represents the response from a gRPC call.
 *
 * <p>Contains the serialized protobuf response payload, gRPC status code,
 * error details (if any), and response metadata. The payload is treated
 * as opaque serialized bytes so that the core domain remains independent
 * of gRPC generated classes.</p>
 */
public final class GrpcCallResponse {

    private final int statusCode;

    private final byte[] serializedResponse;

    private final Map<String, String> metadata;

    private final boolean successful;

    private final String errorMessage;

    private GrpcCallResponse(
            final int statusCode,
            final byte[] serializedResponse,
            final Map<String, String> metadata,
            final boolean successful,
            final String errorMessage) {

        this.statusCode = statusCode;
        this.serializedResponse = serializedResponse;
        this.metadata = metadata;
        this.successful = successful;
        this.errorMessage = errorMessage;
    }

    /**
     * Creates a successful gRPC response.
     *
     * @param statusCode      the gRPC status code value
     * @param serializedResponse the serialized protobuf response payload
     * @param metadata         the response metadata
     * @return a successful response
     */
    public static GrpcCallResponse success(
            final int statusCode,
            final byte[] serializedResponse,
            final Map<String, String> metadata) {

        return new GrpcCallResponse(
                statusCode,
                serializedResponse,
                metadata,
                true,
                null);
    }

    /**
     * Creates a failed gRPC response.
     *
     * @param statusCode  the gRPC status code value
     * @param errorMessage the error message
     * @param metadata    the response metadata (may be {@code null})
     * @return a failed response
     */
    public static GrpcCallResponse failure(
            final int statusCode,
            final String errorMessage,
            final Map<String, String> metadata) {

        return new GrpcCallResponse(
                statusCode,
                new byte[0],
                metadata != null ? metadata : Map.of(),
                false,
                errorMessage);
    }

    /**
     * Returns the gRPC status code value.
     *
     * @return status code value
     */
    public int statusCode() {
        return statusCode;
    }

    /**
     * Returns the serialized protobuf response payload.
     *
     * @return serialized response bytes
     */
    public byte[] serializedResponse() {
        return serializedResponse;
    }

    /**
     * Returns the response metadata.
     *
     * @return metadata map
     */
    public Map<String, String> metadata() {
        return metadata;
    }

    /**
     * Returns {@code true} if the call succeeded.
     *
     * @return {@code true} for successful calls
     */
    public boolean isSuccessful() {
        return successful;
    }

    /**
     * Returns the error message for failed calls.
     *
     * @return error message, or {@code null} if the call succeeded
     */
    public String errorMessage() {
        return errorMessage;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof GrpcCallResponse)) {
            return false;
        }

        final GrpcCallResponse that = (GrpcCallResponse) other;
        return statusCode == that.statusCode
                && successful == that.successful
                && Objects.equals(errorMessage, that.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(statusCode, successful, errorMessage);
    }
}
