package io.github.avinashio.ozhuku.application.pipeline;

import io.github.avinashio.ozhuku.domain.identity.PipelineId;
import io.github.avinashio.ozhuku.domain.identity.PipelineVersion;
import io.github.avinashio.ozhuku.domain.pipeline.PipelineConfiguration;
import io.github.avinashio.ozhuku.domain.pipeline.PipelinePlan;
import io.github.avinashio.ozhuku.persistence.PipelineConfigurationRepository;
import io.github.avinashio.ozhuku.foundation.validation.Validation;

/**
 * Resolves a persisted pipeline configuration into an executable pipeline plan.
 *
 * <p>The persistence layer supplies the declarative configuration while the
 * existing pipeline plan resolver remains responsible for creating the
 * executable plan.</p>
 */
public final class PipelineConfigurationResolutionService {

    private final PipelineConfigurationRepository configurationRepository;
    private final PipelinePlanResolver pipelinePlanResolver;

    /**
     * Creates the configuration resolution service.
     *
     * @param configurationRepository pipeline configuration persistence port
     * @param pipelinePlanResolver executable plan resolver
     */
    public PipelineConfigurationResolutionService(
            final PipelineConfigurationRepository configurationRepository,
            final PipelinePlanResolver pipelinePlanResolver) {
        this.configurationRepository =
                Validation.requireNonNull(
                        configurationRepository,
                        "configurationRepository");
        this.pipelinePlanResolver =
                Validation.requireNonNull(
                        pipelinePlanResolver,
                        "pipelinePlanResolver");
    }

    /**
     * Resolves a persisted pipeline configuration version into an executable plan.
     *
     * @param pipelineId pipeline identifier
     * @param pipelineVersion pipeline configuration version
     * @return executable pipeline plan
     */
    public PipelinePlan resolve(
            final PipelineId pipelineId,
            final PipelineVersion pipelineVersion) {
        Validation.requireNonNull(pipelineId, "pipelineId");
        Validation.requireNonNull(pipelineVersion, "pipelineVersion");

        final PipelineConfiguration configuration =
                configurationRepository
                        .findByVersion(pipelineId, pipelineVersion)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Pipeline configuration not found: "
                                                        + pipelineId.value()
                                                        + ":"
                                                        + pipelineVersion.value()));

        return pipelinePlanResolver.resolve(
                configuration.pipelineDefinition(),
                configuration.flow(),
                configuration.source(),
                configuration.destination(),
                configuration.deliveryPolicy());
    }
}