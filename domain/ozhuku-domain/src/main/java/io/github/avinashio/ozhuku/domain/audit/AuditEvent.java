package io.github.avinashio.ozhuku.domain.audit;

import io.github.avinashio.ozhuku.foundation.validation.Validation;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a significant security, administrative, or exceptional
 * event recorded for compliance and traceability purposes.
 *
 * <p>Audit is separate from both technical logging and the execution
 * journal. Logs carry high-volume technical diagnostics; the execution
 * journal carries execution/recovery history; audit carries
 * security/admin/compliance events such as configuration changes,
 * authorization failures, lease takeovers, and manual reprocessing.</p>
 */
public final class AuditEvent {

    private final AuditEventId eventId;
    private final Instant timestamp;
    private final String organizationId;
    private final String actor;
    private final AuditEventType eventType;
    private final String resourceType;
    private final String resourceId;
    private final String pipelineId;
    private final String executionId;
    private final String sourceIdentity;
    private final String previousState;
    private final String newState;
    private final String reason;
    private final String outcome;
    private final String correlationId;
    private final Map<String, String> metadata;

    /**
     * Creates an audit event.
     *
     * @param eventId        unique identifier for this event
     * @param timestamp      when the event occurred
     * @param organizationId owning organization identifier
     * @param actor          who or what triggered the event
     * @param eventType      category of the event
     * @param resourceType   type of the affected resource
     * @param resourceId     identifier of the affected resource
     * @param pipelineId     pipeline associated with the event, or null
     * @param executionId    execution associated with the event, or null
     * @param sourceIdentity source identity, or null
     * @param previousState  state before the event, or null
     * @param newState       state after the event, or null
     * @param reason         human-readable reason for the event
     * @param outcome        outcome of the event (e.g. "success", "failure")
     * @param correlationId  identifier correlating related events
     * @param metadata       additional context, or null
     */
    public AuditEvent(
            final AuditEventId eventId,
            final Instant timestamp,
            final String organizationId,
            final String actor,
            final AuditEventType eventType,
            final String resourceType,
            final String resourceId,
            final String pipelineId,
            final String executionId,
            final String sourceIdentity,
            final String previousState,
            final String newState,
            final String reason,
            final String outcome,
            final String correlationId,
            final Map<String, String> metadata) {

        this.eventId = Validation.requireNonNull(
                eventId,
                "Event ID must not be null");
        this.timestamp = Validation.requireNonNull(
                timestamp,
                "Timestamp must not be null");
        this.organizationId = Validation.requireNonBlank(
                organizationId,
                "Organization ID must not be blank");
        this.actor = Validation.requireNonNull(
                actor,
                "Actor must not be null");
        this.eventType = Validation.requireNonNull(
                eventType,
                "Event type must not be null");
        this.resourceType = Validation.requireNonNull(
                resourceType,
                "Resource type must not be null");
        this.resourceId = Validation.requireNonBlank(
                resourceId,
                "Resource ID must not be blank");
        this.pipelineId = pipelineId;
        this.executionId = executionId;
        this.sourceIdentity = sourceIdentity;
        this.previousState = previousState;
        this.newState = newState;
        this.reason = reason;
        this.outcome = Validation.requireNonNull(
                outcome,
                "Outcome must not be null");
        this.correlationId = Validation.requireNonBlank(
                correlationId,
                "Correlation ID must not be blank");
        this.metadata = metadata != null
                ? Map.copyOf(metadata)
                : Map.of();
    }

    /**
     * Returns the unique event identifier.
     *
     * @return event identifier
     */
    public AuditEventId eventId() {
        return eventId;
    }

    /**
     * Returns the event timestamp.
     *
     * @return event timestamp
     */
    public Instant timestamp() {
        return timestamp;
    }

    /**
     * Returns the organization identifier.
     *
     * @return organization identifier
     */
    public String organizationId() {
        return organizationId;
    }

    /**
     * Returns the actor that triggered the event.
     *
     * @return actor
     */
    public String actor() {
        return actor;
    }

    /**
     * Returns the event type.
     *
     * @return event type
     */
    public AuditEventType eventType() {
        return eventType;
    }

    /**
     * Returns the resource type.
     *
     * @return resource type
     */
    public String resourceType() {
        return resourceType;
    }

    /**
     * Returns the resource identifier.
     *
     * @return resource identifier
     */
    public String resourceId() {
        return resourceId;
    }

    /**
     * Returns the pipeline identifier, if applicable.
     *
     * @return pipeline identifier, or null
     */
    public String pipelineId() {
        return pipelineId;
    }

    /**
     * Returns the execution identifier, if applicable.
     *
     * @return execution identifier, or null
     */
    public String executionId() {
        return executionId;
    }

    /**
     * Returns the source identity, if applicable.
     *
     * @return source identity, or null
     */
    public String sourceIdentity() {
        return sourceIdentity;
    }

    /**
     * Returns the previous state before the event, if applicable.
     *
     * @return previous state, or null
     */
    public String previousState() {
        return previousState;
    }

    /**
     * Returns the new state after the event, if applicable.
     *
     * @return new state, or null
     */
    public String newState() {
        return newState;
    }

    /**
     * Returns the reason for the event.
     *
     * @return reason, or null
     */
    public String reason() {
        return reason;
    }

    /**
     * Returns the outcome of the event.
     *
     * @return outcome
     */
    public String outcome() {
        return outcome;
    }

    /**
     * Returns the correlation identifier.
     *
     * @return correlation identifier
     */
    public String correlationId() {
        return correlationId;
    }

    /**
     * Returns additional context metadata.
     *
     * @return metadata map (never null, may be empty)
     */
    public Map<String, String> metadata() {
        return metadata;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof AuditEvent)) {
            return false;
        }

        final AuditEvent that = (AuditEvent) other;
        return eventId.equals(that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    @Override
    public String toString() {
        return "AuditEvent{"
                + "eventId=" + eventId
                + ", timestamp=" + timestamp
                + ", organizationId=" + organizationId
                + ", actor=" + actor
                + ", eventType=" + eventType
                + ", resourceType=" + resourceType
                + ", resourceId=" + resourceId
                + ", pipelineId=" + pipelineId
                + ", executionId=" + executionId
                + ", sourceIdentity=" + sourceIdentity
                + ", previousState=" + previousState
                + ", newState=" + newState
                + ", reason=" + reason
                + ", outcome=" + outcome
                + ", correlationId=" + correlationId
                + ", metadata=" + metadata
                + '}';
    }
}
