package io.github.avinashio.ozhuku.notification.model;

import io.github.avinashio.ozhuku.security.secret.SecretReference;
import java.time.Duration;
import java.util.Objects;

/**
 * Configuration profile for an SMTP mail server.
 *
 * <p>Never store SMTP credentials in pipeline YAML. The password
 * is referenced via a {@link SecretReference} and resolved at
 * runtime by a {@code SecretResolver}.</p>
 */
public final class MailServerProfile {

    public enum SecurityMode {
        NONE,
        STARTTLS,
        TLS
    }

    public enum AuthenticationMode {
        NONE,
        USERNAME_PASSWORD,
        OAUTH2
    }

    private final String id;

    private final String host;

    private final int port;

    private final SecurityMode securityMode;

    private final AuthenticationMode authenticationMode;

    private final Duration connectionTimeout;

    private final Duration readTimeout;

    private final Duration writeTimeout;

    private final String sender;

    private final String replyTo;

    private final SecretReference secretReference;

    private MailServerProfile(
            final String id,
            final String host,
            final int port,
            final SecurityMode securityMode,
            final AuthenticationMode authenticationMode,
            final Duration connectionTimeout,
            final Duration readTimeout,
            final Duration writeTimeout,
            final String sender,
            final String replyTo,
            final SecretReference secretReference) {

        this.id = Objects.requireNonNull(id, "id must not be null");
        this.host = Objects.requireNonNull(host, "host must not be null");
        this.port = port;
        this.securityMode = Objects.requireNonNull(
                securityMode, "securityMode must not be null");
        this.authenticationMode = Objects.requireNonNull(
                authenticationMode, "authenticationMode must not be null");
        this.connectionTimeout = connectionTimeout;
        this.readTimeout = readTimeout;
        this.writeTimeout = writeTimeout;
        this.sender = sender;
        this.replyTo = replyTo;
        this.secretReference = secretReference;
    }

    /**
     * Returns the profile identifier.
     *
     * @return profile ID
     */
    public String id() {
        return id;
    }

    /**
     * Returns the mail server host.
     *
     * @return host
     */
    public String host() {
        return host;
    }

    /**
     * Returns the mail server port.
     *
     * @return port
     */
    public int port() {
        return port;
    }

    /**
     * Returns the security mode.
     *
     * @return security mode
     */
    public SecurityMode securityMode() {
        return securityMode;
    }

    /**
     * Returns the authentication mode.
     *
     * @return authentication mode
     */
    public AuthenticationMode authenticationMode() {
        return authenticationMode;
    }

    /**
     * Returns the connection timeout.
     *
     * @return connection timeout
     */
    public Duration connectionTimeout() {
        return connectionTimeout;
    }

    /**
     * Returns the read timeout.
     *
     * @return read timeout
     */
    public Duration readTimeout() {
        return readTimeout;
    }

    /**
     * Returns the write timeout.
     *
     * @return write timeout
     */
    public Duration writeTimeout() {
        return writeTimeout;
    }

    /**
     * Returns the sender email address.
     *
     * @return sender address
     */
    public String sender() {
        return sender;
    }

    /**
     * Returns the reply-to email address.
     *
     * @return reply-to address, or {@code null} if not configured
     */
    public String replyTo() {
        return replyTo;
    }

    /**
     * Returns the secret reference for the mail password.
     *
     * @return secret reference
     */
    public SecretReference secretReference() {
        return secretReference;
    }

    /**
     * Creates a builder for a {@link MailServerProfile}.
     *
     * @param id   the profile ID
     * @param host the mail server host
     * @return a new builder
     */
    public static Builder builder(
            final String id,
            final String host) {
        return new Builder(id, host);
    }

    /**
     * Builder for {@link MailServerProfile}.
     */
    public static final class Builder {

        private final String id;

        private final String host;

        private int port = 587;

        private SecurityMode securityMode = SecurityMode.STARTTLS;

        private AuthenticationMode authenticationMode =
                AuthenticationMode.USERNAME_PASSWORD;

        private Duration connectionTimeout = Duration.ofSeconds(30);

        private Duration readTimeout = Duration.ofSeconds(30);

        private Duration writeTimeout = Duration.ofSeconds(30);

        private String sender;

        private String replyTo;

        private SecretReference secretReference;

        private Builder(
                final String id,
                final String host) {

            this.id = id;
            this.host = host;
        }

        /**
         * Sets the mail server port.
         *
         * @param port the port
         * @return this builder
         */
        public Builder port(final int port) {
            this.port = port;
            return this;
        }

        /**
         * Sets the security mode.
         *
         * @param securityMode the security mode
         * @return this builder
         */
        public Builder securityMode(final SecurityMode securityMode) {
            this.securityMode = securityMode;
            return this;
        }

        /**
         * Sets the authentication mode.
         *
         * @param authenticationMode the authentication mode
         * @return this builder
         */
        public Builder authenticationMode(
                final AuthenticationMode authenticationMode) {
            this.authenticationMode = authenticationMode;
            return this;
        }

        /**
         * Sets the connection timeout.
         *
         * @param connectionTimeout the connection timeout
         * @return this builder
         */
        public Builder connectionTimeout(final Duration connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        /**
         * Sets the read timeout.
         *
         * @param readTimeout the read timeout
         * @return this builder
         */
        public Builder readTimeout(final Duration readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        /**
         * Sets the write timeout.
         *
         * @param writeTimeout the write timeout
         * @return this builder
         */
        public Builder writeTimeout(final Duration writeTimeout) {
            this.writeTimeout = writeTimeout;
            return this;
        }

        /**
         * Sets the sender email address.
         *
         * @param sender the sender address
         * @return this builder
         */
        public Builder sender(final String sender) {
            this.sender = sender;
            return this;
        }

        /**
         * Sets the reply-to email address.
         *
         * @param replyTo the reply-to address
         * @return this builder
         */
        public Builder replyTo(final String replyTo) {
            this.replyTo = replyTo;
            return this;
        }

        /**
         * Sets the secret reference for the mail password.
         *
         * @param secretReference the secret reference
         * @return this builder
         */
        public Builder secretReference(
                final SecretReference secretReference) {
            this.secretReference = secretReference;
            return this;
        }

        /**
         * Builds the mail server profile.
         *
         * @return a new {@link MailServerProfile}
         */
        public MailServerProfile build() {
            return new MailServerProfile(
                    id,
                    host,
                    port,
                    securityMode,
                    authenticationMode,
                    connectionTimeout,
                    readTimeout,
                    writeTimeout,
                    sender,
                    replyTo,
                    secretReference);
        }
    }
}
