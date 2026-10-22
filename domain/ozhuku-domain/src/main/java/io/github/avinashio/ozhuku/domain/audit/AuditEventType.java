package io.github.avinashio.ozhuku.domain.audit;

/**
 * Enumeration of audit event categories that Ozhuku must record for
 * compliance, security, and traceability purposes.
 */
public enum AuditEventType {

    /** A pipeline was (re)processed manually after a prior execution. */
    REPROCESSING,

    /** A duplicate resource was detected and skipped. */
    DUPLICATE_DETECTION,

    /** A source resource changed since its last successful processing. */
    SOURCE_CHANGE,

    /** A source resource was deleted after successful processing. */
    SOURCE_DELETION,

    /** An existing destination was replaced by a new resource. */
    DESTINATION_REPLACEMENT,

    /** A manual retry was issued for a failed execution. */
    MANUAL_RETRY,

    /** A configuration value or pipeline definition was changed. */
    CONFIGURATION_CHANGE,

    /** A pipeline was published or unregistered. */
    PIPELINE_PUBLISHING,

    /** A connection profile was added, updated, or removed. */
    CONNECTION_CHANGE,

    /** A secret was rotated in an external secret store. */
    SECRET_ROTATION,

    /** An authorization check denied access to a resource. */
    AUTHORIZATION_FAILURE,

    /** A worker assumed ownership of a lease via fencing. */
    WORKER_FENCING,

    /** A lease was taken over by another worker. */
    LEASE_TAKEOVER,

    /** A recovery operation was executed. */
    RECOVERY,

    /** A diagnostic operation was run. */
    DIAGNOSTICS,

    /** A test email operation was performed. */
    TEST_EMAIL_OPERATION
}
