package io.github.avinashio.ozhuku.domain.execution;

import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.foundation.validation.Validation;
import java.util.Objects;

/**
 * Immutable identity reference for a destination participating in an
 * execution.
 *
 * <p>The reference associates a destination resource with its parent
 * execution. It contains identity only and does not represent destination
 * access, delivery state, or commit state.</p>
 */
public final class DestinationExecutionReference {

    private final ExecutionId executionId;
    private final ResourceId resourceId;

    /**
     * Creates a destination execution reference.
     *
     * @param executionId parent execution identifier
     * @param resourceId destination resource identifier
     */
    public DestinationExecutionReference(
            final ExecutionId executionId,
            final ResourceId resourceId) {

        this.executionId = Validation.requireNonNull(
                executionId,
                "Execution ID must not be null");
        this.resourceId = Validation.requireNonNull(
                resourceId,
                "Resource ID must not be null");
    }

    /**
     * Returns the parent execution identifier.
     *
     * @return execution identifier
     */
    public ExecutionId executionId() {
        return executionId;
    }

    /**
     * Returns the destination resource identifier.
     *
     * @return resource identifier
     */
    public ResourceId resourceId() {
        return resourceId;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof DestinationExecutionReference)) {
            return false;
        }

        final DestinationExecutionReference that =
                (DestinationExecutionReference) other;

        return executionId.equals(that.executionId)
                && resourceId.equals(that.resourceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(executionId, resourceId);
    }

    @Override
    public String toString() {
        return "DestinationExecutionReference{"
                + "executionId=" + executionId
                + ", resourceId=" + resourceId
                + '}';
    }
}