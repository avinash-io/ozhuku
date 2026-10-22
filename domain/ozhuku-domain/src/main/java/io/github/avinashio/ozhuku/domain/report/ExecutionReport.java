package io.github.avinashio.ozhuku.domain.report;

import io.github.avinashio.ozhuku.domain.audit.AuditEvent;
import io.github.avinashio.ozhuku.domain.audit.AuditEventId;
import io.github.avinashio.ozhuku.domain.audit.AuditEventType;
import io.github.avinashio.ozhuku.domain.deduplication.ProcessingStatus;
import io.github.avinashio.ozhuku.domain.execution.ExecutionStatus;
import io.github.avinashio.ozhuku.foundation.validation.Validation;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a consolidated execution report for an Ozhuku execution.
 *
 * <p>The report unit is the <strong>execution</strong>, not each individual file.
 * One execution processing ten files produces one consolidated execution report.
 *
 * <p>Report != Audit. This is a separate entity for execution results and notifications,
 * distinct from audit events which record significant security/administrative events.
 */
public final class ExecutionReport {

    private final String reportId;
    private final Instant generatedAt;
    private final String pipelineId;
    private final String executionId;
    private final String organizationId;
    private final ExecutionStatus status;
    private final Instant startTime;
    private final Instant endTime;
    private final int filesDiscovered;
    private final int filesProcessed;
    private final int filesFailed;
    private final int filesSkipped;
    private final List<DestinationOutcome> destinationOutcomes;
    private final String overallStatus;
    private final String errors;
    private final AuditEvent auditEvent;

    /**
     * Creates an execution report.
     *
     * @param reportId             unique identifier for this report
     * @param generatedAt          when the report was generated
     * @param pipelineId           pipeline identifier
     * @param executionId          execution identifier
     * @param organizationId       owning organization identifier
     * @param status               execution status
     * @param startTime            execution start timestamp
     * @param endTime              execution completion timestamp
     * @param filesDiscovered      number of files discovered
     * @param filesProcessed       number of files successfully processed
     * @param filesFailed          number of files that failed processing
     * @param filesSkipped         number of files skipped (deduplicated)
     * @param destinationOutcomes  list of destination processing outcomes
     * @param overallStatus        overall processing status
     * @param errors               error messages, or null
     * @param auditEvent           associated audit event, or null
     */
    public ExecutionReport(
            final String reportId,
            final Instant generatedAt,
            final String pipelineId,
            final String executionId,
            final String organizationId,
            final ExecutionStatus status,
            final Instant startTime,
            final Instant endTime,
            final int filesDiscovered,
            final int filesProcessed,
            final int filesFailed,
            final int filesSkipped,
            final List<DestinationOutcome> destinationOutcomes,
            final String overallStatus,
            final String errors,
            final AuditEvent auditEvent) {

        this.reportId = Validation.requireNonBlank(
                reportId,
                "Report ID must not be blank");
        this.generatedAt = Validation.requireNonNull(
                generatedAt,
                "Generated at must not be null");
        this.pipelineId = Validation.requireNonBlank(
                pipelineId,
                "Pipeline ID must not be blank");
        this.executionId = Validation.requireNonBlank(
                executionId,
                "Execution ID must not be blank");
        this.organizationId = Validation.requireNonBlank(
                organizationId,
                "Organization ID must not be blank");
        this.status = Validation.requireNonNull(
                status,
                "Status must not be null");
        this.startTime = startTime;
        this.endTime = endTime;
        this.filesDiscovered = filesDiscovered;
        this.filesProcessed = filesProcessed;
        this.filesFailed = filesFailed;
        this.filesSkipped = filesSkipped;
        this.destinationOutcomes = List.copyOf(
                Objects.requireNonNull(destinationOutcomes,
                        "Destination outcomes must not be null"));
        this.overallStatus = Validation.requireNonNull(
                overallStatus,
                "Overall status must not be null");
        this.errors = errors;
        this.auditEvent = auditEvent;
    }

    /**
     * Returns the unique report identifier.
     *
     * @return report identifier
     */
    public String reportId() {
        return reportId;
    }

    /**
     * Returns the report generation timestamp.
     *
     * @return generation timestamp
     */
    public Instant generatedAt() {
        return generatedAt;
    }

    /**
     * Returns the pipeline identifier.
     *
     * @return pipeline identifier
     */
    public String pipelineId() {
        return pipelineId;
    }

    /**
     * Returns the execution identifier.
     *
     * @return execution identifier
     */
    public String executionId() {
        return executionId;
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
     * Returns the execution status.
     *
     * @return execution status
     */
    public ExecutionStatus status() {
        return status;
    }

    /**
     * Returns the overall processing status.
     *
     * @return overall status
     */
    public String overallStatus() {
        return overallStatus;
    }

    /**
     * Returns the associated audit event.
     *
     * @return audit event, or null
     */
    public AuditEvent auditEvent() {
        return auditEvent;
    }

    /**
     * Returns the execution start timestamp.
     *
     * @return start timestamp
     */
    public Instant startTime() {
        return startTime;
    }

    /**
     * Returns the execution completion timestamp.
     *
     * @return completion timestamp
     */
    public Instant endTime() {
        return endTime;
    }

    /**
     * Returns the number of files discovered.
     *
     * @return files discovered
     */
    public int filesDiscovered() {
        return filesDiscovered;
    }

    /**
     * Returns the number of files successfully processed.
     *
     * @return files processed
     */
    public int filesProcessed() {
        return filesProcessed;
    }

    /**
     * Returns the number of files that failed processing.
     *
     * @return files failed
     */
    public int filesFailed() {
        return filesFailed;
    }

    /**
     * Returns the number of files skipped (deduplicated).
     *
     * @return files skipped
     */
    public int filesSkipped() {
        return filesSkipped;
    }

    /**
     * Returns the list of destination processing outcomes.
     *
     * @return destination outcomes
     */
    public List<DestinationOutcome> destinationOutcomes() {
        return destinationOutcomes;
    }

    /**
     * Returns the error messages.
     *
     * @return errors, or null
     */
    public String errors() {
        return errors;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof ExecutionReport)) {
            return false;
        }

        final ExecutionReport that = (ExecutionReport) other;
        return reportId.equals(that.reportId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reportId);
    }

    @Override
    public String toString() {
        return "ExecutionReport{"
                + "reportId=" + reportId
                + ", generatedAt=" + generatedAt
                + ", pipelineId=" + pipelineId
                + ", executionId=" + executionId
                + ", organizationId=" + organizationId
                + ", status=" + status
                + ", startTime=" + startTime
                + ", endTime=" + endTime
                + ", filesDiscovered=" + filesDiscovered
                + ", filesProcessed=" + filesProcessed
                + ", filesFailed=" + filesFailed
                + ", filesSkipped=" + filesSkipped
                + ", destinationOutcomes=" + destinationOutcomes
                + ", overallStatus=" + overallStatus
                + ", errors=" + errors
                + ", auditEvent=" + auditEvent
                + '}';
    }

    /**
     * Creates an execution report builder.
     *
     * @return new report builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link ExecutionReport}.
     */
    public static final class Builder {

        private String reportId;
        private Instant generatedAt;
        private String pipelineId;
        private String executionId;
        private String organizationId;
        private ExecutionStatus status;
        private Instant startTime;
        private Instant endTime;
        private int filesDiscovered;
        private int filesProcessed;
        private int filesFailed;
        private int filesSkipped;
        private List<DestinationOutcome> destinationOutcomes;
        private String overallStatus;
        private String errors;
        private AuditEvent auditEvent;

        private Builder() {
        }

        /**
         * Sets the report ID.
         *
         * @param reportId the report ID
         * @return this builder
         */
        public Builder reportId(final String reportId) {
            this.reportId = Validation.requireNonBlank(
                    reportId,
                    "Report ID must not be blank");
            return this;
        }

        /**
         * Sets the generation timestamp.
         *
         * @param generatedAt the generation timestamp
         * @return this builder
         */
        public Builder generatedAt(final Instant generatedAt) {
            this.generatedAt = Validation.requireNonNull(
                    generatedAt,
                    "Generated at must not be null");
            return this;
        }

        /**
         * Sets the pipeline ID.
         *
         * @param pipelineId the pipeline ID
         * @return this builder
         */
        public Builder pipelineId(final String pipelineId) {
            this.pipelineId = Validation.requireNonBlank(
                    pipelineId,
                    "Pipeline ID must not be blank");
            return this;
        }

        /**
         * Sets the execution ID.
         *
         * @param executionId the execution ID
         * @return this builder
         */
        public Builder executionId(final String executionId) {
            this.executionId = Validation.requireNonBlank(
                    executionId,
                    "Execution ID must not be blank");
            return this;
        }

        /**
         * Sets the organization ID.
         *
         * @param organizationId the organization ID
         * @return this builder
         */
        public Builder organizationId(final String organizationId) {
            this.organizationId = Validation.requireNonBlank(
                    organizationId,
                    "Organization ID must not be blank");
            return this;
        }

        /**
         * Sets the execution status.
         *
         * @param status the execution status
         * @return this builder
         */
        public Builder status(final ExecutionStatus status) {
            this.status = Validation.requireNonNull(
                    status,
                    "Status must not be null");
            return this;
        }

        /**
         * Sets the start timestamp.
         *
         * @param startTime the start timestamp
         * @return this builder
         */
        public Builder startTime(final Instant startTime) {
            this.startTime = startTime;
            return this;
        }

        /**
         * Sets the end timestamp.
         *
         * @param endTime the end timestamp
         * @return this builder
         */
        public Builder endTime(final Instant endTime) {
            this.endTime = endTime;
            return this;
        }

        /**
         * Sets the files discovered count.
         *
         * @param filesDiscovered files discovered
         * @return this builder
         */
        public Builder filesDiscovered(final int filesDiscovered) {
            this.filesDiscovered = filesDiscovered;
            return this;
        }

        /**
         * Sets the files processed count.
         *
         * @param filesProcessed files processed
         * @return this builder
         */
        public Builder filesProcessed(final int filesProcessed) {
            this.filesProcessed = filesProcessed;
            return this;
        }

        /**
         * Sets the files failed count.
         *
         * @param filesFailed files failed
         * @return this builder
         */
        public Builder filesFailed(final int filesFailed) {
            this.filesFailed = filesFailed;
            return this;
        }

        /**
         * Sets the files skipped count.
         *
         * @param filesSkipped files skipped
         * @return this builder
         */
        public Builder filesSkipped(final int filesSkipped) {
            this.filesSkipped = filesSkipped;
            return this;
        }

        /**
         * Sets the destination outcomes.
         *
         * @param destinationOutcomes destination outcomes
         * @return this builder
         */
        public Builder destinationOutcomes(
                final List<DestinationOutcome> destinationOutcomes) {

            this.destinationOutcomes = List.copyOf(
                    Objects.requireNonNull(destinationOutcomes,
                            "Destination outcomes must not be null"));
            return this;
        }

        /**
         * Sets the overall status.
         *
         * @param overallStatus overall status
         * @return this builder
         */
        public Builder overallStatus(final String overallStatus) {
            this.overallStatus = Validation.requireNonNull(
                    overallStatus,
                    "Overall status must not be null");
            return this;
        }

        /**
         * Sets the errors.
         *
         * @param errors errors
         * @return this builder
         */
        public Builder errors(final String errors) {
            this.errors = errors;
            return this;
        }

        /**
         * Sets the audit event.
         *
         * @param auditEvent audit event
         * @return this builder
         */
        public Builder auditEvent(final AuditEvent auditEvent) {
            this.auditEvent = auditEvent;
            return this;
        }

        /**
         * Builds the execution report.
         *
         * @return a new {@link ExecutionReport}
         */
        public ExecutionReport build() {
            return new ExecutionReport(
                    reportId,
                    generatedAt,
                    pipelineId,
                    executionId,
                    organizationId,
                    status,
                    startTime,
                    endTime,
                    filesDiscovered,
                    filesProcessed,
                    filesFailed,
                    filesSkipped,
                    destinationOutcomes,
                    overallStatus,
                    errors,
                    auditEvent);
        }
    }
}
