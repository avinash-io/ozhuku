package io.github.avinashio.ozhuku.domain.lease;

/**
 * Lifecycle state of a durable lease claimed by a worker.
 */
public enum LeaseStatus {

    /**
     * The lease is currently held and valid.
     */
    CLAIMED,

    /**
     * The lease has expired because the holder has not renewed it
     * within the configured lease timeout.
     */
    EXPIRED,

    /**
     * The lease was forcibly taken over by another worker via
     * fencing, superseding the original holder.
     */
    REVOKED
}
