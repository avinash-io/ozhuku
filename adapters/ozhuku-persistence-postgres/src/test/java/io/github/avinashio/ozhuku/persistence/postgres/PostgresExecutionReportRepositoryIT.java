package io.github.avinashio.ozhuku.persistence.postgres;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.avinashio.ozhuku.domain.deduplication.ProcessingStatus;
import io.github.avinashio.ozhuku.domain.execution.ExecutionStatus;
import io.github.avinashio.ozhuku.domain.report.DestinationOutcome;
import io.github.avinashio.ozhuku.domain.report.ExecutionReport;
import io.github.avinashio.ozhuku.persistence.ExecutionReportRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class PostgresExecutionReportRepositoryIT {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17-alpine");

    private static DataSource dataSource;

    private static ExecutionReportRepository repository;

    @BeforeAll
    static void setUp() {
        dataSource = createDataSource();

        Flyway.configure()
                .dataSource(dataSource)
                .load()
                .migrate();

        repository = new PostgresExecutionReportRepository(dataSource);
    }

    @Test
    void shouldReturnEmptyWhenReportDoesNotExist() {
        final Optional<ExecutionReport> result =
                repository.findById("report-nonexistent");

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldSaveAndRetrieveReportWithAllFields() {
        final Instant generatedAt =
                Instant.parse("2026-10-22T12:00:00Z");

        final ExecutionReport report = ExecutionReport.builder()
                .reportId("report-001")
                .generatedAt(generatedAt)
                .pipelineId("pipeline-001")
                .executionId("execution-001")
                .organizationId("org-test")
                .status(ExecutionStatus.COMPLETED)
                .startTime(generatedAt)
                .endTime(generatedAt.plusSeconds(30))
                .filesDiscovered(10)
                .filesProcessed(8)
                .filesFailed(1)
                .filesSkipped(1)
                .destinationOutcomes(List.of(
                        new DestinationOutcome(
                                "dest-001",
                                ProcessingStatus.PROCESSED,
                                "ok"),
                        new DestinationOutcome(
                                "dest-002",
                                ProcessingStatus.PROCESSED,
                                "ok")))
                .overallStatus("SUCCESS")
                .errors(null)
                .auditEvent(null)
                .build();

        repository.save(report);

        final Optional<ExecutionReport> result =
                repository.findById("report-001");

        assertTrue(result.isPresent());

        final ExecutionReport retrieved = result.orElseThrow();

        assertEquals("report-001", retrieved.reportId());
        assertEquals(generatedAt, retrieved.generatedAt());
        assertEquals("pipeline-001", retrieved.pipelineId());
        assertEquals("execution-001", retrieved.executionId());
        assertEquals("org-test", retrieved.organizationId());
        assertEquals(ExecutionStatus.COMPLETED, retrieved.status());
        assertEquals(generatedAt, retrieved.startTime());
        assertEquals(generatedAt.plusSeconds(30), retrieved.endTime());
        assertEquals(10, retrieved.filesDiscovered());
        assertEquals(8, retrieved.filesProcessed());
        assertEquals(1, retrieved.filesFailed());
        assertEquals(1, retrieved.filesSkipped());
        assertEquals(2, retrieved.destinationOutcomes().size());
        assertEquals("dest-001",
                retrieved.destinationOutcomes().get(0).resourceId());
        assertEquals(ProcessingStatus.PROCESSED,
                retrieved.destinationOutcomes().get(0).status());
        assertEquals("SUCCESS", retrieved.overallStatus());
    }

    @Test
    void shouldStoreNullOptionalFields() {
        final ExecutionReport report = ExecutionReport.builder()
                .reportId("report-002")
                .generatedAt(Instant.parse("2026-10-22T14:00:00Z"))
                .pipelineId("pipeline-002")
                .executionId("execution-002")
                .organizationId("org-test")
                .status(ExecutionStatus.FAILED)
                .startTime(null)
                .endTime(null)
                .filesDiscovered(5)
                .filesProcessed(0)
                .filesFailed(5)
                .filesSkipped(0)
                .destinationOutcomes(List.of())
                .overallStatus("FAILURE")
                .errors("Some error occurred")
                .auditEvent(null)
                .build();

        repository.save(report);

        final Optional<ExecutionReport> result =
                repository.findById("report-002");

        assertTrue(result.isPresent());

        final ExecutionReport retrieved = result.orElseThrow();

        assertEquals(ExecutionStatus.FAILED, retrieved.status());
        assertEquals(5, retrieved.filesDiscovered());
        assertEquals(0, retrieved.filesProcessed());
        assertEquals(5, retrieved.filesFailed());
        assertEquals(0, retrieved.filesSkipped());
        assertEquals("FAILURE", retrieved.overallStatus());
        assertEquals("Some error occurred", retrieved.errors());
        assertTrue(retrieved.destinationOutcomes().isEmpty());
    }

    @Test
    void shouldFindReportsByExecutionId() {
        final Instant generatedAt =
                Instant.parse("2026-10-22T15:00:00Z");

        final ExecutionReport firstReport = ExecutionReport.builder()
                .reportId("report-exec-1")
                .generatedAt(generatedAt)
                .pipelineId("pipeline-A")
                .executionId("execution-target")
                .organizationId("org-test")
                .status(ExecutionStatus.COMPLETED)
                .overallStatus("SUCCESS")
                .destinationOutcomes(List.of())
                .build();

        final ExecutionReport secondReport = ExecutionReport.builder()
                .reportId("report-exec-2")
                .generatedAt(generatedAt.plusSeconds(60))
                .pipelineId("pipeline-A")
                .executionId("execution-target")
                .organizationId("org-test")
                .status(ExecutionStatus.COMPLETED)
                .overallStatus("SUCCESS")
                .destinationOutcomes(List.of())
                .build();

        repository.save(firstReport);
        repository.save(secondReport);

        final List<ExecutionReport> results =
                repository.findByExecutionId("execution-target");

        assertEquals(2, results.size());

        // Verify ordering by generatedAt descending
        assertEquals("report-exec-2", results.get(0).reportId());
        assertEquals("report-exec-1", results.get(1).reportId());
    }

    @Test
    void shouldReturnEmptyListWhenExecutionIdNotFound() {
        final List<ExecutionReport> results =
                repository.findByExecutionId("nonexistent-execution");

        assertTrue(results.isEmpty());
    }

    @Test
    void shouldOverwriteExistingReport() {
        final Instant generatedAt =
                Instant.parse("2026-10-22T16:00:00Z");

        final ExecutionReport report = ExecutionReport.builder()
                .reportId("report-overwrite")
                .generatedAt(generatedAt)
                .pipelineId("pipeline-001")
                .executionId("execution-001")
                .organizationId("org-test")
                .status(ExecutionStatus.PENDING)
                .overallStatus("PENDING")
                .destinationOutcomes(List.of())
                .build();

        repository.save(report);

        final ExecutionReport updated = ExecutionReport.builder()
                .reportId("report-overwrite")
                .generatedAt(generatedAt)
                .pipelineId("pipeline-001")
                .executionId("execution-001")
                .organizationId("org-test")
                .status(ExecutionStatus.COMPLETED)
                .overallStatus("SUCCESS")
                .destinationOutcomes(List.of(
                        new DestinationOutcome(
                                "dest-001",
                                ProcessingStatus.PROCESSED,
                                "ok")))
                .build();

        repository.save(updated);

        final Optional<ExecutionReport> result =
                repository.findById("report-overwrite");

        assertTrue(result.isPresent());

        final ExecutionReport retrieved = result.orElseThrow();
        assertEquals(ExecutionStatus.COMPLETED, retrieved.status());
        assertEquals("SUCCESS", retrieved.overallStatus());
        assertEquals(1, retrieved.destinationOutcomes().size());
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
