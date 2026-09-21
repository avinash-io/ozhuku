package io.github.avinashio.ozhuku.domain.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.FlowId;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class FlowExecutionTest {

    @Test
    void shouldCreatePendingFlowExecution() {
        final ExecutionId executionId =
                new ExecutionId("execution-001");
        final FlowId flowId =
                new FlowId("flow-001");

        final FlowExecution execution =
                new FlowExecution(executionId, flowId);

        assertEquals(executionId, execution.executionId());
        assertEquals(flowId, execution.flowId());
        assertEquals(FlowExecutionStatus.PENDING, execution.status());
        assertNull(execution.startedAt());
        assertNull(execution.completedAt());
    }

    @Test
    void shouldStartPendingFlowExecution() {
        final Instant startedAt =
                Instant.parse("2026-09-22T10:00:00Z");

        final FlowExecution execution =
                new FlowExecution(
                        new ExecutionId("execution-001"),
                        new FlowId("flow-001"));

        final FlowExecution started =
                execution.start(startedAt);

        assertEquals(FlowExecutionStatus.RUNNING, started.status());
        assertEquals(startedAt, started.startedAt());
        assertNull(started.completedAt());
    }

    @Test
    void shouldCompleteRunningFlowExecution() {
        final Instant startedAt =
                Instant.parse("2026-09-22T10:00:00Z");
        final Instant completedAt =
                Instant.parse("2026-09-22T10:01:00Z");

        final FlowExecution execution =
                new FlowExecution(
                        new ExecutionId("execution-001"),
                        new FlowId("flow-001"))
                        .start(startedAt);

        final FlowExecution completed =
                execution.complete(completedAt);

        assertEquals(FlowExecutionStatus.COMPLETED, completed.status());
        assertEquals(startedAt, completed.startedAt());
        assertEquals(completedAt, completed.completedAt());
    }

    @Test
    void shouldFailRunningFlowExecution() {
        final Instant startedAt =
                Instant.parse("2026-09-22T10:00:00Z");
        final Instant failedAt =
                Instant.parse("2026-09-22T10:01:00Z");

        final FlowExecution execution =
                new FlowExecution(
                        new ExecutionId("execution-001"),
                        new FlowId("flow-001"))
                        .start(startedAt);

        final FlowExecution failed =
                execution.fail(failedAt);

        assertEquals(FlowExecutionStatus.FAILED, failed.status());
        assertEquals(startedAt, failed.startedAt());
        assertEquals(failedAt, failed.completedAt());
    }

    @Test
    void shouldCancelPendingFlowExecution() {
        final Instant cancelledAt =
                Instant.parse("2026-09-22T10:00:00Z");

        final FlowExecution cancelled =
                new FlowExecution(
                        new ExecutionId("execution-001"),
                        new FlowId("flow-001"))
                        .cancel(cancelledAt);

        assertEquals(FlowExecutionStatus.CANCELLED, cancelled.status());
        assertNull(cancelled.startedAt());
        assertEquals(cancelledAt, cancelled.completedAt());
    }

    @Test
    void shouldRejectStartingNonPendingFlowExecution() {
        final FlowExecution execution =
                new FlowExecution(
                        new ExecutionId("execution-001"),
                        new FlowId("flow-001"))
                        .start(
                                Instant.parse("2026-09-22T10:00:00Z"));

        assertThrows(
                IllegalStateException.class,
                () -> execution.start(
                        Instant.parse("2026-09-22T10:01:00Z")));
    }

    @Test
    void shouldRejectCompletingNonRunningFlowExecution() {
        final FlowExecution execution =
                new FlowExecution(
                        new ExecutionId("execution-001"),
                        new FlowId("flow-001"));

        assertThrows(
                IllegalStateException.class,
                () -> execution.complete(
                        Instant.parse("2026-09-22T10:01:00Z")));
    }

    @Test
    void shouldRejectCompletionBeforeStart() {
        final FlowExecution execution =
                new FlowExecution(
                        new ExecutionId("execution-001"),
                        new FlowId("flow-001"))
                        .start(
                                Instant.parse("2026-09-22T10:01:00Z"));

        assertThrows(
                IllegalArgumentException.class,
                () -> execution.complete(
                        Instant.parse("2026-09-22T10:00:00Z")));
    }

    @Test
    void shouldCompareFlowExecutionsByParentExecutionAndFlow() {
        final FlowExecution first =
                new FlowExecution(
                        new ExecutionId("execution-001"),
                        new FlowId("flow-001"));

        final FlowExecution second =
                new FlowExecution(
                        new ExecutionId("execution-001"),
                        new FlowId("flow-001"));

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldNotTreatDifferentFlowsAsEqual() {
        final FlowExecution first =
                new FlowExecution(
                        new ExecutionId("execution-001"),
                        new FlowId("flow-001"));

        final FlowExecution second =
                new FlowExecution(
                        new ExecutionId("execution-001"),
                        new FlowId("flow-002"));

        assertEquals(false, first.equals(second));
    }
}