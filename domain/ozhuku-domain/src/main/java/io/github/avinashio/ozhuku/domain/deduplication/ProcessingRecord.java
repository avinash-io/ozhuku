package io.github.avinashio.ozhuku.domain.deduplication;

import io.github.avinashio.ozhuku.domain.identity.ProcessingIdentity;
import io.github.avinashio.ozhuku.domain.identity.SourceFingerprint;
import io.github.avinashio.ozhuku.foundation.validation.Validation;
import java.time.Instant;
import java.util.Objects;

/**
 * Immutable record of the durable processing state for a processing identity.
 *
 * <p>The record stores the source fingerprint observed when processing
 * completed successfully. This allows duplicate handling policies to
 * distinguish an unchanged source from a changed source.</p>
 */
public final class ProcessingRecord {

    private final ProcessingIdentity identity;
    private final ProcessingStatus status;
    private final SourceFingerprint sourceFingerprint;
    private final Instant processedAt;

    /**
     * Creates a processing record.
     *
     * @param identity processing identity
     * @param status processing status
     * @param sourceFingerprint observed source fingerprint
     * @param processedAt time at which successful processing completed
     */
    public ProcessingRecord(
            final ProcessingIdentity identity,
            final ProcessingStatus status,
            final SourceFingerprint sourceFingerprint,
            final Instant processedAt) {

        this.identity = Validation.requireNonNull(
                identity,
                "Processing identity must not be null");
        this.status = Validation.requireNonNull(
                status,
                "Processing status must not be null");

        if (status == ProcessingStatus.PROCESSED
                && sourceFingerprint == null) {
            throw new IllegalArgumentException(
                    "Source fingerprint is required for processed status");
        }

        if (status == ProcessingStatus.PROCESSED
                && processedAt == null) {
            throw new IllegalArgumentException(
                    "Processed timestamp is required for processed status");
        }

        if (status == ProcessingStatus.NOT_PROCESSED
                && sourceFingerprint != null) {
            throw new IllegalArgumentException(
                    "Source fingerprint must be null for not processed status");
        }

        if (status == ProcessingStatus.NOT_PROCESSED
                && processedAt != null) {
            throw new IllegalArgumentException(
                    "Processed timestamp must be null for not processed status");
        }

        this.sourceFingerprint = sourceFingerprint;
        this.processedAt = processedAt;
    }

    /**
     * Creates a record representing a source that has not been processed.
     *
     * @param identity processing identity
     * @return not processed record
     */
    public static ProcessingRecord notProcessed(
            final ProcessingIdentity identity) {

        return new ProcessingRecord(
                identity,
                ProcessingStatus.NOT_PROCESSED,
                null,
                null);
    }

    /**
     * Creates a record representing successful processing.
     *
     * @param identity processing identity
     * @param sourceFingerprint source state observed during processing
     * @param processedAt successful processing completion time
     * @return processed record
     */
    public static ProcessingRecord processed(
            final ProcessingIdentity identity,
            final SourceFingerprint sourceFingerprint,
            final Instant processedAt) {

        return new ProcessingRecord(
                identity,
                ProcessingStatus.PROCESSED,
                sourceFingerprint,
                processedAt);
    }

    /**
     * Returns the processing identity.
     *
     * @return processing identity
     */
    public ProcessingIdentity identity() {
        return identity;
    }

    /**
     * Returns the processing status.
     *
     * @return processing status
     */
    public ProcessingStatus status() {
        return status;
    }

    /**
     * Returns the source fingerprint recorded for successful processing.
     *
     * @return source fingerprint, or null when not processed
     */
    public SourceFingerprint sourceFingerprint() {
        return sourceFingerprint;
    }

    /**
     * Returns the successful processing completion time.
     *
     * @return processing completion time, or null when not processed
     */
    public Instant processedAt() {
        return processedAt;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof ProcessingRecord)) {
            return false;
        }

        final ProcessingRecord that = (ProcessingRecord) other;

        return identity.equals(that.identity)
                && status == that.status
                && Objects.equals(
                sourceFingerprint,
                that.sourceFingerprint)
                && Objects.equals(processedAt, that.processedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                identity,
                status,
                sourceFingerprint,
                processedAt);
    }
}