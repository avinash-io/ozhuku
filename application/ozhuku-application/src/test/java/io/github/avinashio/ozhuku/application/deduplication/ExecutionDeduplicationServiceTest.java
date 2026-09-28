package io.github.avinashio.ozhuku.application.deduplication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.application.source.SourceFingerprintService;
import io.github.avinashio.ozhuku.application.source.SourceIdentityService;
import io.github.avinashio.ozhuku.domain.deduplication.DeduplicationDecision;
import io.github.avinashio.ozhuku.domain.deduplication.DeduplicationEvaluator;
import io.github.avinashio.ozhuku.domain.deduplication.DuplicatePolicy;
import io.github.avinashio.ozhuku.domain.deduplication.ProcessingRecord;
import io.github.avinashio.ozhuku.domain.delivery.ConflictBehavior;
import io.github.avinashio.ozhuku.domain.delivery.DeliveryPolicy;
import io.github.avinashio.ozhuku.domain.execution.ExecutionReference;
import io.github.avinashio.ozhuku.domain.flow.Flow;
import io.github.avinashio.ozhuku.domain.identity.FlowId;
import io.github.avinashio.ozhuku.domain.flow.FlowMode;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
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
import io.github.avinashio.ozhuku.persistence.ProcessingRecordRepository;
import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ExecutionDeduplicationServiceTest {

    @Test
    void shouldEvaluateNewSourceAsProcess()
            throws IOException {

        final SourceIdentity sourceIdentity =
                new SourceIdentity("source-1");

        final SourceFingerprint fingerprint =
                new SourceFingerprint("fingerprint-1");

        final ExecutionDeduplicationService service =
                createService(
                        sourceIdentity,
                        fingerprint,
                        Optional.empty());

        final ExecutionDeduplicationService.Result result =
                service.evaluate(
                        createExecutionReference(),
                        createPipelinePlan(
                                DuplicatePolicy.SKIP_IF_PROCESSED),
                        createSource());

        assertEquals(
                DeduplicationDecision.PROCESS,
                result.decision());

        assertEquals(
                sourceIdentity,
                result.processingIdentity().sourceIdentity());

        assertEquals(
                new PipelineId("pipeline-1"),
                result.processingIdentity().pipelineId());

        assertEquals(
                new PipelineVersion(1L),
                result.processingIdentity().pipelineVersion());

        assertEquals(
                fingerprint,
                result.sourceFingerprint());
    }

    @Test
    void shouldEvaluateAlreadyProcessedSourceAsSkip()
            throws IOException {

        final SourceIdentity sourceIdentity =
                new SourceIdentity("source-1");

        final SourceFingerprint fingerprint =
                new SourceFingerprint("fingerprint-1");

        final ProcessingRecord existingRecord =
                ProcessingRecord.processed(
                        new ProcessingIdentity(
                                sourceIdentity,
                                new PipelineId("pipeline-1"),
                                new PipelineVersion(1L)),
                        fingerprint,
                        Instant.parse(
                                "2026-01-01T00:00:00Z"));

        final ExecutionDeduplicationService service =
                createService(
                        sourceIdentity,
                        fingerprint,
                        Optional.of(existingRecord));

        final ExecutionDeduplicationService.Result result =
                service.evaluate(
                        createExecutionReference(),
                        createPipelinePlan(
                                DuplicatePolicy.SKIP_IF_PROCESSED),
                        createSource());

        assertEquals(
                DeduplicationDecision.SKIP,
                result.decision());

        assertEquals(
                sourceIdentity,
                result.processingIdentity().sourceIdentity());

        assertEquals(
                fingerprint,
                result.sourceFingerprint());
    }

    @Test
    void shouldUsePipelineDuplicatePolicy()
            throws IOException {

        final SourceIdentity sourceIdentity =
                new SourceIdentity("source-1");

        final SourceFingerprint fingerprint =
                new SourceFingerprint("fingerprint-1");

        final ProcessingRecord existingRecord =
                ProcessingRecord.processed(
                        new ProcessingIdentity(
                                sourceIdentity,
                                new PipelineId("pipeline-1"),
                                new PipelineVersion(1L)),
                        fingerprint,
                        Instant.parse(
                                "2026-01-01T00:00:00Z"));

        final ExecutionDeduplicationService service =
                createService(
                        sourceIdentity,
                        fingerprint,
                        Optional.of(existingRecord));

        final ExecutionDeduplicationService.Result result =
                service.evaluate(
                        createExecutionReference(),
                        createPipelinePlan(
                                DuplicatePolicy.ALWAYS_PROCESS),
                        createSource());

        assertEquals(
                DeduplicationDecision.PROCESS,
                result.decision());

        assertEquals(
                sourceIdentity,
                result.processingIdentity().sourceIdentity());

        assertEquals(
                fingerprint,
                result.sourceFingerprint());
    }

    @Test
    void shouldRejectNullExecutionReference() {

        final ExecutionDeduplicationService service =
                createService(
                        new SourceIdentity("source-1"),
                        new SourceFingerprint("fingerprint-1"),
                        Optional.empty());

        assertThrows(
                NullPointerException.class,
                () -> service.evaluate(
                        null,
                        createPipelinePlan(
                                DuplicatePolicy.SKIP_IF_PROCESSED),
                        createSource()));
    }

    @Test
    void shouldRejectNullPipelinePlan() {

        final ExecutionDeduplicationService service =
                createService(
                        new SourceIdentity("source-1"),
                        new SourceFingerprint("fingerprint-1"),
                        Optional.empty());

        assertThrows(
                NullPointerException.class,
                () -> service.evaluate(
                        createExecutionReference(),
                        null,
                        createSource()));
    }

    @Test
    void shouldRejectNullSource() {

        final ExecutionDeduplicationService service =
                createService(
                        new SourceIdentity("source-1"),
                        new SourceFingerprint("fingerprint-1"),
                        Optional.empty());

        assertThrows(
                NullPointerException.class,
                () -> service.evaluate(
                        createExecutionReference(),
                        createPipelinePlan(
                                DuplicatePolicy.SKIP_IF_PROCESSED),
                        null));
    }

    private ExecutionDeduplicationService createService(
            final SourceIdentity sourceIdentity,
            final SourceFingerprint fingerprint,
            final Optional<ProcessingRecord> existingRecord) {

        final SourceIdentityService identityService =
                new SourceIdentityService(
                        resource -> sourceIdentity);

        final SourceFingerprintService fingerprintService =
                new SourceFingerprintService(
                        resource -> fingerprint);

        final ProcessingRecordRepository repository =
                new ProcessingRecordRepository() {
                    @Override
                    public Optional<ProcessingRecord> findByIdentity(
                            final ProcessingIdentity identity) {
                        return existingRecord;
                    }

                    @Override
                    public void save(
                            final ProcessingRecord record) {
                        throw new UnsupportedOperationException(
                                "Save is not required by this test");
                    }
                };

        return new ExecutionDeduplicationService(
                identityService,
                fingerprintService,
                new DeduplicationService(
                        repository,
                        new DeduplicationEvaluator()));
    }

    private ExecutionReference createExecutionReference() {
        return new ExecutionReference(
                new ExecutionId("execution-1"),
                new PipelineId("pipeline-1"),
                new PipelineVersion(1L));
    }

    private PipelinePlan createPipelinePlan(
            final DuplicatePolicy duplicatePolicy) {

        final PipelineDefinition definition =
                new PipelineDefinition(
                        new PipelineId("pipeline-1"),
                        new PipelineVersion(1L),
                        "test pipeline");

        final Flow flow =
                new Flow(
                        new FlowId("flow-1"),
                        "test-flow",
                        FlowMode.RESOURCE_TRANSFER);

        final Resource source =
                createSource();

        final Resource destination =
                new Resource(
                        new ResourceId("destination-1"),
                        new ResourceLocation(
                                "file:///storage/output.txt"));

        final DeliveryPolicy deliveryPolicy =
                new DeliveryPolicy(
                        ConflictBehavior.REPLACE);

        return new PipelinePlan(
                definition,
                flow,
                source,
                destination,
                deliveryPolicy,
                duplicatePolicy);
    }

    private Resource createSource() {
        return new Resource(
                new ResourceId("source-1"),
                new ResourceLocation(
                        "file:///storage/input.txt"));
    }
}