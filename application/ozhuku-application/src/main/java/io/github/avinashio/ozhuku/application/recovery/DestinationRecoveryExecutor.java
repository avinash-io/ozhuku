package io.github.avinashio.ozhuku.application.recovery;

import io.github.avinashio.ozhuku.application.pipeline.PipelineConfigurationResolutionService;
import io.github.avinashio.ozhuku.application.transfer.ResourceTransferService;
import io.github.avinashio.ozhuku.domain.execution.DestinationCommit;
import io.github.avinashio.ozhuku.domain.execution.DestinationCommitReference;
import io.github.avinashio.ozhuku.domain.execution.DestinationExecutionReference;
import io.github.avinashio.ozhuku.domain.execution.Execution;
import io.github.avinashio.ozhuku.domain.pipeline.PipelinePlan;
import io.github.avinashio.ozhuku.persistence.DestinationCommitRepository;
import io.github.avinashio.ozhuku.persistence.ExecutionRepository;
import io.github.avinashio.ozhuku.foundation.validation.Validation;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;

public final class DestinationRecoveryExecutor {

    private final DestinationRecoveryService recoveryService;
    private final ExecutionRepository executionRepository;
    private final PipelineConfigurationResolutionService
            configurationResolutionService;
    private final ResourceTransferService resourceTransferService;
    private final DestinationCommitRepository destinationCommitRepository;
    private final Clock clock;

    public DestinationRecoveryExecutor(
            final DestinationRecoveryService recoveryService,
            final ExecutionRepository executionRepository,
            final PipelineConfigurationResolutionService
                    configurationResolutionService,
            final ResourceTransferService resourceTransferService,
            final DestinationCommitRepository destinationCommitRepository,
            final Clock clock) {

        this.recoveryService =
                Validation.requireNonNull(
                        recoveryService,
                        "recoveryService");

        this.executionRepository =
                Validation.requireNonNull(
                        executionRepository,
                        "executionRepository");

        this.configurationResolutionService =
                Validation.requireNonNull(
                        configurationResolutionService,
                        "configurationResolutionService");

        this.resourceTransferService =
                Validation.requireNonNull(
                        resourceTransferService,
                        "resourceTransferService");

        this.destinationCommitRepository =
                Validation.requireNonNull(
                        destinationCommitRepository,
                        "destinationCommitRepository");

        this.clock =
                Validation.requireNonNull(
                        clock,
                        "clock");
    }

    public void execute(
            final DestinationExecutionReference reference)
            throws IOException {

        Validation.requireNonNull(reference, "reference");

        final RecoveryResult recoveryResult =
                recoveryService.decide(reference);

        if (recoveryResult.decision()
                != RecoveryDecision.RETRY) {
            return;
        }

        final Execution execution =
                executionRepository
                        .findById(reference.executionId())
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Execution does not exist: "
                                                        + reference
                                                        .executionId()
                                                        .value()));

        final PipelinePlan pipelinePlan =
                configurationResolutionService.resolve(
                        execution.reference().pipelineId(),
                        execution.reference().pipelineVersion());

        final DestinationCommitReference
                destinationCommitReference =
                new DestinationCommitReference(reference);

        // Avinash: Persist uncertainty before the destination side effect.
        destinationCommitRepository.save(
                DestinationCommit.unknown(
                        destinationCommitReference));

        resourceTransferService.transfer(
                pipelinePlan.source(),
                pipelinePlan.destination(),
                pipelinePlan.deliveryPolicy());


        final DestinationCommit committed =
                DestinationCommit.committed(
                        destinationCommitReference,
                        Instant.now(clock));

        destinationCommitRepository.save(committed);
    }
}