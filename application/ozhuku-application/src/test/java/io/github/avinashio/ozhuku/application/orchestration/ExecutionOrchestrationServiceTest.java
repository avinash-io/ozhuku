package io.github.avinashio.ozhuku.application.orchestration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.application.execution.ExecutionLifecycleService;
import io.github.avinashio.ozhuku.application.execution.FlowExecutionLifecycleService;
import io.github.avinashio.ozhuku.domain.execution.Execution;
import io.github.avinashio.ozhuku.domain.execution.ExecutionStatus;
import io.github.avinashio.ozhuku.domain.execution.FlowExecution;
import io.github.avinashio.ozhuku.domain.execution.FlowExecutionStatus;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.FlowId;
import io.github.avinashio.ozhuku.persistence.ExecutionRepository;
import io.github.avinashio.ozhuku.persistence.FlowExecutionRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExecutionOrchestrationServiceTest {

    private static final Instant FIXED_TIME =
            Instant.parse("2026-09-25T10:15:30Z");

    private static final ExecutionId EXECUTION_ID =
            new ExecutionId("execution-1");

    private static final FlowId FLOW_ID =
            new FlowId("flow-1");

    private FakeExecutionRepository executionRepository;
    private FakeFlowExecutionRepository flowExecutionRepository;
    private ExecutionOrchestrationService service;

    @BeforeEach
    void setUp() {
        executionRepository =
                new FakeExecutionRepository();

        flowExecutionRepository =
                new FakeFlowExecutionRepository();

        final Clock clock =
                Clock.fixed(
                        FIXED_TIME,
                        ZoneOffset.UTC);

        final ExecutionLifecycleService executionLifecycleService =
                new ExecutionLifecycleService(
                        executionRepository,
                        clock);

        final FlowExecutionLifecycleService flowExecutionLifecycleService =
                new FlowExecutionLifecycleService(
                        flowExecutionRepository,
                        clock);

        service = new ExecutionOrchestrationService(
                executionLifecycleService,
                flowExecutionLifecycleService);
    }

    @Test
    void startExecutionShouldStartExecutionBeforeFlowExecution() {
        final Execution execution =
                new Execution(
                        createExecutionReference());

        final FlowExecution flowExecution =
                new FlowExecution(
                        EXECUTION_ID,
                        FLOW_ID);

        executionRepository.save(execution);
        flowExecutionRepository.save(flowExecution);

        final FlowExecution result =
                service.startExecution(
                        EXECUTION_ID,
                        FLOW_ID);

        assertNotNull(result);

        assertEquals(
                FlowExecutionStatus.RUNNING,
                result.status());

        assertEquals(
                FIXED_TIME,
                result.startedAt());

        final Execution savedExecution =
                executionRepository
                        .findById(EXECUTION_ID)
                        .orElseThrow();

        assertEquals(
                ExecutionStatus.RUNNING,
                savedExecution.status());

        assertEquals(
                FIXED_TIME,
                savedExecution.startedAt());

        final FlowExecution savedFlowExecution =
                flowExecutionRepository
                        .findById(
                                EXECUTION_ID,
                                FLOW_ID)
                        .orElseThrow();

        assertEquals(
                FlowExecutionStatus.RUNNING,
                savedFlowExecution.status());

        assertEquals(
                FIXED_TIME,
                savedFlowExecution.startedAt());
    }

    @Test
    void startExecutionShouldReturnStartedFlowExecution() {
        final Execution execution =
                new Execution(
                        createExecutionReference());

        final FlowExecution flowExecution =
                new FlowExecution(
                        EXECUTION_ID,
                        FLOW_ID);

        executionRepository.save(execution);
        flowExecutionRepository.save(flowExecution);

        final FlowExecution result =
                service.startExecution(
                        EXECUTION_ID,
                        FLOW_ID);

        assertEquals(
                flowExecution.executionId(),
                result.executionId());

        assertEquals(
                flowExecution.flowId(),
                result.flowId());

        assertEquals(
                FlowExecutionStatus.RUNNING,
                result.status());
    }

    @Test
    void startExecutionShouldFailWhenExecutionDoesNotExist() {
        final FlowExecution flowExecution =
                new FlowExecution(
                        EXECUTION_ID,
                        FLOW_ID);

        flowExecutionRepository.save(flowExecution);

        assertThrows(
                IllegalStateException.class,
                () -> service.startExecution(
                        EXECUTION_ID,
                        FLOW_ID));

        assertEquals(
                FlowExecutionStatus.PENDING,
                flowExecutionRepository
                        .findById(
                                EXECUTION_ID,
                                FLOW_ID)
                        .orElseThrow()
                        .status());
    }

    @Test
    void startExecutionShouldFailExecutionWhenFlowExecutionDoesNotExist() {
        final Execution execution =
                new Execution(
                        createExecutionReference());

        executionRepository.save(execution);

        assertThrows(
                IllegalStateException.class,
                () -> service.startExecution(
                        EXECUTION_ID,
                        FLOW_ID));

        final Execution savedExecution =
                executionRepository
                        .findById(EXECUTION_ID)
                        .orElseThrow();

        assertEquals(
                ExecutionStatus.FAILED,
                savedExecution.status());

        assertEquals(
                FIXED_TIME,
                savedExecution.completedAt());

        assertEquals(
                Optional.empty(),
                flowExecutionRepository.findById(
                        EXECUTION_ID,
                        FLOW_ID));
    }

    @Test
    void startExecutionShouldRejectNullExecutionId() {
        assertThrows(
                NullPointerException.class,
                () -> service.startExecution(
                        null,
                        FLOW_ID));

        assertEquals(
                0,
                executionRepository.saveCount);
    }

    @Test
    void startExecutionShouldRejectNullFlowId() {
        assertThrows(
                NullPointerException.class,
                () -> service.startExecution(
                        EXECUTION_ID,
                        null));

        assertEquals(
                0,
                executionRepository.saveCount);
    }

    @Test
    void startExecutionShouldFailExecutionWhenFlowStartFails() {
        final Execution execution =
                new Execution(
                        createExecutionReference());

        final FlowExecution flowExecution =
                new FlowExecution(
                        EXECUTION_ID,
                        FLOW_ID);

        executionRepository.save(execution);
        flowExecutionRepository.save(flowExecution);

        final ExecutionOrchestrationService failingService =
                new ExecutionOrchestrationService(
                        new ExecutionLifecycleService(
                                executionRepository,
                                Clock.fixed(
                                        FIXED_TIME,
                                        ZoneOffset.UTC)),
                        new FlowExecutionLifecycleService(
                                new FlowExecutionRepository() {
                                    @Override
                                    public Optional<FlowExecution> findById(
                                            final ExecutionId executionId,
                                            final FlowId flowId) {
                                        return Optional.of(flowExecution);
                                    }

                                    @Override
                                    public void save(
                                            final FlowExecution execution) {
                                        throw new IllegalStateException(
                                                "Flow execution start failed");
                                    }
                                },
                                Clock.fixed(
                                        FIXED_TIME,
                                        ZoneOffset.UTC)));

        assertThrows(
                IllegalStateException.class,
                () -> failingService.startExecution(
                        EXECUTION_ID,
                        FLOW_ID));

        final Execution savedExecution =
                executionRepository
                        .findById(EXECUTION_ID)
                        .orElseThrow();

        assertEquals(
                ExecutionStatus.FAILED,
                savedExecution.status());

        assertEquals(
                FIXED_TIME,
                savedExecution.completedAt());
    }

    @Test
    void completeExecutionShouldCompleteFlowBeforeExecution() {
        final Execution execution =
                new Execution(
                        createExecutionReference());

        final FlowExecution flowExecution =
                new FlowExecution(
                        EXECUTION_ID,
                        FLOW_ID);

        executionRepository.save(execution);
        flowExecutionRepository.save(flowExecution);

        service.startExecution(
                EXECUTION_ID,
                FLOW_ID);

        final FlowExecution result =
                service.completeExecution(
                        EXECUTION_ID,
                        FLOW_ID);

        assertNotNull(result);

        assertEquals(
                FlowExecutionStatus.COMPLETED,
                result.status());

        assertEquals(
                FIXED_TIME,
                result.completedAt());

        final Execution savedExecution =
                executionRepository
                        .findById(EXECUTION_ID)
                        .orElseThrow();

        assertEquals(
                ExecutionStatus.COMPLETED,
                savedExecution.status());

        assertEquals(
                FIXED_TIME,
                savedExecution.completedAt());

        final FlowExecution savedFlowExecution =
                flowExecutionRepository
                        .findById(
                                EXECUTION_ID,
                                FLOW_ID)
                        .orElseThrow();

        assertEquals(
                FlowExecutionStatus.COMPLETED,
                savedFlowExecution.status());
    }

    @Test
    void completeExecutionShouldFailWhenFlowIsNotRunning() {
        final Execution execution =
                new Execution(
                        createExecutionReference());

        final FlowExecution flowExecution =
                new FlowExecution(
                        EXECUTION_ID,
                        FLOW_ID);

        executionRepository.save(execution);
        flowExecutionRepository.save(flowExecution);

        service.startExecution(
                EXECUTION_ID,
                FLOW_ID);

        service.completeExecution(
                EXECUTION_ID,
                FLOW_ID);

        assertThrows(
                IllegalStateException.class,
                () -> service.completeExecution(
                        EXECUTION_ID,
                        FLOW_ID));
    }

    @Test
    void failExecutionShouldFailFlowBeforeExecution() {
        final Execution execution =
                new Execution(
                        createExecutionReference());

        final FlowExecution flowExecution =
                new FlowExecution(
                        EXECUTION_ID,
                        FLOW_ID);

        executionRepository.save(execution);
        flowExecutionRepository.save(flowExecution);

        service.startExecution(
                EXECUTION_ID,
                FLOW_ID);

        final FlowExecution result =
                service.failExecution(
                        EXECUTION_ID,
                        FLOW_ID);

        assertNotNull(result);

        assertEquals(
                FlowExecutionStatus.FAILED,
                result.status());

        assertEquals(
                FIXED_TIME,
                result.completedAt());

        final Execution savedExecution =
                executionRepository
                        .findById(EXECUTION_ID)
                        .orElseThrow();

        assertEquals(
                ExecutionStatus.FAILED,
                savedExecution.status());

        assertEquals(
                FIXED_TIME,
                savedExecution.completedAt());
    }

    @Test
    void failExecutionShouldRejectFlowThatIsNotRunning() {
        final Execution execution =
                new Execution(
                        createExecutionReference());

        final FlowExecution flowExecution =
                new FlowExecution(
                        EXECUTION_ID,
                        FLOW_ID);

        executionRepository.save(execution);
        flowExecutionRepository.save(flowExecution);

        assertThrows(
                IllegalStateException.class,
                () -> service.failExecution(
                        EXECUTION_ID,
                        FLOW_ID));

        assertEquals(
                ExecutionStatus.PENDING,
                executionRepository
                        .findById(EXECUTION_ID)
                        .orElseThrow()
                        .status());

        assertEquals(
                FlowExecutionStatus.PENDING,
                flowExecutionRepository
                        .findById(
                                EXECUTION_ID,
                                FLOW_ID)
                        .orElseThrow()
                        .status());
    }

    @Test
    void cancelExecutionShouldCancelFlowBeforeExecution() {
        final Execution execution =
                new Execution(
                        createExecutionReference());

        final FlowExecution flowExecution =
                new FlowExecution(
                        EXECUTION_ID,
                        FLOW_ID);

        executionRepository.save(execution);
        flowExecutionRepository.save(flowExecution);

        service.startExecution(
                EXECUTION_ID,
                FLOW_ID);

        final FlowExecution result =
                service.cancelExecution(
                        EXECUTION_ID,
                        FLOW_ID);

        assertNotNull(result);

        assertEquals(
                FlowExecutionStatus.CANCELLED,
                result.status());

        assertEquals(
                FIXED_TIME,
                result.completedAt());

        final Execution savedExecution =
                executionRepository
                        .findById(EXECUTION_ID)
                        .orElseThrow();

        assertEquals(
                ExecutionStatus.CANCELLED,
                savedExecution.status());

        assertEquals(
                FIXED_TIME,
                savedExecution.completedAt());

        final FlowExecution savedFlowExecution =
                flowExecutionRepository
                        .findById(
                                EXECUTION_ID,
                                FLOW_ID)
                        .orElseThrow();

        assertEquals(
                FlowExecutionStatus.CANCELLED,
                savedFlowExecution.status());
    }

    @Test
    void cancelExecutionShouldCancelPendingFlowAndExecution() {
        final Execution execution =
                new Execution(
                        createExecutionReference());

        final FlowExecution flowExecution =
                new FlowExecution(
                        EXECUTION_ID,
                        FLOW_ID);

        executionRepository.save(execution);
        flowExecutionRepository.save(flowExecution);

        final FlowExecution result =
                service.cancelExecution(
                        EXECUTION_ID,
                        FLOW_ID);

        assertNotNull(result);

        assertEquals(
                FlowExecutionStatus.CANCELLED,
                result.status());

        assertEquals(
                FIXED_TIME,
                result.completedAt());

        final Execution savedExecution =
                executionRepository
                        .findById(EXECUTION_ID)
                        .orElseThrow();

        assertEquals(
                ExecutionStatus.CANCELLED,
                savedExecution.status());

        assertEquals(
                FIXED_TIME,
                savedExecution.completedAt());

        final FlowExecution savedFlowExecution =
                flowExecutionRepository
                        .findById(
                                EXECUTION_ID,
                                FLOW_ID)
                        .orElseThrow();

        assertEquals(
                FlowExecutionStatus.CANCELLED,
                savedFlowExecution.status());
    }

    private static io.github.avinashio.ozhuku.domain.execution.ExecutionReference
    createExecutionReference() {

        return new io.github.avinashio.ozhuku.domain.execution.ExecutionReference(
                EXECUTION_ID,
                new io.github.avinashio.ozhuku.domain.identity.PipelineId(
                        "pipeline-1"),
                new io.github.avinashio.ozhuku.domain.identity.PipelineVersion(
                        1));
    }

    private static final class FakeExecutionRepository
            implements ExecutionRepository {

        private final Map<ExecutionId, Execution> executions =
                new HashMap<>();

        private int saveCount;

        @Override
        public Optional<Execution> findById(
                final ExecutionId executionId) {

            return Optional.ofNullable(
                    executions.get(executionId));
        }

        @Override
        public void save(
                final Execution execution) {

            saveCount++;

            executions.put(
                    execution.reference().executionId(),
                    execution);
        }
    }

    private static final class FakeFlowExecutionRepository
            implements FlowExecutionRepository {

        private final Map<String, FlowExecution> executions =
                new HashMap<>();

        @Override
        public Optional<FlowExecution> findById(
                final ExecutionId executionId,
                final FlowId flowId) {

            return Optional.ofNullable(
                    executions.get(
                            key(
                                    executionId,
                                    flowId)));
        }

        @Override
        public void save(
                final FlowExecution flowExecution) {

            executions.put(
                    key(
                            flowExecution.executionId(),
                            flowExecution.flowId()),
                    flowExecution);
        }

        private static String key(
                final ExecutionId executionId,
                final FlowId flowId) {

            return executionId + ":" + flowId;
        }
    }
}