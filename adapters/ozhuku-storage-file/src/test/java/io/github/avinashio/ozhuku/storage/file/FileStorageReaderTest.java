package io.github.avinashio.ozhuku.storage.file;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.domain.resource.ResourceLocation;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.AccessDeniedException;


class FileStorageReaderTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void shouldOpenExistingFileAsStream() throws IOException {
        final Path root =
                temporaryDirectory.resolve("storage");

        Files.createDirectories(root);

        final Path file =
                root.resolve("input.txt");

        final byte[] expected =
                "ozhuku-reader-test".getBytes();

        Files.write(file, expected);

        final FileStorageReader reader =
                new FileStorageReader(
                        new FileStoragePathResolver(root));

        final Resource resource =
                new Resource(
                        new ResourceId("resource-1"),
                        new ResourceLocation(
                                file.toUri().toString()));

        try (InputStream input =
                     reader.open(resource)) {

            final byte[] actual =
                    input.readAllBytes();

            assertArrayEquals(
                    expected,
                    actual);
        }
    }

    @Test
    void shouldFailWhenFileDoesNotExist() {
        final Path root =
                temporaryDirectory.resolve("storage");

        final FileStorageReader reader =
                new FileStorageReader(
                        new FileStoragePathResolver(root));

        final Path missingFile =
                root.resolve("missing.txt");

        final Resource resource =
                new Resource(
                        new ResourceId("resource-1"),
                        new ResourceLocation(
                                missingFile.toUri().toString()));

        assertThrows(
                IOException.class,
                () -> reader.open(resource));
    }

    @Test
    void shouldRejectNullResource() {
        final FileStorageReader reader =
                new FileStorageReader(
                        new FileStoragePathResolver(
                                temporaryDirectory));

        assertThrows(
                NullPointerException.class,
                () -> reader.open(null));
    }

    @Test
    void shouldRejectFileOutsideRoot() {
        final Path root =
                temporaryDirectory.resolve("storage");

        final FileStorageReader reader =
                new FileStorageReader(
                        new FileStoragePathResolver(root));

        final Path outside =
                temporaryDirectory.resolve("outside.txt");

        final Resource resource =
                new Resource(
                        new ResourceId("resource-1"),
                        new ResourceLocation(
                                outside.toUri().toString()));

        assertThrows(
                IllegalArgumentException.class,
                () -> reader.open(resource));
    }

    @Test
    void shouldRejectSymbolicLinkOutsideRoot()
            throws IOException {

        final Path root =
                temporaryDirectory.resolve("storage");

        Files.createDirectories(root);

        final Path outside =
                temporaryDirectory.resolve("outside.txt");

        Files.write(
                outside,
                "outside-content".getBytes());

        final Path link =
                root.resolve("outside-link.txt");

        try {
            Files.createSymbolicLink(
                    link,
                    outside);
        } catch (final UnsupportedOperationException
                       | SecurityException
                       | AccessDeniedException exception) {
            return;
        }

        final FileStorageReader reader =
                new FileStorageReader(
                        new FileStoragePathResolver(root));

        final Resource resource =
                new Resource(
                        new ResourceId("symlink-resource"),
                        new ResourceLocation(
                                link.toUri().toString()));

        assertThrows(
                IllegalArgumentException.class,
                () -> reader.open(resource));
    }
}