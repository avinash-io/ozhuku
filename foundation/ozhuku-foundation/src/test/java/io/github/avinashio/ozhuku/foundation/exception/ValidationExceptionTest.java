package io.github.avinashio.ozhuku.foundation.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ValidationExceptionTest {

    @Test
    void shouldPreserveValidationMessage() {
        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> {
                    throw new ValidationException("Invalid value");
                });

        assertEquals("Invalid value", exception.getMessage());
    }

    @Test
    void shouldPreserveCause() {
        final IllegalArgumentException cause =
                new IllegalArgumentException("Original cause");

        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> {
                    throw new ValidationException("Validation failed", cause);
                });

        assertEquals(cause, exception.getCause());
    }
}