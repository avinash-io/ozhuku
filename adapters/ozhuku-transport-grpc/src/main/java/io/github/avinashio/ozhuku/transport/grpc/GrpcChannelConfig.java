package io.github.avinashio.ozhuku.transport.grpc;

import io.github.avinashio.ozhuku.foundation.validation.Validation;
import java.time.Duration;

/**
 * Configuration for a gRPC managed channel.
 *
 * <p>Specifies the target host and port, whether to use TLS, and optional
 * connection parameters. The target is specified as a host/port pair
 * (or a full gRPC target string) and is resolved by the underlying
 * gRPC library.</p>
 *
 * <p>Example:
 * <pre>
 * GrpcChannelConfig config = GrpcChannelConfig.builder()
 *     .host("api.example.com")
 *     .port(443)
 *     .useTls(true)
 *     .build();
 * </pre></p>
 */
public final class GrpcChannelConfig {

    private final String target;

    private final boolean useTls;

    private final Duration connectTimeout;

    private GrpcChannelConfig(
            final String target,
            final boolean useTls,
            final Duration connectTimeout) {

        this.target = Validation.requireNonNull(
                target,
                "target must not be null");
        this.useTls = useTls;
        this.connectTimeout = connectTimeout;
    }

    /**
     * Returns the gRPC target string.
     *
     * @return the target (e.g. "host:port" or "dns:///host:port")
     */
    public String target() {
        return target;
    }

    /**
     * Returns {@code true} if TLS should be used.
     *
     * @return {@code true} for TLS
     */
    public boolean useTls() {
        return useTls;
    }

    /**
     * Returns the connection timeout, or {@code null} if unspecified.
     *
     * @return connect timeout duration
     */
    public Duration connectTimeout() {
        return connectTimeout;
    }

    /**
     * Creates a builder for a {@link GrpcChannelConfig}.
     *
     * @param target the gRPC target string
     * @return a new builder
     */
    public static Builder builder(final String target) {
        return new Builder(target);
    }

    /**
     * Builder for {@link GrpcChannelConfig}.
     */
    public static final class Builder {

        private final String target;

        private boolean useTls;

        private Duration connectTimeout;

        private Builder(final String target) {
            this.target = target;
        }

        /**
         * Enables or disables TLS.
         *
         * @param useTls {@code true} to use TLS
         * @return this builder
         */
        public Builder useTls(final boolean useTls) {
            this.useTls = useTls;
            return this;
        }

        /**
         * Sets the connection timeout.
         *
         * @param connectTimeout the connect timeout duration
         * @return this builder
         */
        public Builder connectTimeout(final Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        /**
         * Builds the gRPC channel configuration.
         *
         * @return a new {@link GrpcChannelConfig}
         */
        public GrpcChannelConfig build() {
            return new GrpcChannelConfig(
                    target,
                    useTls,
                    connectTimeout);
        }
    }
}
