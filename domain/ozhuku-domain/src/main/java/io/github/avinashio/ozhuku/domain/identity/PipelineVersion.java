package io.github.avinashio.ozhuku.domain.identity;

import io.github.avinashio.ozhuku.foundation.exception.ValidationException;
import java.util.Objects;

/**
 * Immutable version number for a pipeline.
 */
public final class PipelineVersion {

    private final long value;

    /**
     * Creates a pipeline version.
     *
     * @param value positive version number
     */
    public PipelineVersion(final long value) {
        if (value <= 0) {
            throw new ValidationException(
                    "Pipeline version must be greater than zero");
        }

        this.value = value;
    }

    /**
     * Returns the version number.
     *
     * @return version number
     */
    public long value() {
        return value;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof PipelineVersion)) {
            return false;
        }

        final PipelineVersion that = (PipelineVersion) other;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return Long.toString(value);
    }
}