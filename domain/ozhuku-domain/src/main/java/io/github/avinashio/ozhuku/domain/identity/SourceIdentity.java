package io.github.avinashio.ozhuku.domain.identity;

import io.github.avinashio.ozhuku.foundation.validation.Validation;
import java.util.Objects;

/**
 * Immutable identity of a source resource as observed by Ozhuku.
 *
 * <p>Source identity is intentionally separate from resource location.
 * Location describes where a resource can be found; identity describes the
 * logical source resource used for processing and deduplication.</p>
 */
public final class SourceIdentity {

    private final String value;

    /**
     * Creates a source identity.
     *
     * @param value stable source identity value
     */
    public SourceIdentity(final String value) {
        this.value = Validation.requireNonBlank(
                value,
                "Source identity value must not be blank");
    }

    /**
     * Returns the source identity value.
     *
     * @return source identity value
     */
    public String value() {
        return value;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof SourceIdentity)) {
            return false;
        }

        final SourceIdentity that = (SourceIdentity) other;
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