package io.github.avinashio.ozhuku.domain.execution;

import io.github.avinashio.ozhuku.foundation.validation.Validation;
import java.time.Instant;
import java.util.Objects;

/**
 * Represents the durable commit state associated with a destination
 * execution.
 *
 * <p>Commit state is intentionally separate from destination execution
 * lifecycle. An unknown outcome must remain distinguishable from a confirmed
 * failure so recovery can safely inspect the destination before retrying.</p>
 */
public final class DestinationCommit {

    private final DestinationCommitReference reference;
    private final CommitStatus status;
    private final Instant committedAt;

    /**
     * Creates a destination commit with the supplied status.
     *
     * @param reference commit identity
     * @param status current commit status
     */
    public DestinationCommit(
            final DestinationCommitReference reference,
            final CommitStatus status) {

        this(reference, status, null);
    }

    private DestinationCommit(
            final DestinationCommitReference reference,
            final CommitStatus status,
            final Instant committedAt) {

        this.reference = Validation.requireNonNull(
                reference,
                "Destination commit reference must not be null");
        this.status = Validation.requireNonNull(
                status,
                "Commit status must not be null");

        if (status == CommitStatus.COMMITTED && committedAt == null) {
            throw new IllegalArgumentException(
                    "Committed status requires a commit timestamp");
        }

        if (status != CommitStatus.COMMITTED && committedAt != null) {
            throw new IllegalArgumentException(
                    "Only committed status may have a commit timestamp");
        }

        this.committedAt = committedAt;
    }

    /**
     * Creates a not-committed state.
     *
     * @param reference commit identity
     * @return not-committed state
     */
    public static DestinationCommit notCommitted(
            final DestinationCommitReference reference) {

        return new DestinationCommit(
                reference,
                CommitStatus.NOT_COMMITTED);
    }

    /**
     * Creates an unknown commit state.
     *
     * @param reference commit identity
     * @return unknown commit state
     */
    public static DestinationCommit unknown(
            final DestinationCommitReference reference) {

        return new DestinationCommit(
                reference,
                CommitStatus.UNKNOWN);
    }

    /**
     * Creates a confirmed committed state.
     *
     * @param reference commit identity
     * @param committedAt timestamp at which the commit was confirmed
     * @return committed state
     */
    public static DestinationCommit committed(
            final DestinationCommitReference reference,
            final Instant committedAt) {

        Validation.requireNonNull(
                committedAt,
                "Commit timestamp must not be null");

        return new DestinationCommit(
                reference,
                CommitStatus.COMMITTED,
                committedAt);
    }

    /**
     * Returns the commit reference.
     *
     * @return commit reference
     */
    public DestinationCommitReference reference() {
        return reference;
    }

    /**
     * Returns the current commit status.
     *
     * @return commit status
     */
    public CommitStatus status() {
        return status;
    }

    /**
     * Returns the confirmed commit timestamp.
     *
     * @return commit timestamp, or {@code null} if not committed
     */
    public Instant committedAt() {
        return committedAt;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof DestinationCommit)) {
            return false;
        }

        final DestinationCommit that = (DestinationCommit) other;

        return reference.equals(that.reference)
                && status == that.status
                && Objects.equals(committedAt, that.committedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reference, status, committedAt);
    }

    @Override
    public String toString() {
        return "DestinationCommit{"
                + "reference=" + reference
                + ", status=" + status
                + ", committedAt=" + committedAt
                + '}';
    }
}