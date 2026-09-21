package io.github.avinashio.ozhuku.foundation.exception;

/**
 * Indicates that supplied data violates an Ozhuku validation rule.
 */
public class ValidationException extends RuntimeException {

    /**
     * Creates a validation exception with the supplied message.
     *
     * @param message validation failure description
     */
    public ValidationException(final String message) {
        super(message);
    }

    /**
     * Creates a validation exception with the supplied message and cause.
     *
     * @param message validation failure description
     * @param cause underlying cause
     */
    public ValidationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}