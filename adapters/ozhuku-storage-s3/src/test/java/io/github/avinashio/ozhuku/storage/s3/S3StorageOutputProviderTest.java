package io.github.avinashio.ozhuku.storage.s3;

import io.github.avinashio.ozhuku.domain.delivery.ConflictBehavior;
import io.github.avinashio.ozhuku.domain.delivery.DeliveryPolicy;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.domain.resource.ResourceLocation;
import io.github.avinashio.ozhuku.storage.StorageOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class S3StorageOutputProviderTest {

    @Test
    void shouldWriteNewDestinationAtomically() throws IOException {

        final Map<String, byte[]> objects = new HashMap<>();

        final S3StorageOutputProvider provider =
                new S3StorageOutputProvider(
                        new TestS3Client(objects),
                        new S3StoragePathResolver("default-bucket"));

        final Resource destination = new Resource(
                new ResourceId("output-1"),
                new ResourceLocation("s3://default-bucket/output.txt"));

        try (StorageOutput output = provider.open(
                destination,
                new DeliveryPolicy(ConflictBehavior.FAIL))) {
            output.stream().write(
                    "hello".getBytes(StandardCharsets.UTF_8));
            output.commit();
        }

        assertTrue(objects.containsKey("default-bucket:output.txt"));
        assertArrayEquals(
                "hello".getBytes(StandardCharsets.UTF_8),
                objects.get("default-bucket:output.txt"));
    }

    @Test
    void shouldFailWhenDestinationExists() {

        final Map<String, byte[]> objects = new HashMap<>();
        objects.put(
                "default-bucket:output.txt",
                "existing".getBytes(StandardCharsets.UTF_8));

        final S3StorageOutputProvider provider =
                new S3StorageOutputProvider(
                        new TestS3Client(objects),
                        new S3StoragePathResolver("default-bucket"));

        final Resource destination = new Resource(
                new ResourceId("output-1"),
                new ResourceLocation("s3://default-bucket/output.txt"));

        assertThrows(
                IOException.class,
                () -> provider.open(
                        destination,
                        new DeliveryPolicy(ConflictBehavior.FAIL)));
    }

    @Test
    void shouldRejectUnsupportedVersionBehavior() {

        final Map<String, byte[]> objects = new HashMap<>();

        final S3StorageOutputProvider provider =
                new S3StorageOutputProvider(
                        new TestS3Client(objects),
                        new S3StoragePathResolver("default-bucket"));

        final Resource destination = new Resource(
                new ResourceId("output-1"),
                new ResourceLocation("s3://default-bucket/output.txt"));

        assertThrows(
                UnsupportedOperationException.class,
                () -> provider.open(
                        destination,
                        new DeliveryPolicy(ConflictBehavior.VERSION)));
    }

    @Test
    void shouldRejectStreamAfterClose() throws IOException {

        final Map<String, byte[]> objects = new HashMap<>();

        final S3StorageOutputProvider provider =
                new S3StorageOutputProvider(
                        new TestS3Client(objects),
                        new S3StoragePathResolver("default-bucket"));

        final Resource destination = new Resource(
                new ResourceId("output-1"),
                new ResourceLocation("s3://default-bucket/output.txt"));

        final StorageOutput output = provider.open(
                destination,
                new DeliveryPolicy(ConflictBehavior.FAIL));

        output.close();

        assertThrows(
                IllegalStateException.class,
                output::stream);
    }

    @Test
    void shouldNotWriteOnCloseWithoutCommit() throws IOException {

        final Map<String, byte[]> objects = new HashMap<>();

        final S3StorageOutputProvider provider =
                new S3StorageOutputProvider(
                        new TestS3Client(objects),
                        new S3StoragePathResolver("default-bucket"));

        final Resource destination = new Resource(
                new ResourceId("output-1"),
                new ResourceLocation("s3://default-bucket/output.txt"));

        final StorageOutput output = provider.open(
                destination,
                new DeliveryPolicy(ConflictBehavior.FAIL));

        try (OutputStream stream = output.stream()) {
            stream.write("partial".getBytes(StandardCharsets.UTF_8));
        }

        output.close();

        assertFalse(objects.containsKey("default-bucket:output.txt"));
    }

    @Test
    void shouldCommitDestinationExplicitly() throws IOException {

        final Map<String, byte[]> objects = new HashMap<>();

        final S3StorageOutputProvider provider =
                new S3StorageOutputProvider(
                        new TestS3Client(objects),
                        new S3StoragePathResolver("default-bucket"));

        final Resource destination = new Resource(
                new ResourceId("output-1"),
                new ResourceLocation("s3://default-bucket/output.txt"));

        final StorageOutput output = provider.open(
                destination,
                new DeliveryPolicy(ConflictBehavior.FAIL));

        try (OutputStream stream = output.stream()) {
            stream.write("committed".getBytes(StandardCharsets.UTF_8));
        }

        output.commit();
        output.close();

        assertArrayEquals(
                "committed".getBytes(StandardCharsets.UTF_8),
                objects.get("default-bucket:output.txt"));
    }

    @Test
    void shouldRejectSecondCommit() throws IOException {

        final Map<String, byte[]> objects = new HashMap<>();

        final S3StorageOutputProvider provider =
                new S3StorageOutputProvider(
                        new TestS3Client(objects),
                        new S3StoragePathResolver("default-bucket"));

        final Resource destination = new Resource(
                new ResourceId("output-1"),
                new ResourceLocation("s3://default-bucket/output.txt"));

        final StorageOutput output = provider.open(
                destination,
                new DeliveryPolicy(ConflictBehavior.FAIL));

        try (OutputStream stream = output.stream()) {
            stream.write("committed".getBytes(StandardCharsets.UTF_8));
        }

        output.commit();

        assertThrows(
                IllegalStateException.class,
                output::commit);

        output.close();
    }

    @Test
    void shouldRejectCommitAfterClose() throws IOException {

        final Map<String, byte[]> objects = new HashMap<>();

        final S3StorageOutputProvider provider =
                new S3StorageOutputProvider(
                        new TestS3Client(objects),
                        new S3StoragePathResolver("default-bucket"));

        final Resource destination = new Resource(
                new ResourceId("output-1"),
                new ResourceLocation("s3://default-bucket/output.txt"));

        final StorageOutput output = provider.open(
                destination,
                new DeliveryPolicy(ConflictBehavior.FAIL));

        output.close();

        assertThrows(
                IllegalStateException.class,
                output::commit);
    }
}
