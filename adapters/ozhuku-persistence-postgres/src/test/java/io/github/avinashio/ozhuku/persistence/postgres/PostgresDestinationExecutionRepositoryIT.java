package io.github.avinashio.ozhuku.persistence.postgres;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.avinashio.ozhuku.domain.execution.DestinationExecution;
import io.github.avinashio.ozhuku.domain.execution.DestinationExecutionReference;
import io.github.avinashio.ozhuku.domain.execution.DestinationExecutionStatus;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.persistence.DestinationExecutionRepository;
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
class PostgresDestinationExecutionRepositoryIT {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17-alpine");

    private static DataSource dataSource;

    private static DestinationExecutionRepository repository;

    @BeforeAll
    static void setUp() {
        dataSource = createDataSource();

        Flyway.configure()
                .dataSource(dataSource)
                .load()
                .migrate();

        repository =
                new PostgresDestinationExecutionRepository(
                        dataSource);
    }

    @Test
    void shouldReturnEmptyWhenDestinationExecutionDoesNotExist() {
        final ExecutionId executionId =
                new ExecutionId("execution-unknown");

        final ResourceId resourceId =
                new ResourceId("resource-unknown");

        final Optional<DestinationExecution> result =
                repository.findById(
                        executionId,
                        resourceId);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldSaveAndRetrievePendingDestinationExecution() {
        final DestinationExecution execution =
                pendingDestinationExecution(
                        "execution-pending",
                        "resource-pending");

        repository.save(execution);

        final Optional<DestinationExecution> result =
                repository.findById(
                        execution.reference().executionId(),
                        execution.reference().resourceId());

        assertTrue(result.isPresent());
        assertEquals(
                execution,
                result.orElseThrow());
    }

    @Test
    void shouldSaveAndRetrieveCompletedDestinationExecution() {
        final Instant startedAt =
                Instant.parse("2026-09-22T10:00:00Z");

        final Instant completedAt =
                Instant.parse("2026-09-22T10:00:05Z");

        final DestinationExecution execution =
                DestinationExecution.rehydrate(
                        new DestinationExecutionReference(
                                new ExecutionId("execution-completed"),
                                new ResourceId("resource-completed")),
                        DestinationExecutionStatus.COMPLETED,
                        startedAt,
                        completedAt);

        repository.save(execution);

        final Optional<DestinationExecution> result =
                repository.findById(
                        execution.reference().executionId(),
                        execution.reference().resourceId());

        assertTrue(result.isPresent());

        final DestinationExecution retrieved =
                result.orElseThrow();

        assertEquals(
                DestinationExecutionStatus.COMPLETED,
                retrieved.status());
        assertEquals(
                startedAt,
                retrieved.startedAt());
        assertEquals(
                completedAt,
                retrieved.completedAt());
    }

    @Test
    void shouldKeepDifferentResourcesSeparateForSameExecution() {
        final ExecutionId executionId =
                new ExecutionId("execution-multiple-resources");

        final DestinationExecution first =
                pendingDestinationExecution(
                        executionId.value(),
                        "resource-one");

        final DestinationExecution second =
                pendingDestinationExecution(
                        executionId.value(),
                        "resource-two");

        repository.save(first);
        repository.save(second);

        final Optional<DestinationExecution> firstResult =
                repository.findById(
                        executionId,
                        new ResourceId("resource-one"));

        final Optional<DestinationExecution> secondResult =
                repository.findById(
                        executionId,
                        new ResourceId("resource-two"));

        assertTrue(firstResult.isPresent());
        assertTrue(secondResult.isPresent());

        assertEquals(
                first,
                firstResult.orElseThrow());

        assertEquals(
                second,
                secondResult.orElseThrow());
    }

    @Test
    void shouldReplaceExistingDestinationExecutionState() {
        final ExecutionId executionId =
                new ExecutionId("execution-replace");

        final ResourceId resourceId =
                new ResourceId("resource-replace");

        final DestinationExecution pending =
                DestinationExecution.rehydrate(
                        new DestinationExecutionReference(
                                executionId,
                                resourceId),
                        DestinationExecutionStatus.PENDING,
                        null,
                        null);

        final Instant startedAt =
                Instant.parse("2026-09-22T11:00:00Z");

        final Instant completedAt =
                Instant.parse("2026-09-22T11:00:10Z");

        final DestinationExecution completed =
                DestinationExecution.rehydrate(
                        new DestinationExecutionReference(
                                executionId,
                                resourceId),
                        DestinationExecutionStatus.COMPLETED,
                        startedAt,
                        completedAt);

        repository.save(pending);
        repository.save(completed);

        final Optional<DestinationExecution> result =
                repository.findById(
                        executionId,
                        resourceId);

        assertTrue(result.isPresent());
        assertEquals(
                completed,
                result.orElseThrow());
    }

    private static DestinationExecution pendingDestinationExecution(
            final String executionId,
            final String resourceId) {

        return DestinationExecution.rehydrate(
                new DestinationExecutionReference(
                        new ExecutionId(executionId),
                        new ResourceId(resourceId)),
                DestinationExecutionStatus.PENDING,
                null,
                null);
    }

    private static DataSource createDataSource() {
        final org.postgresql.ds.PGSimpleDataSource dataSource =
                new org.postgresql.ds.PGSimpleDataSource();

        dataSource.setServerNames(
                new String[]{POSTGRES.getHost()});
        dataSource.setPortNumbers(
                new int[]{POSTGRES.getMappedPort(5432)});
        dataSource.setDatabaseName(
                POSTGRES.getDatabaseName());
        dataSource.setUser(
                POSTGRES.getUsername());
        dataSource.setPassword(
                POSTGRES.getPassword());

        return dataSource;
    }
}