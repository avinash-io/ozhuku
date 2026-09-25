package io.github.avinashio.ozhuku.domain.pipeline;

import io.github.avinashio.ozhuku.domain.delivery.DeliveryPolicy;
import io.github.avinashio.ozhuku.domain.flow.Flow;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.foundation.validation.Validation;
import java.util.Objects;

/**
 * Immutable executable plan resolved from a pipeline definition and flow.
 *
 * <p>The plan contains the concrete resources and delivery policy required
 * to execute a flow. Infrastructure implementations, credentials, format
 * readers, format writers, and transport implementations remain outside the
 * domain plan.</p>
 */
public final class PipelinePlan {

    private final PipelineDefinition pipelineDefinition;
    private final Flow flow;
    private final Resource source;
    private final Resource destination;
    private final DeliveryPolicy deliveryPolicy;

    /**
     * Creates an executable pipeline plan.
     *
     * @param pipelineDefinition pipeline definition being executed
     * @param flow flow to execute
     * @param source resolved source resource
     * @param destination resolved destination resource
     * @param deliveryPolicy destination delivery policy
     */
    public PipelinePlan(
            final PipelineDefinition pipelineDefinition,
            final Flow flow,
            final Resource source,
            final Resource destination,
            final DeliveryPolicy deliveryPolicy) {

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
    }

    /**
     * Returns the pipeline definition.
     *
     * @return pipeline definition
     */
    public PipelineDefinition pipelineDefinition() {
        return pipelineDefinition;
    }

    /**
     * Returns the flow.
     *
     * @return flow
     */
    public Flow flow() {
        return flow;
    }

    /**
     * Returns the resolved source resource.
     *
     * @return source resource
     */
    public Resource source() {
        return source;
    }

    /**
     * Returns the resolved destination resource.
     *
     * @return destination resource
     */
    public Resource destination() {
        return destination;
    }

    /**
     * Returns the destination delivery policy.
     *
     * @return delivery policy
     */
    public DeliveryPolicy deliveryPolicy() {
        return deliveryPolicy;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof PipelinePlan)) {
            return false;
        }

        final PipelinePlan that = (PipelinePlan) other;

        return pipelineDefinition.equals(that.pipelineDefinition)
                && flow.equals(that.flow)
                && source.equals(that.source)
                && destination.equals(that.destination)
                && deliveryPolicy.equals(that.deliveryPolicy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                pipelineDefinition,
                flow,
                source,
                destination,
                deliveryPolicy);
    }

    @Override
    public String toString() {
        return "PipelinePlan{"
                + "pipelineDefinition=" + pipelineDefinition
                + ", flow=" + flow
                + ", source=" + source
                + ", destination=" + destination
                + ", deliveryPolicy=" + deliveryPolicy
                + '}';
    }
}