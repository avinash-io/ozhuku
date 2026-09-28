package io.github.avinashio.ozhuku.application.deduplication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.domain.deduplication.DeduplicationDecision;
import io.github.avinashio.ozhuku.domain.deduplication.DeduplicationEvaluator;
import io.github.avinashio.ozhuku.domain.deduplication.DuplicatePolicy;
import io.github.avinashio.ozhuku.domain.deduplication.ProcessingRecord;
import io.github.avinashio.ozhuku.domain.identity.PipelineId;
import io.github.avinashio.ozhuku.domain.identity.PipelineVersion;
import io.github.avinashio.ozhuku.domain.identity.ProcessingIdentity;
import io.github.avinashio.ozhuku.domain.identity.SourceFingerprint;
import io.github.avinashio.ozhuku.domain.identity.SourceIdentity;
import io.github.avinashio.ozhuku.persistence.ProcessingRecordRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeduplicationServiceTest {

    private InMemoryProcessingRecordRepository repository;
    private DeduplicationService service;
    private ProcessingIdentity identity;
    private SourceFingerprint fingerprint;

    @BeforeEach
    void setUp() {
        repository = new InMemoryProcessingRecordRepository();
        service = new DeduplicationService(
                repository,
                new DeduplicationEvaluator());

        identity = new ProcessingIdentity(
                new SourceIdentity("source-1"),
                new PipelineId("pipeline-1"),
                new PipelineVersion(1));

        fingerprint = new SourceFingerprint("fingerprint-1");
    }

    @Test
    void shouldProcessWhenNoProcessingHistoryExists() {
        final DeduplicationDecision decision = service.evaluate(
                identity,
                DuplicatePolicy.SKIP_IF_PROCESSED,
                fingerprint);

        assertEquals(
                DeduplicationDecision.PROCESS,
                decision);
    }

    @Test
    void shouldSkipWhenAlreadyProcessedWithSkipPolicy() {
        repository.save(ProcessingRecord.processed(
                identity,
                fingerprint,
                Instant.parse("2026-09-28T10:00:00Z")));

        final DeduplicationDecision decision = service.evaluate(
                identity,
                DuplicatePolicy.SKIP_IF_PROCESSED,
                fingerprint);

        assertEquals(
                DeduplicationDecision.SKIP,
                decision);
    }

    @Test
    void shouldFailWhenAlreadyProcessedWithFailPolicy() {
        repository.save(ProcessingRecord.processed(
                identity,
                fingerprint,
                Instant.parse("2026-09-28T10:00:00Z")));

        final DeduplicationDecision decision = service.evaluate(
                identity,
                DuplicatePolicy.FAIL_IF_DUPLICATE,
                fingerprint);

        assertEquals(
                DeduplicationDecision.FAIL,
                decision);
    }

    @Test
    void shouldProcessWhenSourceChangedWithReprocessPolicy() {
        repository.save(ProcessingRecord.processed(
                identity,
                fingerprint,
                Instant.parse("2026-09-28T10:00:00Z")));

        final DeduplicationDecision decision = service.evaluate(
                identity,
                DuplicatePolicy.REPROCESS_IF_CHANGED,
                new SourceFingerprint("fingerprint-2"));

        assertEquals(
                DeduplicationDecision.PROCESS,
                decision);
    }

    @Test
    void shouldSkipWhenSourceIsUnchangedWithReprocessPolicy() {
        repository.save(ProcessingRecord.processed(
                identity,
                fingerprint,
                Instant.parse("2026-09-28T10:00:00Z")));

        final DeduplicationDecision decision = service.evaluate(
                identity,
                DuplicatePolicy.REPROCESS_IF_CHANGED,
                fingerprint);

        assertEquals(
                DeduplicationDecision.SKIP,
                decision);
    }

    @Test
    void shouldAlwaysProcessWithAlwaysProcessPolicy() {
        repository.save(ProcessingRecord.processed(
                identity,
                fingerprint,
                Instant.parse("2026-09-28T10:00:00Z")));

        final DeduplicationDecision decision = service.evaluate(
                identity,
                DuplicatePolicy.ALWAYS_PROCESS,
                fingerprint);

        assertEquals(
                DeduplicationDecision.PROCESS,
                decision);
    }

    @Test
    void shouldRejectNullIdentity() {
        assertThrows(
                RuntimeException.class,
                () -> service.evaluate(
                        null,
                        DuplicatePolicy.SKIP_IF_PROCESSED,
                        fingerprint));
    }

    @Test
    void shouldRejectNullPolicy() {
        assertThrows(
                RuntimeException.class,
                () -> service.evaluate(
                        identity,
                        null,
                        fingerprint));
    }

    @Test
    void shouldRejectNullFingerprint() {
        assertThrows(
                RuntimeException.class,
                () -> service.evaluate(
                        identity,
                        DuplicatePolicy.SKIP_IF_PROCESSED,
                        null));
    }

    private static final class InMemoryProcessingRecordRepository
            implements ProcessingRecordRepository {

        private final Map<ProcessingIdentity, ProcessingRecord> records =
                new HashMap<>();

        @Override
        public Optional<ProcessingRecord> findByIdentity(
                final ProcessingIdentity processingIdentity) {
            return Optional.ofNullable(records.get(processingIdentity));
        }

        @Override
        public void save(final ProcessingRecord record) {
            records.put(record.identity(), record);
        }
    }
}