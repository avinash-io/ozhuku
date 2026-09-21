package io.github.avinashio.ozhuku.domain.execution;

/**
 * Defines the lifecycle state of an individual Ozhuku source execution.
 */
public enum SourceExecutionStatus {

    /**
     * Source execution has been created but has not started.
     */
    PENDING,

    /**
     * Source execution is actively processing.
     */
    RUNNING,

    /**
     * Source execution completed successfully.
     */
    COMPLETED,

    /**
     * Source execution terminated because of a failure.
     */
    FAILED,

    /**
     * Source execution was explicitly cancelled.
     */
    CANCELLED
}