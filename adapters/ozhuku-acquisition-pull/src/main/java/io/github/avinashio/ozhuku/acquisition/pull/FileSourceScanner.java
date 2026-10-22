package io.github.avinashio.ozhuku.acquisition.pull;

import io.github.avinashio.ozhuku.acquisition.ScannedResource;
import io.github.avinashio.ozhuku.acquisition.SourceScanner;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.domain.resource.ResourceLocation;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Filesystem-based implementation of {@link SourceScanner}.
 *
 * <p>Scans a local directory for files and returns them as
 * {@link ScannedResource} instances with path-based resource IDs and
 * {@code file://} locations.</p>
 */
public final class FileSourceScanner implements SourceScanner {

    private final Path sourceDirectory;

    /**
     * Creates a filesystem source scanner.
     *
     * @param sourceDirectory the root directory to scan
     */
    public FileSourceScanner(final Path sourceDirectory) {
        this.sourceDirectory = Objects.requireNonNull(
                sourceDirectory,
                "sourceDirectory must not be null");
    }

    @Override
    public List<ScannedResource> scan() throws IOException {

        final List<ScannedResource> resources = new ArrayList<>();

        if (!Files.exists(sourceDirectory)) {
            return resources;
        }

        try (var stream = Files.find(
                sourceDirectory,
                Integer.MAX_VALUE,
                (path, attrs) -> attrs.isRegularFile())) {

            for (final Path file : (Iterable<Path>) stream.toList()) {
                resources.add(toScannedResource(file));
            }
        }

        return resources;
    }

    private ScannedResource toScannedResource(final Path file)
            throws IOException {

        final BasicFileAttributes attrs =
                Files.readAttributes(file, BasicFileAttributes.class);

        final String relativePath = sourceDirectory
                .relativize(file)
                .toString()
                .replace('\\', '/');

        final String resourceId = "file:" + relativePath;

        return ScannedResource.of(
                new ResourceId(resourceId),
                new ResourceLocation("file:" + file.toUri().getPath()),
                attrs.size(),
                toEpochMillis(attrs.lastModifiedTime()),
                null,
                null);
    }

    private static long toEpochMillis(final FileTime fileTime) {
        return fileTime != null
                ? fileTime.toMillis()
                : 0L;
    }
}
