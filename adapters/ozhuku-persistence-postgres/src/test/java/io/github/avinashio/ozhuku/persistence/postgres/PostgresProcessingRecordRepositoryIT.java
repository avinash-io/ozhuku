package io.github.avinashio.ozhuku.persistence.postgres;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.avinashio.ozhuku.domain.deduplication.ProcessingRecord;
import io.github.avinashio.ozhuku.domain.identity.PipelineId;
import io.github.avinashio.ozhuku.domain.identity.PipelineVersion;
import io.github.avinashio.ozhuku.domain.identity.ProcessingIdentity;
import io.github.avinashio.ozhuku.domain.identity.SourceFingerprint;
import io.github.avinashio.ozhuku.domain.identity.SourceIdentity;
import io.github.avinashio.ozhuku.persistence.ProcessingRecordRepository;
import java.time.Instant;
import java.util.Optional;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class PostgresProcessingRecordRepositoryIT {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17-alpine")
                    .withDatabaseName("ozhuku")
                    .withUsername("ozhuku")
                    .withPassword("ozhuku-test");

    private static ProcessingRecordRepository repository;

    @BeforeAll
    static void setUp() {
        final DataSource dataSource =
                createDataSource();

        Flyway.configure()
                .dataSource(
                        POSTGRES.getJdbcUrl(),
                        POSTGRES.getUsername(),
                        POSTGRES.getPassword())
                .locations("classpath:db/migration")
                .load()
                .migrate();

        repository =
                new PostgresProcessingRecordRepository(
                        dataSource);
    }

    @Test
    void shouldReturnEmptyForUnknownProcessingIdentity() {
        final Optional<ProcessingRecord> result =
                repository.findByIdentity(identity());

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldSaveAndRetrieveProcessedRecord() {
        final ProcessingIdentity identity =
                identity();

        final SourceFingerprint fingerprint =
                new SourceFingerprint("fingerprint-001");

        final Instant processedAt =
                Instant.parse("2026-09-22T10:00:00Z");

        final ProcessingRecord record =
                ProcessingRecord.processed(
                        identity,
                        fingerprint,
                        processedAt);

        repository.save(record);

        final Optional<ProcessingRecord> result =
                repository.findByIdentity(identity);

        assertTrue(result.isPresent());
        assertEquals(
                record,
                result.orElseThrow());
    }

    @Test
    void shouldReplaceExistingProcessingRecord() {
        final ProcessingIdentity identity =
                new ProcessingIdentity(
                        new SourceIdentity("source-replace"),
                        new PipelineId("pipeline-001"),
                        new PipelineVersion(1));

        final ProcessingRecord first =
                ProcessingRecord.processed(
                        identity,
                        new SourceFingerprint("fingerprint-001"),
                        Instant.parse(
                                "2026-09-22T10:00:00Z"));

        final ProcessingRecord second =
                ProcessingRecord.processed(
                        identity,
                        new SourceFingerprint("fingerprint-002"),
                        Instant.parse(
                                "2026-09-22T11:00:00Z"));

        repository.save(first);
        repository.save(second);

        final Optional<ProcessingRecord> result =
                repository.findByIdentity(identity);

        assertTrue(result.isPresent());
        assertEquals(
                second,
                result.orElseThrow());
    }

    @Test
    void shouldKeepDifferentPipelineVersionsSeparate() {
        final ProcessingIdentity versionOne =
                new ProcessingIdentity(
                        new SourceIdentity("source-version"),
                        new PipelineId("pipeline-001"),
                        new PipelineVersion(1));

        final ProcessingIdentity versionTwo =
                new ProcessingIdentity(
                        new SourceIdentity("source-version"),
                        new PipelineId("pipeline-001"),
                        new PipelineVersion(2));

        final ProcessingRecord first =
                ProcessingRecord.processed(
                        versionOne,
                        new SourceFingerprint("fingerprint-v1"),
                        Instant.parse(
                                "2026-09-22T10:00:00Z"));

        final ProcessingRecord second =
                ProcessingRecord.processed(
                        versionTwo,
                        new SourceFingerprint("fingerprint-v2"),
                        Instant.parse(
                                "2026-09-22T11:00:00Z"));

        repository.save(first);
        repository.save(second);

        assertEquals(
                first,
                repository.findByIdentity(versionOne)
                        .orElseThrow());

        assertEquals(
                second,
                repository.findByIdentity(versionTwo)
                        .orElseThrow());
    }

    private static DataSource createDataSource() {
        final org.postgresql.ds.PGSimpleDataSource dataSource =
                new org.postgresql.ds.PGSimpleDataSource();

        dataSource.setURL(
                POSTGRES.getJdbcUrl());
        dataSource.setUser(
                POSTGRES.getUsername());
        dataSource.setPassword(
                POSTGRES.getPassword());

        return dataSource;
    }

    private static ProcessingIdentity identity() {
        return new ProcessingIdentity(
                new SourceIdentity("source-001"),
                new PipelineId("pipeline-001"),
                new PipelineVersion(1));
    }
}