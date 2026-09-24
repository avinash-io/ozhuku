package io.github.avinashio.ozhuku.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.avinashio.ozhuku.application.record.RecordProcessingService;
import io.github.avinashio.ozhuku.domain.delivery.ConflictBehavior;
import io.github.avinashio.ozhuku.domain.delivery.DeliveryPolicy;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.domain.resource.ResourceLocation;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.format.csv.CsvFormatReader;
import io.github.avinashio.ozhuku.format.csv.CsvFormatWriter;
import io.github.avinashio.ozhuku.format.csv.CsvReaderConfiguration;
import io.github.avinashio.ozhuku.format.csv.CsvWriterConfiguration;
import io.github.avinashio.ozhuku.storage.file.FileStorageOutputProvider;
import io.github.avinashio.ozhuku.storage.file.FileStoragePathResolver;
import io.github.avinashio.ozhuku.storage.file.FileStorageReader;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CsvRecordProcessingIT {

    @TempDir
    Path temporaryDirectory;

    @Test
    void shouldProcessCsvFromLocalSourceToLocalDestination()
            throws Exception {

        final Path sourcePath = temporaryDirectory.resolve("source.csv");
        final Path destinationPath =
                temporaryDirectory.resolve("destination.csv");

        Files.writeString(
                sourcePath,
                "name,age\nAlice,30\nBob,40\n");

        final Resource source = resource(sourcePath);
        final Resource destination = resource(destinationPath);

        final FileStoragePathResolver pathResolver =
                new FileStoragePathResolver(temporaryDirectory);

        final RecordProcessingService service =
                new RecordProcessingService(
                        new FileStorageReader(pathResolver),
                        new FileStorageOutputProvider(pathResolver));

        final CsvFormatReader reader =
                new CsvFormatReader(
                        CsvReaderConfiguration.builder()
                                .headerPresent(true)
                                .build());

        final CsvFormatWriter writer =
                new CsvFormatWriter(
                        CsvWriterConfiguration.builder()
                                .writeHeader(true)
                                .build());

        service.process(
                source,
                destination,
                new DeliveryPolicy(ConflictBehavior.REPLACE),
                reader,
                writer);

        assertEquals(
                "name,age\r\nAlice,30\r\nBob,40\r\n",
                Files.readString(destinationPath));
    }

    private Resource resource(final Path path) {
        return new Resource(
                new ResourceId(path.getFileName().toString()),
                new ResourceLocation(path.toUri().toString()));
    }
}