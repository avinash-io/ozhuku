package io.github.avinashio.ozhuku.transport.grpc;

/**
 * Thrown when a gRPC transport operation fails.
 *
 * <p>This exception wraps lower-level gRPC status exceptions and provides
 * a transport-level error that Ozhuku application code can handle
 * uniformly across different transport implementations.</p>
 */
public class GrpcTransportException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a gRPC transport exception with a message.
     *
     * @param message the error message
     */
    public GrpcTransportException(
            final String message) {

        super(message);
    }

    /**
     * Creates a gRPC transport exception with a message and cause.
     *
     * @param message the error message
     * @param cause   the underlying cause
     */
    public GrpcTransportException(
            final String message,
            final Throwable cause) {

        super(message, cause);
    }
}
