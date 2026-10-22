package io.github.avinashio.ozhuku.acquisition;

/**
 * Lifecycle states for a push/inbound resource.
 *
 * <p>Resources received via inbound gateways progress through these
 * states:
 * <pre>
 *   RECEIVING → RECEIVED → AVAILABLE → PROCESSING → COMPLETED
 * </pre>
 * </p>
 */
public enum PushResourceStatus {

    /**
     * The resource is currently being received (upload in progress).
     */
    RECEIVING,

    /**
     * The resource has been fully received and durably staged,
     * but has not yet been made available for processing.
     */
    RECEIVED,

    /**
     * The resource is available for processing.
     */
    AVAILABLE,

    /**
     * The resource is currently being processed.
     */
    PROCESSING,

    /**
     * The resource has been fully processed.
     */
    COMPLETED
}
