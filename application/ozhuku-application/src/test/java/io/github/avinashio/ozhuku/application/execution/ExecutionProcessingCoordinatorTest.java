package io.github.avinashio.ozhuku.application.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.application.deduplication.DeduplicationService;
import io.github.avinashio.ozhuku.application.deduplication.ExecutionDeduplicationService;
import io.github.avinashio.ozhuku.application.orchestration.ExecutionOrchestrationService;
import io.github.avinashio.ozhuku.application.processing.ExecutionProcessingService;
import io.github.avinashio.ozhuku.application.processing.RecordProcessingRequest;
import io.github.avinashio.ozhuku.application.processing.ResourceTransferRequest;
import io.github.avinashio.ozhuku.application.record.RecordProcessingService;
import io.github.avinashio.ozhuku.application.source.SourceFingerprintService;
import io.github.avinashio.ozhuku.application.source.SourceIdentityService;
import io.github.avinashio.ozhuku.application.transfer.ResourceTransferService;
import io.github.avinashio.ozhuku.domain.checkpoint.ProcessingCheckpoint;
import io.github.avinashio.ozhuku.domain.deduplication.DeduplicationEvaluator;
import io.github.avinashio.ozhuku.domain.deduplication.DuplicatePolicy;
import io.github.avinashio.ozhuku.domain.deduplication.ProcessingRecord;
import io.github.avinashio.ozhuku.domain.delivery.ConflictBehavior;
import io.github.avinashio.ozhuku.domain.delivery.DeliveryPolicy;
import io.github.avinashio.ozhuku.domain.execution.CommitStatus;
import io.github.avinashio.ozhuku.domain.execution.DestinationCommit;
import io.github.avinashio.ozhuku.domain.execution.DestinationCommitReference;
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
import io.github.avinashio.ozhuku.domain.record.Record;
import io.github.avinashio.ozhuku.domain.record.RecordField;
import io.github.avinashio.ozhuku.domain.record.RecordMetadata;
import io.github.avinashio.ozhuku.domain.record.RecordValueType;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.domain.resource.ResourceLocation;
import io.github.avinashio.ozhuku.format.FormatReadResult;
import io.github.avinashio.ozhuku.format.FormatReader;
import io.github.avinashio.ozhuku.format.FormatWriter;
import io.github.avinashio.ozhuku.persistence.DestinationCommitRepository;
import io.github.avinashio.ozhuku.persistence.DestinationExecutionRepository;
import io.github.avinashio.ozhuku.persistence.ExecutionRepository;
import io.github.avinashio.ozhuku.persistence.FlowExecutionRepository;
import io.github.avinashio.ozhuku.persistence.ProcessingCheckpointRepository;
import io.github.avinashio.ozhuku.persistence.ProcessingRecordRepository;
import io.github.avinashio.ozhuku.persistence.SourceExecutionRepository;
import io.github.avinashio.ozhuku.storage.StorageOutput;
import io.github.avinashio.ozhuku.storage.StorageOutputProvider;
import io.github.avinashio.ozhuku.storage.StorageReader;
import io.github.avinashio.ozhuku.storage.StorageWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExecutionProcessingCoordinatorTest {

    private static final Instant FIXED_TIME =
            Instant.parse("2026-09-25T10:15:30Z");

    private static final ExecutionId EXECUTION_ID =
            new ExecutionId("execution-1");

    private static final FlowId FLOW_ID =
            new FlowId("flow-1");

    private static final PipelineId PIPELINE_ID =
            new PipelineId("pipeline-1");

    private static final PipelineVersion PIPELINE_VERSION =
            new PipelineVersion(1L);

    private static final ResourceId SOURCE_RESOURCE_ID =
            new ResourceId("source");

    private static final ResourceId DESTINATION_RESOURCE_ID =
            new ResourceId("destination");

    private FakeExecutionRepository executionRepository;
    private FakeFlowExecutionRepository flowExecutionRepository;
    private FakeSourceExecutionRepository sourceExecutionRepository;
    private FakeDestinationExecutionRepository destinationExecutionRepository;
    private FakeDestinationCommitRepository destinationCommitRepository;
    private FakeProcessingCheckpointRepository processingCheckpointRepository;
    private FakeProcessingRecordRepository processingRecordRepository;

    private ExecutionProcessingCoordinator service;

    @BeforeEach
    void setUp() {
        executionRepository =
                new FakeExecutionRepository();

        flowExecutionRepository =
                new FakeFlowExecutionRepository();

        sourceExecutionRepository =
                new FakeSourceExecutionRepository();

        destinationExecutionRepository =
                new FakeDestinationExecutionRepository();

        destinationCommitRepository =
                new FakeDestinationCommitRepository();

        processingCheckpointRepository =
                new FakeProcessingCheckpointRepository();

        processingRecordRepository =
                new FakeProcessingRecordRepository();

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

        final SourceExecutionLifecycleService
                sourceExecutionLifecycleService =
                new SourceExecutionLifecycleService(
                        sourceExecutionRepository,
                        clock);

        final DestinationExecutionLifecycleService
                destinationExecutionLifecycleService =
                new DestinationExecutionLifecycleService(
                        destinationExecutionRepository,
                        clock);

        final ExecutionOrchestrationService orchestrationService =
                new ExecutionOrchestrationService(
                        executionLifecycleService,
                        flowExecutionLifecycleService,
                        sourceExecutionLifecycleService,
                        destinationExecutionLifecycleService);

        final ResourceTransferService resourceTransferService =
                new ResourceTransferService(
                        new TestStorageReader(
                                "transfer-input".getBytes()),
                        new TestStorageWriter());

        final RecordProcessingService recordProcessingService =
                new RecordProcessingService(
                        new TestStorageReader(
                                "record-input".getBytes()),
                        new TestStorageOutputProvider());

        final ExecutionProcessingService processingService =
                new ExecutionProcessingService(
                        resourceTransferService,
                        recordProcessingService);

        final ExecutionResourceValidator executionResourceValidator =
                new ExecutionResourceValidator(
                        sourceExecutionRepository,
                        destinationExecutionRepository);

        service =
                createService(
                        orchestrationService,
                        processingService,
                        executionResourceValidator,
                        clock);
    }

    @Test
    void processResourceTransferShouldCompleteExecution()
            throws IOException {

        createPendingExecution();

        final ResourceTransferRequest request =
                new ResourceTransferRequest(
                        resource("source"),
                        resource("destination"),
                        deliveryPolicy());

        service.processResourceTransfer(
                createExecutionReference(),
                createPipelinePlan(
                        DuplicatePolicy.SKIP_IF_PROCESSED),
                request);

        assertEquals(
                ExecutionStatus.COMPLETED,
                executionRepository
                        .findById(EXECUTION_ID)
                        .orElseThrow()
                        .status());

        assertEquals(
                FlowExecutionStatus.COMPLETED,
                flowExecutionRepository
                        .findById(
                                EXECUTION_ID,
                                FLOW_ID)
                        .orElseThrow()
                        .status());

        assertEquals(
                SourceExecutionStatus.COMPLETED,
                sourceExecutionRepository
                        .findById(
                                EXECUTION_ID,
                                SOURCE_RESOURCE_ID)
                        .orElseThrow()
                        .status());

        assertEquals(
                DestinationExecutionStatus.COMPLETED,
                destinationExecutionRepository
                        .findById(
                                EXECUTION_ID,
                                DESTINATION_RESOURCE_ID)
                        .orElseThrow()
                        .status());

        assertDestinationCommitIsCommitted();

        assertEquals(
                1,
                processingRecordRepository.size());

        assertEquals(
                0,
                processingCheckpointRepository.size());
    }

    @Test
    void processRecordProcessingShouldCompleteExecution()
            throws IOException {

        createPendingExecution();

        final Record firstRecord =
                record(0, "first");

        final Record secondRecord =
                record(1, "second");

        final TestFormatReader formatReader =
                new TestFormatReader(
                        firstRecord,
                        secondRecord);

        final TestFormatWriter formatWriter =
                new TestFormatWriter();

        final RecordProcessingRequest request =
                new RecordProcessingRequest(
                        resource("source"),
                        resource("destination"),
                        deliveryPolicy(),
                        formatReader,
                        formatWriter);

        service.processRecordProcessing(
                createExecutionReference(),
                createPipelinePlan(
                        DuplicatePolicy.SKIP_IF_PROCESSED),
                request);

        assertEquals(
                List.of(
                        firstRecord,
                        secondRecord),
                formatWriter.records());

        assertEquals(
                ExecutionStatus.COMPLETED,
                executionRepository
                        .findById(EXECUTION_ID)
                        .orElseThrow()
                        .status());

        assertEquals(
                FlowExecutionStatus.COMPLETED,
                flowExecutionRepository
                        .findById(
                                EXECUTION_ID,
                                FLOW_ID)
                        .orElseThrow()
                        .status());

        assertEquals(
                SourceExecutionStatus.COMPLETED,
                sourceExecutionRepository
                        .findById(
                                EXECUTION_ID,
                                SOURCE_RESOURCE_ID)
                        .orElseThrow()
                        .status());

        assertEquals(
                DestinationExecutionStatus.COMPLETED,
                destinationExecutionRepository
                        .findById(
                                EXECUTION_ID,
                                DESTINATION_RESOURCE_ID)
                        .orElseThrow()
                        .status());

        assertDestinationCommitIsCommitted();

        assertEquals(
                1,
                processingCheckpointRepository.size());

        final ProcessingCheckpoint checkpoint =
                processingCheckpointRepository
                        .findBySourceExecution(
                                new SourceExecutionReference(
                                        EXECUTION_ID,
                                        SOURCE_RESOURCE_ID))
                        .orElseThrow();

        assertEquals(
                new SourceExecutionReference(
                        EXECUTION_ID,
                        SOURCE_RESOURCE_ID),
                checkpoint.sourceExecutionReference());

        assertEquals(
                1L,
                checkpoint.recordSequence());

        assertEquals(
                FIXED_TIME,
                checkpoint.checkpointedAt());

        assertEquals(
                1,
                processingRecordRepository.size());
    }

    @Test
    void processRecordProcessingShouldNotSaveCheckpointForEmptyInput()
            throws IOException {

        createPendingExecution();

        final RecordProcessingRequest request =
                new RecordProcessingRequest(
                        resource("source"),
                        resource("destination"),
                        deliveryPolicy(),
                        new TestFormatReader(),
                        new TestFormatWriter());

        service.processRecordProcessing(
                createExecutionReference(),
                createPipelinePlan(
                        DuplicatePolicy.SKIP_IF_PROCESSED),
                request);

        assertEquals(
                ExecutionStatus.COMPLETED,
                executionRepository
                        .findById(EXECUTION_ID)
                        .orElseThrow()
                        .status());

        assertEquals(
                CommitStatus.COMMITTED,
                destinationCommitRepository
                        .findById(
                                new DestinationCommitReference(
                                        new DestinationExecutionReference(
                                                EXECUTION_ID,
                                                DESTINATION_RESOURCE_ID)))
                        .orElseThrow()
                        .status());

        assertEquals(
                0,
                processingCheckpointRepository.size());

        assertEquals(
                1,
                processingRecordRepository.size());
    }

    @Test
    void processRecordProcessingShouldFailWhenDestinationCommitPersistenceFails()
            throws IOException {

        createPendingExecution();

        destinationCommitRepository.setFailWhenSavingCommitted(true);

        final TestFormatWriter formatWriter =
                new TestFormatWriter();

        final RecordProcessingRequest request =
                new RecordProcessingRequest(
                        resource("source"),
                        resource("destination"),
                        deliveryPolicy(),
                        new TestFormatReader(
                                record(0, "first"),
                                record(1, "second")),
                        formatWriter);

        final IllegalStateException actual =
                assertThrows(
                        IllegalStateException.class,
                        () -> service.processRecordProcessing(
                                createExecutionReference(),
                                createPipelinePlan(
                                        DuplicatePolicy.SKIP_IF_PROCESSED),
                                request));

        assertEquals(
                "Simulated destination commit persistence failure",
                actual.getMessage());

        assertEquals(
                List.of(
                        record(0, "first"),
                        record(1, "second")),
                formatWriter.records());

        assertEquals(
                ExecutionStatus.FAILED,
                executionRepository
                        .findById(EXECUTION_ID)
                        .orElseThrow()
                        .status());

        final DestinationCommitReference reference =
                new DestinationCommitReference(
                        new DestinationExecutionReference(
                                EXECUTION_ID,
                                DESTINATION_RESOURCE_ID));

        assertEquals(
                CommitStatus.UNKNOWN,
                destinationCommitRepository
                        .findById(reference)
                        .orElseThrow()
                        .status());

        assertEquals(
                0,
                processingCheckpointRepository.size());

        assertEquals(
                0,
                processingRecordRepository.size());
    }

    @Test
    void processResourceTransferShouldFailExecutionWhenProcessingFails()
            throws IOException {

        createPendingExecution();

        final IOException expected =
                new IOException(
                        "Simulated transfer failure");

        final ResourceTransferService failingTransferService =
                new ResourceTransferService(
                        resource -> {
                            throw expected;
                        },
                        new TestStorageWriter());

        final RecordProcessingService recordProcessingService =
                new RecordProcessingService(
                        new TestStorageReader(
                                "unused".getBytes()),
                        new TestStorageOutputProvider());

        final ExecutionProcessingService processingService =
                new ExecutionProcessingService(
                        failingTransferService,
                        recordProcessingService);

        final Clock clock =
                Clock.fixed(
                        FIXED_TIME,
                        ZoneOffset.UTC);

        service =
                createService(
                        processingService,
                        clock);

        final ResourceTransferRequest request =
                new ResourceTransferRequest(
                        resource("source"),
                        resource("destination"),
                        deliveryPolicy());

        final IOException actual =
                assertThrows(
                        IOException.class,
                        () -> service.processResourceTransfer(
                                createExecutionReference(),
                                createPipelinePlan(
                                        DuplicatePolicy.SKIP_IF_PROCESSED),
                                request));

        assertEquals(
                expected,
                actual);

        assertEquals(
                ExecutionStatus.FAILED,
                executionRepository
                        .findById(EXECUTION_ID)
                        .orElseThrow()
                        .status());

        assertEquals(
                FlowExecutionStatus.FAILED,
                flowExecutionRepository
                        .findById(
                                EXECUTION_ID,
                                FLOW_ID)
                        .orElseThrow()
                        .status());

        assertEquals(
                SourceExecutionStatus.FAILED,
                sourceExecutionRepository
                        .findById(
                                EXECUTION_ID,
                                SOURCE_RESOURCE_ID)
                        .orElseThrow()
                        .status());

        assertEquals(
                DestinationExecutionStatus.FAILED,
                destinationExecutionRepository
                        .findById(
                                EXECUTION_ID,
                                DESTINATION_RESOURCE_ID)
                        .orElseThrow()
                        .status());

        assertEquals(
                0,
                processingRecordRepository.size());

        assertEquals(
                0,
                processingCheckpointRepository.size());
    }

    @Test
    void processRecordProcessingShouldFailExecutionWhenProcessingFails()
            throws IOException {

        createPendingExecution();

        final IOException expected =
                new IOException(
                        "Simulated record processing failure");

        final TestFormatWriter formatWriter =
                new TestFormatWriter() {
                    @Override
                    public void write(
                            final Record record)
                            throws IOException {

                        throw expected;
                    }
                };

        final RecordProcessingRequest request =
                new RecordProcessingRequest(
                        resource("source"),
                        resource("destination"),
                        deliveryPolicy(),
                        new TestFormatReader(
                                record(0, "first")),
                        formatWriter);

        final IOException actual =
                assertThrows(
                        IOException.class,
                        () -> service.processRecordProcessing(
                                createExecutionReference(),
                                createPipelinePlan(
                                        DuplicatePolicy.SKIP_IF_PROCESSED),
                                request));

        assertEquals(
                expected,
                actual);

        assertEquals(
                ExecutionStatus.FAILED,
                executionRepository
                        .findById(EXECUTION_ID)
                        .orElseThrow()
                        .status());

        assertEquals(
                FlowExecutionStatus.FAILED,
                flowExecutionRepository
                        .findById(
                                EXECUTION_ID,
                                FLOW_ID)
                        .orElseThrow()
                        .status());

        assertEquals(
                SourceExecutionStatus.FAILED,
                sourceExecutionRepository
                        .findById(
                                EXECUTION_ID,
                                SOURCE_RESOURCE_ID)
                        .orElseThrow()
                        .status());

        assertEquals(
                DestinationExecutionStatus.FAILED,
                destinationExecutionRepository
                        .findById(
                                EXECUTION_ID,
                                DESTINATION_RESOURCE_ID)
                        .orElseThrow()
                        .status());

        assertEquals(
                0,
                processingRecordRepository.size());

        assertEquals(
                0,
                processingCheckpointRepository.size());
    }

    @Test
    void processResourceTransferShouldSkipAlreadyProcessedSource()
            throws IOException {

        createPendingExecution();

        final ProcessingIdentity identity =
                new ProcessingIdentity(
                        new SourceIdentity(
                                "file:///source"),
                        PIPELINE_ID,
                        PIPELINE_VERSION);

        processingRecordRepository.save(
                ProcessingRecord.processed(
                        identity,
                        new SourceFingerprint(
                                "fingerprint-1"),
                        FIXED_TIME));

        final ResourceTransferRequest request =
                new ResourceTransferRequest(
                        resource("source"),
                        resource("destination"),
                        deliveryPolicy());

        service.processResourceTransfer(
                createExecutionReference(),
                createPipelinePlan(
                        DuplicatePolicy.SKIP_IF_PROCESSED),
                request);

        assertEquals(
                ExecutionStatus.COMPLETED,
                executionRepository
                        .findById(EXECUTION_ID)
                        .orElseThrow()
                        .status());

        assertEquals(
                1,
                processingRecordRepository.size());

        assertEquals(
                0,
                processingCheckpointRepository.size());
    }

    @Test
    void processResourceTransferShouldProcessWhenDuplicatePolicyAllowsReprocessing()
            throws IOException {

        createPendingExecution();

        final ProcessingIdentity identity =
                new ProcessingIdentity(
                        new SourceIdentity(
                                "file:///source"),
                        PIPELINE_ID,
                        PIPELINE_VERSION);

        processingRecordRepository.save(
                ProcessingRecord.processed(
                        identity,
                        new SourceFingerprint(
                                "old-fingerprint"),
                        FIXED_TIME));

        final ResourceTransferRequest request =
                new ResourceTransferRequest(
                        resource("source"),
                        resource("destination"),
                        deliveryPolicy());

        service.processResourceTransfer(
                createExecutionReference(),
                createPipelinePlan(
                        DuplicatePolicy.REPROCESS_IF_CHANGED),
                request);

        assertEquals(
                ExecutionStatus.COMPLETED,
                executionRepository
                        .findById(EXECUTION_ID)
                        .orElseThrow()
                        .status());

        assertEquals(
                1,
                processingRecordRepository.size());

        assertEquals(
                0,
                processingCheckpointRepository.size());
    }

    @Test
    void processResourceTransferShouldProcessWhenDuplicatePolicyIsAlwaysProcess()
            throws IOException {

        createPendingExecution();

        final ProcessingIdentity identity =
                new ProcessingIdentity(
                        new SourceIdentity(
                                "file:///source"),
                        PIPELINE_ID,
                        PIPELINE_VERSION);

        processingRecordRepository.save(
                ProcessingRecord.processed(
                        identity,
                        new SourceFingerprint(
                                "fingerprint-1"),
                        FIXED_TIME));

        final ResourceTransferRequest request =
                new ResourceTransferRequest(
                        resource("source"),
                        resource("destination"),
                        deliveryPolicy());

        service.processResourceTransfer(
                createExecutionReference(),
                createPipelinePlan(
                        DuplicatePolicy.ALWAYS_PROCESS),
                request);

        assertEquals(
                ExecutionStatus.COMPLETED,
                executionRepository
                        .findById(EXECUTION_ID)
                        .orElseThrow()
                        .status());

        assertEquals(
                1,
                processingRecordRepository.size());

        assertEquals(
                0,
                processingCheckpointRepository.size());
    }

    @Test
    void processResourceTransferShouldFailWhenDuplicatePolicyFails()
            throws IOException {

        createPendingExecution();

        final ProcessingIdentity identity =
                new ProcessingIdentity(
                        new SourceIdentity(
                                "file:///source"),
                        PIPELINE_ID,
                        PIPELINE_VERSION);

        processingRecordRepository.save(
                ProcessingRecord.processed(
                        identity,
                        new SourceFingerprint(
                                "fingerprint-1"),
                        FIXED_TIME));

        final ResourceTransferRequest request =
                new ResourceTransferRequest(
                        resource("source"),
                        resource("destination"),
                        deliveryPolicy());

        final IllegalStateException actual =
                assertThrows(
                        IllegalStateException.class,
                        () -> service.processResourceTransfer(
                                createExecutionReference(),
                                createPipelinePlan(
                                        DuplicatePolicy.FAIL_IF_DUPLICATE),
                                request));

        assertEquals(
                "Duplicate source detected for processing identity",
                actual.getMessage());

        assertEquals(
                ExecutionStatus.FAILED,
                executionRepository
                        .findById(EXECUTION_ID)
                        .orElseThrow()
                        .status());

        assertEquals(
                1,
                processingRecordRepository.size());

        assertEquals(
                0,
                processingCheckpointRepository.size());
    }

    @Test
    void processResourceTransferShouldRejectUnknownSourceBeforeStartingExecution()
            throws IOException {

        createPendingExecution();

        final ResourceTransferRequest request =
                new ResourceTransferRequest(
                        resource("unknown-source"),
                        resource("destination"),
                        deliveryPolicy());

        final IllegalStateException actual =
                assertThrows(
                        IllegalStateException.class,
                        () -> service.processResourceTransfer(
                                createExecutionReference(),
                                createPipelinePlan(
                                        DuplicatePolicy.SKIP_IF_PROCESSED),
                                request));

        assertEquals(
                "Source execution not found for execution "
                        + EXECUTION_ID
                        + " and resource "
                        + new ResourceId("unknown-source"),
                actual.getMessage());

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

        assertEquals(
                SourceExecutionStatus.PENDING,
                sourceExecutionRepository
                        .findById(
                                EXECUTION_ID,
                                SOURCE_RESOURCE_ID)
                        .orElseThrow()
                        .status());

        assertEquals(
                DestinationExecutionStatus.PENDING,
                destinationExecutionRepository
                        .findById(
                                EXECUTION_ID,
                                DESTINATION_RESOURCE_ID)
                        .orElseThrow()
                        .status());
    }

    @Test
    void processRecordProcessingShouldFailWhenCheckpointPersistenceFails()
            throws IOException {

        createPendingExecution();

        processingCheckpointRepository.setFailWhenSaving(true);

        final RecordProcessingRequest request =
                new RecordProcessingRequest(
                        resource("source"),
                        resource("destination"),
                        deliveryPolicy(),
                        new TestFormatReader(
                                record(0, "first"),
                                record(1, "second")),
                        new TestFormatWriter());

        final IllegalStateException actual =
                assertThrows(
                        IllegalStateException.class,
                        () -> service.processRecordProcessing(
                                createExecutionReference(),
                                createPipelinePlan(
                                        DuplicatePolicy.SKIP_IF_PROCESSED),
                                request));

        assertEquals(
                "Simulated checkpoint persistence failure",
                actual.getMessage());

        final DestinationCommitReference reference =
                new DestinationCommitReference(
                        new DestinationExecutionReference(
                                EXECUTION_ID,
                                DESTINATION_RESOURCE_ID));

        assertEquals(
                CommitStatus.COMMITTED,
                destinationCommitRepository
                        .findById(reference)
                        .orElseThrow()
                        .status());

        assertEquals(
                0,
                processingCheckpointRepository.size());

        assertEquals(
                ExecutionStatus.FAILED,
                executionRepository
                        .findById(EXECUTION_ID)
                        .orElseThrow()
                        .status());
    }

    private void assertDestinationCommitIsCommitted() {
        final DestinationCommitReference reference =
                new DestinationCommitReference(
                        new DestinationExecutionReference(
                                EXECUTION_ID,
                                DESTINATION_RESOURCE_ID));

        final DestinationCommit destinationCommit =
                destinationCommitRepository
                        .findById(reference)
                        .orElseThrow();

        assertEquals(
                CommitStatus.COMMITTED,
                destinationCommit.status());

        assertEquals(
                FIXED_TIME,
                destinationCommit.committedAt());
    }

    private void createPendingExecution() {
        executionRepository.save(
                new Execution(
                        createExecutionReference()));

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

        destinationCommitRepository.save(
                DestinationCommit.notCommitted(
                        new DestinationCommitReference(
                                new DestinationExecutionReference(
                                        EXECUTION_ID,
                                        DESTINATION_RESOURCE_ID))));
    }

    private ExecutionProcessingCoordinator createService(
            final ExecutionProcessingService processingService,
            final Clock clock) {

        final ExecutionOrchestrationService orchestrationService =
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

        final ExecutionResourceValidator executionResourceValidator =
                new ExecutionResourceValidator(
                        sourceExecutionRepository,
                        destinationExecutionRepository);

        final SourceIdentityService sourceIdentityService =
                new SourceIdentityService(
                        resource ->
                                new SourceIdentity(
                                        resource.location().value()));

        final SourceFingerprintService sourceFingerprintService =
                new SourceFingerprintService(
                        resource ->
                                new SourceFingerprint(
                                        "fingerprint-1"));

        final DeduplicationService deduplicationService =
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
                processingCheckpointRepository,
                destinationCommitRepository,
                clock);
    }

    private ExecutionProcessingCoordinator createService(
            final ExecutionOrchestrationService orchestrationService,
            final ExecutionProcessingService processingService,
            final ExecutionResourceValidator executionResourceValidator,
            final Clock clock) {

        final SourceIdentityService sourceIdentityService =
                new SourceIdentityService(
                        resource ->
                                new SourceIdentity(
                                        resource.location().value()));

        final SourceFingerprintService sourceFingerprintService =
                new SourceFingerprintService(
                        resource ->
                                new SourceFingerprint(
                                        "fingerprint-1"));

        final DeduplicationService deduplicationService =
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
                processingCheckpointRepository,
                destinationCommitRepository,
                clock);
    }

    private static ExecutionReference createExecutionReference() {
        return new ExecutionReference(
                EXECUTION_ID,
                PIPELINE_ID,
                PIPELINE_VERSION);
    }

    private static PipelinePlan createPipelinePlan(
            final DuplicatePolicy duplicatePolicy) {

        final PipelineDefinition definition =
                new PipelineDefinition(
                        PIPELINE_ID,
                        PIPELINE_VERSION,
                        "test pipeline");

        final Flow flow =
                new Flow(
                        FLOW_ID,
                        "test-flow",
                        FlowMode.RESOURCE_TRANSFER);

        return new PipelinePlan(
                definition,
                flow,
                resource("source"),
                resource("destination"),
                deliveryPolicy(),
                duplicatePolicy);
    }

    private static Resource resource(
            final String name) {

        return new Resource(
                new ResourceId(name),
                new ResourceLocation(
                        "file:///" + name));
    }

    private static DeliveryPolicy deliveryPolicy() {
        return new DeliveryPolicy(
                ConflictBehavior.REPLACE);
    }

    private static Record record(
            final long sequence,
            final String value) {

        return new Record(
                sequence,
                List.of(
                        new RecordField(
                                "value",
                                RecordValueType.STRING,
                                value)),
                RecordMetadata.empty());
    }

    private static final class TestStorageReader
            implements StorageReader {

        private final byte[] content;

        private TestStorageReader(
                final byte[] content) {

            this.content = content.clone();
        }

        @Override
        public InputStream open(
                final Resource resource) {

            return new ByteArrayInputStream(
                    content);
        }
    }

    private static final class TestStorageWriter
            implements StorageWriter {

        private final ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        @Override
        public void write(
                final Resource destination,
                final InputStream content,
                final ConflictBehavior conflictBehavior)
                throws IOException {

            content.transferTo(output);
        }
    }

    private static final class TestStorageOutputProvider
            implements StorageOutputProvider {

        private final ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        @Override
        public StorageOutput open(
                final Resource destination,
                final DeliveryPolicy deliveryPolicy) {

            return new StorageOutput() {

                private boolean closed;

                @Override
                public OutputStream stream() {

                    if (closed) {
                        throw new IllegalStateException(
                                "Storage output is already closed");
                    }

                    return output;
                }

                @Override
                public void commit() {

                    if (closed) {
                        throw new IllegalStateException(
                                "Storage output is already closed");
                    }
                }

                @Override
                public void close() {
                    closed = true;
                }
            };
        }
    }

    private static class TestFormatReader
            implements FormatReader {

        private final List<Record> records;
        private int index;

        private TestFormatReader(
                final Record... records) {

            this.records = List.of(records);
        }

        @Override
        public void open(
                final InputStream inputStream) {
        }

        @Override
        public FormatReadResult read() {

            if (index >= records.size()) {
                return FormatReadResult.endOfInput();
            }

            return FormatReadResult.record(
                    records.get(index++));
        }

        @Override
        public void close() {
        }
    }

    private static class TestFormatWriter
            implements FormatWriter {

        private final List<Record> records =
                new ArrayList<>();

        @Override
        public void open(
                final OutputStream outputStream) {
        }

        @Override
        public void write(
                final Record record)
                throws IOException {

            records.add(record);
        }

        @Override
        public void close() {
        }

        private List<Record> records() {
            return List.copyOf(records);
        }
    }

    private static final class FakeExecutionRepository
            implements ExecutionRepository {

        private final Map<ExecutionId, Execution> executions =
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

    private static final class FakeSourceExecutionRepository
            implements SourceExecutionRepository {

        private final Map<String, SourceExecution> executions =
                new HashMap<>();

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

    private static final class FakeDestinationCommitRepository
            implements DestinationCommitRepository {

        private final Map<DestinationCommitReference, DestinationCommit>
                commits = new HashMap<>();

        private boolean failWhenSavingCommitted;

        @Override
        public Optional<DestinationCommit> findById(
                final DestinationCommitReference reference) {

            return Optional.ofNullable(
                    commits.get(reference));
        }

        @Override
        public void save(
                final DestinationCommit destinationCommit) {

            if (failWhenSavingCommitted
                    && destinationCommit.status() == CommitStatus.COMMITTED) {
                throw new IllegalStateException(
                        "Simulated destination commit persistence failure");
            }

            commits.put(
                    destinationCommit.reference(),
                    destinationCommit);
        }

        private void setFailWhenSavingCommitted(
                final boolean failWhenSavingCommitted) {

            this.failWhenSavingCommitted = failWhenSavingCommitted;
        }
    }

    private static final class FakeProcessingCheckpointRepository
            implements ProcessingCheckpointRepository {

        private final Map<SourceExecutionReference, ProcessingCheckpoint>
                checkpoints = new HashMap<>();

        private boolean failWhenSaving;

        @Override
        public Optional<ProcessingCheckpoint> findBySourceExecution(
                final SourceExecutionReference reference) {

            return Optional.ofNullable(
                    checkpoints.get(reference));
        }

        @Override
        public void save(
                final ProcessingCheckpoint checkpoint) {

            if (failWhenSaving) {
                throw new IllegalStateException(
                        "Simulated checkpoint persistence failure");
            }

            checkpoints.put(
                    checkpoint.sourceExecutionReference(),
                    checkpoint);
        }

        private void setFailWhenSaving(
                final boolean failWhenSaving) {

            this.failWhenSaving = failWhenSaving;
        }

        private int size() {
            return checkpoints.size();
        }
    }

    private static final class FakeProcessingRecordRepository
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

        private int size() {
            return records.size();
        }
    }


}
