package io.github.avinashio.ozhuku.storage.s3;

import io.github.avinashio.ozhuku.domain.delivery.ConflictBehavior;
import io.github.avinashio.ozhuku.domain.delivery.DeliveryPolicy;
import io.github.avinashio.ozhuku.domain.execution.DestinationCommitReference;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.storage.DestinationWriteEvidenceStore;
import io.github.avinashio.ozhuku.storage.StorageOutput;
import io.github.avinashio.ozhuku.storage.StorageOutputProvider;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Provides storage outputs for writing to Amazon S3.
 *
 * <p>The output provider opens an S3 destination for writing and supports
 * two-phase commit semantics: content is buffered in memory until commit,
 * at which point a single {@code PutObject} request is issued. This
 * mirrors the file storage adapter's transactional pattern.</p>
 */
public final class S3StorageOutputProvider
        implements StorageOutputProvider {

    private final S3Client s3;
    private final S3StoragePathResolver pathResolver;
    private final DestinationWriteEvidenceStore evidenceStore;

    /**
     * Creates an S3 storage output provider without an evidence store.
     *
     * @param s3           the S3 client used to access S3
     * @param pathResolver the path resolver for S3 resource locations
     */
    public S3StorageOutputProvider(
            final S3Client s3,
            final S3StoragePathResolver pathResolver) {
        this(s3, pathResolver, null);
    }

    /**
     * Creates an S3 storage output provider with an evidence store.
     *
     * @param s3           the S3 client used to access S3
     * @param pathResolver the path resolver for S3 resource locations
     * @param evidenceStore the destination write evidence store
     */
    public S3StorageOutputProvider(
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
    public StorageOutput open(
            final Resource destination,
            final DeliveryPolicy deliveryPolicy)
            throws IOException {
        return openInternal(destination, deliveryPolicy, null);
    }

    @Override
    public StorageOutput open(
            final Resource destination,
            final DeliveryPolicy deliveryPolicy,
            final DestinationCommitReference reference)
            throws IOException {

        Objects.requireNonNull(
                reference,
                "reference must not be null");

        if (evidenceStore == null) {
            throw new IllegalStateException(
                    "Destination write evidence store is not configured");
        }

        return openInternal(destination, deliveryPolicy, reference);
    }

    private StorageOutput openInternal(
            final Resource destination,
            final DeliveryPolicy deliveryPolicy,
            final DestinationCommitReference reference)
            throws IOException {

        Objects.requireNonNull(
                destination,
                "destination must not be null");
        Objects.requireNonNull(
                deliveryPolicy,
                    "deliveryPolicy must not be null");

        final S3StoragePathResolver.S3ObjectReference target =
                pathResolver.resolve(destination.location());

        final ConflictBehavior conflictBehavior =
                deliveryPolicy.conflictBehavior();

        if (conflictBehavior == ConflictBehavior.VERSION) {
            throw new UnsupportedOperationException(
                    "VERSION conflict behavior is not "
                            + "supported by the S3 storage "
                            + "output provider");
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
                    return new DiscardingStorageOutput();
                }
            } catch (final NoSuchKeyException exception) {
                // Object does not exist, proceed with write
            } catch (final SdkException exception) {
                throw new IOException(
                        "Failed to check S3 object existence: "
                                + target.bucket()
                                + "/"
                                + target.key(),
                        exception);
            }
        }

        return new S3StreamingStorageOutput(
                target,
                s3,
                evidenceStore,
                reference);
    }

    /**
     * Storage output that streams content to S3 on commit.
     */
    private static final class S3StreamingStorageOutput
            implements StorageOutput {

        private final S3StoragePathResolver.S3ObjectReference target;
        private final S3Client s3;
        private final DestinationWriteEvidenceStore evidenceStore;
        private final DestinationCommitReference reference;

        private final ByteArrayOutputStream buffer;
        private boolean closed;
        private boolean committed;

        S3StreamingStorageOutput(
                final S3StoragePathResolver.S3ObjectReference target,
                final S3Client s3,
                final DestinationWriteEvidenceStore evidenceStore,
                final DestinationCommitReference reference) {

            this.target = Objects.requireNonNull(
                    target,
                    "target must not be null");
            this.s3 = Objects.requireNonNull(
                    s3,
                    "s3 must not be null");
            this.evidenceStore = evidenceStore;
            this.reference = reference;
            this.buffer = new ByteArrayOutputStream();
        }

        @Override
        public OutputStream stream() {
            ensureOpen();
            return buffer;
        }

        @Override
        public void commit() throws IOException {
            ensureOpen();

            if (committed) {
                throw new IllegalStateException(
                        "Storage output has already been committed");
            }

            final byte[] data = buffer.toByteArray();

            final PutObjectRequest request =
                    PutObjectRequest.builder()
                            .bucket(target.bucket())
                            .key(target.key())
                            .build();

            try {
                s3.putObject(
                        request,
                        new ByteArrayInputStream(data));
                committed = true;
            } catch (final SdkException exception) {
                throw new IOException(
                        "Failed to write S3 object: "
                                + target.bucket()
                                + "/"
                                + target.key(),
                        exception);
            }

            if (reference != null && evidenceStore != null) {
                try {
                    evidenceStore.recordCommitted(reference);
                } catch (final IOException exception) {
                    throw new IOException(
                            "Failed to record destination commit "
                                    + "evidence for "
                                    + target.bucket()
                                    + "/"
                                    + target.key(),
                            exception);
                }
            }
        }

        @Override
        public void close() throws IOException {
            if (closed) {
                return;
            }

            closed = true;

            if (!committed) {
                buffer.reset();
            }
        }

        private void ensureOpen() {
            if (closed) {
                throw new IllegalStateException(
                        "Storage output is already closed");
            }
        }
    }

    /**
     * Storage output that discards all writes (used for SKIP behavior).
     */
    private static final class DiscardingStorageOutput
            implements StorageOutput {

        private boolean closed;
        private boolean committed;

        @Override
        public OutputStream stream() {
            if (closed) {
                throw new IllegalStateException(
                        "Storage output is already closed");
            }
            return new OutputStream() {
                @Override
                public void write(final int b) {
                    // Discard all output
                }
            };
        }

        @Override
        public void commit() {
            if (closed) {
                throw new IllegalStateException(
                        "Storage output is already closed");
            }
            committed = true;
        }

        @Override
        public void close() {
            closed = true;
        }
    }
}
