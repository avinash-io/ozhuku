package io.github.avinashio.ozhuku.persistence;

import io.github.avinashio.ozhuku.domain.execution.Execution;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import java.util.Optional;

/**
 * Persistence port for durable execution state.
 */
public interface ExecutionRepository {

    /**
     * Finds an execution by its identifier.
     *
     * @param executionId execution identifier
     * @return persisted execution when present
     */
    Optional<Execution> findById(ExecutionId executionId);

    /**
     * Persists an execution.
     *
     * @param execution execution to persist
     */
    void save(Execution execution);
}