package io.github.avinashio.ozhuku.persistence;

import io.github.avinashio.ozhuku.domain.deduplication.ProcessingRecord;
import io.github.avinashio.ozhuku.domain.identity.ProcessingIdentity;
import java.util.Optional;

/**
 * Persistence port for durable processing history.
 *
 * <p>The port exposes domain concepts only and remains independent of the
 * underlying persistence technology.</p>
 */
public interface ProcessingRecordRepository {

    /**
     * Finds processing history for the supplied processing identity.
     *
     * @param identity processing identity
     * @return processing record when history exists
     */
    Optional<ProcessingRecord> findByIdentity(
            ProcessingIdentity identity);

    /**
     * Persists processing history.
     *
     * <p>Implementations must provide durable semantics appropriate for
     * recovery and duplicate detection.</p>
     *
     * @param record processing record
     */
    void save(ProcessingRecord record);
}