package io.github.avinashio.ozhuku.persistence.postgres;

import io.github.avinashio.ozhuku.domain.audit.AuditEvent;
import io.github.avinashio.ozhuku.domain.audit.AuditEventId;
import io.github.avinashio.ozhuku.domain.audit.AuditEventType;
import io.github.avinashio.ozhuku.foundation.validation.Validation;
import io.github.avinashio.ozhuku.persistence.AuditEventRepository;
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
 * PostgreSQL implementation of the audit event persistence port.
 */
public final class PostgresAuditEventRepository
        implements AuditEventRepository {

    private static final String SAVE_SQL = """
            INSERT INTO audit_events (
                event_id,
                timestamp,
                organization_id,
                actor,
                event_type,
                resource_type,
                resource_id,
                pipeline_id,
                execution_id,
                source_identity,
                previous_state,
                new_state,
                reason,
                outcome,
                correlation_id,
                metadata_json
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (event_id)
            DO NOTHING
            """;

    private static final String FIND_BY_ID_SQL = """
            SELECT
                event_id,
                timestamp,
                organization_id,
                actor,
                event_type,
                resource_type,
                resource_id,
                pipeline_id,
                execution_id,
                source_identity,
                previous_state,
                new_state,
                reason,
                outcome,
                correlation_id,
                metadata_json
            FROM audit_events
            WHERE event_id = ?
            """;

    private static final String FIND_BY_TIMESTAMP_BETWEEN_SQL = """
            SELECT
                event_id,
                timestamp,
                organization_id,
                actor,
                event_type,
                resource_type,
                resource_id,
                pipeline_id,
                execution_id,
                source_identity,
                previous_state,
                new_state,
                reason,
                outcome,
                correlation_id,
                metadata_json
            FROM audit_events
            WHERE timestamp >= ?
              AND timestamp <= ?
            ORDER BY timestamp DESC
            LIMIT ?
            """;

    private static final String FIND_BY_ACTOR_SQL = """
            SELECT
                event_id,
                timestamp,
                organization_id,
                actor,
                event_type,
                resource_type,
                resource_id,
                pipeline_id,
                execution_id,
                source_identity,
                previous_state,
                new_state,
                reason,
                outcome,
                correlation_id,
                metadata_json
            FROM audit_events
            WHERE actor = ?
            ORDER BY timestamp DESC
            LIMIT ?
            """;

    private final DataSource dataSource;

    /**
     * Creates the PostgreSQL audit event repository.
     *
     * @param dataSource PostgreSQL data source
     */
    public PostgresAuditEventRepository(
            final DataSource dataSource) {

        this.dataSource = Validation.requireNonNull(
                dataSource,
                "Data source must not be null");
    }

    @Override
    public void save(final AuditEvent auditEvent) {

        Validation.requireNonNull(
                auditEvent,
                "Audit event must not be null");

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(SAVE_SQL)) {

            statement.setString(1, auditEvent.eventId().value());
            setInstant(statement, 2, auditEvent.timestamp());
            statement.setString(3, auditEvent.organizationId());
            statement.setString(4, auditEvent.actor());
            statement.setString(5, auditEvent.eventType().name());
            statement.setString(6, auditEvent.resourceType());
            statement.setString(7, auditEvent.resourceId());
            setString(statement, 8, auditEvent.pipelineId());
            setString(statement, 9, auditEvent.executionId());
            setString(statement, 10, auditEvent.sourceIdentity());
            setString(statement, 11, auditEvent.previousState());
            setString(statement, 12, auditEvent.newState());
            setString(statement, 13, auditEvent.reason());
            statement.setString(14, auditEvent.outcome());
            statement.setString(15, auditEvent.correlationId());
            setString(statement, 16, toJson(auditEvent.metadata()));

            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Failed to save audit event",
                    exception);
        }
    }

    @Override
    public Optional<AuditEvent> findById(
            final AuditEventId eventId) {

        Validation.requireNonNull(
                eventId,
                "Event ID must not be null");

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(FIND_BY_ID_SQL)) {

            statement.setString(1, eventId.value());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(mapAuditEvent(resultSet));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Failed to find audit event",
                    exception);
        }
    }

    @Override
    public List<AuditEvent> findByTimestampBetween(
            final Instant from,
            final Instant to,
            final int limit) {

        Validation.requireNonNull(
                from,
                "From timestamp must not be null");

        Validation.requireNonNull(
                to,
                "To timestamp must not be null");

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(
                             FIND_BY_TIMESTAMP_BETWEEN_SQL)) {

            setInstant(statement, 1, from);
            setInstant(statement, 2, to);
            statement.setInt(3, limit);

            try (ResultSet resultSet = statement.executeQuery()) {
                final List<AuditEvent> events = new ArrayList<>();
                while (resultSet.next()) {
                    events.add(mapAuditEvent(resultSet));
                }
                return events;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Failed to find audit events by timestamp range",
                    exception);
        }
    }

    @Override
    public List<AuditEvent> findByActor(
            final String actor,
            final int limit) {

        Validation.requireNonBlank(
                actor,
                "Actor must not be blank");

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(FIND_BY_ACTOR_SQL)) {

            statement.setString(1, actor);
            statement.setInt(2, limit);

            try (ResultSet resultSet = statement.executeQuery()) {
                final List<AuditEvent> events = new ArrayList<>();
                while (resultSet.next()) {
                    events.add(mapAuditEvent(resultSet));
                }
                return events;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Failed to find audit events by actor",
                    exception);
        }
    }

    private AuditEvent mapAuditEvent(
            final ResultSet resultSet) throws SQLException {

        return new AuditEvent(
                new AuditEventId(resultSet.getString("event_id")),
                toInstant(resultSet.getObject(
                        "timestamp",
                        OffsetDateTime.class)),
                resultSet.getString("organization_id"),
                resultSet.getString("actor"),
                AuditEventType.valueOf(
                        resultSet.getString("event_type")),
                resultSet.getString("resource_type"),
                resultSet.getString("resource_id"),
                resultSet.getString("pipeline_id"),
                resultSet.getString("execution_id"),
                resultSet.getString("source_identity"),
                resultSet.getString("previous_state"),
                resultSet.getString("new_state"),
                resultSet.getString("reason"),
                resultSet.getString("outcome"),
                resultSet.getString("correlation_id"),
                fromJson(resultSet.getString("metadata_json")));
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

    private static void setString(
            final PreparedStatement statement,
            final int parameterIndex,
            final String value) throws SQLException {

        if (value == null) {
            statement.setObject(parameterIndex, null);
            return;
        }

        statement.setString(parameterIndex, value);
    }

    private static Instant toInstant(
            final OffsetDateTime value) {

        if (value == null) {
            return null;
        }

        return value.toInstant();
    }

    /**
     * Converts a metadata map to a simple JSON string.
     * Each key-value pair is escaped and enclosed in double quotes.
     *
     * @param metadata the metadata map
     * @return JSON string, or null if the map is empty
     */
    private static String toJson(
            final Map<String, String> metadata) {

        if (metadata == null || metadata.isEmpty()) {
            return null;
        }

        final StringBuilder builder = new StringBuilder();
        builder.append('{');

        boolean first = true;
        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            if (!first) {
                builder.append(',');
            }
            first = false;
            builder.append('"')
                    .append(escapeJson(entry.getKey()))
                    .append("\":\"")
                    .append(escapeJson(entry.getValue()))
                    .append('"');
        }

        builder.append('}');
        return builder.toString();
    }

    /**
     * Parses a simple JSON string into a metadata map.
     *
     * @param json the JSON string
     * @return parsed map, or empty map if null or empty
     */
    private static Map<String, String> fromJson(
            final String json) {

        if (json == null || json.isBlank()) {
            return Map.of();
        }

        final Map<String, String> result = new HashMap<>();
        final String trimmed = json.trim();

        if (trimmed.length() < 2
                || trimmed.charAt(0) != '{'
                || trimmed.charAt(trimmed.length() - 1) != '}') {
            return result;
        }

        final String body =
                trimmed.substring(1, trimmed.length() - 1).trim();
        if (body.isEmpty()) {
            return result;
        }

        final String[] pairs = body.split(",");
        for (String pair : pairs) {
            final int colonIndex = pair.indexOf(':');
            if (colonIndex < 0) {
                continue;
            }

            final String key =
                    unescapeJson(
                            pair.substring(1, colonIndex - 1).trim());
            final String value =
                    unescapeJson(
                            pair.substring(colonIndex + 1).trim());
            result.put(key, value);
        }

        return result;
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
