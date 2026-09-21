package io.github.avinashio.ozhuku.domain.execution;

import io.github.avinashio.ozhuku.foundation.validation.Validation;
import java.time.Instant;
import java.util.Objects;

/**
 * Represents execution state for a destination resource within an Ozhuku
 * execution.
 *
 * <p>The destination execution contains lifecycle state only. Delivery,
 * commit, retry orchestration, persistence, and destination-specific
 * behavior belong outside the domain model.</p>
 */
public final class DestinationExecution {

    private final DestinationExecutionReference reference;
    private final DestinationExecutionStatus status;
    private final Instant startedAt;
    private final Instant completedAt;

    /**
     * Creates a pending destination execution.
     *
     * @param reference destination execution identity
     */
    public DestinationExecution(
            final DestinationExecutionReference reference) {

        this(
                reference,
                DestinationExecutionStatus.PENDING,
                null,
                null);
    }

    private DestinationExecution(
            final DestinationExecutionReference reference,
            final DestinationExecutionStatus status,
            final Instant startedAt,
            final Instant completedAt) {

        this.reference = Validation.requireNonNull(
                reference,
                "Destination execution reference must not be null");
        this.status = Validation.requireNonNull(
                status,
                "Destination execution status must not be null");

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
     * Returns the destination execution reference.
     *
     * @return destination execution reference
     */
    public DestinationExecutionReference reference() {
        return reference;
    }

    /**
     * Returns the current destination execution status.
     *
     * @return destination execution status
     */
    public DestinationExecutionStatus status() {
        return status;
    }

    /**
     * Returns the destination execution start timestamp.
     *
     * @return start timestamp, or {@code null} if not started
     */
    public Instant startedAt() {
        return startedAt;
    }

    /**
     * Returns the destination execution completion timestamp.
     *
     * @return completion timestamp, or {@code null} if not complete
     */
    public Instant completedAt() {
        return completedAt;
    }

    /**
     * Creates a running copy of this destination execution.
     *
     * @param startedAt destination execution start timestamp
     * @return running destination execution
     */
    public DestinationExecution start(final Instant startedAt) {
        Validation.requireNonNull(
                startedAt,
                "Destination execution start timestamp must not be null");

        if (status != DestinationExecutionStatus.PENDING) {
            throw new IllegalStateException(
                    "Only pending destination executions can be started");
        }

        return new DestinationExecution(
                reference,
                DestinationExecutionStatus.RUNNING,
                startedAt,
                null);
    }

    /**
     * Creates a completed copy of this destination execution.
     *
     * @param completedAt destination execution completion timestamp
     * @return completed destination execution
     */
    public DestinationExecution complete(final Instant completedAt) {
        Validation.requireNonNull(
                completedAt,
                "Destination execution completion timestamp must not be null");

        if (status != DestinationExecutionStatus.RUNNING) {
            throw new IllegalStateException(
                    "Only running destination executions can be completed");
        }

        return new DestinationExecution(
                reference,
                DestinationExecutionStatus.COMPLETED,
                startedAt,
                completedAt);
    }

    /**
     * Creates a failed copy of this destination execution.
     *
     * @param completedAt destination failure timestamp
     * @return failed destination execution
     */
    public DestinationExecution fail(final Instant completedAt) {
        Validation.requireNonNull(
                completedAt,
                "Destination execution failure timestamp must not be null");

        if (status != DestinationExecutionStatus.RUNNING) {
            throw new IllegalStateException(
                    "Only running destination executions can fail");
        }

        return new DestinationExecution(
                reference,
                DestinationExecutionStatus.FAILED,
                startedAt,
                completedAt);
    }

    /**
     * Creates a cancelled copy of this destination execution.
     *
     * @param completedAt cancellation timestamp
     * @return cancelled destination execution
     */
    public DestinationExecution cancel(final Instant completedAt) {
        Validation.requireNonNull(
                completedAt,
                "Destination execution cancellation timestamp must not be null");

        if (status != DestinationExecutionStatus.PENDING
                && status != DestinationExecutionStatus.RUNNING) {
            throw new IllegalStateException(
                    "Only pending or running destination executions can be cancelled");
        }

        return new DestinationExecution(
                reference,
                DestinationExecutionStatus.CANCELLED,
                startedAt,
                completedAt);
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof DestinationExecution)) {
            return false;
        }

        final DestinationExecution that =
                (DestinationExecution) other;

        return reference.equals(that.reference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reference);
    }

    @Override
    public String toString() {
        return "DestinationExecution{"
                + "reference=" + reference
                + ", status=" + status
                + ", startedAt=" + startedAt
                + ", completedAt=" + completedAt
                + '}';
    }
}