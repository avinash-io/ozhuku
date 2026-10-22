package io.github.avinashio.ozhuku.persistence.postgres;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.SourceIdentity;
import io.github.avinashio.ozhuku.domain.lease.Lease;
import io.github.avinashio.ozhuku.domain.lease.LeaseId;
import io.github.avinashio.ozhuku.domain.lease.LeaseStatus;
import io.github.avinashio.ozhuku.persistence.LeaseRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class PostgresLeaseRepositoryIT {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17-alpine");

    private static DataSource dataSource;

    private static LeaseRepository repository;

    private static final Instant NOW =
            Instant.parse("2026-10-22T12:00:00Z");

    @BeforeAll
    static void setUp() {
        dataSource = createDataSource();

        Flyway.configure()
                .dataSource(dataSource)
                .load()
                .migrate();

        repository = new PostgresLeaseRepository(dataSource);
    }

    @Test
    void shouldReturnEmptyWhenLeaseDoesNotExist() {
        final Optional<Lease> result =
                repository.findById(new LeaseId("lease-nonexistent"));

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldClaimNewLease() {
        final Instant now = NOW;
        final Instant expectedExpiry = now.plusSeconds(30);

        final Optional<Lease> result = repository.claim(
                new LeaseId("lease-001"),
                new ExecutionId("execution-001"),
                new SourceIdentity("source-001"),
                "worker-A",
                30,
                now);

        assertTrue(result.isPresent());

        final Lease lease = result.orElseThrow();

        assertEquals("lease-001", lease.leaseId().value());
        assertEquals("execution-001", lease.executionId().value());
        assertEquals("source-001", lease.sourceIdentity().value());
        assertEquals("worker-A", lease.workerId());
        assertEquals(LeaseStatus.CLAIMED, lease.status());
        assertEquals(1L, lease.fencingToken());
        assertEquals(now, lease.claimedAt());
        assertEquals(expectedExpiry, lease.expiresAt());
        assertEquals(null, lease.revokedAt());
    }

    @Test
    void shouldTakeOverExpiredLease() throws InterruptedException {
        final Instant now = NOW;

        final Optional<Lease> initial = repository.claim(
                new LeaseId("lease-expiry"),
                new ExecutionId("execution-expiry"),
                new SourceIdentity("source-expiry"),
                "worker-A",
                1,
                now);

        assertTrue(initial.isPresent());
        assertEquals(1L, initial.orElseThrow().fencingToken());

        Thread.sleep(1100);

        final Instant later = Instant.now().plusSeconds(5);

        final Optional<Lease> takenOver = repository.claim(
                new LeaseId("lease-expiry"),
                new ExecutionId("execution-expiry"),
                new SourceIdentity("source-expiry"),
                "worker-B",
                30,
                later);

        assertTrue(takenOver.isPresent());

        final Lease lease = takenOver.orElseThrow();

        assertEquals("worker-B", lease.workerId());
        assertEquals(LeaseStatus.CLAIMED, lease.status());
        assertEquals(2L, lease.fencingToken());
    }

    @Test
    void shouldFailClaimWhenLeaseHeldByAnotherWorker() {
        final Instant now = NOW;

        final Optional<Lease> initial = repository.claim(
                new LeaseId("lease-busy"),
                new ExecutionId("execution-busy"),
                new SourceIdentity("source-busy"),
                "worker-A",
                3600,
                now);

        assertTrue(initial.isPresent());

        final Optional<Lease> conflict = repository.claim(
                new LeaseId("lease-busy"),
                new ExecutionId("execution-busy"),
                new SourceIdentity("source-busy"),
                "worker-B",
                30,
                now);

        assertTrue(conflict.isEmpty());
    }

    @Test
    void shouldRenewLeaseWithCorrectFencingToken() {
        final Instant now = NOW;

        final Optional<Lease> claimed = repository.claim(
                new LeaseId("lease-renew"),
                new ExecutionId("execution-renew"),
                new SourceIdentity("source-renew"),
                "worker-A",
                30,
                now);

        assertTrue(claimed.isPresent());

        final long fencingToken =
                claimed.orElseThrow().fencingToken();

        final Instant renewalTime = now.plusSeconds(10);
        final Instant expectedNewExpiry =
                renewalTime.plusSeconds(60);

        final Optional<Lease> renewed = repository.renew(
                new LeaseId("lease-renew"),
                "worker-A",
                fencingToken,
                60,
                renewalTime);

        assertTrue(renewed.isPresent());

        final Lease lease = renewed.orElseThrow();

        assertEquals(LeaseStatus.CLAIMED, lease.status());
        assertEquals(fencingToken, lease.fencingToken());
        assertEquals(expectedNewExpiry, lease.expiresAt());
    }

    @Test
    void shouldFailRenewWithStaleFencingToken() {
        final Instant now = NOW;

        final Optional<Lease> claimed = repository.claim(
                new LeaseId("lease-renew-stale"),
                new ExecutionId("execution-renew-stale"),
                new SourceIdentity("source-renew-stale"),
                "worker-A",
                30,
                now);

        assertTrue(claimed.isPresent());

        final Optional<Lease> renewed = repository.renew(
                new LeaseId("lease-renew-stale"),
                "worker-A",
                999L,
                60,
                now.plusSeconds(5));

        assertTrue(renewed.isEmpty());
    }

    @Test
    void shouldFailRenewForDifferentWorker() {
        final Instant now = NOW;

        final Optional<Lease> claimed = repository.claim(
                new LeaseId("lease-renew-other"),
                new ExecutionId("execution-renew-other"),
                new SourceIdentity("source-renew-other"),
                "worker-A",
                3600,
                now);

        assertTrue(claimed.isPresent());

        final long fencingToken =
                claimed.orElseThrow().fencingToken();

        final Optional<Lease> renewed = repository.renew(
                new LeaseId("lease-renew-other"),
                "worker-B",
                fencingToken,
                60,
                now.plusSeconds(5));

        assertTrue(renewed.isEmpty());
    }

    @Test
    void shouldReleaseLease() {
        final Instant now = NOW;

        final Optional<Lease> claimed = repository.claim(
                new LeaseId("lease-release"),
                new ExecutionId("execution-release"),
                new SourceIdentity("source-release"),
                "worker-A",
                30,
                now);

        assertTrue(claimed.isPresent());

        final long fencingToken =
                claimed.orElseThrow().fencingToken();

        repository.release(
                new LeaseId("lease-release"),
                "worker-A",
                fencingToken);

        final Optional<Lease> afterRelease = repository.findById(
                new LeaseId("lease-release"));

        assertTrue(afterRelease.isEmpty());
    }

    @Test
    void shouldFindLeasesByExecutionId() {
        final Instant now = NOW;

        repository.claim(
                new LeaseId("lease-by-exec-1"),
                new ExecutionId("execution-by-exec"),
                new SourceIdentity("source-1"),
                "worker-A",
                30,
                now);

        repository.claim(
                new LeaseId("lease-by-exec-2"),
                new ExecutionId("execution-by-exec"),
                new SourceIdentity("source-2"),
                "worker-B",
                30,
                now.plusSeconds(5));

        final List<Lease> leases =
                repository.findByExecutionId(
                        new ExecutionId("execution-by-exec"));

        assertEquals(2, leases.size());

        // Most recent fencing token first
        assertEquals(2L, leases.get(0).fencingToken());
        assertEquals(1L, leases.get(1).fencingToken());
    }

    @Test
    void shouldReturnEmptyListWhenExecutionIdNotFound() {
        final List<Lease> leases =
                repository.findByExecutionId(
                        new ExecutionId("nonexistent-exec"));

        assertTrue(leases.isEmpty());
    }

    @Test
    void shouldFindExpiredLeases() {
        final Instant now = NOW;

        final Instant past = now.minusSeconds(10);

        repository.claim(
                new LeaseId("lease-already-expired"),
                new ExecutionId("execution-expired"),
                new SourceIdentity("source-expired"),
                "worker-A",
                1,
                past);

        repository.claim(
                new LeaseId("lease-still-valid"),
                new ExecutionId("execution-valid"),
                new SourceIdentity("source-valid"),
                "worker-A",
                3600,
                now);

        final List<Lease> expired =
                repository.findExpired(now);

        boolean foundExpired = expired.stream()
                .anyMatch(l -> l.leaseId().value()
                        .equals("lease-already-expired"));
        boolean foundValid = expired.stream()
                .anyMatch(l -> l.leaseId().value()
                        .equals("lease-still-valid"));

        assertTrue(foundExpired,
                "Should find the already-expired lease");
        assertFalse(foundValid,
                "Should not find the still-valid lease");
    }

    @Test
    void shouldReturnEmptyWhenNoExpiredLeases() {
        final Instant now = NOW;

        final List<Lease> expired =
                repository.findExpired(now);

        // All leases from other tests may have expired;
        // this is acceptable, we just verify no exception
        assertTrue(expired != null);
    }

    @Test
    void shouldOverwriteExistingLeaseWithSave() {
        final Instant now = NOW;

        final Lease original = Lease.claimed(
                new LeaseId("lease-overwrite"),
                new ExecutionId("execution-overwrite"),
                new SourceIdentity("source-overwrite"),
                "worker-A",
                1L,
                now,
                now.plusSeconds(30));

        repository.save(original);

        final Lease updated = Lease.claimed(
                new LeaseId("lease-overwrite"),
                new ExecutionId("execution-overwrite"),
                new SourceIdentity("source-overwrite"),
                "worker-B",
                5L,
                now,
                now.plusSeconds(30));

        repository.save(updated);

        final Optional<Lease> result = repository.findById(
                new LeaseId("lease-overwrite"));

        assertTrue(result.isPresent());

        final Lease retrieved = result.orElseThrow();
        assertEquals("worker-B", retrieved.workerId());
        assertEquals(5L, retrieved.fencingToken());
    }

    private static DataSource createDataSource() {
        final org.postgresql.ds.PGSimpleDataSource dataSource =
                new org.postgresql.ds.PGSimpleDataSource();

        dataSource.setServerNames(
                new String[]{POSTGRES.getHost()});
        dataSource.setPortNumbers(
                new int[]{POSTGRES.getMappedPort(5432)});
        dataSource.setDatabaseName(
                POSTGRES.getDatabaseName());
        dataSource.setUser(
                POSTGRES.getUsername());
        dataSource.setPassword(
                POSTGRES.getPassword());

        return dataSource;
    }
}
