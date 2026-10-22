package io.github.avinashio.ozhuku.storage.s3;

import io.github.avinashio.ozhuku.domain.identity.SourceFingerprint;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.storage.SourceFingerprintProvider;
import java.io.IOException;
import java.util.Objects;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

/**
 * Provides source fingerprints for S3 resources.
 *
 * <p>A source fingerprint for an S3 object is derived from its ETag,
 * size, and last-modified timestamp. This provides a content-aware
 * fingerprint suitable for deduplication policy evaluation.</p>
 */
public final class S3SourceFingerprintProvider
        implements SourceFingerprintProvider {

    private final S3Client s3;
    private final S3StoragePathResolver pathResolver;

    /**
     * Creates an S3 source fingerprint provider.
     *
     * @param s3           the S3 client used to access S3
     * @param pathResolver the path resolver for S3 resource locations
     */
    public S3SourceFingerprintProvider(
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
    public SourceFingerprint fingerprint(
            final Resource resource) throws IOException {

        Objects.requireNonNull(
                resource,
                "resource must not be null");

        final S3StoragePathResolver.S3ObjectReference reference =
                pathResolver.resolve(resource.location());

        final HeadObjectRequest request =
                HeadObjectRequest.builder()
                        .bucket(reference.bucket())
                        .key(reference.key())
                        .build();

        try {
            final software.amazon.awssdk.services.s3.model.HeadObjectResponse response =
                    s3.headObject(request);

            final long size = response.contentLength() != null
                    ? response.contentLength()
                    : 0L;
            final long modified = response.lastModified() != null
                    ? response.lastModified().toEpochMilli()
                    : 0L;
            final String etag = response.eTag() != null
                    ? response.eTag()
                    : "";

            final String fingerprint =
                    reference.bucket()
                            + "/"
                            + reference.key()
                            + "|"
                            + size
                            + "|"
                            + modified
                            + "|"
                            + etag;

            return new SourceFingerprint(fingerprint);
        } catch (final NoSuchKeyException exception) {
            throw new IOException(
                    "S3 object does not exist: "
                            + reference.bucket()
                            + "/"
                            + reference.key(),
                    exception);
        } catch (final SdkException exception) {
            throw new IOException(
                    "Failed to fingerprint S3 source: "
                            + reference.bucket()
                            + "/"
                            + reference.key(),
                    exception);
        }
    }
}
