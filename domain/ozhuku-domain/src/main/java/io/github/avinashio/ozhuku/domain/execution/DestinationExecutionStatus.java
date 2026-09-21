package io.github.avinashio.ozhuku.domain.execution;

/**
 * Defines the lifecycle state of an individual Ozhuku destination execution.
 */
public enum DestinationExecutionStatus {

    /**
     * Destination execution has been created but has not started.
     */
    PENDING,

    /**
     * Destination execution is actively processing.
     */
    RUNNING,

    /**
     * Destination execution completed successfully.
     */
    COMPLETED,

    /**
     * Destination execution terminated because of a failure.
     */
    FAILED,

    /**
     * Destination execution was explicitly cancelled.
     */
    CANCELLED
}