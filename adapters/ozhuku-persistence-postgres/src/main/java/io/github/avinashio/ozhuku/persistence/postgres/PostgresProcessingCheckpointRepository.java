package io.github.avinashio.ozhuku.persistence.postgres;

import io.github.avinashio.ozhuku.domain.checkpoint.ProcessingCheckpoint;
import io.github.avinashio.ozhuku.domain.execution.SourceExecutionReference;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.foundation.validation.Validation;
import io.github.avinashio.ozhuku.persistence.ProcessingCheckpointRepository;
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
 * PostgreSQL implementation of the processing checkpoint persistence port.
 */
public final class PostgresProcessingCheckpointRepository
        implements ProcessingCheckpointRepository {

    private static final String FIND_BY_SOURCE_EXECUTION_SQL = """
            SELECT
                execution_id,
                resource_id,
                record_sequence,
                checkpointed_at
            FROM processing_checkpoints
            WHERE execution_id = ?
              AND resource_id = ?
            """;

    private static final String SAVE_SQL = """
            INSERT INTO processing_checkpoints (
                execution_id,
                resource_id,
                record_sequence,
                checkpointed_at
            )
            VALUES (?, ?, ?, ?)
            ON CONFLICT (
                execution_id,
                resource_id
            )
            DO UPDATE SET
                record_sequence = EXCLUDED.record_sequence,
                checkpointed_at = EXCLUDED.checkpointed_at
            """;

    private final DataSource dataSource;

    /**
     * Creates the PostgreSQL processing checkpoint repository.
     *
     * @param dataSource PostgreSQL data source
     */
    public PostgresProcessingCheckpointRepository(
            final DataSource dataSource) {

        this.dataSource = Validation.requireNonNull(
                dataSource,
                "Data source must not be null");
    }

    @Override
    public Optional<ProcessingCheckpoint> findBySourceExecution(
            final SourceExecutionReference reference) {

        Validation.requireNonNull(
                reference,
                "Source execution reference must not be null");

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(
                             FIND_BY_SOURCE_EXECUTION_SQL)) {

            statement.setString(
                    1,
                    reference.executionId().value());

            statement.setString(
                    2,
                    reference.resourceId().value());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(
                        mapCheckpoint(resultSet));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Failed to find processing checkpoint",
                    exception);
        }
    }

    @Override
    public void save(final ProcessingCheckpoint checkpoint) {

        Validation.requireNonNull(
                checkpoint,
                "Processing checkpoint must not be null");

        final SourceExecutionReference reference =
                checkpoint.sourceExecutionReference();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(SAVE_SQL)) {

            statement.setString(
                    1,
                    reference.executionId().value());

            statement.setString(
                    2,
                    reference.resourceId().value());

            statement.setLong(
                    3,
                    checkpoint.recordSequence());

            statement.setObject(
                    4,
                    checkpoint.checkpointedAt()
                            .atOffset(ZoneOffset.UTC));

            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Failed to save processing checkpoint",
                    exception);
        }
    }

    private ProcessingCheckpoint mapCheckpoint(
            final ResultSet resultSet) throws SQLException {

        final ExecutionId executionId =
                new ExecutionId(
                        resultSet.getString("execution_id"));

        final ResourceId resourceId =
                new ResourceId(
                        resultSet.getString("resource_id"));

        final SourceExecutionReference reference =
                new SourceExecutionReference(
                        executionId,
                        resourceId);

        final long recordSequence =
                resultSet.getLong("record_sequence");

        final OffsetDateTime checkpointedAtValue =
                resultSet.getObject(
                        "checkpointed_at",
                        OffsetDateTime.class);

        final Instant checkpointedAt =
                checkpointedAtValue.toInstant();

        return new ProcessingCheckpoint(
                reference,
                recordSequence,
                checkpointedAt);
    }
}