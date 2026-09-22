package io.github.avinashio.ozhuku.persistence;

import io.github.avinashio.ozhuku.domain.execution.DestinationCommit;
import io.github.avinashio.ozhuku.domain.execution.DestinationCommitReference;
import java.util.Optional;

public interface DestinationCommitRepository {

    Optional<DestinationCommit> findById(
            DestinationCommitReference reference);

    void save(DestinationCommit destinationCommit);
}