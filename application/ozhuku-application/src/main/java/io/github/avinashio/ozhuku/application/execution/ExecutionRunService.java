package io.github.avinashio.ozhuku.application.execution;

import io.github.avinashio.ozhuku.application.initialization.ExecutionInitializationService;
import io.github.avinashio.ozhuku.application.processing.RecordProcessingRequest;
import io.github.avinashio.ozhuku.application.processing.ResourceTransferRequest;
import io.github.avinashio.ozhuku.domain.execution.ExecutionReference;
import io.github.avinashio.ozhuku.domain.identity.FlowId;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.domain.pipeline.PipelinePlan;
import io.github.avinashio.ozhuku.format.FormatReader;
import io.github.avinashio.ozhuku.format.FormatWriter;
import java.io.IOException;
import java.util.Objects;

public final class ExecutionRunService {

    private final ExecutionInitializationService
            executionInitializationService;

    private final ExecutionProcessingCoordinator
            executionProcessingCoordinator;

    public ExecutionRunService(
            final ExecutionInitializationService executionInitializationService,
            final ExecutionProcessingCoordinator executionProcessingCoordinator) {

        this.executionInitializationService =
                Objects.requireNonNull(
                        executionInitializationService,
                        "executionInitializationService must not be null");

        this.executionProcessingCoordinator =
                Objects.requireNonNull(
                        executionProcessingCoordinator,
                        "executionProcessingCoordinator must not be null");
    }

    public void runResourceTransfer(
            final ExecutionReference executionReference,
            final PipelinePlan pipelinePlan)
            throws IOException {

        Objects.requireNonNull(
                executionReference,
                "executionReference must not be null");

        Objects.requireNonNull(
                pipelinePlan,
                "pipelinePlan must not be null");

        final ResourceTransferRequest request =
                new ResourceTransferRequest(
                        pipelinePlan.source(),
                        pipelinePlan.destination(),
                        pipelinePlan.deliveryPolicy());

        initialize(
                executionReference,
                pipelinePlan.flow().id(),
                request.source().id(),
                request.destination().id());

        executionProcessingCoordinator.processResourceTransfer(
                executionReference,
                pipelinePlan,
                request);
    }

    public void runRecordProcessing(
            final ExecutionReference executionReference,
            final PipelinePlan pipelinePlan,
            final FormatReader formatReader,
            final FormatWriter formatWriter)
            throws IOException {

        Objects.requireNonNull(
                executionReference,
                "executionReference must not be null");

        Objects.requireNonNull(
                pipelinePlan,
                "pipelinePlan must not be null");

        Objects.requireNonNull(
                formatReader,
                "formatReader must not be null");

        Objects.requireNonNull(
                formatWriter,
                "formatWriter must not be null");

        final RecordProcessingRequest request =
                new RecordProcessingRequest(
                        pipelinePlan.source(),
                        pipelinePlan.destination(),
                        pipelinePlan.deliveryPolicy(),
                        formatReader,
                        formatWriter);

        initialize(
                executionReference,
                pipelinePlan.flow().id(),
                request.source().id(),
                request.destination().id());

        executionProcessingCoordinator.processRecordProcessing(
                executionReference,
                pipelinePlan,
                request);
    }

    private void initialize(
            final ExecutionReference executionReference,
            final FlowId flowId,
            final ResourceId sourceResourceId,
            final ResourceId destinationResourceId) {

        executionInitializationService.initialize(
                executionReference,
                flowId,
                sourceResourceId,
                destinationResourceId);
    }
}
