package io.github.avinashio.ozhuku.domain.identity;

import io.github.avinashio.ozhuku.foundation.validation.Validation;
import java.util.Objects;

/**
 * Immutable identity used to determine whether a source resource has already
 * been processed for a specific pipeline version.
 *
 * <p>Processing identity is intentionally separate from execution identity.
 * Multiple execution attempts may refer to the same processing identity.</p>
 */
public final class ProcessingIdentity {

    private final SourceIdentity sourceIdentity;
    private final PipelineId pipelineId;
    private final PipelineVersion pipelineVersion;

    /**
     * Creates a processing identity.
     *
     * @param sourceIdentity logical source resource identity
     * @param pipelineId pipeline identifier
     * @param pipelineVersion pipeline version used for processing
     */
    public ProcessingIdentity(
            final SourceIdentity sourceIdentity,
            final PipelineId pipelineId,
            final PipelineVersion pipelineVersion) {

        this.sourceIdentity = Validation.requireNonNull(
                sourceIdentity,
                "Source identity must not be null");
        this.pipelineId = Validation.requireNonNull(
                pipelineId,
                "Pipeline ID must not be null");
        this.pipelineVersion = Validation.requireNonNull(
                pipelineVersion,
                "Pipeline version must not be null");
    }

    /**
     * Returns the source identity.
     *
     * @return source identity
     */
    public SourceIdentity sourceIdentity() {
        return sourceIdentity;
    }

    /**
     * Returns the pipeline identifier.
     *
     * @return pipeline identifier
     */
    public PipelineId pipelineId() {
        return pipelineId;
    }

    /**
     * Returns the pipeline version.
     *
     * @return pipeline version
     */
    public PipelineVersion pipelineVersion() {
        return pipelineVersion;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof ProcessingIdentity)) {
            return false;
        }

        final ProcessingIdentity that = (ProcessingIdentity) other;

        return sourceIdentity.equals(that.sourceIdentity)
                && pipelineId.equals(that.pipelineId)
                && pipelineVersion.equals(that.pipelineVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                sourceIdentity,
                pipelineId,
                pipelineVersion);
    }

    @Override
    public String toString() {
        return "ProcessingIdentity{"
                + "sourceIdentity=" + sourceIdentity
                + ", pipelineId=" + pipelineId
                + ", pipelineVersion=" + pipelineVersion
                + '}';
    }
}