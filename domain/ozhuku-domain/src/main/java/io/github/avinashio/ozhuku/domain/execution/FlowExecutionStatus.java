package io.github.avinashio.ozhuku.domain.execution;

/**
 * Defines the lifecycle state of an individual Ozhuku flow execution.
 */
public enum FlowExecutionStatus {

    /**
     * Flow execution has been created but has not started.
     */
    PENDING,

    /**
     * Flow execution is actively processing.
     */
    RUNNING,

    /**
     * Flow execution completed successfully.
     */
    COMPLETED,

    /**
     * Flow execution terminated because of a failure.
     */
    FAILED,

    /**
     * Flow execution was explicitly cancelled.
     */
    CANCELLED
}