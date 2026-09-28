package io.github.avinashio.ozhuku.storage.file;

import io.github.avinashio.ozhuku.domain.identity.SourceFingerprint;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.storage.SourceFingerprintProvider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

public final class FileSourceFingerprintProvider
        implements SourceFingerprintProvider {

    private final FileStoragePathResolver pathResolver;

    public FileSourceFingerprintProvider(
            final FileStoragePathResolver pathResolver) {

        this.pathResolver = Objects.requireNonNull(
                pathResolver,
                "pathResolver must not be null");
    }

    @Override
    public SourceFingerprint fingerprint(
            final Resource resource) throws IOException {

        Objects.requireNonNull(
                resource,
                "resource must not be null");

        final Path path =
                pathResolver.resolveExisting(resource.location());

        final BasicFileAttributes attributes =
                Files.readAttributes(
                        path,
                        BasicFileAttributes.class);

        final String fingerprint =
                path.toAbsolutePath()
                        .normalize()
                        + "|"
                        + attributes.size()
                        + "|"
                        + attributes.lastModifiedTime().toMillis();

        return new SourceFingerprint(fingerprint);
    }
}