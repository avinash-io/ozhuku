package io.github.avinashio.ozhuku.application.source;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.domain.identity.SourceFingerprint;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.storage.SourceFingerprintProvider;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class SourceFingerprintServiceTest {

    @Test
    void shouldResolveSourceFingerprint()
            throws IOException {

        final SourceFingerprint expected =
                new SourceFingerprint(
                        "source-fingerprint-1");

        final SourceFingerprintProvider provider =
                new SourceFingerprintProvider() {
                    @Override
                    public SourceFingerprint fingerprint(
                            final Resource resource)
                            throws IOException {
                        return expected;
                    }
                };

        final SourceFingerprintService service =
                new SourceFingerprintService(provider);

        final SourceFingerprint actual =
                service.fingerprint(
                        createResource());

        assertEquals(
                expected,
                actual);
    }

    @Test
    void shouldPropagateProviderFailure()
            throws IOException {

        final SourceFingerprintProvider provider =
                new SourceFingerprintProvider() {
                    @Override
                    public SourceFingerprint fingerprint(
                            final Resource resource)
                            throws IOException {
                        throw new IOException(
                                "fingerprint failure");
                    }
                };

        final SourceFingerprintService service =
                new SourceFingerprintService(provider);

        assertThrows(
                IOException.class,
                () -> service.fingerprint(
                        createResource()));
    }

    @Test
    void shouldRejectNullResource() {
        final SourceFingerprintProvider provider =
                resource ->
                        new SourceFingerprint(
                                "fingerprint");

        final SourceFingerprintService service =
                new SourceFingerprintService(provider);

        assertThrows(
                NullPointerException.class,
                () -> service.fingerprint(null));
    }

    @Test
    void shouldRejectNullProvider() {
        assertThrows(
                NullPointerException.class,
                () -> new SourceFingerprintService(null));
    }

    private Resource createResource() {
        return new Resource(
                new io.github.avinashio.ozhuku.domain.identity.ResourceId(
                        "resource-1"),
                new io.github.avinashio.ozhuku.domain.resource.ResourceLocation(
                        "file:///storage/input.txt"));
    }
}
