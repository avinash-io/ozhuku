package io.github.avinashio.ozhuku.domain.identity;

import io.github.avinashio.ozhuku.foundation.validation.Validation;
import java.util.Objects;

/**
 * Immutable identifier value object used by Ozhuku domain concepts.
 */
public final class Identifier {

    private final String value;

    /**
     * Creates an identifier.
     *
     * @param value identifier value
     */
    public Identifier(final String value) {
        this.value = Validation.requireNonBlank(
                value,
                "Identifier value must not be blank");
    }

    /**
     * Returns the identifier value.
     *
     * @return identifier value
     */
    public String value() {
        return value;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof Identifier)) {
            return false;
        }

        final Identifier that = (Identifier) other;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}