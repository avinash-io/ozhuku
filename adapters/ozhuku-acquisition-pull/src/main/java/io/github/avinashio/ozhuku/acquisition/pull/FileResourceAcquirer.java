package io.github.avinashio.ozhuku.acquisition.pull;

import io.github.avinashio.ozhuku.acquisition.ResourceAcquirer;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Filesystem-based implementation of {@link ResourceAcquirer}.
 *
 * <p>Streams file content directly from the local filesystem, ensuring
 * bounded memory usage regardless of file size.</p>
 */
public final class FileResourceAcquirer implements ResourceAcquirer {

    @Override
    public InputStream acquire(
            final Resource resource) throws IOException {

        Objects.requireNonNull(
                resource,
                "resource must not be null");

        final String scheme = resource.location().scheme();

        if (!"file".equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException(
                    "Resource location must use the file scheme, "
                            + "got: " + scheme);
        }

        final String path = resource.location().uri().getPath();

        try {
            final Path filePath = Paths.get(resource.location().uri());
            return Files.newInputStream(filePath);
        } catch (final IllegalArgumentException exception) {
            throw new IOException(
                    "Failed to resolve file path for resource: "
                            + resource.location().value(),
                    exception);
        }
    }
}
