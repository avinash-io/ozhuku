package io.github.avinashio.ozhuku.application.initialization;

import io.github.avinashio.ozhuku.domain.execution.DestinationExecution;
import io.github.avinashio.ozhuku.domain.execution.DestinationExecutionReference;
import io.github.avinashio.ozhuku.domain.execution.Execution;
import io.github.avinashio.ozhuku.domain.execution.ExecutionReference;
import io.github.avinashio.ozhuku.domain.execution.FlowExecution;
import io.github.avinashio.ozhuku.domain.execution.SourceExecution;
import io.github.avinashio.ozhuku.domain.execution.SourceExecutionReference;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.FlowId;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.persistence.DestinationExecutionRepository;
import io.github.avinashio.ozhuku.persistence.ExecutionRepository;
import io.github.avinashio.ozhuku.persistence.FlowExecutionRepository;
import io.github.avinashio.ozhuku.persistence.SourceExecutionRepository;
import java.util.Objects;

public final class ExecutionInitializationService {

    private final ExecutionRepository executionRepository;
    private final FlowExecutionRepository flowExecutionRepository;
    private final SourceExecutionRepository sourceExecutionRepository;
    private final DestinationExecutionRepository destinationExecutionRepository;

    public ExecutionInitializationService(
            final ExecutionRepository executionRepository,
            final FlowExecutionRepository flowExecutionRepository,
            final SourceExecutionRepository sourceExecutionRepository,
            final DestinationExecutionRepository destinationExecutionRepository) {

        this.executionRepository = Objects.requireNonNull(
                executionRepository,
                "executionRepository must not be null");

        this.flowExecutionRepository = Objects.requireNonNull(
                flowExecutionRepository,
                "flowExecutionRepository must not be null");

        this.sourceExecutionRepository = Objects.requireNonNull(
                sourceExecutionRepository,
                "sourceExecutionRepository must not be null");

        this.destinationExecutionRepository = Objects.requireNonNull(
                destinationExecutionRepository,
                "destinationExecutionRepository must not be null");
    }

    public void initialize(
            final ExecutionReference executionReference,
            final FlowId flowId,
            final ResourceId sourceResourceId,
            final ResourceId destinationResourceId) {

        Objects.requireNonNull(
                executionReference,
                "executionReference must not be null");

        Objects.requireNonNull(
                flowId,
                "flowId must not be null");

        Objects.requireNonNull(
                sourceResourceId,
                "sourceResourceId must not be null");

        Objects.requireNonNull(
                destinationResourceId,
                "destinationResourceId must not be null");

        final ExecutionId executionId =
                executionReference.executionId();

        final SourceExecutionReference sourceExecutionReference =
                new SourceExecutionReference(
                        executionId,
                        sourceResourceId);

        final DestinationExecutionReference destinationExecutionReference =
                new DestinationExecutionReference(
                        executionId,
                        destinationResourceId);

        validateDoesNotExist(
                executionId,
                flowId,
                sourceExecutionReference,
                destinationExecutionReference);

        final Execution execution =
                new Execution(executionReference);

        final FlowExecution flowExecution =
                new FlowExecution(
                        executionId,
                        flowId);

        final SourceExecution sourceExecution =
                new SourceExecution(
                        sourceExecutionReference);

        final DestinationExecution destinationExecution =
                new DestinationExecution(
                        destinationExecutionReference);

        executionRepository.save(execution);
        flowExecutionRepository.save(flowExecution);
        sourceExecutionRepository.save(sourceExecution);
        destinationExecutionRepository.save(destinationExecution);
    }

    private void validateDoesNotExist(
            final ExecutionId executionId,
            final FlowId flowId,
            final SourceExecutionReference sourceExecutionReference,
            final DestinationExecutionReference destinationExecutionReference) {

        if (executionRepository.findById(executionId).isPresent()) {
            throw new IllegalStateException(
                    "Execution already exists: " + executionId);
        }

        if (flowExecutionRepository
                .findById(executionId, flowId)
                .isPresent()) {

            throw new IllegalStateException(
                    "Flow execution already exists: executionId="
                            + executionId
                            + ", flowId="
                            + flowId);
        }

        if (sourceExecutionRepository
                .findById(
                        sourceExecutionReference.executionId(),
                        sourceExecutionReference.resourceId())
                .isPresent()) {

            throw new IllegalStateException(
                    "Source execution already exists: executionId="
                            + sourceExecutionReference.executionId()
                            + ", resourceId="
                            + sourceExecutionReference.resourceId());
        }

        if (destinationExecutionRepository
                .findById(
                        destinationExecutionReference.executionId(),
                        destinationExecutionReference.resourceId())
                .isPresent()) {

            throw new IllegalStateException(
                    "Destination execution already exists: executionId="
                            + destinationExecutionReference.executionId()
                            + ", resourceId="
                            + destinationExecutionReference.resourceId());
        }
    }
}