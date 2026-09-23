package io.github.avinashio.ozhuku.application.recovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.domain.execution.CommitStatus;
import io.github.avinashio.ozhuku.domain.execution.DestinationCommit;
import io.github.avinashio.ozhuku.domain.execution.DestinationCommitReference;
import io.github.avinashio.ozhuku.domain.execution.DestinationExecution;
import io.github.avinashio.ozhuku.domain.execution.DestinationExecutionReference;
import io.github.avinashio.ozhuku.domain.execution.DestinationExecutionStatus;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.persistence.DestinationCommitRepository;
import io.github.avinashio.ozhuku.persistence.DestinationExecutionRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DestinationRecoveryServiceTest {

    @Test
    void shouldNotRetryWhenCommitIsConfirmed() {
        final DestinationExecutionReference reference =
                reference();

        final InMemoryDestinationExecutionRepository
                executionRepository =
                new InMemoryDestinationExecutionRepository();

        final InMemoryDestinationCommitRepository
                commitRepository =
                new InMemoryDestinationCommitRepository();

        saveFailedExecution(
                executionRepository,
                reference);

        commitRepository.save(
                DestinationCommit.committed(
                        new DestinationCommitReference(reference),
                        Instant.parse(
                                "2026-09-23T00:00:00Z")));

        final DestinationRecoveryService service =
                service(
                        executionRepository,
                        commitRepository,
                        ignoredReference -> CommitStatus.UNKNOWN);

        final RecoveryResult result =
                service.decide(reference);

        assertEquals(
                RecoveryDecision.DO_NOT_RETRY,
                result.decision());

        assertEquals(
                RecoveryReason.COMMIT_CONFIRMED,
                result.reason());
    }

    @Test
    void shouldRetryWhenCommitIsNotCommitted() {
        final DestinationExecutionReference reference =
                reference();

        final InMemoryDestinationExecutionRepository
                executionRepository =
                new InMemoryDestinationExecutionRepository();

        final InMemoryDestinationCommitRepository
                commitRepository =
                new InMemoryDestinationCommitRepository();

        saveFailedExecution(
                executionRepository,
                reference);

        commitRepository.save(
                DestinationCommit.notCommitted(
                        new DestinationCommitReference(reference)));

        final DestinationRecoveryService service =
                service(
                        executionRepository,
                        commitRepository,
                        ignoredReference -> CommitStatus.UNKNOWN);

        final RecoveryResult result =
                service.decide(reference);

        assertEquals(
                RecoveryDecision.RETRY,
                result.decision());

        assertEquals(
                RecoveryReason.COMMIT_NOT_CONFIRMED,
                result.reason());
    }

    @Test
    void shouldInspectUnknownCommit() {
        final DestinationExecutionReference reference =
                reference();

        final InMemoryDestinationExecutionRepository
                executionRepository =
                new InMemoryDestinationExecutionRepository();

        final InMemoryDestinationCommitRepository
                commitRepository =
                new InMemoryDestinationCommitRepository();

        saveFailedExecution(
                executionRepository,
                reference);

        commitRepository.save(
                DestinationCommit.unknown(
                        new DestinationCommitReference(reference)));

        final DestinationRecoveryService service =
                service(
                        executionRepository,
                        commitRepository,
                        ignoredReference -> CommitStatus.COMMITTED);

        final RecoveryResult result =
                service.decide(reference);

        assertEquals(
                RecoveryDecision.DO_NOT_RETRY,
                result.decision());

        assertEquals(
                RecoveryReason.DESTINATION_OUTCOME_CONFIRMED,
                result.reason());
    }

    @Test
    void shouldRemainUnresolvedWhenInspectionRemainsUnknown() {
        final DestinationExecutionReference reference =
                reference();

        final InMemoryDestinationExecutionRepository
                executionRepository =
                new InMemoryDestinationExecutionRepository();

        final InMemoryDestinationCommitRepository
                commitRepository =
                new InMemoryDestinationCommitRepository();

        saveFailedExecution(
                executionRepository,
                reference);

        commitRepository.save(
                DestinationCommit.unknown(
                        new DestinationCommitReference(reference)));

        final DestinationRecoveryService service =
                service(
                        executionRepository,
                        commitRepository,
                        ignoredReference -> CommitStatus.UNKNOWN);

        final RecoveryResult result =
                service.decide(reference);

        assertEquals(
                RecoveryDecision.UNRESOLVED,
                result.decision());

        assertEquals(
                RecoveryReason.DESTINATION_OUTCOME_UNKNOWN,
                result.reason());
    }

    @Test
    void shouldFailWhenDestinationExecutionStateDoesNotExist() {
        final DestinationExecutionReference reference =
                reference();

        final InMemoryDestinationExecutionRepository
                executionRepository =
                new InMemoryDestinationExecutionRepository();

        final InMemoryDestinationCommitRepository
                commitRepository =
                new InMemoryDestinationCommitRepository();

        final DestinationRecoveryService service =
                service(
                        executionRepository,
                        commitRepository,
                        ignoredReference -> CommitStatus.UNKNOWN);

        assertThrows(
                IllegalStateException.class,
                () -> service.decide(reference));
    }

    @Test
    void shouldFailWhenDestinationCommitStateDoesNotExist() {
        final DestinationExecutionReference reference =
                reference();

        final InMemoryDestinationExecutionRepository
                executionRepository =
                new InMemoryDestinationExecutionRepository();

        final InMemoryDestinationCommitRepository
                commitRepository =
                new InMemoryDestinationCommitRepository();

        saveFailedExecution(
                executionRepository,
                reference);

        final DestinationRecoveryService service =
                service(
                        executionRepository,
                        commitRepository,
                        ignoredReference -> CommitStatus.UNKNOWN);

        assertThrows(
                IllegalStateException.class,
                () -> service.decide(reference));
    }

    @Test
    void shouldNotRetryWhenDestinationExecutionIsCompleted() {
        final DestinationExecutionReference reference =
                reference();

        final InMemoryDestinationExecutionRepository
                executionRepository =
                new InMemoryDestinationExecutionRepository();

        final InMemoryDestinationCommitRepository
                commitRepository =
                new InMemoryDestinationCommitRepository();

        executionRepository.save(
                DestinationExecution.rehydrate(
                        reference,
                        DestinationExecutionStatus.COMPLETED,
                        Instant.parse(
                                "2026-09-23T00:00:00Z"),
                        Instant.parse(
                                "2026-09-23T00:00:10Z")));

        commitRepository.save(
                DestinationCommit.notCommitted(
                        new DestinationCommitReference(reference)));

        final DestinationRecoveryService service =
                service(
                        executionRepository,
                        commitRepository,
                        ignoredReference -> CommitStatus.UNKNOWN);

        final RecoveryResult result =
                service.decide(reference);

        assertEquals(
                RecoveryDecision.DO_NOT_RETRY,
                result.decision());

        assertEquals(
                RecoveryReason.EXECUTION_NOT_RECOVERABLE,
                result.reason());
    }

    @Test
    void shouldNotRetryWhenDestinationExecutionIsCancelled() {
        final DestinationExecutionReference reference =
                reference();

        final InMemoryDestinationExecutionRepository
                executionRepository =
                new InMemoryDestinationExecutionRepository();

        final InMemoryDestinationCommitRepository
                commitRepository =
                new InMemoryDestinationCommitRepository();

        executionRepository.save(
                DestinationExecution.rehydrate(
                        reference,
                        DestinationExecutionStatus.CANCELLED,
                        null,
                        Instant.parse(
                                "2026-09-23T00:00:10Z")));

        commitRepository.save(
                DestinationCommit.notCommitted(
                        new DestinationCommitReference(reference)));

        final DestinationRecoveryService service =
                service(
                        executionRepository,
                        commitRepository,
                        ignoredReference -> CommitStatus.UNKNOWN);

        final RecoveryResult result =
                service.decide(reference);

        assertEquals(
                RecoveryDecision.DO_NOT_RETRY,
                result.decision());

        assertEquals(
                RecoveryReason.EXECUTION_NOT_RECOVERABLE,
                result.reason());
    }

    @Test
    void shouldAllowRecoveryWhenDestinationExecutionIsPending() {
        final DestinationExecutionReference reference =
                reference();

        final InMemoryDestinationExecutionRepository
                executionRepository =
                new InMemoryDestinationExecutionRepository();

        final InMemoryDestinationCommitRepository
                commitRepository =
                new InMemoryDestinationCommitRepository();

        executionRepository.save(
                DestinationExecution.rehydrate(
                        reference,
                        DestinationExecutionStatus.PENDING,
                        null,
                        null));

        commitRepository.save(
                DestinationCommit.notCommitted(
                        new DestinationCommitReference(reference)));

        final DestinationRecoveryService service =
                service(
                        executionRepository,
                        commitRepository,
                        ignoredReference -> CommitStatus.UNKNOWN);

        final RecoveryResult result =
                service.decide(reference);

        assertEquals(
                RecoveryDecision.RETRY,
                result.decision());

        assertEquals(
                RecoveryReason.COMMIT_NOT_CONFIRMED,
                result.reason());
    }

    @Test
    void shouldAllowRecoveryWhenDestinationExecutionIsRunning() {
        final DestinationExecutionReference reference =
                reference();

        final InMemoryDestinationExecutionRepository
                executionRepository =
                new InMemoryDestinationExecutionRepository();

        final InMemoryDestinationCommitRepository
                commitRepository =
                new InMemoryDestinationCommitRepository();

        executionRepository.save(
                DestinationExecution.rehydrate(
                        reference,
                        DestinationExecutionStatus.RUNNING,
                        Instant.parse(
                                "2026-09-23T00:00:00Z"),
                        null));

        commitRepository.save(
                DestinationCommit.notCommitted(
                        new DestinationCommitReference(reference)));

        final DestinationRecoveryService service =
                service(
                        executionRepository,
                        commitRepository,
                        ignoredReference -> CommitStatus.UNKNOWN);

        final RecoveryResult result =
                service.decide(reference);

        assertEquals(
                RecoveryDecision.RETRY,
                result.decision());

        assertEquals(
                RecoveryReason.COMMIT_NOT_CONFIRMED,
                result.reason());
    }

    @Test
    void shouldAllowRecoveryWhenDestinationExecutionFailed() {
        final DestinationExecutionReference reference =
                reference();

        final InMemoryDestinationExecutionRepository
                executionRepository =
                new InMemoryDestinationExecutionRepository();

        final InMemoryDestinationCommitRepository
                commitRepository =
                new InMemoryDestinationCommitRepository();

        saveFailedExecution(
                executionRepository,
                reference);

        commitRepository.save(
                DestinationCommit.notCommitted(
                        new DestinationCommitReference(reference)));

        final DestinationRecoveryService service =
                service(
                        executionRepository,
                        commitRepository,
                        ignoredReference -> CommitStatus.UNKNOWN);

        final RecoveryResult result =
                service.decide(reference);

        assertEquals(
                RecoveryDecision.RETRY,
                result.decision());

        assertEquals(
                RecoveryReason.COMMIT_NOT_CONFIRMED,
                result.reason());
    }

    private static DestinationRecoveryService service(
            final DestinationExecutionRepository executionRepository,
            final DestinationCommitRepository commitRepository,
            final DestinationOutcomeInspector inspector) {

        final DestinationRecoveryDecider decider =
                new DestinationRecoveryDecider(inspector);

        return new DestinationRecoveryService(
                executionRepository,
                commitRepository,
                new DestinationExecutionRecoveryPolicy(),
                decider);
    }

    private static DestinationExecutionReference reference() {
        return new DestinationExecutionReference(
                new ExecutionId("execution-recovery-service"),
                new ResourceId("resource-recovery-service"));
    }

    private static void saveFailedExecution(
            final DestinationExecutionRepository repository,
            final DestinationExecutionReference reference) {

        repository.save(
                DestinationExecution.rehydrate(
                        reference,
                        DestinationExecutionStatus.FAILED,
                        Instant.parse(
                                "2026-09-23T00:00:00Z"),
                        Instant.parse(
                                "2026-09-23T00:00:10Z")));
    }

    private static final class InMemoryDestinationExecutionRepository
            implements DestinationExecutionRepository {

        private final Map<
                DestinationExecutionReference,
                DestinationExecution>
                executions =
                new HashMap<>();

        @Override
        public Optional<DestinationExecution> findById(
                final ExecutionId executionId,
                final ResourceId resourceId) {

            return Optional.ofNullable(
                    executions.get(
                            new DestinationExecutionReference(
                                    executionId,
                                    resourceId)));
        }

        @Override
        public void save(
                final DestinationExecution destinationExecution) {

            executions.put(
                    destinationExecution.reference(),
                    destinationExecution);
        }
    }

    private static final class InMemoryDestinationCommitRepository
            implements DestinationCommitRepository {

        private final Map<
                DestinationCommitReference,
                DestinationCommit>
                commits =
                new HashMap<>();

        @Override
        public Optional<DestinationCommit> findById(
                final DestinationCommitReference reference) {

            return Optional.ofNullable(
                    commits.get(reference));
        }

        @Override
        public void save(
                final DestinationCommit destinationCommit) {

            commits.put(
                    destinationCommit.reference(),
                    destinationCommit);
        }
    }
}

