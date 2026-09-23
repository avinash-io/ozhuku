package io.github.avinashio.ozhuku.storage.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.domain.resource.ResourceLocation;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class FileStoragePathResolverTest {

    @Test
    void shouldResolveLocationInsideRoot() {
        final Path root =
                Path.of("target", "storage-root")
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
                Path.of("target", "storage-root")
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
                Path.of("target", "storage-root")
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
                Path.of("target", "storage-root")
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
    void shouldRejectNullLocation() {
        final Path root =
                Path.of("target", "storage-root");

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