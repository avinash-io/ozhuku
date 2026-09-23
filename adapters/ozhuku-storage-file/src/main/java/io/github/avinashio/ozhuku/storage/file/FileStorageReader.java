package io.github.avinashio.ozhuku.storage.file;

import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.storage.StorageReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Objects;

public final class FileStorageReader implements StorageReader {

    private final FileStoragePathResolver pathResolver;

    public FileStorageReader(
            final FileStoragePathResolver pathResolver) {

        this.pathResolver = Objects.requireNonNull(
                pathResolver,
                "pathResolver must not be null");
    }

    @Override
    public InputStream open(
            final Resource resource) throws IOException {

        Objects.requireNonNull(
                resource,
                "resource must not be null");

        final Path path =
                pathResolver.resolveExisting(
                        resource.location());

        return java.nio.file.Files.newInputStream(path);
    }
}