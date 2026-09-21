package io.github.avinashio.ozhuku.domain.pipeline;

import io.github.avinashio.ozhuku.domain.identity.PipelineId;
import io.github.avinashio.ozhuku.domain.identity.PipelineVersion;
import io.github.avinashio.ozhuku.foundation.validation.Validation;
import java.util.Objects;

/**
 * Immutable declarative definition of a specific pipeline version.
 *
 * <p>The definition represents what the pipeline declares. It does not
 * represent the executable plan produced after validation and resolution.</p>
 */
public final class PipelineDefinition {

    private final PipelineId pipelineId;
    private final PipelineVersion version;
    private final String description;

    /**
     * Creates a pipeline definition.
     *
     * @param pipelineId pipeline identifier
     * @param version pipeline version
     * @param description optional human-readable description
     */
    public PipelineDefinition(
            final PipelineId pipelineId,
            final PipelineVersion version,
            final String description) {

        this.pipelineId = Validation.requireNonNull(
                pipelineId,
                "Pipeline ID must not be null");
        this.version = Validation.requireNonNull(
                version,
                "Pipeline version must not be null");

        this.description = description == null
                ? ""
                : description.trim();
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
    public PipelineVersion version() {
        return version;
    }

    /**
     * Returns the optional description.
     *
     * @return description, or an empty string when not supplied
     */
    public String description() {
        return description;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof PipelineDefinition)) {
            return false;
        }

        final PipelineDefinition that = (PipelineDefinition) other;

        return pipelineId.equals(that.pipelineId)
                && version.equals(that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pipelineId, version);
    }

    @Override
    public String toString() {
        return "PipelineDefinition{"
                + "pipelineId=" + pipelineId
                + ", version=" + version
                + '}';
    }
}