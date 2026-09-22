package io.github.avinashio.ozhuku.persistence.postgres;

import io.github.avinashio.ozhuku.domain.execution.SourceExecution;
import io.github.avinashio.ozhuku.domain.execution.SourceExecutionReference;
import io.github.avinashio.ozhuku.domain.execution.SourceExecutionStatus;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.foundation.validation.Validation;
import io.github.avinashio.ozhuku.persistence.SourceExecutionRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import javax.sql.DataSource;

/**
 * PostgreSQL implementation of the source execution persistence port.
 */
public final class PostgresSourceExecutionRepository
        implements SourceExecutionRepository {

    private static final String FIND_BY_ID_SQL = """
            SELECT
                execution_id,
                resource_id,
                status,
                started_at,
                completed_at
            FROM source_executions
            WHERE execution_id = ?
              AND resource_id = ?
            """;

    private static final String SAVE_SQL = """
            INSERT INTO source_executions (
                execution_id,
                resource_id,
                status,
                started_at,
                completed_at
            )
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (
                execution_id,
                resource_id
            )
            DO UPDATE SET
                status = EXCLUDED.status,
                started_at = EXCLUDED.started_at,
                completed_at = EXCLUDED.completed_at
            """;

    private final DataSource dataSource;

    /**
     * Creates the PostgreSQL source execution repository.
     *
     * @param dataSource PostgreSQL data source
     */
    public PostgresSourceExecutionRepository(
            final DataSource dataSource) {

        this.dataSource = Validation.requireNonNull(
                dataSource,
                "Data source must not be null");
    }

    @Override
    public Optional<SourceExecution> findById(
            final ExecutionId executionId,
            final ResourceId resourceId) {

        Validation.requireNonNull(
                executionId,
                "Execution ID must not be null");

        Validation.requireNonNull(
                resourceId,
                "Resource ID must not be null");

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(
                             FIND_BY_ID_SQL)) {

            statement.setString(
                    1,
                    executionId.value());

            statement.setString(
                    2,
                    resourceId.value());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(
                        mapSourceExecution(resultSet));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Failed to find source execution",
                    exception);
        }
    }

    @Override
    public void save(final SourceExecution sourceExecution) {

        Validation.requireNonNull(
                sourceExecution,
                "Source execution must not be null");

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(SAVE_SQL)) {

            statement.setString(
                    1,
                    sourceExecution.reference()
                            .executionId()
                            .value());

            statement.setString(
                    2,
                    sourceExecution.reference()
                            .resourceId()
                            .value());

            statement.setString(
                    3,
                    sourceExecution.status().name());

            setInstant(
                    statement,
                    4,
                    sourceExecution.startedAt());

            setInstant(
                    statement,
                    5,
                    sourceExecution.completedAt());

            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Failed to save source execution",
                    exception);
        }
    }

    private SourceExecution mapSourceExecution(
            final ResultSet resultSet) throws SQLException {

        final ExecutionId executionId =
                new ExecutionId(
                        resultSet.getString("execution_id"));

        final ResourceId resourceId =
                new ResourceId(
                        resultSet.getString("resource_id"));

        final SourceExecutionStatus status =
                SourceExecutionStatus.valueOf(
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

        return SourceExecution.rehydrate(
                new SourceExecutionReference(
                        executionId,
                        resourceId),
                status,
                startedAt,
                completedAt);
    }

    private static void setInstant(
            final PreparedStatement statement,
            final int parameterIndex,
            final Instant value) throws SQLException {

        if (value == null) {
            statement.setObject(
                    parameterIndex,
                    null);
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
}