package io.github.avinashio.ozhuku.domain.execution;

/**
 * Defines the lifecycle state of an Ozhuku pipeline execution.
 */
public enum ExecutionStatus {

    /**
     * Execution has been created but has not started.
     */
    PENDING,

    /**
     * Execution is actively processing.
     */
    RUNNING,

    /**
     * Execution completed successfully.
     */
    COMPLETED,

    /**
     * Execution terminated because of a failure.
     */
    FAILED,

    /**
     * Execution was explicitly cancelled.
     */
    CANCELLED
}