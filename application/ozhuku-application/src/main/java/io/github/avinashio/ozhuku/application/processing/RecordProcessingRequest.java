package io.github.avinashio.ozhuku.application.processing;

import io.github.avinashio.ozhuku.domain.delivery.DeliveryPolicy;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.format.FormatReader;
import io.github.avinashio.ozhuku.format.FormatWriter;
import java.util.Objects;

public record RecordProcessingRequest(
        Resource source,
        Resource destination,
        DeliveryPolicy deliveryPolicy,
        FormatReader formatReader,
        FormatWriter formatWriter) {

    public RecordProcessingRequest {
        Objects.requireNonNull(
                source,
                "source must not be null");

        Objects.requireNonNull(
                destination,
                "destination must not be null");

        Objects.requireNonNull(
                deliveryPolicy,
                "deliveryPolicy must not be null");

        Objects.requireNonNull(
                formatReader,
                "formatReader must not be null");

        Objects.requireNonNull(
                formatWriter,
                "formatWriter must not be null");
    }
}