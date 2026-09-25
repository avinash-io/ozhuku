package io.github.avinashio.ozhuku.application.orchestration;

import io.github.avinashio.ozhuku.application.execution.DestinationExecutionLifecycleService;
import io.github.avinashio.ozhuku.application.execution.ExecutionLifecycleService;
import io.github.avinashio.ozhuku.application.execution.FlowExecutionLifecycleService;
import io.github.avinashio.ozhuku.application.execution.SourceExecutionLifecycleService;
import io.github.avinashio.ozhuku.domain.execution.DestinationExecution;
import io.github.avinashio.ozhuku.domain.execution.FlowExecution;
import io.github.avinashio.ozhuku.domain.execution.SourceExecution;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.FlowId;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import java.util.Objects;

public final class ExecutionOrchestrationService {

    private final ExecutionLifecycleService executionLifecycleService;
    private final FlowExecutionLifecycleService flowExecutionLifecycleService;
    private final SourceExecutionLifecycleService sourceExecutionLifecycleService;
    private final DestinationExecutionLifecycleService
            destinationExecutionLifecycleService;

    public ExecutionOrchestrationService(
            final ExecutionLifecycleService executionLifecycleService,
            final FlowExecutionLifecycleService flowExecutionLifecycleService,
            final SourceExecutionLifecycleService
                    sourceExecutionLifecycleService,
            final DestinationExecutionLifecycleService
                    destinationExecutionLifecycleService) {

        this.executionLifecycleService =
                Objects.requireNonNull(
                        executionLifecycleService,
                        "executionLifecycleService must not be null");

        this.flowExecutionLifecycleService =
                Objects.requireNonNull(
                        flowExecutionLifecycleService,
                        "flowExecutionLifecycleService must not be null");

        this.sourceExecutionLifecycleService =
                Objects.requireNonNull(
                        sourceExecutionLifecycleService,
                        "sourceExecutionLifecycleService must not be null");

        this.destinationExecutionLifecycleService =
                Objects.requireNonNull(
                        destinationExecutionLifecycleService,
                        "destinationExecutionLifecycleService must not be null");
    }

    public DestinationExecution startExecution(
            final ExecutionId executionId,
            final FlowId flowId,
            final ResourceId sourceResourceId,
            final ResourceId destinationResourceId) {

        requireArguments(
                executionId,
                flowId,
                sourceResourceId,
                destinationResourceId);

        executionLifecycleService.start(executionId);

        try {
            flowExecutionLifecycleService.start(
                    executionId,
                    flowId);

            try {
                sourceExecutionLifecycleService.start(
                        executionId,
                        sourceResourceId);

                try {
                    return destinationExecutionLifecycleService.start(
                            executionId,
                            destinationResourceId);
                } catch (RuntimeException exception) {
                    failSourceExecution(
                            executionId,
                            sourceResourceId,
                            exception);
                    failFlowExecution(
                            executionId,
                            flowId,
                            exception);
                    failExecution(
                            executionId,
                            exception);
                    throw exception;
                }
            } catch (RuntimeException exception) {
                failFlowExecution(
                        executionId,
                        flowId,
                        exception);
                failExecution(
                        executionId,
                        exception);
                throw exception;
            }
        } catch (RuntimeException exception) {
            failExecution(
                    executionId,
                    exception);
            throw exception;
        }
    }

    public DestinationExecution completeExecution(
            final ExecutionId executionId,
            final FlowId flowId,
            final ResourceId sourceResourceId,
            final ResourceId destinationResourceId) {

        requireArguments(
                executionId,
                flowId,
                sourceResourceId,
                destinationResourceId);

        final DestinationExecution completedDestination =
                destinationExecutionLifecycleService.complete(
                        executionId,
                        destinationResourceId);

        sourceExecutionLifecycleService.complete(
                executionId,
                sourceResourceId);

        flowExecutionLifecycleService.complete(
                executionId,
                flowId);

        executionLifecycleService.complete(executionId);

        return completedDestination;
    }

    public DestinationExecution failExecution(
            final ExecutionId executionId,
            final FlowId flowId,
            final ResourceId sourceResourceId,
            final ResourceId destinationResourceId) {

        requireArguments(
                executionId,
                flowId,
                sourceResourceId,
                destinationResourceId);

        final DestinationExecution failedDestination =
                destinationExecutionLifecycleService.fail(
                        executionId,
                        destinationResourceId);

        sourceExecutionLifecycleService.fail(
                executionId,
                sourceResourceId);

        flowExecutionLifecycleService.fail(
                executionId,
                flowId);

        executionLifecycleService.fail(executionId);

        return failedDestination;
    }

    public DestinationExecution cancelExecution(
            final ExecutionId executionId,
            final FlowId flowId,
            final ResourceId sourceResourceId,
            final ResourceId destinationResourceId) {

        requireArguments(
                executionId,
                flowId,
                sourceResourceId,
                destinationResourceId);

        final DestinationExecution cancelledDestination =
                destinationExecutionLifecycleService.cancel(
                        executionId,
                        destinationResourceId);

        sourceExecutionLifecycleService.cancel(
                executionId,
                sourceResourceId);

        flowExecutionLifecycleService.cancel(
                executionId,
                flowId);

        executionLifecycleService.cancel(executionId);

        return cancelledDestination;
    }

    private void requireArguments(
            final ExecutionId executionId,
            final FlowId flowId,
            final ResourceId sourceResourceId,
            final ResourceId destinationResourceId) {

        Objects.requireNonNull(
                executionId,
                "executionId must not be null");

        Objects.requireNonNull(
                flowId,
                "flowId must not be null");

        Objects.requireNonNull(
                sourceResourceId,
                "sourceResourceId must not be null");

        Objects.requireNonNull(
                destinationResourceId,
                "destinationResourceId must not be null");
    }

    private void failSourceExecution(
            final ExecutionId executionId,
            final ResourceId sourceResourceId,
            final RuntimeException originalException) {

        try {
            sourceExecutionLifecycleService.fail(
                    executionId,
                    sourceResourceId);
        } catch (RuntimeException failureException) {
            originalException.addSuppressed(failureException);
        }
    }

    private void failFlowExecution(
            final ExecutionId executionId,
            final FlowId flowId,
            final RuntimeException originalException) {

        try {
            flowExecutionLifecycleService.fail(
                    executionId,
                    flowId);
        } catch (RuntimeException failureException) {
            originalException.addSuppressed(failureException);
        }
    }

    private void failExecution(
            final ExecutionId executionId,
            final RuntimeException originalException) {

        try {
            executionLifecycleService.fail(executionId);
        } catch (RuntimeException failureException) {
            originalException.addSuppressed(failureException);
        }
    }
}