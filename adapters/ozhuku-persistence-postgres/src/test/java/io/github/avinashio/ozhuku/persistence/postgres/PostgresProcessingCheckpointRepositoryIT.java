package io.github.avinashio.ozhuku.persistence.postgres;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.avinashio.ozhuku.domain.checkpoint.ProcessingCheckpoint;
import io.github.avinashio.ozhuku.domain.execution.SourceExecutionReference;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.persistence.ProcessingCheckpointRepository;
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
class PostgresProcessingCheckpointRepositoryIT {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17-alpine")
                    .withDatabaseName("ozhuku")
                    .withUsername("ozhuku")
                    .withPassword("ozhuku-test");

    private static ProcessingCheckpointRepository repository;

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
                new PostgresProcessingCheckpointRepository(
                        dataSource);
    }

    @Test
    void shouldReturnEmptyForUnknownSourceExecution() {
        final Optional<ProcessingCheckpoint> result =
                repository.findBySourceExecution(
                        sourceExecutionReference());

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldSaveAndRetrieveCheckpoint() {
        final SourceExecutionReference reference =
                sourceExecutionReference();

        final ProcessingCheckpoint checkpoint =
                new ProcessingCheckpoint(
                        reference,
                        100L,
                        Instant.parse(
                                "2026-09-22T10:00:00Z"));

        repository.save(checkpoint);

        final Optional<ProcessingCheckpoint> result =
                repository.findBySourceExecution(
                        reference);

        assertTrue(result.isPresent());
        assertEquals(
                checkpoint,
                result.orElseThrow());
    }

    @Test
    void shouldReplaceExistingCheckpoint() {
        final SourceExecutionReference reference =
                new SourceExecutionReference(
                        new ExecutionId("execution-replace"),
                        new ResourceId("resource-replace"));

        final ProcessingCheckpoint first =
                new ProcessingCheckpoint(
                        reference,
                        100L,
                        Instant.parse(
                                "2026-09-22T10:00:00Z"));

        final ProcessingCheckpoint second =
                new ProcessingCheckpoint(
                        reference,
                        200L,
                        Instant.parse(
                                "2026-09-22T11:00:00Z"));

        repository.save(first);
        repository.save(second);

        final Optional<ProcessingCheckpoint> result =
                repository.findBySourceExecution(
                        reference);

        assertTrue(result.isPresent());
        assertEquals(
                second,
                result.orElseThrow());
    }

    @Test
    void shouldKeepDifferentSourceExecutionsSeparate() {
        final SourceExecutionReference firstReference =
                new SourceExecutionReference(
                        new ExecutionId("execution-one"),
                        new ResourceId("resource-one"));

        final SourceExecutionReference secondReference =
                new SourceExecutionReference(
                        new ExecutionId("execution-two"),
                        new ResourceId("resource-two"));

        final ProcessingCheckpoint first =
                new ProcessingCheckpoint(
                        firstReference,
                        100L,
                        Instant.parse(
                                "2026-09-22T10:00:00Z"));

        final ProcessingCheckpoint second =
                new ProcessingCheckpoint(
                        secondReference,
                        200L,
                        Instant.parse(
                                "2026-09-22T11:00:00Z"));

        repository.save(first);
        repository.save(second);

        assertEquals(
                first,
                repository.findBySourceExecution(
                                firstReference)
                        .orElseThrow());

        assertEquals(
                second,
                repository.findBySourceExecution(
                                secondReference)
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

    private static SourceExecutionReference sourceExecutionReference() {
        return new SourceExecutionReference(
                new ExecutionId("execution-001"),
                new ResourceId("resource-001"));
    }
}