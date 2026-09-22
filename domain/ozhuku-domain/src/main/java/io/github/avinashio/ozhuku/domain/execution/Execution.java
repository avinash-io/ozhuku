package io.github.avinashio.ozhuku.domain.execution;

import io.github.avinashio.ozhuku.foundation.validation.Validation;
import java.time.Instant;
import java.util.Objects;

/**
 * Represents a concrete execution of a versioned Ozhuku pipeline.
 *
 * <p>The execution contains domain state only. Persistence, scheduling,
 * recovery orchestration, and infrastructure concerns belong outside the
 * domain model.</p>
 */
public final class Execution {

    private final ExecutionReference reference;
    private final ExecutionStatus status;
    private final Instant startedAt;
    private final Instant completedAt;

    /**
     * Creates a pending execution.
     *
     * @param reference execution identity and pipeline version reference
     */
    public Execution(final ExecutionReference reference) {
        this(reference, ExecutionStatus.PENDING, null, null);
    }

    private Execution(
            final ExecutionReference reference,
            final ExecutionStatus status,
            final Instant startedAt,
            final Instant completedAt) {

        this.reference = Validation.requireNonNull(
                reference,
                "Execution reference must not be null");
        this.status = Validation.requireNonNull(
                status,
                "Execution status must not be null");

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
     * Returns the execution reference.
     *
     * @return execution reference
     */
    public ExecutionReference reference() {
        return reference;
    }

    /**
     * Returns the execution status.
     *
     * @return execution status
     */
    public ExecutionStatus status() {
        return status;
    }

    /**
     * Returns the execution start timestamp.
     *
     * @return start timestamp, or {@code null} if execution has not started
     */
    public Instant startedAt() {
        return startedAt;
    }

    /**
     * Returns the execution completion timestamp.
     *
     * @return completion timestamp, or {@code null} if execution is not
     * complete
     */
    public Instant completedAt() {
        return completedAt;
    }

    /**
     * Creates a running copy of this execution.
     *
     * @param startedAt execution start timestamp
     * @return running execution
     */
    public Execution start(final Instant startedAt) {
        Validation.requireNonNull(
                startedAt,
                "Execution start timestamp must not be null");

        if (status != ExecutionStatus.PENDING) {
            throw new IllegalStateException(
                    "Only pending executions can be started");
        }

        return new Execution(
                reference,
                ExecutionStatus.RUNNING,
                startedAt,
                null);
    }

    /**
     * Creates a completed copy of this execution.
     *
     * @param completedAt execution completion timestamp
     * @return completed execution
     */
    public Execution complete(final Instant completedAt) {
        Validation.requireNonNull(
                completedAt,
                "Execution completion timestamp must not be null");

        if (status != ExecutionStatus.RUNNING) {
            throw new IllegalStateException(
                    "Only running executions can be completed");
        }

        return new Execution(
                reference,
                ExecutionStatus.COMPLETED,
                startedAt,
                completedAt);
    }

    /**
     * Creates a failed copy of this execution.
     *
     * @param completedAt failure timestamp
     * @return failed execution
     */
    public Execution fail(final Instant completedAt) {
        Validation.requireNonNull(
                completedAt,
                "Execution failure timestamp must not be null");

        if (status != ExecutionStatus.RUNNING) {
            throw new IllegalStateException(
                    "Only running executions can fail");
        }

        return new Execution(
                reference,
                ExecutionStatus.FAILED,
                startedAt,
                completedAt);
    }

    /**
     * Creates a cancelled copy of this execution.
     *
     * @param completedAt cancellation timestamp
     * @return cancelled execution
     */
    public Execution cancel(final Instant completedAt) {
        Validation.requireNonNull(
                completedAt,
                "Execution cancellation timestamp must not be null");

        if (status != ExecutionStatus.RUNNING
                && status != ExecutionStatus.PENDING) {
            throw new IllegalStateException(
                    "Only pending or running executions can be cancelled");
        }

        return new Execution(
                reference,
                ExecutionStatus.CANCELLED,
                startedAt,
                completedAt);
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof Execution)) {
            return false;
        }

        final Execution that = (Execution) other;
        return reference.equals(that.reference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reference);
    }

    @Override
    public String toString() {
        return "Execution{"
                + "reference=" + reference
                + ", status=" + status
                + ", startedAt=" + startedAt
                + ", completedAt=" + completedAt
                + '}';
    }

    /**
     * Rehydrates an execution from durable persistence.
     *
     * <p>This method is intended for persistence adapters reconstructing
     * previously persisted domain state. It does not perform a lifecycle
     * transition.</p>
     *
     * @param reference execution reference
     * @param status persisted execution status
     * @param startedAt persisted start timestamp
     * @param completedAt persisted completion timestamp
     * @return rehydrated execution
     */
    public static Execution rehydrate(
            final ExecutionReference reference,
            final ExecutionStatus status,
            final Instant startedAt,
            final Instant completedAt) {

        return new Execution(
                reference,
                status,
                startedAt,
                completedAt);
    }
}