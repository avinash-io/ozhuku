package io.github.avinashio.ozhuku.persistence.postgres;

import io.github.avinashio.ozhuku.domain.execution.Execution;
import io.github.avinashio.ozhuku.domain.execution.ExecutionStatus;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.execution.ExecutionReference;
import io.github.avinashio.ozhuku.domain.identity.PipelineId;
import io.github.avinashio.ozhuku.domain.identity.PipelineVersion;
import io.github.avinashio.ozhuku.foundation.validation.Validation;
import io.github.avinashio.ozhuku.persistence.ExecutionRepository;
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
 * PostgreSQL implementation of the execution persistence port.
 */
public final class PostgresExecutionRepository
        implements ExecutionRepository {

    private static final String FIND_BY_ID_SQL = """
            SELECT
                execution_id,
                pipeline_id,
                pipeline_version,
                status,
                started_at,
                completed_at
            FROM executions
            WHERE execution_id = ?
            """;

    private static final String SAVE_SQL = """
            INSERT INTO executions (
                execution_id,
                pipeline_id,
                pipeline_version,
                status,
                started_at,
                completed_at
            )
            VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT (execution_id)
            DO UPDATE SET
                pipeline_id = EXCLUDED.pipeline_id,
                pipeline_version = EXCLUDED.pipeline_version,
                status = EXCLUDED.status,
                started_at = EXCLUDED.started_at,
                completed_at = EXCLUDED.completed_at
            """;

    private final DataSource dataSource;

    /**
     * Creates the PostgreSQL execution repository.
     *
     * @param dataSource PostgreSQL data source
     */
    public PostgresExecutionRepository(
            final DataSource dataSource) {

        this.dataSource = Validation.requireNonNull(
                dataSource,
                "Data source must not be null");
    }

    @Override
    public Optional<Execution> findById(
            final ExecutionId executionId) {

        Validation.requireNonNull(
                executionId,
                "Execution ID must not be null");

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(
                             FIND_BY_ID_SQL)) {

            statement.setString(
                    1,
                    executionId.value());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(mapExecution(resultSet));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Failed to find execution",
                    exception);
        }
    }

    @Override
    public void save(final Execution execution) {

        Validation.requireNonNull(
                execution,
                "Execution must not be null");

        final ExecutionReference reference =
                execution.reference();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(SAVE_SQL)) {

            statement.setString(
                    1,
                    reference.executionId().value());

            statement.setString(
                    2,
                    reference.pipelineId().value());

            statement.setLong(
                    3,
                    reference.pipelineVersion().value());

            statement.setString(
                    4,
                    execution.status().name());

            setInstant(
                    statement,
                    5,
                    execution.startedAt());

            setInstant(
                    statement,
                    6,
                    execution.completedAt());

            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Failed to save execution",
                    exception);
        }
    }

    private Execution mapExecution(
            final ResultSet resultSet) throws SQLException {

        final ExecutionId executionId =
                new ExecutionId(
                        resultSet.getString("execution_id"));

        final PipelineId pipelineId =
                new PipelineId(
                        resultSet.getString("pipeline_id"));

        final PipelineVersion pipelineVersion =
                new PipelineVersion(
                        resultSet.getLong("pipeline_version"));

        final ExecutionReference reference =
                new ExecutionReference(
                        executionId,
                        pipelineId,
                        pipelineVersion);

        final ExecutionStatus status =
                ExecutionStatus.valueOf(
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

        return Execution.rehydrate(
                reference,
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

        final OffsetDateTime offsetDateTime =
                value.atOffset(ZoneOffset.UTC);

        statement.setObject(
                parameterIndex,
                offsetDateTime);
    }

    private static Instant toInstant(
            final OffsetDateTime value) {

        if (value == null) {
            return null;
        }

        return value.toInstant();
    }
}