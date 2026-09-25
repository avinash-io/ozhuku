package io.github.avinashio.ozhuku.application.execution;

import io.github.avinashio.ozhuku.domain.execution.FlowExecution;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.FlowId;
import io.github.avinashio.ozhuku.persistence.FlowExecutionRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class FlowExecutionLifecycleService {

    private final FlowExecutionRepository flowExecutionRepository;
    private final Clock clock;

    public FlowExecutionLifecycleService(
            final FlowExecutionRepository flowExecutionRepository,
            final Clock clock) {
        this.flowExecutionRepository = Objects.requireNonNull(
                flowExecutionRepository,
                "flowExecutionRepository must not be null");
        this.clock = Objects.requireNonNull(
                clock,
                "clock must not be null");
    }

    public FlowExecution start(
            final ExecutionId executionId,
            final FlowId flowId) {

        final FlowExecution flowExecution =
                findFlowExecution(executionId, flowId);

        final FlowExecution updated =
                flowExecution.start(now());

        flowExecutionRepository.save(updated);
        return updated;
    }

    public FlowExecution complete(
            final ExecutionId executionId,
            final FlowId flowId) {

        final FlowExecution flowExecution =
                findFlowExecution(executionId, flowId);

        final FlowExecution updated =
                flowExecution.complete(now());

        flowExecutionRepository.save(updated);
        return updated;
    }

    public FlowExecution fail(
            final ExecutionId executionId,
            final FlowId flowId) {

        final FlowExecution flowExecution =
                findFlowExecution(executionId, flowId);

        final FlowExecution updated =
                flowExecution.fail(now());

        flowExecutionRepository.save(updated);
        return updated;
    }

    public FlowExecution cancel(
            final ExecutionId executionId,
            final FlowId flowId) {

        final FlowExecution flowExecution =
                findFlowExecution(executionId, flowId);

        final FlowExecution updated =
                flowExecution.cancel(now());

        flowExecutionRepository.save(updated);
        return updated;
    }

    private FlowExecution findFlowExecution(
            final ExecutionId executionId,
            final FlowId flowId) {

        Objects.requireNonNull(
                executionId,
                "executionId must not be null");

        Objects.requireNonNull(
                flowId,
                "flowId must not be null");

        return flowExecutionRepository
                .findById(executionId, flowId)
                .orElseThrow(() -> new IllegalStateException(
                        "Flow execution not found: executionId="
                                + executionId
                                + ", flowId="
                                + flowId));
    }

    private Instant now() {
        return Instant.now(clock);
    }
}