package io.github.avinashio.ozhuku.application.recovery;

import io.github.avinashio.ozhuku.domain.execution.DestinationCommit;
import io.github.avinashio.ozhuku.domain.execution.DestinationCommitReference;
import io.github.avinashio.ozhuku.domain.execution.DestinationExecution;
import io.github.avinashio.ozhuku.domain.execution.DestinationExecutionReference;
import io.github.avinashio.ozhuku.persistence.DestinationCommitRepository;
import io.github.avinashio.ozhuku.persistence.DestinationExecutionRepository;
import java.util.Objects;
import java.util.Optional;

public final class DestinationRecoveryService
        implements RecoveryDecisionProvider {

    private final DestinationExecutionRepository
            destinationExecutionRepository;

    private final DestinationCommitRepository
            destinationCommitRepository;

    private final DestinationExecutionRecoveryPolicy
            executionRecoveryPolicy;

    private final DestinationRecoveryDecider recoveryDecider;

    public DestinationRecoveryService(
            final DestinationExecutionRepository
                    destinationExecutionRepository,
            final DestinationCommitRepository destinationCommitRepository,
            final DestinationExecutionRecoveryPolicy
                    executionRecoveryPolicy,
            final DestinationRecoveryDecider recoveryDecider) {

        this.destinationExecutionRepository =
                Objects.requireNonNull(
                        destinationExecutionRepository,
                        "destinationExecutionRepository "
                                + "must not be null");

        this.destinationCommitRepository =
                Objects.requireNonNull(
                        destinationCommitRepository,
                        "destinationCommitRepository "
                                + "must not be null");

        this.executionRecoveryPolicy =
                Objects.requireNonNull(
                        executionRecoveryPolicy,
                        "executionRecoveryPolicy "
                                + "must not be null");

        this.recoveryDecider = Objects.requireNonNull(
                recoveryDecider,
                "recoveryDecider must not be null");
    }

    public RecoveryResult decide(
            final DestinationExecutionReference reference) {

        Objects.requireNonNull(
                reference,
                "reference must not be null");

        final Optional<DestinationExecution> execution =
                destinationExecutionRepository.findById(
                        reference.executionId(),
                        reference.resourceId());

        if (execution.isEmpty()) {
            throw new IllegalStateException(
                    "Destination execution state does not exist "
                            + "for recovery reference: "
                            + reference);
        }

        if (!executionRecoveryPolicy.canRecover(
                execution.orElseThrow().status())) {

            return new RecoveryResult(
                    RecoveryDecision.DO_NOT_RETRY,
                    RecoveryReason.EXECUTION_NOT_RECOVERABLE);
        }

        final DestinationCommitReference commitReference =
                new DestinationCommitReference(reference);

        final Optional<DestinationCommit> commit =
                destinationCommitRepository.findById(
                        commitReference);

        if (commit.isEmpty()) {
            throw new IllegalStateException(
                    "Destination commit state does not exist "
                            + "for recovery reference: "
                            + reference);
        }

        return recoveryDecider.decide(
                commit.orElseThrow());
    }
}