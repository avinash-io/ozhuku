package io.github.avinashio.ozhuku.storage.file;

import io.github.avinashio.ozhuku.domain.identity.SourceIdentity;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.storage.SourceIdentityProvider;
import java.io.IOException;
import java.util.Objects;

public final class FileSourceIdentityProvider
        implements SourceIdentityProvider {

    private final FileStoragePathResolver pathResolver;

    public FileSourceIdentityProvider(
            final FileStoragePathResolver pathResolver) {

        this.pathResolver = Objects.requireNonNull(
                pathResolver,
                "pathResolver must not be null");
    }

    @Override
    public SourceIdentity identify(
            final Resource resource) throws IOException {

        Objects.requireNonNull(
                resource,
                "resource must not be null");

        return new SourceIdentity(
                pathResolver
                        .resolveExisting(resource.location())
                        .toAbsolutePath()
                        .normalize()
                        .toString());
    }
}