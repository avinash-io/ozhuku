package io.github.avinashio.ozhuku.domain.deduplication;

/**
 * Defines how Ozhuku handles a source resource whose processing identity
 * has already been encountered.
 */
public enum DuplicatePolicy {

    /**
     * Skip processing when the processing identity has already been
     * successfully processed.
     */
    SKIP_IF_PROCESSED,

    /**
     * Reprocess the source when its relevant source state has changed.
     */
    REPROCESS_IF_CHANGED,

    /**
     * Process the source regardless of whether its processing identity
     * has already been processed.
     */
    ALWAYS_PROCESS,

    /**
     * Fail the execution when the processing identity has already been
     * processed.
     */
    FAIL_IF_DUPLICATE
}