package io.github.avinashio.ozhuku.application.initialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.domain.execution.DestinationExecution;
import io.github.avinashio.ozhuku.domain.execution.DestinationExecutionReference;
import io.github.avinashio.ozhuku.domain.execution.DestinationExecutionStatus;
import io.github.avinashio.ozhuku.domain.execution.Execution;
import io.github.avinashio.ozhuku.domain.execution.ExecutionReference;
import io.github.avinashio.ozhuku.domain.execution.ExecutionStatus;
import io.github.avinashio.ozhuku.domain.execution.FlowExecution;
import io.github.avinashio.ozhuku.domain.execution.FlowExecutionStatus;
import io.github.avinashio.ozhuku.domain.execution.SourceExecution;
import io.github.avinashio.ozhuku.domain.execution.SourceExecutionReference;
import io.github.avinashio.ozhuku.domain.execution.SourceExecutionStatus;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.FlowId;
import io.github.avinashio.ozhuku.domain.identity.PipelineId;
import io.github.avinashio.ozhuku.domain.identity.PipelineVersion;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.persistence.DestinationExecutionRepository;
import io.github.avinashio.ozhuku.persistence.ExecutionRepository;
import io.github.avinashio.ozhuku.persistence.FlowExecutionRepository;
import io.github.avinashio.ozhuku.persistence.SourceExecutionRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExecutionInitializationServiceTest {

    private static final ExecutionId EXECUTION_ID =
            new ExecutionId("execution-1");

    private static final FlowId FLOW_ID =
            new FlowId("flow-1");

    private static final ResourceId SOURCE_RESOURCE_ID =
            new ResourceId("source-1");

    private static final ResourceId DESTINATION_RESOURCE_ID =
            new ResourceId("destination-1");

    private ExecutionReference executionReference;

    private FakeExecutionRepository executionRepository;
    private FakeFlowExecutionRepository flowExecutionRepository;
    private FakeSourceExecutionRepository sourceExecutionRepository;
    private FakeDestinationExecutionRepository destinationExecutionRepository;

    private ExecutionInitializationService service;

    @BeforeEach
    void setUp() {
        executionReference = createExecutionReference();

        executionRepository =
                new FakeExecutionRepository();

        flowExecutionRepository =
                new FakeFlowExecutionRepository();

        sourceExecutionRepository =
                new FakeSourceExecutionRepository();

        destinationExecutionRepository =
                new FakeDestinationExecutionRepository();

        service = new ExecutionInitializationService(
                executionRepository,
                flowExecutionRepository,
                sourceExecutionRepository,
                destinationExecutionRepository);
    }

    @Test
    void initializeShouldCreateAllExecutionsAsPending() {
        service.initialize(
                executionReference,
                FLOW_ID,
                SOURCE_RESOURCE_ID,
                DESTINATION_RESOURCE_ID);

        final Execution execution =
                executionRepository
                        .findById(EXECUTION_ID)
                        .orElseThrow();

        assertEquals(
                ExecutionStatus.PENDING,
                execution.status());

        assertNull(execution.startedAt());
        assertNull(execution.completedAt());

        final FlowExecution flowExecution =
                flowExecutionRepository
                        .findById(
                                EXECUTION_ID,
                                FLOW_ID)
                        .orElseThrow();

        assertEquals(
                FlowExecutionStatus.PENDING,
                flowExecution.status());

        assertNull(flowExecution.startedAt());
        assertNull(flowExecution.completedAt());

        final SourceExecution sourceExecution =
                sourceExecutionRepository
                        .findById(
                                EXECUTION_ID,
                                SOURCE_RESOURCE_ID)
                        .orElseThrow();

        assertEquals(
                SourceExecutionStatus.PENDING,
                sourceExecution.status());

        assertNull(sourceExecution.startedAt());
        assertNull(sourceExecution.completedAt());

        final DestinationExecution destinationExecution =
                destinationExecutionRepository
                        .findById(
                                EXECUTION_ID,
                                DESTINATION_RESOURCE_ID)
                        .orElseThrow();

        assertEquals(
                DestinationExecutionStatus.PENDING,
                destinationExecution.status());

        assertNull(destinationExecution.startedAt());
        assertNull(destinationExecution.completedAt());
    }

    @Test
    void initializeShouldPreserveExecutionIdentity() {
        service.initialize(
                executionReference,
                FLOW_ID,
                SOURCE_RESOURCE_ID,
                DESTINATION_RESOURCE_ID);

        final Execution execution =
                executionRepository
                        .findById(EXECUTION_ID)
                        .orElseThrow();

        assertEquals(
                executionReference,
                execution.reference());

        final FlowExecution flowExecution =
                flowExecutionRepository
                        .findById(
                                EXECUTION_ID,
                                FLOW_ID)
                        .orElseThrow();

        assertEquals(
                EXECUTION_ID,
                flowExecution.executionId());

        assertEquals(
                FLOW_ID,
                flowExecution.flowId());

        final SourceExecution sourceExecution =
                sourceExecutionRepository
                        .findById(
                                EXECUTION_ID,
                                SOURCE_RESOURCE_ID)
                        .orElseThrow();

        assertEquals(
                new SourceExecutionReference(
                        EXECUTION_ID,
                        SOURCE_RESOURCE_ID),
                sourceExecution.reference());

        final DestinationExecution destinationExecution =
                destinationExecutionRepository
                        .findById(
                                EXECUTION_ID,
                                DESTINATION_RESOURCE_ID)
                        .orElseThrow();

        assertEquals(
                new DestinationExecutionReference(
                        EXECUTION_ID,
                        DESTINATION_RESOURCE_ID),
                destinationExecution.reference());
    }

    @Test
    void initializeShouldRejectExistingExecution() {
        executionRepository.save(
                new Execution(executionReference));

        assertThrows(
                IllegalStateException.class,
                () -> service.initialize(
                        executionReference,
                        FLOW_ID,
                        SOURCE_RESOURCE_ID,
                        DESTINATION_RESOURCE_ID));

        assertEquals(
                1,
                executionRepository.saveCount);

        assertEquals(
                0,
                flowExecutionRepository.saveCount);

        assertEquals(
                0,
                sourceExecutionRepository.saveCount);

        assertEquals(
                0,
                destinationExecutionRepository.saveCount);
    }

    @Test
    void initializeShouldRejectExistingFlowExecution() {
        flowExecutionRepository.save(
                new FlowExecution(
                        EXECUTION_ID,
                        FLOW_ID));

        assertThrows(
                IllegalStateException.class,
                () -> service.initialize(
                        executionReference,
                        FLOW_ID,
                        SOURCE_RESOURCE_ID,
                        DESTINATION_RESOURCE_ID));

        assertEquals(
                0,
                executionRepository.saveCount);

        assertEquals(
                1,
                flowExecutionRepository.saveCount);

        assertEquals(
                0,
                sourceExecutionRepository.saveCount);

        assertEquals(
                0,
                destinationExecutionRepository.saveCount);
    }

    @Test
    void initializeShouldRejectExistingSourceExecution() {
        sourceExecutionRepository.save(
                new SourceExecution(
                        new SourceExecutionReference(
                                EXECUTION_ID,
                                SOURCE_RESOURCE_ID)));

        assertThrows(
                IllegalStateException.class,
                () -> service.initialize(
                        executionReference,
                        FLOW_ID,
                        SOURCE_RESOURCE_ID,
                        DESTINATION_RESOURCE_ID));

        assertEquals(
                0,
                executionRepository.saveCount);

        assertEquals(
                0,
                flowExecutionRepository.saveCount);

        assertEquals(
                1,
                sourceExecutionRepository.saveCount);

        assertEquals(
                0,
                destinationExecutionRepository.saveCount);
    }

    @Test
    void initializeShouldRejectExistingDestinationExecution() {
        destinationExecutionRepository.save(
                new DestinationExecution(
                        new DestinationExecutionReference(
                                EXECUTION_ID,
                                DESTINATION_RESOURCE_ID)));

        assertThrows(
                IllegalStateException.class,
                () -> service.initialize(
                        executionReference,
                        FLOW_ID,
                        SOURCE_RESOURCE_ID,
                        DESTINATION_RESOURCE_ID));

        assertEquals(
                0,
                executionRepository.saveCount);

        assertEquals(
                0,
                flowExecutionRepository.saveCount);

        assertEquals(
                0,
                sourceExecutionRepository.saveCount);

        assertEquals(
                1,
                destinationExecutionRepository.saveCount);
    }

    @Test
    void initializeShouldRejectNullExecutionReference() {
        assertThrows(
                NullPointerException.class,
                () -> service.initialize(
                        null,
                        FLOW_ID,
                        SOURCE_RESOURCE_ID,
                        DESTINATION_RESOURCE_ID));

        assertNothingWasSaved();
    }

    @Test
    void initializeShouldRejectNullFlowId() {
        assertThrows(
                NullPointerException.class,
                () -> service.initialize(
                        executionReference,
                        null,
                        SOURCE_RESOURCE_ID,
                        DESTINATION_RESOURCE_ID));

        assertNothingWasSaved();
    }

    @Test
    void initializeShouldRejectNullSourceResourceId() {
        assertThrows(
                NullPointerException.class,
                () -> service.initialize(
                        executionReference,
                        FLOW_ID,
                        null,
                        DESTINATION_RESOURCE_ID));

        assertNothingWasSaved();
    }

    @Test
    void initializeShouldRejectNullDestinationResourceId() {
        assertThrows(
                NullPointerException.class,
                () -> service.initialize(
                        executionReference,
                        FLOW_ID,
                        SOURCE_RESOURCE_ID,
                        null));

        assertNothingWasSaved();
    }

    private void assertNothingWasSaved() {
        assertEquals(
                0,
                executionRepository.saveCount);

        assertEquals(
                0,
                flowExecutionRepository.saveCount);

        assertEquals(
                0,
                sourceExecutionRepository.saveCount);

        assertEquals(
                0,
                destinationExecutionRepository.saveCount);
    }

    private static ExecutionReference createExecutionReference() {
        return new ExecutionReference(
                EXECUTION_ID,
                new PipelineId("pipeline-1"),
                new PipelineVersion(1));
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
        public void save(final Execution execution) {
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

        private int saveCount;

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

            saveCount++;

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

    private static final class FakeSourceExecutionRepository
            implements SourceExecutionRepository {

        private final Map<String, SourceExecution> executions =
                new HashMap<>();

        private int saveCount;

        @Override
        public Optional<SourceExecution> findById(
                final ExecutionId executionId,
                final ResourceId resourceId) {

            return Optional.ofNullable(
                    executions.get(
                            key(
                                    executionId,
                                    resourceId)));
        }

        @Override
        public void save(
                final SourceExecution sourceExecution) {

            saveCount++;

            executions.put(
                    key(
                            sourceExecution.reference().executionId(),
                            sourceExecution.reference().resourceId()),
                    sourceExecution);
        }

        private static String key(
                final ExecutionId executionId,
                final ResourceId resourceId) {

            return executionId + ":" + resourceId;
        }
    }

    private static final class FakeDestinationExecutionRepository
            implements DestinationExecutionRepository {

        private final Map<String, DestinationExecution> executions =
                new HashMap<>();

        private int saveCount;

        @Override
        public Optional<DestinationExecution> findById(
                final ExecutionId executionId,
                final ResourceId resourceId) {

            return Optional.ofNullable(
                    executions.get(
                            key(
                                    executionId,
                                    resourceId)));
        }

        @Override
        public void save(
                final DestinationExecution destinationExecution) {

            saveCount++;

            executions.put(
                    key(
                            destinationExecution.reference().executionId(),
                            destinationExecution.reference().resourceId()),
                    destinationExecution);
        }

        private static String key(
                final ExecutionId executionId,
                final ResourceId resourceId) {

            return executionId + ":" + resourceId;
        }
    }
}