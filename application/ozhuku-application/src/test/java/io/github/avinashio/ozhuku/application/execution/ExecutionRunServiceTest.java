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
import io.github.avinashio.ozhuku.domain.execution.DestinationExecution;
import io.github.avinashio.ozhuku.domain.execution.Execution;
import io.github.avinashio.ozhuku.domain.execution.ExecutionReference;
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

class ExecutionRunServiceTest {

    private static final ExecutionId EXECUTION_ID =
            new ExecutionId("execution-1");

    private static final FlowId FLOW_ID =
            new FlowId("flow-1");

    private static final ResourceId SOURCE_RESOURCE_ID =
            new ResourceId("source");

    private static final ResourceId DESTINATION_RESOURCE_ID =
            new ResourceId("destination");

    private ExecutionRepository executionRepository;

    private FlowExecutionRepository flowExecutionRepository;

    private SourceExecutionRepository sourceExecutionRepository;

    private DestinationExecutionRepository destinationExecutionRepository;

    private DestinationCommitRepository destinationCommitRepository;

    private ExecutionRunService service;

    @BeforeEach
    void setUp() {

        executionRepository =
                new InMemoryExecutionRepository();

        flowExecutionRepository =
                new InMemoryFlowExecutionRepository();

        sourceExecutionRepository =
                new InMemorySourceExecutionRepository();

        destinationExecutionRepository =
                new InMemoryDestinationExecutionRepository();

        destinationCommitRepository =
                new InMemoryDestinationCommitRepository();

        final ExecutionInitializationService initializationService =
                new ExecutionInitializationService(
                        executionRepository,
                        flowExecutionRepository,
                        sourceExecutionRepository,
                        destinationExecutionRepository,
                        destinationCommitRepository);

        final ExecutionProcessingCoordinator processingCoordinator =
                createProcessingCoordinator();

        service =
                new ExecutionRunService(
                        initializationService,
                        processingCoordinator);
    }

    @Test
    void shouldRunResourceTransferUsingPipelinePlan()
            throws IOException {

        final PipelinePlan pipelinePlan =
                pipelinePlan(FlowMode.RESOURCE_TRANSFER);

        service.runResourceTransfer(
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
    void shouldRunRecordProcessingUsingPipelinePlan()
            throws IOException {

        final PipelinePlan pipelinePlan =
                pipelinePlan(FlowMode.RECORD_PROCESSING);

        final TestFormatReader formatReader =
                new TestFormatReader();

        final TestFormatWriter formatWriter =
                new TestFormatWriter();

        service.runRecordProcessing(
                executionReference(),
                pipelinePlan,
                formatReader,
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
    void shouldRejectNullPipelinePlanForResourceTransfer() {

        assertThrows(
                NullPointerException.class,
                () -> service.runResourceTransfer(
                        executionReference(),
                        null));
    }

    @Test
    void shouldRejectNullPipelinePlanForRecordProcessing() {

        assertThrows(
                NullPointerException.class,
                () -> service.runRecordProcessing(
                        executionReference(),
                        null,
                        new TestFormatReader(),
                        new TestFormatWriter()));
    }

    @Test
    void shouldRejectNullFormatReader() {

        final PipelinePlan pipelinePlan =
                pipelinePlan(FlowMode.RECORD_PROCESSING);

        assertThrows(
                NullPointerException.class,
                () -> service.runRecordProcessing(
                        executionReference(),
                        pipelinePlan,
                        null,
                        new TestFormatWriter()));
    }

    @Test
    void shouldRejectNullFormatWriter() {

        final PipelinePlan pipelinePlan =
                pipelinePlan(FlowMode.RECORD_PROCESSING);

        assertThrows(
                NullPointerException.class,
                () -> service.runRecordProcessing(
                        executionReference(),
                        pipelinePlan,
                        new TestFormatReader(),
                        null));
    }

    private ExecutionProcessingCoordinator
    createProcessingCoordinator() {

        final Clock clock =
                Clock.systemUTC();

        final ExecutionOrchestrationService
                orchestrationService =
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
                                clock));

        final ExecutionProcessingService
                processingService =
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
                                        }));

        final ExecutionResourceValidator
                executionResourceValidator =
                new ExecutionResourceValidator(
                        sourceExecutionRepository,
                        destinationExecutionRepository);

        final InMemoryProcessingRecordRepository
                processingRecordRepository =
                new InMemoryProcessingRecordRepository();

        final SourceIdentityService
                sourceIdentityService =
                new SourceIdentityService(
                        resource ->
                                new SourceIdentity(
                                        resource.location().value()));

        final SourceFingerprintService
                sourceFingerprintService =
                new SourceFingerprintService(
                        resource ->
                                new SourceFingerprint(
                                        "test-fingerprint"));

        final DeduplicationService
                deduplicationService =
                new DeduplicationService(
                        processingRecordRepository,
                        new DeduplicationEvaluator());

        final ExecutionDeduplicationService
                executionDeduplicationService =
                new ExecutionDeduplicationService(
                        sourceIdentityService,
                        sourceFingerprintService,
                        deduplicationService);

        return new ExecutionProcessingCoordinator(
                orchestrationService,
                processingService,
                executionResourceValidator,
                executionDeduplicationService,
                processingRecordRepository,
                destinationCommitRepository,
                clock);
    }

    private static PipelinePlan pipelinePlan(
            final FlowMode flowMode) {

        return new PipelinePlan(
                new PipelineDefinition(
                        new PipelineId("pipeline-1"),
                        new PipelineVersion(1L),
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
                new PipelineId("pipeline-1"),
                new PipelineVersion(1L));
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
