package io.github.avinashio.ozhuku.application.execution;

import io.github.avinashio.ozhuku.domain.execution.Execution;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.persistence.ExecutionRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class ExecutionLifecycleService {

    private final ExecutionRepository executionRepository;
    private final Clock clock;

    public ExecutionLifecycleService(
            final ExecutionRepository executionRepository,
            final Clock clock) {
        this.executionRepository = Objects.requireNonNull(
                executionRepository,
                "executionRepository must not be null");
        this.clock = Objects.requireNonNull(
                clock,
                "clock must not be null");
    }

    public Execution start(final ExecutionId executionId) {
        final Execution execution = findExecution(executionId);
        final Execution updated = execution.start(now());
        executionRepository.save(updated);
        return updated;
    }

    public Execution complete(final ExecutionId executionId) {
        final Execution execution = findExecution(executionId);
        final Execution updated = execution.complete(now());
        executionRepository.save(updated);
        return updated;
    }

    public Execution fail(final ExecutionId executionId) {
        final Execution execution = findExecution(executionId);
        final Execution updated = execution.fail(now());
        executionRepository.save(updated);
        return updated;
    }

    public Execution cancel(final ExecutionId executionId) {
        final Execution execution = findExecution(executionId);
        final Execution updated = execution.cancel(now());
        executionRepository.save(updated);
        return updated;
    }

    private Execution findExecution(final ExecutionId executionId) {
        Objects.requireNonNull(
                executionId,
                "executionId must not be null");

        return executionRepository.findById(executionId)
                .orElseThrow(() -> new IllegalStateException(
                        "Execution not found: " + executionId));
    }

    private Instant now() {
        return Instant.now(clock);
    }
}
