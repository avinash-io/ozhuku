package io.github.avinashio.ozhuku.application.source;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.domain.identity.SourceIdentity;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.storage.SourceIdentityProvider;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class SourceIdentityServiceTest {

    @Test
    void shouldResolveSourceIdentity() throws IOException {
        final SourceIdentity expected =
                new SourceIdentity("source-identity-1");

        final SourceIdentityProvider provider =
                new SourceIdentityProvider() {
                    @Override
                    public SourceIdentity identify(
                            final Resource resource)
                            throws IOException {
                        return expected;
                    }
                };

        final SourceIdentityService service =
                new SourceIdentityService(provider);

        final Resource resource =
                createResource();

        final SourceIdentity actual =
                service.identify(resource);

        assertEquals(
                expected,
                actual);
    }

    @Test
    void shouldPropagateProviderFailure()
            throws IOException {

        final SourceIdentityProvider provider =
                new SourceIdentityProvider() {
                    @Override
                    public SourceIdentity identify(
                            final Resource resource)
                            throws IOException {
                        throw new IOException(
                                "identity failure");
                    }
                };

        final SourceIdentityService service =
                new SourceIdentityService(provider);

        assertThrows(
                IOException.class,
                () -> service.identify(createResource()));
    }

    @Test
    void shouldRejectNullResource() {
        final SourceIdentityProvider provider =
                resource ->
                        new SourceIdentity("identity");

        final SourceIdentityService service =
                new SourceIdentityService(provider);

        assertThrows(
                NullPointerException.class,
                () -> service.identify(null));
    }

    @Test
    void shouldRejectNullProvider() {
        assertThrows(
                NullPointerException.class,
                () -> new SourceIdentityService(null));
    }

    private Resource createResource() {
        return new Resource(
                new io.github.avinashio.ozhuku.domain.identity.ResourceId(
                        "resource-1"),
                new io.github.avinashio.ozhuku.domain.resource.ResourceLocation(
                        "file:///storage/input.txt"));
    }
}
