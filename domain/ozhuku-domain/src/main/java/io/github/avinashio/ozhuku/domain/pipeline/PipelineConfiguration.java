package io.github.avinashio.ozhuku.domain.pipeline;

import io.github.avinashio.ozhuku.domain.delivery.DeliveryPolicy;
import io.github.avinashio.ozhuku.domain.deduplication.DuplicatePolicy;
import io.github.avinashio.ozhuku.domain.flow.Flow;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.foundation.validation.Validation;
import java.util.Objects;

/**
 * Immutable declarative configuration required to resolve a pipeline plan.
 *
 * <p>The configuration represents the durable definition of how a pipeline
 * connects its flow, source, destination, delivery policy, and duplicate
 * processing policy. It does not perform execution and does not represent
 * the executable plan.</p>
 */
public final class PipelineConfiguration {

    private final PipelineDefinition pipelineDefinition;
    private final Flow flow;
    private final Resource source;
    private final Resource destination;
    private final DeliveryPolicy deliveryPolicy;
    private final DuplicatePolicy duplicatePolicy;

    /**
     * Creates a pipeline configuration.
     *
     * @param pipelineDefinition versioned pipeline definition
     * @param flow configured flow
     * @param source configured source resource
     * @param destination configured destination resource
     * @param deliveryPolicy destination delivery policy
     * @param duplicatePolicy duplicate processing policy
     */
    public PipelineConfiguration(
            final PipelineDefinition pipelineDefinition,
            final Flow flow,
            final Resource source,
            final Resource destination,
            final DeliveryPolicy deliveryPolicy,
            final DuplicatePolicy duplicatePolicy) {

        this.pipelineDefinition = Validation.requireNonNull(
                pipelineDefinition,
                "Pipeline definition must not be null");
        this.flow = Validation.requireNonNull(
                flow,
                "Flow must not be null");
        this.source = Validation.requireNonNull(
                source,
                "Source resource must not be null");
        this.destination = Validation.requireNonNull(
                destination,
                "Destination resource must not be null");
        this.deliveryPolicy = Validation.requireNonNull(
                deliveryPolicy,
                "Delivery policy must not be null");
        this.duplicatePolicy = Validation.requireNonNull(
                duplicatePolicy,
                "Duplicate policy must not be null");
    }

    /**
     * Returns the versioned pipeline definition.
     *
     * @return pipeline definition
     */
    public PipelineDefinition pipelineDefinition() {
        return pipelineDefinition;
    }

    /**
     * Returns the configured flow.
     *
     * @return flow
     */
    public Flow flow() {
        return flow;
    }

    /**
     * Returns the configured source resource.
     *
     * @return source resource
     */
    public Resource source() {
        return source;
    }

    /**
     * Returns the configured destination resource.
     *
     * @return destination resource
     */
    public Resource destination() {
        return destination;
    }

    /**
     * Returns the configured delivery policy.
     *
     * @return delivery policy
     */
    public DeliveryPolicy deliveryPolicy() {
        return deliveryPolicy;
    }

    /**
     * Returns the configured duplicate processing policy.
     *
     * @return duplicate processing policy
     */
    public DuplicatePolicy duplicatePolicy() {
        return duplicatePolicy;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof PipelineConfiguration)) {
            return false;
        }

        final PipelineConfiguration that = (PipelineConfiguration) other;

        return pipelineDefinition.equals(that.pipelineDefinition)
                && flow.equals(that.flow)
                && source.equals(that.source)
                && destination.equals(that.destination)
                && deliveryPolicy.equals(that.deliveryPolicy)
                && duplicatePolicy.equals(that.duplicatePolicy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                pipelineDefinition,
                flow,
                source,
                destination,
                deliveryPolicy,
                duplicatePolicy);
    }

    @Override
    public String toString() {
        return "PipelineConfiguration{"
                + "pipelineDefinition=" + pipelineDefinition
                + ", flow=" + flow
                + ", source=" + source
                + ", destination=" + destination
                + ", deliveryPolicy=" + deliveryPolicy
                + ", duplicatePolicy=" + duplicatePolicy
                + '}';
    }
}