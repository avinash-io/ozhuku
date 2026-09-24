package io.github.avinashio.ozhuku.storage.file;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.avinashio.ozhuku.domain.delivery.ConflictBehavior;
import io.github.avinashio.ozhuku.domain.delivery.DeliveryPolicy;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.domain.resource.ResourceLocation;
import io.github.avinashio.ozhuku.storage.StorageOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileStorageOutputProviderTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void shouldWriteNewDestinationAtomically() throws Exception {
        final FileStorageOutputProvider provider = provider();
        final Resource destination = resource("output.csv");

        try (StorageOutput output = provider.open(
                destination,
                policy(ConflictBehavior.FAIL))) {
            output.stream().write("hello".getBytes(StandardCharsets.UTF_8));
        }

        final Path target = temporaryDirectory.resolve("output.csv");

        assertArrayEquals(
                "hello".getBytes(StandardCharsets.UTF_8),
                Files.readAllBytes(target));
    }

    @Test
    void shouldReplaceExistingDestination() throws Exception {
        final Path target = temporaryDirectory.resolve("output.csv");
        Files.writeString(target, "old");

        final FileStorageOutputProvider provider = provider();

        try (StorageOutput output = provider.open(
                resource("output.csv"),
                policy(ConflictBehavior.REPLACE))) {
            output.stream().write("new".getBytes(StandardCharsets.UTF_8));
        }

        assertEquals("new", Files.readString(target));
    }

    @Test
    void shouldFailWhenDestinationExists() throws Exception {
        final Path target = temporaryDirectory.resolve("output.csv");
        Files.writeString(target, "existing");

        final FileStorageOutputProvider provider = provider();

        assertThrows(
                IOException.class,
                () -> provider.open(
                        resource("output.csv"),
                        policy(ConflictBehavior.FAIL)));

        assertEquals("existing", Files.readString(target));
    }

    @Test
    void shouldSkipExistingDestination() throws Exception {
        final Path target = temporaryDirectory.resolve("output.csv");
        Files.writeString(target, "existing");

        final FileStorageOutputProvider provider = provider();

        try (StorageOutput output = provider.open(
                resource("output.csv"),
                policy(ConflictBehavior.SKIP))) {
            output.stream().write("replacement".getBytes(StandardCharsets.UTF_8));
        }

        assertEquals("existing", Files.readString(target));
    }

    @Test
    void shouldRejectUnsupportedVersionBehavior() throws Exception {
        final Path target = temporaryDirectory.resolve("output.csv");
        Files.writeString(target, "existing");

        final FileStorageOutputProvider provider = provider();

        assertThrows(
                UnsupportedOperationException.class,
                () -> provider.open(
                        resource("output.csv"),
                        policy(ConflictBehavior.VERSION)));
    }

    @Test
    void shouldRejectStreamAfterClose() throws Exception {
        final FileStorageOutputProvider provider = provider();

        final StorageOutput output = provider.open(
                resource("output.csv"),
                policy(ConflictBehavior.FAIL));

        output.close();

        assertThrows(
                IllegalStateException.class,
                output::stream);
    }

    @Test
    void shouldRemoveTemporaryFileAfterSuccessfulCommit() throws Exception {
        final FileStorageOutputProvider provider = provider();

        final StorageOutput output = provider.open(
                resource("output.csv"),
                policy(ConflictBehavior.FAIL));

        output.stream().write("partial".getBytes(StandardCharsets.UTF_8));
        output.close();

        final Path target = temporaryDirectory.resolve("output.csv");

        assertEquals("partial", Files.readString(target));

        try (Stream<Path> files = Files.list(temporaryDirectory)) {
            assertEquals(
                    1,
                    files.count(),
                    "Only the committed destination should remain");
        }
    }

    private FileStorageOutputProvider provider() {
        return new FileStorageOutputProvider(
                new FileStoragePathResolver(temporaryDirectory));
    }

    private Resource resource(final String path) {
        final Path resourcePath = temporaryDirectory.resolve(path);

        return new Resource(
                new io.github.avinashio.ozhuku.domain.identity.ResourceId(path),
                new ResourceLocation(resourcePath.toUri().toString()));
    }

    private DeliveryPolicy policy(final ConflictBehavior behavior) {
        return new DeliveryPolicy(behavior);
    }
}