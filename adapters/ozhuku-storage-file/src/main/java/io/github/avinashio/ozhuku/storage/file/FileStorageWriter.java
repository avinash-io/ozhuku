package io.github.avinashio.ozhuku.storage.file;

import io.github.avinashio.ozhuku.domain.delivery.ConflictBehavior;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.storage.StorageWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public final class FileStorageWriter implements StorageWriter {

    private static final String TEMPORARY_FILE_PREFIX =
            ".ozhuku-transfer-";

    private static final String TEMPORARY_FILE_SUFFIX =
            ".tmp";

    private final FileStoragePathResolver pathResolver;

    public FileStorageWriter(
            final FileStoragePathResolver pathResolver) {
        this.pathResolver = Objects.requireNonNull(
                pathResolver,
                "pathResolver must not be null");
    }

    @Override
    public void write(
            final Resource destination,
            final InputStream content,
            final ConflictBehavior conflictBehavior)
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

        final Path destinationPath =
                pathResolver.resolveForWrite(
                        destination.location());

        switch (conflictBehavior) {
            case FAIL:
                if (Files.exists(destinationPath)) {
                    throw new IOException(
                            "Destination already exists: "
                                    + destinationPath);
                }
                break;

            case REPLACE:
                break;

            case SKIP:
                if (Files.exists(destinationPath)) {
                    return;
                }
                break;

            case VERSION:
                throw new UnsupportedOperationException(
                        "VERSION conflict behavior is not "
                                + "supported by the file storage "
                                + "adapter yet");

            default:
                throw new IllegalArgumentException(
                        "Unsupported conflict behavior: "
                                + conflictBehavior);
        }

        final Path parent =
                destinationPath.getParent();

        if (parent == null) {
            throw new IOException(
                    "Destination parent directory is missing");
        }

        final Path temporaryPath =
                Files.createTempFile(
                        parent,
                        TEMPORARY_FILE_PREFIX,
                        TEMPORARY_FILE_SUFFIX);

        boolean moved = false;

        try {
            try (OutputStream output =
                         Files.newOutputStream(temporaryPath)) {

                content.transferTo(output);
            }

            try {
                Files.move(
                        temporaryPath,
                        destinationPath,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);

                moved = true;
            } catch (final AtomicMoveNotSupportedException exception) {
                throw new IOException(
                        "Atomic move is not supported "
                                + "by the destination filesystem",
                        exception);
            }
        } finally {
            if (!moved) {
                Files.deleteIfExists(temporaryPath);
            }
        }
    }
}