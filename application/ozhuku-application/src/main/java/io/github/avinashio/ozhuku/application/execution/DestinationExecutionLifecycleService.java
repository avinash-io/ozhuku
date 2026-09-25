package io.github.avinashio.ozhuku.application.execution;

import io.github.avinashio.ozhuku.domain.execution.DestinationExecution;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.persistence.DestinationExecutionRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class DestinationExecutionLifecycleService {

    private final DestinationExecutionRepository destinationExecutionRepository;
    private final Clock clock;

    public DestinationExecutionLifecycleService(
            final DestinationExecutionRepository destinationExecutionRepository,
            final Clock clock) {
        this.destinationExecutionRepository = Objects.requireNonNull(
                destinationExecutionRepository,
                "destinationExecutionRepository must not be null");
        this.clock = Objects.requireNonNull(
                clock,
                "clock must not be null");
    }

    public DestinationExecution start(
            final ExecutionId executionId,
            final ResourceId resourceId) {

        final DestinationExecution destinationExecution =
                findDestinationExecution(executionId, resourceId);

        final DestinationExecution updated =
                destinationExecution.start(now());

        destinationExecutionRepository.save(updated);
        return updated;
    }

    public DestinationExecution complete(
            final ExecutionId executionId,
            final ResourceId resourceId) {

        final DestinationExecution destinationExecution =
                findDestinationExecution(executionId, resourceId);

        final DestinationExecution updated =
                destinationExecution.complete(now());

        destinationExecutionRepository.save(updated);
        return updated;
    }

    public DestinationExecution fail(
            final ExecutionId executionId,
            final ResourceId resourceId) {

        final DestinationExecution destinationExecution =
                findDestinationExecution(executionId, resourceId);

        final DestinationExecution updated =
                destinationExecution.fail(now());

        destinationExecutionRepository.save(updated);
        return updated;
    }

    public DestinationExecution cancel(
            final ExecutionId executionId,
            final ResourceId resourceId) {

        final DestinationExecution destinationExecution =
                findDestinationExecution(executionId, resourceId);

        final DestinationExecution updated =
                destinationExecution.cancel(now());

        destinationExecutionRepository.save(updated);
        return updated;
    }

    private DestinationExecution findDestinationExecution(
            final ExecutionId executionId,
            final ResourceId resourceId) {

        Objects.requireNonNull(
                executionId,
                "executionId must not be null");

        Objects.requireNonNull(
                resourceId,
                "resourceId must not be null");

        return destinationExecutionRepository
                .findById(executionId, resourceId)
                .orElseThrow(() -> new IllegalStateException(
                        "Destination execution not found: executionId="
                                + executionId
                                + ", resourceId="
                                + resourceId));
    }

    private Instant now() {
        return Instant.now(clock);
    }
}