package io.github.avinashio.ozhuku.domain.execution;

/**
 * Defines the durable commit state of a destination execution.
 *
 * <p>Commit state is distinct from execution state. In particular, an
 * unknown commit outcome must not be interpreted as a failed commit because
 * the destination may already have accepted the delivery.</p>
 */
public enum CommitStatus {

    /**
     * Durable destination acceptance has not been established.
     */
    NOT_COMMITTED,

    /**
     * Durable destination acceptance has been established.
     */
    COMMITTED,

    /**
     * The destination outcome cannot currently be established.
     */
    UNKNOWN
}