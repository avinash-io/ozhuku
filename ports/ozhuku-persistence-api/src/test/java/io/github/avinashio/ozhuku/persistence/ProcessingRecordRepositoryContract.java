package io.github.avinashio.ozhuku.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.avinashio.ozhuku.domain.deduplication.ProcessingRecord;
import io.github.avinashio.ozhuku.domain.identity.PipelineId;
import io.github.avinashio.ozhuku.domain.identity.PipelineVersion;
import io.github.avinashio.ozhuku.domain.identity.ProcessingIdentity;
import io.github.avinashio.ozhuku.domain.identity.SourceFingerprint;
import io.github.avinashio.ozhuku.domain.identity.SourceIdentity;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Contract tests that every processing record repository implementation must
 * satisfy.
 *
 * <p>Concrete persistence modules should extend this contract and provide
 * their repository implementation.</p>
 */
public abstract class ProcessingRecordRepositoryContract {

    /**
     * Creates the repository implementation under test.
     *
     * @return repository under test
     */
    protected abstract ProcessingRecordRepository repository();

    @Test
    void shouldReturnEmptyWhenProcessingIdentityDoesNotExist() {
        final Optional<ProcessingRecord> result =
                repository().findByIdentity(identity());

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldSaveAndRetrieveProcessingRecord() {
        final ProcessingRecord record =
                ProcessingRecord.processed(
                        identity(),
                        new SourceFingerprint("fingerprint-001"),
                        Instant.parse(
                                "2026-09-22T10:00:00Z"));

        repository().save(record);

        final Optional<ProcessingRecord> result =
                repository().findByIdentity(identity());

        assertTrue(result.isPresent());
        assertEquals(record, result.orElseThrow());
    }

    private ProcessingIdentity identity() {
        return new ProcessingIdentity(
                new SourceIdentity("source-001"),
                new PipelineId("pipeline-001"),
                new PipelineVersion(1));
    }
}