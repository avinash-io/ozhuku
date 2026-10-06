package io.github.avinashio.ozhuku.persistence;

import io.github.avinashio.ozhuku.domain.checkpoint.ProcessingCheckpoint;
import io.github.avinashio.ozhuku.domain.execution.SourceExecutionReference;
import java.util.Optional;

/**
 * Persistence port for durable processing checkpoints.
 *
 * <p>The port exposes domain concepts only and remains independent of the
 * underlying persistence technology.</p>
 */
public interface ProcessingCheckpointRepository {

    /**
     * Finds the checkpoint for the supplied source execution.
     *
     * @param reference source execution reference
     * @return checkpoint when one exists
     */
    Optional<ProcessingCheckpoint> findBySourceExecution(
            SourceExecutionReference reference);

    /**
     * Persists the supplied processing checkpoint.
     *
     * <p>Implementations must provide durable semantics appropriate for
     * processing recovery.</p>
     *
     * @param checkpoint processing checkpoint
     */
    void save(ProcessingCheckpoint checkpoint);
}
