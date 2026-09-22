package io.github.avinashio.ozhuku.persistence;

import io.github.avinashio.ozhuku.domain.execution.DestinationExecution;
import io.github.avinashio.ozhuku.domain.identity.ExecutionId;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import java.util.Optional;

public interface DestinationExecutionRepository {

    Optional<DestinationExecution> findById(
            ExecutionId executionId,
            ResourceId resourceId);

    void save(DestinationExecution destinationExecution);
}