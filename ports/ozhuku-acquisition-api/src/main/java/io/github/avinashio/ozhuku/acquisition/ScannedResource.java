package io.github.avinashio.ozhuku.acquisition;

import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.domain.resource.ResourceLocation;
import java.util.Objects;

/**
 * A resource discovered during source scanning.
 *
 * <p>Contains the resource location and optional connection metadata observed
 * during the scan. The resource identity is resolved later by the source
 * identity provider.</p>
 */
public final class ScannedResource {

    private final ResourceId resourceId;
    private final ResourceLocation location;
    private final long size;
    private final long lastModified;
    private final String etag;
    private final String version;

    private ScannedResource(
            final ResourceId resourceId,
            final ResourceLocation location,
            final long size,
            final long lastModified,
            final String etag,
            final String version) {

        this.resourceId = Objects.requireNonNull(
                resourceId,
                "resourceId must not be null");
        this.location = Objects.requireNonNull(
                location,
                "location must not be null");
        this.size = size;
        this.lastModified = lastModified;
        this.etag = etag;
        this.version = version;
    }

    /**
     * Creates a scanned resource with full metadata.
     *
     * @param resourceId   the resource identifier
     * @param location     the resource location
     * @param size         the resource size in bytes, or -1 if unknown
     * @param lastModified the last modified timestamp in epoch millis,
     *                     or 0 if unknown
     * @param etag         the ETag or checksum, or {@code null} if unknown
     * @param version      the version identifier, or {@code null} if unknown
     */
    public static ScannedResource of(
            final ResourceId resourceId,
            final ResourceLocation location,
            final long size,
            final long lastModified,
            final String etag,
            final String version) {

        return new ScannedResource(
                resourceId,
                location,
                size,
                lastModified,
                etag,
                version);
    }

    /**
     * Creates a scanned resource with minimal metadata.
     *
     * @param resourceId the resource identifier
     * @param location   the resource location
     */
    public static ScannedResource of(
            final ResourceId resourceId,
            final ResourceLocation location) {

        return new ScannedResource(
                resourceId,
                location,
                -1L,
                0L,
                null,
                null);
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
     * Returns the resource location.
     *
     * @return resource location
     */
    public ResourceLocation location() {
        return location;
    }

    /**
     * Returns the resource size in bytes, or -1 if unknown.
     *
     * @return size in bytes
     */
    public long size() {
        return size;
    }

    /**
     * Returns the last modified timestamp in epoch millis, or 0 if unknown.
     *
     * @return last modified timestamp
     */
    public long lastModified() {
        return lastModified;
    }

    /**
     * Returns the ETag or checksum, or {@code null} if unknown.
     *
     * @return ETag or checksum
     */
    public String etag() {
        return etag;
    }

    /**
     * Returns the version identifier, or {@code null} if unknown.
     *
     * @return version identifier
     */
    public String version() {
        return version;
    }

    /**
     * Converts this scanned resource to a domain {@link Resource}.
     *
     * @return the domain resource
     */
    public Resource toResource() {
        return new Resource(resourceId, location);
    }
}
