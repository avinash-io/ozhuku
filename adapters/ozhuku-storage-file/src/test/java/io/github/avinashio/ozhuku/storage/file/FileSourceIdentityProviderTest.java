package io.github.avinashio.ozhuku.storage.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.domain.identity.SourceIdentity;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.domain.resource.ResourceLocation;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileSourceIdentityProviderTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void shouldIdentifyExistingFile() throws IOException {
        final Path root =
                temporaryDirectory.resolve("storage");

        Files.createDirectories(root);

        final Path file =
                root.resolve("input.txt");

        Files.writeString(
                file,
                "ozhuku-identity-test");

        final FileSourceIdentityProvider provider =
                new FileSourceIdentityProvider(
                        new FileStoragePathResolver(root));

        final Resource resource =
                new Resource(
                        new ResourceId("resource-1"),
                        new ResourceLocation(
                                file.toUri().toString()));

        final SourceIdentity result =
                provider.identify(resource);

        assertEquals(
                file.toAbsolutePath()
                        .normalize()
                        .toString(),
                result.value());
    }

    @Test
    void shouldRejectMissingFile() {
        final Path root =
                temporaryDirectory.resolve("storage");

        final FileSourceIdentityProvider provider =
                new FileSourceIdentityProvider(
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
                () -> provider.identify(resource));
    }

    @Test
    void shouldRejectNullResource() {
        final FileSourceIdentityProvider provider =
                new FileSourceIdentityProvider(
                        new FileStoragePathResolver(
                                temporaryDirectory));

        assertThrows(
                NullPointerException.class,
                () -> provider.identify(null));
    }

    @Test
    void shouldRejectFileOutsideRoot() {
        final Path root =
                temporaryDirectory.resolve("storage");

        final FileSourceIdentityProvider provider =
                new FileSourceIdentityProvider(
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
                () -> provider.identify(resource));
    }
}
