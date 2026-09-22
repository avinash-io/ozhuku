package io.github.avinashio.ozhuku.persistence.postgres;

import io.github.avinashio.ozhuku.domain.execution.FlowExecution;
import io.github.avinashio.ozhuku.domain.execution.FlowExecutionStatus;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.FlowId;
import io.github.avinashio.ozhuku.foundation.validation.Validation;
import io.github.avinashio.ozhuku.persistence.FlowExecutionRepository;
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
 * PostgreSQL implementation of the flow execution persistence port.
 */
public final class PostgresFlowExecutionRepository
        implements FlowExecutionRepository {

    private static final String FIND_BY_ID_SQL = """
            SELECT
                execution_id,
                flow_id,
                status,
                started_at,
                completed_at
            FROM flow_executions
            WHERE execution_id = ?
              AND flow_id = ?
            """;

    private static final String SAVE_SQL = """
            INSERT INTO flow_executions (
                execution_id,
                flow_id,
                status,
                started_at,
                completed_at
            )
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (
                execution_id,
                flow_id
            )
            DO UPDATE SET
                status = EXCLUDED.status,
                started_at = EXCLUDED.started_at,
                completed_at = EXCLUDED.completed_at
            """;

    private final DataSource dataSource;

    /**
     * Creates the PostgreSQL flow execution repository.
     *
     * @param dataSource PostgreSQL data source
     */
    public PostgresFlowExecutionRepository(
            final DataSource dataSource) {

        this.dataSource = Validation.requireNonNull(
                dataSource,
                "Data source must not be null");
    }

    @Override
    public Optional<FlowExecution> findById(
            final ExecutionId executionId,
            final FlowId flowId) {

        Validation.requireNonNull(
                executionId,
                "Execution ID must not be null");

        Validation.requireNonNull(
                flowId,
                "Flow ID must not be null");

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(
                             FIND_BY_ID_SQL)) {

            statement.setString(
                    1,
                    executionId.value());

            statement.setString(
                    2,
                    flowId.value());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(
                        mapFlowExecution(resultSet));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Failed to find flow execution",
                    exception);
        }
    }

    @Override
    public void save(final FlowExecution flowExecution) {

        Validation.requireNonNull(
                flowExecution,
                "Flow execution must not be null");

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(SAVE_SQL)) {

            statement.setString(
                    1,
                    flowExecution.executionId().value());

            statement.setString(
                    2,
                    flowExecution.flowId().value());

            statement.setString(
                    3,
                    flowExecution.status().name());

            setInstant(
                    statement,
                    4,
                    flowExecution.startedAt());

            setInstant(
                    statement,
                    5,
                    flowExecution.completedAt());

            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Failed to save flow execution",
                    exception);
        }
    }

    private FlowExecution mapFlowExecution(
            final ResultSet resultSet) throws SQLException {

        final ExecutionId executionId =
                new ExecutionId(
                        resultSet.getString("execution_id"));

        final FlowId flowId =
                new FlowId(
                        resultSet.getString("flow_id"));

        final FlowExecutionStatus status =
                FlowExecutionStatus.valueOf(
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

        return FlowExecution.rehydrate(
                executionId,
                flowId,
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