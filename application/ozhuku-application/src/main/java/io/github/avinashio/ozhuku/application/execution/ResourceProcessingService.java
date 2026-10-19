package io.github.avinashio.ozhuku.application.execution;

import io.github.avinashio.ozhuku.domain.execution.ExecutionReference;
import io.github.avinashio.ozhuku.domain.pipeline.PipelinePlan;
import io.github.avinashio.ozhuku.application.processing.ResourceTransferRequest;
import java.io.IOException;

/**
 * Skeleton service for the {@code RESOURCE_PROCESSING} flow mode.
 *
 * The full implementation will handle processing of resources that produce
 * intermediate artifacts rather than a simple transfer or record
 * processing. This placeholder provides the required bean definition and API so
 * that the application can compile and the scheduler can invoke it safely.
 */
public final class ResourceProcessingService {

    public ResourceProcessingService() {
        // No dependencies for the placeholder implementation.
    }

    /**
     * Process a {@code RESOURCE_PROCESSING} flow.
     *
     * @param executionReference reference to the execution
     * @param pipelinePlan the pipeline plan describing the flow
     * @param request details of the source and destination resources
     * @throws IOException if an I/O error occurs (currently never thrown)
     */
    public void processResourceProcessing(final ExecutionReference executionReference,
                                          final PipelinePlan pipelinePlan,
                                          final ResourceTransferRequest request) throws IOException {
        // TODO: Implement actual RESOURCE_PROCESSING logic.
        throw new UnsupportedOperationException("RESOURCE_PROCESSING flow not yet implemented");
    }
}