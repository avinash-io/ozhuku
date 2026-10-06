package io.github.avinashio.ozhuku.application.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.application.deduplication.DeduplicationService;
import io.github.avinashio.ozhuku.application.deduplication.ExecutionDeduplicationService;
import io.github.avinashio.ozhuku.application.initialization.ExecutionInitializationService;
import io.github.avinashio.ozhuku.application.orchestration.ExecutionOrchestrationService;
import io.github.avinashio.ozhuku.application.processing.ExecutionProcessingService;
import io.github.avinashio.ozhuku.application.record.RecordProcessingService;
import io.github.avinashio.ozhuku.application.source.SourceFingerprintService;
import io.github.avinashio.ozhuku.application.source.SourceIdentityService;
import io.github.avinashio.ozhuku.application.transfer.ResourceTransferService;
import io.github.avinashio.ozhuku.domain.deduplication.DeduplicationEvaluator;
import io.github.avinashio.ozhuku.domain.deduplication.DuplicatePolicy;
import io.github.avinashio.ozhuku.domain.deduplication.ProcessingRecord;
import io.github.avinashio.ozhuku.domain.delivery.ConflictBehavior;
import io.github.avinashio.ozhuku.domain.delivery.DeliveryPolicy;
import io.github.avinashio.ozhuku.domain.execution.DestinationCommit;
import io.github.avinashio.ozhuku.domain.execution.DestinationCommitReference;
import io.github.avinashio.ozhuku.domain.execution.Execution;
import io.github.avinashio.ozhuku.domain.execution.ExecutionReference;
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
import io.github.avinashio.ozhuku.domain.pipeline.PipelineDefinition;
import io.github.avinashio.ozhuku.domain.pipeline.PipelinePlan;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.domain.resource.ResourceLocation;
import io.github.avinashio.ozhuku.format.FormatReadResult;
import io.github.avinashio.ozhuku.format.FormatReader;
import io.github.avinashio.ozhuku.format.FormatWriter;
import io.github.avinashio.ozhuku.persistence.DestinationCommitRepository;
import io.github.avinashio.ozhuku.persistence.DestinationExecutionRepository;
import io.github.avinashio.ozhuku.persistence.ExecutionRepository;
import io.github.avinashio.ozhuku.persistence.FlowExecutionRepository;
import io.github.avinashio.ozhuku.persistence.ProcessingRecordRepository;
import io.github.avinashio.ozhuku.persistence.SourceExecutionRepository;
import io.github.avinashio.ozhuku.storage.StorageOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PipelineExecutionServiceTest {

    private static final ExecutionId EXECUTION_ID =
            new ExecutionId("execution-1");

    private static final FlowId FLOW_ID =
            new FlowId("flow-1");

    private static final PipelineId PIPELINE_ID =
            new PipelineId("pipeline-1");

    private static final PipelineVersion PIPELINE_VERSION =
            new PipelineVersion(1L);

    private ExecutionRepository executionRepository;

    private PipelineExecutionService service;

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
                                                new java.io.ByteArrayInputStream(
                                                        "test".getBytes()),
                                        (destination, content, conflictBehavior) ->
                                                content.transferTo(
                                                        OutputStream
                                                                .nullOutputStream())),
                                new RecordProcessingService(
                                        resource ->
                                                new java.io.ByteArrayInputStream(
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
                        destinationCommitRepository,
                        clock);

        final ExecutionRunService executionRunService =
                new ExecutionRunService(
                        initializationService,
                        processingCoordinator);

        service =
                new PipelineExecutionService(
                        executionRunService);
    }

    @Test
    void shouldExecuteResourceTransferPipeline()
            throws IOException {

        final PipelinePlan pipelinePlan =
                pipelinePlan(FlowMode.RESOURCE_TRANSFER);

        service.execute(
                executionReference(),
                pipelinePlan);

        final Execution execution =
                executionRepository
                        .findById(EXECUTION_ID)
                        .orElseThrow();

        assertEquals(
                io.github.avinashio.ozhuku.domain.execution.ExecutionStatus
                        .COMPLETED,
                execution.status());
    }

    @Test
    void shouldRejectRecordProcessingThroughExecute()
            throws IOException {

        final PipelinePlan pipelinePlan =
                pipelinePlan(FlowMode.RECORD_PROCESSING);

        final IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.execute(
                                executionReference(),
                                pipelinePlan));

        assertEquals(
                "RECORD_PROCESSING requires format reader and writer",
                exception.getMessage());
    }

    @Test
    void shouldRejectUnsupportedResourceProcessing()
            throws IOException {

        final PipelinePlan pipelinePlan =
                pipelinePlan(FlowMode.RESOURCE_PROCESSING);

        final UnsupportedOperationException exception =
                assertThrows(
                        UnsupportedOperationException.class,
                        () -> service.execute(
                                executionReference(),
                                pipelinePlan));

        assertEquals(
                "RESOURCE_PROCESSING execution is not implemented yet",
                exception.getMessage());
    }

    @Test
    void shouldExecuteRecordProcessingPipeline()
            throws IOException {

        final PipelinePlan pipelinePlan =
                pipelinePlan(FlowMode.RECORD_PROCESSING);

        final TestFormatWriter formatWriter =
                new TestFormatWriter();

        service.executeRecordProcessing(
                executionReference(),
                pipelinePlan,
                new TestFormatReader(),
                formatWriter);

        final Execution execution =
                executionRepository
                        .findById(EXECUTION_ID)
                        .orElseThrow();

        assertEquals(
                io.github.avinashio.ozhuku.domain.execution.ExecutionStatus
                        .COMPLETED,
                execution.status());

        assertEquals(
                1,
                formatWriter.writeCount());
    }

    @Test
    void shouldRejectWrongFlowModeForRecordProcessing()
            throws IOException {

        final PipelinePlan pipelinePlan =
                pipelinePlan(FlowMode.RESOURCE_TRANSFER);

        final IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.executeRecordProcessing(
                                executionReference(),
                                pipelinePlan,
                                new TestFormatReader(),
                                new TestFormatWriter()));

        assertEquals(
                "Pipeline plan flow mode must be RECORD_PROCESSING",
                exception.getMessage());
    }

    @Test
    void shouldRejectNullExecutionReference() {

        assertThrows(
                NullPointerException.class,
                () -> service.execute(
                        null,
                        pipelinePlan(FlowMode.RESOURCE_TRANSFER)));
    }

    @Test
    void shouldRejectNullPipelinePlan() {

        assertThrows(
                NullPointerException.class,
                () -> service.execute(
                        executionReference(),
                        null));
    }

    @Test
    void shouldRejectNullFormatReader() {

        assertThrows(
                NullPointerException.class,
                () -> service.executeRecordProcessing(
                        executionReference(),
                        pipelinePlan(FlowMode.RECORD_PROCESSING),
                        null,
                        new TestFormatWriter()));
    }

    @Test
    void shouldRejectNullFormatWriter() {

        assertThrows(
                NullPointerException.class,
                () -> service.executeRecordProcessing(
                        executionReference(),
                        pipelinePlan(FlowMode.RECORD_PROCESSING),
                        new TestFormatReader(),
                        null));
    }

    private static PipelinePlan pipelinePlan(
            final FlowMode flowMode) {

        return new PipelinePlan(
                new PipelineDefinition(
                        PIPELINE_ID,
                        PIPELINE_VERSION,
                        "Test pipeline"),
                new Flow(
                        FLOW_ID,
                        "Test flow",
                        flowMode),
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

    private static final class TestFormatReader
            implements FormatReader {

        private boolean read;

        @Override
        public void open(
                final InputStream inputStream) {
        }

        @Override
        public FormatReadResult read() {

            if (read) {
                return FormatReadResult.endOfInput();
            }

            read = true;

            return FormatReadResult.record(
                    new io.github.avinashio.ozhuku.domain.record.Record(
                            0,
                            List.of(),
                            io.github.avinashio.ozhuku.domain.record
                                    .RecordMetadata.empty()));
        }

        @Override
        public void close() {
        }
    }

    private static final class TestFormatWriter
            implements FormatWriter {

        private int writeCount;

        @Override
        public void open(
                final OutputStream outputStream) {
        }

        @Override
        public void write(
                final io.github.avinashio.ozhuku.domain.record.Record record) {
            writeCount++;
        }

        @Override
        public void close() {
        }

        int writeCount() {
            return writeCount;
        }
    }

    private static final class InMemoryProcessingRecordRepository
            implements ProcessingRecordRepository {

        private final Map<ProcessingIdentity, ProcessingRecord> records =
                new HashMap<>();

        @Override
        public Optional<ProcessingRecord> findByIdentity(
                final ProcessingIdentity identity) {

            return Optional.ofNullable(
                    records.get(identity));
        }

        @Override
        public void save(
                final ProcessingRecord record) {

            records.put(
                    record.identity(),
                    record);
        }
    }

    private static final class InMemoryExecutionRepository
            implements ExecutionRepository {

        private Execution execution;

        @Override
        public Optional<Execution> findById(
                final ExecutionId executionId) {

            if (execution != null
                    && execution.reference().executionId()
                    .equals(executionId)) {
                return Optional.of(execution);
            }

            return Optional.empty();
        }

        @Override
        public void save(
                final Execution execution) {

            this.execution = execution;
        }
    }

    private static final class InMemoryFlowExecutionRepository
            implements FlowExecutionRepository {

        private io.github.avinashio.ozhuku.domain.execution.FlowExecution
                flowExecution;

        @Override
        public Optional<
                io.github.avinashio.ozhuku.domain.execution.FlowExecution>
        findById(
                final ExecutionId executionId,
                final FlowId flowId) {

            if (flowExecution != null) {
                return Optional.of(flowExecution);
            }

            return Optional.empty();
        }

        @Override
        public void save(
                final io.github.avinashio.ozhuku.domain.execution.FlowExecution
                        flowExecution) {

            this.flowExecution = flowExecution;
        }
    }

    private static final class InMemorySourceExecutionRepository
            implements SourceExecutionRepository {

        private io.github.avinashio.ozhuku.domain.execution.SourceExecution
                sourceExecution;

        @Override
        public Optional<
                io.github.avinashio.ozhuku.domain.execution.SourceExecution>
        findById(
                final ExecutionId executionId,
                final ResourceId resourceId) {

            if (sourceExecution != null) {
                return Optional.of(sourceExecution);
            }

            return Optional.empty();
        }

        @Override
        public void save(
                final io.github.avinashio.ozhuku.domain.execution.SourceExecution
                        sourceExecution) {

            this.sourceExecution = sourceExecution;
        }
    }

    private static final class InMemoryDestinationExecutionRepository
            implements DestinationExecutionRepository {

        private io.github.avinashio.ozhuku.domain.execution.DestinationExecution
                destinationExecution;

        @Override
        public Optional<
                io.github.avinashio.ozhuku.domain.execution.DestinationExecution>
        findById(
                final ExecutionId executionId,
                final ResourceId resourceId) {

            if (destinationExecution != null) {
                return Optional.of(destinationExecution);
            }

            return Optional.empty();
        }

        @Override
        public void save(
                final io.github.avinashio.ozhuku.domain.execution.DestinationExecution
                        destinationExecution) {

            this.destinationExecution = destinationExecution;
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