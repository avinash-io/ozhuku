package io.github.avinashio.ozhuku.domain.execution;

import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.foundation.validation.Validation;
import java.util.Objects;

/**
 * Immutable identity reference for a source participating in an execution.
 *
 * <p>The reference associates a source resource with its parent execution.
 * It contains identity only and does not represent source access or state.</p>
 */
public final class SourceExecutionReference {

    private final ExecutionId executionId;
    private final ResourceId resourceId;

    /**
     * Creates a source execution reference.
     *
     * @param executionId parent execution identifier
     * @param resourceId source resource identifier
     */
    public SourceExecutionReference(
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
     * Returns the source resource identifier.
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

        if (!(other instanceof SourceExecutionReference)) {
            return false;
        }

        final SourceExecutionReference that =
                (SourceExecutionReference) other;

        return executionId.equals(that.executionId)
                && resourceId.equals(that.resourceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(executionId, resourceId);
    }

    @Override
    public String toString() {
        return "SourceExecutionReference{"
                + "executionId=" + executionId
                + ", resourceId=" + resourceId
                + '}';
    }
}