package io.github.avinashio.ozhuku.domain.flow;

import io.github.avinashio.ozhuku.domain.identity.FlowId;
import io.github.avinashio.ozhuku.foundation.validation.Validation;
import java.util.Objects;

/**
 * Declarative flow within a pipeline.
 */
public final class Flow {

    private final FlowId id;
    private final String name;
    private final FlowMode mode;

    /**
     * Creates a flow.
     *
     * @param id flow identifier
     * @param name human-readable flow name
     * @param mode flow processing mode
     */
    public Flow(
            final FlowId id,
            final String name,
            final FlowMode mode) {

        this.id = Validation.requireNonNull(
                id,
                "Flow ID must not be null");
        this.name = Validation.requireNonBlank(
                name,
                "Flow name must not be blank");
        this.mode = Validation.requireNonNull(
                mode,
                "Flow mode must not be null");
    }

    /**
     * Returns the flow identifier.
     *
     * @return flow identifier
     */
    public FlowId id() {
        return id;
    }

    /**
     * Returns the flow name.
     *
     * @return flow name
     */
    public String name() {
        return name;
    }

    /**
     * Returns the processing mode.
     *
     * @return flow mode
     */
    public FlowMode mode() {
        return mode;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof Flow)) {
            return false;
        }

        final Flow that = (Flow) other;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}