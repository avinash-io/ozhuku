package io.github.avinashio.ozhuku.acquisition;

import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Persistence contract for push/inbound resources.
 *
 * <p>Stores durable metadata about resources received via inbound gateways,
 * enabling discovery of newly received resources for processing.</p>
 */
public interface PushResourceRepository {

    /**
     * Persists a newly received push resource.
     *
     * @param resource the resource to persist
     * @throws IOException if the resource cannot be persisted
     */
    void save(
            PushResource resource) throws IOException;

    /**
     * Finds a push resource by its identifier.
     *
     * @param resourceId the resource identifier
     * @return the resource, or empty if not found
     * @throws IOException if the lookup fails
     */
    Optional<PushResource> findById(
            ResourceId resourceId) throws IOException;

    /**
     * Returns all push resources that are available for processing
     * (status {@link PushResourceStatus#AVAILABLE}).
     *
     * @return list of available resources
     * @throws IOException if the query fails
     */
    List<PushResource> findAvailable() throws IOException;

    /**
     * Updates the status of a push resource.
     *
     * @param resourceId the resource identifier
     * @param status     the new status
     * @throws IOException if the update fails
     */
    void updateStatus(
            ResourceId resourceId,
            PushResourceStatus status) throws IOException;
}
