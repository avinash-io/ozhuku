package io.github.avinashio.ozhuku.persistence.postgres;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.avinashio.ozhuku.domain.execution.FlowExecution;
import io.github.avinashio.ozhuku.domain.execution.FlowExecutionStatus;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.FlowId;
import io.github.avinashio.ozhuku.persistence.FlowExecutionRepository;
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
class PostgresFlowExecutionRepositoryIT {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17-alpine")
                    .withDatabaseName("ozhuku")
                    .withUsername("ozhuku")
                    .withPassword("ozhuku-test");

    private static FlowExecutionRepository repository;

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
                new PostgresFlowExecutionRepository(
                        dataSource);
    }

    @Test
    void shouldReturnEmptyForUnknownFlowExecution() {
        final Optional<FlowExecution> result =
                repository.findById(
                        new ExecutionId("execution-unknown"),
                        new FlowId("flow-unknown"));

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldSaveAndRetrievePendingFlowExecution() {
        final FlowExecution flowExecution =
                pendingFlowExecution(
                        "execution-pending",
                        "flow-001");

        repository.save(flowExecution);

        final Optional<FlowExecution> result =
                repository.findById(
                        flowExecution.executionId(),
                        flowExecution.flowId());

        assertTrue(result.isPresent());
        assertEquals(
                flowExecution,
                result.orElseThrow());
    }

    @Test
    void shouldSaveAndRetrieveCompletedFlowExecution() {
        final ExecutionId executionId =
                new ExecutionId("execution-completed");

        final FlowId flowId =
                new FlowId("flow-completed");

        final Instant startedAt =
                Instant.parse(
                        "2026-09-22T10:00:00Z");

        final Instant completedAt =
                Instant.parse(
                        "2026-09-22T10:01:00Z");

        final FlowExecution flowExecution =
                FlowExecution.rehydrate(
                        executionId,
                        flowId,
                        FlowExecutionStatus.COMPLETED,
                        startedAt,
                        completedAt);

        repository.save(flowExecution);

        final Optional<FlowExecution> result =
                repository.findById(
                        executionId,
                        flowId);

        assertTrue(result.isPresent());
        assertEquals(
                flowExecution,
                result.orElseThrow());
    }

    @Test
    void shouldKeepDifferentFlowsSeparate() {
        final FlowExecution first =
                FlowExecution.rehydrate(
                        new ExecutionId("execution-shared"),
                        new FlowId("flow-one"),
                        FlowExecutionStatus.COMPLETED,
                        Instant.parse(
                                "2026-09-22T10:00:00Z"),
                        Instant.parse(
                                "2026-09-22T10:01:00Z"));

        final FlowExecution second =
                FlowExecution.rehydrate(
                        new ExecutionId("execution-shared"),
                        new FlowId("flow-two"),
                        FlowExecutionStatus.FAILED,
                        Instant.parse(
                                "2026-09-22T11:00:00Z"),
                        Instant.parse(
                                "2026-09-22T11:01:00Z"));

        repository.save(first);
        repository.save(second);

        assertEquals(
                first,
                repository.findById(
                                first.executionId(),
                                first.flowId())
                        .orElseThrow());

        assertEquals(
                second,
                repository.findById(
                                second.executionId(),
                                second.flowId())
                        .orElseThrow());
    }

    @Test
    void shouldReplaceExistingFlowExecutionState() {
        final ExecutionId executionId =
                new ExecutionId("execution-replace");

        final FlowId flowId =
                new FlowId("flow-replace");

        final FlowExecution pending =
                pendingFlowExecution(
                        executionId.value(),
                        flowId.value());

        final FlowExecution running =
                pending.start(
                        Instant.parse(
                                "2026-09-22T12:00:00Z"));

        repository.save(pending);
        repository.save(running);

        final Optional<FlowExecution> result =
                repository.findById(
                        executionId,
                        flowId);

        assertTrue(result.isPresent());
        assertEquals(
                running,
                result.orElseThrow());
    }

    private static FlowExecution pendingFlowExecution(
            final String executionId,
            final String flowId) {

        return FlowExecution.rehydrate(
                new ExecutionId(executionId),
                new FlowId(flowId),
                FlowExecutionStatus.PENDING,
                null,
                null);
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