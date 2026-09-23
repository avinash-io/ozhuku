package io.github.avinashio.ozhuku.application.recovery;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.avinashio.ozhuku.domain.execution.CommitStatus;
import io.github.avinashio.ozhuku.domain.execution.DestinationCommit;
import io.github.avinashio.ozhuku.domain.execution.DestinationCommitReference;
import io.github.avinashio.ozhuku.domain.execution.DestinationExecutionReference;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import org.junit.jupiter.api.Test;

class DestinationRecoveryDeciderTest {

    @Test
    void shouldNotRetryWhenCommitIsAlreadyConfirmed() {
        final DestinationCommitReference reference =
                reference();

        final DestinationCommit commit =
                DestinationCommit.committed(
                        reference,
                        java.time.Instant.parse(
                                "2026-09-23T00:00:00Z"));

        final DestinationOutcomeInspector inspector =
                ignoredReference -> CommitStatus.UNKNOWN;

        final DestinationRecoveryDecider decider =
                new DestinationRecoveryDecider(inspector);

        final RecoveryResult result =
                decider.decide(commit);

        assertEquals(
                RecoveryDecision.DO_NOT_RETRY,
                result.decision());

        assertEquals(
                RecoveryReason.COMMIT_CONFIRMED,
                result.reason());
    }

    @Test
    void shouldRetryWhenCommitIsNotCommitted() {
        final DestinationCommitReference reference =
                reference();

        final DestinationCommit commit =
                DestinationCommit.notCommitted(reference);

        final DestinationOutcomeInspector inspector =
                ignoredReference -> CommitStatus.UNKNOWN;

        final DestinationRecoveryDecider decider =
                new DestinationRecoveryDecider(inspector);

        final RecoveryResult result =
                decider.decide(commit);

        assertEquals(
                RecoveryDecision.RETRY,
                result.decision());

        assertEquals(
                RecoveryReason.COMMIT_NOT_CONFIRMED,
                result.reason());
    }

    @Test
    void shouldInspectUnknownCommitAndNotRetryWhenDestinationIsCommitted() {
        final DestinationCommitReference reference =
                reference();

        final DestinationCommit commit =
                DestinationCommit.unknown(reference);

        final DestinationOutcomeInspector inspector =
                ignoredReference -> CommitStatus.COMMITTED;

        final DestinationRecoveryDecider decider =
                new DestinationRecoveryDecider(inspector);

        final RecoveryResult result =
                decider.decide(commit);

        assertEquals(
                RecoveryDecision.DO_NOT_RETRY,
                result.decision());

        assertEquals(
                RecoveryReason.DESTINATION_OUTCOME_CONFIRMED,
                result.reason());
    }

    @Test
    void shouldInspectUnknownCommitAndRetryWhenDestinationIsNotCommitted() {
        final DestinationCommitReference reference =
                reference();

        final DestinationCommit commit =
                DestinationCommit.unknown(reference);

        final DestinationOutcomeInspector inspector =
                ignoredReference -> CommitStatus.NOT_COMMITTED;

        final DestinationRecoveryDecider decider =
                new DestinationRecoveryDecider(inspector);

        final RecoveryResult result =
                decider.decide(commit);

        assertEquals(
                RecoveryDecision.RETRY,
                result.decision());

        assertEquals(
                RecoveryReason.DESTINATION_OUTCOME_NOT_CONFIRMED,
                result.reason());
    }

    @Test
    void shouldRemainUnresolvedWhenUnknownOutcomeRemainsUnknown() {
        final DestinationCommitReference reference =
                reference();

        final DestinationCommit commit =
                DestinationCommit.unknown(reference);

        final DestinationOutcomeInspector inspector =
                ignoredReference -> CommitStatus.UNKNOWN;

        final DestinationRecoveryDecider decider =
                new DestinationRecoveryDecider(inspector);

        final RecoveryResult result =
                decider.decide(commit);

        assertEquals(
                RecoveryDecision.UNRESOLVED,
                result.decision());

        assertEquals(
                RecoveryReason.DESTINATION_OUTCOME_UNKNOWN,
                result.reason());
    }

    private static DestinationCommitReference reference() {
        return new DestinationCommitReference(
                new DestinationExecutionReference(
                        new ExecutionId("execution-recovery-test"),
                        new ResourceId("resource-recovery-test")));
    }
}
