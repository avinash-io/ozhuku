package io.github.avinashio.ozhuku.domain.execution;

import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.FlowId;
import io.github.avinashio.ozhuku.foundation.validation.Validation;
import java.time.Instant;
import java.util.Objects;

/**
 * Represents execution of a specific flow within an Ozhuku execution.
 *
 * <p>The flow execution contains domain lifecycle state only. Persistence,
 * scheduling, recovery orchestration, and infrastructure concerns belong
 * outside the domain model.</p>
 */
public final class FlowExecution {

    private final ExecutionId executionId;
    private final FlowId flowId;
    private final FlowExecutionStatus status;
    private final Instant startedAt;
    private final Instant completedAt;

    /**
     * Creates a pending flow execution.
     *
     * @param executionId parent execution identifier
     * @param flowId flow identifier
     */
    public FlowExecution(
            final ExecutionId executionId,
            final FlowId flowId) {

        this(
                executionId,
                flowId,
                FlowExecutionStatus.PENDING,
                null,
                null);
    }

    private FlowExecution(
            final ExecutionId executionId,
            final FlowId flowId,
            final FlowExecutionStatus status,
            final Instant startedAt,
            final Instant completedAt) {

        this.executionId = Validation.requireNonNull(
                executionId,
                "Execution ID must not be null");
        this.flowId = Validation.requireNonNull(
                flowId,
                "Flow ID must not be null");
        this.status = Validation.requireNonNull(
                status,
                "Flow execution status must not be null");

        if (startedAt != null
                && completedAt != null
                && completedAt.isBefore(startedAt)) {
            throw new IllegalArgumentException(
                    "Completed timestamp must not be before started timestamp");
        }

        this.startedAt = startedAt;
        this.completedAt = completedAt;
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
     * Returns the flow identifier.
     *
     * @return flow identifier
     */
    public FlowId flowId() {
        return flowId;
    }

    /**
     * Returns the current flow execution status.
     *
     * @return flow execution status
     */
    public FlowExecutionStatus status() {
        return status;
    }

    /**
     * Returns the flow execution start timestamp.
     *
     * @return start timestamp, or {@code null} if not started
     */
    public Instant startedAt() {
        return startedAt;
    }

    /**
     * Returns the flow execution completion timestamp.
     *
     * @return completion timestamp, or {@code null} if not complete
     */
    public Instant completedAt() {
        return completedAt;
    }

    /**
     * Creates a running copy of this flow execution.
     *
     * @param startedAt flow execution start timestamp
     * @return running flow execution
     */
    public FlowExecution start(final Instant startedAt) {
        Validation.requireNonNull(
                startedAt,
                "Flow execution start timestamp must not be null");

        if (status != FlowExecutionStatus.PENDING) {
            throw new IllegalStateException(
                    "Only pending flow executions can be started");
        }

        return new FlowExecution(
                executionId,
                flowId,
                FlowExecutionStatus.RUNNING,
                startedAt,
                null);
    }

    /**
     * Creates a completed copy of this flow execution.
     *
     * @param completedAt flow execution completion timestamp
     * @return completed flow execution
     */
    public FlowExecution complete(final Instant completedAt) {
        Validation.requireNonNull(
                completedAt,
                "Flow execution completion timestamp must not be null");

        if (status != FlowExecutionStatus.RUNNING) {
            throw new IllegalStateException(
                    "Only running flow executions can be completed");
        }

        return new FlowExecution(
                executionId,
                flowId,
                FlowExecutionStatus.COMPLETED,
                startedAt,
                completedAt);
    }

    /**
     * Creates a failed copy of this flow execution.
     *
     * @param completedAt failure timestamp
     * @return failed flow execution
     */
    public FlowExecution fail(final Instant completedAt) {
        Validation.requireNonNull(
                completedAt,
                "Flow execution failure timestamp must not be null");

        if (status != FlowExecutionStatus.RUNNING) {
            throw new IllegalStateException(
                    "Only running flow executions can fail");
        }

        return new FlowExecution(
                executionId,
                flowId,
                FlowExecutionStatus.FAILED,
                startedAt,
                completedAt);
    }

    /**
     * Creates a cancelled copy of this flow execution.
     *
     * @param completedAt cancellation timestamp
     * @return cancelled flow execution
     */
    public FlowExecution cancel(final Instant completedAt) {
        Validation.requireNonNull(
                completedAt,
                "Flow execution cancellation timestamp must not be null");

        if (status != FlowExecutionStatus.PENDING
                && status != FlowExecutionStatus.RUNNING) {
            throw new IllegalStateException(
                    "Only pending or running flow executions can be cancelled");
        }

        return new FlowExecution(
                executionId,
                flowId,
                FlowExecutionStatus.CANCELLED,
                startedAt,
                completedAt);
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof FlowExecution)) {
            return false;
        }

        final FlowExecution that = (FlowExecution) other;

        return executionId.equals(that.executionId)
                && flowId.equals(that.flowId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(executionId, flowId);
    }

    @Override
    public String toString() {
        return "FlowExecution{"
                + "executionId=" + executionId
                + ", flowId=" + flowId
                + ", status=" + status
                + ", startedAt=" + startedAt
                + ", completedAt=" + completedAt
                + '}';
    }

    /**
     * Rehydrates a flow execution from durable persistence.
     *
     * <p>This method is intended for persistence adapters reconstructing
     * previously persisted domain state. It does not perform a lifecycle
     * transition.</p>
     *
     * @param executionId execution identifier
     * @param flowId flow identifier
     * @param status persisted flow execution status
     * @param startedAt persisted start timestamp
     * @param completedAt persisted completion timestamp
     * @return rehydrated flow execution
     */
    public static FlowExecution rehydrate(
            final ExecutionId executionId,
            final FlowId flowId,
            final FlowExecutionStatus status,
            final Instant startedAt,
            final Instant completedAt) {

        return new FlowExecution(
                executionId,
                flowId,
                status,
                startedAt,
                completedAt);
    }
}