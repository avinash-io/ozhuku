package io.github.avinashio.ozhuku.domain.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class SourceExecutionTest {

    @Test
    void shouldCreatePendingSourceExecution() {
        final SourceExecutionReference reference =
                new SourceExecutionReference(
                        new ExecutionId("execution-001"),
                        new ResourceId("resource-001"));

        final SourceExecution execution =
                new SourceExecution(reference);

        assertEquals(reference, execution.reference());
        assertEquals(
                SourceExecutionStatus.PENDING,
                execution.status());
        assertNull(execution.startedAt());
        assertNull(execution.completedAt());
    }

    @Test
    void shouldStartPendingSourceExecution() {
        final Instant startedAt =
                Instant.parse("2026-09-22T10:00:00Z");

        final SourceExecution execution =
                new SourceExecution(reference());

        final SourceExecution started =
                execution.start(startedAt);

        assertEquals(SourceExecutionStatus.RUNNING, started.status());
        assertEquals(startedAt, started.startedAt());
        assertNull(started.completedAt());
    }

    @Test
    void shouldCompleteRunningSourceExecution() {
        final Instant startedAt =
                Instant.parse("2026-09-22T10:00:00Z");
        final Instant completedAt =
                Instant.parse("2026-09-22T10:01:00Z");

        final SourceExecution execution =
                new SourceExecution(reference())
                        .start(startedAt);

        final SourceExecution completed =
                execution.complete(completedAt);

        assertEquals(
                SourceExecutionStatus.COMPLETED,
                completed.status());
        assertEquals(startedAt, completed.startedAt());
        assertEquals(completedAt, completed.completedAt());
    }

    @Test
    void shouldFailRunningSourceExecution() {
        final Instant startedAt =
                Instant.parse("2026-09-22T10:00:00Z");
        final Instant failedAt =
                Instant.parse("2026-09-22T10:01:00Z");

        final SourceExecution execution =
                new SourceExecution(reference())
                        .start(startedAt);

        final SourceExecution failed =
                execution.fail(failedAt);

        assertEquals(
                SourceExecutionStatus.FAILED,
                failed.status());
        assertEquals(startedAt, failed.startedAt());
        assertEquals(failedAt, failed.completedAt());
    }

    @Test
    void shouldCancelPendingSourceExecution() {
        final Instant cancelledAt =
                Instant.parse("2026-09-22T10:00:00Z");

        final SourceExecution cancelled =
                new SourceExecution(reference())
                        .cancel(cancelledAt);

        assertEquals(
                SourceExecutionStatus.CANCELLED,
                cancelled.status());
        assertNull(cancelled.startedAt());
        assertEquals(cancelledAt, cancelled.completedAt());
    }

    @Test
    void shouldRejectStartingNonPendingSourceExecution() {
        final SourceExecution execution =
                new SourceExecution(reference())
                        .start(
                                Instant.parse("2026-09-22T10:00:00Z"));

        assertThrows(
                IllegalStateException.class,
                () -> execution.start(
                        Instant.parse("2026-09-22T10:01:00Z")));
    }

    @Test
    void shouldRejectCompletingNonRunningSourceExecution() {
        final SourceExecution execution =
                new SourceExecution(reference());

        assertThrows(
                IllegalStateException.class,
                () -> execution.complete(
                        Instant.parse("2026-09-22T10:01:00Z")));
    }

    @Test
    void shouldRejectFailingNonRunningSourceExecution() {
        final SourceExecution execution =
                new SourceExecution(reference());

        assertThrows(
                IllegalStateException.class,
                () -> execution.fail(
                        Instant.parse("2026-09-22T10:01:00Z")));
    }

    @Test
    void shouldRejectCompletionBeforeStart() {
        final SourceExecution execution =
                new SourceExecution(reference())
                        .start(
                                Instant.parse("2026-09-22T10:01:00Z"));

        assertThrows(
                IllegalArgumentException.class,
                () -> execution.complete(
                        Instant.parse("2026-09-22T10:00:00Z")));
    }

    @Test
    void shouldCompareSourceExecutionsByReference() {
        final SourceExecution first =
                new SourceExecution(reference());

        final SourceExecution second =
                new SourceExecution(reference());

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    private SourceExecutionReference reference() {
        return new SourceExecutionReference(
                new ExecutionId("execution-001"),
                new ResourceId("resource-001"));
    }
}