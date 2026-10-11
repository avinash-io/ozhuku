
package io.github.avinashio.ozhuku.storage;

import io.github.avinashio.ozhuku.domain.execution.CommitStatus;
import io.github.avinashio.ozhuku.domain.execution.DestinationCommitReference;
import java.io.IOException;

/**
 * Avinash: Defines the storage-level contract for destination write evidence.
 */
public interface DestinationWriteEvidenceStore {

    /**
     * Avinash: Records positive evidence after destination publication succeeds.
     *
     * @param reference identity of the destination execution
     * @throws IOException if evidence cannot be recorded
     */
    void recordCommitted(
            DestinationCommitReference reference)
            throws IOException;

    /**
     * Avinash: Inspects independent evidence for a destination execution.
     *
     * @param reference identity of the destination execution
     * @return the established outcome, or UNKNOWN when it cannot be established
     * @throws IOException if evidence inspection fails
     */
    CommitStatus inspect(
            DestinationCommitReference reference)
            throws IOException;
}
