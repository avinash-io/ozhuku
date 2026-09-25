package io.github.avinashio.ozhuku.application.record;

import io.github.avinashio.ozhuku.domain.delivery.DeliveryPolicy;
import io.github.avinashio.ozhuku.domain.record.Record;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.format.FormatReadResult;
import io.github.avinashio.ozhuku.format.FormatReader;
import io.github.avinashio.ozhuku.format.FormatWriter;
import io.github.avinashio.ozhuku.storage.StorageOutput;
import io.github.avinashio.ozhuku.storage.StorageOutputProvider;
import io.github.avinashio.ozhuku.storage.StorageReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public final class RecordProcessingService {

    private final StorageReader storageReader;
    private final StorageOutputProvider storageOutputProvider;

    public RecordProcessingService(
            final StorageReader storageReader,
            final StorageOutputProvider storageOutputProvider) {
        this.storageReader = Objects.requireNonNull(
                storageReader,
                "storageReader must not be null");
        this.storageOutputProvider = Objects.requireNonNull(
                storageOutputProvider,
                "storageOutputProvider must not be null");
    }

    public void process(
            final Resource source,
            final Resource destination,
            final DeliveryPolicy deliveryPolicy,
            final FormatReader formatReader,
            final FormatWriter formatWriter)
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
        Objects.requireNonNull(
                formatReader,
                "formatReader must not be null");
        Objects.requireNonNull(
                formatWriter,
                "formatWriter must not be null");

        try (InputStream input = storageReader.open(source);
             StorageOutput output =
                     storageOutputProvider.open(
                             destination,
                             deliveryPolicy)) {

            formatReader.open(input);
            formatWriter.open(output.stream());

            try {
                readAndWrite(formatReader, formatWriter);
            } finally {
                formatWriter.close();
                formatReader.close();
            }

            output.commit();
        }
    }

    private void readAndWrite(
            final FormatReader formatReader,
            final FormatWriter formatWriter)
            throws IOException {
        while (true) {
            final FormatReadResult result = formatReader.read();

            switch (result.status()) {
                case RECORD:
                    final Record record = result.record();
                    formatWriter.write(record);
                    break;

                case END_OF_INPUT:
                    return;

                case ERROR:
                    throw new IOException(
                            "Format reader reported an error",
                            result.error());

                default:
                    throw new IllegalStateException(
                            "Unsupported format read status: "
                                    + result.status());
            }
        }
    }
}
