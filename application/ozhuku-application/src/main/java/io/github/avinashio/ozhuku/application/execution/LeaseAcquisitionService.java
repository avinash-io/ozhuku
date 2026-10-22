package io.github.avinashio.ozhuku.application.execution;

import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.SourceIdentity;
import io.github.avinashio.ozhuku.domain.lease.Lease;
import io.github.avinashio.ozhuku.domain.lease.LeaseId;
import io.github.avinashio.ozhuku.domain.lease.LeaseStatus;
import io.github.avinashio.ozhuku.persistence.LeaseRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Application service for acquiring and releasing execution leases.
 *
 * <p>Provides distributed-execution safety by ensuring only one worker
 * processes a given execution at a time. Uses durable leases with
 * fencing tokens to guarantee that a worker holding a stale token
 * (superseded by a takeover) does not commit work after losing ownership.</p>
 *
 * <p>The worker ID identifies the owning worker in the lease row and
 * must be unique per processing node.</p>
 */
public final class LeaseAcquisitionService {

    private final LeaseRepository leaseRepository;
    private final Clock clock;
    private final String workerId;
    private final int leaseTimeoutSeconds;

    /**
     * Creates a lease acquisition service.
     *
     * @param leaseRepository lease persistence port
     * @param clock clock for timestamp generation
     * @param workerId identifier of the worker claiming leases
     * @param leaseTimeoutSeconds lease duration in seconds before a stale
     *        claim is considered abandoned
     */
    public LeaseAcquisitionService(
            final LeaseRepository leaseRepository,
            final Clock clock,
            final String workerId,
            final int leaseTimeoutSeconds) {

        this.leaseRepository = Objects.requireNonNull(
                leaseRepository,
                "leaseRepository must not be null");
        this.clock = Objects.requireNonNull(
                clock,
                "clock must not be null");
        this.workerId = Objects.requireNonNull(
                workerId,
                "workerId must not be null");

        if (leaseTimeoutSeconds < 1) {
            throw new IllegalArgumentException(
                    "Lease timeout must be at least 1 second");
        }

        this.leaseTimeoutSeconds = leaseTimeoutSeconds;
    }

    /**
     * Attempts to acquire a lease for the given execution.
     *
     * <p>The lease ID is derived deterministically from the execution ID
     * so that all workers attempting to process the same execution target
     * the same lease row. If the lease is currently claimed by another
     * worker, acquisition fails and an empty optional is returned,
     * signalling that processing must be skipped.</p>
     *
     * @param executionId execution to lease
     * @param sourceIdentity source identity protected by the lease
     * @return the acquired lease, or empty if the lease is held by another
     *         worker
     */
    public Optional<Lease> acquire(
            final ExecutionId executionId,
            final SourceIdentity sourceIdentity) {

        Objects.requireNonNull(
                executionId,
                "executionId must not be null");
        Objects.requireNonNull(
                sourceIdentity,
                "sourceIdentity must not be null");

        final Instant now = Instant.now(clock);
        final LeaseId leaseId = leaseIdFor(executionId);

        return leaseRepository.claim(
                leaseId,
                executionId,
                sourceIdentity,
                workerId,
                leaseTimeoutSeconds,
                now);
    }

    /**
     * Releases the given lease on successful completion.
     *
     * @param leaseId execution lease identifier
     * @param fencingToken the fencing token the worker currently holds
     */
    public void release(
            final LeaseId leaseId,
            final long fencingToken) {

        Objects.requireNonNull(
                leaseId,
                "leaseId must not be null");

        leaseRepository.release(
                leaseId,
                workerId,
                fencingToken);
    }

    /**
     * Re-acquires (renews) the lease to extend its expiry, enabling
     * long-running executions to hold the lease.
     *
     * @param leaseId execution lease identifier
     * @param currentFencingToken the fencing token the worker currently holds
     * @return the renewed lease, and empty if the worker no longer holds the
     *         lease
     */
    public Optional<Lease> renew(
            final LeaseId leaseId,
            final long currentFencingToken) {

        Objects.requireNonNull(
                leaseId,
                "leaseId must not be null");

        final Instant now = Instant.now(clock);

        return leaseRepository.renew(
                leaseId,
                workerId,
                currentFencingToken,
                leaseTimeoutSeconds,
                now);
    }

    /**
     * Derives a deterministic lease ID from an execution ID.
     *
     * @param executionId execution identifier
     * @return lease identifier
     */
    public static LeaseId leaseIdFor(final ExecutionId executionId) {

        Objects.requireNonNull(
                executionId,
                "executionId must not be null");

        return new LeaseId("lease-" + executionId.value());
    }

    /**
     * Checks whether this worker is still the legitimate lease holder.
     *
     * <p>If another worker has taken over the lease (fencing token
     * incremented), the current holder returns {@code false} and
     * must cease all commit operations immediately.</p>
     *
     * @param leaseId execution lease identifier
     * @param currentFencingToken the fencing token this worker expects
     * @return {@code true} if this worker still holds the lease with the
     *         expected fencing token
     */
    public boolean isStillHolder(
            final LeaseId leaseId,
            final long currentFencingToken) {

        Objects.requireNonNull(
                leaseId,
                "leaseId must not be null");

        return leaseRepository.findById(leaseId)
                .map(lease ->
                        lease.status() == LeaseStatus.CLAIMED
                        && lease.workerId().equals(workerId)
                        && lease.fencingToken() == currentFencingToken)
                .orElse(false);
    }
}