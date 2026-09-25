package io.github.avinashio.ozhuku.application.execution;

import io.github.avinashio.ozhuku.domain.flow.FlowMode;
import io.github.avinashio.ozhuku.domain.pipeline.PipelinePlan;
import io.github.avinashio.ozhuku.domain.execution.ExecutionReference;
import io.github.avinashio.ozhuku.format.FormatReader;
import io.github.avinashio.ozhuku.format.FormatWriter;
import java.io.IOException;
import java.util.Objects;

public final class PipelineExecutionService {

    private final ExecutionRunService executionRunService;

    public PipelineExecutionService(
            final ExecutionRunService executionRunService) {

        this.executionRunService =
                Objects.requireNonNull(
                        executionRunService,
                        "executionRunService must not be null");
    }

    public void execute(
            final ExecutionReference executionReference,
            final PipelinePlan pipelinePlan)
            throws IOException {

        Objects.requireNonNull(
                executionReference,
                "executionReference must not be null");

        Objects.requireNonNull(
                pipelinePlan,
                "pipelinePlan must not be null");

        switch (pipelinePlan.flow().mode()) {
            case RESOURCE_TRANSFER ->
                    executionRunService.runResourceTransfer(
                            executionReference,
                            pipelinePlan);

            case RECORD_PROCESSING ->
                    throw new IllegalArgumentException(
                            "RECORD_PROCESSING requires format reader and writer");

            case RESOURCE_PROCESSING ->
                    throw new UnsupportedOperationException(
                            "RESOURCE_PROCESSING execution is not implemented yet");
        }
    }

    public void executeRecordProcessing(
            final ExecutionReference executionReference,
            final PipelinePlan pipelinePlan,
            final FormatReader formatReader,
            final FormatWriter formatWriter)
            throws IOException {

        Objects.requireNonNull(
                executionReference,
                "executionReference must not be null");

        Objects.requireNonNull(
                pipelinePlan,
                "pipelinePlan must not be null");

        Objects.requireNonNull(
                formatReader,
                "formatReader must not be null");

        Objects.requireNonNull(
                formatWriter,
                "formatWriter must not be null");

        if (pipelinePlan.flow().mode() != FlowMode.RECORD_PROCESSING) {
            throw new IllegalArgumentException(
                    "Pipeline plan flow mode must be RECORD_PROCESSING");
        }

        executionRunService.runRecordProcessing(
                executionReference,
                pipelinePlan,
                formatReader,
                formatWriter);
    }
}