package io.github.avinashio.ozhuku.persistence;

import io.github.avinashio.ozhuku.domain.execution.SourceExecution;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import java.util.Optional;

/**
 * Persistence port for durable source execution state.
 */
public interface SourceExecutionRepository {

    /**
     * Finds a source execution by its execution and resource identifiers.
     *
     * @param executionId execution identifier
     * @param resourceId resource identifier
     * @return persisted source execution when present
     */
    Optional<SourceExecution> findById(
            ExecutionId executionId,
            ResourceId resourceId);

    /**
     * Persists a source execution.
     *
     * @param sourceExecution source execution to persist
     */
    void save(SourceExecution sourceExecution);
}