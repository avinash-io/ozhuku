package io.github.avinashio.ozhuku.domain.audit;

import io.github.avinashio.ozhuku.foundation.validation.Validation;

/**
 * Unique identifier for an audit event.
 */
public final class AuditEventId {

    private final String value;

    /**
     * Creates an audit event identifier.
     *
     * @param value identifier value
     */
    public AuditEventId(final String value) {
        this.value = Validation.requireNonBlank(
                value,
                "Audit event ID must not be blank");
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

        if (!(other instanceof AuditEventId)) {
            return false;
        }

        final AuditEventId that = (AuditEventId) other;
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
