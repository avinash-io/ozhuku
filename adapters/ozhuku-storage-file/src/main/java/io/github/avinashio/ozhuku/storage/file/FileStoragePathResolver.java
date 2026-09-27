package io.github.avinashio.ozhuku.storage.file;

import io.github.avinashio.ozhuku.domain.resource.ResourceLocation;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
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

        if (uri.getAuthority() != null
                && !uri.getAuthority().isBlank()) {
            throw new IllegalArgumentException(
                    "File resource location must not "
                            + "contain an authority");
        }

        final String uriPath = uri.getPath();

        if (uriPath == null
                || uriPath.isBlank()) {
            throw new IllegalArgumentException(
                    "File resource location must contain a path");
        }

        final String relativePath =
                uriPath.startsWith("/")
                        ? uriPath.substring(1)
                        : uriPath;

        if (relativePath.isBlank()) {
            throw new IllegalArgumentException(
                    "File resource location must contain a path");
        }

        final Path requestedPath =
                root.resolve(relativePath)
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