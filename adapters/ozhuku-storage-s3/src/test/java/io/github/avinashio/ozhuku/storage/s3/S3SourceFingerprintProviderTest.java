package io.github.avinashio.ozhuku.storage.s3;

import io.github.avinashio.ozhuku.domain.identity.SourceFingerprint;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.domain.resource.ResourceLocation;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class S3SourceFingerprintProviderTest {

    @Test
    void shouldReturnFingerprintForExistingObject() throws IOException {

        final Map<String, byte[]> objects = new HashMap<>();
        objects.put(
                "default-bucket:path/to/source.txt",
                "content".getBytes());

        final S3SourceFingerprintProvider provider =
                new S3SourceFingerprintProvider(
                        new TestS3Client(objects),
                        new S3StoragePathResolver("default-bucket"));

        final Resource resource = new Resource(
                new io.github.avinashio.ozhuku.domain.identity.ResourceId("resource-1"),
                new ResourceLocation("s3://default-bucket/path/to/source.txt"));

        final SourceFingerprint fingerprint =
                provider.fingerprint(resource);

        assertNotNull(fingerprint);
        assertTrue(fingerprint.value().contains("default-bucket/path/to/source.txt"));
    }

    @Test
    void shouldThrowIOExceptionForNonExistentObject() {

        final Map<String, byte[]> objects = new HashMap<>();

        final S3SourceFingerprintProvider provider =
                new S3SourceFingerprintProvider(
                        new TestS3Client(objects),
                        new S3StoragePathResolver("default-bucket"));

        final Resource resource = new Resource(
                new io.github.avinashio.ozhuku.domain.identity.ResourceId("resource-1"),
                new ResourceLocation("s3://default-bucket/missing.txt"));

        assertThrows(
                IOException.class,
                () -> provider.fingerprint(resource));
    }

    @Test
    void shouldRejectNullResource() {

        final S3SourceFingerprintProvider provider =
                new S3SourceFingerprintProvider(
                        new TestS3Client(new HashMap<>()),
                        new S3StoragePathResolver("default-bucket"));

        assertThrows(
                NullPointerException.class,
                () -> provider.fingerprint(null));
    }
}
