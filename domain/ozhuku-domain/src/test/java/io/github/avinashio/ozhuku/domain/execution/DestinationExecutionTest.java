package io.github.avinashio.ozhuku.domain.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class DestinationExecutionTest {

    @Test
    void shouldCreatePendingDestinationExecution() {
        final DestinationExecutionReference reference =
                reference();

        final DestinationExecution execution =
                new DestinationExecution(reference);

        assertEquals(reference, execution.reference());
        assertEquals(
                DestinationExecutionStatus.PENDING,
                execution.status());
        assertNull(execution.startedAt());
        assertNull(execution.completedAt());
    }

    @Test
    void shouldStartPendingDestinationExecution() {
        final Instant startedAt =
                Instant.parse("2026-09-22T10:00:00Z");

        final DestinationExecution execution =
                new DestinationExecution(reference());

        final DestinationExecution started =
                execution.start(startedAt);

        assertEquals(
                DestinationExecutionStatus.RUNNING,
                started.status());
        assertEquals(startedAt, started.startedAt());
        assertNull(started.completedAt());
    }

    @Test
    void shouldCompleteRunningDestinationExecution() {
        final Instant startedAt =
                Instant.parse("2026-09-22T10:00:00Z");
        final Instant completedAt =
                Instant.parse("2026-09-22T10:01:00Z");

        final DestinationExecution execution =
                new DestinationExecution(reference())
                        .start(startedAt);

        final DestinationExecution completed =
                execution.complete(completedAt);

        assertEquals(
                DestinationExecutionStatus.COMPLETED,
                completed.status());
        assertEquals(startedAt, completed.startedAt());
        assertEquals(completedAt, completed.completedAt());
    }

    @Test
    void shouldFailRunningDestinationExecution() {
        final Instant startedAt =
                Instant.parse("2026-09-22T10:00:00Z");
        final Instant failedAt =
                Instant.parse("2026-09-22T10:01:00Z");

        final DestinationExecution execution =
                new DestinationExecution(reference())
                        .start(startedAt);

        final DestinationExecution failed =
                execution.fail(failedAt);

        assertEquals(
                DestinationExecutionStatus.FAILED,
                failed.status());
        assertEquals(startedAt, failed.startedAt());
        assertEquals(failedAt, failed.completedAt());
    }

    @Test
    void shouldCancelPendingDestinationExecution() {
        final Instant cancelledAt =
                Instant.parse("2026-09-22T10:00:00Z");

        final DestinationExecution cancelled =
                new DestinationExecution(reference())
                        .cancel(cancelledAt);

        assertEquals(
                DestinationExecutionStatus.CANCELLED,
                cancelled.status());
        assertNull(cancelled.startedAt());
        assertEquals(cancelledAt, cancelled.completedAt());
    }

    @Test
    void shouldCancelRunningDestinationExecution() {
        final Instant startedAt =
                Instant.parse("2026-09-22T10:00:00Z");
        final Instant cancelledAt =
                Instant.parse("2026-09-22T10:01:00Z");

        final DestinationExecution cancelled =
                new DestinationExecution(reference())
                        .start(startedAt)
                        .cancel(cancelledAt);

        assertEquals(
                DestinationExecutionStatus.CANCELLED,
                cancelled.status());
        assertEquals(startedAt, cancelled.startedAt());
        assertEquals(cancelledAt, cancelled.completedAt());
    }

    @Test
    void shouldRejectStartingNonPendingDestinationExecution() {
        final DestinationExecution execution =
                new DestinationExecution(reference())
                        .start(
                                Instant.parse("2026-09-22T10:00:00Z"));

        assertThrows(
                IllegalStateException.class,
                () -> execution.start(
                        Instant.parse("2026-09-22T10:01:00Z")));
    }

    @Test
    void shouldRejectCompletingNonRunningDestinationExecution() {
        final DestinationExecution execution =
                new DestinationExecution(reference());

        assertThrows(
                IllegalStateException.class,
                () -> execution.complete(
                        Instant.parse("2026-09-22T10:01:00Z")));
    }

    @Test
    void shouldRejectFailingNonRunningDestinationExecution() {
        final DestinationExecution execution =
                new DestinationExecution(reference());

        assertThrows(
                IllegalStateException.class,
                () -> execution.fail(
                        Instant.parse("2026-09-22T10:01:00Z")));
    }

    @Test
    void shouldRejectCompletionBeforeStart() {
        final DestinationExecution execution =
                new DestinationExecution(reference())
                        .start(
                                Instant.parse("2026-09-22T10:01:00Z"));

        assertThrows(
                IllegalArgumentException.class,
                () -> execution.complete(
                        Instant.parse("2026-09-22T10:00:00Z")));
    }

    @Test
    void shouldCompareDestinationExecutionsByReference() {
        final DestinationExecution first =
                new DestinationExecution(reference());

        final DestinationExecution second =
                new DestinationExecution(reference());

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    private DestinationExecutionReference reference() {
        return new DestinationExecutionReference(
                new ExecutionId("execution-001"),
                new ResourceId("resource-001"));
    }
}