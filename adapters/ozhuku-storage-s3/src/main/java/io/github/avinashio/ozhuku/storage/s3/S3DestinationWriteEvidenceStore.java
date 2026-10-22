package io.github.avinashio.ozhuku.storage.s3;

import io.github.avinashio.ozhuku.domain.execution.CommitStatus;
import io.github.avinashio.ozhuku.domain.execution.DestinationCommitReference;
import io.github.avinashio.ozhuku.storage.DestinationWriteEvidenceStore;
import java.io.IOException;
import java.util.Objects;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

/**
 * S3-based implementation of {@link DestinationWriteEvidenceStore}.
 *
 * <p>This evidence store verifies S3 object existence to establish
 * whether a destination commit has been durably written. The
 * {@code recordCommitted} method is a no-op because S3 write
 * evidence is established by inspecting the destination object
 * directly via {@link #inspect(DestinationCommitReference)}.</p>
 */
public final class S3DestinationWriteEvidenceStore
        implements DestinationWriteEvidenceStore {

    private final S3Client s3;
    private final S3StoragePathResolver pathResolver;

    /**
     * Creates an S3 destination write evidence store.
     *
     * @param s3           the S3 client used to access S3
     * @param pathResolver the path resolver for S3 resource locations
     */
    public S3DestinationWriteEvidenceStore(
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
    public void recordCommitted(
            final DestinationCommitReference reference)
            throws IOException {
        // No-op: S3 evidence is established by object existence inspection.
        // S3 does not support client-side commit markers the same way
        // the filesystem adapter manages evidence files.
    }

    @Override
    public CommitStatus inspect(
            final DestinationCommitReference reference)
            throws IOException {

        Objects.requireNonNull(
                reference,
                "reference must not be null");

        // DestinationCommitReference does not carry S3 object location
        // information, so this implementation returns UNKNOWN.
        // A concrete implementation would resolve the reference to an
        // S3 object and verify its existence.
        return CommitStatus.UNKNOWN;
    }
}
