package io.github.avinashio.ozhuku.application.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.application.deduplication.DeduplicationService;
import io.github.avinashio.ozhuku.application.deduplication.ExecutionDeduplicationService;
import io.github.avinashio.ozhuku.application.execution.*;
import io.github.avinashio.ozhuku.application.initialization.ExecutionInitializationService;
import io.github.avinashio.ozhuku.application.orchestration.ExecutionOrchestrationService;
import io.github.avinashio.ozhuku.application.processing.ExecutionProcessingService;
import io.github.avinashio.ozhuku.application.record.RecordProcessingService;
import io.github.avinashio.ozhuku.application.source.SourceFingerprintService;
import io.github.avinashio.ozhuku.application.source.SourceIdentityService;
import io.github.avinashio.ozhuku.application.transfer.ResourceTransferService;
import io.github.avinashio.ozhuku.domain.checkpoint.ProcessingCheckpoint;
import io.github.avinashio.ozhuku.domain.delivery.ConflictBehavior;
import io.github.avinashio.ozhuku.domain.delivery.DeliveryPolicy;
import io.github.avinashio.ozhuku.domain.deduplication.DeduplicationEvaluator;
import io.github.avinashio.ozhuku.domain.deduplication.DuplicatePolicy;
import io.github.avinashio.ozhuku.domain.deduplication.ProcessingRecord;
import io.github.avinashio.ozhuku.domain.execution.DestinationCommit;
import io.github.avinashio.ozhuku.domain.execution.DestinationCommitReference;
import io.github.avinashio.ozhuku.domain.execution.DestinationExecution;
import io.github.avinashio.ozhuku.domain.execution.Execution;
import io.github.avinashio.ozhuku.domain.execution.ExecutionReference;
import io.github.avinashio.ozhuku.domain.execution.ExecutionStatus;
import io.github.avinashio.ozhuku.domain.execution.FlowExecution;
import io.github.avinashio.ozhuku.domain.execution.SourceExecution;
import io.github.avinashio.ozhuku.domain.flow.Flow;
import io.github.avinashio.ozhuku.domain.flow.FlowMode;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.FlowId;
import io.github.avinashio.ozhuku.domain.identity.PipelineId;
import io.github.avinashio.ozhuku.domain.identity.PipelineVersion;
import io.github.avinashio.ozhuku.domain.identity.ProcessingIdentity;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.domain.identity.SourceFingerprint;
import io.github.avinashio.ozhuku.domain.identity.SourceIdentity;
import io.github.avinashio.ozhuku.domain.pipeline.PipelineConfiguration;
import io.github.avinashio.ozhuku.domain.pipeline.PipelineDefinition;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.domain.resource.ResourceLocation;
import io.github.avinashio.ozhuku.persistence.DestinationCommitRepository;
import io.github.avinashio.ozhuku.persistence.DestinationExecutionRepository;
import io.github.avinashio.ozhuku.persistence.ExecutionRepository;
import io.github.avinashio.ozhuku.persistence.FlowExecutionRepository;
import io.github.avinashio.ozhuku.persistence.PipelineConfigurationRepository;
import io.github.avinashio.ozhuku.persistence.ProcessingCheckpointRepository;
import io.github.avinashio.ozhuku.persistence.ProcessingRecordRepository;
import io.github.avinashio.ozhuku.persistence.SourceExecutionRepository;
import io.github.avinashio.ozhuku.storage.StorageOutput;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConfiguredPipelineExecutionServiceTest {

    private static final ExecutionId EXECUTION_ID =
            new ExecutionId("execution-1");

    private static final PipelineId PIPELINE_ID =
            new PipelineId("pipeline-1");

    private static final PipelineVersion PIPELINE_VERSION =
            new PipelineVersion(1L);

    private static final FlowId FLOW_ID =
            new FlowId("flow-1");

    private ExecutionRepository executionRepository;

    private ConfiguredPipelineExecutionService service;

    @BeforeEach
    void setUp() {

        executionRepository =
                new InMemoryExecutionRepository();

        final FlowExecutionRepository flowExecutionRepository =
                new InMemoryFlowExecutionRepository();

        final SourceExecutionRepository sourceExecutionRepository =
                new InMemorySourceExecutionRepository();

        final DestinationExecutionRepository
                destinationExecutionRepository =
                new InMemoryDestinationExecutionRepository();

        final DestinationCommitRepository destinationCommitRepository =
                new InMemoryDestinationCommitRepository();

        final ProcessingCheckpointRepository
                processingCheckpointRepository =
                new InMemoryProcessingCheckpointRepository();

        final ExecutionInitializationService initializationService =
                new ExecutionInitializationService(
                        executionRepository,
                        flowExecutionRepository,
                        sourceExecutionRepository,
                        destinationExecutionRepository,
                        destinationCommitRepository);

        final ProcessingRecordRepository processingRecordRepository =
                new InMemoryProcessingRecordRepository();

        final ExecutionDeduplicationService
                executionDeduplicationService =
                new ExecutionDeduplicationService(
                        new SourceIdentityService(
                                resource ->
                                        new SourceIdentity("test-source")),
                        new SourceFingerprintService(
                                resource ->
                                        new SourceFingerprint(
                                                "test-fingerprint")),
                        new DeduplicationService(
                                processingRecordRepository,
                                new DeduplicationEvaluator()));

        final Clock clock = Clock.systemUTC();

        final ExecutionProcessingCoordinator
                processingCoordinator =
                new ExecutionProcessingCoordinator(
                        new ExecutionOrchestrationService(
                                new ExecutionLifecycleService(
                                        executionRepository,
                                        clock),
                                new FlowExecutionLifecycleService(
                                        flowExecutionRepository,
                                        clock),
                                new SourceExecutionLifecycleService(
                                        sourceExecutionRepository,
                                        clock),
                                new DestinationExecutionLifecycleService(
                                        destinationExecutionRepository,
                                        clock)),
                        new ExecutionProcessingService(
                                new ResourceTransferService(
                                        resource ->
                                                new ByteArrayInputStream(
                                                        "test".getBytes()),
                                        (destination, content, conflictBehavior) ->
                                                content.transferTo(
                                                        OutputStream
                                                                .nullOutputStream())),
                                new RecordProcessingService(
                                        resource ->
                                                new ByteArrayInputStream(
                                                        "test".getBytes()),
                                        (destination, deliveryPolicy) ->
                                                new StorageOutput() {

                                                    @Override
                                                    public OutputStream stream() {
                                                        return OutputStream
                                                                .nullOutputStream();
                                                    }

                                                    @Override
                                                    public void commit() {
                                                    }

                                                    @Override
                                                    public void close() {
                                                    }
                                                })),
                        new ExecutionResourceValidator(
                                sourceExecutionRepository,
                                destinationExecutionRepository),
                        executionDeduplicationService,
                        processingRecordRepository,
                        processingCheckpointRepository,
                        destinationCommitRepository,
                        clock);

        final ExecutionRunService executionRunService =
                new ExecutionRunService(
                        initializationService,
                        processingCoordinator);

        final PipelineExecutionService pipelineExecutionService =
                new PipelineExecutionService(
                        executionRunService);

        final PipelineConfigurationRepository
                configurationRepository =
                new InMemoryPipelineConfigurationRepository();

        final PipelineConfigurationResolutionService
                configurationResolutionService =
                new PipelineConfigurationResolutionService(
                        configurationRepository,
                        new PipelinePlanResolver());

        service =
                new ConfiguredPipelineExecutionService(
                        configurationResolutionService,
                        pipelineExecutionService);

        configurationRepository.save(configuration());
    }

    @Test
    void shouldResolveConfigurationAndExecutePipeline()
            throws IOException {

        service.execute(
                executionReference(),
                PIPELINE_ID,
                PIPELINE_VERSION);

        final Execution execution =
                executionRepository
                        .findById(EXECUTION_ID)
                        .orElseThrow();

        assertEquals(
                ExecutionStatus.COMPLETED,
                execution.status());
    }

    @Test
    void shouldExecuteRequestedPipelineVersion()
            throws IOException {

        final PipelineId secondPipelineId =
                new PipelineId("pipeline-2");

        final PipelineVersion secondVersion =
                new PipelineVersion(2L);

        final PipelineConfiguration secondConfiguration =
                new PipelineConfiguration(
                        new PipelineDefinition(
                                secondPipelineId,
                                secondVersion,
                                "Second configured pipeline"),
                        new Flow(
                                new FlowId("flow-2"),
                                "Second flow",
                                FlowMode.RESOURCE_TRANSFER),
                        resource("source-2"),
                        resource("destination-2"),
                        new DeliveryPolicy(
                                ConflictBehavior.REPLACE),
                        DuplicatePolicy.SKIP_IF_PROCESSED);

        final InMemoryPipelineConfigurationRepository
                configurationRepository =
                new InMemoryPipelineConfigurationRepository();

        configurationRepository.save(configuration());
        configurationRepository.save(secondConfiguration);

        final PipelineConfigurationResolutionService
                configurationResolutionService =
                new PipelineConfigurationResolutionService(
                        configurationRepository,
                        new PipelinePlanResolver());

        final PipelineExecutionService
                pipelineExecutionService =
                createPipelineExecutionService();

        final ConfiguredPipelineExecutionService
                configuredService =
                new ConfiguredPipelineExecutionService(
                        configurationResolutionService,
                        pipelineExecutionService);

        configuredService.execute(
                new ExecutionReference(
                        new ExecutionId("execution-2"),
                        secondPipelineId,
                        secondVersion),
                secondPipelineId,
                secondVersion);

        final Execution execution =
                executionRepository
                        .findById(new ExecutionId("execution-2"))
                        .orElseThrow();

        assertEquals(
                ExecutionStatus.COMPLETED,
                execution.status());
    }

    @Test
    void shouldRejectMissingConfiguration()
            throws IOException {

        final PipelineId missingPipelineId =
                new PipelineId("missing-pipeline");

        final PipelineVersion missingVersion =
                new PipelineVersion(99L);

        final IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                service.execute(
                                        new ExecutionReference(
                                                EXECUTION_ID,
                                                missingPipelineId,
                                                missingVersion),
                                        missingPipelineId,
                                        missingVersion));

        assertEquals(
                "Pipeline configuration not found: "
                        + "missing-pipeline:99",
                exception.getMessage());
    }

    private PipelineExecutionService
    createPipelineExecutionService() {

        final FlowExecutionRepository flowExecutionRepository =
                new InMemoryFlowExecutionRepository();

        final SourceExecutionRepository sourceExecutionRepository =
                new InMemorySourceExecutionRepository();

        final DestinationExecutionRepository
                destinationExecutionRepository =
                new InMemoryDestinationExecutionRepository();

        final DestinationCommitRepository destinationCommitRepository =
                new InMemoryDestinationCommitRepository();

        final ProcessingCheckpointRepository
                processingCheckpointRepository =
                new InMemoryProcessingCheckpointRepository();

        final ExecutionInitializationService initializationService =
                new ExecutionInitializationService(
                        executionRepository,
                        flowExecutionRepository,
                        sourceExecutionRepository,
                        destinationExecutionRepository,
                        destinationCommitRepository);

        final ProcessingRecordRepository processingRecordRepository =
                new InMemoryProcessingRecordRepository();

        final ExecutionDeduplicationService
                executionDeduplicationService =
                new ExecutionDeduplicationService(
                        new SourceIdentityService(
                                resource ->
                                        new SourceIdentity("test-source")),
                        new SourceFingerprintService(
                                resource ->
                                        new SourceFingerprint(
                                                "test-fingerprint")),
                        new DeduplicationService(
                                processingRecordRepository,
                                new DeduplicationEvaluator()));

        final Clock clock = Clock.systemUTC();

        final ExecutionProcessingCoordinator
                processingCoordinator =
                new ExecutionProcessingCoordinator(
                        new ExecutionOrchestrationService(
                                new ExecutionLifecycleService(
                                        executionRepository,
                                        clock),
                                new FlowExecutionLifecycleService(
                                        flowExecutionRepository,
                                        clock),
                                new SourceExecutionLifecycleService(
                                        sourceExecutionRepository,
                                        clock),
                                new DestinationExecutionLifecycleService(
                                        destinationExecutionRepository,
                                        clock)),
                        new ExecutionProcessingService(
                                new ResourceTransferService(
                                        resource ->
                                                new ByteArrayInputStream(
                                                        "test".getBytes()),
                                        (destination, content, conflictBehavior) ->
                                                content.transferTo(
                                                        OutputStream
                                                                .nullOutputStream())),
                                new RecordProcessingService(
                                        resource ->
                                                new ByteArrayInputStream(
                                                        "test".getBytes()),
                                        (destination, deliveryPolicy) ->
                                                new StorageOutput() {

                                                    @Override
                                                    public OutputStream stream() {
                                                        return OutputStream
                                                                .nullOutputStream();
                                                    }

                                                    @Override
                                                    public void commit() {
                                                    }

                                                    @Override
                                                    public void close() {
                                                    }
                                                })),
                        new ExecutionResourceValidator(
                                sourceExecutionRepository,
                                destinationExecutionRepository),
                        executionDeduplicationService,
                        processingRecordRepository,
                        processingCheckpointRepository,
                        destinationCommitRepository,
                        clock);

        return new PipelineExecutionService(
                new ExecutionRunService(
                        initializationService,
                        processingCoordinator));
    }

    private static PipelineConfiguration configuration() {

        return new PipelineConfiguration(
                new PipelineDefinition(
                        PIPELINE_ID,
                        PIPELINE_VERSION,
                        "Configured test pipeline"),
                new Flow(
                        FLOW_ID,
                        "Configured test flow",
                        FlowMode.RESOURCE_TRANSFER),
                resource("source"),
                resource("destination"),
                new DeliveryPolicy(
                        ConflictBehavior.REPLACE),
                DuplicatePolicy.SKIP_IF_PROCESSED);
    }

    private static Resource resource(
            final String name) {

        return new Resource(
                new ResourceId(name),
                new ResourceLocation(
                        "file:///" + name));
    }

    private static ExecutionReference executionReference() {

        return new ExecutionReference(
                EXECUTION_ID,
                PIPELINE_ID,
                PIPELINE_VERSION);
    }

    private static final class InMemoryPipelineConfigurationRepository
            implements PipelineConfigurationRepository {

        private final Map<String, PipelineConfiguration>
                configurations =
                new HashMap<>();

        @Override
        public Optional<PipelineConfiguration> findByVersion(
                final PipelineId pipelineId,
                final PipelineVersion pipelineVersion) {

            return Optional.ofNullable(
                    configurations.get(
                            key(pipelineId, pipelineVersion)));
        }

        @Override
        public void save(
                final PipelineConfiguration configuration) {

            configurations.put(
                    key(
                            configuration.pipelineDefinition()
                                    .pipelineId(),
                            configuration.pipelineDefinition()
                                    .version()),
                    configuration);
        }

        private static String key(
                final PipelineId pipelineId,
                final PipelineVersion pipelineVersion) {

            return pipelineId.value()
                    + ":"
                    + pipelineVersion.value();
        }
    }

    private static final class InMemoryExecutionRepository
            implements ExecutionRepository {

        private final Map<ExecutionId, Execution>
                executions =
                new HashMap<>();

        @Override
        public Optional<Execution> findById(
                final ExecutionId executionId) {

            return Optional.ofNullable(
                    executions.get(executionId));
        }

        @Override
        public void save(
                final Execution execution) {

            executions.put(
                    execution.reference().executionId(),
                    execution);
        }
    }

    private static final class InMemoryFlowExecutionRepository
            implements FlowExecutionRepository {

        private FlowExecution flowExecution;

        @Override
        public Optional<FlowExecution> findById(
                final ExecutionId executionId,
                final FlowId flowId) {

            if (flowExecution != null) {
                return Optional.of(flowExecution);
            }

            return Optional.empty();
        }

        @Override
        public void save(
                final FlowExecution flowExecution) {

            this.flowExecution = flowExecution;
        }
    }

    private static final class InMemorySourceExecutionRepository
            implements SourceExecutionRepository {

        private SourceExecution sourceExecution;

        @Override
        public Optional<SourceExecution> findById(
                final ExecutionId executionId,
                final ResourceId resourceId) {

            if (sourceExecution != null) {
                return Optional.of(sourceExecution);
            }

            return Optional.empty();
        }

        @Override
        public void save(
                final SourceExecution sourceExecution) {

            this.sourceExecution = sourceExecution;
        }
    }

    private static final class InMemoryDestinationExecutionRepository
            implements DestinationExecutionRepository {

        private DestinationExecution destinationExecution;

        @Override
        public Optional<DestinationExecution> findById(
                final ExecutionId executionId,
                final ResourceId resourceId) {

            if (destinationExecution != null) {
                return Optional.of(destinationExecution);
            }

            return Optional.empty();
        }

        @Override
        public void save(
                final DestinationExecution destinationExecution) {

            this.destinationExecution = destinationExecution;
        }
    }

    private static final class InMemoryProcessingRecordRepository
            implements ProcessingRecordRepository {

        private final Map<ProcessingIdentity, ProcessingRecord> records =
                new HashMap<>();

        @Override
        public Optional<ProcessingRecord> findByIdentity(
                final ProcessingIdentity identity) {

            return Optional.ofNullable(records.get(identity));
        }

        @Override
        public void save(
                final ProcessingRecord record) {

            records.put(record.identity(), record);
        }
    }

    private static final class InMemoryProcessingCheckpointRepository
            implements ProcessingCheckpointRepository {

        private final Map<
                io.github.avinashio.ozhuku.domain.execution
                        .SourceExecutionReference,
                ProcessingCheckpoint> checkpoints =
                new HashMap<>();

        @Override
        public Optional<ProcessingCheckpoint> findBySourceExecution(
                final io.github.avinashio.ozhuku.domain.execution
                        .SourceExecutionReference reference) {

            return Optional.ofNullable(
                    checkpoints.get(reference));
        }

        @Override
        public void save(
                final ProcessingCheckpoint checkpoint) {

            checkpoints.put(
                    checkpoint.sourceExecutionReference(),
                    checkpoint);
        }
    }

    private static final class InMemoryDestinationCommitRepository
            implements DestinationCommitRepository {

        private final Map<DestinationCommitReference, DestinationCommit>
                commits = new HashMap<>();

        @Override
        public Optional<DestinationCommit> findById(
                final DestinationCommitReference reference) {

            return Optional.ofNullable(commits.get(reference));
        }

        @Override
        public void save(
                final DestinationCommit destinationCommit) {

            commits.put(
                    destinationCommit.reference(),
                    destinationCommit);
        }
    }
}
