package io.github.avinashio.ozhuku.storage.s3;

import io.github.avinashio.ozhuku.domain.resource.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class S3StoragePathResolverTest {

    @Test
    void shouldResolveBucketAndKeyFromLocation() {

        final S3StoragePathResolver resolver =
                new S3StoragePathResolver("default-bucket");

        final ResourceLocation location =
                new ResourceLocation("s3://my-bucket/path/to/object.txt");

        final S3StoragePathResolver.S3ObjectReference reference =
                resolver.resolve(location);

        assertEquals("my-bucket", reference.bucket());
        assertEquals("path/to/object.txt", reference.key());
    }

    @Test
    void shouldUseDefaultBucketWhenNotSpecified() {

        final S3StoragePathResolver resolver =
                new S3StoragePathResolver("default-bucket");

        final ResourceLocation location =
                new ResourceLocation("s3:///path/to/object.txt");

        final S3StoragePathResolver.S3ObjectReference reference =
                resolver.resolve(location);

        assertEquals("default-bucket", reference.bucket());
        assertEquals("path/to/object.txt", reference.key());
    }

    @Test
    void shouldRejectNonS3Scheme() {

        final S3StoragePathResolver resolver =
                new S3StoragePathResolver("default-bucket");

        final ResourceLocation location =
                new ResourceLocation("file:///path/to/object.txt");

        assertThrows(
                IllegalArgumentException.class,
                () -> resolver.resolve(location));
    }

    @Test
    void shouldRejectLocationWithoutBucketAndNoDefault() {

        final S3StoragePathResolver resolver =
                new S3StoragePathResolver(null);

        final ResourceLocation location =
                new ResourceLocation("s3:///path/to/object.txt");

        assertThrows(
                IllegalArgumentException.class,
                () -> resolver.resolve(location));
    }

    @Test
    void shouldRejectLocationWithoutKey() {

        final S3StoragePathResolver resolver =
                new S3StoragePathResolver("default-bucket");

        final ResourceLocation location =
                new ResourceLocation("s3://my-bucket/");

        assertThrows(
                IllegalArgumentException.class,
                () -> resolver.resolve(location));
    }

    @Test
    void shouldRejectNullLocation() {

        final S3StoragePathResolver resolver =
                new S3StoragePathResolver("default-bucket");

        assertThrows(
                NullPointerException.class,
                () -> resolver.resolve(null));
    }
}
