package io.github.avinashio.ozhuku.application.orchestration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.application.execution.DestinationExecutionLifecycleService;
import io.github.avinashio.ozhuku.application.execution.ExecutionLifecycleService;
import io.github.avinashio.ozhuku.application.execution.FlowExecutionLifecycleService;
import io.github.avinashio.ozhuku.application.execution.SourceExecutionLifecycleService;
import io.github.avinashio.ozhuku.domain.execution.DestinationExecution;
import io.github.avinashio.ozhuku.domain.execution.Execution;
import io.github.avinashio.ozhuku.domain.execution.FlowExecution;
import io.github.avinashio.ozhuku.domain.execution.SourceExecution;
import io.github.avinashio.ozhuku.domain.execution.ExecutionStatus;
import io.github.avinashio.ozhuku.domain.execution.FlowExecutionStatus;
import io.github.avinashio.ozhuku.domain.execution.SourceExecutionStatus;
import io.github.avinashio.ozhuku.domain.execution.DestinationExecutionStatus;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.FlowId;
import io.github.avinashio.ozhuku.domain.identity.PipelineId;
import io.github.avinashio.ozhuku.domain.identity.PipelineVersion;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.domain.execution.ExecutionReference;
import io.github.avinashio.ozhuku.domain.execution.SourceExecutionReference;
import io.github.avinashio.ozhuku.domain.execution.DestinationExecutionReference;
import io.github.avinashio.ozhuku.persistence.DestinationExecutionRepository;
import io.github.avinashio.ozhuku.persistence.ExecutionRepository;
import io.github.avinashio.ozhuku.persistence.FlowExecutionRepository;
import io.github.avinashio.ozhuku.persistence.SourceExecutionRepository;
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
            Instant.parse("2026-01-01T00:00:00Z");

    private static final ExecutionId EXECUTION_ID =
            new ExecutionId("execution-1");

    private static final FlowId FLOW_ID =
            new FlowId("flow-1");

    private static final ResourceId SOURCE_RESOURCE_ID =
            new ResourceId("source-1");

    private static final ResourceId DESTINATION_RESOURCE_ID =
            new ResourceId("destination-1");

    private ExecutionRepository executionRepository;
    private FlowExecutionRepository flowExecutionRepository;
    private SourceExecutionRepository sourceExecutionRepository;
    private DestinationExecutionRepository destinationExecutionRepository;

    private ExecutionOrchestrationService service;

    @BeforeEach
    void setUp() {
        executionRepository = new FakeExecutionRepository();
        flowExecutionRepository = new FakeFlowExecutionRepository();
        sourceExecutionRepository = new FakeSourceExecutionRepository();
        destinationExecutionRepository =
                new FakeDestinationExecutionRepository();

        final Clock clock =
                Clock.fixed(FIXED_TIME, ZoneOffset.UTC);

        final ExecutionLifecycleService executionLifecycleService =
                new ExecutionLifecycleService(
                        executionRepository,
                        clock);

        final FlowExecutionLifecycleService flowExecutionLifecycleService =
                new FlowExecutionLifecycleService(
                        flowExecutionRepository,
                        clock);

        final SourceExecutionLifecycleService sourceExecutionLifecycleService =
                new SourceExecutionLifecycleService(
                        sourceExecutionRepository,
                        clock);

        final DestinationExecutionLifecycleService
                destinationExecutionLifecycleService =
                new DestinationExecutionLifecycleService(
                        destinationExecutionRepository,
                        clock);

        service = new ExecutionOrchestrationService(
                executionLifecycleService,
                flowExecutionLifecycleService,
                sourceExecutionLifecycleService,
                destinationExecutionLifecycleService);
    }

    @Test
    void startExecutionStartsAllExecutionLevels() {
        savePendingExecutions();

        service.startExecution(
                EXECUTION_ID,
                FLOW_ID,
                SOURCE_RESOURCE_ID,
                DESTINATION_RESOURCE_ID);

        assertEquals(
                ExecutionStatus.RUNNING,
                execution().status());

        assertEquals(
                FlowExecutionStatus.RUNNING,
                flowExecution().status());

        assertEquals(
                SourceExecutionStatus.RUNNING,
                sourceExecution().status());

        assertEquals(
                DestinationExecutionStatus.RUNNING,
                destinationExecution().status());
    }

    @Test
    void completeExecutionCompletesAllExecutionLevels() {
        savePendingExecutions();

        service.startExecution(
                EXECUTION_ID,
                FLOW_ID,
                SOURCE_RESOURCE_ID,
                DESTINATION_RESOURCE_ID);

        service.completeExecution(
                EXECUTION_ID,
                FLOW_ID,
                SOURCE_RESOURCE_ID,
                DESTINATION_RESOURCE_ID);

        assertEquals(
                ExecutionStatus.COMPLETED,
                execution().status());

        assertEquals(
                FlowExecutionStatus.COMPLETED,
                flowExecution().status());

        assertEquals(
                SourceExecutionStatus.COMPLETED,
                sourceExecution().status());

        assertEquals(
                DestinationExecutionStatus.COMPLETED,
                destinationExecution().status());
    }

    @Test
    void failExecutionFailsAllExecutionLevels() {
        savePendingExecutions();

        service.startExecution(
                EXECUTION_ID,
                FLOW_ID,
                SOURCE_RESOURCE_ID,
                DESTINATION_RESOURCE_ID);

        service.failExecution(
                EXECUTION_ID,
                FLOW_ID,
                SOURCE_RESOURCE_ID,
                DESTINATION_RESOURCE_ID);

        assertEquals(
                ExecutionStatus.FAILED,
                execution().status());

        assertEquals(
                FlowExecutionStatus.FAILED,
                flowExecution().status());

        assertEquals(
                SourceExecutionStatus.FAILED,
                sourceExecution().status());

        assertEquals(
                DestinationExecutionStatus.FAILED,
                destinationExecution().status());
    }

    @Test
    void cancelExecutionCancelsAllExecutionLevels() {
        savePendingExecutions();

        service.cancelExecution(
                EXECUTION_ID,
                FLOW_ID,
                SOURCE_RESOURCE_ID,
                DESTINATION_RESOURCE_ID);

        assertEquals(
                ExecutionStatus.CANCELLED,
                execution().status());

        assertEquals(
                FlowExecutionStatus.CANCELLED,
                flowExecution().status());

        assertEquals(
                SourceExecutionStatus.CANCELLED,
                sourceExecution().status());

        assertEquals(
                DestinationExecutionStatus.CANCELLED,
                destinationExecution().status());
    }

    private void savePendingExecutions() {
        executionRepository.save(
                new Execution(
                        new ExecutionReference(
                                EXECUTION_ID,
                                new PipelineId("pipeline-1"),
                                new PipelineVersion(1))));

        flowExecutionRepository.save(
                new FlowExecution(
                        EXECUTION_ID,
                        FLOW_ID));

        sourceExecutionRepository.save(
                new SourceExecution(
                        new SourceExecutionReference(
                                EXECUTION_ID,
                                SOURCE_RESOURCE_ID)));

        destinationExecutionRepository.save(
                new DestinationExecution(
                        new DestinationExecutionReference(
                                EXECUTION_ID,
                                DESTINATION_RESOURCE_ID)));
    }

    private Execution execution() {
        return executionRepository.findById(EXECUTION_ID).orElseThrow();
    }

    private FlowExecution flowExecution() {
        return flowExecutionRepository
                .findById(EXECUTION_ID, FLOW_ID)
                .orElseThrow();
    }

    private SourceExecution sourceExecution() {
        return sourceExecutionRepository
                .findById(
                        EXECUTION_ID,
                        SOURCE_RESOURCE_ID)
                .orElseThrow();
    }

    private DestinationExecution destinationExecution() {
        return destinationExecutionRepository
                .findById(
                        EXECUTION_ID,
                        DESTINATION_RESOURCE_ID)
                .orElseThrow();
    }

    private static final class FakeExecutionRepository
            implements ExecutionRepository {

        private final Map<ExecutionId, Execution> executions =
                new HashMap<>();

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
                    executions.get(key(executionId, flowId)));
        }

        @Override
        public void save(final FlowExecution flowExecution) {
            executions.put(
                    key(
                            flowExecution.executionId(),
                            flowExecution.flowId()),
                    flowExecution);
        }

        private String key(
                final ExecutionId executionId,
                final FlowId flowId) {
            return executionId.value() + ":" + flowId.value();
        }
    }

    private static final class FakeSourceExecutionRepository
            implements SourceExecutionRepository {

        private final Map<String, SourceExecution> executions =
                new HashMap<>();

        @Override
        public Optional<SourceExecution> findById(
                final ExecutionId executionId,
                final ResourceId resourceId) {

            return Optional.ofNullable(
                    executions.get(key(executionId, resourceId)));
        }

        @Override
        public void save(final SourceExecution sourceExecution) {
            executions.put(
                    key(
                            sourceExecution.reference().executionId(),
                            sourceExecution.reference().resourceId()),
                    sourceExecution);
        }

        private String key(
                final ExecutionId executionId,
                final ResourceId resourceId) {
            return executionId.value()
                    + ":"
                    + resourceId.value();
        }
    }

    private static final class FakeDestinationExecutionRepository
            implements DestinationExecutionRepository {

        private final Map<String, DestinationExecution> executions =
                new HashMap<>();

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

            executions.put(
                    key(
                            destinationExecution.reference().executionId(),
                            destinationExecution.reference().resourceId()),
                    destinationExecution);
        }

        private String key(
                final ExecutionId executionId,
                final ResourceId resourceId) {
            return executionId.value()
                    + ":"
                    + resourceId.value();
        }
    }
}