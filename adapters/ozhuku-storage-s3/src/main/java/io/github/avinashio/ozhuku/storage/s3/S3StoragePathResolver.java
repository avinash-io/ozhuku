package io.github.avinashio.ozhuku.storage.s3;

import io.github.avinashio.ozhuku.domain.resource.ResourceLocation;
import java.net.URI;
import java.util.Objects;

/**
 * Resolves S3 resource locations to bucket and object key pairs.
 *
 * <p>The S3 scheme is {@code s3://bucket/key}. This resolver extracts and
 * validates the bucket name and object key from resource locations that use
 * the {@code s3} scheme.</p>
 */
public final class S3StoragePathResolver {

    private final String bucket;

    /**
     * Creates an S3 storage path resolver rooted at the given bucket.
     *
     * @param bucket the default S3 bucket name (may be {@code null} to
     *               require the bucket in every resource location)
     */
    public S3StoragePathResolver(final String bucket) {
        this.bucket = bucket;
    }

    /**
     * Resolves a resource location to an S3 object reference.
     *
     * @param location the resource location using the {@code s3://} scheme
     * @return the S3 object reference (bucket and key)
     * @throws IllegalArgumentException if the location does not use the
     *         {@code s3} scheme or is otherwise invalid
     */
    public S3ObjectReference resolve(
            final ResourceLocation location) {

        Objects.requireNonNull(
                location,
                "location must not be null");

        final URI uri = location.uri();

        if (!"s3".equalsIgnoreCase(uri.getScheme())) {
            throw new IllegalArgumentException(
                    "Resource location must use the s3 scheme");
        }

        final String resolvedBucket = uri.getHost();

        if (resolvedBucket == null || resolvedBucket.isBlank()) {
            if (bucket == null || bucket.isBlank()) {
                throw new IllegalArgumentException(
                        "Resource location must contain a bucket");
            }
            return new S3ObjectReference(bucket, keyFromPath(uri.getPath()));
        }

        return new S3ObjectReference(
                resolvedBucket,
                keyFromPath(uri.getPath()));
    }

    private static String keyFromPath(
            final String path) {

        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException(
                    "Resource location must contain an object key");
        }

        final String trimmed = path.startsWith("/")
                ? path.substring(1)
                : path;

        if (trimmed.isBlank()) {
            throw new IllegalArgumentException(
                    "Resource location must contain an object key");
        }

        return trimmed;
    }

    /**
     * Immutable reference to an S3 object.
     */
    public record S3ObjectReference(
            String bucket,
            String key) {

        /**
         * Creates an S3 object reference.
         *
         * @param bucket the S3 bucket name
         * @param key    the S3 object key
         */
        public S3ObjectReference {
            Objects.requireNonNull(
                    bucket,
                    "bucket must not be null");
            Objects.requireNonNull(
                    key,
                    "key must not be null");
        }
    }
}
