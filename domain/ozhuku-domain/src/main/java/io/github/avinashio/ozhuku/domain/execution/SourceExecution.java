package io.github.avinashio.ozhuku.domain.execution;

import io.github.avinashio.ozhuku.foundation.validation.Validation;
import java.time.Instant;
import java.util.Objects;

/**
 * Represents execution state for a source resource within an Ozhuku
 * execution.
 *
 * <p>The source execution contains lifecycle state only. Source access,
 * checkpoint persistence, retry orchestration, and storage-specific behavior
 * belong outside the domain model.</p>
 */
public final class SourceExecution {

    private final SourceExecutionReference reference;
    private final SourceExecutionStatus status;
    private final Instant startedAt;
    private final Instant completedAt;

    /**
     * Creates a pending source execution.
     *
     * @param reference source execution identity
     */
    public SourceExecution(final SourceExecutionReference reference) {
        this(
                reference,
                SourceExecutionStatus.PENDING,
                null,
                null);
    }

    private SourceExecution(
            final SourceExecutionReference reference,
            final SourceExecutionStatus status,
            final Instant startedAt,
            final Instant completedAt) {

        this.reference = Validation.requireNonNull(
                reference,
                "Source execution reference must not be null");
        this.status = Validation.requireNonNull(
                status,
                "Source execution status must not be null");

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
     * Returns the source execution reference.
     *
     * @return source execution reference
     */
    public SourceExecutionReference reference() {
        return reference;
    }

    /**
     * Returns the current source execution status.
     *
     * @return source execution status
     */
    public SourceExecutionStatus status() {
        return status;
    }

    /**
     * Returns the source execution start timestamp.
     *
     * @return start timestamp, or {@code null} if not started
     */
    public Instant startedAt() {
        return startedAt;
    }

    /**
     * Returns the source execution completion timestamp.
     *
     * @return completion timestamp, or {@code null} if not complete
     */
    public Instant completedAt() {
        return completedAt;
    }

    /**
     * Creates a running copy of this source execution.
     *
     * @param startedAt source execution start timestamp
     * @return running source execution
     */
    public SourceExecution start(final Instant startedAt) {
        Validation.requireNonNull(
                startedAt,
                "Source execution start timestamp must not be null");

        if (status != SourceExecutionStatus.PENDING) {
            throw new IllegalStateException(
                    "Only pending source executions can be started");
        }

        return new SourceExecution(
                reference,
                SourceExecutionStatus.RUNNING,
                startedAt,
                null);
    }

    /**
     * Creates a completed copy of this source execution.
     *
     * @param completedAt source execution completion timestamp
     * @return completed source execution
     */
    public SourceExecution complete(final Instant completedAt) {
        Validation.requireNonNull(
                completedAt,
                "Source execution completion timestamp must not be null");

        if (status != SourceExecutionStatus.RUNNING) {
            throw new IllegalStateException(
                    "Only running source executions can be completed");
        }

        return new SourceExecution(
                reference,
                SourceExecutionStatus.COMPLETED,
                startedAt,
                completedAt);
    }

    /**
     * Creates a failed copy of this source execution.
     *
     * @param completedAt source failure timestamp
     * @return failed source execution
     */
    public SourceExecution fail(final Instant completedAt) {
        Validation.requireNonNull(
                completedAt,
                "Source execution failure timestamp must not be null");

        if (status != SourceExecutionStatus.RUNNING) {
            throw new IllegalStateException(
                    "Only running source executions can fail");
        }

        return new SourceExecution(
                reference,
                SourceExecutionStatus.FAILED,
                startedAt,
                completedAt);
    }

    /**
     * Creates a cancelled copy of this source execution.
     *
     * @param completedAt cancellation timestamp
     * @return cancelled source execution
     */
    public SourceExecution cancel(final Instant completedAt) {
        Validation.requireNonNull(
                completedAt,
                "Source execution cancellation timestamp must not be null");

        if (status != SourceExecutionStatus.PENDING
                && status != SourceExecutionStatus.RUNNING) {
            throw new IllegalStateException(
                    "Only pending or running source executions can be cancelled");
        }

        return new SourceExecution(
                reference,
                SourceExecutionStatus.CANCELLED,
                startedAt,
                completedAt);
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof SourceExecution)) {
            return false;
        }

        final SourceExecution that = (SourceExecution) other;
        return reference.equals(that.reference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reference);
    }

    @Override
    public String toString() {
        return "SourceExecution{"
                + "reference=" + reference
                + ", status=" + status
                + ", startedAt=" + startedAt
                + ", completedAt=" + completedAt
                + '}';
    }

    public static SourceExecution rehydrate(
            final SourceExecutionReference reference,
            final SourceExecutionStatus status,
            final Instant startedAt,
            final Instant completedAt) {

        return new SourceExecution(
                reference,
                status,
                startedAt,
                completedAt);
    }
}