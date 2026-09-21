package io.github.avinashio.ozhuku.domain.pipeline;

import io.github.avinashio.ozhuku.domain.identity.PipelineId;
import io.github.avinashio.ozhuku.foundation.validation.Validation;
import java.util.Objects;

/**
 * Stable identity of an Ozhuku pipeline.
 *
 * <p>A pipeline does not contain a mutable configuration. Concrete
 * configuration belongs to a versioned pipeline definition.</p>
 */
public final class Pipeline {

    private final PipelineId id;
    private final String name;

    /**
     * Creates a pipeline.
     *
     * @param id pipeline identifier
     * @param name human-readable pipeline name
     */
    public Pipeline(final PipelineId id, final String name) {
        this.id = Validation.requireNonNull(
                id,
                "Pipeline ID must not be null");
        this.name = Validation.requireNonBlank(
                name,
                "Pipeline name must not be blank");
    }

    /**
     * Returns the pipeline identifier.
     *
     * @return pipeline identifier
     */
    public PipelineId id() {
        return id;
    }

    /**
     * Returns the human-readable pipeline name.
     *
     * @return pipeline name
     */
    public String name() {
        return name;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof Pipeline)) {
            return false;
        }

        final Pipeline that = (Pipeline) other;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}