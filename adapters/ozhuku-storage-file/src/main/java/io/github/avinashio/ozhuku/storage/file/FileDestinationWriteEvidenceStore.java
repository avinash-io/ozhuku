
package io.github.avinashio.ozhuku.storage.file;

import io.github.avinashio.ozhuku.domain.execution.CommitStatus;
import io.github.avinashio.ozhuku.domain.execution.DestinationCommitReference;
import io.github.avinashio.ozhuku.domain.execution.DestinationExecutionReference;
import io.github.avinashio.ozhuku.storage.DestinationWriteEvidenceStore;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

public final class FileDestinationWriteEvidenceStore
        implements DestinationWriteEvidenceStore {

    private static final String METADATA_DIRECTORY = ".ozhuku";
    private static final String EVIDENCE_DIRECTORY =
            "destination-evidence";
    private static final String TEMPORARY_PREFIX = ".evidence-";
    private static final String TEMPORARY_SUFFIX = ".tmp";

    private final Path storageRoot;

    public FileDestinationWriteEvidenceStore(final Path storageRoot) {
        this.storageRoot = Objects.requireNonNull(
                        storageRoot,
                        "storageRoot must not be null")
                .toAbsolutePath()
                .normalize();
    }

    @Override
    public void recordCommitted(
            final DestinationCommitReference reference)
            throws IOException {

        Objects.requireNonNull(
                reference,
                "reference must not be null");

        final Path directory = evidenceDirectory(true);
        final String evidence = evidenceKey(reference);
        final Path marker = directory.resolve(evidence + ".marker");
        final Path temporaryFile = Files.createTempFile(
                directory,
                TEMPORARY_PREFIX,
                TEMPORARY_SUFFIX);

        try {
            writeDurably(temporaryFile, evidence);

            try {
                Files.move(
                        temporaryFile,
                        marker,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                throw new IOException(
                        "Atomic publication of destination evidence "
                                + "is not supported",
                        exception);
            }
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    @Override
    public CommitStatus inspect(
            final DestinationCommitReference reference)
            throws IOException {

        Objects.requireNonNull(
                reference,
                "reference must not be null");

        final Path directory = evidenceDirectory(false);

        if (directory == null) {
            return CommitStatus.UNKNOWN;
        }

        final String evidence = evidenceKey(reference);
        final Path marker = directory.resolve(evidence + ".marker");

        if (Files.isSymbolicLink(marker)
                || !Files.isRegularFile(
                marker,
                LinkOption.NOFOLLOW_LINKS)) {
            return CommitStatus.UNKNOWN;
        }

        final String recordedEvidence =
                Files.readString(marker, StandardCharsets.UTF_8);

        if (!evidence.equals(recordedEvidence)) {
            return CommitStatus.UNKNOWN;
        }

        return CommitStatus.COMMITTED;
    }

    private Path evidenceDirectory(final boolean create)
            throws IOException {

        final Path realRoot = storageRoot.toRealPath();
        final Path metadataDirectory =
                realRoot.resolve(METADATA_DIRECTORY);

        if (!Files.exists(
                metadataDirectory,
                LinkOption.NOFOLLOW_LINKS)) {
            if (!create) {
                return null;
            }

            Files.createDirectory(metadataDirectory);
        }

        if (Files.isSymbolicLink(metadataDirectory)
                || !Files.isDirectory(
                metadataDirectory,
                LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException(
                    "Ozhuku metadata directory is not a safe directory");
        }

        final Path realMetadataDirectory =
                metadataDirectory.toRealPath();

        if (!realMetadataDirectory.startsWith(realRoot)) {
            throw new IOException(
                    "Ozhuku metadata directory resolves outside "
                            + "the storage root");
        }

        final Path directory =
                realMetadataDirectory.resolve(EVIDENCE_DIRECTORY);

        if (!Files.exists(directory, LinkOption.NOFOLLOW_LINKS)) {
            if (!create) {
                return null;
            }

            Files.createDirectory(directory);
        }

        if (Files.isSymbolicLink(directory)
                || !Files.isDirectory(
                directory,
                LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException(
                    "Destination evidence path is not a safe directory");
        }

        final Path realDirectory = directory.toRealPath();

        if (!realDirectory.startsWith(realRoot)) {
            throw new IOException(
                    "Destination evidence resolves outside "
                            + "the storage root");
        }

        return realDirectory;
    }

    private String evidenceKey(
            final DestinationCommitReference reference) {

        final DestinationExecutionReference destinationReference =
                reference.destinationExecutionReference();

        final String executionId =
                destinationReference.executionId().value();
        final String resourceId =
                destinationReference.resourceId().value();

        final String identity =
                executionId.length() + ":" + executionId
                        + resourceId.length() + ":" + resourceId;

        try {
            final MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            return HexFormat.of().formatHex(
                    digest.digest(
                            identity.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is unavailable",
                    exception);
        }
    }

    private void writeDurably(
            final Path file,
            final String evidence)
            throws IOException {

        final ByteBuffer buffer = ByteBuffer.wrap(
                evidence.getBytes(StandardCharsets.UTF_8));

        try (FileChannel channel = FileChannel.open(
                file,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING)) {

            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }

            channel.force(true);
        }
    }
}
