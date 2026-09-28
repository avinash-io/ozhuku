package io.github.avinashio.ozhuku.application.source;

import io.github.avinashio.ozhuku.domain.identity.SourceIdentity;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.storage.SourceIdentityProvider;
import java.io.IOException;
import java.util.Objects;

/**
 * Application service for resolving stable source identities.
 *
 * Avinash: Keep source identity resolution behind the application boundary so
 * execution and deduplication logic do not depend on a storage implementation.
 */
public final class SourceIdentityService {

    private final SourceIdentityProvider provider;

    public SourceIdentityService(
            final SourceIdentityProvider provider) {
        this.provider = Objects.requireNonNull(
                provider,
                "Source identity provider must not be null");
    }

    public SourceIdentity identify(
            final Resource resource)
            throws IOException {

        Objects.requireNonNull(
                resource,
                "Resource must not be null");

        return provider.identify(resource);
    }
}