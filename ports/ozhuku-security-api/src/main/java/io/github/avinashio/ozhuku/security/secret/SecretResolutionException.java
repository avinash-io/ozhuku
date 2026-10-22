package io.github.avinashio.ozhuku.security.secret;

/**
 * Thrown when a secret cannot be resolved.
 *
 * <p>This exception indicates that a {@link SecretReference} could not be
 * resolved to a secret value, either because the secret store was
 * unavailable, the reference was malformed, or the secret does not exist
 * and resolution was required.</p>
 */
public class SecretResolutionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a secret resolution exception with a message.
     *
     * @param message the error message
     */
    public SecretResolutionException(final String message) {
        super(message);
    }

    /**
     * Creates a secret resolution exception with a message and cause.
     *
     * @param message the error message
     * @param cause   the underlying cause
     */
    public SecretResolutionException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
