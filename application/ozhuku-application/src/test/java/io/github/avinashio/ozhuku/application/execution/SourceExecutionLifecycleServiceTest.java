package io.github.avinashio.ozhuku.application.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.domain.execution.SourceExecution;
import io.github.avinashio.ozhuku.domain.execution.SourceExecutionReference;
import io.github.avinashio.ozhuku.domain.execution.SourceExecutionStatus;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.persistence.SourceExecutionRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SourceExecutionLifecycleServiceTest {

    private static final Instant FIXED_TIME =
            Instant.parse("2026-09-25T10:15:30Z");

    private static final ExecutionId EXECUTION_ID =
            new ExecutionId("execution-1");

    private static final ResourceId RESOURCE_ID =
            new ResourceId("resource-1");

    private FakeSourceExecutionRepository repository;
    private SourceExecutionLifecycleService service;

    @BeforeEach
    void setUp() {
        repository = new FakeSourceExecutionRepository();

        final Clock clock =
                Clock.fixed(FIXED_TIME, ZoneOffset.UTC);

        service = new SourceExecutionLifecycleService(
                repository,
                clock);
    }

    @Test
    void startShouldTransitionPendingToRunningAndSave() {
        final SourceExecution sourceExecution =
                createSourceExecution();

        repository.save(sourceExecution);

        final SourceExecution result =
                service.start(EXECUTION_ID, RESOURCE_ID);

        assertNotNull(result);
        assertEquals(
                SourceExecutionStatus.RUNNING,
                result.status());
        assertEquals(
                FIXED_TIME,
                result.startedAt());
        assertEquals(
                result,
                repository.saved());
    }

    @Test
    void completeShouldTransitionRunningToCompletedAndSave() {
        final SourceExecution sourceExecution =
                createSourceExecution();

        repository.save(sourceExecution);

        service.start(EXECUTION_ID, RESOURCE_ID);

        final SourceExecution result =
                service.complete(EXECUTION_ID, RESOURCE_ID);

        assertEquals(
                SourceExecutionStatus.COMPLETED,
                result.status());
        assertEquals(
                FIXED_TIME,
                result.completedAt());
        assertEquals(
                result,
                repository.saved());
    }

    @Test
    void failShouldTransitionRunningToFailedAndSave() {
        final SourceExecution sourceExecution =
                createSourceExecution();

        repository.save(sourceExecution);

        service.start(EXECUTION_ID, RESOURCE_ID);

        final SourceExecution result =
                service.fail(EXECUTION_ID, RESOURCE_ID);

        assertEquals(
                SourceExecutionStatus.FAILED,
                result.status());
        assertEquals(
                FIXED_TIME,
                result.completedAt());
        assertEquals(
                result,
                repository.saved());
    }

    @Test
    void cancelShouldTransitionPendingToCancelledAndSave() {
        final SourceExecution sourceExecution =
                createSourceExecution();

        repository.save(sourceExecution);

        final SourceExecution result =
                service.cancel(EXECUTION_ID, RESOURCE_ID);

        assertEquals(
                SourceExecutionStatus.CANCELLED,
                result.status());
        assertEquals(
                FIXED_TIME,
                result.completedAt());
        assertEquals(
                result,
                repository.saved());
    }

    @Test
    void startShouldFailWhenSourceExecutionDoesNotExist() {
        assertThrows(
                IllegalStateException.class,
                () -> service.start(EXECUTION_ID, RESOURCE_ID));
    }

    @Test
    void completeShouldRejectInvalidDomainTransition() {
        final SourceExecution sourceExecution =
                createSourceExecution();

        repository.save(sourceExecution);

        assertThrows(
                IllegalStateException.class,
                () -> service.complete(EXECUTION_ID, RESOURCE_ID));

        assertEquals(
                sourceExecution,
                repository.saved());
    }

    @Test
    void failShouldRejectInvalidDomainTransition() {
        final SourceExecution sourceExecution =
                createSourceExecution();

        repository.save(sourceExecution);

        assertThrows(
                IllegalStateException.class,
                () -> service.fail(EXECUTION_ID, RESOURCE_ID));

        assertEquals(
                sourceExecution,
                repository.saved());
    }

    @Test
    void cancelShouldRejectNullExecutionId() {
        assertThrows(
                NullPointerException.class,
                () -> service.cancel(null, RESOURCE_ID));
    }

    @Test
    void cancelShouldRejectNullResourceId() {
        assertThrows(
                NullPointerException.class,
                () -> service.cancel(EXECUTION_ID, null));
    }

    private static SourceExecution createSourceExecution() {
        final SourceExecutionReference reference =
                new SourceExecutionReference(
                        EXECUTION_ID,
                        RESOURCE_ID);

        return new SourceExecution(reference);
    }

    private static final class FakeSourceExecutionRepository
            implements SourceExecutionRepository {

        private final Map<String, SourceExecution> executions =
                new HashMap<>();

        private SourceExecution saved;

        @Override
        public Optional<SourceExecution> findById(
                final ExecutionId executionId,
                final ResourceId resourceId) {

            return Optional.ofNullable(
                    executions.get(key(executionId, resourceId)));
        }

        @Override
        public void save(
                final SourceExecution sourceExecution) {

            saved = sourceExecution;

            executions.put(
                    key(
                            sourceExecution.reference().executionId(),
                            sourceExecution.reference().resourceId()),
                    sourceExecution);
        }

        private SourceExecution saved() {
            return saved;
        }

        private static String key(
                final ExecutionId executionId,
                final ResourceId resourceId) {

            return executionId + ":" + resourceId;
        }
    }
}