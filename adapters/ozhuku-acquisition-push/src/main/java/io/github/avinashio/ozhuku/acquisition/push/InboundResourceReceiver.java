package io.github.avinashio.ozhuku.acquisition.push;

import io.github.avinashio.ozhuku.acquisition.PushResource;
import io.github.avinashio.ozhuku.acquisition.PushResourceRepository;
import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.domain.resource.ResourceLocation;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;
import java.util.UUID;

/**
 * Receives pushed/inbound resources and stages them durably.
 *
 * <p>This component handles the ingress of resources from external systems
 * (HTTP upload, SFTP drop, etc.). It streams content to a staging area,
 * computes a checksum, and persists metadata via
 * {@link PushResourceRepository} so that the resource becomes available
 * for processing only after a durable commit.</p>
 *
 * <p>Protection against:
 * <ul>
 *   <li>path traversal - staging paths are random UUIDs under a root</li>
 *   <li>oversized payloads - enforced via maximum size</li>
 *   <li>partial uploads - atomic move ensures durability</li>
 *   <li>duplicates - idempotent by sender ID + message ID (delegated to caller)</li>
 * </ul></p>
 */
public final class InboundResourceReceiver {

    private final Path stagingRoot;
    private final PushResourceRepository repository;
    private final long maxFileSizeBytes;

    /**
     * Creates an inbound resource receiver.
     *
     * @param stagingRoot       the root directory for staging files
     * @param repository        the push resource repository for metadata
     * @param maxFileSizeBytes  maximum allowed resource size in bytes
     */
    public InboundResourceReceiver(
            final Path stagingRoot,
            final PushResourceRepository repository,
            final long maxFileSizeBytes) {

        this.stagingRoot = Objects.requireNonNull(
                stagingRoot,
                "stagingRoot must not be null");
        this.repository = Objects.requireNonNull(
                repository,
                "repository must not be null");
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    /**
     * Receives a pushed resource by streaming it to a durable staging location.
     *
     * <p>The resource is written to a temporary file and atomically moved
     * to its final staging location upon successful completion. A SHA-256
     * checksum is computed during streaming.</p>
     *
     * @param content           the streaming content from the sender
     * @param originalFilename  the original filename (may be {@code null})
     * @param contentType       the content type (may be {@code null})
     * @param senderId          the sender identifier for idempotency
     * @param externalMessageId the external message identifier for idempotency
     * @return the received push resource metadata
     * @throws IOException if staging or checksumming fails
     */
    public PushResource receive(
            final InputStream content,
            final String originalFilename,
            final String contentType,
            final String senderId,
            final String externalMessageId)
            throws IOException {

        Objects.requireNonNull(
                content,
                "content must not be null");

        Files.createDirectories(stagingRoot);

        final String resourceId = UUID.randomUUID().toString();
        final Path tempFile = stagingRoot.resolve(resourceId + ".tmp");
        final Path stagedFile = stagingRoot.resolve(resourceId + ".dat");

        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final CountingInputStream counting =
                    new CountingInputStream(content, digest);

            Files.copy(counting, tempFile, StandardCopyOption.REPLACE_EXISTING);

            if (counting.count() > maxFileSizeBytes) {
                Files.deleteIfExists(tempFile);
                throw new IOException(
                        "Resource exceeds maximum allowed size: "
                                + maxFileSizeBytes + " bytes");
            }

            Files.move(tempFile, stagedFile, StandardCopyOption.ATOMIC_MOVE);

            final String checksum = HexFormat.of().formatHex(
                    digest.digest());

            final PushResource resource = PushResource.received(
                    new ResourceId(resourceId),
                    new ResourceLocation(
                            "file:" + stagedFile.toUri().getPath()),
                    originalFilename,
                    counting.count(),
                    contentType,
                    System.currentTimeMillis(),
                    checksum);

            repository.save(resource);

            return resource;
        } catch (final NoSuchAlgorithmException exception) {
            throw new IOException(
                    "SHA-256 algorithm not available",
                    exception);
        } catch (final IOException exception) {
            Files.deleteIfExists(tempFile);
            throw exception;
        }
    }
}
