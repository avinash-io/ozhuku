package io.github.avinashio.ozhuku.storage.file;

import io.github.avinashio.ozhuku.domain.delivery.ConflictBehavior;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.domain.resource.ResourceLocation;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileStorageWriterTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void shouldWriteResourceContent() throws IOException {
        final Path root =
                temporaryDirectory.resolve("storage");

        Files.createDirectories(root);

        final Path destination =
                root.resolve("output.txt");

        final FileStorageWriter writer =
                new FileStorageWriter(
                        new FileStoragePathResolver(root));

        final Resource resource =
                new Resource(
                        new ResourceId("destination-1"),
                        new ResourceLocation(
                                destination.toUri().toString()));

        final byte[] expected =
                "ozhuku-writer-test".getBytes();

        try (InputStream content =
                     new ByteArrayInputStream(expected)) {

            writer.write(
                    resource,
                    content,
                    ConflictBehavior.REPLACE);
        }

        assertArrayEquals(
                expected,
                Files.readAllBytes(destination));
    }

    @Test
    void shouldReplaceExistingFile() throws IOException {
        final Path root =
                temporaryDirectory.resolve("storage");

        Files.createDirectories(root);

        final Path destination =
                root.resolve("output.txt");

        Files.write(
                destination,
                "old-content".getBytes());

        final FileStorageWriter writer =
                new FileStorageWriter(
                        new FileStoragePathResolver(root));

        final Resource resource =
                new Resource(
                        new ResourceId("destination-1"),
                        new ResourceLocation(
                                destination.toUri().toString()));

        final byte[] replacement =
                "new-content".getBytes();

        try (InputStream content =
                     new ByteArrayInputStream(replacement)) {

            writer.write(
                    resource,
                    content,
                    ConflictBehavior.REPLACE);
        }

        assertArrayEquals(
                replacement,
                Files.readAllBytes(destination));
    }

    @Test
    void shouldFailWhenParentDirectoryDoesNotExist()
            throws IOException {

        final Path root =
                temporaryDirectory.resolve("storage");

        Files.createDirectories(root);

        final FileStorageWriter writer =
                new FileStorageWriter(
                        new FileStoragePathResolver(root));

        final Path destination =
                root.resolve("missing")
                        .resolve("output.txt");

        final Resource resource =
                new Resource(
                        new ResourceId("destination-1"),
                        new ResourceLocation(
                                destination.toUri().toString()));

        try (InputStream content =
                     new ByteArrayInputStream(
                             "content".getBytes())) {

            assertThrows(
                    IOException.class,
                    () -> writer.write(
                            resource,
                            content,
                            ConflictBehavior.REPLACE));
        }
    }

    @Test
    void shouldRejectNullDestination() {
        final FileStorageWriter writer =
                new FileStorageWriter(
                        new FileStoragePathResolver(
                                temporaryDirectory));

        final InputStream content =
                new ByteArrayInputStream(
                        "content".getBytes());

        assertThrows(
                NullPointerException.class,
                () -> writer.write(
                        null,
                        content,
                        ConflictBehavior.REPLACE));
    }

    @Test
    void shouldRejectNullContent() {
        final FileStorageWriter writer =
                new FileStorageWriter(
                        new FileStoragePathResolver(
                                temporaryDirectory));

        final Resource resource =
                new Resource(
                        new ResourceId("destination-1"),
                        new ResourceLocation(
                                temporaryDirectory
                                        .resolve("output.txt")
                                        .toUri()
                                        .toString()));

        assertThrows(
                NullPointerException.class,
                () -> writer.write(
                        resource,
                        null,
                        ConflictBehavior.REPLACE));
    }

    @Test
    void shouldRejectDestinationOutsideRoot() {
        final Path root =
                temporaryDirectory.resolve("storage");

        final FileStorageWriter writer =
                new FileStorageWriter(
                        new FileStoragePathResolver(root));

        final Path outside =
                temporaryDirectory.resolve("outside.txt");

        final Resource resource =
                new Resource(
                        new ResourceId("destination-1"),
                        new ResourceLocation(
                                outside.toUri().toString()));

        final InputStream content =
                new ByteArrayInputStream(
                        "content".getBytes());

        assertThrows(
                IllegalArgumentException.class,
                () -> writer.write(
                        resource,
                        content,
                        ConflictBehavior.REPLACE));
    }

    @Test
    void shouldRejectDestinationThroughSymbolicLinkOutsideRoot()
            throws IOException {

        final Path root =
                temporaryDirectory.resolve("storage");

        Files.createDirectories(root);

        final Path outside =
                temporaryDirectory.resolve("outside");

        Files.createDirectories(outside);

        final Path link =
                root.resolve("output");

        try {
            Files.createSymbolicLink(
                    link,
                    outside);
        } catch (final UnsupportedOperationException
                       | SecurityException
                       | java.nio.file.AccessDeniedException exception) {
            return;
        }

        final FileStorageWriter writer =
                new FileStorageWriter(
                        new FileStoragePathResolver(root));

        final Path destination =
                link.resolve("destination.txt");

        final Resource resource =
                new Resource(
                        new ResourceId("destination-1"),
                        new ResourceLocation(
                                destination.toUri().toString()));

        try (InputStream content =
                     new ByteArrayInputStream(
                             "must-not-write".getBytes())) {

            assertThrows(
                    IllegalArgumentException.class,
                    () -> writer.write(
                            resource,
                            content,
                            ConflictBehavior.REPLACE));
        }

        assertTrue(
                Files.notExists(
                        outside.resolve("destination.txt")));
    }

    @Test
    void shouldFailWhenDestinationAlreadyExists()
            throws IOException {

        final Path root =
                temporaryDirectory.resolve("storage");

        Files.createDirectories(root);

        final Path destination =
                root.resolve("output.txt");

        Files.write(
                destination,
                "existing-content".getBytes());

        final FileStorageWriter writer =
                new FileStorageWriter(
                        new FileStoragePathResolver(root));

        final Resource resource =
                new Resource(
                        new ResourceId("destination-1"),
                        new ResourceLocation(
                                destination.toUri().toString()));

        try (InputStream content =
                     new ByteArrayInputStream(
                             "new-content".getBytes())) {

            assertThrows(
                    IOException.class,
                    () -> writer.write(
                            resource,
                            content,
                            ConflictBehavior.FAIL));
        }

        assertArrayEquals(
                "existing-content".getBytes(),
                Files.readAllBytes(destination));
    }

    @Test
    void shouldSkipWhenDestinationAlreadyExists()
            throws IOException {

        final Path root =
                temporaryDirectory.resolve("storage");

        Files.createDirectories(root);

        final Path destination =
                root.resolve("output.txt");

        Files.write(
                destination,
                "existing-content".getBytes());

        final FileStorageWriter writer =
                new FileStorageWriter(
                        new FileStoragePathResolver(root));

        final Resource resource =
                new Resource(
                        new ResourceId("destination-1"),
                        new ResourceLocation(
                                destination.toUri().toString()));

        try (InputStream content =
                     new ByteArrayInputStream(
                             "new-content".getBytes())) {

            writer.write(
                    resource,
                    content,
                    ConflictBehavior.SKIP);
        }

        assertArrayEquals(
                "existing-content".getBytes(),
                Files.readAllBytes(destination));
    }

    @Test
    void shouldRejectVersionConflictBehavior() throws IOException {

        final Path root =
                temporaryDirectory.resolve("storage");

        Files.createDirectories(root);

        final Path destination =
                root.resolve("output.txt");

        final FileStorageWriter writer =
                new FileStorageWriter(
                        new FileStoragePathResolver(root));

        final Resource resource =
                new Resource(
                        new ResourceId("destination-1"),
                        new ResourceLocation(
                                destination.toUri().toString()));

        try (InputStream content =
                     new ByteArrayInputStream(
                             "content".getBytes())) {

            assertThrows(
                    UnsupportedOperationException.class,
                    () -> writer.write(
                            resource,
                            content,
                            ConflictBehavior.VERSION));
        }

        assertTrue(
                Files.notExists(destination));
    }

    @Test
    void shouldPreserveExistingDestinationWhenContentWriteFails()
            throws IOException {

        final Path root =
                temporaryDirectory.resolve("storage");

        Files.createDirectories(root);

        final Path destination =
                root.resolve("output.txt");

        Files.write(
                destination,
                "original-content".getBytes());

        final FileStorageWriter writer =
                new FileStorageWriter(
                        new FileStoragePathResolver(root));

        final Resource resource =
                new Resource(
                        new ResourceId("destination-1"),
                        new ResourceLocation(
                                destination.toUri().toString()));

        final InputStream failingContent =
                new InputStream() {

                    private final byte[] content =
                            "partial-new-content".getBytes();

                    private int position;

                    @Override
                    public int read(
                            final byte[] buffer,
                            final int offset,
                            final int length)
                            throws IOException {

                        if (position >= 7) {
                            throw new IOException(
                                    "Simulated source failure");
                        }

                        final int remaining =
                                7 - position;

                        final int count =
                                Math.min(
                                        remaining,
                                        length);

                        System.arraycopy(
                                content,
                                position,
                                buffer,
                                offset,
                                count);

                        position += count;

                        return count;
                    }

                    @Override
                    public int read() throws IOException {
                        if (position >= 7) {
                            throw new IOException(
                                    "Simulated source failure");
                        }

                        return content[position++];
                    }
                };

        assertThrows(
                IOException.class,
                () -> writer.write(
                        resource,
                        failingContent,
                        ConflictBehavior.REPLACE));

        assertArrayEquals(
                "original-content".getBytes(),
                Files.readAllBytes(destination));
    }
}