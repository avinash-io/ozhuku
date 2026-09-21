package io.github.avinashio.ozhuku.foundation.validation;

import io.github.avinashio.ozhuku.foundation.exception.ValidationException;

/**
 * Common validation utilities used across Ozhuku.
 */
public final class Validation {

    private Validation() {
        // Utility class.
    }

    /**
     * Requires that a value is not null.
     *
     * @param value value to validate
     * @param message validation failure description
     * @param <T> value type
     * @return the validated value
     * @throws ValidationException if the value is null
     */
    public static <T> T requireNonNull(final T value, final String message) {
        if (value == null) {
            throw new ValidationException(message);
        }

        return value;
    }

    /**
     * Requires that text is not null or blank.
     *
     * @param value text to validate
     * @param message validation failure description
     * @return the validated text
     * @throws ValidationException if the text is null or blank
     */
    public static String requireNonBlank(final String value, final String message) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(message);
        }

        return value;
    }

    /**
     * Requires that a condition is true.
     *
     * @param condition condition to validate
     * @param message validation failure description
     * @throws ValidationException if the condition is false
     */
    public static void require(final boolean condition, final String message) {
        if (!condition) {
            throw new ValidationException(message);
        }
    }
}