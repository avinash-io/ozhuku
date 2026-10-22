package io.github.avinashio.ozhuku.domain.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.domain.audit.AuditEvent;
import io.github.avinashio.ozhuku.domain.audit.AuditEventId;
import io.github.avinashio.ozhuku.domain.audit.AuditEventType;
import io.github.avinashio.ozhuku.domain.deduplication.ProcessingStatus;
import io.github.avinashio.ozhuku.domain.execution.ExecutionStatus;
import io.github.avinashio.ozhuku.foundation.exception.ValidationException;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class ExecutionReportTest {

    private static final Instant FIXED_TIME =
            Instant.parse("2026-10-22T12:00:00Z");

    @Test
    void shouldCreateExecutionReportWithBuilder() {
        final ExecutionReport report = ExecutionReport.builder()
                .reportId("report-001")
                .generatedAt(FIXED_TIME)
                .pipelineId("pipeline-001")
                .executionId("execution-001")
                .organizationId("org-test")
                .status(ExecutionStatus.COMPLETED)
                .startTime(FIXED_TIME)
                .endTime(FIXED_TIME.plusSeconds(30))
                .filesDiscovered(10)
                .filesProcessed(8)
                .filesFailed(1)
                .filesSkipped(1)
                .destinationOutcomes(List.of(
                        new DestinationOutcome("dest-001",
                                ProcessingStatus.PROCESSED, "ok")))
                .overallStatus("SUCCESS")
                .errors(null)
                .auditEvent(null)
                .build();

        assertEquals("report-001", report.reportId());
        assertEquals(FIXED_TIME, report.generatedAt());
        assertEquals("pipeline-001", report.pipelineId());
        assertEquals("execution-001", report.executionId());
        assertEquals("org-test", report.organizationId());
        assertEquals(ExecutionStatus.COMPLETED, report.status());
        assertEquals(FIXED_TIME, report.startTime());
        assertEquals(FIXED_TIME.plusSeconds(30), report.endTime());
        assertEquals(10, report.filesDiscovered());
        assertEquals(8, report.filesProcessed());
        assertEquals(1, report.filesFailed());
        assertEquals(1, report.filesSkipped());
        assertEquals(1, report.destinationOutcomes().size());
        assertEquals("SUCCESS", report.overallStatus());
        assertNull(report.errors());
        assertNull(report.auditEvent());
    }

    @Test
    void shouldCreateReportWithAuditEvent() {
        final AuditEvent auditEvent = new AuditEvent(
                new AuditEventId("audit-001"),
                FIXED_TIME,
                "org-test",
                "admin",
                AuditEventType.REPROCESSING,
                "pipeline",
                "pipeline-001",
                "pipeline-001",
                "execution-001",
                null,
                null,
                null,
                "Manual reprocess",
                "success",
                "corr-001",
                null);

        final ExecutionReport report = ExecutionReport.builder()
                .reportId("report-002")
                .generatedAt(FIXED_TIME)
                .pipelineId("pipeline-001")
                .executionId("execution-001")
                .organizationId("org-test")
                .status(ExecutionStatus.COMPLETED)
                .overallStatus("SUCCESS")
                .destinationOutcomes(List.of())
                .auditEvent(auditEvent)
                .build();

        assertNotNull(report.auditEvent());
        assertEquals("audit-001", report.auditEvent().eventId().value());
    }

    @Test
    void shouldRejectBlankReportId() {
        assertThrows(
                ValidationException.class,
                () -> ExecutionReport.builder()
                        .reportId("  ")
                        .generatedAt(FIXED_TIME)
                        .pipelineId("pipeline-001")
                        .executionId("execution-001")
                        .organizationId("org-test")
                        .status(ExecutionStatus.COMPLETED)
                        .overallStatus("SUCCESS")
                        .destinationOutcomes(List.of())
                        .build());
    }

    @Test
    void shouldRejectNullGeneratedAt() {
        assertThrows(
                ValidationException.class,
                () -> ExecutionReport.builder()
                        .reportId("report-003")
                        .generatedAt(null)
                        .pipelineId("pipeline-001")
                        .executionId("execution-001")
                        .organizationId("org-test")
                        .status(ExecutionStatus.COMPLETED)
                        .overallStatus("SUCCESS")
                        .destinationOutcomes(List.of())
                        .build());
    }

    @Test
    void shouldRejectBlankPipelineId() {
        assertThrows(
                ValidationException.class,
                () -> ExecutionReport.builder()
                        .reportId("report-004")
                        .generatedAt(FIXED_TIME)
                        .pipelineId("  ")
                        .executionId("execution-001")
                        .organizationId("org-test")
                        .status(ExecutionStatus.COMPLETED)
                        .overallStatus("SUCCESS")
                        .destinationOutcomes(List.of())
                        .build());
    }

    @Test
    void shouldRejectNullStatus() {
        assertThrows(
                ValidationException.class,
                () -> ExecutionReport.builder()
                        .reportId("report-005")
                        .generatedAt(FIXED_TIME)
                        .pipelineId("pipeline-001")
                        .executionId("execution-001")
                        .organizationId("org-test")
                        .status(null)
                        .overallStatus("SUCCESS")
                        .destinationOutcomes(List.of())
                        .build());
    }

    @Test
    void shouldRejectNullOverallStatus() {
        assertThrows(
                ValidationException.class,
                () -> ExecutionReport.builder()
                        .reportId("report-006")
                        .generatedAt(FIXED_TIME)
                        .pipelineId("pipeline-001")
                        .executionId("execution-001")
                        .organizationId("org-test")
                        .status(ExecutionStatus.COMPLETED)
                        .overallStatus(null)
                        .destinationOutcomes(List.of())
                        .build());
    }

    @Test
    void shouldRejectNullDestinationOutcomes() {
        assertThrows(
                NullPointerException.class,
                () -> ExecutionReport.builder()
                        .reportId("report-007")
                        .generatedAt(FIXED_TIME)
                        .pipelineId("pipeline-001")
                        .executionId("execution-001")
                        .organizationId("org-test")
                        .status(ExecutionStatus.COMPLETED)
                        .overallStatus("SUCCESS")
                        .destinationOutcomes(null)
                        .build());
    }

    @Test
    void shouldReturnImmutableDestinationOutcomes() {
        final ExecutionReport report = ExecutionReport.builder()
                .reportId("report-008")
                .generatedAt(FIXED_TIME)
                .pipelineId("pipeline-001")
                .executionId("execution-001")
                .organizationId("org-test")
                .status(ExecutionStatus.COMPLETED)
                .overallStatus("SUCCESS")
                .destinationOutcomes(List.of(
                        new DestinationOutcome("dest-001",
                                ProcessingStatus.PROCESSED, "ok")))
                .build();

        final List<DestinationOutcome> outcomes =
                report.destinationOutcomes();

        assertThrows(
                UnsupportedOperationException.class,
                () -> outcomes.add(
                        new DestinationOutcome("dest-002",
                                ProcessingStatus.NOT_PROCESSED, "failed")));
    }

    @Test
    void shouldCompareByReportId() {
        final ExecutionReport first = ExecutionReport.builder()
                .reportId("report-same")
                .generatedAt(FIXED_TIME)
                .pipelineId("pipeline-001")
                .executionId("execution-001")
                .organizationId("org-test")
                .status(ExecutionStatus.COMPLETED)
                .overallStatus("SUCCESS")
                .destinationOutcomes(List.of())
                .build();

        final ExecutionReport second = ExecutionReport.builder()
                .reportId("report-same")
                .generatedAt(FIXED_TIME.plusSeconds(60))
                .pipelineId("pipeline-002")
                .executionId("execution-002")
                .organizationId("org-other")
                .status(ExecutionStatus.FAILED)
                .overallStatus("FAILURE")
                .destinationOutcomes(List.of())
                .build();

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldNotEqualDifferentReportId() {
        final ExecutionReport first = ExecutionReport.builder()
                .reportId("report-a")
                .generatedAt(FIXED_TIME)
                .pipelineId("pipeline-001")
                .executionId("execution-001")
                .organizationId("org-test")
                .status(ExecutionStatus.COMPLETED)
                .overallStatus("SUCCESS")
                .destinationOutcomes(List.of())
                .build();

        final ExecutionReport second = ExecutionReport.builder()
                .reportId("report-b")
                .generatedAt(FIXED_TIME)
                .pipelineId("pipeline-001")
                .executionId("execution-001")
                .organizationId("org-test")
                .status(ExecutionStatus.COMPLETED)
                .overallStatus("SUCCESS")
                .destinationOutcomes(List.of())
                .build();

        assertNotEquals(first, second);
    }

    @Test
    void shouldNotEqualNull() {
        final ExecutionReport report = ExecutionReport.builder()
                .reportId("report-009")
                .generatedAt(FIXED_TIME)
                .pipelineId("pipeline-001")
                .executionId("execution-001")
                .organizationId("org-test")
                .status(ExecutionStatus.COMPLETED)
                .overallStatus("SUCCESS")
                .destinationOutcomes(List.of())
                .build();

        assertNotEquals(report, null);
    }

    @Test
    void shouldNotEqualOtherType() {
        final ExecutionReport report = ExecutionReport.builder()
                .reportId("report-010")
                .generatedAt(FIXED_TIME)
                .pipelineId("pipeline-001")
                .executionId("execution-001")
                .organizationId("org-test")
                .status(ExecutionStatus.COMPLETED)
                .overallStatus("SUCCESS")
                .destinationOutcomes(List.of())
                .build();

        assertNotEquals(report, "some-string");
    }
}
