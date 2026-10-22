package io.github.avinashio.ozhuku.persistence.postgres;

import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.SourceIdentity;
import io.github.avinashio.ozhuku.domain.lease.Lease;
import io.github.avinashio.ozhuku.domain.lease.LeaseId;
import io.github.avinashio.ozhuku.domain.lease.LeaseStatus;
import io.github.avinashio.ozhuku.foundation.validation.Validation;
import io.github.avinashio.ozhuku.persistence.LeaseRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

/**
 * PostgreSQL implementation of the lease persistence port.
 *
 * <p>Implements atomic claim, renew, and release operations using
 * optimistic locking via the fencing token column. The claim operation
 * uses a conditional INSERT or UPDATE that only succeeds when the
 * existing lease (if any) is not currently claimed by another worker.</p>
 */
public final class PostgresLeaseRepository
        implements LeaseRepository {

    private static final String CLAIM_AND_RETURN_SQL = """
            WITH upsert AS (
                INSERT INTO leases (
                    lease_id,
                    execution_id,
                    source_identity,
                    worker_id,
                    status,
                    fencing_token,
                    claimed_at,
                    expires_at,
                    revoked_at
                )
                VALUES (?, ?, ?, ?, 'CLAIMED', 1, ?, ?, NULL)
                ON CONFLICT (lease_id)
                DO UPDATE SET
                    worker_id = EXCLUDED.worker_id,
                    status = EXCLUDED.status,
                    fencing_token = leases.fencing_token + 1,
                    claimed_at = EXCLUDED.claimed_at,
                    expires_at = EXCLUDED.expires_at,
                    revoked_at = NULL
                WHERE
                    leases.status IN ('EXPIRED', 'REVOKED')
                RETURNING *
            )
            SELECT * FROM upsert
            """;

    private static final String RENEW_SQL = """
            UPDATE leases
            SET
                expires_at = ?
            WHERE lease_id = ?
              AND worker_id = ?
              AND fencing_token = ?
              AND status = 'CLAIMED'
            RETURNING *
            """;

    private static final String RELEASE_SQL = """
            DELETE FROM leases
            WHERE lease_id = ?
              AND worker_id = ?
              AND fencing_token = ?
            """;

    private static final String FIND_BY_ID_SQL = """
            SELECT
                lease_id,
                execution_id,
                source_identity,
                worker_id,
                status,
                fencing_token,
                claimed_at,
                expires_at,
                revoked_at
            FROM leases
            WHERE lease_id = ?
            """;

    private static final String FIND_BY_EXECUTION_ID_SQL = """
            SELECT
                lease_id,
                execution_id,
                source_identity,
                worker_id,
                status,
                fencing_token,
                claimed_at,
                expires_at,
                revoked_at
            FROM leases
            WHERE execution_id = ?
            ORDER BY fencing_token DESC
            """;

    private static final String FIND_EXPIRED_SQL = """
            SELECT
                lease_id,
                execution_id,
                source_identity,
                worker_id,
                status,
                fencing_token,
                claimed_at,
                expires_at,
                revoked_at
            FROM leases
            WHERE status = 'CLAIMED'
              AND expires_at <= ?
            """;

    private static final String SAVE_SQL = """
            INSERT INTO leases (
                lease_id,
                execution_id,
                source_identity,
                worker_id,
                status,
                fencing_token,
                claimed_at,
                expires_at,
                revoked_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (lease_id)
            DO UPDATE SET
                execution_id = EXCLUDED.execution_id,
                source_identity = EXCLUDED.source_identity,
                worker_id = EXCLUDED.worker_id,
                status = EXCLUDED.status,
                fencing_token = EXCLUDED.fencing_token,
                claimed_at = EXCLUDED.claimed_at,
                expires_at = EXCLUDED.expires_at,
                revoked_at = EXCLUDED.revoked_at
            """;

    private final DataSource dataSource;

    /**
     * Creates the PostgreSQL lease repository.
     *
     * @param dataSource PostgreSQL data source
     */
    public PostgresLeaseRepository(
            final DataSource dataSource) {

        this.dataSource = Validation.requireNonNull(
                dataSource,
                "Data source must not be null");
    }

    @Override
    public Optional<Lease> claim(
            final LeaseId leaseId,
            final ExecutionId executionId,
            final SourceIdentity sourceIdentity,
            final String workerId,
            final int leaseTimeoutSeconds,
            final Instant now) {

        Validation.requireNonNull(
                leaseId,
                "Lease ID must not be null");
        Validation.requireNonNull(
                executionId,
                "Execution ID must not be null");
        Validation.requireNonNull(
                sourceIdentity,
                "Source identity must not be null");
        Validation.requireNonBlank(
                workerId,
                "Worker ID must not be blank");

        if (leaseTimeoutSeconds < 1) {
            throw new IllegalArgumentException(
                    "Lease timeout must be at least 1 second");
        }

        final Instant expiresAt = now.plusSeconds(leaseTimeoutSeconds);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(
                             CLAIM_AND_RETURN_SQL)) {

            statement.setString(1, leaseId.value());
            statement.setString(2, executionId.value());
            statement.setString(3, sourceIdentity.value());
            statement.setString(4, workerId);
            statement.setObject(5, now.atOffset(ZoneOffset.UTC));
            statement.setObject(6, expiresAt.atOffset(ZoneOffset.UTC));

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(mapLease(resultSet));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Failed to claim lease",
                    exception);
        }
    }

    @Override
    public Optional<Lease> renew(
            final LeaseId leaseId,
            final String workerId,
            final long expectedFencingToken,
            final int leaseTimeoutSeconds,
            final Instant now) {

        Validation.requireNonNull(
                leaseId,
                "Lease ID must not be null");
        Validation.requireNonBlank(
                workerId,
                "Worker ID must not be blank");

        if (leaseTimeoutSeconds < 1) {
            throw new IllegalArgumentException(
                    "Lease timeout must be at least 1 second");
        }

        final Instant newExpiry = now.plusSeconds(leaseTimeoutSeconds);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(RENEW_SQL)) {

            statement.setObject(1, newExpiry.atOffset(ZoneOffset.UTC));
            statement.setString(2, leaseId.value());
            statement.setString(3, workerId);
            statement.setLong(4, expectedFencingToken);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(mapLease(resultSet));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Failed to renew lease",
                    exception);
        }
    }

    @Override
    public void release(
            final LeaseId leaseId,
            final String workerId,
            final long expectedFencingToken) {

        Validation.requireNonNull(
                leaseId,
                "Lease ID must not be null");
        Validation.requireNonBlank(
                workerId,
                "Worker ID must not be blank");

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(RELEASE_SQL)) {

            statement.setString(1, leaseId.value());
            statement.setString(2, workerId);
            statement.setLong(3, expectedFencingToken);

            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Failed to release lease",
                    exception);
        }
    }

    @Override
    public Optional<Lease> findById(final LeaseId leaseId) {

        Validation.requireNonNull(
                leaseId,
                "Lease ID must not be null");

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(FIND_BY_ID_SQL)) {

            statement.setString(1, leaseId.value());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(mapLease(resultSet));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Failed to find lease by ID",
                    exception);
        }
    }

    @Override
    public List<Lease> findByExecutionId(
            final ExecutionId executionId) {

        Validation.requireNonNull(
                executionId,
                "Execution ID must not be null");

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(
                             FIND_BY_EXECUTION_ID_SQL)) {

            statement.setString(1, executionId.value());

            try (ResultSet resultSet = statement.executeQuery()) {
                final List<Lease> leases = new ArrayList<>();
                while (resultSet.next()) {
                    leases.add(mapLease(resultSet));
                }
                return leases;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Failed to find leases by execution ID",
                    exception);
        }
    }

    @Override
    public List<Lease> findExpired(final Instant now) {

        Validation.requireNonNull(
                now,
                "Current timestamp must not be null");

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(FIND_EXPIRED_SQL)) {

            statement.setObject(1, now.atOffset(ZoneOffset.UTC));

            try (ResultSet resultSet = statement.executeQuery()) {
                final List<Lease> leases = new ArrayList<>();
                while (resultSet.next()) {
                    leases.add(mapLease(resultSet));
                }
                return leases;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Failed to find expired leases",
                    exception);
        }
    }

    @Override
    public void save(final Lease lease) {

        Validation.requireNonNull(
                lease,
                "Lease must not be null");

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(SAVE_SQL)) {

            statement.setString(1, lease.leaseId().value());
            statement.setString(2, lease.executionId().value());
            statement.setString(3, lease.sourceIdentity().value());
            statement.setString(4, lease.workerId());
            statement.setString(5, lease.status().name());
            statement.setLong(6, lease.fencingToken());
            statement.setObject(7, lease.claimedAt().atOffset(ZoneOffset.UTC));
            statement.setObject(8, lease.expiresAt().atOffset(ZoneOffset.UTC));

            if (lease.revokedAt() != null) {
                statement.setObject(9, lease.revokedAt().atOffset(ZoneOffset.UTC));
            } else {
                statement.setObject(9, null);
            }

            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Failed to save lease",
                    exception);
        }
    }

    private Lease mapLease(final ResultSet resultSet) throws SQLException {

        final String leaseId = resultSet.getString("lease_id");
        final String executionId = resultSet.getString("execution_id");
        final String sourceIdentity = resultSet.getString("source_identity");
        final String workerId = resultSet.getString("worker_id");
        final LeaseStatus status =
                LeaseStatus.valueOf(resultSet.getString("status"));
        final long fencingToken =
                resultSet.getLong("fencing_token");
        final Instant claimedAt =
                toInstant(resultSet.getObject(
                        "claimed_at", OffsetDateTime.class));
        final Instant expiresAt =
                toInstant(resultSet.getObject(
                        "expires_at", OffsetDateTime.class));
        final Instant revokedAt =
                toInstant(resultSet.getObject(
                        "revoked_at", OffsetDateTime.class));

        return new Lease(
                new LeaseId(leaseId),
                new ExecutionId(executionId),
                new SourceIdentity(sourceIdentity),
                workerId,
                status,
                fencingToken,
                claimedAt,
                expiresAt,
                revokedAt);
    }

    private static Instant toInstant(final OffsetDateTime value) {

        if (value == null) {
            return null;
        }

        return value.toInstant();
    }
}
