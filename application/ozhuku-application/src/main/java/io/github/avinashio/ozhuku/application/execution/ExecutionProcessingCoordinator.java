
package io.github.avinashio.ozhuku.application.execution;

import io.github.avinashio.ozhuku.application.deduplication.ExecutionDeduplicationService;
import io.github.avinashio.ozhuku.application.orchestration.ExecutionOrchestrationService;
import io.github.avinashio.ozhuku.application.processing.ExecutionProcessingService;
import io.github.avinashio.ozhuku.application.processing.RecordProcessingRequest;
import io.github.avinashio.ozhuku.application.processing.ResourceTransferRequest;
import io.github.avinashio.ozhuku.domain.checkpoint.ProcessingCheckpoint;
import io.github.avinashio.ozhuku.domain.deduplication.DeduplicationDecision;
import io.github.avinashio.ozhuku.domain.deduplication.ProcessingRecord;
import io.github.avinashio.ozhuku.domain.execution.DestinationCommit;
import io.github.avinashio.ozhuku.domain.execution.DestinationCommitReference;
import io.github.avinashio.ozhuku.domain.execution.DestinationExecutionReference;
import io.github.avinashio.ozhuku.domain.execution.ExecutionReference;
import io.github.avinashio.ozhuku.domain.execution.SourceExecutionReference;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.FlowId;
import io.github.avinashio.ozhuku.domain.pipeline.PipelinePlan;
import io.github.avinashio.ozhuku.persistence.DestinationCommitRepository;
import io.github.avinashio.ozhuku.persistence.ProcessingCheckpointRepository;
import io.github.avinashio.ozhuku.persistence.ProcessingRecordRepository;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class ExecutionProcessingCoordinator {

    private final ExecutionOrchestrationService executionOrchestrationService;
    private final ExecutionProcessingService executionProcessingService;
    private final ExecutionResourceValidator executionResourceValidator;
    private final ExecutionDeduplicationService executionDeduplicationService;
    private final ProcessingRecordRepository processingRecordRepository;
    private final ProcessingCheckpointRepository processingCheckpointRepository;
    private final DestinationCommitRepository destinationCommitRepository;
    private final Clock clock;

    public ExecutionProcessingCoordinator(
            final ExecutionOrchestrationService executionOrchestrationService,
            final ExecutionProcessingService executionProcessingService,
            final ExecutionResourceValidator executionResourceValidator,
            final ExecutionDeduplicationService executionDeduplicationService,
            final ProcessingRecordRepository processingRecordRepository,
            final ProcessingCheckpointRepository processingCheckpointRepository,
            final DestinationCommitRepository destinationCommitRepository,
            final Clock clock) {

        this.executionOrchestrationService =
                Objects.requireNonNull(
                        executionOrchestrationService,
                        "executionOrchestrationService must not be null");

        this.executionProcessingService =
                Objects.requireNonNull(
                        executionProcessingService,
                        "executionProcessingService must not be null");

        this.executionResourceValidator =
                Objects.requireNonNull(
                        executionResourceValidator,
                        "executionResourceValidator must not be null");

        this.executionDeduplicationService =
                Objects.requireNonNull(
                        executionDeduplicationService,
                        "executionDeduplicationService must not be null");

        this.processingRecordRepository =
                Objects.requireNonNull(
                        processingRecordRepository,
                        "processingRecordRepository must not be null");

        this.processingCheckpointRepository =
                Objects.requireNonNull(
                        processingCheckpointRepository,
                        "processingCheckpointRepository must not be null");

        this.destinationCommitRepository =
                Objects.requireNonNull(
                        destinationCommitRepository,
                        "destinationCommitRepository must not be null");

        this.clock =
                Objects.requireNonNull(
                        clock,
                        "clock must not be null");
    }

    public void processResourceTransfer(
            final ExecutionReference executionReference,
            final PipelinePlan pipelinePlan,
            final ResourceTransferRequest request)
            throws IOException {

        Objects.requireNonNull(
                executionReference,
                "executionReference must not be null");

        Objects.requireNonNull(
                pipelinePlan,
                "pipelinePlan must not be null");

        Objects.requireNonNull(
                request,
                "request must not be null");

        final ExecutionId executionId =
                executionReference.executionId();

        final FlowId flowId =
                pipelinePlan.flow().id();

        executionResourceValidator.validate(
                executionId,
                request.source().id(),
                request.destination().id());

        executionOrchestrationService.startExecution(
                executionId,
                flowId,
                request.source().id(),
                request.destination().id());

        try {
            final ExecutionDeduplicationService.Result deduplicationResult =
                    executionDeduplicationService.evaluate(
                            executionReference,
                            pipelinePlan,
                            request.source());

            if (deduplicationResult.decision()
                    == DeduplicationDecision.SKIP) {

                executionOrchestrationService.completeExecution(
                        executionId,
                        flowId,
                        request.source().id(),
                        request.destination().id());

                return;
            }

            if (deduplicationResult.decision()
                    == DeduplicationDecision.FAIL) {

                throw new IllegalStateException(
                        "Duplicate source detected for processing identity");
            }

            // Avinash: Persist uncertainty before the destination side effect.
            saveDestinationCommitUnknown(
                    executionId,
                    request.destination().id());

            executionProcessingService.processResourceTransfer(
                    executionId,
                    request);

            saveDestinationCommit(
                    executionId,
                    request.destination().id());

            saveProcessedRecord(
                    deduplicationResult);

            executionOrchestrationService.completeExecution(
                    executionId,
                    flowId,
                    request.source().id(),
                    request.destination().id());

        } catch (IOException exception) {
            failExecution(
                    executionId,
                    flowId,
                    request,
                    exception);
            throw exception;

        } catch (RuntimeException exception) {
            failExecution(
                    executionId,
                    flowId,
                    request,
                    exception);
            throw exception;
        }
    }

    public void processRecordProcessing(
            final ExecutionReference executionReference,
            final PipelinePlan pipelinePlan,
            final RecordProcessingRequest request)
            throws IOException {

        Objects.requireNonNull(
                executionReference,
                "executionReference must not be null");

        Objects.requireNonNull(
                pipelinePlan,
                "pipelinePlan must not be null");

        Objects.requireNonNull(
                request,
                "request must not be null");

        final ExecutionId executionId =
                executionReference.executionId();

        final FlowId flowId =
                pipelinePlan.flow().id();

        executionResourceValidator.validate(
                executionId,
                request.source().id(),
                request.destination().id());

        executionOrchestrationService.startExecution(
                executionId,
                flowId,
                request.source().id(),
                request.destination().id());

        try {
            final ExecutionDeduplicationService.Result deduplicationResult =
                    executionDeduplicationService.evaluate(
                            executionReference,
                            pipelinePlan,
                            request.source());

            if (deduplicationResult.decision()
                    == DeduplicationDecision.SKIP) {

                executionOrchestrationService.completeExecution(
                        executionId,
                        flowId,
                        request.source().id(),
                        request.destination().id());

                return;
            }

            if (deduplicationResult.decision()
                    == DeduplicationDecision.FAIL) {

                throw new IllegalStateException(
                        "Duplicate source detected for processing identity");
            }

            // Avinash: Persist uncertainty before the destination side effect.
            saveDestinationCommitUnknown(
                    executionId,
                    request.destination().id());

            final long lastRecordSequence =
                    executionProcessingService.processRecordProcessing(
                            executionId,
                            request);

            saveDestinationCommit(
                    executionId,
                    request.destination().id());

            saveProcessingCheckpoint(
                    executionId,
                    request.source().id(),
                    lastRecordSequence);

            saveProcessedRecord(
                    deduplicationResult);

            executionOrchestrationService.completeExecution(
                    executionId,
                    flowId,
                    request.source().id(),
                    request.destination().id());

        } catch (IOException exception) {
            failExecution(
                    executionId,
                    flowId,
                    request,
                    exception);
            throw exception;

        } catch (RuntimeException exception) {
            failExecution(
                    executionId,
                    flowId,
                    request,
                    exception);
            throw exception;
        }
    }

    private void saveDestinationCommitUnknown(
            final ExecutionId executionId,
            final io.github.avinashio.ozhuku.domain.identity.ResourceId
                    destinationResourceId) {

        final DestinationExecutionReference destinationExecutionReference =
                new DestinationExecutionReference(
                        executionId,
                        destinationResourceId);

        final DestinationCommitReference destinationCommitReference =
                new DestinationCommitReference(
                        destinationExecutionReference);

        destinationCommitRepository.save(
                DestinationCommit.unknown(
                        destinationCommitReference));
    }

    private void saveDestinationCommit(
            final ExecutionId executionId,
            final io.github.avinashio.ozhuku.domain.identity.ResourceId
                    destinationResourceId) {

        final DestinationExecutionReference destinationExecutionReference =
                new DestinationExecutionReference(
                        executionId,
                        destinationResourceId);

        final DestinationCommitReference destinationCommitReference =
                new DestinationCommitReference(
                        destinationExecutionReference);

        final DestinationCommit destinationCommit =
                DestinationCommit.committed(
                        destinationCommitReference,
                        Instant.now(clock));

        destinationCommitRepository.save(
                destinationCommit);
    }

    private void saveProcessingCheckpoint(
            final ExecutionId executionId,
            final io.github.avinashio.ozhuku.domain.identity.ResourceId
                    sourceResourceId,
            final long lastRecordSequence) {

        if (lastRecordSequence < 0) {
            return;
        }

        final SourceExecutionReference sourceExecutionReference =
                new SourceExecutionReference(
                        executionId,
                        sourceResourceId);

        final ProcessingCheckpoint checkpoint =
                new ProcessingCheckpoint(
                        sourceExecutionReference,
                        lastRecordSequence,
                        Instant.now(clock));

        processingCheckpointRepository.save(
                checkpoint);
    }

    private void saveProcessedRecord(
            final ExecutionDeduplicationService.Result result) {

        final Instant processedAt =
                Instant.now(clock);

        processingRecordRepository.save(
                ProcessingRecord.processed(
                        result.processingIdentity(),
                        result.sourceFingerprint(),
                        processedAt));
    }

    private void failExecution(
            final ExecutionId executionId,
            final FlowId flowId,
            final ResourceTransferRequest request,
            final IOException originalException) {

        try {
            executionOrchestrationService.failExecution(
                    executionId,
                    flowId,
                    request.source().id(),
                    request.destination().id());

        } catch (RuntimeException failureException) {
            originalException.addSuppressed(
                    failureException);
        }
    }

    private void failExecution(
            final ExecutionId executionId,
            final FlowId flowId,
            final ResourceTransferRequest request,
            final RuntimeException originalException) {

        try {
            executionOrchestrationService.failExecution(
                    executionId,
                    flowId,
                    request.source().id(),
                    request.destination().id());

        } catch (RuntimeException failureException) {
            originalException.addSuppressed(
                    failureException);
        }
    }

    private void failExecution(
            final ExecutionId executionId,
            final FlowId flowId,
            final RecordProcessingRequest request,
            final IOException originalException) {

        try {
            executionOrchestrationService.failExecution(
                    executionId,
                    flowId,
                    request.source().id(),
                    request.destination().id());

        } catch (RuntimeException failureException) {
            originalException.addSuppressed(
                    failureException);
        }
    }

    private void failExecution(
            final ExecutionId executionId,
            final FlowId flowId,
            final RecordProcessingRequest request,
            final RuntimeException originalException) {

        try {
            executionOrchestrationService.failExecution(
                    executionId,
                    flowId,
                    request.source().id(),
                    request.destination().id());

        } catch (RuntimeException failureException) {
            originalException.addSuppressed(
                    failureException);
        }
    }
}
