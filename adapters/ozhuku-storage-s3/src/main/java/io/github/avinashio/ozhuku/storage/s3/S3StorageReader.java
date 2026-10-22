package io.github.avinashio.ozhuku.storage.s3;

import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.storage.StorageReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

/**
 * Reads resources from Amazon S3.
 *
 * <p>This reader streams S3 object content directly from S3, so memory
 * usage stays bounded regardless of object size.</p>
 */
public final class S3StorageReader implements StorageReader {

    private final S3Client s3;
    private final S3StoragePathResolver pathResolver;

    /**
     * Creates an S3 storage reader.
     *
     * @param s3           the S3 client used to access S3
     * @param pathResolver the path resolver for S3 resource locations
     */
    public S3StorageReader(
            final S3Client s3,
            final S3StoragePathResolver pathResolver) {

        this.s3 = Objects.requireNonNull(
                s3,
                "s3 must not be null");
        this.pathResolver = Objects.requireNonNull(
                pathResolver,
                "pathResolver must not be null");
    }

    @Override
    public InputStream open(
            final Resource resource) throws IOException {

        Objects.requireNonNull(
                resource,
                "resource must not be null");

        final S3StoragePathResolver.S3ObjectReference reference =
                pathResolver.resolve(resource.location());

        final GetObjectRequest request =
                GetObjectRequest.builder()
                        .bucket(reference.bucket())
                        .key(reference.key())
                        .build();

        try {
            return s3.getObject(request);
        } catch (final NoSuchKeyException exception) {
            throw new IOException(
                    "S3 object does not exist: "
                            + reference.bucket()
                            + "/"
                            + reference.key(),
                    exception);
        } catch (final SdkException exception) {
            throw new IOException(
                    "Failed to read S3 object: "
                            + reference.bucket()
                            + "/"
                            + reference.key(),
                    exception);
        }
    }
}
