package io.github.avinashio.ozhuku.storage.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.domain.identity.SourceFingerprint;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.domain.resource.ResourceLocation;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileSourceFingerprintProviderTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void shouldCreateFingerprintForExistingFile()
            throws IOException {

        final Path root =
                temporaryDirectory.resolve("storage");

        Files.createDirectories(root);

        final Path file =
                root.resolve("input.txt");

        Files.writeString(
                file,
                "ozhuku-fingerprint-test");

        final FileSourceFingerprintProvider provider =
                new FileSourceFingerprintProvider(
                        new FileStoragePathResolver(root));

        final Resource resource =
                new Resource(
                        new ResourceId("resource-1"),
                        new ResourceLocation(
                                file.toUri().toString()));

        final SourceFingerprint result =
                provider.fingerprint(resource);

        final String expected =
                file.toAbsolutePath()
                        .normalize()
                        + "|"
                        + Files.size(file)
                        + "|"
                        + Files.getLastModifiedTime(file)
                        .toMillis();

        assertEquals(
                new SourceFingerprint(expected),
                result);
    }

    @Test
    void shouldReturnSameFingerprintWhenFileStateIsUnchanged()
            throws IOException {

        final Path root =
                temporaryDirectory.resolve("storage");

        Files.createDirectories(root);

        final Path file =
                root.resolve("input.txt");

        Files.writeString(
                file,
                "ozhuku-fingerprint-test");

        final FileSourceFingerprintProvider provider =
                new FileSourceFingerprintProvider(
                        new FileStoragePathResolver(root));

        final Resource resource =
                new Resource(
                        new ResourceId("resource-1"),
                        new ResourceLocation(
                                file.toUri().toString()));

        final SourceFingerprint first =
                provider.fingerprint(resource);

        final SourceFingerprint second =
                provider.fingerprint(resource);

        assertEquals(
                first,
                second);
    }

    @Test
    void shouldChangeFingerprintWhenFileSizeChanges()
            throws IOException {

        final Path root =
                temporaryDirectory.resolve("storage");

        Files.createDirectories(root);

        final Path file =
                root.resolve("input.txt");

        Files.writeString(
                file,
                "original");

        final FileSourceFingerprintProvider provider =
                new FileSourceFingerprintProvider(
                        new FileStoragePathResolver(root));

        final Resource resource =
                new Resource(
                        new ResourceId("resource-1"),
                        new ResourceLocation(
                                file.toUri().toString()));

        final SourceFingerprint first =
                provider.fingerprint(resource);

        Files.writeString(
                file,
                "changed-content-with-different-size");

        final SourceFingerprint second =
                provider.fingerprint(resource);

        assertNotEquals(
                first,
                second);
    }

    @Test
    void shouldChangeFingerprintWhenLastModifiedTimeChanges()
            throws IOException {

        final Path root =
                temporaryDirectory.resolve("storage");

        Files.createDirectories(root);

        final Path file =
                root.resolve("input.txt");

        Files.writeString(
                file,
                "same-content");

        final FileSourceFingerprintProvider provider =
                new FileSourceFingerprintProvider(
                        new FileStoragePathResolver(root));

        final Resource resource =
                new Resource(
                        new ResourceId("resource-1"),
                        new ResourceLocation(
                                file.toUri().toString()));

        final SourceFingerprint first =
                provider.fingerprint(resource);

        final FileTime originalTime =
                Files.getLastModifiedTime(file);

        Files.setLastModifiedTime(
                file,
                FileTime.fromMillis(
                        originalTime.toMillis() + 10_000));

        final SourceFingerprint second =
                provider.fingerprint(resource);

        assertNotEquals(
                first,
                second);
    }

    @Test
    void shouldRejectMissingFile() {
        final Path root =
                temporaryDirectory.resolve("storage");

        final FileSourceFingerprintProvider provider =
                new FileSourceFingerprintProvider(
                        new FileStoragePathResolver(root));

        final Path missingFile =
                root.resolve("missing.txt");

        final Resource resource =
                new Resource(
                        new ResourceId("resource-1"),
                        new ResourceLocation(
                                missingFile.toUri().toString()));

        assertThrows(
                IOException.class,
                () -> provider.fingerprint(resource));
    }

    @Test
    void shouldRejectNullResource() {
        final FileSourceFingerprintProvider provider =
                new FileSourceFingerprintProvider(
                        new FileStoragePathResolver(
                                temporaryDirectory));

        assertThrows(
                NullPointerException.class,
                () -> provider.fingerprint(null));
    }

    @Test
    void shouldRejectFileOutsideRoot() {
        final Path root =
                temporaryDirectory.resolve("storage");

        final FileSourceFingerprintProvider provider =
                new FileSourceFingerprintProvider(
                        new FileStoragePathResolver(root));

        final Path outside =
                temporaryDirectory.resolve("outside.txt");

        final Resource resource =
                new Resource(
                        new ResourceId("resource-1"),
                        new ResourceLocation(
                                outside.toUri().toString()));

        assertThrows(
                IllegalArgumentException.class,
                () -> provider.fingerprint(resource));
    }
}
