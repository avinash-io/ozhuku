
package io.github.avinashio.ozhuku.storage.file;

import io.github.avinashio.ozhuku.domain.delivery.ConflictBehavior;
import io.github.avinashio.ozhuku.domain.delivery.DeliveryPolicy;
import io.github.avinashio.ozhuku.domain.execution.DestinationCommitReference;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.storage.DestinationWriteEvidenceStore;
import io.github.avinashio.ozhuku.storage.StorageOutput;
import io.github.avinashio.ozhuku.storage.StorageOutputProvider;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

public final class FileStorageOutputProvider
        implements StorageOutputProvider {

    private final FileStoragePathResolver pathResolver;
    private final DestinationWriteEvidenceStore evidenceStore;

    public FileStorageOutputProvider(
            final FileStoragePathResolver pathResolver) {
        this(pathResolver, null);
    }

    public FileStorageOutputProvider(
            final FileStoragePathResolver pathResolver,
            final DestinationWriteEvidenceStore evidenceStore) {
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

        final Path target =
                pathResolver.resolveForWrite(destination.location());

        final ConflictBehavior conflictBehavior =
                deliveryPolicy.conflictBehavior();

        if (Files.exists(target)) {
            switch (conflictBehavior) {
                case FAIL:
                    throw new IOException(
                            "Destination already exists: " + target);
                case SKIP:
                    return new SkippingStorageOutput();
                case VERSION:
                    throw new UnsupportedOperationException(
                            "VERSION conflict behavior is not supported "
                                    + "by the file storage output provider");
                case REPLACE:
                    break;
                default:
                    throw new IllegalStateException(
                            "Unsupported conflict behavior: "
                                    + conflictBehavior);
            }
        }

        final Path parent = target.getParent();

        if (parent == null) {
            throw new IOException(
                    "Destination parent directory is missing");
        }

        Files.createDirectories(parent);

        final Path temporaryFile = Files.createTempFile(
                parent,
                ".ozhuku-",
                ".tmp");

        try {
            final OutputStream outputStream = Files.newOutputStream(
                    temporaryFile,
                    StandardOpenOption.WRITE);

            return new AtomicFileStorageOutput(
                    target,
                    temporaryFile,
                    outputStream,
                    evidenceStore,
                    reference);
        } catch (IOException exception) {
            Files.deleteIfExists(temporaryFile);
            throw exception;
        }
    }

    private static final class AtomicFileStorageOutput
            implements StorageOutput {

        private final Path target;
        private final Path temporaryFile;
        private final OutputStream outputStream;
        private final DestinationWriteEvidenceStore evidenceStore;
        private final DestinationCommitReference reference;

        private boolean closed;
        private boolean committed;

        private AtomicFileStorageOutput(
                final Path target,
                final Path temporaryFile,
                final OutputStream outputStream,
                final DestinationWriteEvidenceStore evidenceStore,
                final DestinationCommitReference reference) {
            this.target = target;
            this.temporaryFile = temporaryFile;
            this.outputStream = outputStream;
            this.evidenceStore = evidenceStore;
            this.reference = reference;
        }

        @Override
        public OutputStream stream() {
            ensureOpen();
            return outputStream;
        }

        @Override
        public void commit() throws IOException {
            ensureOpen();

            if (committed) {
                throw new IllegalStateException(
                        "Storage output has already been committed");
            }

            IOException failure = null;

            try {
                outputStream.close();
            } catch (IOException exception) {
                failure = exception;
            }

            if (failure == null) {
                try {
                    Files.move(
                            temporaryFile,
                            target,
                            StandardCopyOption.ATOMIC_MOVE,
                            StandardCopyOption.REPLACE_EXISTING);
                    committed = true;
                } catch (IOException exception) {
                    failure = exception;
                }
            }

            if (!committed) {
                cleanupTemporaryFile(failure);
            }

            if (failure != null) {
                throw failure;
            }

            if (reference != null) {
                evidenceStore.recordCommitted(reference);
            }
        }

        @Override
        public void close() throws IOException {
            if (closed) {
                return;
            }

            closed = true;

            IOException failure = null;

            try {
                outputStream.close();
            } catch (IOException exception) {
                failure = exception;
            }

            if (!committed) {
                try {
                    Files.deleteIfExists(temporaryFile);
                } catch (IOException cleanupException) {
                    if (failure == null) {
                        failure = cleanupException;
                    } else {
                        failure.addSuppressed(cleanupException);
                    }
                }
            }

            if (failure != null) {
                throw failure;
            }
        }

        private void cleanupTemporaryFile(
                final IOException failure)
                throws IOException {
            try {
                Files.deleteIfExists(temporaryFile);
            } catch (IOException cleanupException) {
                if (failure == null) {
                    throw cleanupException;
                }

                failure.addSuppressed(cleanupException);
            }
        }

        private void ensureOpen() {
            if (closed) {
                throw new IllegalStateException(
                        "Storage output is already closed");
            }
        }
    }

    private static final class SkippingStorageOutput
            implements StorageOutput {

        private boolean closed;

        @Override
        public OutputStream stream() {
            if (closed) {
                throw new IllegalStateException(
                        "Storage output is already closed");
            }

            return OutputStream.nullOutputStream();
        }

        @Override
        public void commit() {
            if (closed) {
                throw new IllegalStateException(
                        "Storage output is already closed");
            }
        }

        @Override
        public void close() {
            closed = true;
        }
    }
}
