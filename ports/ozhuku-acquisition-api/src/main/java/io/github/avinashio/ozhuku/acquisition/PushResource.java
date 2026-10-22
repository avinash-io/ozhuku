package io.github.avinashio.ozhuku.acquisition;

import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.domain.resource.ResourceLocation;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Represents a resource received via a push/inbound mechanism.
 *
 * <p>Contains metadata about a resource received through an inbound gateway
 * (HTTP upload, SFTP drop, etc.) and provides streaming access to its
 * content. The resource is staged durably before being made available for
 * processing.</p>
 *
 * <p>Lifecycle:
 * <pre>
 *   RECEIVING → RECEIVED → AVAILABLE → PROCESSING → COMPLETED
 * </pre></p>
 */
public final class PushResource {

    private final ResourceId resourceId;
    private final ResourceLocation stagingLocation;
    private final String originalFilename;
    private final long size;
    private final String contentType;
    private final long receivedAt;
    private final String checksum;
    private final PushResourceStatus status;

    private PushResource(
            final ResourceId resourceId,
            final ResourceLocation stagingLocation,
            final String originalFilename,
            final long size,
            final String contentType,
            final long receivedAt,
            final String checksum,
            final PushResourceStatus status) {

        this.resourceId = Objects.requireNonNull(
                resourceId,
                "resourceId must not be null");
        this.stagingLocation = Objects.requireNonNull(
                stagingLocation,
                "stagingLocation must not be null");
        this.originalFilename = originalFilename;
        this.size = size;
        this.contentType = contentType;
        this.receivedAt = receivedAt;
        this.checksum = checksum;
        this.status = Objects.requireNonNull(
                status,
                "status must not be null");
    }

    /**
     * Creates a push resource in the RECEIVED state.
     *
     * @param resourceId        the resource identifier
     * @param stagingLocation   the durable staging location
     * @param originalFilename  the original filename from the sender
     * @param size              the resource size in bytes
     * @param contentType       the content type
     * @param receivedAt        the timestamp when the resource was fully received
     * @param checksum          the checksum of the resource content
     * @return a new push resource
     */
    public static PushResource received(
            final ResourceId resourceId,
            final ResourceLocation stagingLocation,
            final String originalFilename,
            final long size,
            final String contentType,
            final long receivedAt,
            final String checksum) {

        return new PushResource(
                resourceId,
                stagingLocation,
                originalFilename,
                size,
                contentType,
                receivedAt,
                checksum,
                PushResourceStatus.RECEIVED);
    }

    /**
     * Returns the resource identifier.
     *
     * @return resource identifier
     */
    public ResourceId resourceId() {
        return resourceId;
    }

    /**
     * Returns the staging location where the resource content is stored durably.
     *
     * @return staging location
     */
    public ResourceLocation stagingLocation() {
        return stagingLocation;
    }

    /**
     * Returns the original filename provided by the sender, if available.
     *
     * @return original filename, or {@code null} if not provided
     */
    public String originalFilename() {
        return originalFilename;
    }

    /**
     * Returns the resource size in bytes.
     *
     * @return size in bytes
     */
    public long size() {
        return size;
    }

    /**
     * Returns the content type of the resource.
     *
     * @return content type, or {@code null} if unknown
     */
    public String contentType() {
        return contentType;
    }

    /**
     * Returns the timestamp when the resource was fully received.
     *
     * @return received-at timestamp in epoch millis
     */
    public long receivedAt() {
        return receivedAt;
    }

    /**
     * Returns the checksum of the resource content.
     *
     * @return checksum, or {@code null} if not computed
     */
    public String checksum() {
        return checksum;
    }

    /**
     * Returns the current status of the push resource.
     *
     * @return status
     */
    public PushResourceStatus status() {
        return status;
    }

    /**
     * Transitions this resource to the AVAILABLE state.
     *
     * @return a new push resource in the AVAILABLE state
     * @throws IllegalStateException if the resource is not in RECEIVED state
     */
    public PushResource markAvailable() {
        if (status != PushResourceStatus.RECEIVED) {
            throw new IllegalStateException(
                    "Resource must be in RECEIVED state to mark available");
        }
        return new PushResource(
                resourceId,
                stagingLocation,
                originalFilename,
                size,
                contentType,
                receivedAt,
                checksum,
                PushResourceStatus.AVAILABLE);
    }

    /**
     * Transitions this resource to the PROCESSING state.
     *
     * @return a new push resource in the PROCESSING state
     * @throws IllegalStateException if the resource is not in AVAILABLE state
     */
    public PushResource markProcessing() {
        if (status != PushResourceStatus.AVAILABLE) {
            throw new IllegalStateException(
                    "Resource must be in AVAILABLE state to mark processing");
        }
        return new PushResource(
                resourceId,
                stagingLocation,
                originalFilename,
                size,
                contentType,
                receivedAt,
                checksum,
                PushResourceStatus.PROCESSING);
    }

    /**
     * Transitions this resource to the COMPLETED state.
     *
     * @return a new push resource in the COMPLETED state
     * @throws IllegalStateException if the resource is not in PROCESSING state
     */
    public PushResource markCompleted() {
        if (status != PushResourceStatus.PROCESSING) {
            throw new IllegalStateException(
                    "Resource must be in PROCESSING state to mark completed");
        }
        return new PushResource(
                resourceId,
                stagingLocation,
                originalFilename,
                size,
                contentType,
                receivedAt,
                checksum,
                PushResourceStatus.COMPLETED);
    }

    /**
     * Opens a streaming input for the resource content.
     *
     * @return input stream for resource content
     * @throws IOException if the content cannot be opened
     */
    public InputStream openContent() throws IOException {
        return stagingLocation.uri()
                .toURL()
                .openStream();
    }
}
