package io.github.avinashio.ozhuku.storage;

import io.github.avinashio.ozhuku.domain.delivery.DeliveryPolicy;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import java.io.IOException;

public interface StorageOutputProvider {

    StorageOutput open(
            Resource destination,
            DeliveryPolicy deliveryPolicy)
            throws IOException;
}