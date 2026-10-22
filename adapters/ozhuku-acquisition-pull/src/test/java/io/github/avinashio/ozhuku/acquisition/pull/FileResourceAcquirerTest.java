package io.github.avinashio.ozhuku.acquisition.pull;

import io.github.avinashio.ozhuku.domain.identity.ResourceId;
import io.github.avinashio.ozhuku.domain.resource.Resource;
import io.github.avinashio.ozhuku.domain.resource.ResourceLocation;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileResourceAcquirerTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldStreamFileContent() throws IOException {

        final Path file = tempDir.resolve("input.txt");
        final byte[] content = "hello-world".getBytes(StandardCharsets.UTF_8);
        Files.write(file, content);

        final FileResourceAcquirer acquirer = new FileResourceAcquirer();

        final Resource resource = new Resource(
                new ResourceId("file-1"),
                new ResourceLocation("file:" + file.toUri().getPath()));

        try (InputStream input = acquirer.acquire(resource)) {
            assertNotNull(input);
            assertArrayEquals(
                    content,
                    input.readAllBytes());
        }
    }

    @Test
    void shouldRejectNonFileScheme() {

        final FileResourceAcquirer acquirer = new FileResourceAcquirer();

        final Resource resource = new Resource(
                new ResourceId("file-1"),
                new ResourceLocation("s3://bucket/key"));

        assertThrows(
                IllegalArgumentException.class,
                () -> acquirer.acquire(resource));
    }

    @Test
    void shouldRejectNullResource() {

        final FileResourceAcquirer acquirer = new FileResourceAcquirer();

        assertThrows(
                NullPointerException.class,
                () -> acquirer.acquire(null));
    }
}
