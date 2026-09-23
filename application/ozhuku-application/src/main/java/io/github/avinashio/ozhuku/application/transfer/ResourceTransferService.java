package io.github.avinashio.ozhuku.application.transfer;

import io.github.avinashio.ozhuku.domain.delivery.DeliveryPolicy;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.storage.StorageReader;
import io.github.avinashio.ozhuku.storage.StorageWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public final class ResourceTransferService {

    private final StorageReader storageReader;
    private final StorageWriter storageWriter;

    public ResourceTransferService(
            final StorageReader storageReader,
            final StorageWriter storageWriter) {

        this.storageReader = Objects.requireNonNull(
                storageReader,
                "storageReader must not be null");

        this.storageWriter = Objects.requireNonNull(
                storageWriter,
                "storageWriter must not be null");
    }

    public void transfer(
            final Resource source,
            final Resource destination,
            final DeliveryPolicy deliveryPolicy)
            throws IOException {

        Objects.requireNonNull(
                source,
                "source must not be null");

        Objects.requireNonNull(
                destination,
                "destination must not be null");

        Objects.requireNonNull(
                deliveryPolicy,
                "deliveryPolicy must not be null");

        try (InputStream input =
                     storageReader.open(source)) {

            storageWriter.write(
                    destination,
                    input,
                    deliveryPolicy.conflictBehavior());
        }
    }
}