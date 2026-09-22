package io.github.avinashio.ozhuku.persistence.postgres;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.avinashio.ozhuku.domain.execution.Execution;
import io.github.avinashio.ozhuku.domain.execution.ExecutionStatus;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.execution.ExecutionReference;
import io.github.avinashio.ozhuku.domain.identity.PipelineId;
import io.github.avinashio.ozhuku.domain.identity.PipelineVersion;
import io.github.avinashio.ozhuku.persistence.ExecutionRepository;
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
class PostgresExecutionRepositoryIT {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17-alpine")
                    .withDatabaseName("ozhuku")
                    .withUsername("ozhuku")
                    .withPassword("ozhuku-test");

    private static ExecutionRepository repository;

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
                new PostgresExecutionRepository(
                        dataSource);
    }

    @Test
    void shouldReturnEmptyForUnknownExecution() {
        final Optional<Execution> result =
                repository.findById(
                        new ExecutionId("execution-unknown"));

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldSaveAndRetrievePendingExecution() {
        final Execution execution =
                pendingExecution("execution-pending");

        repository.save(execution);

        final Optional<Execution> result =
                repository.findById(
                        execution.reference().executionId());

        assertTrue(result.isPresent());
        assertEquals(
                execution,
                result.orElseThrow());
    }

    @Test
    void shouldSaveAndRetrieveCompletedExecution() {
        final ExecutionReference reference =
                reference("execution-completed");

        final Instant startedAt =
                Instant.parse(
                        "2026-09-22T10:00:00Z");

        final Instant completedAt =
                Instant.parse(
                        "2026-09-22T10:01:00Z");

        final Execution execution =
                Execution.rehydrate(
                        reference,
                        ExecutionStatus.COMPLETED,
                        startedAt,
                        completedAt);

        repository.save(execution);

        final Optional<Execution> result =
                repository.findById(
                        reference.executionId());

        assertTrue(result.isPresent());
        assertEquals(
                execution,
                result.orElseThrow());
    }

    @Test
    void shouldReplaceExistingExecutionState() {
        final ExecutionReference reference =
                reference("execution-replace");

        final Execution pending =
                pendingExecution(
                        "execution-replace");

        final Execution running =
                pending.start(
                        Instant.parse(
                                "2026-09-22T10:00:00Z"));

        repository.save(pending);
        repository.save(running);

        final Optional<Execution> result =
                repository.findById(
                        reference.executionId());

        assertTrue(result.isPresent());
        assertEquals(
                running,
                result.orElseThrow());
    }

    private static Execution pendingExecution(
            final String executionId) {

        return Execution.rehydrate(
                reference(executionId),
                ExecutionStatus.PENDING,
                null,
                null);
    }

    private static ExecutionReference reference(
            final String executionId) {

        return new ExecutionReference(
                new ExecutionId(executionId),
                new PipelineId("pipeline-001"),
                new PipelineVersion(1));
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

    @Test
    void shouldKeepDifferentExecutionsSeparate() {
        final Execution first =
                Execution.rehydrate(
                        reference("execution-one"),
                        ExecutionStatus.COMPLETED,
                        Instant.parse(
                                "2026-09-22T10:00:00Z"),
                        Instant.parse(
                                "2026-09-22T10:01:00Z"));

        final Execution second =
                Execution.rehydrate(
                        reference("execution-two"),
                        ExecutionStatus.FAILED,
                        Instant.parse(
                                "2026-09-22T11:00:00Z"),
                        Instant.parse(
                                "2026-09-22T11:01:00Z"));

        repository.save(first);
        repository.save(second);

        assertEquals(
                first,
                repository.findById(
                                first.reference().executionId())
                        .orElseThrow());

        assertEquals(
                second,
                repository.findById(
                                second.reference().executionId())
                        .orElseThrow());
    }

    @Test
    void shouldPersistCancelledPendingExecution() {
        final Execution pending =
                pendingExecution("execution-cancelled");

        final Execution cancelled =
                pending.cancel(
                        Instant.parse(
                                "2026-09-22T12:00:00Z"));

        repository.save(cancelled);

        final Optional<Execution> result =
                repository.findById(
                        cancelled.reference().executionId());

        assertTrue(result.isPresent());
        assertEquals(
                cancelled,
                result.orElseThrow());
    }
}