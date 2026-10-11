
package io.github.avinashio.ozhuku.storage;

import io.github.avinashio.ozhuku.domain.delivery.DeliveryPolicy;
import io.github.avinashio.ozhuku.domain.execution.DestinationCommitReference;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import java.io.IOException;

public interface StorageOutputProvider {

    StorageOutput open(
            Resource destination,
            DeliveryPolicy deliveryPolicy)
            throws IOException;

    default StorageOutput open(
            final Resource destination,
            final DeliveryPolicy deliveryPolicy,
            final DestinationCommitReference reference)
            throws IOException {
        if (reference == null) {
            throw new IllegalArgumentException(
                    "reference must not be null");
        }

        return open(destination, deliveryPolicy);
    }
}
