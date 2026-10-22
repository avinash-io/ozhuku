package io.github.avinashio.ozhuku.application.execution;

import io.github.avinashio.ozhuku.application.initialization.ExecutionInitializationService;
import io.github.avinashio.ozhuku.application.processing.RecordProcessingRequest;
import io.github.avinashio.ozhuku.application.processing.ResourceTransferRequest;
import io.github.avinashio.ozhuku.application.source.SourceIdentityService;
import io.github.avinashio.ozhuku.domain.execution.ExecutionReference;
import io.github.avinashio.ozhuku.domain.identity.FlowId;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.domain.identity.SourceIdentity;
import io.github.avinashio.ozhuku.domain.lease.Lease;
import io.github.avinashio.ozhuku.domain.lease.LeaseId;
import io.github.avinashio.ozhuku.domain.pipeline.PipelinePlan;
import io.github.avinashio.ozhuku.format.FormatReader;
import io.github.avinashio.ozhuku.format.FormatWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public final class ExecutionRunService {

    private final ExecutionInitializationService
            executionInitializationService;

    private final ExecutionProcessingCoordinator
            executionProcessingCoordinator;

    private final SourceIdentityService sourceIdentityService;

    private final Optional<LeaseAcquisitionService> leaseAcquisitionService;

    public ExecutionRunService(
            final ExecutionInitializationService executionInitializationService,
            final ExecutionProcessingCoordinator executionProcessingCoordinator,
            final SourceIdentityService sourceIdentityService,
            final Optional<LeaseAcquisitionService> leaseAcquisitionService) {

        this.executionInitializationService =
                Objects.requireNonNull(
                        executionInitializationService,
                        "executionInitializationService must not be null");

        this.executionProcessingCoordinator =
                Objects.requireNonNull(
                        executionProcessingCoordinator,
                        "executionProcessingCoordinator must not be null");

        this.sourceIdentityService =
                Objects.requireNonNull(
                        sourceIdentityService,
                        "sourceIdentityService must not be null");

        this.leaseAcquisitionService =
                Objects.requireNonNull(
                        leaseAcquisitionService,
                        "leaseAcquisitionService must not be null");
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

        final LeaseId leaseId =
                withLease(
                        executionReference,
                        pipelinePlan,
                        request.source().id(),
                        () -> executionProcessingCoordinator
                                .processResourceTransfer(
                                        executionReference,
                                        pipelinePlan,
                                        request));
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

        withLease(
                executionReference,
                pipelinePlan,
                request.source().id(),
                () -> executionProcessingCoordinator
                        .processRecordProcessing(
                                executionReference,
                                pipelinePlan,
                                request));
    }

    public void runResourceProcessing(
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

        withLease(
                executionReference,
                pipelinePlan,
                request.source().id(),
                () -> executionProcessingCoordinator
                        .processResourceProcessing(
                                executionReference,
                                pipelinePlan,
                                request));
    }

    private LeaseId withLease(
            final ExecutionReference executionReference,
            final PipelinePlan pipelinePlan,
            final ResourceId sourceResourceId,
            final ThrowingAction action)
            throws IOException {

        if (leaseAcquisitionService.isEmpty()) {
            action.run();
            return null;
        }

        final LeaseAcquisitionService service =
                leaseAcquisitionService.orElseThrow();

        final SourceIdentity sourceIdentity =
                sourceIdentityService.identify(
                        pipelinePlan.source());

        final Optional<Lease> lease =
                service.acquire(
                        executionReference.executionId(),
                        sourceIdentity);

        if (lease.isEmpty()) {
            throw new IllegalStateException(
                    "Execution lease could not be acquired for execution "
                            + executionReference.executionId()
                            + "; another worker may be processing it");
        }

        final Lease claimedLease = lease.orElseThrow();
        final LeaseId leaseId = claimedLease.leaseId();

        try {
            action.run();
            service.release(
                    leaseId,
                    claimedLease.fencingToken());
            return leaseId;
        } catch (RuntimeException exception) {
            service.release(
                    leaseId,
                    claimedLease.fencingToken());
            throw exception;
        }
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

    /**
     * Functional interface for an action that can throw IOException.
     */
    @FunctionalInterface
    private interface ThrowingAction {
        void run() throws IOException;
    }
}
