package io.github.avinashio.ozhuku.domain.execution;

import io.github.avinashio.ozhuku.foundation.validation.Validation;
import java.util.Objects;

/**
 * Immutable identity of the durable commit associated with a destination
 * execution.
 *
 * <p>The reference identifies the logical destination commit. It does not
 * represent the commit result itself or perform any persistence operation.</p>
 */
public final class DestinationCommitReference {

    private final DestinationExecutionReference destinationExecutionReference;

    /**
     * Creates a destination commit reference.
     *
     * @param destinationExecutionReference destination execution identity
     */
    public DestinationCommitReference(
            final DestinationExecutionReference destinationExecutionReference) {

        this.destinationExecutionReference = Validation.requireNonNull(
                destinationExecutionReference,
                "Destination execution reference must not be null");
    }

    /**
     * Returns the associated destination execution reference.
     *
     * @return destination execution reference
     */
    public DestinationExecutionReference destinationExecutionReference() {
        return destinationExecutionReference;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof DestinationCommitReference)) {
            return false;
        }

        final DestinationCommitReference that =
                (DestinationCommitReference) other;

        return destinationExecutionReference.equals(
                that.destinationExecutionReference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(destinationExecutionReference);
    }

    @Override
    public String toString() {
        return "DestinationCommitReference{"
                + "destinationExecutionReference="
                + destinationExecutionReference
                + '}';
    }
}