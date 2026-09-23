package io.github.avinashio.ozhuku.application.delivery;

import io.github.avinashio.ozhuku.domain.execution.DestinationExecutionReference;
import io.github.avinashio.ozhuku.domain.resource.Resource;

public interface DestinationDeliveryPort {

    void deliver(
            DestinationExecutionReference reference,
            Resource sourceResource);
}