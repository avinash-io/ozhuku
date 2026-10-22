package io.github.avinashio.ozhuku.domain.lease;

import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.SourceIdentity;
import io.github.avinashio.ozhuku.foundation.validation.Validation;
import java.time.Instant;
import java.util.Objects;

/**
 * Represents a durable lease that grants a worker exclusive ownership of
 * an execution's processing.
 *
 * <p>A lease is the distributed-execution mechanism that prevents multiple
 * workers from processing the same execution simultaneously. When a lease
 * is claimed, the worker is the sole owner until one of three outcomes:
 * the worker releases it, the lease expires (no renewal), or the lease
 * is revoked via fencing when another worker takes ownership.</p>
 *
 * <p>The fencing token ({@link #fencingToken()}) is a monotonically increasing
 * counter stored in the lease row. Each successful claim or takeover
 * increments it. A worker holding a stale fencing token must cease all
 * commit operations — this is the <em>fencing</em> guarantee.</p>
 */
public final class Lease {

    private final LeaseId leaseId;
    private final ExecutionId executionId;
    private final SourceIdentity sourceIdentity;
    private final String workerId;
    private final LeaseStatus status;
    private final long fencingToken;
    private final Instant claimedAt;
    private final Instant expiresAt;
    private final Instant revokedAt;

    /**
     * Creates a lease.
     *
     * @param leaseId unique lease identifier
     * @param executionId execution protected by this lease
     * @param sourceIdentity source identity protected by this lease
     * @param workerId identifier of the worker holding the lease
     * @param status current lease status
     * @param fencingToken monotonically increasing fencing counter
     * @param claimedAt timestamp when the lease was last claimed
     * @param expiresAt timestamp when the lease expires
     * @param revokedAt timestamp when the lease was revoked, or null
     */
    public Lease(
            final LeaseId leaseId,
            final ExecutionId executionId,
            final SourceIdentity sourceIdentity,
            final String workerId,
            final LeaseStatus status,
            final long fencingToken,
            final Instant claimedAt,
            final Instant expiresAt,
            final Instant revokedAt) {

        this.leaseId = Validation.requireNonNull(
                leaseId,
                "Lease ID must not be null");
        this.executionId = Validation.requireNonNull(
                executionId,
                "Execution ID must not be null");
        this.sourceIdentity = Validation.requireNonNull(
                sourceIdentity,
                "Source identity must not be null");
        this.workerId = Validation.requireNonBlank(
                workerId,
                "Worker ID must not be blank");
        this.status = Validation.requireNonNull(
                status,
                "Lease status must not be null");

        if (fencingToken < 0) {
            throw new IllegalArgumentException(
                    "Fencing token must not be negative");
        }

        this.fencingToken = fencingToken;
        this.claimedAt = Validation.requireNonNull(
                claimedAt,
                "Claimed timestamp must not be null");
        this.expiresAt = Validation.requireNonNull(
                expiresAt,
                "Expiry timestamp must not be null");

        if (expiresAt.isBefore(claimedAt)) {
            throw new IllegalArgumentException(
                    "Expiry timestamp must not be before claimed timestamp");
        }

        this.revokedAt = revokedAt;
    }

    /**
     * Creates a newly claimed lease.
     *
     * @param leaseId unique lease identifier
     * @param executionId execution protected by this lease
     * @param sourceIdentity source identity protected by this lease
     * @param workerId identifier of the claiming worker
     * @param fencingToken fencing token assigned to this claim
     * @param claimedAt timestamp when the claim occurred
     * @param expiresAt timestamp when the lease will expire
     * @return a claimed lease
     */
    public static Lease claimed(
            final LeaseId leaseId,
            final ExecutionId executionId,
            final SourceIdentity sourceIdentity,
            final String workerId,
            final long fencingToken,
            final Instant claimedAt,
            final Instant expiresAt) {

        return new Lease(
                leaseId,
                executionId,
                sourceIdentity,
                workerId,
                LeaseStatus.CLAIMED,
                fencingToken,
                claimedAt,
                expiresAt,
                null);
    }

    /**
     * Creates an expired lease.
     *
     * @param leaseId unique lease identifier
     * @param executionId execution protected by this lease
     * @param sourceIdentity source identity protected by this lease
     * @param workerId identifier of the last worker holding the lease
     * @param fencingToken fencing token of the last claim
     * @param claimedAt timestamp when the lease was last claimed
     * @param expiresAt timestamp when the lease expired
     * @return an expired lease
     */
    public static Lease expired(
            final LeaseId leaseId,
            final ExecutionId executionId,
            final SourceIdentity sourceIdentity,
            final String workerId,
            final long fencingToken,
            final Instant claimedAt,
            final Instant expiresAt) {

        return new Lease(
                leaseId,
                executionId,
                sourceIdentity,
                workerId,
                LeaseStatus.EXPIRED,
                fencingToken,
                claimedAt,
                expiresAt,
                null);
    }

    /**
     * Creates a revoked lease (taken over by another worker via fencing).
     *
     * @param leaseId unique lease identifier
     * @param executionId execution protected by this lease
     * @param sourceIdentity source identity protected by this lease
     * @param workerId identifier of the original worker
     * @param fencingToken fencing token of the original claim
     * @param claimedAt timestamp when the lease was originally claimed
     * @param expiresAt timestamp when the lease would have expired
     * @param revokedAt timestamp when the lease was revoked
     * @return a revoked lease
     */
    public static Lease revoked(
            final LeaseId leaseId,
            final ExecutionId executionId,
            final SourceIdentity sourceIdentity,
            final String workerId,
            final long fencingToken,
            final Instant claimedAt,
            final Instant expiresAt,
            final Instant revokedAt) {

        Validation.requireNonNull(
                revokedAt,
                "Revoked timestamp must not be null");

        return new Lease(
                leaseId,
                executionId,
                sourceIdentity,
                workerId,
                LeaseStatus.REVOKED,
                fencingToken,
                claimedAt,
                expiresAt,
                revokedAt);
    }

    /**
     * Returns the lease identifier.
     *
     * @return lease identifier
     */
    public LeaseId leaseId() {
        return leaseId;
    }

    /**
     * Returns the execution identifier protected by this lease.
     *
     * @return execution identifier
     */
    public ExecutionId executionId() {
        return executionId;
    }

    /**
     * Returns the source identity protected by this lease.
     *
     * @return source identity
     */
    public SourceIdentity sourceIdentity() {
        return sourceIdentity;
    }

    /**
     * Returns the worker identifier that holds or last held this lease.
     *
     * @return worker identifier
     */
    public String workerId() {
        return workerId;
    }

    /**
     * Returns the current lease status.
     *
     * @return lease status
     */
    public LeaseStatus status() {
        return status;
    }

    /**
     * Returns the fencing token for this lease.
     *
     * <p>The fencing token is a monotonically increasing counter.
     * A worker holding a stale fencing token (one lower than the
     * current token) must stop committing work.</p>
     *
     * @return fencing token
     */
    public long fencingToken() {
        return fencingToken;
    }

    /**
     * Returns the timestamp when the lease was last claimed.
     *
     * @return claimed timestamp
     */
    public Instant claimedAt() {
        return claimedAt;
    }

    /**
     * Returns the timestamp when the lease expires.
     *
     * @return expiry timestamp
     */
    public Instant expiresAt() {
        return expiresAt;
    }

    /**
     * Returns the timestamp when the lease was revoked via fencing,
     * or null if the lease has not been revoked.
     *
     * @return revoked timestamp, or null
     */
    public Instant revokedAt() {
        return revokedAt;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof Lease)) {
            return false;
        }

        final Lease that = (Lease) other;
        return leaseId.equals(that.leaseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(leaseId);
    }

    @Override
    public String toString() {
        return "Lease{"
                + "leaseId=" + leaseId
                + ", executionId=" + executionId
                + ", sourceIdentity=" + sourceIdentity
                + ", workerId=" + workerId
                + ", status=" + status
                + ", fencingToken=" + fencingToken
                + ", claimedAt=" + claimedAt
                + ", expiresAt=" + expiresAt
                + ", revokedAt=" + revokedAt
                + '}';
    }
}
