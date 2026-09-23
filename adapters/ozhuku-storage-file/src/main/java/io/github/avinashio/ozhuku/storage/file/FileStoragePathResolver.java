package io.github.avinashio.ozhuku.storage.file;

import io.github.avinashio.ozhuku.domain.resource.ResourceLocation;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public final class FileStoragePathResolver {

    private final Path root;

    public FileStoragePathResolver(
            final Path root) {

        this.root = Objects.requireNonNull(
                        root,
                        "root must not be null")
                .toAbsolutePath()
                .normalize();
    }

    public Path resolve(
            final ResourceLocation location) {

        Objects.requireNonNull(
                location,
                "location must not be null");

        if (!"file".equalsIgnoreCase(location.scheme())) {
            throw new IllegalArgumentException(
                    "Resource location must use the file scheme");
        }

        final URI uri = location.uri();

        final Path requestedPath =
                Paths.get(uri)
                        .toAbsolutePath()
                        .normalize();

        if (!requestedPath.startsWith(root)) {
            throw new IllegalArgumentException(
                    "Resource location resolves outside "
                            + "the configured storage root");
        }

        return requestedPath;
    }

    public Path resolveExisting(
            final ResourceLocation location)
            throws IOException {

        final Path resolved = resolve(location);

        final Path realRoot =
                root.toRealPath();

        final Path realPath =
                resolved.toRealPath();

        if (!realPath.startsWith(realRoot)) {
            throw new IllegalArgumentException(
                    "Resource location resolves outside "
                            + "the configured storage root");
        }

        return realPath;
    }

    public Path resolveForWrite(
            final ResourceLocation location)
            throws IOException {

        final Path resolved = resolve(location);

        final Path realRoot =
                root.toRealPath();

        Path existingParent =
                resolved.getParent();

        while (existingParent != null
                && !Files.exists(existingParent)) {
            existingParent = existingParent.getParent();
        }

        if (existingParent == null) {
            throw new IOException(
                    "Unable to determine an existing "
                            + "destination parent");
        }

        final Path realParent =
                existingParent.toRealPath();

        if (!realParent.startsWith(realRoot)) {
            throw new IllegalArgumentException(
                    "Destination resolves outside "
                            + "the configured storage root");
        }

        return resolved;
    }
}