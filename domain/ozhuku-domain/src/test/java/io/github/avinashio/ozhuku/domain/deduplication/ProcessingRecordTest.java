package io.github.avinashio.ozhuku.domain.deduplication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.domain.identity.PipelineId;
import io.github.avinashio.ozhuku.domain.identity.PipelineVersion;
import io.github.avinashio.ozhuku.domain.identity.ProcessingIdentity;
import io.github.avinashio.ozhuku.domain.identity.SourceFingerprint;
import io.github.avinashio.ozhuku.domain.identity.SourceIdentity;
import io.github.avinashio.ozhuku.foundation.exception.ValidationException;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ProcessingRecordTest {

    @Test
    void shouldCreateNotProcessedRecord() {
        final ProcessingIdentity identity = identity();

        final ProcessingRecord record =
                ProcessingRecord.notProcessed(identity);

        assertEquals(identity, record.identity());
        assertEquals(
                ProcessingStatus.NOT_PROCESSED,
                record.status());
        assertNull(record.sourceFingerprint());
        assertNull(record.processedAt());
    }

    @Test
    void shouldCreateProcessedRecord() {
        final ProcessingIdentity identity = identity();
        final SourceFingerprint fingerprint =
                new SourceFingerprint("fingerprint-001");
        final Instant processedAt =
                Instant.parse("2026-09-22T10:00:00Z");

        final ProcessingRecord record =
                ProcessingRecord.processed(
                        identity,
                        fingerprint,
                        processedAt);

        assertEquals(identity, record.identity());
        assertEquals(
                ProcessingStatus.PROCESSED,
                record.status());
        assertEquals(
                fingerprint,
                record.sourceFingerprint());
        assertEquals(processedAt, record.processedAt());
    }

    @Test
    void shouldRejectNullIdentity() {
        assertThrows(
                ValidationException.class,
                () -> new ProcessingRecord(
                        null,
                        ProcessingStatus.NOT_PROCESSED,
                        null,
                        null));
    }

    @Test
    void shouldRejectNullStatus() {
        assertThrows(
                ValidationException.class,
                () -> new ProcessingRecord(
                        identity(),
                        null,
                        null,
                        null));
    }

    @Test
    void shouldRequireFingerprintForProcessedStatus() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ProcessingRecord(
                        identity(),
                        ProcessingStatus.PROCESSED,
                        null,
                        Instant.parse(
                                "2026-09-22T10:00:00Z")));
    }

    @Test
    void shouldRequireTimestampForProcessedStatus() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ProcessingRecord(
                        identity(),
                        ProcessingStatus.PROCESSED,
                        new SourceFingerprint(
                                "fingerprint-001"),
                        null));
    }

    @Test
    void shouldRejectFingerprintForNotProcessedStatus() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ProcessingRecord(
                        identity(),
                        ProcessingStatus.NOT_PROCESSED,
                        new SourceFingerprint(
                                "fingerprint-001"),
                        null));
    }

    @Test
    void shouldRejectTimestampForNotProcessedStatus() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ProcessingRecord(
                        identity(),
                        ProcessingStatus.NOT_PROCESSED,
                        null,
                        Instant.parse(
                                "2026-09-22T10:00:00Z")));
    }

    @Test
    void shouldCompareRecordsByValue() {
        final SourceFingerprint fingerprint =
                new SourceFingerprint("fingerprint-001");
        final Instant processedAt =
                Instant.parse("2026-09-22T10:00:00Z");

        final ProcessingRecord first =
                ProcessingRecord.processed(
                        identity(),
                        fingerprint,
                        processedAt);

        final ProcessingRecord second =
                ProcessingRecord.processed(
                        identity(),
                        fingerprint,
                        processedAt);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    private ProcessingIdentity identity() {
        return new ProcessingIdentity(
                new SourceIdentity("source-001"),
                new PipelineId("pipeline-001"),
                new PipelineVersion(1));
    }
}