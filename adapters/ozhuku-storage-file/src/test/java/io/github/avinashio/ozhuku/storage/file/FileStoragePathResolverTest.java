package io.github.avinashio.ozhuku.storage.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.domain.resource.ResourceLocation;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileStoragePathResolverTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void shouldResolveLocationInsideRoot() {
        final Path root =
                temporaryDirectory.resolve("storage-root")
                        .toAbsolutePath()
                        .normalize();

        final FileStoragePathResolver resolver =
                new FileStoragePathResolver(root);

        final ResourceLocation location =
                new ResourceLocation(
                        root.resolve("input")
                                .resolve("file.txt")
                                .toUri()
                                .toString());

        final Path resolved =
                resolver.resolve(location);

        assertEquals(
                root.resolve("input")
                        .resolve("file.txt")
                        .normalize(),
                resolved);
    }

    @Test
    void shouldRejectLocationOutsideRoot() {
        final Path root =
                temporaryDirectory.resolve("storage-root")
                        .toAbsolutePath()
                        .normalize();

        final FileStoragePathResolver resolver =
                new FileStoragePathResolver(root);

        final Path outside =
                root.getParent()
                        .resolve("outside.txt")
                        .toAbsolutePath()
                        .normalize();

        final ResourceLocation location =
                new ResourceLocation(
                        outside.toUri().toString());

        assertThrows(
                IllegalArgumentException.class,
                () -> resolver.resolve(location));
    }

    @Test
    void shouldRejectTraversalOutsideRoot() {
        final Path root =
                temporaryDirectory.resolve("storage-root")
                        .toAbsolutePath()
                        .normalize();

        final FileStoragePathResolver resolver =
                new FileStoragePathResolver(root);

        final ResourceLocation location =
                new ResourceLocation(
                        root.resolve("..")
                                .resolve("outside.txt")
                                .toUri()
                                .toString());

        assertThrows(
                IllegalArgumentException.class,
                () -> resolver.resolve(location));
    }

    @Test
    void shouldRejectNonFileScheme() {
        final Path root =
                temporaryDirectory.resolve("storage-root")
                        .toAbsolutePath()
                        .normalize();

        final FileStoragePathResolver resolver =
                new FileStoragePathResolver(root);

        final ResourceLocation location =
                new ResourceLocation(
                        "s3://bucket/object.txt");

        assertThrows(
                IllegalArgumentException.class,
                () -> resolver.resolve(location));
    }

    @Test
    void shouldResolveExistingFileInsideRoot()
            throws IOException {

        final Path root =
                temporaryDirectory.resolve("storage-root")
                        .toAbsolutePath()
                        .normalize();

        final Path file =
                root.resolve("input")
                        .resolve("file.txt");

        Files.createDirectories(file.getParent());
        Files.writeString(file, "test");

        final FileStoragePathResolver resolver =
                new FileStoragePathResolver(root);

        final Path resolved =
                resolver.resolveExisting(
                        new ResourceLocation(
                                file.toUri().toString()));

        assertEquals(
                file.toRealPath(),
                resolved);
    }

    @Test
    void shouldRejectMissingFileForResolveExisting()
            throws IOException {

        final Path root =
                temporaryDirectory.resolve("storage-root")
                        .toAbsolutePath()
                        .normalize();

        Files.createDirectories(root);

        final Path missingFile =
                root.resolve("missing.txt");

        final FileStoragePathResolver resolver =
                new FileStoragePathResolver(root);

        assertThrows(
                IOException.class,
                () -> resolver.resolveExisting(
                        new ResourceLocation(
                                missingFile.toUri().toString())));
    }

    @Test
    void shouldResolveNewFileForWrite()
            throws IOException {

        final Path root =
                temporaryDirectory.resolve("storage-root")
                        .toAbsolutePath()
                        .normalize();

        Files.createDirectories(root);

        final Path destination =
                root.resolve("output")
                        .resolve("result.txt");

        Files.createDirectories(destination.getParent());

        final FileStoragePathResolver resolver =
                new FileStoragePathResolver(root);

        final Path resolved =
                resolver.resolveForWrite(
                        new ResourceLocation(
                                destination.toUri().toString()));

        assertEquals(
                destination.normalize(),
                resolved);
    }

    @Test
    void shouldRejectWriteWhenExistingParentIsOutsideRoot()
            throws IOException {

        final Path root =
                temporaryDirectory.resolve("storage-root")
                        .toAbsolutePath()
                        .normalize();

        Files.createDirectories(root);

        final Path outside =
                temporaryDirectory.resolve("outside");

        Files.createDirectories(outside);

        final Path destination =
                outside.resolve("result.txt");

        final FileStoragePathResolver resolver =
                new FileStoragePathResolver(root);

        assertThrows(
                IllegalArgumentException.class,
                () -> resolver.resolveForWrite(
                        new ResourceLocation(
                                destination.toUri().toString())));
    }

    @Test
    void shouldRejectNullLocation() {
        final Path root =
                temporaryDirectory.resolve("storage-root");

        final FileStoragePathResolver resolver =
                new FileStoragePathResolver(root);

        assertThrows(
                NullPointerException.class,
                () -> resolver.resolve(null));
    }

    @Test
    void shouldRejectNullRoot() {
        assertThrows(
                NullPointerException.class,
                () -> new FileStoragePathResolver(null));
    }
}