package io.github.avinashio.ozhuku.domain.deduplication;

/**
 * Represents the durable processing state associated with a processing
 * identity.
 */
public enum ProcessingStatus {

    /**
     * Processing has not completed successfully.
     */
    NOT_PROCESSED,

    /**
     * Processing completed successfully.
     */
    PROCESSED
}