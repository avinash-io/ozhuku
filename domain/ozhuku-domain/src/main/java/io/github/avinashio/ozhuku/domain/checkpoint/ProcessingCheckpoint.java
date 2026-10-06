package io.github.avinashio.ozhuku.domain.checkpoint;

import io.github.avinashio.ozhuku.domain.execution.SourceExecutionReference;
import io.github.avinashio.ozhuku.foundation.validation.Validation;
import java.time.Instant;
import java.util.Objects;

/**
 * Represents a durable logical processing checkpoint for a source execution.
 *
 * <p>The checkpoint records the last successfully processed logical record
 * sequence. It is separate from source execution lifecycle state and
 * destination commit state.</p>
 */
public final class ProcessingCheckpoint {

    private final SourceExecutionReference sourceExecutionReference;
    private final long recordSequence;
    private final Instant checkpointedAt;

    /**
     * Creates a processing checkpoint.
     *
     * @param sourceExecutionReference source execution reference
     * @param recordSequence last successfully processed record sequence
     * @param checkpointedAt checkpoint persistence timestamp
     */
    public ProcessingCheckpoint(
            final SourceExecutionReference sourceExecutionReference,
            final long recordSequence,
            final Instant checkpointedAt) {

        this.sourceExecutionReference = Validation.requireNonNull(
                sourceExecutionReference,
                "Source execution reference must not be null");

        if (recordSequence < 0) {
            throw new IllegalArgumentException(
                    "Record sequence must not be negative");
        }

        this.recordSequence = recordSequence;

        this.checkpointedAt = Validation.requireNonNull(
                checkpointedAt,
                "Checkpoint timestamp must not be null");
    }

    /**
     * Returns the source execution reference.
     *
     * @return source execution reference
     */
    public SourceExecutionReference sourceExecutionReference() {
        return sourceExecutionReference;
    }

    /**
     * Returns the last successfully processed record sequence.
     *
     * @return record sequence
     */
    public long recordSequence() {
        return recordSequence;
    }

    /**
     * Returns the checkpoint persistence timestamp.
     *
     * @return checkpoint timestamp
     */
    public Instant checkpointedAt() {
        return checkpointedAt;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof ProcessingCheckpoint)) {
            return false;
        }

        final ProcessingCheckpoint that =
                (ProcessingCheckpoint) other;

        return recordSequence == that.recordSequence
                && sourceExecutionReference.equals(
                that.sourceExecutionReference)
                && checkpointedAt.equals(
                that.checkpointedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                sourceExecutionReference,
                recordSequence,
                checkpointedAt);
    }

    @Override
    public String toString() {
        return "ProcessingCheckpoint{"
                + "sourceExecutionReference="
                + sourceExecutionReference
                + ", recordSequence="
                + recordSequence
                + ", checkpointedAt="
                + checkpointedAt
                + '}';
    }
}
