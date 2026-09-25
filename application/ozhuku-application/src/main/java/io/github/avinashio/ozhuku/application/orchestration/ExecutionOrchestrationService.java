package io.github.avinashio.ozhuku.application.orchestration;

import io.github.avinashio.ozhuku.application.execution.ExecutionLifecycleService;
import io.github.avinashio.ozhuku.application.execution.FlowExecutionLifecycleService;
import io.github.avinashio.ozhuku.domain.execution.FlowExecution;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.FlowId;
import java.util.Objects;

public final class ExecutionOrchestrationService {

    private final ExecutionLifecycleService executionLifecycleService;
    private final FlowExecutionLifecycleService flowExecutionLifecycleService;

    public ExecutionOrchestrationService(
            final ExecutionLifecycleService executionLifecycleService,
            final FlowExecutionLifecycleService flowExecutionLifecycleService) {
        this.executionLifecycleService = Objects.requireNonNull(
                executionLifecycleService,
                "executionLifecycleService must not be null");
        this.flowExecutionLifecycleService = Objects.requireNonNull(
                flowExecutionLifecycleService,
                "flowExecutionLifecycleService must not be null");
    }

    public FlowExecution startExecution(
            final ExecutionId executionId,
            final FlowId flowId) {

        Objects.requireNonNull(
                executionId,
                "executionId must not be null");
        Objects.requireNonNull(
                flowId,
                "flowId must not be null");

        executionLifecycleService.start(executionId);

        try {
            return flowExecutionLifecycleService.start(
                    executionId,
                    flowId);
        } catch (RuntimeException exception) {
            executionLifecycleService.fail(executionId);
            throw exception;
        }
    }
}