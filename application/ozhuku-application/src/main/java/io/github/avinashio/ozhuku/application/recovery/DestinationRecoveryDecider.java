package io.github.avinashio.ozhuku.application.recovery;

import io.github.avinashio.ozhuku.domain.execution.CommitStatus;
import io.github.avinashio.ozhuku.domain.execution.DestinationCommit;
import io.github.avinashio.ozhuku.domain.execution.DestinationCommitReference;
import java.util.Objects;

public final class DestinationRecoveryDecider {

    private final DestinationOutcomeInspector outcomeInspector;

    public DestinationRecoveryDecider(
            final DestinationOutcomeInspector outcomeInspector) {

        this.outcomeInspector = Objects.requireNonNull(
                outcomeInspector,
                "outcomeInspector must not be null");
    }

    public RecoveryResult decide(
            final DestinationCommit destinationCommit) {

        Objects.requireNonNull(
                destinationCommit,
                "destinationCommit must not be null");

        return switch (destinationCommit.status()) {
            case COMMITTED ->
                    new RecoveryResult(
                            RecoveryDecision.DO_NOT_RETRY,
                            RecoveryReason.COMMIT_CONFIRMED);

            case NOT_COMMITTED ->
                    new RecoveryResult(
                            RecoveryDecision.RETRY,
                            RecoveryReason.COMMIT_NOT_CONFIRMED);

            case UNKNOWN ->
                    decideUnknownOutcome(
                            destinationCommit.reference());
        };
    }

    private RecoveryResult decideUnknownOutcome(
            final DestinationCommitReference reference) {

        final CommitStatus inspectedStatus =
                Objects.requireNonNull(
                        outcomeInspector.inspect(reference),
                        "inspected destination status must not be null");

        return switch (inspectedStatus) {
            case COMMITTED ->
                    new RecoveryResult(
                            RecoveryDecision.DO_NOT_RETRY,
                            RecoveryReason.DESTINATION_OUTCOME_CONFIRMED);

            case NOT_COMMITTED ->
                    new RecoveryResult(
                            RecoveryDecision.RETRY,
                            RecoveryReason.DESTINATION_OUTCOME_NOT_CONFIRMED);

            case UNKNOWN ->
                    new RecoveryResult(
                            RecoveryDecision.UNRESOLVED,
                            RecoveryReason.DESTINATION_OUTCOME_UNKNOWN);
        };
    }
}