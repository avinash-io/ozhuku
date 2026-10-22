package io.github.avinashio.ozhuku.storage.s3;

import io.github.avinashio.ozhuku.domain.delivery.ConflictBehavior;
import io.github.avinashio.ozhuku.domain.execution.DestinationCommitReference;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.storage.DestinationWriteEvidenceStore;
import io.github.avinashio.ozhuku.storage.StorageWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Writes resources to Amazon S3.
 *
 * <p>This writer streams content directly to S3 via a {@code PutObject}
 * request, so memory usage stays bounded regardless of object size.
 * The writer supports conflict behavior handling:
 * <ul>
 *   <li>{@code FAIL} - throws if the object already exists</li>
 *   <li>{@code REPLACE} - overwrites the existing object</li>
 *   <li>{@code SKIP} - silently skips the write if the object exists</li>
 *   <li>{@code VERSION} - not supported, throws {@link
 *       UnsupportedOperationException}</li>
 * </ul></p>
 */
public final class S3StorageWriter implements StorageWriter {

    private final S3Client s3;
    private final S3StoragePathResolver pathResolver;
    private final DestinationWriteEvidenceStore evidenceStore;

    /**
     * Creates an S3 storage writer without an evidence store.
     *
     * @param s3           the S3 client used to access S3
     * @param pathResolver the path resolver for S3 resource locations
     */
    public S3StorageWriter(
            final S3Client s3,
            final S3StoragePathResolver pathResolver) {
        this(s3, pathResolver, null);
    }

    /**
     * Creates an S3 storage writer with an evidence store.
     *
     * @param s3           the S3 client used to access S3
     * @param pathResolver the path resolver for S3 resource locations
     * @param evidenceStore the destination write evidence store
     */
    public S3StorageWriter(
            final S3Client s3,
            final S3StoragePathResolver pathResolver,
            final DestinationWriteEvidenceStore evidenceStore) {

        this.s3 = Objects.requireNonNull(
                s3,
                "s3 must not be null");
        this.pathResolver = Objects.requireNonNull(
                pathResolver,
                "pathResolver must not be null");
        this.evidenceStore = evidenceStore;
    }

    @Override
    public void write(
            final Resource destination,
            final InputStream content,
            final ConflictBehavior conflictBehavior)
            throws IOException {
        writeInternal(destination, content, conflictBehavior, null);
    }

    @Override
    public void write(
            final Resource destination,
            final InputStream content,
            final ConflictBehavior conflictBehavior,
            final DestinationCommitReference reference)
            throws IOException {

        Objects.requireNonNull(
                reference,
                "reference must not be null");

        if (evidenceStore == null) {
            throw new IllegalStateException(
                    "Destination write evidence store is not configured");
        }

        writeInternal(destination, content, conflictBehavior, reference);
    }

    private void writeInternal(
            final Resource destination,
            final InputStream content,
            final ConflictBehavior conflictBehavior,
            final DestinationCommitReference reference)
            throws IOException {

        Objects.requireNonNull(
                destination,
                "destination must not be null");
        Objects.requireNonNull(
                content,
                "content must not be null");
        Objects.requireNonNull(
                conflictBehavior,
                "conflictBehavior must not be null");

        final S3StoragePathResolver.S3ObjectReference target =
                pathResolver.resolve(destination.location());

        if (shouldSkip(target, conflictBehavior)) {
            return;
        }

        final PutObjectRequest request =
                PutObjectRequest.builder()
                        .bucket(target.bucket())
                        .key(target.key())
                        .build();

        try {
            s3.putObject(request, content);
        } catch (final SdkException exception) {
            throw new IOException(
                    "Failed to write S3 object: "
                            + target.bucket()
                            + "/"
                            + target.key(),
                    exception);
        }

        if (reference != null) {
            if (evidenceStore == null) {
                throw new IllegalStateException(
                        "Destination write evidence store is not configured");
            }
            try {
                evidenceStore.recordCommitted(reference);
            } catch (final IOException exception) {
                throw new IOException(
                        "Failed to record destination commit evidence "
                                + "for "
                                + target.bucket()
                                + "/"
                                + target.key(),
                        exception);
            }
        }
    }

        private boolean shouldSkip(
            final S3StoragePathResolver.S3ObjectReference target,
            final ConflictBehavior conflictBehavior)
            throws IOException {

        if (conflictBehavior == ConflictBehavior.VERSION) {
            throw new UnsupportedOperationException(
                    "VERSION conflict behavior is not "
                            + "supported by the S3 storage "
                            + "adapter yet");
        }

        if (conflictBehavior == ConflictBehavior.FAIL
                || conflictBehavior == ConflictBehavior.SKIP) {

            final HeadObjectRequest headRequest =
                    HeadObjectRequest.builder()
                            .bucket(target.bucket())
                            .key(target.key())
                            .build();

            try {
                s3.headObject(headRequest);

                if (conflictBehavior == ConflictBehavior.FAIL) {
                    throw new IOException(
                            "Destination already exists: "
                                    + target.bucket()
                                    + "/"
                                    + target.key());
                }

                if (conflictBehavior == ConflictBehavior.SKIP) {
                    return true;
                }
            } catch (final NoSuchKeyException exception) {
                // Object does not exist, proceed with write
                return false;
            } catch (final SdkException exception) {
                throw new IOException(
                        "Failed to check S3 object existence: "
                                + target.bucket()
                                + "/"
                                + target.key(),
                        exception);
            }
        }

        return false;
    }
}
