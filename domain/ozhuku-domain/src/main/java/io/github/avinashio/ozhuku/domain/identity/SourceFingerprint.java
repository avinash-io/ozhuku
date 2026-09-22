package io.github.avinashio.ozhuku.domain.identity;

import io.github.avinashio.ozhuku.foundation.validation.Validation;
import java.util.Objects;

/**
 * Immutable fingerprint representing the observed state of a source resource.
 *
 * <p>The fingerprint value is intentionally opaque to the domain. Different
 * source adapters may derive it using mechanisms appropriate to the source,
 * such as content hashes, object version identifiers, or provider metadata.</p>
 */
public final class SourceFingerprint {

    private final String value;

    /**
     * Creates a source fingerprint.
     *
     * @param value opaque source state representation
     */
    public SourceFingerprint(final String value) {
        this.value = Validation.requireNonBlank(
                value,
                "Source fingerprint value must not be blank");
    }

    /**
     * Returns the fingerprint value.
     *
     * @return fingerprint value
     */
    public String value() {
        return value;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof SourceFingerprint)) {
            return false;
        }

        final SourceFingerprint that = (SourceFingerprint) other;
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