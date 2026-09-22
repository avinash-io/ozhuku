package io.github.avinashio.ozhuku.domain.deduplication;

/**
 * Defines the result of evaluating a duplicate processing policy.
 */
public enum DeduplicationDecision {

    /**
     * Processing should proceed normally.
     */
    PROCESS,

    /**
     * Processing should be skipped because the source was already processed.
     */
    SKIP,

    /**
     * Processing should fail because a duplicate was detected.
     */
    FAIL
}