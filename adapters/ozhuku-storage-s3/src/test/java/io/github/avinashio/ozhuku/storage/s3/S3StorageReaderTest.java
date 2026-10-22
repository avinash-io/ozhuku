package io.github.avinashio.ozhuku.storage.s3;

import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.domain.resource.ResourceLocation;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class S3StorageReaderTest {

    @Test
    void shouldOpenExistingObjectAsStream() throws IOException {

        final Map<String, byte[]> objects = new HashMap<>();
        objects.put(
                "default-bucket:path/to/object.txt",
                "hello-s3".getBytes(StandardCharsets.UTF_8));

        final S3StorageReader reader =
                new S3StorageReader(
                        new TestS3Client(objects),
                        new S3StoragePathResolver("default-bucket"));

        final Resource resource = new Resource(
                new ResourceId("resource-1"),
                new ResourceLocation("s3://default-bucket/path/to/object.txt"));

        try (InputStream input = reader.open(resource)) {
            assertArrayEquals(
                    "hello-s3".getBytes(StandardCharsets.UTF_8),
                    input.readAllBytes());
        }
    }

    @Test
    void shouldThrowIOExceptionForNonExistentObject() {

        final Map<String, byte[]> objects = new HashMap<>();

        final S3StorageReader reader =
                new S3StorageReader(
                        new TestS3Client(objects),
                        new S3StoragePathResolver("default-bucket"));

        final Resource resource = new Resource(
                new ResourceId("resource-1"),
                new ResourceLocation("s3://default-bucket/missing.txt"));

        assertThrows(
                IOException.class,
                () -> reader.open(resource));
    }

    @Test
    void shouldRejectNullResource() {

        final S3StorageReader reader =
                new S3StorageReader(
                        new TestS3Client(new HashMap<>()),
                        new S3StoragePathResolver("default-bucket"));

        assertThrows(
                NullPointerException.class,
                () -> reader.open(null));
    }

    @Test
    void shouldValidateSchemeInLocation() {

        final S3StorageReader reader =
                new S3StorageReader(
                        new TestS3Client(new HashMap<>()),
                        new S3StoragePathResolver("default-bucket"));

        final Resource resource = new Resource(
                new ResourceId("resource-1"),
                new ResourceLocation("file:///path/to/object.txt"));

        assertThrows(
                IllegalArgumentException.class,
                () -> reader.open(resource));
    }
}
