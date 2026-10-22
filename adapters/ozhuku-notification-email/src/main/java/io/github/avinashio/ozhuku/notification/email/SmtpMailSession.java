package io.github.avinashio.ozhuku.notification.email;

import io.github.avinashio.ozhuku.notification.model.MailServerProfile;
import io.github.avinashio.ozhuku.security.secret.SecretResolver;
import java.util.Objects;

/**
 * Represents an SMTP mail session configuration.
 *
 * <p>This class captures the resolved configuration for an SMTP session,
 * including the resolved password from a {@link SecretResolver}. The
 * session is immutable and can be used for testing or as the basis for
 * an actual SMTP connection in a Jakarta Mail implementation.</p>
 */
public final class SmtpMailSession {

    private final String host;

    private final int port;

    private final MailServerProfile.SecurityMode securityMode;

    private final MailServerProfile.AuthenticationMode authenticationMode;

    private final String sender;

    private final String replyTo;

    private final char[] password;

    /**
     * Creates an SMTP mail session from a profile and secret resolver.
     *
     * @param profile       the mail server profile
     * @param secretResolver the secret resolver for credential lookup
     */
    public SmtpMailSession(
            final MailServerProfile profile,
            final SecretResolver secretResolver) {

        this.host = Objects.requireNonNull(
                profile, "profile must not be null").host();
        this.port = profile.port();
        this.securityMode = profile.securityMode();
        this.authenticationMode = profile.authenticationMode();
        this.sender = profile.sender();
        this.replyTo = profile.replyTo();

        if (authenticationMode
                == MailServerProfile.AuthenticationMode.USERNAME_PASSWORD
                && profile.secretReference() != null
                && secretResolver != null) {
            final char[] resolved = secretResolver.resolve(
                    profile.secretReference()).orElse(new char[0]);
            this.password = resolved.length > 0 ? resolved : null;
        } else if (authenticationMode
                == MailServerProfile.AuthenticationMode.OAUTH2
                && profile.secretReference() != null
                && secretResolver != null) {
            final char[] resolved = secretResolver.resolve(
                    profile.secretReference()).orElse(new char[0]);
            this.password = resolved.length > 0 ? resolved : null;
        } else {
            this.password = null;
        }
    }

    /**
     * Returns the SMTP host.
     *
     * @return host
     */
    public String host() {
        return host;
    }

    /**
     * Returns the SMTP port.
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
    public MailServerProfile.SecurityMode securityMode() {
        return securityMode;
    }

    /**
     * Returns the authentication mode.
     *
     * @return authentication mode
     */
    public MailServerProfile.AuthenticationMode authenticationMode() {
        return authenticationMode;
    }

    /**
     * Returns the sender email address.
     *
     * @return sender
     */
    public String sender() {
        return sender;
    }

    /**
     * Returns the reply-to email address.
     *
     * @return reply-to, or {@code null} if not set
     */
    public String replyTo() {
        return replyTo;
    }

    /**
     * Returns the resolved password.
     *
     * @return password chars, or {@code null} if not applicable
     */
    public char[] password() {
        return password;
    }
}
