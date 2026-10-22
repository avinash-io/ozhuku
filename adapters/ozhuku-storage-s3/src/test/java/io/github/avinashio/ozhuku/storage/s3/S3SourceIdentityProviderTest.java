package io.github.avinashio.ozhuku.storage.s3;

import io.github.avinashio.ozhuku.domain.identity.SourceIdentity;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.domain.resource.ResourceLocation;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class S3SourceIdentityProviderTest {

    @Test
    void shouldReturnSourceIdentityForExistingObject() throws IOException {

        final Map<String, byte[]> objects = new HashMap<>();
        objects.put(
                "default-bucket:path/to/source.txt",
                "content".getBytes());

        final S3SourceIdentityProvider provider =
                new S3SourceIdentityProvider(
                        new TestS3Client(objects),
                        new S3StoragePathResolver("default-bucket"));

        final Resource resource = new Resource(
                new io.github.avinashio.ozhuku.domain.identity.ResourceId("resource-1"),
                new ResourceLocation("s3://default-bucket/path/to/source.txt"));

        final SourceIdentity identity = provider.identify(resource);

        assertNotNull(identity);
        assertTrue(identity.value().contains("default-bucket/path/to/source.txt"));
    }

    @Test
    void shouldThrowIOExceptionForNonExistentObject() {

        final Map<String, byte[]> objects = new HashMap<>();

        final S3SourceIdentityProvider provider =
                new S3SourceIdentityProvider(
                        new TestS3Client(objects),
                        new S3StoragePathResolver("default-bucket"));

        final Resource resource = new Resource(
                new io.github.avinashio.ozhuku.domain.identity.ResourceId("resource-1"),
                new ResourceLocation("s3://default-bucket/missing.txt"));

        assertThrows(
                IOException.class,
                () -> provider.identify(resource));
    }

    @Test
    void shouldRejectNullResource() {

        final S3SourceIdentityProvider provider =
                new S3SourceIdentityProvider(
                        new TestS3Client(new HashMap<>()),
                        new S3StoragePathResolver("default-bucket"));

        assertThrows(
                NullPointerException.class,
                () -> provider.identify(null));
    }
}
