package io.github.avinashio.ozhuku.storage.s3;

import io.github.avinashio.ozhuku.domain.delivery.ConflictBehavior;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class S3StorageWriterTest {

    @Test
    void shouldWriteResourceContent() throws IOException {

        final Map<String, byte[]> objects = new HashMap<>();

        final S3StorageWriter writer =
                new S3StorageWriter(
                        new TestS3Client(objects),
                        new S3StoragePathResolver("default-bucket"));

        final Resource resource = new Resource(
                new ResourceId("dest-1"),
                new ResourceLocation("s3://default-bucket/path/to/output.txt"));

        final byte[] content =
                "hello-s3".getBytes(StandardCharsets.UTF_8);

        try (InputStream input = new ByteArrayInputStream(content)) {
            writer.write(resource, input, ConflictBehavior.REPLACE);
        }

        assertTrue(objects.containsKey(
                "default-bucket:path/to/output.txt"));
        assertArrayEquals(
                content,
                objects.get("default-bucket:path/to/output.txt"));
    }

    @Test
    void shouldReplaceExistingObjectWithReplaceBehavior() throws IOException {

        final Map<String, byte[]> objects = new HashMap<>();
        objects.put(
                "default-bucket:path/to/output.txt",
                "old-content".getBytes(StandardCharsets.UTF_8));

        final S3StorageWriter writer =
                new S3StorageWriter(
                        new TestS3Client(objects),
                        new S3StoragePathResolver("default-bucket"));

        final Resource resource = new Resource(
                new ResourceId("dest-1"),
                new ResourceLocation("s3://default-bucket/path/to/output.txt"));

        final byte[] replacement =
                "new-content".getBytes(StandardCharsets.UTF_8);

        try (InputStream input = new ByteArrayInputStream(replacement)) {
            writer.write(resource, input, ConflictBehavior.REPLACE);
        }

        assertArrayEquals(
                replacement,
                objects.get("default-bucket:path/to/output.txt"));
    }

    @Test
    void shouldFailWhenObjectExists() throws IOException {

        final Map<String, byte[]> objects = new HashMap<>();
        objects.put(
                "default-bucket:path/to/output.txt",
                "existing-content".getBytes(StandardCharsets.UTF_8));

        final S3StorageWriter writer =
                new S3StorageWriter(
                        new TestS3Client(objects),
                        new S3StoragePathResolver("default-bucket"));

        final Resource resource = new Resource(
                new ResourceId("dest-1"),
                new ResourceLocation("s3://default-bucket/path/to/output.txt"));

        final byte[] content =
                "new-content".getBytes(StandardCharsets.UTF_8);

        try (InputStream input = new ByteArrayInputStream(content)) {
            assertThrows(
                    IOException.class,
                    () -> writer.write(resource, input, ConflictBehavior.FAIL));
        }
    }

    @Test
    void shouldSkipWhenObjectExistsWithSkipBehavior() throws IOException {

        final Map<String, byte[]> objects = new HashMap<>();
        objects.put(
                "default-bucket:path/to/output.txt",
                "existing-content".getBytes(StandardCharsets.UTF_8));

        final S3StorageWriter writer =
                new S3StorageWriter(
                        new TestS3Client(objects),
                        new S3StoragePathResolver("default-bucket"));

        final Resource resource = new Resource(
                new ResourceId("dest-1"),
                new ResourceLocation("s3://default-bucket/path/to/output.txt"));

        final byte[] replacement =
                "new-content".getBytes(StandardCharsets.UTF_8);

        try (InputStream input = new ByteArrayInputStream(replacement)) {
            writer.write(resource, input, ConflictBehavior.SKIP);
        }

        assertArrayEquals(
                "existing-content".getBytes(StandardCharsets.UTF_8),
                objects.get("default-bucket:path/to/output.txt"));
    }

    @Test
    void shouldRejectVersionConflictBehavior() throws IOException {

        final S3StorageWriter writer =
                new S3StorageWriter(
                        new TestS3Client(new HashMap<>()),
                        new S3StoragePathResolver("default-bucket"));

        final Resource resource = new Resource(
                new ResourceId("dest-1"),
                new ResourceLocation("s3://default-bucket/path/to/output.txt"));

        final byte[] content =
                "content".getBytes(StandardCharsets.UTF_8);

        try (InputStream input = new ByteArrayInputStream(content)) {
            assertThrows(
                    UnsupportedOperationException.class,
                    () -> writer.write(resource, input, ConflictBehavior.VERSION));
        }
    }

    @Test
    void shouldRejectNullDestination() {

        final S3StorageWriter writer =
                new S3StorageWriter(
                        new TestS3Client(new HashMap<>()),
                        new S3StoragePathResolver("default-bucket"));

        final InputStream content =
                new ByteArrayInputStream("content".getBytes());

        assertThrows(
                NullPointerException.class,
                () -> writer.write(null, content, ConflictBehavior.REPLACE));
    }

    @Test
    void shouldRejectNullContent() {

        final S3StorageWriter writer =
                new S3StorageWriter(
                        new TestS3Client(new HashMap<>()),
                        new S3StoragePathResolver("default-bucket"));

        final Resource resource = new Resource(
                new ResourceId("dest-1"),
                new ResourceLocation("s3://default-bucket/path/to/output.txt"));

        assertThrows(
                NullPointerException.class,
                () -> writer.write(resource, null, ConflictBehavior.REPLACE));
    }

    @Test
    void shouldRejectNullConflictBehavior() {

        final S3StorageWriter writer =
                new S3StorageWriter(
                        new TestS3Client(new HashMap<>()),
                        new S3StoragePathResolver("default-bucket"));

        final Resource resource = new Resource(
                new ResourceId("dest-1"),
                new ResourceLocation("s3://default-bucket/path/to/output.txt"));

        final InputStream content =
                new ByteArrayInputStream("content".getBytes());

        assertThrows(
                NullPointerException.class,
                () -> writer.write(resource, content, null));
    }
}
