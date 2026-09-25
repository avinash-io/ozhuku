package io.github.avinashio.ozhuku.application.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.domain.execution.FlowExecutionStatus;
import io.github.avinashio.ozhuku.domain.execution.FlowExecution;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.FlowId;
import io.github.avinashio.ozhuku.persistence.FlowExecutionRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FlowExecutionLifecycleServiceTest {

    private static final Instant FIXED_TIME =
            Instant.parse("2026-09-25T10:15:30Z");

    private static final ExecutionId EXECUTION_ID =
            new ExecutionId("execution-1");

    private static final FlowId FLOW_ID =
            new FlowId("flow-1");

    private FakeFlowExecutionRepository repository;
    private FlowExecutionLifecycleService service;

    @BeforeEach
    void setUp() {
        repository = new FakeFlowExecutionRepository();

        final Clock clock =
                Clock.fixed(FIXED_TIME, ZoneOffset.UTC);

        service = new FlowExecutionLifecycleService(
                repository,
                clock);
    }

    @Test
    void startShouldTransitionPendingToRunningAndSave() {
        final FlowExecution flowExecution =
                new FlowExecution(EXECUTION_ID, FLOW_ID);

        repository.save(flowExecution);

        final FlowExecution result =
                service.start(EXECUTION_ID, FLOW_ID);

        assertNotNull(result);
        assertEquals(
                FlowExecutionStatus.RUNNING,
                result.status());
        assertEquals(
                FIXED_TIME,
                result.startedAt());
        assertEquals(
                flowExecution,
                repository.saved());
    }

    @Test
    void completeShouldTransitionRunningToCompletedAndSave() {
        final FlowExecution flowExecution =
                new FlowExecution(EXECUTION_ID, FLOW_ID);

        repository.save(flowExecution);

        service.start(EXECUTION_ID, FLOW_ID);

        final FlowExecution result =
                service.complete(EXECUTION_ID, FLOW_ID);

        assertEquals(
                FlowExecutionStatus.COMPLETED,
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
        final FlowExecution flowExecution =
                new FlowExecution(EXECUTION_ID, FLOW_ID);

        repository.save(flowExecution);

        service.start(EXECUTION_ID, FLOW_ID);

        final FlowExecution result =
                service.fail(EXECUTION_ID, FLOW_ID);

        assertEquals(
                FlowExecutionStatus.FAILED,
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
        final FlowExecution flowExecution =
                new FlowExecution(EXECUTION_ID, FLOW_ID);

        repository.save(flowExecution);

        final FlowExecution result =
                service.cancel(EXECUTION_ID, FLOW_ID);

        assertEquals(
                FlowExecutionStatus.CANCELLED,
                result.status());
        assertEquals(
                FIXED_TIME,
                result.completedAt());
        assertEquals(
                result,
                repository.saved());
    }

    @Test
    void startShouldFailWhenFlowExecutionDoesNotExist() {
        assertThrows(
                IllegalStateException.class,
                () -> service.start(EXECUTION_ID, FLOW_ID));
    }

    @Test
    void completeShouldRejectInvalidDomainTransition() {
        final FlowExecution flowExecution =
                new FlowExecution(EXECUTION_ID, FLOW_ID);

        repository.save(flowExecution);

        assertThrows(
                IllegalStateException.class,
                () -> service.complete(EXECUTION_ID, FLOW_ID));

        assertEquals(
                flowExecution,
                repository.saved());
    }

    @Test
    void failShouldRejectInvalidDomainTransition() {
        final FlowExecution flowExecution =
                new FlowExecution(EXECUTION_ID, FLOW_ID);

        repository.save(flowExecution);

        assertThrows(
                IllegalStateException.class,
                () -> service.fail(EXECUTION_ID, FLOW_ID));

        assertEquals(
                flowExecution,
                repository.saved());
    }

    @Test
    void cancelShouldRejectNullExecutionId() {
        assertThrows(
                NullPointerException.class,
                () -> service.cancel(null, FLOW_ID));
    }

    @Test
    void cancelShouldRejectNullFlowId() {
        assertThrows(
                NullPointerException.class,
                () -> service.cancel(EXECUTION_ID, null));
    }

    private static final class FakeFlowExecutionRepository
            implements FlowExecutionRepository {

        private final Map<String, FlowExecution> executions =
                new HashMap<>();

        private FlowExecution saved;

        @Override
        public Optional<FlowExecution> findById(
                final ExecutionId executionId,
                final FlowId flowId) {

            return Optional.ofNullable(
                    executions.get(key(executionId, flowId)));
        }

        @Override
        public void save(
                final FlowExecution flowExecution) {

            saved = flowExecution;

            executions.put(
                    key(
                            flowExecution.executionId(),
                            flowExecution.flowId()),
                    flowExecution);
        }

        private FlowExecution saved() {
            return saved;
        }

        private static String key(
                final ExecutionId executionId,
                final FlowId flowId) {

            return executionId + ":" + flowId;
        }
    }
}