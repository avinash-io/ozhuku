package io.github.avinashio.ozhuku.application.pipeline;

import io.github.avinashio.ozhuku.application.execution.PipelineExecutionService;
import io.github.avinashio.ozhuku.domain.execution.ExecutionReference;
import io.github.avinashio.ozhuku.domain.identity.PipelineId;
import io.github.avinashio.ozhuku.domain.identity.PipelineVersion;
import io.github.avinashio.ozhuku.domain.pipeline.PipelinePlan;
import io.github.avinashio.ozhuku.foundation.validation.Validation;
import java.io.IOException;

public final class ConfiguredPipelineExecutionService {

    private final PipelineConfigurationResolutionService
            configurationResolutionService;

    private final PipelineExecutionService pipelineExecutionService;

    public ConfiguredPipelineExecutionService(
            final PipelineConfigurationResolutionService configurationResolutionService,
            final PipelineExecutionService pipelineExecutionService) {
        this.configurationResolutionService =
                Validation.requireNonNull(
                        configurationResolutionService,
                        "configurationResolutionService");

        this.pipelineExecutionService =
                Validation.requireNonNull(
                        pipelineExecutionService,
                        "pipelineExecutionService");
    }

    public void execute(
            final ExecutionReference executionReference,
            final PipelineId pipelineId,
            final PipelineVersion pipelineVersion)
            throws IOException {

        Validation.requireNonNull(executionReference, "executionReference");
        Validation.requireNonNull(pipelineId, "pipelineId");
        Validation.requireNonNull(pipelineVersion, "pipelineVersion");

        final PipelinePlan pipelinePlan =
                configurationResolutionService.resolve(
                        pipelineId,
                        pipelineVersion);

        pipelineExecutionService.execute(
                executionReference,
                pipelinePlan);
    }
}