package io.github.avinashio.ozhuku.domain.lease;

import io.github.avinashio.ozhuku.foundation.validation.Validation;

/**
 * Unique identifier for a processing lease.
 */
public final class LeaseId {

    private final String value;

    /**
     * Creates a lease identifier.
     *
     * @param value identifier value
     */
    public LeaseId(final String value) {
        this.value = Validation.requireNonBlank(
                value,
                "Lease ID must not be blank");
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

        if (!(other instanceof LeaseId)) {
            return false;
        }

        final LeaseId that = (LeaseId) other;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
