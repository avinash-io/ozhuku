package io.github.avinashio.ozhuku.application.execution;

import io.github.avinashio.ozhuku.domain.execution.SourceExecution;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.persistence.SourceExecutionRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class SourceExecutionLifecycleService {

    private final SourceExecutionRepository sourceExecutionRepository;
    private final Clock clock;

    public SourceExecutionLifecycleService(
            final SourceExecutionRepository sourceExecutionRepository,
            final Clock clock) {
        this.sourceExecutionRepository = Objects.requireNonNull(
                sourceExecutionRepository,
                "sourceExecutionRepository must not be null");
        this.clock = Objects.requireNonNull(
                clock,
                "clock must not be null");
    }

    public SourceExecution start(
            final ExecutionId executionId,
            final ResourceId resourceId) {

        final SourceExecution sourceExecution =
                findSourceExecution(executionId, resourceId);

        final SourceExecution updated =
                sourceExecution.start(now());

        sourceExecutionRepository.save(updated);
        return updated;
    }

    public SourceExecution complete(
            final ExecutionId executionId,
            final ResourceId resourceId) {

        final SourceExecution sourceExecution =
                findSourceExecution(executionId, resourceId);

        final SourceExecution updated =
                sourceExecution.complete(now());

        sourceExecutionRepository.save(updated);
        return updated;
    }

    public SourceExecution fail(
            final ExecutionId executionId,
            final ResourceId resourceId) {

        final SourceExecution sourceExecution =
                findSourceExecution(executionId, resourceId);

        final SourceExecution updated =
                sourceExecution.fail(now());

        sourceExecutionRepository.save(updated);
        return updated;
    }

    public SourceExecution cancel(
            final ExecutionId executionId,
            final ResourceId resourceId) {

        final SourceExecution sourceExecution =
                findSourceExecution(executionId, resourceId);

        final SourceExecution updated =
                sourceExecution.cancel(now());

        sourceExecutionRepository.save(updated);
        return updated;
    }

    private SourceExecution findSourceExecution(
            final ExecutionId executionId,
            final ResourceId resourceId) {

        Objects.requireNonNull(
                executionId,
                "executionId must not be null");

        Objects.requireNonNull(
                resourceId,
                "resourceId must not be null");

        return sourceExecutionRepository
                .findById(executionId, resourceId)
                .orElseThrow(() -> new IllegalStateException(
                        "Source execution not found: executionId="
                                + executionId
                                + ", resourceId="
                                + resourceId));
    }

    private Instant now() {
        return Instant.now(clock);
    }
}