package io.github.avinashio.ozhuku.integration;

import io.github.avinashio.ozhuku.application.transfer.ResourceTransferService;
import io.github.avinashio.ozhuku.domain.delivery.ConflictBehavior;
import io.github.avinashio.ozhuku.domain.delivery.DeliveryPolicy;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.domain.resource.ResourceLocation;
import io.github.avinashio.ozhuku.storage.file.FileStoragePathResolver;
import io.github.avinashio.ozhuku.storage.file.FileStorageReader;
import io.github.avinashio.ozhuku.storage.file.FileStorageWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalFileResourceTransferIT {

    @TempDir
    Path temporaryDirectory;

    @Test
    void shouldTransferFileBetweenLocalLocations()
            throws IOException {

        final Path storageRoot =
                temporaryDirectory.resolve("storage");

        Files.createDirectories(storageRoot);

        final Path sourcePath =
                storageRoot
                        .resolve("input")
                        .resolve("source.txt");

        final Path destinationPath =
                storageRoot
                        .resolve("output")
                        .resolve("destination.txt");

        Files.createDirectories(sourcePath.getParent());
        Files.createDirectories(destinationPath.getParent());

        final byte[] expectedContent =
                "Ozhuku resource transfer integration test"
                        .getBytes(StandardCharsets.UTF_8);

        Files.write(sourcePath, expectedContent);

        final FileStoragePathResolver pathResolver =
                new FileStoragePathResolver(storageRoot);

        final FileStorageReader reader =
                new FileStorageReader(pathResolver);

        final FileStorageWriter writer =
                new FileStorageWriter(pathResolver);

        final ResourceTransferService transferService =
                new ResourceTransferService(
                        reader,
                        writer);

        final Resource source =
                new Resource(
                        new ResourceId("source-1"),
                        new ResourceLocation(
                                sourcePath.toUri().toString()));

        final Resource destination =
                new Resource(
                        new ResourceId("destination-1"),
                        new ResourceLocation(
                                destinationPath.toUri().toString()));

        transferService.transfer(
                source,
                destination,
                replacePolicy());

        assertTrue(Files.exists(destinationPath));

        assertArrayEquals(
                expectedContent,
                Files.readAllBytes(destinationPath));
    }

    @Test
    void shouldRejectSourceOutsideConfiguredRoot()
            throws IOException {

        final Path storageRoot =
                temporaryDirectory.resolve("storage");

        Files.createDirectories(storageRoot);

        final Path outsideFile =
                temporaryDirectory.resolve("outside.txt");

        Files.write(
                outsideFile,
                "sensitive-content".getBytes(StandardCharsets.UTF_8));

        final Path destinationPath =
                storageRoot
                        .resolve("output")
                        .resolve("destination.txt");

        Files.createDirectories(destinationPath.getParent());

        final FileStoragePathResolver pathResolver =
                new FileStoragePathResolver(storageRoot);

        final ResourceTransferService transferService =
                new ResourceTransferService(
                        new FileStorageReader(pathResolver),
                        new FileStorageWriter(pathResolver));

        final Resource source =
                new Resource(
                        new ResourceId("outside-source"),
                        new ResourceLocation(
                                outsideFile.toUri().toString()));

        final Resource destination =
                new Resource(
                        new ResourceId("destination-1"),
                        new ResourceLocation(
                                destinationPath.toUri().toString()));

        assertThrows(
                IllegalArgumentException.class,
                () -> transferService.transfer(
                        source,
                        destination,
                        replacePolicy()));

        assertTrue(
                Files.notExists(destinationPath));
    }

    @Test
    void shouldRejectDestinationOutsideConfiguredRoot()
            throws IOException {

        final Path storageRoot =
                temporaryDirectory.resolve("storage");

        Files.createDirectories(storageRoot);

        final Path sourcePath =
                storageRoot
                        .resolve("input")
                        .resolve("source.txt");

        Files.createDirectories(sourcePath.getParent());

        Files.write(
                sourcePath,
                "source-content".getBytes(StandardCharsets.UTF_8));

        final Path outsideDestination =
                temporaryDirectory.resolve("outside-destination.txt");

        final FileStoragePathResolver pathResolver =
                new FileStoragePathResolver(storageRoot);

        final ResourceTransferService transferService =
                new ResourceTransferService(
                        new FileStorageReader(pathResolver),
                        new FileStorageWriter(pathResolver));

        final Resource source =
                new Resource(
                        new ResourceId("source-1"),
                        new ResourceLocation(
                                sourcePath.toUri().toString()));

        final Resource destination =
                new Resource(
                        new ResourceId("outside-destination"),
                        new ResourceLocation(
                                outsideDestination.toUri().toString()));

        assertThrows(
                IllegalArgumentException.class,
                () -> transferService.transfer(
                        source,
                        destination,
                        replacePolicy()));

        assertTrue(
                Files.notExists(outsideDestination));
    }

    @Test
    void shouldFailWhenSourceDoesNotExist()
            throws IOException {

        final Path storageRoot =
                temporaryDirectory.resolve("storage");

        Files.createDirectories(storageRoot);

        final Path missingSource =
                storageRoot
                        .resolve("input")
                        .resolve("missing.txt");

        final Path destinationPath =
                storageRoot
                        .resolve("output")
                        .resolve("destination.txt");

        Files.createDirectories(destinationPath.getParent());

        final FileStoragePathResolver pathResolver =
                new FileStoragePathResolver(storageRoot);

        final ResourceTransferService transferService =
                new ResourceTransferService(
                        new FileStorageReader(pathResolver),
                        new FileStorageWriter(pathResolver));

        final Resource source =
                new Resource(
                        new ResourceId("missing-source"),
                        new ResourceLocation(
                                missingSource.toUri().toString()));

        final Resource destination =
                new Resource(
                        new ResourceId("destination-1"),
                        new ResourceLocation(
                                destinationPath.toUri().toString()));

        assertThrows(
                IOException.class,
                () -> transferService.transfer(
                        source,
                        destination,
                        replacePolicy()));

        assertTrue(
                Files.notExists(destinationPath));
    }

    private static DeliveryPolicy replacePolicy() {
        return new DeliveryPolicy(
                ConflictBehavior.REPLACE);
    }
}