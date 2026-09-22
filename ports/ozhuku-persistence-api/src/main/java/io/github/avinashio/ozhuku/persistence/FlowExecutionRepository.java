package io.github.avinashio.ozhuku.persistence;

import io.github.avinashio.ozhuku.domain.execution.FlowExecution;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.FlowId;
import java.util.Optional;

/**
 * Persistence port for durable flow execution state.
 */
public interface FlowExecutionRepository {

    /**
     * Finds a flow execution by its execution and flow identifiers.
     *
     * @param executionId execution identifier
     * @param flowId flow identifier
     * @return persisted flow execution when present
     */
    Optional<FlowExecution> findById(
            ExecutionId executionId,
            FlowId flowId);

    /**
     * Persists a flow execution.
     *
     * @param flowExecution flow execution to persist
     */
    void save(FlowExecution flowExecution);
}