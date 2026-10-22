package io.github.avinashio.ozhuku.persistence;

import io.github.avinashio.ozhuku.domain.audit.AuditEvent;
import io.github.avinashio.ozhuku.domain.audit.AuditEventId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Persistence port for audit event recording and retrieval.
 *
 * <p>Audit events are immutable records of significant
 * security, administrative, and exceptional events. They must
 * never contain passwords, tokens, private keys, or unnecessary
 * sensitive payloads.</p>
 */
public interface AuditEventRepository {

    /**
     * Persists an audit event.
     *
     * @param auditEvent the audit event to persist
     */
    void save(AuditEvent auditEvent);

    /**
     * Retrieves an audit event by its identifier.
     *
     * @param eventId event identifier
     * @return the audit event, or empty if not found
     */
    Optional<AuditEvent> findById(AuditEventId eventId);

    /**
     * Finds audit events within a time range, ordered by timestamp
     * descending (most recent first).
     *
     * @param from   start of the time range (inclusive)
     * @param to     end of the time range (inclusive)
     * @param limit  maximum number of results
     * @return list of audit events, ordered by timestamp descending
     */
    List<AuditEvent> findByTimestampBetween(
            Instant from,
            Instant to,
            int limit);

    /**
     * Finds audit events by actor, ordered by timestamp descending
     * (most recent first).
     *
     * @param actor    actor identifier
     * @param limit    maximum number of results
     * @return list of audit events for the given actor
     */
    List<AuditEvent> findByActor(
            String actor,
            int limit);
}
