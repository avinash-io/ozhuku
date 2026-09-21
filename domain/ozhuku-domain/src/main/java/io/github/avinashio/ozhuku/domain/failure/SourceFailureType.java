package io.github.avinashio.ozhuku.domain.failure;

/**
 * Defines source-side failure categories used by Ozhuku execution and
 * recovery logic.
 */
public enum SourceFailureType {

    /**
     * The source cannot currently be reached or accessed.
     */
    SOURCE_UNAVAILABLE,

    /**
     * The source changed while it was being processed.
     */
    SOURCE_CHANGED,

    /**
     * The source resource is not yet complete or ready for processing.
     */
    SOURCE_INCOMPLETE,

    /**
     * The source resource is currently locked by another operation.
     */
    SOURCE_LOCKED
}