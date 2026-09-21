package io.github.avinashio.ozhuku.domain.execution;

import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.PipelineId;
import io.github.avinashio.ozhuku.domain.identity.PipelineVersion;
import io.github.avinashio.ozhuku.foundation.validation.Validation;
import java.util.Objects;

/**
 * Immutable reference identifying the pipeline version associated with an
 * execution.
 *
 * <p>The reference contains identity only. Execution state and lifecycle
 * information are represented by the execution model.</p>
 */
public final class ExecutionReference {

    private final ExecutionId executionId;
    private final PipelineId pipelineId;
    private final PipelineVersion pipelineVersion;

    /**
     * Creates an execution reference.
     *
     * @param executionId execution identifier
     * @param pipelineId pipeline identifier
     * @param pipelineVersion pipeline version used by the execution
     */
    public ExecutionReference(
            final ExecutionId executionId,
            final PipelineId pipelineId,
            final PipelineVersion pipelineVersion) {

        this.executionId = Validation.requireNonNull(
                executionId,
                "Execution ID must not be null");
        this.pipelineId = Validation.requireNonNull(
                pipelineId,
                "Pipeline ID must not be null");
        this.pipelineVersion = Validation.requireNonNull(
                pipelineVersion,
                "Pipeline version must not be null");
    }

    /**
     * Returns the execution identifier.
     *
     * @return execution identifier
     */
    public ExecutionId executionId() {
        return executionId;
    }

    /**
     * Returns the pipeline identifier.
     *
     * @return pipeline identifier
     */
    public PipelineId pipelineId() {
        return pipelineId;
    }

    /**
     * Returns the pipeline version used by the execution.
     *
     * @return pipeline version
     */
    public PipelineVersion pipelineVersion() {
        return pipelineVersion;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof ExecutionReference)) {
            return false;
        }

        final ExecutionReference that = (ExecutionReference) other;

        return executionId.equals(that.executionId)
                && pipelineId.equals(that.pipelineId)
                && pipelineVersion.equals(that.pipelineVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                executionId,
                pipelineId,
                pipelineVersion);
    }

    @Override
    public String toString() {
        return "ExecutionReference{"
                + "executionId=" + executionId
                + ", pipelineId=" + pipelineId
                + ", pipelineVersion=" + pipelineVersion
                + '}';
    }
}