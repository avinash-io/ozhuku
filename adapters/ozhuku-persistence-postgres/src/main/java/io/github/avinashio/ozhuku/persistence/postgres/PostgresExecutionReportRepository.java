package io.github.avinashio.ozhuku.persistence.postgres;

import io.github.avinashio.ozhuku.domain.audit.AuditEvent;
import io.github.avinashio.ozhuku.domain.audit.AuditEventId;
import io.github.avinashio.ozhuku.domain.audit.AuditEventType;
import io.github.avinashio.ozhuku.domain.execution.ExecutionStatus;
import io.github.avinashio.ozhuku.domain.report.DestinationOutcome;
import io.github.avinashio.ozhuku.domain.report.ExecutionReport;
import io.github.avinashio.ozhuku.domain.deduplication.ProcessingStatus;
import io.github.avinashio.ozhuku.foundation.validation.Validation;
import io.github.avinashio.ozhuku.persistence.ExecutionReportRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;

/**
 * PostgreSQL implementation of the execution report persistence port.
 */
public final class PostgresExecutionReportRepository
        implements ExecutionReportRepository {

    private static final String SAVE_SQL = """
            INSERT INTO execution_reports (
                report_id,
                generated_at,
                pipeline_id,
                execution_id,
                organization_id,
                status,
                start_time,
                end_time,
                files_discovered,
                files_processed,
                files_failed,
                files_skipped,
                destination_outcomes_json,
                overall_status,
                errors,
                audit_event_id
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (report_id)
            DO UPDATE SET
                generated_at = EXCLUDED.generated_at,
                pipeline_id = EXCLUDED.pipeline_id,
                execution_id = EXCLUDED.execution_id,
                organization_id = EXCLUDED.organization_id,
                status = EXCLUDED.status,
                start_time = EXCLUDED.start_time,
                end_time = EXCLUDED.end_time,
                files_discovered = EXCLUDED.files_discovered,
                files_processed = EXCLUDED.files_processed,
                files_failed = EXCLUDED.files_failed,
                files_skipped = EXCLUDED.files_skipped,
                destination_outcomes_json = EXCLUDED.destination_outcomes_json,
                overall_status = EXCLUDED.overall_status,
                errors = EXCLUDED.errors,
                audit_event_id = EXCLUDED.audit_event_id
            """;

    private static final String FIND_BY_ID_SQL = """
            SELECT
                report_id,
                generated_at,
                pipeline_id,
                execution_id,
                organization_id,
                status,
                start_time,
                end_time,
                files_discovered,
                files_processed,
                files_failed,
                files_skipped,
                destination_outcomes_json,
                overall_status,
                errors,
                audit_event_id
            FROM execution_reports
            WHERE report_id = ?
            """;

    private static final String FIND_BY_EXECUTION_ID_SQL = """
            SELECT
                report_id,
                generated_at,
                pipeline_id,
                execution_id,
                organization_id,
                status,
                start_time,
                end_time,
                files_discovered,
                files_processed,
                files_failed,
                files_skipped,
                destination_outcomes_json,
                overall_status,
                errors,
                audit_event_id
            FROM execution_reports
            WHERE execution_id = ?
            ORDER BY generated_at DESC
            """;

    private final DataSource dataSource;

    /**
     * Creates the PostgreSQL execution report repository.
     *
     * @param dataSource PostgreSQL data source
     */
    public PostgresExecutionReportRepository(
            final DataSource dataSource) {

        this.dataSource = Validation.requireNonNull(
                dataSource,
                "Data source must not be null");
    }

    @Override
    public void save(final ExecutionReport executionReport) {

        Validation.requireNonNull(
                executionReport,
                "Execution report must not be null");

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(SAVE_SQL)) {

            statement.setString(1, executionReport.reportId());
            setInstant(statement, 2, executionReport.generatedAt());
            statement.setString(3, executionReport.pipelineId());
            statement.setString(4, executionReport.executionId());
            statement.setString(5, executionReport.organizationId());
            statement.setString(6, executionReport.status().name());
            setInstant(statement, 7, executionReport.startTime());
            setInstant(statement, 8, executionReport.endTime());
            statement.setInt(9, executionReport.filesDiscovered());
            statement.setInt(10, executionReport.filesProcessed());
            statement.setInt(11, executionReport.filesFailed());
            statement.setInt(12, executionReport.filesSkipped());
            statement.setString(13, toJson(executionReport.destinationOutcomes()));
            statement.setString(14, executionReport.overallStatus());
            statement.setString(15, executionReport.errors());
            if (executionReport.auditEvent() != null) {
                statement.setString(
                        16,
                        executionReport.auditEvent().eventId().value());
            } else {
                statement.setObject(16, null);
            }

            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Failed to save execution report",
                    exception);
        }
    }

    @Override
    public Optional<ExecutionReport> findById(
            final String reportId) {

        Validation.requireNonNull(reportId, "Report ID must not be null");

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(FIND_BY_ID_SQL)) {

            statement.setString(1, reportId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(mapExecutionReport(resultSet));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Failed to find execution report",
                    exception);
        }
    }

    @Override
    public List<ExecutionReport> findByExecutionId(
            final String executionId) {

        Validation.requireNonBlank(
                executionId,
                "Execution ID must not be blank");

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(FIND_BY_EXECUTION_ID_SQL)) {

            statement.setString(1, executionId);

            try (ResultSet resultSet = statement.executeQuery()) {
                final List<ExecutionReport> reports = new ArrayList<>();
                while (resultSet.next()) {
                    reports.add(mapExecutionReport(resultSet));
                }
                return reports;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Failed to find execution reports by execution ID",
                    exception);
        }
    }

    private ExecutionReport mapExecutionReport(
            final ResultSet resultSet) throws SQLException {

        return ExecutionReport.builder()
                .reportId(resultSet.getString("report_id"))
                .generatedAt(toInstant(
                        resultSet.getObject(
                                "generated_at",
                                OffsetDateTime.class)))
                .pipelineId(resultSet.getString("pipeline_id"))
                .executionId(resultSet.getString("execution_id"))
                .organizationId(resultSet.getString("organization_id"))
                .status(ExecutionStatus.valueOf(
                        resultSet.getString("status")))
                .startTime(toInstant(
                        resultSet.getObject(
                                "start_time",
                                OffsetDateTime.class)))
                .endTime(toInstant(
                        resultSet.getObject(
                                "end_time",
                                OffsetDateTime.class)))
                .filesDiscovered(
                        resultSet.getInt("files_discovered"))
                .filesProcessed(
                        resultSet.getInt("files_processed"))
                .filesFailed(
                        resultSet.getInt("files_failed"))
                .filesSkipped(
                        resultSet.getInt("files_skipped"))
                .destinationOutcomes(
                        fromJson(resultSet.getString("destination_outcomes_json")))
                .overallStatus(resultSet.getString("overall_status"))
                .errors(resultSet.getString("errors"))
                .auditEvent(mapAuditEvent(resultSet))
                .build();
    }

    private AuditEvent mapAuditEvent(
            final ResultSet resultSet) throws SQLException {

        final String auditEventId =
                resultSet.getString("audit_event_id");

        if (auditEventId == null) {
            return null;
        }

        return new AuditEvent(
                new AuditEventId(auditEventId),
                Instant.now(),
                resultSet.getString("organization_id"),
                "system",
                AuditEventType.REPROCESSING,
                "execution_report",
                resultSet.getString("execution_id"),
                resultSet.getString("pipeline_id"),
                resultSet.getString("execution_id"),
                null,
                null,
                null,
                null,
                "success",
                resultSet.getString("report_id"),
                null);
    }

    private static void setInstant(
            final PreparedStatement statement,
            final int parameterIndex,
            final Instant value) throws SQLException {

        if (value == null) {
            statement.setObject(parameterIndex, null);
            return;
        }

        statement.setObject(
                parameterIndex,
                value.atOffset(ZoneOffset.UTC));
    }

    private static Instant toInstant(
            final OffsetDateTime value) {

        if (value == null) {
            return null;
        }

        return value.toInstant();
    }

    private static String toJson(
            final List<DestinationOutcome> outcomes) {

        if (outcomes == null || outcomes.isEmpty()) {
            return null;
        }

        final StringBuilder builder = new StringBuilder();
        builder.append('[');

        boolean first = true;
        for (DestinationOutcome outcome : outcomes) {
            if (!first) {
                builder.append(',');
            }
            first = false;
            builder.append('{')
                    .append("\"resourceId\":\"")
                    .append(escapeJson(outcome.resourceId()))
                    .append("\",")
                    .append("\"status\":\"")
                    .append(outcome.status().name())
                    .append("\",")
                    .append("\"detail\":\"")
                    .append(escapeJson(outcome.detail()))
                    .append("\"")
                    .append('}');
        }

        builder.append(']');
        return builder.toString();
    }

    private static List<DestinationOutcome> fromJson(
            final String json) {

        if (json == null || json.isBlank()) {
            return List.of();
        }

        final List<DestinationOutcome> result = new ArrayList<>();
        final String trimmed = json.trim();

        if (trimmed.length() < 2
                || trimmed.charAt(0) != '['
                || trimmed.charAt(trimmed.length() - 1) != ']') {
            return result;
        }

        final String body =
                trimmed.substring(1, trimmed.length() - 1).trim();
        if (body.isEmpty()) {
            return result;
        }

        // Simple JSON parsing - split on object boundaries
        int depth = 0;
        int start = -1;
        for (int i = 0; i < body.length(); i++) {
            final char c = body.charAt(i);
            if (c == '{') {
                if (depth == 0) {
                    start = i;
                }
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    final String objStr =
                            body.substring(start, i + 1);
                    result.add(parseDestinationOutcome(objStr));
                }
            }
        }

        return result;
    }

    private static DestinationOutcome parseDestinationOutcome(
            final String json) {

        String resourceId = null;
        ProcessingStatus status = null;
        String detail = null;

        final String trimmed = json.trim();
        if (trimmed.length() < 2
                || trimmed.charAt(0) != '{'
                || trimmed.charAt(trimmed.length() - 1) != '}') {
            return new DestinationOutcome(null, null, null);
        }

        final String body =
                trimmed.substring(1, trimmed.length() - 1).trim();
        if (body.isEmpty()) {
            return new DestinationOutcome(null, null, null);
        }

        // Simple key-value parsing
        final String[] pairs = body.split(",");
        for (String pair : pairs) {
            final int colonIndex = pair.indexOf(':');
            if (colonIndex < 0) {
                continue;
            }

            final String key =
                    unescapeJson(pair.substring(0, colonIndex).trim());
            final String value =
                    unescapeJson(pair.substring(colonIndex + 1).trim());

            if ("resourceId".equals(key)) {
                resourceId = value;
            } else if ("status".equals(key)) {
                status = ProcessingStatus.valueOf(value);
            } else if ("detail".equals(key)) {
                detail = value;
            }
        }

        return new DestinationOutcome(resourceId, status, detail);
    }

    private static String escapeJson(final String value) {
        if (value == null) {
            return "";
        }

        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            final char c = value.charAt(i);
            switch (c) {
                case '"':
                    builder.append("\\\"");
                    break;
                case '\\':
                    builder.append("\\\\");
                    break;
                case '\n':
                    builder.append("\\n");
                    break;
                case '\r':
                    builder.append("\\r");
                    break;
                case '\t':
                    builder.append("\\t");
                    break;
                default:
                    builder.append(c);
                    break;
            }
        }

        return builder.toString();
    }

    private static String unescapeJson(final String value) {
        if (value == null || value.length() < 2) {
            return value;
        }

        final String trimmed = value.trim();
        final String content;
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            content = trimmed.substring(1, trimmed.length() - 1);
        } else {
            content = trimmed;
        }

        return content.replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }
}
