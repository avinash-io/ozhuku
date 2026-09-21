package io.github.avinashio.ozhuku.domain.identity;

import io.github.avinashio.ozhuku.foundation.validation.Validation;
import java.util.Objects;

/**
 * Strongly typed identifier for an Ozhuku execution.
 */
public final class ExecutionId {

    private final Identifier identifier;

    /**
     * Creates an execution identifier.
     *
     * @param value identifier value
     */
    public ExecutionId(final String value) {
        this.identifier = new Identifier(
                Validation.requireNonBlank(
                        value,
                        "Execution ID must not be blank"));
    }

    /**
     * Returns the identifier value.
     *
     * @return identifier value
     */
    public String value() {
        return identifier.value();
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof ExecutionId)) {
            return false;
        }

        final ExecutionId that = (ExecutionId) other;
        return identifier.equals(that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }

    @Override
    public String toString() {
        return identifier.toString();
    }
}