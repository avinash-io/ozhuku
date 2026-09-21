package io.github.avinashio.ozhuku.domain.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.PipelineId;
import io.github.avinashio.ozhuku.domain.identity.PipelineVersion;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ExecutionTest {

    @Test
    void shouldCreatePendingExecution() {
        final ExecutionReference reference = reference();

        final Execution execution = new Execution(reference);

        assertEquals(reference, execution.reference());
        assertEquals(ExecutionStatus.PENDING, execution.status());
        assertNull(execution.startedAt());
        assertNull(execution.completedAt());
    }

    @Test
    void shouldStartPendingExecution() {
        final Execution execution = new Execution(reference());
        final Instant startedAt = Instant.parse("2026-09-22T10:00:00Z");

        final Execution started = execution.start(startedAt);

        assertEquals(ExecutionStatus.RUNNING, started.status());
        assertEquals(startedAt, started.startedAt());
        assertNull(started.completedAt());
    }

    @Test
    void shouldCompleteRunningExecution() {
        final Instant startedAt = Instant.parse("2026-09-22T10:00:00Z");
        final Instant completedAt = Instant.parse("2026-09-22T10:01:00Z");

        final Execution execution =
                new Execution(reference()).start(startedAt);

        final Execution completed = execution.complete(completedAt);

        assertEquals(ExecutionStatus.COMPLETED, completed.status());
        assertEquals(startedAt, completed.startedAt());
        assertEquals(completedAt, completed.completedAt());
    }

    @Test
    void shouldFailRunningExecution() {
        final Instant startedAt = Instant.parse("2026-09-22T10:00:00Z");
        final Instant failedAt = Instant.parse("2026-09-22T10:01:00Z");

        final Execution execution =
                new Execution(reference()).start(startedAt);

        final Execution failed = execution.fail(failedAt);

        assertEquals(ExecutionStatus.FAILED, failed.status());
        assertEquals(startedAt, failed.startedAt());
        assertEquals(failedAt, failed.completedAt());
    }

    @Test
    void shouldCancelPendingExecution() {
        final Instant cancelledAt =
                Instant.parse("2026-09-22T10:00:00Z");

        final Execution cancelled =
                new Execution(reference()).cancel(cancelledAt);

        assertEquals(ExecutionStatus.CANCELLED, cancelled.status());
        assertNull(cancelled.startedAt());
        assertEquals(cancelledAt, cancelled.completedAt());
    }

    @Test
    void shouldCancelRunningExecution() {
        final Instant startedAt =
                Instant.parse("2026-09-22T10:00:00Z");
        final Instant cancelledAt =
                Instant.parse("2026-09-22T10:01:00Z");

        final Execution cancelled =
                new Execution(reference())
                        .start(startedAt)
                        .cancel(cancelledAt);

        assertEquals(ExecutionStatus.CANCELLED, cancelled.status());
        assertEquals(startedAt, cancelled.startedAt());
        assertEquals(cancelledAt, cancelled.completedAt());
    }

    @Test
    void shouldRejectStartingNonPendingExecution() {
        final Execution execution =
                new Execution(reference())
                        .start(Instant.parse("2026-09-22T10:00:00Z"));

        assertThrows(
                IllegalStateException.class,
                () -> execution.start(
                        Instant.parse("2026-09-22T10:01:00Z")));
    }

    @Test
    void shouldRejectCompletingNonRunningExecution() {
        final Execution execution = new Execution(reference());

        assertThrows(
                IllegalStateException.class,
                () -> execution.complete(
                        Instant.parse("2026-09-22T10:01:00Z")));
    }

    @Test
    void shouldRejectFailingNonRunningExecution() {
        final Execution execution = new Execution(reference());

        assertThrows(
                IllegalStateException.class,
                () -> execution.fail(
                        Instant.parse("2026-09-22T10:01:00Z")));
    }

    @Test
    void shouldRejectCompletionBeforeStart() {
        final Instant startedAt =
                Instant.parse("2026-09-22T10:01:00Z");
        final Instant completedAt =
                Instant.parse("2026-09-22T10:00:00Z");

        final Execution execution =
                new Execution(reference()).start(startedAt);

        assertThrows(
                IllegalArgumentException.class,
                () -> execution.complete(completedAt));
    }

    @Test
    void shouldRejectNullReference() {
        assertThrows(
                RuntimeException.class,
                () -> new Execution(null));
    }

    private ExecutionReference reference() {
        return new ExecutionReference(
                new ExecutionId("execution-001"),
                new PipelineId("pipeline-001"),
                new PipelineVersion(1));
    }
}