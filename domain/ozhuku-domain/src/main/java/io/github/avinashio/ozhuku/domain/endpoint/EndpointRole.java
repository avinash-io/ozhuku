package io.github.avinashio.ozhuku.domain.endpoint;

/**
 * Defines how an endpoint participates in a flow.
 */
public enum EndpointRole {

    /**
     * Endpoint from which a resource is acquired.
     */
    SOURCE,

    /**
     * Endpoint to which a resource is delivered.
     */
    DESTINATION
}