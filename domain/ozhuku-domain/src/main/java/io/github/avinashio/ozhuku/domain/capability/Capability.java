package io.github.avinashio.ozhuku.domain.capability;

/**
 * Operation capabilities that a source or destination may support.
 */
public enum Capability {

    /**
     * Allows creation of a new resource.
     */
    CREATE,

    /**
     * Allows replacement of an existing resource.
     */
    REPLACE,

    /**
     * Allows appending data to an existing resource.
     */
    APPEND,

    /**
     * Allows deletion of a resource.
     */
    DELETE,

    /**
     * Allows creation of a versioned resource.
     */
    VERSION
}