package io.github.avinashio.ozhuku.persistence.postgres;

import io.github.avinashio.ozhuku.domain.deduplication.ProcessingRecord;
import io.github.avinashio.ozhuku.domain.deduplication.ProcessingStatus;
import io.github.avinashio.ozhuku.domain.identity.PipelineId;
import io.github.avinashio.ozhuku.domain.identity.PipelineVersion;
import io.github.avinashio.ozhuku.domain.identity.ProcessingIdentity;
import io.github.avinashio.ozhuku.domain.identity.SourceFingerprint;
import io.github.avinashio.ozhuku.domain.identity.SourceIdentity;
import io.github.avinashio.ozhuku.foundation.validation.Validation;
import io.github.avinashio.ozhuku.persistence.ProcessingRecordRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.util.Optional;
import javax.sql.DataSource;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * PostgreSQL implementation of the processing record persistence port.
 */
public final class PostgresProcessingRecordRepository
        implements ProcessingRecordRepository {

    private static final String FIND_BY_IDENTITY_SQL = """
            SELECT
                source_identity,
                pipeline_id,
                pipeline_version,
                status,
                source_fingerprint,
                processed_at
            FROM processing_records
            WHERE source_identity = ?
              AND pipeline_id = ?
              AND pipeline_version = ?
            """;

    private static final String SAVE_SQL = """
            INSERT INTO processing_records (
                source_identity,
                pipeline_id,
                pipeline_version,
                status,
                source_fingerprint,
                processed_at
            )
            VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT (
                source_identity,
                pipeline_id,
                pipeline_version
            )
            DO UPDATE SET
                status = EXCLUDED.status,
                source_fingerprint = EXCLUDED.source_fingerprint,
                processed_at = EXCLUDED.processed_at
            """;

    private final DataSource dataSource;

    /**
     * Creates the PostgreSQL repository.
     *
     * @param dataSource PostgreSQL data source
     */
    public PostgresProcessingRecordRepository(
            final DataSource dataSource) {

        this.dataSource = Validation.requireNonNull(
                dataSource,
                "Data source must not be null");
    }

    @Override
    public Optional<ProcessingRecord> findByIdentity(
            final ProcessingIdentity identity) {

        Validation.requireNonNull(
                identity,
                "Processing identity must not be null");

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(
                             FIND_BY_IDENTITY_SQL)) {

            statement.setString(
                    1,
                    identity.sourceIdentity().value());

            statement.setString(
                    2,
                    identity.pipelineId().value());

            statement.setLong(
                    3,
                    identity.pipelineVersion().value());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(mapRecord(resultSet));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Failed to find processing record",
                    exception);
        }
    }

    @Override
    public void save(final ProcessingRecord record) {

        Validation.requireNonNull(
                record,
                "Processing record must not be null");

        final ProcessingIdentity identity =
                record.identity();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(SAVE_SQL)) {

            statement.setString(
                    1,
                    identity.sourceIdentity().value());

            statement.setString(
                    2,
                    identity.pipelineId().value());

            statement.setLong(
                    3,
                    identity.pipelineVersion().value());

            statement.setString(
                    4,
                    record.status().name());

            if (record.sourceFingerprint() == null) {
                statement.setNull(
                        5,
                        Types.VARCHAR);
            } else {
                statement.setString(
                        5,
                        record.sourceFingerprint().value());
            }

            if (record.processedAt() == null) {
                statement.setObject(6, null);
            } else {
                final OffsetDateTime processedAt =
                        record.processedAt().atOffset(ZoneOffset.UTC);

                statement.setObject(
                        6,
                        processedAt);
            }

            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Failed to save processing record",
                    exception);
        }
    }

    private ProcessingRecord mapRecord(
            final ResultSet resultSet) throws SQLException {

        final SourceIdentity sourceIdentity =
                new SourceIdentity(
                        resultSet.getString("source_identity"));

        final PipelineId pipelineId =
                new PipelineId(
                        resultSet.getString("pipeline_id"));

        final PipelineVersion pipelineVersion =
                new PipelineVersion(
                        resultSet.getLong("pipeline_version"));

        final ProcessingIdentity identity =
                new ProcessingIdentity(
                        sourceIdentity,
                        pipelineId,
                        pipelineVersion);

        final ProcessingStatus status =
                ProcessingStatus.valueOf(
                        resultSet.getString("status"));

        final String fingerprint =
                resultSet.getString("source_fingerprint");

        final OffsetDateTime processedAtValue =
                resultSet.getObject(
                        "processed_at",
                        OffsetDateTime.class);

        final Instant processedAt =
                processedAtValue == null
                        ? null
                        : processedAtValue.toInstant();

        if (status == ProcessingStatus.NOT_PROCESSED) {
            return ProcessingRecord.notProcessed(identity);
        }

        return ProcessingRecord.processed(
                identity,
                new SourceFingerprint(fingerprint),
                processedAt);
    }
}