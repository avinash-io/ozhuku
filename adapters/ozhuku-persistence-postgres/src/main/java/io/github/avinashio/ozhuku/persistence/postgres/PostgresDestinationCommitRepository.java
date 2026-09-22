package io.github.avinashio.ozhuku.persistence.postgres;

import io.github.avinashio.ozhuku.domain.execution.CommitStatus;
import io.github.avinashio.ozhuku.domain.execution.DestinationCommit;
import io.github.avinashio.ozhuku.domain.execution.DestinationCommitReference;
import io.github.avinashio.ozhuku.domain.execution.DestinationExecutionReference;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.persistence.DestinationCommitRepository;
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

public final class PostgresDestinationCommitRepository
        implements DestinationCommitRepository {

    private static final String FIND_BY_ID_SQL = """
            SELECT
                execution_id,
                resource_id,
                status,
                committed_at
            FROM destination_commits
            WHERE execution_id = ?
              AND resource_id = ?
            """;

    private static final String SAVE_SQL = """
            INSERT INTO destination_commits (
                execution_id,
                resource_id,
                status,
                committed_at
            )
            VALUES (?, ?, ?, ?)
            ON CONFLICT (execution_id, resource_id)
            DO UPDATE SET
                status = EXCLUDED.status,
                committed_at = EXCLUDED.committed_at
            """;

    private final DataSource dataSource;

    public PostgresDestinationCommitRepository(
            final DataSource dataSource) {

        this.dataSource = Objects.requireNonNull(
                dataSource,
                "dataSource must not be null");
    }

    @Override
    public Optional<DestinationCommit> findById(
            final DestinationCommitReference reference) {

        Objects.requireNonNull(
                reference,
                "reference must not be null");

        final DestinationExecutionReference
                destinationExecutionReference =
                reference.destinationExecutionReference();

        final ExecutionId executionId =
                destinationExecutionReference.executionId();

        final ResourceId resourceId =
                destinationExecutionReference.resourceId();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(FIND_BY_ID_SQL)) {

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
                        mapDestinationCommit(resultSet));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Failed to find destination commit",
                    exception);
        }
    }

    @Override
    public void save(
            final DestinationCommit destinationCommit) {

        Objects.requireNonNull(
                destinationCommit,
                "destinationCommit must not be null");

        final DestinationExecutionReference
                destinationExecutionReference =
                destinationCommit.reference()
                        .destinationExecutionReference();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(SAVE_SQL)) {

            statement.setString(
                    1,
                    destinationExecutionReference
                            .executionId()
                            .value());

            statement.setString(
                    2,
                    destinationExecutionReference
                            .resourceId()
                            .value());

            statement.setString(
                    3,
                    destinationCommit.status().name());

            setTimestamp(
                    statement,
                    4,
                    destinationCommit.committedAt());

            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Failed to save destination commit",
                    exception);
        }
    }

    private static DestinationCommit mapDestinationCommit(
            final ResultSet resultSet) throws SQLException {

        final ExecutionId executionId =
                new ExecutionId(
                        resultSet.getString("execution_id"));

        final ResourceId resourceId =
                new ResourceId(
                        resultSet.getString("resource_id"));

        final CommitStatus status =
                CommitStatus.valueOf(
                        resultSet.getString("status"));

        final OffsetDateTime committedAtValue =
                resultSet.getObject(
                        "committed_at",
                        OffsetDateTime.class);

        final Instant committedAt =
                committedAtValue == null
                        ? null
                        : committedAtValue.toInstant();

        final DestinationExecutionReference
                destinationExecutionReference =
                new DestinationExecutionReference(
                        executionId,
                        resourceId);

        final DestinationCommitReference reference =
                new DestinationCommitReference(
                        destinationExecutionReference);

        return switch (status) {
            case NOT_COMMITTED ->
                    DestinationCommit.notCommitted(reference);

            case COMMITTED ->
                    DestinationCommit.committed(
                            reference,
                            committedAt);

            case UNKNOWN ->
                    DestinationCommit.unknown(reference);
        };
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

        statement.setObject(
                parameterIndex,
                timestamp);
    }
}