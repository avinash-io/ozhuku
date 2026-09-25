package io.github.avinashio.ozhuku.application.processing;

import io.github.avinashio.ozhuku.domain.delivery.DeliveryPolicy;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import java.util.Objects;

public record ResourceTransferRequest(
        Resource source,
        Resource destination,
        DeliveryPolicy deliveryPolicy) {

    public ResourceTransferRequest {
        Objects.requireNonNull(
                source,
                "source must not be null");

        Objects.requireNonNull(
                destination,
                "destination must not be null");

        Objects.requireNonNull(
                deliveryPolicy,
                "deliveryPolicy must not be null");
    }
}