package io.github.avinashio.ozhuku.application.execution;

import io.github.avinashio.ozhuku.application.orchestration.ExecutionOrchestrationService;
import io.github.avinashio.ozhuku.application.processing.ExecutionProcessingService;
import io.github.avinashio.ozhuku.application.processing.RecordProcessingRequest;
import io.github.avinashio.ozhuku.application.processing.ResourceTransferRequest;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.FlowId;
import java.io.IOException;
import java.util.Objects;

public final class ExecutionProcessingCoordinator {

    private final ExecutionOrchestrationService executionOrchestrationService;
    private final ExecutionProcessingService executionProcessingService;

    public ExecutionProcessingCoordinator(
            final ExecutionOrchestrationService executionOrchestrationService,
            final ExecutionProcessingService executionProcessingService) {

        this.executionOrchestrationService =
                Objects.requireNonNull(
                        executionOrchestrationService,
                        "executionOrchestrationService must not be null");

        this.executionProcessingService =
                Objects.requireNonNull(
                        executionProcessingService,
                        "executionProcessingService must not be null");
    }

    public void processResourceTransfer(
            final ExecutionId executionId,
            final FlowId flowId,
            final ResourceTransferRequest request)
            throws IOException {

        Objects.requireNonNull(
                executionId,
                "executionId must not be null");

        Objects.requireNonNull(
                flowId,
                "flowId must not be null");

        Objects.requireNonNull(
                request,
                "request must not be null");

        executionOrchestrationService.startExecution(
                executionId,
                flowId);

        try {
            executionProcessingService.processResourceTransfer(
                    executionId,
                    request);

            executionOrchestrationService.completeExecution(
                    executionId,
                    flowId);
        } catch (IOException exception) {
            failExecution(
                    executionId,
                    flowId,
                    exception);
            throw exception;
        } catch (RuntimeException exception) {
            failExecution(
                    executionId,
                    flowId,
                    exception);
            throw exception;
        }
    }

    public void processRecordProcessing(
            final ExecutionId executionId,
            final FlowId flowId,
            final RecordProcessingRequest request)
            throws IOException {

        Objects.requireNonNull(
                executionId,
                "executionId must not be null");

        Objects.requireNonNull(
                flowId,
                "flowId must not be null");

        Objects.requireNonNull(
                request,
                "request must not be null");

        executionOrchestrationService.startExecution(
                executionId,
                flowId);

        try {
            executionProcessingService.processRecordProcessing(
                    executionId,
                    request);

            executionOrchestrationService.completeExecution(
                    executionId,
                    flowId);
        } catch (IOException exception) {
            failExecution(
                    executionId,
                    flowId,
                    exception);
            throw exception;
        } catch (RuntimeException exception) {
            failExecution(
                    executionId,
                    flowId,
                    exception);
            throw exception;
        }
    }

    private void failExecution(
            final ExecutionId executionId,
            final FlowId flowId,
            final IOException originalException) {

        try {
            executionOrchestrationService.failExecution(
                    executionId,
                    flowId);
        } catch (RuntimeException failureException) {
            originalException.addSuppressed(
                    failureException);
        }
    }

    private void failExecution(
            final ExecutionId executionId,
            final FlowId flowId,
            final RuntimeException originalException) {

        try {
            executionOrchestrationService.failExecution(
                    executionId,
                    flowId);
        } catch (RuntimeException failureException) {
            originalException.addSuppressed(
                    failureException);
        }
    }
}