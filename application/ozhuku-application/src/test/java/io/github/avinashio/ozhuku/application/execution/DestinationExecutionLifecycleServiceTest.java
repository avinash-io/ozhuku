package io.github.avinashio.ozhuku.application.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.domain.execution.DestinationExecution;
import io.github.avinashio.ozhuku.domain.execution.DestinationExecutionReference;
import io.github.avinashio.ozhuku.domain.execution.DestinationExecutionStatus;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.persistence.DestinationExecutionRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DestinationExecutionLifecycleServiceTest {

    private static final Instant FIXED_TIME =
            Instant.parse("2026-09-25T10:15:30Z");

    private static final ExecutionId EXECUTION_ID =
            new ExecutionId("execution-1");

    private static final ResourceId RESOURCE_ID =
            new ResourceId("resource-1");

    private FakeDestinationExecutionRepository repository;
    private DestinationExecutionLifecycleService service;

    @BeforeEach
    void setUp() {
        repository = new FakeDestinationExecutionRepository();

        final Clock clock =
                Clock.fixed(FIXED_TIME, ZoneOffset.UTC);

        service = new DestinationExecutionLifecycleService(
                repository,
                clock);
    }

    @Test
    void startShouldTransitionPendingToRunningAndSave() {
        final DestinationExecution destinationExecution =
                createDestinationExecution();

        repository.save(destinationExecution);

        final DestinationExecution result =
                service.start(EXECUTION_ID, RESOURCE_ID);

        assertNotNull(result);
        assertEquals(
                DestinationExecutionStatus.RUNNING,
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
        final DestinationExecution destinationExecution =
                createDestinationExecution();

        repository.save(destinationExecution);

        service.start(EXECUTION_ID, RESOURCE_ID);

        final DestinationExecution result =
                service.complete(EXECUTION_ID, RESOURCE_ID);

        assertEquals(
                DestinationExecutionStatus.COMPLETED,
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
        final DestinationExecution destinationExecution =
                createDestinationExecution();

        repository.save(destinationExecution);

        service.start(EXECUTION_ID, RESOURCE_ID);

        final DestinationExecution result =
                service.fail(EXECUTION_ID, RESOURCE_ID);

        assertEquals(
                DestinationExecutionStatus.FAILED,
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
        final DestinationExecution destinationExecution =
                createDestinationExecution();

        repository.save(destinationExecution);

        final DestinationExecution result =
                service.cancel(EXECUTION_ID, RESOURCE_ID);

        assertEquals(
                DestinationExecutionStatus.CANCELLED,
                result.status());
        assertEquals(
                FIXED_TIME,
                result.completedAt());
        assertEquals(
                result,
                repository.saved());
    }

    @Test
    void startShouldFailWhenDestinationExecutionDoesNotExist() {
        assertThrows(
                IllegalStateException.class,
                () -> service.start(EXECUTION_ID, RESOURCE_ID));
    }

    @Test
    void completeShouldRejectInvalidDomainTransition() {
        final DestinationExecution destinationExecution =
                createDestinationExecution();

        repository.save(destinationExecution);

        assertThrows(
                IllegalStateException.class,
                () -> service.complete(EXECUTION_ID, RESOURCE_ID));

        assertEquals(
                destinationExecution,
                repository.saved());
    }

    @Test
    void failShouldRejectInvalidDomainTransition() {
        final DestinationExecution destinationExecution =
                createDestinationExecution();

        repository.save(destinationExecution);

        assertThrows(
                IllegalStateException.class,
                () -> service.fail(EXECUTION_ID, RESOURCE_ID));

        assertEquals(
                destinationExecution,
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

    private static DestinationExecution createDestinationExecution() {
        final DestinationExecutionReference reference =
                new DestinationExecutionReference(
                        EXECUTION_ID,
                        RESOURCE_ID);

        return new DestinationExecution(reference);
    }

    private static final class FakeDestinationExecutionRepository
            implements DestinationExecutionRepository {

        private final Map<String, DestinationExecution> executions =
                new HashMap<>();

        private DestinationExecution saved;

        @Override
        public Optional<DestinationExecution> findById(
                final ExecutionId executionId,
                final ResourceId resourceId) {

            return Optional.ofNullable(
                    executions.get(key(executionId, resourceId)));
        }

        @Override
        public void save(
                final DestinationExecution destinationExecution) {

            saved = destinationExecution;

            executions.put(
                    key(
                            destinationExecution.reference().executionId(),
                            destinationExecution.reference().resourceId()),
                    destinationExecution);
        }

        private DestinationExecution saved() {
            return saved;
        }

        private static String key(
                final ExecutionId executionId,
                final ResourceId resourceId) {

            return executionId + ":" + resourceId;
        }
    }
}