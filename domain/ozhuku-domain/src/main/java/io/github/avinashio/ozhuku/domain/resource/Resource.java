package io.github.avinashio.ozhuku.domain.resource;

import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.foundation.validation.Validation;
import java.util.Objects;

/**
 * Represents a logical resource participating in an Ozhuku flow.
 *
 * <p>A resource contains its logical identity and current location. Access,
 * transport, credentials, and processing state are managed by other domain
 * and infrastructure concepts.</p>
 */
public final class Resource {

    private final ResourceId id;
    private final ResourceLocation location;

    /**
     * Creates a resource.
     *
     * @param id resource identifier
     * @param location resource location
     */
    public Resource(
            final ResourceId id,
            final ResourceLocation location) {

        this.id = Validation.requireNonNull(
                id,
                "Resource ID must not be null");
        this.location = Validation.requireNonNull(
                location,
                "Resource location must not be null");
    }

    /**
     * Returns the resource identifier.
     *
     * @return resource identifier
     */
    public ResourceId id() {
        return id;
    }

    /**
     * Returns the resource location.
     *
     * @return resource location
     */
    public ResourceLocation location() {
        return location;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof Resource)) {
            return false;
        }

        final Resource that = (Resource) other;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Resource{"
                + "id=" + id
                + ", location=" + location
                + '}';
    }
}