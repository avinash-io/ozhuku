package io.github.avinashio.ozhuku.domain.lease;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.SourceIdentity;
import io.github.avinashio.ozhuku.foundation.exception.ValidationException;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class LeaseTest {

    private static final Instant CLAIMED_AT =
            Instant.parse("2026-10-22T12:00:00Z");
    private static final Instant EXPIRES_AT =
            Instant.parse("2026-10-22T12:00:30Z");

    @Test
    void shouldCreateClaimedLease() {
        final Lease lease = Lease.claimed(
                new LeaseId("lease-001"),
                new ExecutionId("execution-001"),
                new SourceIdentity("source-001"),
                "worker-A",
                1L,
                CLAIMED_AT,
                EXPIRES_AT);

        assertEquals("lease-001", lease.leaseId().value());
        assertEquals("execution-001", lease.executionId().value());
        assertEquals("source-001", lease.sourceIdentity().value());
        assertEquals("worker-A", lease.workerId());
        assertEquals(LeaseStatus.CLAIMED, lease.status());
        assertEquals(1L, lease.fencingToken());
        assertEquals(CLAIMED_AT, lease.claimedAt());
        assertEquals(EXPIRES_AT, lease.expiresAt());
        assertEquals(null, lease.revokedAt());
    }

    @Test
    void shouldCreateExpiredLease() {
        final Lease lease = Lease.expired(
                new LeaseId("lease-002"),
                new ExecutionId("execution-002"),
                new SourceIdentity("source-002"),
                "worker-B",
                1L,
                CLAIMED_AT,
                EXPIRES_AT);

        assertEquals(LeaseStatus.EXPIRED, lease.status());
        assertEquals(null, lease.revokedAt());
    }

    @Test
    void shouldCreateRevokedLease() {
        final Instant revokedAt =
                Instant.parse("2026-10-22T12:01:00Z");

        final Lease lease = Lease.revoked(
                new LeaseId("lease-003"),
                new ExecutionId("execution-003"),
                new SourceIdentity("source-003"),
                "worker-C",
                2L,
                CLAIMED_AT,
                EXPIRES_AT,
                revokedAt);

        assertEquals(LeaseStatus.REVOKED, lease.status());
        assertEquals(revokedAt, lease.revokedAt());
    }

    @Test
    void shouldRejectNullLeaseId() {
        assertThrows(
                ValidationException.class,
                () -> Lease.claimed(
                        null,
                        new ExecutionId("execution-001"),
                        new SourceIdentity("source-001"),
                        "worker-A",
                        1L,
                        CLAIMED_AT,
                        EXPIRES_AT));
    }

    @Test
    void shouldRejectNullExecutionId() {
        assertThrows(
                ValidationException.class,
                () -> Lease.claimed(
                        new LeaseId("lease-001"),
                        null,
                        new SourceIdentity("source-001"),
                        "worker-A",
                        1L,
                        CLAIMED_AT,
                        EXPIRES_AT));
    }

    @Test
    void shouldRejectNullSourceIdentity() {
        assertThrows(
                ValidationException.class,
                () -> Lease.claimed(
                        new LeaseId("lease-001"),
                        new ExecutionId("execution-001"),
                        null,
                        "worker-A",
                        1L,
                        CLAIMED_AT,
                        EXPIRES_AT));
    }

    @Test
    void shouldRejectBlankWorkerId() {
        assertThrows(
                ValidationException.class,
                () -> Lease.claimed(
                        new LeaseId("lease-001"),
                        new ExecutionId("execution-001"),
                        new SourceIdentity("source-001"),
                        "  ",
                        1L,
                        CLAIMED_AT,
                        EXPIRES_AT));
    }

    @Test
    void shouldRejectNullStatus() {
        assertThrows(
                ValidationException.class,
                () -> new Lease(
                        new LeaseId("lease-001"),
                        new ExecutionId("execution-001"),
                        new SourceIdentity("source-001"),
                        "worker-A",
                        null,
                        1L,
                        CLAIMED_AT,
                        EXPIRES_AT,
                        null));
    }

    @Test
    void shouldRejectNegativeFencingToken() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Lease.claimed(
                        new LeaseId("lease-001"),
                        new ExecutionId("execution-001"),
                        new SourceIdentity("source-001"),
                        "worker-A",
                        -1L,
                        CLAIMED_AT,
                        EXPIRES_AT));
    }

    @Test
    void shouldRejectNullClaimedAt() {
        assertThrows(
                ValidationException.class,
                () -> Lease.claimed(
                        new LeaseId("lease-001"),
                        new ExecutionId("execution-001"),
                        new SourceIdentity("source-001"),
                        "worker-A",
                        1L,
                        null,
                        EXPIRES_AT));
    }

    @Test
    void shouldRejectNullExpiresAt() {
        assertThrows(
                ValidationException.class,
                () -> Lease.claimed(
                        new LeaseId("lease-001"),
                        new ExecutionId("execution-001"),
                        new SourceIdentity("source-001"),
                        "worker-A",
                        1L,
                        CLAIMED_AT,
                        null));
    }

    @Test
    void shouldRejectExpiryBeforeClaimed() {
        final Instant expiresBefore =
                Instant.parse("2026-10-22T11:59:00Z");

        assertThrows(
                IllegalArgumentException.class,
                () -> Lease.claimed(
                        new LeaseId("lease-001"),
                        new ExecutionId("execution-001"),
                        new SourceIdentity("source-001"),
                        "worker-A",
                        1L,
                        CLAIMED_AT,
                        expiresBefore));
    }

    @Test
    void shouldRejectRevokedWithoutRevokedAt() {
        assertThrows(
                ValidationException.class,
                () -> Lease.revoked(
                        new LeaseId("lease-001"),
                        new ExecutionId("execution-001"),
                        new SourceIdentity("source-001"),
                        "worker-A",
                        1L,
                        CLAIMED_AT,
                        EXPIRES_AT,
                        null));
    }

    @Test
    void shouldCompareByLeaseId() {
        final Lease first = Lease.claimed(
                new LeaseId("lease-same"),
                new ExecutionId("execution-001"),
                new SourceIdentity("source-001"),
                "worker-A",
                1L,
                CLAIMED_AT,
                EXPIRES_AT);

        final Lease second = Lease.claimed(
                new LeaseId("lease-same"),
                new ExecutionId("execution-002"),
                new SourceIdentity("source-002"),
                "worker-B",
                2L,
                CLAIMED_AT,
                EXPIRES_AT);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldNotEqualDifferentLeaseId() {
        final Lease first = Lease.claimed(
                new LeaseId("lease-1"),
                new ExecutionId("execution-001"),
                new SourceIdentity("source-001"),
                "worker-A",
                1L,
                CLAIMED_AT,
                EXPIRES_AT);

        final Lease second = Lease.claimed(
                new LeaseId("lease-2"),
                new ExecutionId("execution-001"),
                new SourceIdentity("source-001"),
                "worker-A",
                1L,
                CLAIMED_AT,
                EXPIRES_AT);

        assertNotEquals(first, second);
    }

    @Test
    void shouldNotEqualNull() {
        final Lease lease = Lease.claimed(
                new LeaseId("lease-001"),
                new ExecutionId("execution-001"),
                new SourceIdentity("source-001"),
                "worker-A",
                1L,
                CLAIMED_AT,
                EXPIRES_AT);

        assertNotEquals(lease, null);
    }

    @Test
    void shouldNotEqualOtherType() {
        final Lease lease = Lease.claimed(
                new LeaseId("lease-001"),
                new ExecutionId("execution-001"),
                new SourceIdentity("source-001"),
                "worker-A",
                1L,
                CLAIMED_AT,
                EXPIRES_AT);

        assertNotEquals(lease, "some-string");
    }
}
