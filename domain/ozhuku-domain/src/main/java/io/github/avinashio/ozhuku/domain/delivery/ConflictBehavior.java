package io.github.avinashio.ozhuku.domain.delivery;

/**
 * Defines the behavior to apply when a destination resource already exists.
 */
public enum ConflictBehavior {

    /**
     * Fail the delivery when the destination already exists.
     */
    FAIL,

    /**
     * Replace the existing destination resource.
     */
    REPLACE,

    /**
     * Skip delivery when the destination already exists.
     */
    SKIP,

    /**
     * Create a versioned destination resource.
     */
    VERSION
}