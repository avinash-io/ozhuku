package io.github.avinashio.ozhuku.application.source;

import io.github.avinashio.ozhuku.domain.identity.SourceFingerprint;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.storage.SourceFingerprintProvider;
import java.io.IOException;
import java.util.Objects;

/**
 * Application service for resolving source fingerprints.
 *
 * Avinash: Fingerprint calculation remains an infrastructure concern while
 * the application layer owns when the fingerprint is requested.
 */
public final class SourceFingerprintService {

    private final SourceFingerprintProvider provider;

    public SourceFingerprintService(
            final SourceFingerprintProvider provider) {
        this.provider = Objects.requireNonNull(
                provider,
                "Source fingerprint provider must not be null");
    }

    public SourceFingerprint fingerprint(
            final Resource resource)
            throws IOException {

        Objects.requireNonNull(
                resource,
                "Resource must not be null");

        return provider.fingerprint(resource);
    }
}