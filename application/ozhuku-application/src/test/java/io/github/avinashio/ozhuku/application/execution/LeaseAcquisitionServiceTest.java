package io.github.avinashio.ozhuku.application.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.SourceIdentity;
import io.github.avinashio.ozhuku.domain.lease.Lease;
import io.github.avinashio.ozhuku.domain.lease.LeaseId;
import io.github.avinashio.ozhuku.domain.lease.LeaseStatus;
import io.github.avinashio.ozhuku.persistence.LeaseRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LeaseAcquisitionServiceTest {

    private static final Instant FIXED_TIME =
            Instant.parse("2026-10-22T10:00:00Z");

    private static final Clock CLOCK =
            Clock.fixed(FIXED_TIME, ZoneOffset.UTC);

    private static final int LEASE_TIMEOUT_SECONDS = 30;

    private FakeLeaseRepository leaseRepository;

    private LeaseAcquisitionService service;

    @BeforeEach
    void setUp() {

        leaseRepository =
                new FakeLeaseRepository();

        service =
                new LeaseAcquisitionService(
                        leaseRepository,
                        CLOCK,
                        "worker-1",
                        LEASE_TIMEOUT_SECONDS);
    }

    @Test
    void shouldAcquireLeaseWhenNoExistingLease() {

        final Optional<Lease> lease =
                service.acquire(
                        new ExecutionId("execution-1"),
                        new SourceIdentity("source-1"));

        assertTrue(lease.isPresent());

        final Lease claimedLease = lease.orElseThrow();

        assertEquals(
                "lease-execution-1",
                claimedLease.leaseId().value());
        assertEquals("execution-1", claimedLease.executionId().value());
        assertEquals("source-1", claimedLease.sourceIdentity().value());
        assertEquals("worker-1", claimedLease.workerId());
        assertEquals(LeaseStatus.CLAIMED, claimedLease.status());
        assertEquals(1L, claimedLease.fencingToken());
        assertEquals(FIXED_TIME, claimedLease.claimedAt());
    }

    @Test
    void shouldFailAcquireWhenLeaseHeldByAnotherWorker() {

        leaseRepository.claim(
                new LeaseId("lease-execution-1"),
                new ExecutionId("execution-1"),
                new SourceIdentity("source-1"),
                "worker-2",
                LEASE_TIMEOUT_SECONDS,
                FIXED_TIME);

        final Optional<Lease> lease =
                service.acquire(
                        new ExecutionId("execution-1"),
                        new SourceIdentity("source-1"));

        assertTrue(lease.isEmpty());
    }

    @Test
    void shouldTakeOverExpiredLease() {

        final Instant past = FIXED_TIME.minusSeconds(60);

        leaseRepository.claim(
                new LeaseId("lease-execution-1"),
                new ExecutionId("execution-1"),
                new SourceIdentity("source-1"),
                "worker-2",
                LEASE_TIMEOUT_SECONDS,
                past);

        leaseRepository.expireLeases(FIXED_TIME);

        final Optional<Lease> lease =
                service.acquire(
                        new ExecutionId("execution-1"),
                        new SourceIdentity("source-1"));

        assertTrue(lease.isPresent());

        final Lease claimedLease = lease.orElseThrow();

        assertEquals("worker-1", claimedLease.workerId());
        assertEquals(LeaseStatus.CLAIMED, claimedLease.status());
        assertEquals(2L, claimedLease.fencingToken());
    }

    @Test
    void shouldReleaseLeaseSuccessfully() {

        final Optional<Lease> lease =
                service.acquire(
                        new ExecutionId("execution-1"),
                        new SourceIdentity("source-1"));

        service.release(
                lease.orElseThrow().leaseId(),
                lease.orElseThrow().fencingToken());

        assertTrue(leaseRepository.releasedLeases.stream()
                .anyMatch(
                        released -> released.leaseId()
                                .equals(new LeaseId("lease-execution-1"))));
    }

    @Test
    void shouldRenewLeaseWithCorrectFencingToken() {

        final Optional<Lease> lease =
                service.acquire(
                        new ExecutionId("execution-1"),
                        new SourceIdentity("source-1"));

        final Lease claimedLease = lease.orElseThrow();

        final Instant renewalTime = FIXED_TIME.plusSeconds(10);
        final Clock renewalClock =
                Clock.fixed(renewalTime, ZoneOffset.UTC);

        service =
                new LeaseAcquisitionService(
                        leaseRepository,
                        renewalClock,
                        "worker-1",
                        LEASE_TIMEOUT_SECONDS);

        final Optional<Lease> renewed =
                service.renew(
                        claimedLease.leaseId(),
                        claimedLease.fencingToken());

        assertTrue(renewed.isPresent());

        final Lease renewedLease = renewed.orElseThrow();

        assertEquals(
                claimedLease.fencingToken(),
                renewedLease.fencingToken());
        assertEquals(LeaseStatus.CLAIMED, renewedLease.status());
    }

    @Test
    void shouldFailRenewWithStaleFencingToken() {

        final Optional<Lease> lease =
                service.acquire(
                        new ExecutionId("execution-1"),
                        new SourceIdentity("source-1"));

        final LeaseId leaseId = lease.orElseThrow().leaseId();

        final Optional<Lease> renewed =
                service.renew(leaseId, 999L);

        assertTrue(renewed.isEmpty());
    }

    @Test
    void shouldDetectThatStillHolder() {

        final Optional<Lease> lease =
                service.acquire(
                        new ExecutionId("execution-1"),
                        new SourceIdentity("source-1"));

        final Lease claimedLease = lease.orElseThrow();

        assertTrue(
                service.isStillHolder(
                        claimedLease.leaseId(),
                        claimedLease.fencingToken()));
    }

    @Test
    void shouldDetectThatNoLongerHolderAfterTakeover() {

        final Optional<Lease> lease =
                service.acquire(
                        new ExecutionId("execution-1"),
                        new SourceIdentity("source-1"));

        final Lease claimedLease = lease.orElseThrow();

        final long staleToken = claimedLease.fencingToken();

        // First expire the lease (simulating time passing)
        leaseRepository.expireLeases(FIXED_TIME.plusSeconds(60));

        // Then another worker takes over the expired lease
        leaseRepository.claim(
                new LeaseId("lease-execution-1"),
                new ExecutionId("execution-1"),
                new SourceIdentity("source-1"),
                "worker-2",
                LEASE_TIMEOUT_SECONDS,
                FIXED_TIME.plusSeconds(60));

        assertFalse(
                service.isStillHolder(
                        claimedLease.leaseId(),
                        staleToken));
    }

    @Test
    void shouldRejectNullExecutionIdForLeaseId() {

        assertThrows(
                NullPointerException.class,
                () -> service.leaseIdFor(null));
    }

    @Test
    void shouldProduceDeterministicLeaseId() {

        final LeaseId leaseId =
                LeaseAcquisitionService.leaseIdFor(new ExecutionId("execution-42"));

        assertEquals("lease-execution-42", leaseId.value());
    }

    @Test
    void shouldRejectNullWorkerId() {

        assertThrows(
                NullPointerException.class,
                () -> new LeaseAcquisitionService(
                        leaseRepository,
                        CLOCK,
                        null,
                        LEASE_TIMEOUT_SECONDS));
    }

    @Test
    void shouldRejectInvalidLeaseTimeout() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new LeaseAcquisitionService(
                        leaseRepository,
                        CLOCK,
                        "worker-1",
                        0));
    }

    @Test
    void shouldRejectNullArgumentsOnAcquire() {

        assertThrows(
                NullPointerException.class,
                () -> service.acquire(
                        null,
                        new SourceIdentity("source-1")));

        assertThrows(
                NullPointerException.class,
                () -> service.acquire(
                        new ExecutionId("execution-1"),
                        null));
    }

    @Test
    void shouldRejectNullArgumentsOnRelease() {

        assertThrows(
                NullPointerException.class,
                () -> service.release(null, 1L));
    }

    @Test
    void shouldRejectNullArgumentsOnRenew() {

        assertThrows(
                NullPointerException.class,
                () -> service.renew(null, 1L));
    }

    @Test
    void shouldRejectNullArgumentsOnIsStillHolder() {

        assertThrows(
                NullPointerException.class,
                () -> service.isStillHolder(null, 1L));
    }

    @Test
    void shouldRejectNullArgumentsOnLeaseIdFor() {

        assertThrows(
                NullPointerException.class,
                () -> LeaseAcquisitionService.leaseIdFor(null));
    }

    @Test
    void shouldReleaseLeaseOnFailure() {

        final ExecutionId executionId =
                new ExecutionId("execution-fail");

        final FakeLeaseRepository countingRepo =
                new FakeLeaseRepository();

        final LeaseAcquisitionService countingService =
                new LeaseAcquisitionService(
                        countingRepo,
                        CLOCK,
                        "worker-1",
                        LEASE_TIMEOUT_SECONDS);

        final SourceIdentity sourceIdentity =
                new SourceIdentity("source-1");

        final Optional<Lease> lease =
                countingService.acquire(executionId, sourceIdentity);

        assertTrue(lease.isPresent());
    }

    private static final class FakeLeaseRepository
            implements LeaseRepository {

        private final java.util.Map<LeaseId, Lease> leases =
                new java.util.HashMap<>();

        private final java.util.List<ReleasedLease> releasedLeases =
                new java.util.ArrayList<>();

        @Override
        public Optional<Lease> claim(
                final LeaseId leaseId,
                final ExecutionId executionId,
                final SourceIdentity sourceIdentity,
                final String workerId,
                final int leaseTimeoutSeconds,
                final Instant now) {

            final Lease existing = leases.get(leaseId);

            if (existing != null) {
                if (existing.status() == LeaseStatus.CLAIMED) {
                    final Instant existingExpiry =
                            existing.expiresAt();

                    if (!existingExpiry.isBefore(now)) {
                        return Optional.empty();
                    }
                }

                if (existing.status() != LeaseStatus.EXPIRED
                        && existing.status() != LeaseStatus.REVOKED) {
                    return Optional.empty();
                }
            }

            final long nextFencingToken =
                    existing == null ? 1L : existing.fencingToken() + 1;

            final Instant expiresAt =
                    now.plusSeconds(leaseTimeoutSeconds);

            final Lease lease =
                    Lease.claimed(
                            leaseId,
                            executionId,
                            sourceIdentity,
                            workerId,
                            nextFencingToken,
                            now,
                            expiresAt);

            leases.put(leaseId, lease);

            return Optional.of(lease);
        }

        @Override
        public Optional<Lease> renew(
                final LeaseId leaseId,
                final String workerId,
                final long expectedFencingToken,
                final int leaseTimeoutSeconds,
                final Instant now) {

            final Lease existing = leases.get(leaseId);

            if (existing == null
                    || existing.status() != LeaseStatus.CLAIMED
                    || !existing.workerId().equals(workerId)
                    || existing.fencingToken() != expectedFencingToken) {

                return Optional.empty();
            }

            final Instant expiresAt =
                    now.plusSeconds(leaseTimeoutSeconds);

            final Lease renewed =
                    Lease.claimed(
                            leaseId,
                            existing.executionId(),
                            existing.sourceIdentity(),
                            workerId,
                            existing.fencingToken(),
                            existing.claimedAt(),
                            expiresAt);

            leases.put(leaseId, renewed);

            return Optional.of(renewed);
        }

        @Override
        public void release(
                final LeaseId leaseId,
                final String workerId,
                final long expectedFencingToken) {

            final Lease existing = leases.get(leaseId);

            if (existing != null
                    && existing.workerId().equals(workerId)
                    && existing.fencingToken() == expectedFencingToken) {

                releasedLeases.add(
                        new ReleasedLease(
                                leaseId,
                                workerId,
                                expectedFencingToken));
                leases.remove(leaseId);
            }
        }

        @Override
        public Optional<Lease> findById(final LeaseId leaseId) {
            return Optional.ofNullable(leases.get(leaseId));
        }

        @Override
        public java.util.List<Lease> findByExecutionId(
                final ExecutionId executionId) {

            return leases.values().stream()
                    .filter(lease ->
                            lease.executionId().equals(executionId))
                    .toList();
        }

        @Override
        public java.util.List<Lease> findExpired(final Instant now) {

            return leases.values().stream()
                    .filter(lease ->
                            lease.status() == LeaseStatus.CLAIMED
                            && lease.expiresAt().isBefore(now))
                    .toList();
        }

        @Override
        public void save(final Lease lease) {
            leases.put(lease.leaseId(), lease);
        }

        void expireLeases(final Instant now) {

            for (LeaseId id : leases.keySet()) {

                final Lease lease = leases.get(id);

                if (lease.status() == LeaseStatus.CLAIMED
                        && lease.expiresAt().isBefore(now)) {

                    final Lease expired =
                            Lease.expired(
                                    lease.leaseId(),
                                    lease.executionId(),
                                    lease.sourceIdentity(),
                                    lease.workerId(),
                                    lease.fencingToken(),
                                    lease.claimedAt(),
                                    lease.expiresAt());

                    leases.put(id, expired);
                }
            }
        }

        record ReleasedLease(
                LeaseId leaseId,
                String workerId,
                long fencingToken) {
        }
    }
}