package io.github.avinashio.ozhuku.persistence.postgres;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.avinashio.ozhuku.domain.execution.SourceExecution;
import io.github.avinashio.ozhuku.domain.execution.SourceExecutionReference;
import io.github.avinashio.ozhuku.domain.execution.SourceExecutionStatus;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.persistence.SourceExecutionRepository;
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
class PostgresSourceExecutionRepositoryIT {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17-alpine")
                    .withDatabaseName("ozhuku")
                    .withUsername("ozhuku")
                    .withPassword("ozhuku-test");

    private static SourceExecutionRepository repository;

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
                new PostgresSourceExecutionRepository(
                        dataSource);
    }

    @Test
    void shouldReturnEmptyForUnknownSourceExecution() {
        final Optional<SourceExecution> result =
                repository.findById(
                        new ExecutionId("execution-unknown"),
                        new ResourceId("resource-unknown"));

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldSaveAndRetrievePendingSourceExecution() {
        final SourceExecution sourceExecution =
                pendingSourceExecution(
                        "execution-pending",
                        "resource-001");

        repository.save(sourceExecution);

        final Optional<SourceExecution> result =
                repository.findById(
                        sourceExecution.reference()
                                .executionId(),
                        sourceExecution.reference()
                                .resourceId());

        assertTrue(result.isPresent());
        assertEquals(
                sourceExecution,
                result.orElseThrow());
    }

    @Test
    void shouldSaveAndRetrieveCompletedSourceExecution() {
        final SourceExecutionReference reference =
                reference(
                        "execution-completed",
                        "resource-completed");

        final Instant startedAt =
                Instant.parse(
                        "2026-09-22T10:00:00Z");

        final Instant completedAt =
                Instant.parse(
                        "2026-09-22T10:01:00Z");

        final SourceExecution sourceExecution =
                SourceExecution.rehydrate(
                        reference,
                        SourceExecutionStatus.COMPLETED,
                        startedAt,
                        completedAt);

        repository.save(sourceExecution);

        final Optional<SourceExecution> result =
                repository.findById(
                        reference.executionId(),
                        reference.resourceId());

        assertTrue(result.isPresent());
        assertEquals(
                sourceExecution,
                result.orElseThrow());
    }

    @Test
    void shouldKeepDifferentResourcesSeparate() {
        final SourceExecution first =
                SourceExecution.rehydrate(
                        reference(
                                "execution-shared",
                                "resource-one"),
                        SourceExecutionStatus.COMPLETED,
                        Instant.parse(
                                "2026-09-22T10:00:00Z"),
                        Instant.parse(
                                "2026-09-22T10:01:00Z"));

        final SourceExecution second =
                SourceExecution.rehydrate(
                        reference(
                                "execution-shared",
                                "resource-two"),
                        SourceExecutionStatus.FAILED,
                        Instant.parse(
                                "2026-09-22T11:00:00Z"),
                        Instant.parse(
                                "2026-09-22T11:01:00Z"));

        repository.save(first);
        repository.save(second);

        assertEquals(
                first,
                repository.findById(
                                first.reference().executionId(),
                                first.reference().resourceId())
                        .orElseThrow());

        assertEquals(
                second,
                repository.findById(
                                second.reference().executionId(),
                                second.reference().resourceId())
                        .orElseThrow());
    }

    @Test
    void shouldReplaceExistingSourceExecutionState() {
        final SourceExecutionReference reference =
                reference(
                        "execution-replace",
                        "resource-replace");

        final SourceExecution pending =
                SourceExecution.rehydrate(
                        reference,
                        SourceExecutionStatus.PENDING,
                        null,
                        null);

        final SourceExecution running =
                pending.start(
                        Instant.parse(
                                "2026-09-22T12:00:00Z"));

        repository.save(pending);
        repository.save(running);

        final Optional<SourceExecution> result =
                repository.findById(
                        reference.executionId(),
                        reference.resourceId());

        assertTrue(result.isPresent());
        assertEquals(
                running,
                result.orElseThrow());
    }

    private static SourceExecution pendingSourceExecution(
            final String executionId,
            final String resourceId) {

        return SourceExecution.rehydrate(
                reference(
                        executionId,
                        resourceId),
                SourceExecutionStatus.PENDING,
                null,
                null);
    }

    private static SourceExecutionReference reference(
            final String executionId,
            final String resourceId) {

        return new SourceExecutionReference(
                new ExecutionId(executionId),
                new ResourceId(resourceId));
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
}