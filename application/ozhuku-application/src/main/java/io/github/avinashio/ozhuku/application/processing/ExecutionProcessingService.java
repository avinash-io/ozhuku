package io.github.avinashio.ozhuku.application.processing;

import io.github.avinashio.ozhuku.application.record.RecordProcessingService;
import io.github.avinashio.ozhuku.application.transfer.ResourceTransferService;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import java.io.IOException;
import java.util.Objects;

public final class ExecutionProcessingService {

    private final ResourceTransferService resourceTransferService;
    private final RecordProcessingService recordProcessingService;

    public ExecutionProcessingService(
            final ResourceTransferService resourceTransferService,
            final RecordProcessingService recordProcessingService) {

        this.resourceTransferService = Objects.requireNonNull(
                resourceTransferService,
                "resourceTransferService must not be null");

        this.recordProcessingService = Objects.requireNonNull(
                recordProcessingService,
                "recordProcessingService must not be null");
    }

    public void processResourceTransfer(
            final ExecutionId executionId,
            final ResourceTransferRequest request)
            throws IOException {

        Objects.requireNonNull(
                executionId,
                "executionId must not be null");

        Objects.requireNonNull(
                request,
                "request must not be null");

        resourceTransferService.transfer(
                request.source(),
                request.destination(),
                request.deliveryPolicy());
    }

    public void processRecordProcessing(
            final ExecutionId executionId,
            final RecordProcessingRequest request)
            throws IOException {

        Objects.requireNonNull(
                executionId,
                "executionId must not be null");

        Objects.requireNonNull(
                request,
                "request must not be null");

        recordProcessingService.process(
                request.source(),
                request.destination(),
                request.deliveryPolicy(),
                request.formatReader(),
                request.formatWriter());
    }
}