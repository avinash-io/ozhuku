package io.github.avinashio.ozhuku.transport.grpc;

import io.grpc.MethodDescriptor;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Marshallers for byte array payloads in gRPC calls.
 *
 * <p>Since Ozhuku core domain must not depend on gRPC generated classes,
 * these marshallers allow opaque byte array payloads to be used as
 * request and response objects in generic gRPC calls.</p>
 */
public final class GrpcMarshallers {

    /**
     * Marshaller for byte array request payloads.
     */
    public static final MethodDescriptor.Marshaller<byte[]>
            BYTE_ARRAY_REQUEST_MARSHALLER =
            new ByteArrayMarshaller();

    /**
     * Marshaller for byte array response payloads.
     */
    public static final MethodDescriptor.Marshaller<byte[]>
            BYTE_ARRAY_RESPONSE_MARSHALLER =
            new ByteArrayMarshaller();

    private GrpcMarshallers() {
        // Utility class.
    }

    /**
     * Simple byte array marshaller for gRPC.
     */
    private static final class ByteArrayMarshaller
            implements MethodDescriptor.Marshaller<byte[]> {

        @Override
        public InputStream stream(
                final byte[] value) {

            return new ByteArrayInputStream(value);
        }

        @Override
        public byte[] parse(
                final InputStream stream) {

            try {
                return stream.readAllBytes();
            } catch (final java.io.IOException exception) {
                throw new RuntimeException(exception);
            }
        }
    }
}
