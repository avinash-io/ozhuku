package io.github.avinashio.ozhuku.persistence.postgres;

import io.github.avinashio.ozhuku.domain.execution.DestinationExecution;
import io.github.avinashio.ozhuku.domain.execution.DestinationExecutionReference;
import io.github.avinashio.ozhuku.domain.execution.DestinationExecutionStatus;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.persistence.DestinationExecutionRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Optional;
import javax.sql.DataSource;

public final class PostgresDestinationExecutionRepository
        implements DestinationExecutionRepository {

    private static final String FIND_BY_ID_SQL = """
            SELECT
                execution_id,
                resource_id,
                status,
                started_at,
                completed_at
            FROM destination_executions
            WHERE execution_id = ?
              AND resource_id = ?
            """;

    private static final String SAVE_SQL = """
            INSERT INTO destination_executions (
                execution_id,
                resource_id,
                status,
                started_at,
                completed_at
            )
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (execution_id, resource_id)
            DO UPDATE SET
                status = EXCLUDED.status,
                started_at = EXCLUDED.started_at,
                completed_at = EXCLUDED.completed_at
            """;

    private final DataSource dataSource;

    public PostgresDestinationExecutionRepository(
            final DataSource dataSource) {

        this.dataSource = Objects.requireNonNull(
                dataSource,
                "dataSource must not be null");
    }

    @Override
    public Optional<DestinationExecution> findById(
            final ExecutionId executionId,
            final ResourceId resourceId) {

        Objects.requireNonNull(
                executionId,
                "executionId must not be null");
        Objects.requireNonNull(
                resourceId,
                "resourceId must not be null");

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(FIND_BY_ID_SQL)) {

            statement.setString(1, executionId.value());
            statement.setString(2, resourceId.value());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(mapDestinationExecution(resultSet));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Failed to find destination execution",
                    exception);
        }
    }

    @Override
    public void save(
            final DestinationExecution destinationExecution) {

        Objects.requireNonNull(
                destinationExecution,
                "destinationExecution must not be null");

        final DestinationExecutionReference reference =
                destinationExecution.reference();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(SAVE_SQL)) {

            statement.setString(
                    1,
                    reference.executionId().value());
            statement.setString(
                    2,
                    reference.resourceId().value());
            statement.setString(
                    3,
                    destinationExecution.status().name());

            setTimestamp(
                    statement,
                    4,
                    destinationExecution.startedAt());

            setTimestamp(
                    statement,
                    5,
                    destinationExecution.completedAt());

            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Failed to save destination execution",
                    exception);
        }
    }

    private static DestinationExecution mapDestinationExecution(
            final ResultSet resultSet) throws SQLException {

        final ExecutionId executionId =
                new ExecutionId(
                        resultSet.getString("execution_id"));

        final ResourceId resourceId =
                new ResourceId(
                        resultSet.getString("resource_id"));

        final DestinationExecutionStatus status =
                DestinationExecutionStatus.valueOf(
                        resultSet.getString("status"));

        final Instant startedAt =
                toInstant(
                        resultSet.getObject(
                                "started_at",
                                OffsetDateTime.class));

        final Instant completedAt =
                toInstant(
                        resultSet.getObject(
                                "completed_at",
                                OffsetDateTime.class));

        final DestinationExecutionReference reference =
                new DestinationExecutionReference(
                        executionId,
                        resourceId);

        return DestinationExecution.rehydrate(
                reference,
                status,
                startedAt,
                completedAt);
    }

    private static void setTimestamp(
            final PreparedStatement statement,
            final int parameterIndex,
            final Instant value) throws SQLException {

        if (value == null) {
            statement.setObject(parameterIndex, null);
            return;
        }

        final OffsetDateTime timestamp =
                value.atOffset(ZoneOffset.UTC);

        statement.setObject(parameterIndex, timestamp);
    }

    private static Instant toInstant(
            final OffsetDateTime value) {

        return value == null
                ? null
                : value.toInstant();
    }
}