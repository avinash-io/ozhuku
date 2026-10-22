package io.github.avinashio.ozhuku.server.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.nio.file.Path;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Type-safe configuration properties for the Ozhuku server.
 *
 * <p>Properties are prefixed with {@code ozhuku} and configured
 * in {@code application.properties}. This class validates that
 * required settings are present at application startup.</p>
 */
@ConfigurationProperties(prefix = "ozhuku")
@Validated
public class OzhukuServerProperties {

    /**
     * Root directory for file storage operations.
     * Bound from {@code ozhuku.storageRoot}.
     */
    @NotBlank
    private String storageRoot = "./workspace";

    /**
     * Maximum number of concurrent pipeline executions.
     * Bound from {@code ozhuku.max-concurrent-executions}.
     * Defaults to 4 if not specified.
     */
    private int maxConcurrentExecutions = 4;

    /**
     * Whether to enable distributed execution leases (clustered mode).
     * Bound from {@code ozhuku.distributed-enabled}.
     * Defaults to false (single-node mode).
     */
    private boolean distributedEnabled = false;

    /**
     * Lease duration in seconds before a stale claim is considered
     * abandoned. Only relevant when distributed mode is enabled.
     * Bound from {@code ozhuku.lease-timeout-seconds}.
     * Defaults to 30 seconds.
     */
    private int leaseTimeoutSeconds = 30;

    /**
     * Unique identifier for this worker node. Used when acquiring
     * execution leases to identify the worker holding a lease.
     * Only relevant when distributed mode is enabled.
     * Bound from {@code ozhuku.worker-id}.
     */
    private String workerId = "ozhuku-worker";

    /**
     * Returns the configured storage root as a Path.
     *
     * @return the storage root path
     */
    public Path storageRootPath() {
        return Path.of(storageRoot);
    }

    /**
     * Returns the raw storage root string.
     *
     * @return the storage root directory path string
     */
    public String storageRoot() {
        return storageRoot;
    }

    /**
     * Sets the storage root directory.
     *
     * @param storageRoot the storage root path
     */
    public void setStorageRoot(
            final String storageRoot) {
        this.storageRoot = storageRoot;
    }

    /**
     * Returns the maximum number of concurrent pipeline executions.
     *
     * @return the max concurrent executions, minimum 1
     */
    @Min(1)
    public int getMaxConcurrentExecutions() {
        return Math.max(maxConcurrentExecutions, 1);
    }

    /**
     * Sets the maximum concurrent executions.
     *
     * @param maxConcurrentExecutions the max concurrent executions
     */
    public void setMaxConcurrentExecutions(
            final int maxConcurrentExecutions) {
        this.maxConcurrentExecutions = maxConcurrentExecutions;
    }

    /**
     * Returns whether distributed execution mode is enabled.
     *
     * @return true if distributed mode is enabled
     */
    public boolean isDistributedEnabled() {
        return distributedEnabled;
    }

    /**
     * Sets whether distributed mode is enabled.
     *
     * @param distributedEnabled whether distributed mode is enabled
     */
    public void setDistributedEnabled(
            final boolean distributedEnabled) {
        this.distributedEnabled = distributedEnabled;
    }

    /**
     * Returns the lease timeout in seconds.
     *
     * @return the lease timeout in seconds
     */
    @Min(1)
    public int getLeaseTimeoutSeconds() {
        return leaseTimeoutSeconds;
    }

    /**
     * Sets the lease timeout in seconds.
     *
     * @param leaseTimeoutSeconds the lease timeout in seconds
     */
    public void setLeaseTimeoutSeconds(
            final int leaseTimeoutSeconds) {
        this.leaseTimeoutSeconds = leaseTimeoutSeconds;
    }

    /**
     * Returns the configured worker identifier.
     *
     * @return the worker ID
     */
    @NotBlank
    public String getWorkerId() {
        return workerId;
    }

    /**
     * Sets the worker identifier.
     *
     * @param workerId the worker ID
     */
    public void setWorkerId(
            final String workerId) {
        this.workerId = workerId;
    }
}
