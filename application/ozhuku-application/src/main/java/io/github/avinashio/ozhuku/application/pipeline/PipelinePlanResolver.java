package io.github.avinashio.ozhuku.application.pipeline;

import io.github.avinashio.ozhuku.domain.delivery.DeliveryPolicy;
import io.github.avinashio.ozhuku.domain.flow.Flow;
import io.github.avinashio.ozhuku.domain.pipeline.PipelineDefinition;
import io.github.avinashio.ozhuku.domain.pipeline.PipelinePlan;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.foundation.validation.Validation;

/**
 * Resolves already-validated pipeline inputs into an executable pipeline plan.
 *
 * Avinash: Keep configuration resolution separate from the Flow domain object.
 */
public final class PipelinePlanResolver {

    public PipelinePlan resolve(
            final PipelineDefinition pipelineDefinition,
            final Flow flow,
            final Resource source,
            final Resource destination,
            final DeliveryPolicy deliveryPolicy) {

        return new PipelinePlan(
                Validation.requireNonNull(
                        pipelineDefinition,
                        "pipelineDefinition must not be null"),
                Validation.requireNonNull(
                        flow,
                        "flow must not be null"),
                Validation.requireNonNull(
                        source,
                        "source must not be null"),
                Validation.requireNonNull(
                        destination,
                        "destination must not be null"),
                Validation.requireNonNull(
                        deliveryPolicy,
                        "deliveryPolicy must not be null"));
    }
}