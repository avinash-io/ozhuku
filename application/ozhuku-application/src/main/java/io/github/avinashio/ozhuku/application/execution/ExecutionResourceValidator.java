package io.github.avinashio.ozhuku.application.execution;

import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.persistence.DestinationExecutionRepository;
import io.github.avinashio.ozhuku.persistence.SourceExecutionRepository;
import java.util.Objects;

public final class ExecutionResourceValidator {

    private final SourceExecutionRepository sourceExecutionRepository;
    private final DestinationExecutionRepository
            destinationExecutionRepository;

    public ExecutionResourceValidator(
            final SourceExecutionRepository sourceExecutionRepository,
            final DestinationExecutionRepository
                    destinationExecutionRepository) {

        this.sourceExecutionRepository =
                Objects.requireNonNull(
                        sourceExecutionRepository,
                        "sourceExecutionRepository must not be null");

        this.destinationExecutionRepository =
                Objects.requireNonNull(
                        destinationExecutionRepository,
                        "destinationExecutionRepository must not be null");
    }

    public void validate(
            final ExecutionId executionId,
            final ResourceId sourceResourceId,
            final ResourceId destinationResourceId) {

        Objects.requireNonNull(
                executionId,
                "executionId must not be null");

        Objects.requireNonNull(
                sourceResourceId,
                "sourceResourceId must not be null");

        Objects.requireNonNull(
                destinationResourceId,
                "destinationResourceId must not be null");

        sourceExecutionRepository.findById(
                        executionId,
                        sourceResourceId)
                .orElseThrow(() -> new IllegalStateException(
                        "Source execution not found for execution "
                                + executionId
                                + " and resource "
                                + sourceResourceId));

        destinationExecutionRepository.findById(
                        executionId,
                        destinationResourceId)
                .orElseThrow(() -> new IllegalStateException(
                        "Destination execution not found for execution "
                                + executionId
                                + " and resource "
                                + destinationResourceId));
    }
}