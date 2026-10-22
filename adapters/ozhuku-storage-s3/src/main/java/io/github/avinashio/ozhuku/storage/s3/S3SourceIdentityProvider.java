package io.github.avinashio.ozhuku.storage.s3;

import io.github.avinashio.ozhuku.domain.identity.SourceIdentity;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.storage.SourceIdentityProvider;
import java.io.IOException;
import java.util.Objects;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

/**
 * Provides source identity for S3 resources.
 *
 * <p>Source identity for an S3 object is derived from its bucket, key,
 * version (if available), and ETag. This provides a stable, content-
 * aware identity that is suitable for deduplication.</p>
 */
public final class S3SourceIdentityProvider
        implements SourceIdentityProvider {

    private final S3Client s3;
    private final S3StoragePathResolver pathResolver;

    /**
     * Creates an S3 source identity provider.
     *
     * @param s3           the S3 client used to access S3
     * @param pathResolver the path resolver for S3 resource locations
     */
    public S3SourceIdentityProvider(
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
    public SourceIdentity identify(
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

            final String version = response.versionId();
            final String etag = response.eTag();

            final String identity =
                    reference.bucket()
                            + "/"
                            + reference.key()
                            + "|"
                            + (version != null ? version : "")
                            + "|"
                            + (etag != null ? etag : "");

            return new SourceIdentity(identity);
        } catch (final NoSuchKeyException exception) {
            throw new IOException(
                    "S3 object does not exist: "
                            + reference.bucket()
                            + "/"
                            + reference.key(),
                    exception);
        } catch (final SdkException exception) {
            throw new IOException(
                    "Failed to identify S3 source: "
                            + reference.bucket()
                            + "/"
                            + reference.key(),
                    exception);
        }
    }
}
