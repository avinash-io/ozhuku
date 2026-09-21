package io.github.avinashio.ozhuku.domain.identity;

import io.github.avinashio.ozhuku.foundation.validation.Validation;
import java.util.Objects;

/**
 * Strongly typed identifier for a resource.
 */
public final class ResourceId {

    private final Identifier identifier;

    /**
     * Creates a resource identifier.
     *
     * @param value identifier value
     */
    public ResourceId(final String value) {
        this.identifier = new Identifier(
                Validation.requireNonBlank(
                        value,
                        "Resource ID must not be blank"));
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

        if (!(other instanceof ResourceId)) {
            return false;
        }

        final ResourceId that = (ResourceId) other;
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