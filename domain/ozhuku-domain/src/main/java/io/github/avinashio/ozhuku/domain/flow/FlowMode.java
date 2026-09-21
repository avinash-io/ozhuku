package io.github.avinashio.ozhuku.domain.flow;

/**
 * Defines how an Ozhuku flow processes its input resource.
 */
public enum FlowMode {

    /**
     * Transfers an opaque resource without interpreting its contents.
     */
    RESOURCE_TRANSFER,

    /**
     * Parses and processes logical records from a resource.
     */
    RECORD_PROCESSING,

    /**
     * Processes a resource as a whole without business-record interpretation.
     */
    RESOURCE_PROCESSING
}