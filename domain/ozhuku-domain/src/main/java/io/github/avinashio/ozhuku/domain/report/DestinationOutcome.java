package io.github.avinashio.ozhuku.domain.report;

import io.github.avinashio.ozhuku.domain.deduplication.ProcessingStatus;

/**
 * Represents the outcome of processing a destination resource
 * within an execution report.
 */
public final class DestinationOutcome {

    private final String resourceId;
    private final ProcessingStatus status;
    private final String detail;

    /**
     * Creates a destination outcome.
     *
     * @param resourceId the resource identifier
     * @param status     the processing status
     * @param detail     additional processing details
     */
    public DestinationOutcome(
            final String resourceId,
            final ProcessingStatus status,
            final String detail) {

        this.resourceId = resourceId;
        this.status = status;
        this.detail = detail;
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
     * Returns the processing status.
     *
     * @return processing status
     */
    public ProcessingStatus status() {
        return status;
    }

    /**
     * Returns the processing detail.
     *
     * @return processing detail
     */
    public String detail() {
        return detail;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof DestinationOutcome)) {
            return false;
        }

        final DestinationOutcome that = (DestinationOutcome) other;
        return resourceId.equals(that.resourceId);
    }

    @Override
    public int hashCode() {
        return resourceId.hashCode();
    }

    @Override
    public String toString() {
        return "DestinationOutcome{"
                + "resourceId=" + resourceId
                + ", status=" + status
                + ", detail=" + detail
                + '}';
    }
}
