package io.github.avinashio.ozhuku.persistence;

import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.SourceIdentity;
import io.github.avinashio.ozhuku.domain.lease.Lease;
import io.github.avinashio.ozhuku.domain.lease.LeaseId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Persistence port for durable lease management in distributed execution.
 *
 * <p>A lease grants exclusive ownership of an execution's processing to
 * a single worker. The repository provides atomic claim (with fencing
 * token), renewal, expiry, and revocation operations to support safe
 * distributed coordination without external coordination services.</p>
 */
public interface LeaseRepository {

    /**
     * Attempts to claim a lease atomically.
     *
     * <p>If the lease does not exist, it is created with fencing token 1
     * and the supplied worker becomes the owner. If the lease already
     * exists and is expired or revoked, the claim succeeds, incrementing
     * the fencing token. If the lease is currently claimed by another
     * worker, the claim fails and an empty optional is returned.</p>
     *
     * @param leaseId lease identifier
     * @param executionId execution protected by the lease
     * @param sourceIdentity source identity protected by the lease
     * @param workerId identifier of the claiming worker
     * @param leaseTimeoutSeconds duration in seconds from claim time
     * @param now current timestamp
     * @return the newly claimed lease, or empty if the claim failed
     */
    Optional<Lease> claim(
            LeaseId leaseId,
            ExecutionId executionId,
            SourceIdentity sourceIdentity,
            String workerId,
            int leaseTimeoutSeconds,
            Instant now);

    /**
     * Renews an existing lease for the supplied worker.
     *
     * <p>Renewal only succeeds if the worker currently holds the lease
     * with the supplied fencing token. On success the lease expiry is
     * extended.</p>
     *
     * @param leaseId lease identifier
     * @param workerId identifier of the renewing worker
     * @param expectedFencingToken the fencing token the worker currently holds
     * @param leaseTimeoutSeconds new duration in seconds from renewal time
     * @param now current timestamp
     * @return the renewed lease, or empty if renewal failed
     */
    Optional<Lease> renew(
            LeaseId leaseId,
            String workerId,
            long expectedFencingToken,
            int leaseTimeoutSeconds,
            Instant now);

    /**
     * Releases a lease held by the supplied worker.
     *
     * @param leaseId lease identifier
     * @param workerId identifier of the releasing worker
     * @param expectedFencingToken the fencing token the worker currently holds
     */
    void release(
            LeaseId leaseId,
            String workerId,
            long expectedFencingToken);

    /**
     * Finds a lease by its identifier.
     *
     * @param leaseId lease identifier
     * @return the lease, or empty if not found
     */
    Optional<Lease> findById(LeaseId leaseId);

    /**
     * Finds all leases associated with the supplied execution.
     *
     * @param executionId execution identifier
     * @return list of leases for the execution
     */
    List<Lease> findByExecutionId(ExecutionId executionId);

    /**
     * Returns all expired leases that have not been revoked.
     *
     * <p>A lease is expired when its expiry timestamp is in the past
     * and its status is {@code CLAIMED}.</p>
     *
     * @param now current timestamp
     * @return list of expired leases
     */
    List<Lease> findExpired(Instant now);

    /**
     * Persists a lease to the repository.
     *
     * <p>Used for rehydration and internal state transitions.
     * Uses atomic upsert semantics.</p>
     *
     * @param lease the lease to persist
     */
    void save(Lease lease);
}
