package io.github.avinashio.ozhuku.application.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.domain.execution.Execution;
import io.github.avinashio.ozhuku.domain.execution.ExecutionStatus;
import io.github.avinashio.ozhuku.domain.execution.ExecutionReference;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.PipelineId;
import io.github.avinashio.ozhuku.domain.identity.PipelineVersion;
import io.github.avinashio.ozhuku.persistence.ExecutionRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ExecutionLifecycleServiceTest {

    private static final Instant NOW =
            Instant.parse("2026-09-25T12:00:00Z");

    @Test
    void shouldStartExecutionAndPersistUpdatedState() {
        final Execution execution = pendingExecution();
        final FakeExecutionRepository repository =
                new FakeExecutionRepository(execution);

        final ExecutionLifecycleService service =
                new ExecutionLifecycleService(
                        repository,
                        fixedClock());

        final Execution result =
                service.start(execution.reference().executionId());

        assertEquals(ExecutionStatus.RUNNING, result.status());
        assertEquals(NOW, result.startedAt());
        assertEquals(result, repository.saved());
    }

    @Test
    void shouldCompleteRunningExecutionAndPersistUpdatedState() {
        final Execution execution =
                pendingExecution().start(NOW.minusSeconds(60));

        final FakeExecutionRepository repository =
                new FakeExecutionRepository(execution);

        final ExecutionLifecycleService service =
                new ExecutionLifecycleService(
                        repository,
                        fixedClock());

        final Execution result =
                service.complete(execution.reference().executionId());

        assertEquals(ExecutionStatus.COMPLETED, result.status());
        assertEquals(NOW, result.completedAt());
        assertEquals(result, repository.saved());
    }

    @Test
    void shouldFailRunningExecutionAndPersistUpdatedState() {
        final Execution execution =
                pendingExecution().start(NOW.minusSeconds(60));

        final FakeExecutionRepository repository =
                new FakeExecutionRepository(execution);

        final ExecutionLifecycleService service =
                new ExecutionLifecycleService(
                        repository,
                        fixedClock());

        final Execution result =
                service.fail(execution.reference().executionId());

        assertEquals(ExecutionStatus.FAILED, result.status());
        assertEquals(NOW, result.completedAt());
        assertEquals(result, repository.saved());
    }

    @Test
    void shouldCancelPendingExecutionAndPersistUpdatedState() {
        final Execution execution = pendingExecution();

        final FakeExecutionRepository repository =
                new FakeExecutionRepository(execution);

        final ExecutionLifecycleService service =
                new ExecutionLifecycleService(
                        repository,
                        fixedClock());

        final Execution result =
                service.cancel(execution.reference().executionId());

        assertEquals(ExecutionStatus.CANCELLED, result.status());
        assertEquals(NOW, result.completedAt());
        assertEquals(result, repository.saved());
    }

    @Test
    void shouldRejectMissingExecution() {
        final FakeExecutionRepository repository =
                new FakeExecutionRepository();

        final ExecutionLifecycleService service =
                new ExecutionLifecycleService(
                        repository,
                        fixedClock());

        assertThrows(
                IllegalStateException.class,
                () -> service.start(
                        new ExecutionId("missing")));
    }

    @Test
    void shouldNotPersistWhenDomainTransitionFails() {
        final Execution execution =
                pendingExecution();

        final FakeExecutionRepository repository =
                new FakeExecutionRepository(execution);

        final ExecutionLifecycleService service =
                new ExecutionLifecycleService(
                        repository,
                        fixedClock());

        service.cancel(execution.reference().executionId());

        repository.clearSaved();

        assertThrows(
                IllegalStateException.class,
                () -> service.start(
                        execution.reference().executionId()));

        assertEquals(null, repository.saved());
    }

    private Execution pendingExecution() {
        return new Execution(
                new ExecutionReference(
                        new ExecutionId("execution-1"),
                        new PipelineId("pipeline-1"),
                        new PipelineVersion(1)));
    }

    private Clock fixedClock() {
        return Clock.fixed(NOW, ZoneOffset.UTC);
    }

    private static final class FakeExecutionRepository
            implements ExecutionRepository {

        private final Map<ExecutionId, Execution> executions =
                new HashMap<>();

        private Execution saved;

        private FakeExecutionRepository(
                final Execution... executions) {
            for (final Execution execution : executions) {
                this.executions.put(
                        execution.reference().executionId(),
                        execution);
            }
        }

        @Override
        public Optional<Execution> findById(
                final ExecutionId executionId) {
            return Optional.ofNullable(executions.get(executionId));
        }

        @Override
        public void save(final Execution execution) {
            executions.put(
                    execution.reference().executionId(),
                    execution);
            saved = execution;
        }

        private Execution saved() {
            return saved;
        }

        private void clearSaved() {
            saved = null;
        }
    }
}
